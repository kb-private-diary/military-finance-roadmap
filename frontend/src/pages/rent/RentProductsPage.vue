<script setup>
// SCR-RENT-04 · Step 4) 금융상품 + 저장  담당: 수연
// 디자인: 목업 재구성 — 감당도 요약 카드 + 조언 박스 + 정책/KB 탭 + 상품 카드
// Step4는 추천만 보여주는 화면(기획) — 상품 선택 없음, confirmGoal로 로드맵만 저장
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import rentApi from '@/api/rentApi';
import { useRentStore } from '@/stores/rent';
import { useToast } from '@/composables/useToast';
import { formatManwon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseModal from '@/components/common/BaseModal.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import rentPolicyIcon from '@/assets/images/rent-policy-product.png';
import rentKbIcon from '@/assets/images/rent-kb-product.png';

const route = useRoute();
const router = useRouter();
const rentStore = useRentStore();
const { show } = useToast();
const goalId = Number(route.params.goalId);
const listingId = Number(route.query.listingId) || rentStore.selectedListingId;
const months = Number(route.query.months) || rentStore.months || 6;

// 만원 정수 변환(표시·계산 공용). 입력은 "원" 단위.
const toMan = (won) => Math.round((won ?? 0) / 10000).toLocaleString('ko-KR');

const recommend = ref(null); // findProducts 응답 원본
const affordability = ref(null); // findAffordability 응답
const loading = ref(true);
const saving = ref(false);
const isCompleteModalOpen = ref(false); // 저장 완료 모달 (여행/자동차/취업과 통일)

// ── 상품 목록: 세 그룹을 합쳐 productId 중복 제거 ──────────────
const allProducts = computed(() => {
  const r = recommend.value;
  if (!r) return [];
  const merged = [...(r.monthlySubsidy || []), ...(r.depositLoan || []), ...(r.free || [])];
  const map = new Map();
  merged.forEach((p) => {
    if (p && p.productId != null && !map.has(p.productId)) map.set(p.productId, p);
  });
  return [...map.values()];
});
// 정책상품(kb=false): 정부(POLICY) / 지역(LOCAL)
const govProducts = computed(() => allProducts.value.filter((p) => !p.kb && p.productType === 'POLICY'));
const localProducts = computed(() => allProducts.value.filter((p) => !p.kb && p.productType === 'LOCAL'));
// KB상품(kb=true, BANK)
const kbProducts = computed(() => allProducts.value.filter((p) => p.kb));

// 탭
const activeTab = ref('POLICY'); // 'POLICY' | 'KB'

// 상품 간단설명: 금리요약 · 지원금 · 기관 조합
const productDesc = (p) => {
  const parts = [];
  if (p.rateSummary) parts.push(p.rateSummary);
  else if (p.supportAmount) {
    parts.push(`월 최대 ${formatManwon(p.supportAmount)}${p.supportMonths ? ` · ${p.supportMonths}개월` : ''}`);
  }
  if (p.provider) parts.push(p.provider);
  return parts.join(' · ') || (p.provider ?? '');
};

// ── 감당도 요약 ────────────────────────────────────────────
// 감당도 응답이 없으면 빈 객체(모든 파생값이 0/기본으로 안전 처리됨).
// 요약/조언 섹션 자체는 template 에서 affordability 있을 때만 노출한다.
const af = computed(() => affordability.value || {});
const pct = computed(() => {
  const req = af.value.totalRequired ?? 0;
  return req > 0 ? Math.round(((af.value.maturityAmount ?? 0) / req) * 100) : 0;
});
const isEnough = computed(() => pct.value >= 100);
const coverMan = computed(() => toMan(Math.min(af.value.maturityAmount ?? 0, af.value.totalRequired ?? 0)));
const surplusMan = computed(() => toMan(af.value.surplus));
const shortfallMan = computed(() => toMan(af.value.shortfall));
const maturityLabel = computed(() => formatManwon(af.value.maturityAmount));

// 막대: 트랙 분모 = max(100, pct). 노랑=충당(필요 대비 충족분), 나머지=초록 빗금(남음)/빨강 빗금(부족)
const barMax = computed(() => Math.max(100, pct.value));
const coverWidth = computed(() => (Math.min(pct.value, 100) / barMax.value) * 100);
const restWidth = computed(() => 100 - coverWidth.value);

// ── 조언 박스 계산 ─────────────────────────────────────────
const hasSurplus = computed(() => (af.value.surplus ?? 0) > 0);
const hasShortfall = computed(() => (af.value.shortfall ?? 0) > 0);
// 버팀목 대출 실금리: 받아온 상품 중 버팀목의 rateMin(housing_product DB값, 전국 정부상품) 사용
//   응답에 없으면(예외) 연 2.1% 폴백. 주택도시기금 청년전용 버팀목 = region_code NULL(전국)이라 항상 추천됨
const loanRate = computed(() => {
  const all = [
    ...(recommend.value?.monthlySubsidy || []),
    ...(recommend.value?.depositLoan || []),
    ...(recommend.value?.free || []),
  ];
  const bumtmok = all.find((p) => p.productName?.includes('버팀목') && p.rateMin != null);
  return bumtmok ? Number(bumtmok.rateMin) / 100 : 0.021;
});
const loanRatePct = computed(() => (loanRate.value * 100).toFixed(1)); // 표시용 (예: "1.5")
// 버팀목 대출 월 이자(천단위 반올림, 원)
const loanMonthlyInterest = computed(() => {
  const won = ((af.value.shortfall ?? 0) * loanRate.value) / 12;
  return Math.round(won / 1000) * 1000;
});
const loanMonthlyLabel = computed(() => `${loanMonthlyInterest.value.toLocaleString('ko-KR')}원`);

// ── 로드 ───────────────────────────────────────────────────
const loadProducts = async () => {
  try {
    const data = await rentApi.findProducts(listingId, months);
    recommend.value = data && (data.monthlySubsidy || data.depositLoan || data.free) ? data : null;
  } catch {
    recommend.value = null;
    show('금융상품을 불러오지 못했어요', 'error');
  }
};
const loadAffordability = async () => {
  try {
    const data = await rentApi.findAffordability(listingId, months, rentStore.depositMode);
    affordability.value = data || null;
  } catch {
    affordability.value = null; // 요약/조언 섹션은 template 에서 숨김
  }
};
onMounted(async () => {
  loading.value = true;
  await Promise.all([loadProducts(), loadAffordability()]);
  loading.value = false;
});

// 이전: step3 매물 상세로 (listingId·months 유지)
const goPrev = () => {
  router.push({ name: 'RentListingDetail', params: { listingId }, query: { goalId, months } });
};

const saveRoadmap = async () => {
  if (saving.value) return;
  saving.value = true;
  try {
    await rentApi.confirmGoal(goalId, { listingId, months });
  } catch {
    // 저장 실패를 숨기지 않고 정직하게 에러 노출 후 중단(이동 안 함)
    show('로드맵을 저장하지 못했어요', 'error');
    saving.value = false;
    return;
  }
  rentStore.reset(); // 저장 완료 → 위저드 입력값 초기화(다음엔 처음부터). goalId 등은 지역 상수라 영향 없음
  // 저장 완료 모달 노출 → 확인 시 상세로 이동 (여행/자동차/취업 플로우와 통일)
  isCompleteModalOpen.value = true;
  saving.value = false;
};

// 저장 완료 모달 확인/취소 → 저장된 로드맵 상세로 이동
const goToDetail = () => {
  router.push({ name: 'RentGoalDetail', params: { goalId } });
};
</script>

<template>
  <div class="products">
    <RoadmapCharacterSlider :step="4" label="자취 로드맵" />
    <PageHeader title="이 금융상품 어떠십니까?" />

    <p v-if="loading" class="loading">불러오는 중...</p>

    <template v-else>
      <!-- 3. 감당도 요약 카드 (감당도 응답 있을 때만) -->
      <BaseCard v-if="affordability" padding="14px 16px 16px">
        <div class="af-head">
          <span class="af-head__label">필요 금액 대비 내 만기금</span>
        </div>

        <div class="afbar">
          <div class="afbar__cover" :style="{ width: coverWidth + '%' }">
            <span class="afbar__txt">{{ coverMan }}만 충당</span>
          </div>
          <div
            v-if="restWidth > 0"
            class="afbar__rest"
            :class="isEnough ? 'afbar__rest--surplus' : 'afbar__rest--short'"
            :style="{ width: restWidth + '%' }"
          >
            <span class="afbar__txt afbar__txt--rest">{{ isEnough ? '필요선' : shortfallMan + '만 부족' }}</span>
          </div>
        </div>

        <div class="af-foot">
          <span class="af-foot__l">내 만기금 <b>{{ maturityLabel }}</b></span>
          <span v-if="hasSurplus" class="af-foot__r is-enough">남는 돈 +{{ surplusMan }}만원</span>
          <span v-else class="af-foot__r is-short">부족한 돈 -{{ shortfallMan }}만원</span>
        </div>
      </BaseCard>

      <!-- 4. 조언 박스 -->
      <div v-if="hasSurplus" class="advice advice--good">
        <div class="advice__body">
          <p class="advice__t"><span class="advice__emoji">💰</span>{{ surplusMan }}만원이 남아요</p>
          <p class="advice__s">남는 돈은 적금이나 비상금으로 모아두면 좋아요</p>
        </div>
      </div>
      <div v-else-if="hasShortfall" class="advice advice--warn">
        <div class="advice__body">
          <p class="advice__t"><span class="advice__emoji">🏦</span>{{ shortfallMan }}만원을 메워야 해요</p>
          <p class="advice__s">버팀목 대출이면 월 이자 약 {{ loanMonthlyLabel }} (연 {{ loanRatePct }}%, 주택도시기금)</p>
        </div>
      </div>

      <!-- 5. 탭 -->
      <div class="tab-row">
        <button
          type="button"
          class="tab-item"
          :class="{ 'is-active': activeTab === 'POLICY' }"
          @click="activeTab = 'POLICY'"
        >
          정책상품 <img :src="rentPolicyIcon" class="tab-icon" alt="" />
        </button>
        <button
          type="button"
          class="tab-item"
          :class="{ 'is-active': activeTab === 'KB' }"
          @click="activeTab = 'KB'"
        >
          KB상품 <img :src="rentKbIcon" class="tab-icon" alt="" />
        </button>
      </div>

      <!-- 6. 탭 내용 -->
      <p class="pick-hint">목표에 맞는 추천 상품이에요. 카드를 눌러 상세 정보를 확인하세요</p>

      <!-- 정책상품 탭 -->
      <template v-if="activeTab === 'POLICY'">
        <section class="group">
          <p class="group__title">정부 상품</p>
          <template v-if="govProducts.length">
            <button
              v-for="p in govProducts"
              :key="p.productId"
              type="button"
              class="prod"
            >
              <span class="prod__body">
                <span class="prod__name">{{ p.productName }}</span>
                <span class="prod__desc">{{ productDesc(p) }}</span>
              </span>
              <a
                v-if="p.externalUrl"
                :href="p.externalUrl"
                target="_blank"
                rel="noopener noreferrer"
                class="prod__link"
                aria-label="외부 링크 열기"
                @click.stop
              >↗</a>
              <span v-else class="prod__link prod__link--off" aria-hidden="true">↗</span>
            </button>
          </template>
          <p v-else class="empty">해당 상품이 없어요</p>
        </section>

        <section class="group">
          <p class="group__title">지역 상품</p>
          <template v-if="localProducts.length">
            <button
              v-for="p in localProducts"
              :key="p.productId"
              type="button"
              class="prod"
            >
              <span class="prod__body">
                <span class="prod__name">{{ p.productName }}</span>
                <span class="prod__desc">{{ productDesc(p) }}</span>
              </span>
              <a
                v-if="p.externalUrl"
                :href="p.externalUrl"
                target="_blank"
                rel="noopener noreferrer"
                class="prod__link"
                aria-label="외부 링크 열기"
                @click.stop
              >↗</a>
              <span v-else class="prod__link prod__link--off" aria-hidden="true">↗</span>
            </button>
          </template>
          <p v-else class="empty">해당 상품이 없어요</p>
        </section>
      </template>

      <!-- KB상품 탭 -->
      <section v-else class="group">
        <template v-if="kbProducts.length">
          <button
            v-for="p in kbProducts"
            :key="p.productId"
            type="button"
            class="prod"
          >
            <span class="prod__body">
              <span class="prod__name">{{ p.productName }}</span>
              <span class="prod__desc">{{ productDesc(p) }}</span>
            </span>
            <a
              v-if="p.externalUrl"
              :href="p.externalUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="prod__link"
              aria-label="외부 링크 열기"
              @click.stop
            >↗</a>
            <span v-else class="prod__link prod__link--off" aria-hidden="true">↗</span>
          </button>
        </template>
        <p v-else class="empty">해당 상품이 없어요</p>
      </section>

      <!-- 7. 저장 안내 박스 -->
      <div class="promo">
        <p class="promo-t">💾 저장하면 이런 정보가 추가돼요</p>
        <p class="promo-l">✅ 지역별 실제 공과금 반영 (KOSIS 실데이터)</p>
        <p class="promo-l">✅ 후회소비 데이터로 진짜 감당 가능도 판정</p>
        <p class="promo-l">✅ 정확한 위치·주변 편의시설 정보</p>
      </div>

      <!-- 8. 안내 문구 -->
      <p class="foot-note">추천 상품을 확인하고 로드맵을 저장하세요</p>
    </template>

    <BottomButtonBar
      secondary-label="이전"
      :primary-label="saving ? '저장 중...' : '저장'"
      :primary-disabled="saving || loading"
      @secondary-click="goPrev"
      @primary-click="saveRoadmap"
    />

    <BaseModal
      v-model="isCompleteModalOpen"
      title="알림"
      confirm-text="확인"
      @confirm="goToDetail"
      @cancel="goToDetail"
    >
      <p class="complete-modal__message">자취 로드맵이 저장되었습니다.</p>
    </BaseModal>
  </div>
</template>

<style scoped>
.products {
  padding: 20px 0 88px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.loading {
  padding: 60px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}

/* ── 감당도 요약 카드 ──────────────────────────────────── */
.af-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 12px;
}
.af-head__label {
  font-size: 13px;
  color: var(--text-muted);
}
.afbar {
  display: flex;
  width: 100%;
  height: 26px;
  border-radius: 8px;
  overflow: hidden;
  background: var(--kb-gray-pale);
}
.afbar__cover {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--kb-yellow);
  min-width: 0;
}
.afbar__rest {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
}
/* 남음 - 연초록 단색 (step2 "딱 맞아요" 톤과 통일) */
.afbar__rest--surplus {
  background: #e1f3e0;
}
/* 부족 - 연빨강 단색 (step2 "예산 초과" 톤과 통일) */
.afbar__rest--short {
  background: #f4d1d1;
}
.afbar__txt {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-strong);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0 4px;
}
.afbar__txt--rest {
  color: var(--text-body);
}
.af-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 12px;
  color: var(--text-body);
}
.af-foot__l b {
  font-weight: 700;
  color: var(--text-strong);
}
.af-foot__r {
  font-weight: 700;
}
.af-foot__r.is-enough {
  color: var(--success);
}
.af-foot__r.is-short {
  color: var(--danger);
}

/* ── 조언 박스 ─────────────────────────────────────────── */
.advice {
  padding: 10px 12px;
  border-radius: 10px;
}
.advice--good {
  background: var(--military-green-light);
}
.advice--warn {
  background: #fdecec;
}
.advice__emoji {
  font-size: 13px;
  margin-right: 5px;
  vertical-align: middle;
}
.advice__body {
  flex: 1;
  min-width: 0;
}
.advice__t {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
}
.advice__s {
  margin: 3px 0 0;
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.35;
}

/* ── 탭 ───────────────────────────────────────────────── */
.tab-row {
  display: flex;
  gap: 6px;
  margin-top: 4px;
}
.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 0;
  border: 1px solid transparent;
  border-radius: 14px 14px 4px 4px;
  background: var(--kb-gray-pale);
  color: var(--text-hint);
  font-size: 14px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s ease;
}
.tab-item.is-active {
  background: #fff;
  border-color: var(--line-strong);
  color: var(--text-strong);
  font-weight: 700;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
.tab-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

/* ── 상품 그룹 ────────────────────────────────────────── */
.pick-hint {
  margin-top: 2px;
  font-size: 11px;
  color: var(--text-muted);
}
.group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.group__title {
  margin-top: 4px;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
}

/* ── 상품 카드 ────────────────────────────────────────── */
.prod {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fff;
  font-family: inherit;
  text-align: left;
}
.prod__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.prod__name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
}
.prod__desc {
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.4;
}
/* 외부링크 - 회색 원 없이 화살표만 */
.prod__link {
  flex: none;
  color: var(--text-muted);
  font-size: 16px;
  font-weight: 700;
  text-decoration: none;
  padding: 4px;
}
.prod__link--off {
  color: var(--text-hint);
  opacity: 0.6;
}
.empty {
  padding: 18px 0;
  text-align: center;
  font-size: 12px;
  color: var(--text-hint);
  border: 1px dashed var(--line);
  border-radius: 12px;
}

/* ── 저장 안내 박스 ───────────────────────────────────── */
.promo {
  margin-top: 6px;
  padding: 14px;
  border-radius: 12px;
  background: var(--kb-yellow-pale);
}
.promo-t {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
  margin-bottom: 8px;
}
.promo-l {
  font-size: 12px;
  color: var(--text-body);
  line-height: 1.9;
}
.foot-note {
  text-align: center;
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.6;
}
/* 버튼 색은 BottomButtonBar 컴포넌트 기본값 사용 */
</style>
