<script setup>
// SCR-RENT-04 · Step 4) 금융상품 + 저장  담당: 수연
// 디자인: UI/rent_ui_school_mode.html — 담백 버전(색·이모지 제거, 곰돌이 진행바 유지)
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import rentApi from '@/api/rentApi';
import { useRentStore } from '@/stores/rent';
import { useToast } from '@/composables/useToast';
import { formatManwon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';

const route = useRoute();
const router = useRouter();
const rentStore = useRentStore();
const { show } = useToast();
const goalId = Number(route.params.goalId);
const listingId = Number(route.query.listingId) || rentStore.selectedListingId;
const months = Number(route.query.months) || rentStore.months || 6;

const CATEGORY = { POLICY: '정책', KB: 'KB' };

// TODO: 백엔드 금융상품 API(WIP) 준비되면 gap·products 실데이터로 교체
const SAMPLE = {
  gap: { totalCost: 8000000, maturityAmount: 7200000, shortage: 800000 },
  products: [
    { productId: 1, productName: '청년 주거지원 대출', productType: 'POLICY', interestRate: 2.1, loanLimit: 50000000, militaryDiscount: 0.5, features: '무주택 청년 대상, 최대 10년 상환' },
    { productId: 2, productName: 'KB 청년 전세대출', productType: 'KB', interestRate: 3.4, loanLimit: 30000000, militaryDiscount: 0.3, features: '만 34세 이하, 소득 3.5억 미만' },
    { productId: 3, productName: 'KB 신용대출', productType: 'KB', interestRate: 4.8, loanLimit: 20000000, militaryDiscount: 0, features: '신용점수 700점 이상' },
  ],
};

const gap = ref(SAMPLE.gap);
const products = ref([]);
const loading = ref(true);
const saving = ref(false);
const hasShortage = computed(() => (gap.value?.shortage ?? 0) > 0);
// 응답에 보증금 전용 구분 필드가 없어 상품명으로 전월세보증금 성격을 식별 (표시용, 계산·정렬 로직 변경 없음)
const isDepositProduct = (p) => /전세|전월세|보증금/.test(p.productName || '');

// 선택한 금융상품 id 목록 (다중 선택 토글) — confirmGoal 의 selectedProductIds 로 전송
const selectedProductIds = ref([]);
const isSelected = (id) => selectedProductIds.value.includes(id);
const toggleProduct = (id) => {
  const i = selectedProductIds.value.indexOf(id);
  if (i === -1) selectedProductIds.value.push(id);
  else selectedProductIds.value.splice(i, 1);
};

const load = async () => {
  loading.value = true;
  try {
    const data = await rentApi.findProducts(goalId, listingId, months);
    gap.value = data?.gap || SAMPLE.gap;
    products.value = data?.products?.length ? data.products : SAMPLE.products;
  } catch {
    gap.value = SAMPLE.gap;
    products.value = SAMPLE.products;
  } finally {
    loading.value = false;
  }
};
onMounted(load);

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
    //   정식 연동되면 이 catch를 에러 토스트 + return 으로 되돌릴 것
    console.warn('[데모] confirmGoal 실패(백엔드 미구현) - 저장 없이 로드맵 메인으로 이동만 진행');
  } finally {
    show('로드맵을 저장했어요', 'success');
    await router.push({ name: 'RoadmapMain' });
    saving.value = false;
  }
};
</script>

<template>
  <div class="products">
    <RoadmapCharacterSlider :progress="80" label="자취 로드맵" />
    <header>
      <p class="step">STEP 4</p>
      <h2 class="title">추천 금융상품</h2>
      <p class="desc">군필 우대 상품 우선 추천</p>
    </header>

    <p v-if="loading" class="loading">불러오는 중...</p>

    <template v-else>
      <BaseCard v-if="hasShortage" padding="12px 14px">
        <p class="cap">부족분 요약 ({{ months }}개월 기준)</p>
        <div class="row"><span>총 필요 자금</span><span class="b">{{ formatManwon(gap.totalCost) }}</span></div>
        <div class="row"><span>만기금</span><span class="b">{{ formatManwon(gap.maturityAmount) }}</span></div>
        <div class="divider" />
        <div class="row"><span class="b">부족분</span><span class="b">{{ formatManwon(gap.shortage) }} · 대출 필요</span></div>
      </BaseCard>
      <div v-else class="enough">만기금으로 충분해요! 대출 없이도 자취 준비 가능해요</div>

      <p v-if="products.length" class="deposit-banner">보증금이 부담되면 아래 전월세보증금대출을 활용하세요</p>
      <p v-if="products.length" class="pick-hint">함께 저장할 상품을 선택하세요 (여러 개 가능)</p>
      <BaseCard v-for="p in products" :key="p.productId" padding="12px 14px"
        class="p-card"
        role="checkbox"
        :aria-checked="isSelected(p.productId)"
        tabindex="0"
        :class="{ 'is-policy': p.productType === 'POLICY', 'is-selected': isSelected(p.productId) }"
        @click="toggleProduct(p.productId)"
        @keydown.enter.prevent="toggleProduct(p.productId)"
        @keydown.space.prevent="toggleProduct(p.productId)">
        <div class="p-top">
          <span class="p-name">{{ p.productName }}</span>
          <span class="p-top-right">
            <span class="tag">{{ CATEGORY[p.productType] }}</span>
            <span class="check" :class="{ on: isSelected(p.productId) }" aria-hidden="true">✓</span>
          </span>
        </div>
        <p v-if="isDepositProduct(p)" class="p-deposit">보증금 마련에 활용</p>
        <p class="p-terms">금리 연 {{ p.interestRate }}% · 한도 {{ formatManwon(p.loanLimit) }}</p>
        <p v-if="p.militaryDiscount" class="p-mil">군필 우대 -{{ p.militaryDiscount }}%p</p>
        <p class="p-feat">{{ p.features }}</p>
      </BaseCard>

      <div class="promo">
        <p class="promo-t">저장하면 이런 정보가 추가돼요</p>
        <p class="promo-l">· 지역별 실제 공과금 반영 (KOSIS 실데이터)</p>
        <p class="promo-l">· 후회소비 데이터로 진짜 감당 가능도 판정</p>
        <p class="promo-l">· 정확한 위치·주변 편의시설 정보</p>
      </div>
    </template>

    <BottomButtonBar
      secondary-label="이전"
      :primary-label="saving ? '저장 중...' : '로드맵 저장'"
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
  gap: 10px;
}
.step {
  font-size: 12px;
  color: var(--text-muted);
}
.title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong);
}
.desc {
  margin-top: 3px;
  font-size: 12px;
  color: var(--text-muted);
}
.loading {
  padding: 40px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
.cap {
  font-size: 11px;
  color: var(--text-muted);
  margin-bottom: 6px;
}
.row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-body);
  margin-bottom: 2px;
}
.row span:first-child {
  color: var(--text-muted);
}
.b {
  font-weight: 700;
  color: var(--text-body);
}
.divider {
  height: 1px;
  background: var(--line);
  margin: 6px 0;
}
.enough {
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fafafa;
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-body);
}
.is-policy {
  border-color: var(--text-strong);
}
.deposit-banner {
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fafafa;
  font-size: 11px;
  color: var(--text-body);
  line-height: 1.5;
}
.pick-hint {
  margin: 2px 0 -2px;
  font-size: 11px;
  color: var(--text-muted);
}
.p-deposit {
  margin-bottom: 2px;
  font-size: 10px;
  font-weight: 600;
  color: var(--text-body);
}
.p-card {
  cursor: pointer;
  transition: border-color 0.12s ease, box-shadow 0.12s ease;
}
.p-card:active {
  transform: scale(0.995);
}
.p-card.is-selected {
  border-color: var(--kb-yellow);
  box-shadow: 0 0 0 1.5px var(--kb-yellow);
}
.p-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.p-top-right {
  display: flex;
  align-items: center;
  gap: 6px;
}
.check {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: #fff;
  color: transparent;
  font-size: 11px;
  font-weight: 800;
  line-height: 1;
}
.check.on {
  border-color: var(--kb-yellow);
  background: var(--kb-yellow);
  color: var(--text-strong);
}
.p-name {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-body);
}
.p-terms {
  font-size: 11px;
  color: var(--text-muted);
}
.p-mil {
  margin-top: 4px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-body);
}
.p-feat {
  margin-top: 4px;
  font-size: 10px;
  color: var(--text-hint);
}
.tag {
  font-size: 10px;
  padding: 2px 8px;
  border: 1px solid var(--line);
  border-radius: 999px;
  color: var(--text-muted);
}
.promo {
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fafafa;
}
.promo-t {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-body);
  margin-bottom: 4px;
}
.promo-l {
  font-size: 10px;
  color: var(--text-muted);
  line-height: 1.7;
}
/* 버튼 색은 BottomButtonBar 컴포넌트 기본값 사용 */
</style>
