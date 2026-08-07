<script setup>
// SCR-RENT-05 · Step 5) 저장 후 상세보기  담당: 수연
// 여행 상세(TravelCostPage) 결 참고 → 요약 큐카드 + 총 필요자금 + 3탭 구조
// 담백 버전(색·이모지 최소). 비용 비율은 가로 막대(ProgressBar)로 통일, 자취 톤(chart-1/3/4).
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import rentApi from '@/api/rentApi';
import { formatWon, formatManwon, formatDate } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import ProgressBar from '@/components/common/ProgressBar.vue';

const route = useRoute();
const router = useRouter();
const goalId = Number(route.params.goalId);

// 편의시설 타입 라벨 (백엔드 type 코드 → 한글)
const FACILITY_LABEL = {
  SUBWAY: '지하철',
  BUS: '버스',
  CONVENIENCE: '편의점',
  HOSPITAL: '병원',
  MART: '마트',
};

// TODO: 백엔드 저장상세 API(WIP·확장 대기) 준비되면 샘플 폴백 제거
const SAMPLE = {
  goal: {
    createdAt: '2026-08-04',
    residenceMonths: 6,
    listing: {
      listingId: 1,
      buildingName: '부산대 앞 오피스텔',
      jibunAddress: '부산 금정구 장전동 123-45',
      floor: 5,
      area: 23.1,
      deposit: 5000000,
      monthlyRent: 450000,
      latitude: 35.2314,
      longitude: 129.0846,
    },
  },
  nearbyFacilities: [
    { type: 'SUBWAY', name: '부산대역', walkMinutes: 8 },
    { type: 'BUS', name: '장전동 버스정류장', walkMinutes: 3 },
    { type: 'CONVENIENCE', name: 'GS25 장전점', walkMinutes: 2 },
    { type: 'HOSPITAL', name: '금정한마음병원', walkMinutes: 10 },
    { type: 'MART', name: '홈플러스 장전점', walkMinutes: 15 },
  ],
  precisionSimulation: {
    monthlyHousingCost: { rentAndFee: 500000, utilityFee: 80000, total: 580000 },
    userSpending: { avgMonthlySpending: 430000, avgRegretSpending: 120000 },
    totalMonthlyNeed: 1010000,
    possibleMonths: 7.1,
    reducedPossibleMonths: 8.5,
  },
};

// 주거 금융상품 SAMPLE (우리 housing_product 추천 = 월세지원 / 보증금대출·이자지원)
// 중복수급 제한(exclusive_group)에 따라 성격별 섹션으로 나눠 보여줌
// TODO: 백엔드 GET /listings/{id}/products 연동 시 실데이터로 교체
const SAMPLE_PRODUCTS = [
  {
    caption: '월세 지원',
    items: [
      { name: '청년월세 특별지원', org: '국토교통부 · 월 최대 20만원 (12개월)', link: 'https://www.myhome.go.kr' },
      { name: '부산 청년 월세 지원금', org: '부산광역시 · 월 10만원 (10개월)', link: 'https://www.busan.go.kr' },
    ],
  },
  {
    caption: '보증금 대출·이자지원',
    items: [
      { name: '중소기업취업청년 전월세보증금대출', org: '주택도시기금 · 연 1.5%', link: 'https://nhuf.molit.go.kr' },
      { name: '청년전용 버팀목 전세자금대출', org: '주택도시기금 · 연 2%대', link: 'https://nhuf.molit.go.kr' },
    ],
  },
];

const goal = ref(null);
const facilities = ref([]);
const sim = ref(null);
const productSections = ref(SAMPLE_PRODUCTS);
const loading = ref(true);
const activeTab = ref('goal');

const TABS = [
  { key: 'goal', label: '선택한 목표' },
  { key: 'cost', label: '비용 계산' },
  { key: 'products', label: '금융상품' },
];

// 백엔드 findProducts(대출 상품) 응답을 섹션 레이아웃으로 정규화. 실패 시 SAMPLE 유지.
const normalizeProducts = (data) => {
  if (!data?.products?.length) return SAMPLE_PRODUCTS;
  return [
    {
      caption: '관련 금융상품',
      items: data.products.map((p) => ({
        name: p.productName,
        org: p.productType === 'POLICY' ? '정책상품' : 'KB국민',
        link: p.link || '#',
      })),
    },
  ];
};

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
  }

  // 금융상품은 별도 시도 → 실패/없으면 SAMPLE (탭 전환은 이미 받은 데이터로만 분기)
  try {
    const listingId = goal.value?.listing?.listingId;
    const months = goal.value?.residenceMonths || 6;
    const p = await rentApi.findProducts(goalId, listingId, months);
    productSections.value = normalizeProducts(p);
  } catch {
    productSections.value = SAMPLE_PRODUCTS;
  } finally {
    loading.value = false;
  }
};
onMounted(load);

// ── 파생값 ──────────────────────────────────────────────
const residenceMonths = computed(() => goal.value?.residenceMonths || 6);

// 총 예상 준비 비용 = 보증금 + (월 필요자금 × 거주개월)
const totalPrep = computed(() => {
  const s = sim.value;
  if (!s) return 0;
  const deposit = goal.value?.listing?.deposit || 0;
  return deposit + s.totalMonthlyNeed * residenceMonths.value;
});

// 도넛 항목: 월세+관리비 / 공과금 / 생활비 (합 = totalMonthlyNeed)
const costItems = computed(() => {
  const s = sim.value;
  if (!s) return [];
  return [
    { label: '월세+관리비', value: s.monthlyHousingCost.rentAndFee, color: 'var(--chart-1)' },
    { label: '공과금', value: s.monthlyHousingCost.utilityFee, color: 'var(--chart-3)' },
    { label: '생활비', value: s.userSpending.avgMonthlySpending, color: 'var(--chart-4)' },
  ];
});
const costTotal = computed(() =>
  costItems.value.reduce((sum, item) => sum + item.value, 0),
);
const percentOf = (value) =>
  costTotal.value ? Math.round((value / costTotal.value) * 100) : 0;

// 만기금 대비 감당: 목표 거주개월 대비 자취 가능 개월
const affordPercent = computed(() => {
  const s = sim.value;
  if (!s || !residenceMonths.value) return 0;
  return Math.min(Math.round((s.possibleMonths / residenceMonths.value) * 100), 100);
});

// ── 액션 ────────────────────────────────────────────────
const handleDelete = async () => {
  if (!window.confirm('저장한 자취 목표를 삭제할까요?')) return;
  try {
    // rentApi.deleteGoal 미구현 상태 대비 (백엔드 준비되면 정상 호출)
    if (typeof rentApi.deleteGoal === 'function') await rentApi.deleteGoal(goalId);
  } catch {
    // 데모: 백엔드 미구현 시 삭제 실패 무시하고 목록으로 이동
  } finally {
    router.push({ name: 'RoadmapMain' });
  }
};
const goHome = () => router.push({ name: 'Home' });
</script>

<template>
  <div v-if="goal" class="detail">
    <!-- 1) 요약 큐카드 -->
    <BaseCard class="cue" padding="16px">
      <span class="chip chip-cat">자취</span>
      <h2 class="cue-name">{{ goal.listing.buildingName }}</h2>
      <p class="cue-meta">
        저장일 {{ formatDate(goal.createdAt) }} · {{ goal.listing.jibunAddress }}
        ({{ goal.listing.floor }}층)
      </p>
      <div class="cue-chips">
        <span class="chip">군적금 만기 연동</span>
        <span class="chip">월세 지원 상품</span>
      </div>
    </BaseCard>

    <!-- 2) 총 필요자금 카드 -->
    <div class="total-box">
      <p class="total-cap">🧮 총 예상 준비 비용</p>
      <p class="total-amt">{{ formatWon(totalPrep) }}</p>
      <p class="total-sub">보증금 + {{ residenceMonths }}개월 생활 기준</p>
    </div>

    <!-- 3) pill 탭 -->
    <div class="tabs" role="tablist">
      <button
        v-for="t in TABS"
        :key="t.key"
        type="button"
        class="tab"
        :class="{ active: activeTab === t.key }"
        role="tab"
        :aria-selected="activeTab === t.key"
        @click="activeTab = t.key"
      >
        {{ t.label }}
      </button>
    </div>

    <!-- 탭 1: 선택한 목표 -->
    <section v-show="activeTab === 'goal'" class="pane">
      <div class="map" aria-label="매물 위치 지도">지도</div>

      <BaseCard padding="14px">
        <p class="cap">확정 매물 정보</p>
        <div class="row"><span class="muted">건물명</span><span class="v">{{ goal.listing.buildingName }}</span></div>
        <div class="row"><span class="muted">주소</span><span class="v">{{ goal.listing.jibunAddress }}</span></div>
        <div class="row"><span class="muted">면적</span><span class="v">{{ goal.listing.area }}㎡</span></div>
        <div class="row"><span class="muted">층</span><span class="v">{{ goal.listing.floor }}층</span></div>
        <div class="row"><span class="muted">보증금</span><span class="v">{{ formatManwon(goal.listing.deposit) }}</span></div>
        <div class="row"><span class="muted">월세</span><span class="v">{{ formatManwon(goal.listing.monthlyRent) }}</span></div>
      </BaseCard>

      <BaseCard padding="14px">
        <p class="cap">주변 편의시설</p>
        <div v-for="(f, i) in facilities" :key="i" class="row">
          <span class="muted">
            <span v-if="FACILITY_LABEL[f.type]" class="ftag">{{ FACILITY_LABEL[f.type] }}</span>
            {{ f.name }}
          </span>
          <span class="v">도보 {{ f.walkMinutes }}분</span>
        </div>
      </BaseCard>
    </section>

    <!-- 탭 2: 비용 계산 -->
    <section v-show="activeTab === 'cost'" class="pane">
      <BaseCard v-if="sim" padding="20px 18px">
        <p class="cap">월 자취 비용 비율</p>
        <ul class="breakdown">
          <li v-for="item in costItems" :key="item.label">
            <span class="breakdown__label">{{ item.label }}</span>
            <ProgressBar :value="item.value" :total="costTotal" :color="item.color" :height="8" />
            <strong>{{ percentOf(item.value) }}%</strong>
          </li>
        </ul>

        <div class="divider" />
        <div class="row"><span class="muted">월 필요 자금</span><span class="v b">{{ formatManwon(sim.totalMonthlyNeed) }}</span></div>
      </BaseCard>

      <!-- 만기금 대비 감당 -->
      <div v-if="sim" class="afford">
        <div class="afford-head">
          <span class="muted">만기금으로</span>
          <strong>{{ sim.possibleMonths }}개월 자취 가능</strong>
        </div>
        <ProgressBar :value="affordPercent" :total="100" color="var(--chart-1)" :height="8" />
        <p class="afford-sub">목표 {{ residenceMonths }}개월 · 후회소비 줄이면 {{ sim.reducedPossibleMonths }}개월까지 가능</p>
      </div>

      <p class="note">계산 결과는 예상 금액이며 실제와 다를 수 있습니다.</p>
    </section>

    <!-- 탭 3: 금융상품 -->
    <section v-show="activeTab === 'products'" class="pane">
      <BaseCard v-for="sec in productSections" :key="sec.caption" padding="14px">
        <p class="cap">{{ sec.caption }}</p>
        <a
          v-for="(p, i) in sec.items"
          :key="i"
          class="product"
          :href="p.link"
          target="_blank"
          rel="noopener noreferrer"
        >
          <span class="p-info">
            <span class="p-name">{{ p.name }}</span>
            <span class="p-org">{{ p.org }}</span>
          </span>
          <span class="p-link" aria-hidden="true">↗</span>
        </a>
      </BaseCard>
      <p class="veteran-note">군 복무 기간만큼 청년 지원 나이 요건이 연장돼요</p>
    </section>

    <BottomButtonBar
      secondary-label="삭제"
      primary-label="확인"
      @secondary-click="handleDelete"
      @primary-click="goHome"
    />
  </div>
  <p v-else class="loading">불러오는 중...</p>
</template>

<style scoped>
.detail {
  padding: 20px 0 88px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 1) 요약 큐카드 */
.cue {
  display: flex;
  flex-direction: column;
}
.chip {
  display: inline-block;
  font-size: 10px;
  padding: 3px 9px;
  border: 1px solid var(--line);
  border-radius: 999px;
  color: var(--text-muted);
}
.chip-cat {
  align-self: flex-start;
  border: 0;
  background: var(--rent-pink); /* 자취 = 분홍 테마 */
  color: #ffffff;
  font-weight: 700;
  margin-bottom: 8px;
}
.cue-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong);
}
.cue-meta {
  margin-top: 4px;
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.5;
}
.cue-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
}

/* 2) 총 필요자금 카드 (노란 톤) */
.total-box {
  padding: 18px;
  border-radius: 14px;
  background: var(--kb-yellow-pale);
  border: 1px solid var(--roadmap-stage-bg);
  text-align: center;
}
.total-cap {
  font-size: 12px;
  color: var(--text-body);
}
.total-amt {
  margin-top: 6px;
  font-size: 26px;
  font-weight: 700;
  color: var(--text-strong);
}
.total-sub {
  margin-top: 4px;
  font-size: 11px;
  color: var(--text-muted);
}

/* 3) pill 탭 */
.tabs {
  display: flex;
  gap: 6px;
  padding: 4px;
  border-radius: 999px;
  background: var(--gray-pale-bg);
}
.tab {
  flex: 1;
  height: 34px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--text-muted);
  font-family: inherit;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.tab.active {
  background: #ffffff;
  color: var(--text-strong);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

/* 탭 콘텐츠 공통 */
.pane {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
/* 매물 위치 지도 (카카오맵 자리 - 좌표 latitude/longitude 로 렌더 예정) */
.map {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 150px;
  border-radius: 12px;
  background: var(--kb-gray-pale);
  color: var(--text-muted);
  font-size: 13px;
}
.veteran-note {
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--military-green-light);
  color: var(--military-green);
  font-size: 11px;
  text-align: center;
}
.cap {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-body);
  margin-bottom: 10px;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  margin-bottom: 8px;
}
.row:last-child {
  margin-bottom: 0;
}
.muted {
  color: var(--text-muted);
}
.v {
  color: var(--text-body);
  font-weight: 600;
  text-align: right;
}
.v.b {
  font-weight: 700;
  color: var(--text-strong);
}
.ftag {
  display: inline-block;
  font-size: 10px;
  padding: 1px 6px;
  margin-right: 4px;
  border-radius: 4px;
  background: var(--gray-pale-bg);
  color: var(--text-muted);
}
.divider {
  height: 1px;
  background: var(--line);
  margin: 12px 0;
}

/* 비용 계산 - 가로 막대 breakdown (도넛 제거, ProgressBar로 통일) */
.breakdown {
  margin: 6px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.breakdown li {
  display: grid;
  grid-template-columns: 62px minmax(0, 1fr) 34px;
  align-items: center;
  gap: 9px;
  font-size: 12px;
  color: var(--text-body);
}
.breakdown__label {
  font-weight: 500;
}
.breakdown strong {
  font-size: 12px;
  font-weight: 600;
  text-align: right;
  color: var(--text-body);
}

/* 만기금 대비 감당 박스 */
.afford {
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--surface-subtle);
}
.afford-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
}
.afford-head strong {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
}
.afford-sub {
  margin-top: 8px;
  font-size: 11px;
  color: var(--text-muted);
}
.note {
  font-size: 10px;
  color: var(--text-hint);
  text-align: center;
}

/* 금융상품 - 상품명·기관·외부링크 */
.product {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-top: 1px solid var(--line);
  text-decoration: none;
}
.product:first-of-type {
  border-top: 0;
  padding-top: 0;
}
.p-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.p-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-body);
}
.p-org {
  font-size: 11px;
  color: var(--text-hint);
}
.p-link {
  flex-shrink: 0;
  font-size: 14px;
  color: var(--text-muted);
}

.loading {
  padding: 60px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
/* 버튼 색은 BottomButtonBar 컴포넌트 기본값 사용 (삭제=회색 보조, 확인=KB Yellow) */
</style>
