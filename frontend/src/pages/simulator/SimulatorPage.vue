<script setup>
// SCR-SIM-01 · 군적금 시뮬레이터  (담당: 석윤)
// 군적금 만기금 시뮬레이션 메인 + 모의 계산(바텀시트)
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import productApi from '@/api/productApi';
import simulatorApi from '@/api/simulatorApi';
import { formatManwon } from '@/util/format';
import BaseBottomSheet from '@/components/common/BaseBottomSheet.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import TabBar from '@/components/common/TabBar.vue';
import SavingTimeline from '@/components/common/SavingTimeline.vue';
import colliImg from '@/assets/images/colli.png';

const route = useRoute();
const router = useRouter();

const details = ref(null);
const isLoading = ref(true);
const hasNoAccount = ref(false);

const savingLoss = ref(null);

// 'real' = 실제 계좌 기준 상세내역 / 'simulated' = 모의 계산 결과로 대체된 상세내역
const viewMode = ref('real');
const simulatedResult = ref(null);
const simulateError = ref('');

const isCalcSheetOpen = ref(false);
// 'constant' = 동일 금액 매달 납입 / 'variable' = 구간별로 다른 금액 납입
const calcMode = ref('constant');
const monthlySave = ref('');
const saveMonths = ref('');
// 등록 완료된 구간 목록. 각 항목은 { startMonthOffset, endMonthOffset, amount }.
const periods = ref([]);
// 구간별 금액 모드에서 "등록" 누르기 전까지의 입력 중인 구간 — 시작월은 직전 구간
// 종료월+1로 자동 결정되므로(구간이 이어붙어야 함) 종료월만 입력받는다.
const periodDraft = ref({ endMonth: '', amount: '' });

// 기간 설정 드롭다운 선택지 (최대 가입기간 24개월, SIMUL_005와 동일한 한도)
const monthOptions = computed(() =>
  Array.from({ length: 24 }, (_, i) => ({
    value: i + 1,
    label: `${i + 1}개월`,
  })),
);

// 다음 구간의 시작월: 등록된 구간이 없으면 1개월, 있으면 마지막 구간 종료월+1.
const draftStartMonth = computed(() => {
  if (periods.value.length === 0) {
    return 1;
  }
  const lastEnd = Math.max(...periods.value.map((p) => p.endMonthOffset));
  return lastEnd + 1;
});

const canAddMorePeriods = computed(() => draftStartMonth.value <= 24);

const endMonthOptions = computed(() =>
  monthOptions.value.filter((opt) => opt.value >= draftStartMonth.value),
);

// 장병내일준비적금 한도: 월 납입 55만원 · 최대 24개월
const MAX_MONTHLY_MAN = 55;
const MAX_MONTHS = 24;

// 월 납입액 유효성 문구 (한도 초과 시 빨간 안내)
const monthlySaveError = computed(() => {
  if (monthlySave.value === '') return '';
  return Number(monthlySave.value) > MAX_MONTHLY_MAN
    ? `월 납입액은 최대 ${MAX_MONTHLY_MAN}만원까지 가능해요. 다시 입력해 주세요.`
    : '';
});

// 납입 개월 수 유효성 문구 (최대 24개월)
const saveMonthsError = computed(() => {
  if (saveMonths.value === '') return '';
  return Number(saveMonths.value) > MAX_MONTHS
    ? `납입 개월 수는 최대 ${MAX_MONTHS}개월까지 가능해요. 다시 입력해 주세요.`
    : '';
});

// 구간별 금액 유효성 문구 (동일 한도)
const periodAmountError = computed(() => {
  const amt = periodDraft.value.amount;
  if (amt === '' || amt == null) return '';
  return Number(amt) > MAX_MONTHLY_MAN
    ? `금액은 최대 ${MAX_MONTHLY_MAN}만원까지 가능해요.`
    : '';
});

const isDraftValid = computed(() => {
  const end = periodDraft.value.endMonth;
  const amt = Number(periodDraft.value.amount);
  return (
    end !== '' &&
    Number(end) >= draftStartMonth.value &&
    amt > 0 &&
    amt <= MAX_MONTHLY_MAN
  );
});

const isCalcFormValid = computed(() => {
  if (calcMode.value === 'constant') {
    const m = Number(monthlySave.value);
    const months = Number(saveMonths.value);
    return (
      m > 0 &&
      m <= MAX_MONTHLY_MAN &&
      saveMonths.value !== '' &&
      months > 0 &&
      months <= MAX_MONTHS
    );
  }
  return periods.value.length > 0;
});

// 실행 후 결과 카드로 스크롤하기 위한 ref
const simResultRef = ref(null);
// 되돌리기 시 적금 여정(타임라인) 시작점으로 스크롤하기 위한 ref
const timelineRef = ref(null);

// 모의 결과 → 실제 내역으로 되돌리기 (+ 타임라인 시작점으로 스크롤)
const resetToReal = () => {
  viewMode.value = 'real';
  simulatedResult.value = null;
  simulateError.value = '';
  nextTick(() => {
    timelineRef.value?.$el?.scrollIntoView({
      behavior: 'smooth',
      block: 'start',
    });
  });
};

// 개월차 구간의 개월 수 (양 끝 포함)
const monthDiff = (startMonthOffset, endMonthOffset) =>
  endMonthOffset - startMonthOffset + 1;

const fetchSavingDetails = async () => {
  isLoading.value = true;
  hasNoAccount.value = false;
  viewMode.value = 'real';
  try {
    details.value = await simulatorApi.findSavingDetails();
  } catch (error) {
    // 군적금 미가입(SIMUL_002) 등은 빈 상태로 처리
    details.value = null;
    hasNoAccount.value = true;
  } finally {
    isLoading.value = false;
  }
};

const fetchSavingLoss = async () => {
  try {
    savingLoss.value = await simulatorApi.findSavingLoss();
  } catch (error) {
    const code = error.response?.data?.code;
    // 군적금 미가입(SIMUL_002)·이미 전역(SIMUL_008)은 카드 자체를 숨긴다
    if (code !== 'SIMUL_002' && code !== 'SIMUL_008') {
      console.error(error);
    }
    savingLoss.value = null;
  }
};

const openCalcSheet = () => {
  simulateError.value = '';
  // 다시 들어올 때마다 입력값 전부 새로 초기화 (이전 계산값 남지 않게)
  calcMode.value = 'constant';
  monthlySave.value = '';
  saveMonths.value = '';
  periods.value = [];
  periodDraft.value = { endMonth: '', amount: '' };
  isCalcSheetOpen.value = true;
};

const registerPeriod = () => {
  if (!isDraftValid.value) return;
  periods.value.push({
    startMonthOffset: draftStartMonth.value,
    endMonthOffset: Number(periodDraft.value.endMonth),
    amount: Number(periodDraft.value.amount),
  });
  periodDraft.value = { endMonth: '', amount: '' };
};

// 등록된 구간 전체 + 입력 중인 구간을 함께 초기화한다.
const resetPeriods = () => {
  periods.value = [];
  periodDraft.value = { endMonth: '', amount: '' };
};

// 특정 구간 삭제 — 남은 구간은 기간·금액을 유지한 채 1개월부터 연속되게 재배치한다.
const removePeriod = (index) => {
  let cursor = 1;
  periods.value = periods.value
    .filter((_, i) => i !== index)
    .map((p) => {
      const duration = p.endMonthOffset - p.startMonthOffset + 1;
      const reseq = {
        startMonthOffset: cursor,
        endMonthOffset: cursor + duration - 1,
        amount: p.amount,
      };
      cursor += duration;
      return reseq;
    });
};

const runCalculation = async () => {
  if (!isCalcFormValid.value) {
    simulateError.value = '입력값을 모두 채워주세요.';
    return;
  }

  try {
    if (calcMode.value === 'constant') {
      // 입력은 만원 단위 → API/결과는 원 단위라 ×10000 변환
      const monthlyWon = Number(monthlySave.value) * 10000;
      const apiResult = await simulatorApi.calculateConstant({
        monthlySave: monthlyWon,
        saveMonths: Number(saveMonths.value),
      });
      simulatedResult.value = {
        monthlySaveTotal: monthlyWon,
        joinableMonths: Number(saveMonths.value),
        expectedPrincipal: apiResult.totalPrincipal,
        expectedInterest: apiResult.totalInterest,
        expectedMatchingFund: apiResult.totalMatchingFund,
        totalReceiptAmount: apiResult.totalReceiptAmount,
      };
    } else {
      const payload = periods.value.map((period) => ({
        startMonthOffset: period.startMonthOffset,
        endMonthOffset: period.endMonthOffset,
        amount: period.amount * 10000, // 만원 → 원
      }));
      const apiResult = await simulatorApi.calculateVariable(payload);
      const totalMonths = periods.value.reduce(
        (sum, period) =>
          sum + monthDiff(period.startMonthOffset, period.endMonthOffset),
        0,
      );
      simulatedResult.value = {
        monthlySaveTotal:
          totalMonths > 0
            ? Math.round(apiResult.totalPrincipal / totalMonths)
            : 0,
        joinableMonths: totalMonths,
        expectedPrincipal: apiResult.totalPrincipal,
        expectedInterest: apiResult.totalInterest,
        expectedMatchingFund: apiResult.totalMatchingFund,
        totalReceiptAmount: apiResult.totalReceiptAmount,
      };
    }
    viewMode.value = 'simulated';
    simulateError.value = '';
    isCalcSheetOpen.value = false;
    // 결과가 그려진 뒤(다음 틱) 결과 카드로 부드럽게 스크롤
    nextTick(() => {
      simResultRef.value?.scrollIntoView({
        behavior: 'smooth',
        block: 'center',
      });
    });
  } catch (error) {
    console.error(error);
    simulateError.value =
      error.response?.data?.message ?? '계산에 실패했습니다.';
  }
};

// '/simulator/calc'로 들어온 경우 모의 계산 바텀시트를 자동으로 연다.
// '/simulator'와 '/simulator/calc'는 같은 컴포넌트를 쓰므로(Vue Router가 인스턴스를
// 재사용) SPA 내부에서 두 경로를 오갈 때도 열리도록 watch로 감지한다.
watch(
  () => route.meta.openCalc,
  (openCalc) => {
    if (openCalc) {
      openCalcSheet();
    }
  },
  { immediate: true },
);

// ── 예적금 미리보기 (예금 1개 + 적금 2개 고정, 부족하면 있는 만큼만) ──
// 전체 목록은 "+ 상품 계산해보기"로 SimulatorProductListPage(적금/예금/정책 탭)에서 본다.
const savingProducts = ref([]);
const depositProducts = ref([]);
const policyProducts = ref([]);
const isProductLoading = ref(true);
const productError = ref('');

// 미리보기: 적금 2 + 예금 1 + 정책 1 = 최대 4장 (옆으로 넘기는 카드).
// 종류가 달라도 한 카드 모양으로 렌더되게 공통 형태로 정규화한다.
const previewProducts = computed(() => {
  const toCard = (it, category, kind) => ({
    key: `${category}-${category === 'policy' ? it.policyId : it.productId}`,
    category,
    id: category === 'policy' ? it.policyId : it.productId,
    kind,
    name: category === 'policy' ? it.policyName : it.productName,
    isTaxExempt: !!it.isTaxExempt,
    desc:
      category === 'policy'
        ? it.minRate === it.maxRate
          ? `최대 연 ${it.maxRate}%`
          : `연 ${it.minRate}~${it.maxRate}%`
        : `${it.saveTrm}개월 · 최대 연 ${it.maxRate}%`,
  });
  return [
    ...savingProducts.value.slice(0, 2).map((it) => toCard(it, 'savings', '적금')),
    ...depositProducts.value.slice(0, 1).map((it) => toCard(it, 'deposits', '예금')),
    ...policyProducts.value.slice(0, 1).map((it) => toCard(it, 'policy', '정책')),
  ];
});

const fetchProducts = async () => {
  isProductLoading.value = true;
  productError.value = '';
  try {
    const [savings, deposits, policies] = await Promise.all([
      productApi.findSavingProductList('savings'),
      productApi.findSavingProductList('deposits'),
      productApi.findPolicyProductList(),
    ]);
    savingProducts.value = savings;
    depositProducts.value = deposits;
    policyProducts.value = policies;
  } catch (error) {
    console.error(error);
    productError.value = '상품 정보를 불러오지 못했습니다.';
  } finally {
    isProductLoading.value = false;
  }
};

// 미리보기 카드를 누르면 상세페이지 대신, 그 상품이 바로 선택된 예적금 상품 계산기로 이동한다.
// 정책 상품은 policyId, 적금·예금은 productId로 딥링크한다.
const goToProductCalculator = (item) => {
  const query = { tab: item.category };
  if (item.category === 'policy') {
    query.policyId = item.id;
  } else {
    query.productId = item.id;
  }
  router.push({ name: 'SimulatorProductList', query });
};

const goToProductList = () => {
  router.push({ name: 'SimulatorProductList', query: { tab: 'savings' } });
};

// ── 미리보기 카드 캐러셀 (옆으로 넘기기 + 페이지네이션) ──
const carouselRef = ref(null);
const activeCard = ref(0);
const endSpacer = ref(0);

// 카드 폭 + gap(10). 폭이 바뀌어도 맞게 실제 카드에서 측정한다.
const cardStep = () => {
  const card = carouselRef.value?.querySelector('.product-chip-card');
  return card ? card.offsetWidth + 10 : 200;
};

// 마지막 카드도 왼쪽 정렬로 스냅되도록 뒤 여백 폭을 계산(컨테이너 폭 - 카드 폭).
// 이게 있어야 카드가 작아도 모든 카드가 고유한 스냅 위치를 가져 점이 안 겹친다.
const measureCarousel = () => {
  const el = carouselRef.value;
  const card = el?.querySelector('.product-chip-card');
  if (!el || !card) return;
  endSpacer.value = Math.max(0, el.clientWidth - card.offsetWidth);
};

const onCarouselScroll = () => {
  const el = carouselRef.value;
  if (!el) return;
  const i = Math.round(el.scrollLeft / cardStep());
  activeCard.value = Math.min(previewProducts.value.length - 1, Math.max(0, i));
};

const scrollToCard = (i) => {
  carouselRef.value?.scrollTo({ left: i * cardStep(), behavior: 'smooth' });
};

// 데스크탑: 세로 휠을 가로 스크롤로 (모바일은 터치 스와이프가 기본 동작)
const onCarouselWheel = (e) => {
  const el = carouselRef.value;
  if (!el || Math.abs(e.deltaY) <= Math.abs(e.deltaX)) return;
  el.scrollLeft += e.deltaY;
  e.preventDefault();
};

// 미리보기 카드가 렌더된 뒤 스페이서 폭을 다시 잰다.
watch(previewProducts, () => nextTick(measureCarousel));

onMounted(() => {
  fetchSavingDetails();
  fetchSavingLoss();
  fetchProducts();
  window.addEventListener('resize', measureCarousel);
});

onUnmounted(() => {
  window.removeEventListener('resize', measureCarousel);
});
</script>

<template>
  <div class="simulator-page container py-4">
    <PageHeader
      eyebrow="충성! 전역 적금 작전"
      title="만기일, 목돈 집결 완료"
      size="lg"
    />

    <SavingTimeline
      v-if="details && !hasNoAccount"
      ref="timelineRef"
      :details="details"
      :loss="savingLoss"
    />

    <button type="button" class="calc-banner" @click="openCalcSheet">
      <div class="calc-banner__text">
        <p class="calc-banner__title">
          월 납입액을 바꾸면?<br />시뮬레이션으로 미리 확인!
        </p>
        <span class="calc-banner__cta">
          군적금 시뮬레이션 해보기
          <i class="calc-banner__chevron" aria-hidden="true">›</i>
        </span>
      </div>
      <img class="calc-banner__img" :src="colliImg" alt="" />
    </button>

    <div
      v-if="viewMode === 'simulated' && simulatedResult"
      ref="simResultRef"
      class="sim-result"
    >
      <div class="sim-result__top">
        <span class="sim-result__badge">시뮬레이션</span>
        <button type="button" class="sim-result__reset" @click="resetToReal">
          ↑ 실제 내역으로
        </button>
      </div>
      <p class="sim-result__label">
        {{ Math.round(simulatedResult.monthlySaveTotal / 10000).toLocaleString('ko-KR') }}만원으로
        {{ simulatedResult.joinableMonths }}개월 넣으면, 예상 만기 수령액
      </p>
      <b class="sim-result__value">{{
        formatManwon(simulatedResult.totalReceiptAmount)
      }}</b>
      <div class="sim-result__break">
        <span><em>원금</em>{{ formatManwon(simulatedResult.expectedPrincipal) }}</span>
        <span><em>이자</em>{{ formatManwon(simulatedResult.expectedInterest) }}</span>
        <span class="sim-result__gov"
          ><em>정부지원</em>{{ formatManwon(simulatedResult.expectedMatchingFund) }}</span
        >
      </div>
    </div>

    <p v-if="simulateError" class="simulator-page__error">
      {{ simulateError }}
    </p>

    <p v-if="isLoading" class="text-caption">불러오는 중...</p>

    <EmptyState
      v-else-if="hasNoAccount && viewMode === 'real'"
      title="아직 군적금 가입 내역이 없어요"
      description="군적금에 가입하면 예상 만기 수령액을 시뮬레이션할 수 있어요"
    />

    <BaseBottomSheet
      v-model="isCalcSheetOpen"
      title="군적금 시뮬레이션"
      confirm-text="실행"
      cancel-text="취소"
      :show-close="false"
      :close-on-overlay="false"
      :confirm-disabled="!isCalcFormValid"
      @confirm="runCalculation"
    >
      <TabBar
        v-model="calcMode"
        class="calc-sheet__mode-tabs"
        variant="underline"
        :tabs="[
          { label: '고정 금액', value: 'constant' },
          { label: '구간별 금액', value: 'variable' },
        ]"
      />

      <div v-if="calcMode === 'constant'" class="calc-sheet__form">
        <p class="calc-sheet__hint">
          매달 같은 금액을 납입한다고 가정하고 계산해요.
        </p>
        <div class="calc-sheet__field-group">
          <BaseInput
            v-model="monthlySave"
            type="amount"
            label="월 납입액"
            suffix="만원"
            placeholder="최대 55"
          />
          <p class="calc-sheet__field-error">{{ monthlySaveError }}</p>
        </div>
        <div class="calc-sheet__field-group">
          <BaseInput
            v-model="saveMonths"
            type="number"
            label="납입 개월 수"
            suffix="개월"
            placeholder="최대 24"
          />
          <p class="calc-sheet__field-error">{{ saveMonthsError }}</p>
        </div>
      </div>

      <div v-else class="calc-sheet__form">
        <div class="calc-sheet__var-head">
          <p class="calc-sheet__hint">
            구간마다 다른 금액을 납입한다고 가정하고 계산해요.
          </p>
          <button
            v-if="periods.length"
            type="button"
            class="calc-sheet__reset-link"
            @click="resetPeriods"
          >
            전체 초기화
          </button>
        </div>

        <div v-if="periods.length" class="calc-sheet__period-list">
          <div
            v-for="(period, index) in periods"
            :key="index"
            class="calc-sheet__period-row"
          >
            <span class="calc-sheet__period-row-label">
              {{ index + 1 }}구간 ({{ period.startMonthOffset }}개월~{{
                period.endMonthOffset
              }}개월)
            </span>
            <span class="calc-sheet__period-row-amount">
              {{ period.amount.toLocaleString('ko-KR') }}만원
            </span>
            <button
              type="button"
              class="calc-sheet__period-remove"
              aria-label="구간 삭제"
              @click="removePeriod(index)"
            >
              ✕
            </button>
          </div>
        </div>

        <p v-if="!canAddMorePeriods" class="calc-sheet__hint">
          24개월 전체 구간을 다 등록했어요.
        </p>

        <template v-else>
          <div class="calc-sheet__range-field">
            <span class="calc-sheet__range-label">기간 설정</span>
            <div class="calc-sheet__range-inputs">
              <span class="calc-sheet__range-start"
                >{{ draftStartMonth }}개월</span
              >
              <span class="base-input__range-sep">~</span>
              <BaseInput
                type="select"
                :options="endMonthOptions"
                :model-value="periodDraft.endMonth"
                @update:model-value="periodDraft.endMonth = $event"
              />
            </div>
          </div>

          <div class="calc-sheet__field-group">
            <div class="calc-sheet__amount-row">
              <BaseInput
                type="amount"
                label="금액 설정"
                suffix="만원"
                placeholder="최대 55"
                :model-value="periodDraft.amount"
                @update:model-value="periodDraft.amount = $event"
              />
              <div class="calc-sheet__amount-actions">
                <button
                  type="button"
                  class="calc-sheet__register-btn"
                  :disabled="!isDraftValid"
                  @click="registerPeriod"
                >
                  등록
                </button>
              </div>
            </div>
            <!-- 유효성 문구: 고정 높이 슬롯 (있든 없든 자리 유지 → 시트 안 튀게) -->
            <p class="calc-sheet__field-error">{{ periodAmountError }}</p>
          </div>
        </template>
      </div>
    </BaseBottomSheet>

    <div class="product-section">
      <div class="product-intro">
        <PageHeader
          eyebrow="한 푼도 놓치지 마라"
          title="예적금으로 목돈 보급"
          size="lg"
        />
        <button
          type="button"
          class="product-intro__link"
          @click="goToProductList"
        >
          예적금 계산기 바로가기
          <span class="product-intro__chevron" aria-hidden="true">›</span>
        </button>
      </div>

      <div class="product-box">
        <p v-if="productError" class="simulator-page__error">
          {{ productError }}
        </p>
        <p v-else-if="isProductLoading" class="text-caption">불러오는 중...</p>

        <EmptyState
          v-else-if="previewProducts.length === 0"
          title="추천 상품이 없어요"
          description="조건에 맞는 상품을 찾을 수 없어요"
        />

        <template v-else>
          <div
            ref="carouselRef"
            class="product-carousel"
            @scroll="onCarouselScroll"
            @wheel="onCarouselWheel"
          >
            <button
              v-for="item in previewProducts"
              :key="item.key"
              type="button"
              class="product-chip-card"
              :class="`is-${item.category}`"
              @click="goToProductCalculator(item)"
            >
              <div class="product-chip-card__top">
                <span class="product-chip-card__kind">{{ item.kind }}</span>
                <span v-if="item.isTaxExempt" class="product-chip-card__tax"
                  >비과세</span
                >
              </div>
              <p class="product-chip-card__name">{{ item.name }}</p>
              <p class="product-chip-card__desc">{{ item.desc }}</p>
            </button>
            <!-- 마지막 카드도 왼쪽 정렬로 스냅되게 뒤 여백 확보 (점 겹침 방지) -->
            <div
              class="product-carousel__end"
              aria-hidden="true"
              :style="{ flex: `0 0 ${endSpacer}px` }"
            ></div>
          </div>

          <div v-if="previewProducts.length > 1" class="product-dots">
            <button
              v-for="(item, i) in previewProducts"
              :key="item.key"
              type="button"
              class="product-dot"
              :class="{ 'is-active': i === activeCard }"
              :aria-label="`${i + 1}번째 상품 보기`"
              @click="scrollToCard(i)"
            ></button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.simulator-page {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 모의 계산 배너 CTA (제목 + 알약 버튼 + 콜리) */
.calc-banner {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  padding: 20px 20px 20px 22px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(105deg, #e5ecdd 0%, #d6e1c9 100%);
  font-family: inherit;
  text-align: left;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.15s ease;
}
.calc-banner:active {
  transform: scale(0.99);
}
.calc-banner__text {
  display: flex;
  flex-direction: column;
  gap: 9px;
  z-index: 1;
}
.calc-banner__title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.32;
  letter-spacing: -0.02em;
  color: var(--camo-forest);
}
.calc-banner__cta {
  display: inline-flex;
  align-items: center;
  align-self: flex-start;
  gap: 3px;
  color: var(--military-green);
  font-size: 13px;
  font-weight: 700;
}
.calc-banner__chevron {
  font-style: normal;
  font-size: 16px;
  line-height: 1;
}
.calc-banner__img {
  flex: none;
  width: 72px;
  height: 72px;
  object-fit: contain;
  z-index: 1;
}

/* 모의 계산 결과 카드 (배너 밑, 시뮬레이션 상태일 때만) */
.sim-result {
  padding: 15px 18px;
  border-radius: 14px;
  background: var(--surface-default);
  border: 1px solid var(--input-border);
}
.sim-result__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.sim-result__badge {
  padding: 3px 9px;
  border-radius: 999px;
  background: var(--military-green);
  color: #fff;
  font-size: 10.5px;
  font-weight: 700;
}
.sim-result__reset {
  border: none;
  background: none;
  padding: 0;
  color: var(--camo-forest);
  font-size: 12px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
}
.sim-result__label {
  margin: 0 0 2px;
  font-size: 12px;
  font-weight: 600;
  color: var(--camo-green);
}
.sim-result__value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--military-green);
}
.sim-result__break {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--divider-thin);
}
.sim-result__break span {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-strong);
}
.sim-result__break em {
  margin-right: 4px;
  font-style: normal;
  font-weight: 600;
  color: var(--text-muted);
}
.sim-result__gov,
.sim-result__gov em {
  color: var(--camo-olive) !important;
}

.simulator-page__error {
  margin: 0;
  font-size: 13px;
  color: var(--danger, #fa6e6e);
}

/* ── 모의 계산 바텀시트 ── */
.calc-sheet__mode-tabs {
  margin-bottom: 16px;
}

.calc-sheet__form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  /* 고정↔구간별 초기 높이를 맞춰 모드 전환 시 창이 안 흔들리게 */
  min-height: 254px;
}
/* 계산폼 입력값은 모두 오른쪽 정렬 (금액·개월수 통일) */
.calc-sheet__form :deep(.base-input__field) {
  text-align: right;
}
.calc-sheet__form :deep(.base-input__field.has-suffix) {
  padding-right: 56px;
}
/* 개월수(number) 스피너 화살표 숨김 - 오른쪽 정렬 값과 겹치지 않게 */
.calc-sheet__form :deep(input[type='number'])::-webkit-inner-spin-button,
.calc-sheet__form :deep(input[type='number'])::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.calc-sheet__hint {
  margin: 0;
  font-size: 13px;
  color: var(--text-hint, #999999);
}

/* 구간별 상단: 안내문 + 초기화(밑줄) */
.calc-sheet__var-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.calc-sheet__reset-link {
  flex-shrink: 0;
  border: none;
  background: none;
  padding: 0;
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  text-decoration: underline;
  text-underline-offset: 2px;
  cursor: pointer;
}
/* 입력칸 + 유효성 문구 묶음 */
.calc-sheet__field-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
/* 유효성 문구 슬롯: 항상 자리 차지해서 시트 높이 안 바뀌게 */
.calc-sheet__field-error {
  margin: 0;
  min-height: 16px;
  line-height: 16px;
  font-size: 12px;
  font-weight: 500;
  color: var(--danger);
}

.calc-sheet__period-list {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 4px 16px;
}

.calc-sheet__period-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
}

.calc-sheet__period-row + .calc-sheet__period-row {
  border-top: 1px solid var(--line);
}

.calc-sheet__period-row-label {
  flex: 1;
  font-size: 13px;
  color: var(--text-body);
}

.calc-sheet__period-row-amount {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
}

.calc-sheet__period-remove {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  border: none;
  background: none;
  padding: 0;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1;
  cursor: pointer;
  transition: color 0.15s ease;
}
.calc-sheet__period-remove:hover {
  color: var(--danger);
}

.calc-sheet__range-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.calc-sheet__range-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-body);
}

.calc-sheet__range-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
}
/* 종료개월 select: 남은 공간 채움 ("1개월 ~ [select]"로 여백 없이) */
.calc-sheet__range-inputs :deep(.base-input) {
  flex: 1;
  min-width: 0;
}

.calc-sheet__range-start {
  flex-shrink: 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-body);
}

.calc-sheet__amount-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.calc-sheet__amount-row :deep(.base-input) {
  flex: 1;
}

.calc-sheet__amount-actions {
  display: flex;
  gap: 6px;
  /* 버튼이 입력칸보다 작아 바닥에 치우쳐 보이는 걸 세로 중앙으로 살짝 올림 */
  margin-bottom: 5px;
}

.calc-sheet__register-btn {
  padding: 8px 14px;
  border: none;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  background-color: var(--chart-1);
  color: #fff;
}

.calc-sheet__register-btn:disabled {
  background-color: var(--surface-muted);
  color: var(--text-hint);
  cursor: not-allowed;
}

/* ── 예적금 미리보기 (색 채운 카드 + 캐러셀) ── */
.product-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* 위 배너와 간격 넓히기 */
  margin-top: 14px;
}

/* 바깥 네모 박스 (카드만 담는다) — 흰 배경 위에서 뜨게 그림자로 (테두리 X) */
.product-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border-radius: 18px;
  background: var(--surface-default);
  box-shadow: 0 2px 14px rgba(0, 0, 0, 0.06);
}

/* 메인 제목(PageHeader lg) + 바로가기 */
.product-intro {
  display: flex;
  flex-direction: column;
}
/* PageHeader 기본 하단 여백 제거 (바로가기 링크로 간격 제어) */
.product-intro :deep(.page-header) {
  margin-bottom: 0;
}
.product-intro__link {
  align-self: flex-start;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  margin-top: 6px;
  padding: 0;
  border: none;
  background: none;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  color: var(--military-green);
  cursor: pointer;
}
.product-intro__chevron {
  font-size: 15px;
  line-height: 1;
}

/* 옆으로 넘기는 카드 캐러셀 */
.product-carousel {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}
.product-carousel::-webkit-scrollbar {
  display: none;
}
.product-carousel__end {
  align-self: stretch;
}

/* 색으로 채운 상품 카드 */
.product-chip-card {
  flex: 0 0 auto;
  width: 165px;
  scroll-snap-align: start;
  display: flex;
  flex-direction: column;
  gap: 7px;
  padding: 13px 14px;
  border: none;
  border-radius: 14px;
  font-family: inherit;
  text-align: left;
  cursor: pointer;
  transition: transform 0.15s ease;
}
.product-chip-card:active {
  transform: scale(0.97);
}
.product-chip-card.is-savings {
  background: #ece8f7;
}
.product-chip-card.is-deposits {
  background: #e4eefb;
}
.product-chip-card.is-policy {
  background: #fdf1cf;
}
.product-chip-card__top {
  display: flex;
  align-items: center;
  gap: 4px;
}
.product-chip-card__kind {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 10.5px;
  font-weight: 700;
  color: #fff;
}
.is-savings .product-chip-card__kind {
  background: #6f5fb0;
}
.is-deposits .product-chip-card__kind {
  background: #3f6fb5;
}
.is-policy .product-chip-card__kind {
  background: #c1912f;
}
.product-chip-card__tax {
  padding: 2px 7px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  color: #6a5a24;
  font-size: 10.5px;
  font-weight: 700;
}
.product-chip-card__name {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  line-height: 1.35;
  color: var(--text-strong);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 37px;
}
.product-chip-card__desc {
  margin: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-body);
}

/* 페이지네이션 점 */
.product-dots {
  display: flex;
  justify-content: center;
  gap: 6px;
}
.product-dot {
  width: 6px;
  height: 6px;
  padding: 0;
  border: none;
  border-radius: 999px;
  background: var(--divider-thin);
  cursor: pointer;
  transition:
    width 0.2s ease,
    background 0.2s ease;
}
.product-dot.is-active {
  width: 16px;
  background: var(--military-green);
}
</style>

<style>
/* 목돈작전 화면 배경 - D-Day와 같은 은은한 세이지 그린 컬러감 */
.app-content:has(.simulator-page) {
  background-color: rgba(120, 152, 130, 0.06);
}
</style>
