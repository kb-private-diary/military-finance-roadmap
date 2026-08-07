<script setup>
// SCR-RENT-04 · Step 4) 금융상품 + 저장  담당: 수연
// 디자인: 목업 재구성 — 감당도 요약 카드 + 조언 박스 + 정책/KB 탭 + 상품 카드
// 상품 선택(다중 토글)·confirmGoal 저장 로직은 기존 방식 유지
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import rentApi from '@/api/rentApi';
import { useRentStore } from '@/stores/rent';
import { useToast } from '@/composables/useToast';
import { formatManwon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
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

// TODO: 백엔드 금융상품 API(WIP) 준비되면 SAMPLE 폴백 제거
const SAMPLE_PRODUCTS = {
  veteran: true,
  serviceMonths: 18,
  serviceYears: 1,
  monthlySubsidy: [
    { productId: 1, productName: '청년 월세 특별지원', productType: 'POLICY', supportType: 'GRANT', provider: '국토교통부', kb: false, rateSummary: null, supportAmount: 200000, supportMonths: 12, regionCode: null, externalUrl: 'https://www.gov.kr' },
  ],
  depositLoan: [
    { productId: 2, productName: '버팀목 전세자금대출', productType: 'POLICY', supportType: 'LOAN', provider: '주택도시기금', kb: false, rateSummary: '연 1.8~2.7%', supportAmount: null, supportMonths: null, regionCode: null, externalUrl: 'https://nhuf.molit.go.kr' },
    { productId: 3, productName: '부산 청년 임차보증금 이자지원', productType: 'LOCAL', supportType: 'INTEREST', provider: '부산광역시', kb: false, rateSummary: '이자 최대 2%p 지원', supportAmount: null, supportMonths: null, regionCode: '26', externalUrl: 'https://www.busan.go.kr' },
  ],
  free: [
    { productId: 4, productName: 'KB 청년전세 든든대출', productType: 'BANK', supportType: 'LOAN', provider: 'KB국민은행', kb: true, rateSummary: '연 3.4~4.1%', supportAmount: null, supportMonths: null, regionCode: null, externalUrl: 'https://obank.kbstar.com' },
  ],
};
const SAMPLE_AFFORD = { totalRequired: 8000000, maturityAmount: 7200000, surplus: 0, shortfall: 800000, affordabilityLabel: '빠듯해요' };

const recommend = ref(null); // findProducts 응답 원본
const affordability = ref(null); // findAffordability 응답
const loading = ref(true);
const saving = ref(false);

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
const af = computed(() => affordability.value || SAMPLE_AFFORD);
const pct = computed(() => {
  const req = af.value.totalRequired ?? 0;
  return req > 0 ? Math.round(((af.value.maturityAmount ?? 0) / req) * 100) : 0;
});
const isEnough = computed(() => pct.value >= 100);
// 감당도 3단계 라벨(백엔드 affordabilityLabel: 딱 맞아요/빠듯해요/예산 초과)
const affordLabel = computed(
  () => af.value.affordabilityLabel || (isEnough.value ? '딱 맞아요' : '예산 초과'),
);
const afToneClass = computed(() => {
  if (affordLabel.value === '빠듯해요') return 'is-tight';
  if (affordLabel.value === '예산 초과') return 'is-over';
  return 'is-ok';
});
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
// 적금 연 3% 가정 → 12개월 뒤 이자(만원 반올림)
const savingsInterestMan = computed(() => Math.round(((af.value.surplus ?? 0) * 0.03) / 10000));
// 버팀목 대출 연 2.1% 가정 → 월 이자(천단위 반올림, 원)
const loanMonthlyInterest = computed(() => {
  const won = ((af.value.shortfall ?? 0) * 0.021) / 12;
  return Math.round(won / 1000) * 1000;
});
const loanMonthlyLabel = computed(() => `${loanMonthlyInterest.value.toLocaleString('ko-KR')}원`);

// ── 상품 선택(다중 토글) — confirmGoal 의 selectedProductIds 로 전송 ──
const selectedProductIds = ref([]);
const isSelected = (id) => selectedProductIds.value.includes(id);
const toggleProduct = (id) => {
  const i = selectedProductIds.value.indexOf(id);
  if (i === -1) selectedProductIds.value.push(id);
  else selectedProductIds.value.splice(i, 1);
};

// ── 로드 ───────────────────────────────────────────────────
const loadProducts = async () => {
  try {
    const data = await rentApi.findProducts(goalId, listingId, months);
    recommend.value = data && (data.monthlySubsidy || data.depositLoan || data.free) ? data : SAMPLE_PRODUCTS;
  } catch {
    recommend.value = SAMPLE_PRODUCTS;
  }
};
const loadAffordability = async () => {
  try {
    const data = await rentApi.findAffordability(listingId, months, rentStore.depositMode);
    affordability.value = data || null;
  } catch {
    affordability.value = null; // computed af 가 SAMPLE 로 폴백
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
    await rentApi.confirmGoal(goalId, { listingId, months, selectedProductIds: selectedProductIds.value });
  } catch {
    // TODO(데모용/임시): 백엔드 confirm 엔드포인트(POST /api/rent/goals/{goalId}/confirm) 준비 전이라 404임
    //   정식 연동되면 이 catch 를 에러 토스트 + return 으로 되돌릴 것
    console.warn('[데모] confirmGoal 실패(백엔드 미구현) - 저장 없이 목표 상세로 이동만 진행');
  } finally {
    show('로드맵을 저장했어요', 'success');
    await router.push({ name: 'RentGoalDetail', params: { goalId } });
    saving.value = false;
  }
};
</script>

<template>
  <div class="products">
    <RoadmapCharacterSlider :step="4" label="자취 로드맵" />
    <h2 class="page-title">이 금융상품 어떠십니까?</h2>

    <p v-if="loading" class="loading">불러오는 중...</p>

    <template v-else>
      <!-- 3. 감당도 요약 카드 -->
      <BaseCard padding="14px 16px 16px">
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
          <p class="advice__s">적금에 넣으면 12개월 뒤 이자 약 {{ savingsInterestMan }}만원 (연 3% 가정)</p>
        </div>
      </div>
      <div v-else-if="hasShortfall" class="advice advice--warn">
        <div class="advice__body">
          <p class="advice__t"><span class="advice__emoji">🏦</span>{{ shortfallMan }}만원을 메워야 해요</p>
          <p class="advice__s">버팀목 대출이면 월 이자 약 {{ loanMonthlyLabel }} (연 2.1% 가정)</p>
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
      <p class="pick-hint">함께 저장할 상품을 선택하세요 (여러 개 가능)</p>

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
              :class="{ 'is-selected': isSelected(p.productId) }"
              :aria-pressed="isSelected(p.productId)"
              @click="toggleProduct(p.productId)"
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
              :class="{ 'is-selected': isSelected(p.productId) }"
              :aria-pressed="isSelected(p.productId)"
              @click="toggleProduct(p.productId)"
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
            :class="{ 'is-selected': isSelected(p.productId) }"
            :aria-pressed="isSelected(p.productId)"
            @click="toggleProduct(p.productId)"
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
      <p class="foot-note">선택한 상품과 함께 로드맵을 저장할 수 있어요</p>
    </template>

    <BottomButtonBar
      secondary-label="이전"
      :primary-label="saving ? '저장 중...' : '저장'"
      :primary-disabled="saving || loading"
      @secondary-click="goPrev"
      @primary-click="saveRoadmap"
    />
  </div>
</template>

<style scoped>
.products {
  padding: 20px 0 88px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.page-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong);
  line-height: 1.35;
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
.af-head__pct {
  font-size: 18px;
  font-weight: 800;
}
/* 3단계: 딱 맞아요(초록) / 빠듯해요(주황) / 예산 초과(빨강) */
.af-head__pct.is-ok {
  color: var(--success);
}
.af-head__pct.is-tight {
  color: #e08a00;
}
.af-head__pct.is-over {
  color: var(--danger);
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
  font-size: 13px;
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
  cursor: pointer;
  transition: border-color 0.12s ease, box-shadow 0.12s ease;
}
.prod:active {
  transform: scale(0.995);
}
.prod.is-selected {
  border-color: var(--kb-yellow);
  box-shadow: 0 0 0 1.5px var(--kb-yellow);
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
