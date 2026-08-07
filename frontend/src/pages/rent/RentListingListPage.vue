<script setup>
// SCR-RENT-02 · Step 2) 매물 리스트  담당: 수연
// 디자인: UI/rent_ui_school_mode.html (매물 카드) — 목업 톤 반영(교통·재정 뱃지)
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import rentApi from '@/api/rentApi';
import { useRentStore } from '@/stores/rent';
import { formatManwon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
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
  { listingId: 1, estateType: 'OFFICETEL', buildingName: '부산대 앞 오피스텔', umdName: '부산 금정구 장전동', areaSqm: 31.7, floor: 11, deposit: 5000000, monthlyRent: 450000, maintenanceFee: 50000, selectionMode: 'SCHOOL', commuteText: '도보 12분', transitText: null, affordLevel: 'ENOUGH', affordText: '딱 맞아요', effectiveMonthly: 530000, totalCost6M: 8000000, priceLevel: 'LOW' },
  { listingId: 2, estateType: 'ROOM', buildingName: '장전동 원룸', umdName: '부산 금정구 장전동', areaSqm: 23.1, floor: 3, deposit: 3000000, monthlyRent: 400000, maintenanceFee: 30000, selectionMode: 'SCHOOL', commuteText: '도보 8분', transitText: null, affordLevel: 'ENOUGH', affordText: '딱 맞아요', effectiveMonthly: 480000, totalCost6M: 5580000, priceLevel: 'MID' },
  { listingId: 3, estateType: 'VILLA', buildingName: '부곡동 신축 빌라', umdName: '부산 금정구 부곡동', areaSqm: 39.6, floor: 2, deposit: 8000000, monthlyRent: 500000, maintenanceFee: 50000, selectionMode: 'SCHOOL', commuteText: '버스 15분', transitText: null, affordLevel: 'TIGHT', affordText: '빠듯해요', effectiveMonthly: 620000, totalCost6M: 11300000, priceLevel: 'HIGH' },
];

const listings = ref([]);
const loading = ref(true);
const sortMode = ref('recommend'); // recommend | cheap | near

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
    listings.value = arr.length ? arr : SAMPLE;
  } catch {
    listings.value = SAMPLE; // TODO: 폴백 제거
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
  if (t.includes('버스')) return { emoji: '🚌', cls: 'badge--brown', text: t };
  if (t.includes('역')) return { emoji: '🚇', cls: 'badge--blue', text: t };
  return { emoji: '🚶', cls: 'badge--blue', text: t }; // 도보
};

// ── 재정 뱃지(affordLevel) ────────────────────────────────────
const AFFORD = {
  ENOUGH: { emoji: '👍', cls: 'badge--green' },
  TIGHT: { emoji: '⚠️', cls: 'badge--amber' },
  OVER: { emoji: '❌', cls: 'badge--red' },
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

    <h2 class="title">로드맵을 선택해주세요.</h2>

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
        <select v-model="sortMode" class="sort-select" aria-label="정렬">
          <option value="recommend">추천순</option>
          <option value="cheap">저렴한 순</option>
          <option value="near">가까운 순</option>
        </select>
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
            <span class="chip">{{ ESTATE_LABEL[l.estateType] || '매물' }}</span>
            <span class="name">{{ l.buildingName }}</span>
          </span>
          <span class="meta">{{ metaLine(l) }}</span>
          <span class="price">{{ priceLine(l) }}</span>
          <span class="badges">
            <span
              v-if="trafficBadge(l)"
              class="badge"
              :class="trafficBadge(l).cls"
            >
              {{ trafficBadge(l).emoji }} {{ trafficBadge(l).text }}
            </span>
            <span
              v-if="affordBadge(l)"
              class="badge"
              :class="affordBadge(l).cls"
            >
              {{ affordBadge(l).emoji }} {{ affordBadge(l).text }}
            </span>
          </span>
        </button>
      </BaseCard>
    </template>

    <BottomButtonBar
      secondary-label="이전"
      primary-label="선택 완료"
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
.title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong);
  line-height: 1.35;
  margin-bottom: 2px;
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
.sort-select {
  appearance: none;
  padding: 6px 26px 6px 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background-color: #ffffff;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='6' viewBox='0 0 10 6'%3E%3Cpath d='M1 1l4 4 4-4' fill='none' stroke='%23767676' stroke-width='1.5' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  font-size: 12px;
  font-family: inherit;
  color: var(--text-body);
  cursor: pointer;
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
.chip {
  flex-shrink: 0;
  padding: 2px 6px;
  border-radius: 5px;
  background: #9d9d9d;
  color: #fff;
  font-size: 9px;
  font-weight: 600;
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
.badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 10px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 999px;
  white-space: nowrap;
}
/* 뱃지 색: 수연 확정 스펙(목업 기준, 글자색 전부 #000) */
.badge--blue {
  /* 도보/지하철 */
  background: #d3e6ff;
  color: #000;
}
.badge--brown {
  /* 버스 */
  background: #ffead3;
  color: #000;
}
.badge--green {
  /* 딱 맞아요 */
  background: #e1f3e0;
  color: #000;
}
.badge--amber {
  /* 빠듯해요 */
  background: #ffffc3;
  color: #000;
}
.badge--red {
  /* 예산 초과 */
  background: #f4d1d1;
  color: #000;
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
