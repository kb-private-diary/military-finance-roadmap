<script setup>
// SCR-RENT-03 · Step 3) 매물 상세 + 내 재정 체크  담당: 수연
// 디자인: UI/rent_ui_school_mode.html — 담백 버전(색·이모지 제거)
// 거주기간 슬라이더(6~24) 조작 → 총비용/재정체크 실시간 갱신됨, months 는 Step4~5로 이어짐
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import rentApi from '@/api/rentApi';
import { useRentStore } from '@/stores/rent';
import { formatManwon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';

const route = useRoute();
const router = useRouter();
const rentStore = useRentStore();
const listingId = Number(route.params.listingId);
const goalId = Number(route.query.goalId) || rentStore.currentGoalId;
const maturity = computed(() => rentStore.maturityAmount || 7200000);

// TODO: 백엔드 매물 상세 API(WIP) 준비되면 샘플 폴백 제거
const SAMPLE = {
  listing: { buildingName: '부산대 앞 오피스텔', dongName: '부산 금정구 장전동', areaSqm: 23, floor: 5, buildYear: 2018, dealDate: '2026-06-15', deposit: 5000000, monthlyRent: 450000, maintenanceFee: 50000 },
  propertyBadges: ['지역 평균보다 저렴', '1개월 전 실거래'],
};

const data = ref(null);
const loading = ref(true);
const months = ref(rentStore.months || 6);

const load = async () => {
  loading.value = true;
  try {
    const res = await rentApi.findListingDetail(listingId, goalId, months.value);
    data.value = res?.listing ? res : SAMPLE;
  } catch {
    data.value = SAMPLE;
  } finally {
    loading.value = false;
  }
};
onMounted(load);
watch(months, (m) => (rentStore.months = m));

const listing = computed(() => data.value?.listing);
const monthlyCost = computed(() => (listing.value?.monthlyRent ?? 0) + (listing.value?.maintenanceFee ?? 0));
const totalCost = computed(() => (listing.value?.deposit ?? 0) + monthlyCost.value * months.value);
const affordText = computed(() => {
  const t = totalCost.value, m = maturity.value;
  if (m >= t * 1.2) return '딱 맞아요';
  if (m >= t * 0.8) return '빠듯해요';
  return '예산 초과';
});

const goProducts = () => {
  router.push({ name: 'RentProducts', params: { goalId }, query: { listingId, months: months.value } });
};
// 이전: step2 매물 리스트로 (draft·선택값은 store에 유지됨)
const goPrev = () => {
  router.push({ name: 'RentListingList', params: { goalId } });
};
</script>

<template>
  <div v-if="listing" class="detail">
    <RoadmapCharacterSlider :progress="60" label="자취 로드맵" />
    <header>
      <p class="step">STEP 3</p>
      <h2 class="name">{{ listing.buildingName }}</h2>
      <p class="meta">{{ listing.dongName }} · {{ listing.floor }}층 · {{ listing.areaSqm }}㎡</p>
    </header>

    <div class="map">지도</div>

    <div class="pbadges">
      <span v-for="(b, i) in (data.propertyBadges || SAMPLE.propertyBadges)" :key="i" class="tag">{{ b }}</span>
    </div>

    <BaseCard padding="12px 14px">
      <p class="cap">세부 정보</p>
      <div class="row"><span>계약일</span><span>{{ listing.dealDate }}</span></div>
      <div class="row"><span>건축년도</span><span>{{ listing.buildYear }}년</span></div>
      <div class="row"><span>층수</span><span>{{ listing.floor }}층</span></div>
    </BaseCard>

    <section class="period">
      <div class="row"><span class="label">거주기간</span><span class="val">{{ months }}개월</span></div>
      <input v-model.number="months" type="range" min="6" max="24" step="1" class="slider" />
      <div class="scale"><span>6개월</span><span>24개월</span></div>
    </section>

    <BaseCard padding="12px 14px">
      <p class="cap">{{ months }}개월 예상 총 비용</p>
      <p class="total">{{ formatManwon(totalCost) }}</p>
      <div class="divider" />
      <div class="row"><span>보증금</span><span>{{ formatManwon(listing.deposit) }}</span></div>
      <div class="row"><span>월세 × {{ months }}</span><span>{{ formatManwon(listing.monthlyRent * months) }}</span></div>
      <div class="row"><span>관리비 × {{ months }}</span><span>{{ formatManwon(listing.maintenanceFee * months) }}</span></div>
    </BaseCard>

    <div class="check">
      <div class="check__t">내 재정 체크: {{ affordText }}</div>
      <div class="check__s">만기금 {{ formatManwon(maturity) }} 기준</div>
    </div>

    <p class="promo">저장하면 지역별 실제 공과금까지 계산해서 진짜 정확한 자취 예산을 알려드려요</p>

    <BottomButtonBar
      secondary-label="이전"
      primary-label="금융상품 보기"
      @secondary-click="goPrev"
      @primary-click="goProducts"
    />
  </div>
  <p v-else class="loading">불러오는 중...</p>
</template>

<style scoped>
.detail {
  padding: 20px 0 88px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.step {
  font-size: 12px;
  color: var(--text-muted);
}
.name {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-strong);
}
.meta {
  margin-top: 3px;
  font-size: 12px;
  color: var(--text-muted);
}
.map {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100px;
  border-radius: 8px;
  background: var(--kb-gray-pale);
  color: var(--text-muted);
  font-size: 13px;
}
.pbadges {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.tag {
  font-size: 11px;
  padding: 3px 10px;
  border: 1px solid var(--line);
  border-radius: 999px;
  color: var(--text-muted);
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
  margin-bottom: 3px;
}
.row span:first-child {
  color: var(--text-muted);
}
.label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-body);
}
.val {
  font-size: 13px;
  font-weight: 700;
}
.period {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.slider {
  width: 100%;
  accent-color: var(--text-strong);
}
.scale {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: var(--text-hint);
}
.total {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
  margin-bottom: 8px;
}
.divider {
  height: 1px;
  background: var(--line);
  margin: 6px 0;
}
.check {
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fafafa;
}
.check__t {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
}
.check__s {
  margin-top: 3px;
  font-size: 11px;
  color: var(--text-muted);
}
.promo {
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fafafa;
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.6;
}
.loading {
  padding: 60px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
/* 버튼 색은 BottomButtonBar 컴포넌트 기본값 사용 */
</style>
