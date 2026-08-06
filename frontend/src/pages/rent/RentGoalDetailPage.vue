<script setup>
// SCR-RENT-05 · Step 5) 저장 후 상세보기  담당: 수연
// 디자인: UI/rent_ui_school_mode.html — 담백 버전(색·이모지 제거)
// 지번·지도·편의시설 + 정밀 시뮬레이션(공과금 + 후회소비)
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import rentApi from '@/api/rentApi';
import { formatManwon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const route = useRoute();
const router = useRouter();
const goalId = Number(route.params.goalId);

// TODO: 백엔드 저장상세 API(WIP·확장 대기) 준비되면 샘플 폴백 제거
const SAMPLE = {
  goal: { createdAt: '2026-08-04', listing: { buildingName: '부산대 앞 오피스텔', jibunAddress: '부산 금정구 장전동 123-45', floor: 5 } },
  nearbyFacilities: [
    { name: '부산대역', walkMinutes: 8 },
    { name: '버스 정류장', walkMinutes: 3 },
    { name: '편의점', walkMinutes: 2 },
    { name: '병원', walkMinutes: 10 },
    { name: '마트', walkMinutes: 15 },
  ],
  precisionSimulation: {
    monthlyHousingCost: { rentAndFee: 500000, utilityFee: 80000, total: 580000 },
    userSpending: { avgMonthlySpending: 430000, avgRegretSpending: 120000 },
    totalMonthlyNeed: 1010000, possibleMonths: 7.1, reducedPossibleMonths: 8.5,
  },
};

const goal = ref(null);
const facilities = ref([]);
const sim = ref(null);
const loading = ref(true);

const load = async () => {
  loading.value = true;
  try {
    const d = await rentApi.findGoal(goalId);
    const src = d?.goal ? d : SAMPLE;
    goal.value = src.goal;
    facilities.value = src.nearbyFacilities || [];
    sim.value = src.precisionSimulation;
  } catch {
    goal.value = SAMPLE.goal;
    facilities.value = SAMPLE.nearbyFacilities;
    sim.value = SAMPLE.precisionSimulation;
  } finally {
    loading.value = false;
  }
};
onMounted(load);

const confirm = () => router.push({ name: 'Home' });
</script>

<template>
  <div v-if="goal" class="goal">
    <header class="head">
      <div>
        <p class="step">STEP 5</p>
        <h2 class="name">{{ goal.listing.buildingName }}</h2>
        <p class="meta">저장일 {{ goal.createdAt }} · {{ goal.listing.jibunAddress }} ({{ goal.listing.floor }}층)</p>
      </div>
      <span class="badge">저장됨</span>
    </header>

    <div class="map">지도</div>

    <BaseCard padding="12px 14px">
      <p class="cap">주변 편의시설</p>
      <div v-for="(f, i) in facilities" :key="i" class="row">
        <span>{{ f.name }}</span><span class="muted">도보 {{ f.walkMinutes }}분</span>
      </div>
    </BaseCard>

    <BaseCard v-if="sim" padding="14px">
      <p class="sim-t">진짜 자취 지출 시뮬레이션</p>

      <p class="sim-sub">이 매물 예상 지출</p>
      <div class="srow"><span>월세 + 관리비</span><span class="v">{{ formatManwon(sim.monthlyHousingCost.rentAndFee) }}</span></div>
      <div class="srow"><span>공과금 (KOSIS)</span><span class="v">{{ formatManwon(sim.monthlyHousingCost.utilityFee) }}</span></div>
      <div class="divider" />
      <div class="srow"><span>매달 주거비</span><span class="v b">{{ formatManwon(sim.monthlyHousingCost.total) }}</span></div>

      <div class="divider wide" />
      <p class="sim-sub">소비 패턴 (후회소비 분석)</p>
      <div class="srow"><span>월 평균 지출</span><span class="v">{{ formatManwon(sim.userSpending.avgMonthlySpending) }}</span></div>
      <div class="srow"><span>후회 소비</span><span class="v">{{ formatManwon(sim.userSpending.avgRegretSpending) }}/월</span></div>

      <div class="box">
        <div class="row"><span class="muted">진짜 필요한 월 자금</span><span class="b">{{ formatManwon(sim.totalMonthlyNeed) }}</span></div>
        <div class="row"><span class="muted">만기금으로</span><span class="b">{{ sim.possibleMonths }}개월 자취 가능</span></div>
        <div class="row"><span class="muted">후회 소비 줄이면</span><span class="b">{{ sim.reducedPossibleMonths }}개월 가능</span></div>
      </div>
    </BaseCard>

    <BottomButtonBar primary-label="확인" @primary-click="confirm" />
  </div>
  <p v-else class="loading">불러오는 중...</p>
</template>

<style scoped>
.goal {
  padding: 20px 0 88px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
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
  font-size: 11px;
  color: var(--text-muted);
}
.badge {
  flex-shrink: 0;
  font-size: 11px;
  padding: 4px 10px;
  border: 1px solid var(--line);
  border-radius: 999px;
  color: var(--text-muted);
}
.map {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 150px;
  border-radius: 8px;
  background: var(--kb-gray-pale);
  color: var(--text-muted);
  font-size: 13px;
}
.cap {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-body);
  margin-bottom: 8px;
}
.row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-body);
  margin-bottom: 6px;
}
.muted {
  color: var(--text-muted);
}
.sim-t {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
  margin-bottom: 10px;
}
.sim-sub {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-body);
  margin-bottom: 6px;
}
.srow {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 3px;
  padding-left: 8px;
}
.srow .v {
  color: var(--text-body);
  font-weight: 600;
}
.srow .v.b {
  font-weight: 700;
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
.divider.wide {
  margin: 12px 0;
}
.box {
  margin-top: 12px;
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fafafa;
}
.loading {
  padding: 60px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
/* 버튼 색은 BottomButtonBar 컴포넌트 기본값 사용 */
</style>
