<script setup>
// SCR-RENT-02 · Step 2) 매물 리스트  담당: 수연
// 디자인: UI/rent_ui_school_mode.html (매물 카드) — 담백 버전(색·이모지 제거)
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import rentApi from '@/api/rentApi';
import { useRentStore } from '@/stores/rent';
import { formatManwon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';

const route = useRoute();
const router = useRouter();
const rentStore = useRentStore();
const goalId = Number(route.params.goalId);
if (!rentStore.currentGoalId) rentStore.currentGoalId = goalId;

const ESTATE_LABEL = {
  OFFICETEL: '오피스텔',
  APARTMENT: '아파트',
  VILLA: '빌라',
  ROOM: '원룸',
};

// TODO: 백엔드 매물 API(WIP) 준비되면 샘플 폴백 제거
const SAMPLE = [
  { listingId: 1, buildingName: '부산대 앞 오피스텔', estateType: 'OFFICETEL', dongName: '부산 금정구 장전동', deposit: 5000000, monthlyRent: 450000, maintenanceFee: 50000, totalCost6M: 8000000, distanceText: '도보 12분', affordText: '딱 맞아요' },
  { listingId: 2, buildingName: '장전동 원룸', estateType: 'ROOM', dongName: '부산 금정구 장전동', deposit: 3000000, monthlyRent: 400000, maintenanceFee: 30000, totalCost6M: 5580000, distanceText: '도보 8분', affordText: '딱 맞아요' },
  { listingId: 3, buildingName: '부곡동 신축 원룸', estateType: 'VILLA', dongName: '부산 금정구 부곡동', deposit: 8000000, monthlyRent: 500000, maintenanceFee: 50000, totalCost6M: 11300000, distanceText: '버스 15분', affordText: '빠듯해요' },
];

const listings = ref([]);
const loading = ref(true);

const filterText = computed(() => {
  const d = rentStore.draft;
  const loc =
    d.locationType === 'SCHOOL'
      ? d.schoolName || '학교'
      : d.regions.map((r) => r.name.split(' ').pop()).join('·') || '지역';
  return `${loc} · ${d.radiusKm}km · ${d.monthlyBudget}만`;
});

const load = async () => {
  loading.value = true;
  try {
    const data = await rentApi.findListings(goalId);
    listings.value = data?.length ? data : SAMPLE;
  } catch {
    listings.value = SAMPLE; // TODO: 폴백 제거
  } finally {
    loading.value = false;
  }
};
onMounted(load);

const goDetail = (l) => {
  rentStore.selectedListingId = l.listingId;
  router.push({
    name: 'RentListingDetail',
    params: { listingId: l.listingId },
    query: { goalId, months: rentStore.months },
  });
};
const priceLine = (l) =>
  `보증 ${formatManwon(l.deposit)} / 월 ${formatManwon(l.monthlyRent)} / 관리 ${formatManwon(l.maintenanceFee)}`;
</script>

<template>
  <div class="listings">
    <RoadmapCharacterSlider :progress="40" label="자취 로드맵" />
    <header class="head">
      <div>
        <p class="step">STEP 2</p>
        <h2 class="title">추천 매물 {{ listings.length }}개</h2>
      </div>
      <span class="filter">{{ filterText }}</span>
    </header>

    <p v-if="loading" class="loading">불러오는 중...</p>

    <EmptyState
      v-else-if="!listings.length"
      title="조건에 맞는 매물이 없어요"
      description="반경을 넓히거나 월 예산을 조정해보세요"
    >
      <template #action>
        <button class="cta" @click="router.push({ name: 'RentGoalCreate' })">
          Step 1로 돌아가기
        </button>
      </template>
    </EmptyState>

    <template v-else>
      <BaseCard v-for="l in listings" :key="l.listingId" padding="12px">
        <button class="listing-card" @click="goDetail(l)">
          <span class="thumb">{{ ESTATE_LABEL[l.estateType] || '매물' }}</span>
          <span class="info">
            <span class="name">{{ l.buildingName }}</span>
            <span class="dong">{{ l.dongName }}</span>
            <span class="price">{{ priceLine(l) }}</span>
            <span class="tags">
              <span class="tag">{{ l.distanceText }}</span>
              <span class="tag">{{ l.affordText }}</span>
            </span>
            <span class="est">6개월 예상 {{ formatManwon(l.totalCost6M) }}</span>
          </span>
        </button>
      </BaseCard>
      <p class="footnote">6개월 거주 기준으로 재정 진단</p>
    </template>
  </div>
</template>

<style scoped>
.listings {
  padding: 20px 0 24px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
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
.filter {
  font-size: 11px;
  color: var(--text-muted);
}
.loading {
  padding: 40px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
.listing-card {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 12px;
  width: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
}
.thumb {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 8px;
  background: var(--kb-gray-pale);
  color: var(--text-muted);
  font-size: 11px;
  flex-shrink: 0;
}
.info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.name {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-body);
}
.dong {
  font-size: 11px;
  color: var(--text-hint);
}
.price {
  margin-top: 2px;
  font-size: 11px;
  color: var(--text-muted);
}
.tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-top: 5px;
}
.tag {
  font-size: 10px;
  padding: 2px 8px;
  border: 1px solid var(--line);
  border-radius: 999px;
  color: var(--text-muted);
}
.est {
  margin-top: 6px;
  font-size: 11px;
  color: var(--text-body);
}
.footnote {
  text-align: center;
  font-size: 10px;
  color: var(--text-hint);
  margin-top: 4px;
}
.cta {
  padding: 9px 18px;
  border: 0;
  border-radius: 8px;
  background: var(--kb-yellow);
  color: var(--text-strong);
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
}
</style>
