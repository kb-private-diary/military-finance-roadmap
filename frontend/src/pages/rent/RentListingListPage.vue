<script setup>
// SCR-RENT-02 · Step 2) 매물 리스트  담당: 수연
// 디자인: UI/rent_ui_school_mode.html (매물 카드) — 목업 톤 반영(교통·재정 뱃지)
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import rentApi from '@/api/rentApi';
import { useRentStore } from '@/stores/rent';
import { formatManwon } from '@/util/format';
import { useToast } from '@/composables/useToast';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import Chip from '@/components/common/Chip.vue';
import Badge from '@/components/common/Badge.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import busIcon from '@/assets/images/bus.png';
import subwayIcon from '@/assets/images/subway.png';
import walkIcon from '@/assets/images/walk.png';
import checkGreenIcon from '@/assets/images/check-green.png';
import budgetTightIcon from '@/assets/images/budget-tight.png';
import budgetOverIcon from '@/assets/images/budget-over.png';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import PageHeader from '@/components/common/PageHeader.vue';

const route = useRoute();
const router = useRouter();
const rentStore = useRentStore();
const { show: showToast } = useToast();
const goalId = Number(route.params.goalId);
if (!rentStore.currentGoalId) rentStore.currentGoalId = goalId;

const ESTATE_LABEL = {
  OFFICETEL: '오피스텔',
  APARTMENT: '아파트',
  VILLA: '빌라',
  ROOM: '원룸',
};

const listings = ref([]);
const loading = ref(true);
const sortMode = ref('recommend'); // recommend | cheap | near
const sortOptions = [
  { label: '추천순', value: 'recommend' },
  { label: '저렴한 순', value: 'cheap' },
  { label: '가까운 순', value: 'near' },
];

const filterText = computed(() => {
  const d = rentStore.draft;
  const loc =
    d.locationType === 'SCHOOL'
      ? d.schoolName || '학교'
      : d.regions.map((r) => r.name.split(' ').pop()).join('·') || '지역';
  return `${loc} · ${d.radiusKm}km · ${d.monthlyBudget}만원`;
});

const load = async () => {
  loading.value = true;
  try {
    const data = await rentApi.findListings(goalId);
    // 백엔드 계약: { totalCount, listings[] } — 배열로 바로 오는 경우도 방어
    const arr = Array.isArray(data) ? data : (data?.listings ?? []);
    listings.value = arr;
  } catch {
    listings.value = [];
    showToast('매물을 불러오지 못했어요', 'error');
  } finally {
    loading.value = false;
  }
};
onMounted(load);

// ── 교통 뱃지(SCHOOL=commuteText / REGION=transitText) ──────────
// "도보 N분"→🚶, "OO역 도보 N분"→🚇 (둘 다 파랑), "버스…"→🚌(갈색). null이면 미표시.
const trafficText = (l) =>
  l.selectionMode === 'REGION' ? l.transitText : l.commuteText;

const trafficBadge = (l) => {
  const t = trafficText(l);
  if (!t) return null;
  if (t.includes('버스')) return { icon: busIcon, tone: 'cream', text: t, iconSize: 13 };
  if (t.includes('역')) return { icon: subwayIcon, tone: 'blue', text: t };
  return { icon: walkIcon, tone: 'blue', text: t }; // 도보
};

// ── 재정 뱃지(affordLevel) ────────────────────────────────────
const AFFORD = {
  ENOUGH: { icon: checkGreenIcon, tone: 'green' },
  TIGHT: { icon: budgetTightIcon, tone: 'yellow', iconSize: 13 },
  OVER: { icon: budgetOverIcon, tone: 'red', iconSize: 13 },
};
const affordBadge = (l) => {
  const m = AFFORD[l.affordLevel];
  return m ? { ...m, text: l.affordText } : null;
};

// ── 평수 변환(㎡ ÷ 3.3058, 소수1자리) ─────────────────────────
const toPyeong = (sqm) =>
  sqm == null ? null : (sqm / 3.3058).toFixed(1);

const metaLine = (l) =>
  [
    l.umdName,
    toPyeong(l.areaSqm) != null ? `${toPyeong(l.areaSqm)}평` : null,
    l.floor != null ? `${l.floor}층` : null,
  ]
    .filter(Boolean)
    .join(' · ');

const priceLine = (l) =>
  `보증 ${formatManwon(l.deposit)} / 월 ${formatManwon(l.monthlyRent)} / 관리 ${formatManwon(l.maintenanceFee)}`;

// ── 정렬 ──────────────────────────────────────────────────────
// 추천순 = 응답 순서(백엔드 실질월부담 오름차순) / 저렴한 순 = effectiveMonthly↑ / 가까운 순 = 교통 분↑
const parseMinutes = (l) => {
  const t = trafficText(l);
  if (!t) return Infinity;
  const m = t.match(/(\d+)\s*분/);
  // TODO: "버스 이용 지역"처럼 분(分)이 없는 지역 표기는 파싱 불가 → 맨 뒤로
  return m ? Number(m[1]) : Infinity;
};

const sortedListings = computed(() => {
  const arr = [...listings.value];
  if (sortMode.value === 'cheap') {
    return arr.sort(
      (a, b) => (a.effectiveMonthly ?? Infinity) - (b.effectiveMonthly ?? Infinity),
    );
  }
  if (sortMode.value === 'near') {
    return arr.sort((a, b) => parseMinutes(a) - parseMinutes(b));
  }
  return arr; // recommend: 응답 순서 유지
});

// ── 카드 클릭 = 선택만(상세 이동 X). priceLevel 도 store 에 저장해 step3 시세뱃지에서 사용 ──
const selectListing = (l) => {
  rentStore.selectedListingId = l.listingId;
  rentStore.selectedPriceLevel = l.priceLevel ?? null;
};

// ── 상세 이동(하단 "매물 선택" 버튼에서만 호출) ─────────────────
const goDetail = (l) => {
  router.push({
    name: 'RentListingDetail',
    params: { listingId: l.listingId },
    query: {
      goalId,
      months: rentStore.months,
      priceLevel: l.priceLevel ?? undefined,
    },
  });
};

const onPrev = () => router.push({ name: 'RentGoalCreate' });
const onComplete = () => {
  const picked = listings.value.find(
    (l) => l.listingId === rentStore.selectedListingId,
  );
  if (picked) goDetail(picked);
};
</script>

<template>
  <div class="listings">
    <RoadmapCharacterSlider :step="2" label="자취 로드맵" />

    <PageHeader title="로드맵을 선택해주세요." />

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
      <div class="sortbar">
        <BaseInput
          v-model="sortMode"
          type="select"
          :options="sortOptions"
          class="sortbar__select"
        />
        <span class="filter">{{ filterText }}</span>
      </div>

      <BaseCard
        v-for="l in sortedListings"
        :key="l.listingId"
        padding="12px"
        :class="{ 'card-selected': rentStore.selectedListingId === l.listingId }"
      >
        <button class="listing-card" @click="selectListing(l)">
          <span class="name-row">
            <Chip :text="ESTATE_LABEL[l.estateType] || '매물'" />
            <span class="name">{{ l.buildingName }}</span>
          </span>
          <span class="meta">{{ metaLine(l) }}</span>
          <span class="price">{{ priceLine(l) }}</span>
          <span class="badges">
            <Badge
              v-if="trafficBadge(l)"
              :tone="trafficBadge(l).tone"
              :icon="trafficBadge(l).icon"
              :icon-size="trafficBadge(l).iconSize"
              :text="trafficBadge(l).text"
            />
            <Badge
              v-if="affordBadge(l)"
              :tone="affordBadge(l).tone"
              :icon="affordBadge(l).icon"
              :icon-size="affordBadge(l).iconSize"
              :text="affordBadge(l).text"
            />
          </span>
        </button>
      </BaseCard>
    </template>

    <BottomButtonBar
      secondary-label="이전"
      primary-label="선택완료"
      :primary-disabled="!rentStore.selectedListingId"
      @secondary-click="onPrev"
      @primary-click="onComplete"
    />
  </div>
</template>

<style scoped>
.listings {
  padding: 20px 0 88px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.loading {
  padding: 40px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
.sortbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 2px;
}
/* 정렬: confirmed BaseInput select을 컴팩트하게 (내용 폭에 맞춤) */
.sortbar__select {
  flex: 0 0 auto;
  width: auto;
}
.sortbar__select :deep(.dropdown),
.sortbar__select :deep(.dropdown__button) {
  width: auto;
}
.sortbar__select :deep(.dropdown__button) {
  min-height: 0;
  padding: 4px 22px 4px 4px;
  font-size: 12px;
}
.sortbar__select :deep(.dropdown__text) {
  flex: none;
}
.filter {
  font-size: 11px;
  color: var(--text-muted);
}
/* 선택된 매물 카드 강조 (하단 버튼으로 넘어가기 전 표시) */
.card-selected {
  outline: 2px solid var(--kb-yellow-deep);
  outline-offset: -1px;
  border-radius: 12px;
}
.listing-card {
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
}
.name-row {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}
.name {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-body);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.meta {
  margin-top: 3px;
  font-size: 11px;
  color: var(--text-hint);
}
.price {
  margin-top: 2px;
  font-size: 11px;
  color: var(--text-muted);
}
.badges {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-top: 6px;
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
