<script setup>
// SCR-REG-03 · 후회소비 대시보드  담당: 수연
// 이번달 후회/만족 요약 + 후회 캘린더 히트맵 + 카테고리 순위 + 자취 연동 (담백 버전)
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import regretApi from '@/api/regretApi';
import roadmapApi from '@/api/roadmapApi';
import rentApi from '@/api/rentApi';
import { formatManwon, formatWon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import DonutChart from '@/components/common/DonutChart.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import { useToast } from '@/composables/useToast';

const router = useRouter();
const { show: showToast } = useToast();

const now = new Date();
const yearMonth = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`;
const monthLabel = `${now.getMonth() + 1}월`;

// 후회 캘린더가 보고 있는 월 (이전/다음 버튼으로 이동, 미래월은 이번달까지)
const calViewDate = ref(new Date(now.getFullYear(), now.getMonth(), 1));

const CATEGORY_LABEL = {
  FOOD: '식비',
  CAFE: '카페/간식',
  SHOPPING: '쇼핑',
  TRANSPORT: '교통',
  CULTURE: '문화/여가',
  CONVENIENCE: '편의점',
  PX: 'PX·군마트',
  DELIVERY: '배달',
  GAME: '게임',
  VACATION: '휴가/여행',
  ETC: '기타',
};

// 후회소비 전체 실데이터 (findSpendings). 캘린더 월 이동 시 재조회 없이 이 데이터에서 재집계
const allSpendings = ref([]);

// 캘린더가 보고 있는 월의 일자별 후회소비 합계 (히트맵 농담 계산용) — { [day]: 합계원 }
// 데이터 없으면 빈 객체 → 전부 level 0
const dailyRegretAmount = computed(() => {
  const y = calViewDate.value.getFullYear();
  const m = calViewDate.value.getMonth();
  const map = {};
  (allSpendings.value || []).forEach((s) => {
    if (s.reviewType !== 'REGRET') return;
    const dt = new Date(s.spentAt);
    if (dt.getFullYear() !== y || dt.getMonth() !== m) return;
    const day = dt.getDate();
    map[day] = (map[day] || 0) + (s.amount || 0);
  });
  return map;
});

// 후회금액 합계 → 히트맵 레벨(0~4) 매핑
//   0원=0(회색) / ~1만=1 / ~3만=2 / ~5만=3 / 5만 초과=4
const regretAmountToLevel = (amt) => {
  if (!amt || amt <= 0) return 0;
  if (amt <= 10000) return 1;
  if (amt <= 30000) return 2;
  if (amt <= 50000) return 3;
  return 4;
};

const stats = ref(null);
const months = ref([]);
const loading = ref(true);

// 이번달 일자별 후회소비 집계 (실데이터). 실패해도 히트맵은 빈 상태로 방어(토스트 중복 방지)
const loadDaily = async () => {
  try {
    allSpendings.value = (await regretApi.findSpendings()) || [];
  } catch {
    allSpendings.value = [];
  }
};

const load = async () => {
  loading.value = true;
  try {
    const d = await regretApi.getMonthlyStats(yearMonth);
    // 실데이터 없거나 형식 미달이면 null 유지 → 템플릿이 빈 상태로 방어
    stats.value = d && d.totalSpending != null ? d : null;
  } catch {
    stats.value = null;
    showToast('후회소비 통계를 불러오지 못했어요', 'error');
  } finally {
    loading.value = false;
  }
};

// 최근 4개월 통계 조회 → 월별 비교 막대 (실데이터만 사용, 실패 시 빈 배열)
const loadMonths = async () => {
  const list = [];
  for (let i = 3; i >= 0; i -= 1) {
    const dt = new Date(now.getFullYear(), now.getMonth() - i, 1);
    const ym = `${dt.getFullYear()}${String(dt.getMonth() + 1).padStart(2, '0')}`;
    list.push({ ym, label: `${dt.getMonth() + 1}월` });
  }
  try {
    const res = await Promise.all(
      list.map((m) => regretApi.getMonthlyStats(m.ym)),
    );
    months.value = list
      .map((m, i) => ({ m, r: res[i] }))
      .filter(({ r }) => r && r.totalSpending != null)
      .map(({ m, r }) => ({
        label: m.label,
        total: r.totalSpending,
        regret: r.regretAmount,
        soso: r.sosoAmount,
        satisfied: r.satisfiedAmount,
      }));
  } catch {
    months.value = [];
  }
};

const barWidth = (v, t) => (t > 0 ? Math.round((v / t) * 100) : 0);

onMounted(() => {
  load();
  loadMonths();
  loadDaily();
  loadRentCost();
});

const donutItems = computed(() => {
  const s = stats.value;
  if (!s) return [];
  return [
    { label: '후회', value: s.regretAmount, color: 'var(--danger)' },
    { label: '애매', value: s.sosoAmount, color: 'var(--soso)' },
    { label: '만족', value: s.satisfiedAmount, color: 'var(--success)' },
  ].filter((i) => i.value > 0);
});

const ratioText = computed(() =>
  stats.value?.regretRatio != null ? `${stats.value.regretRatio}%` : null,
);

// 이번달 달력 셀 (앞쪽 빈칸 + 1~말일, 각 날짜에 지출 레벨)
const calendarCells = computed(() => {
  const y = calViewDate.value.getFullYear();
  const m = calViewDate.value.getMonth();
  const firstDow = new Date(y, m, 1).getDay(); // 0=일
  const daysInMonth = new Date(y, m + 1, 0).getDate();
  const cells = [];
  for (let i = 0; i < firstDow; i += 1) cells.push({ void: true });
  for (let d = 1; d <= daysInMonth; d += 1) {
    cells.push({ day: d, level: regretAmountToLevel(dailyRegretAmount.value[d]) });
  }
  return cells;
});

// 캘린더 월 이동 (미래월은 이번달까지만)
const calMonthLabel = computed(
  () => `${calViewDate.value.getFullYear()}년 ${calViewDate.value.getMonth() + 1}월`,
);
const canGoNextMonth = computed(() => {
  const v = calViewDate.value;
  return (
    v.getFullYear() < now.getFullYear() ||
    (v.getFullYear() === now.getFullYear() && v.getMonth() < now.getMonth())
  );
});
const goPrevMonth = () => {
  calViewDate.value = new Date(
    calViewDate.value.getFullYear(),
    calViewDate.value.getMonth() - 1,
    1,
  );
};
const goNextMonth = () => {
  if (!canGoNextMonth.value) return;
  calViewDate.value = new Date(
    calViewDate.value.getFullYear(),
    calViewDate.value.getMonth() + 1,
    1,
  );
};

const goReview = () => router.push({ name: 'RegretReview' });

// 라벨 클릭 → 라벨별 모아보기 (REGRET | SOSO | SATISFIED)
const goLabel = (label) =>
  router.push({ name: 'RegretLabelList', params: { label } });

// ── "이만큼 아끼면?" 로드맵 연결 ────────────────────────────────
// 이번달 후회소비를 절감액으로 보고, 4개 로드맵 목표에 보탰을 때 효과 환산
// TODO(백엔드): 각 로드맵 목표 잔여액 API 연동되면 가정치 대신 실값으로 계산
const saveMonthly = computed(() => stats.value?.regretAmount ?? 0);
const saveYear = computed(() => saveMonthly.value * 12);

// 저장된 자취 로드맵의 월 주거비 (하드코딩 대신 실데이터). 로드맵 없으면 null → 등록 유도 멘트
const rentMonthlyCost = ref(null);
const loadRentCost = async () => {
  try {
    const list = await roadmapApi.findRoadmapList('rent');
    if (list?.length) {
      const goal = await rentApi.findGoal(list[0].goalId);
      rentMonthlyCost.value =
        goal?.precisionSimulation?.monthlyHousingCost?.housingTotal ?? null;
    }
  } catch {
    rentMonthlyCost.value = null;
  }
};
// 저장된 자취 로드맵 있으면 실 주거비로 "N개월 더" 환산, 없으면 null (유도 멘트로 분기)
const extraRentMonths = computed(() => {
  if (!rentMonthlyCost.value) return null;
  return Math.max(Math.floor(saveYear.value / rentMonthlyCost.value), 1);
});

const roadmapCards = computed(() => [
  {
    key: 'rent',
    icon: '🏠',
    name: '자취',
    desc:
      extraRentMonths.value != null
        ? `1년 모으면 자취 ${extraRentMonths.value}개월 더`
        : '자취 로드맵 등록하고 분석 받아보기',
    route: 'RentGoalCreate',
  },
  {
    key: 'job',
    icon: '🎓',
    name: '진로',
    desc: `학원비·응시료에 ${formatManwon(saveYear.value)} 보태기`,
    route: 'JobGoalCreate',
  },
  {
    key: 'car',
    icon: '🚗',
    name: '자동차',
    desc: `차량 구입 자금 +${formatManwon(saveYear.value)}`,
    route: 'CarGoalCreate',
  },
  {
    key: 'travel',
    icon: '✈️',
    name: '여행',
    desc: `여행 경비 +${formatManwon(saveYear.value)}`,
    route: 'TravelGoalCreate',
  },
]);

const goRoadmap = (name) => router.push({ name });

// 캘린더 셀 클릭 → 해당 일자 지출 목록으로 이동 (void 셀은 무시)
const goDay = (c) => {
  if (c.void) return;
  const y = calViewDate.value.getFullYear();
  const m = String(calViewDate.value.getMonth() + 1).padStart(2, '0');
  const d = String(c.day).padStart(2, '0');
  router.push({ name: 'RegretDailySpending', params: { date: `${y}-${m}-${d}` } });
};
</script>

<template>
  <div v-if="stats" class="dash">
    <header class="head">
      <p class="cap">후회소비 리포트</p>
      <h2 class="title">{{ monthLabel }} 후회한 소비</h2>
    </header>

    <button class="review-invite" @click="goReview">
      <span class="ri-ico">📮</span>
      <span class="ri-tx">
        <b v-if="stats.untaggedCount">아직 점호 안 한 소비 {{ stats.untaggedCount }}건</b>
        <b v-else>이번 달 점호를 다 했어요</b>
        <span>{{ stats.untaggedCount ? '지금 점호하러 갈까요?' : '새 소비가 들어오면 알려줄게요' }}</span>
      </span>
      <span class="ri-go">›</span>
    </button>

    <BaseCard padding="16px 14px">
      <div class="hero">
        <div class="hero-num">
          <p class="hero-cap">이번달 후회한 소비 총액</p>
          <p class="hero-amt">{{ formatWon(stats.regretAmount) }}</p>
          <p v-if="ratioText" class="hero-sub">이번달 수입의 {{ ratioText }}</p>
        </div>
        <DonutChart
          :items="donutItems"
          :size="110"
          :thickness="22"
          chart-label="후회 대 만족 비율"
        />
      </div>
      <div class="legend">
        <span class="lg"><i class="dot" style="background: var(--danger)" />후회 {{ formatWon(stats.regretAmount) }}</span>
        <span class="lg"><i class="dot" style="background: var(--soso)" />애매 {{ formatWon(stats.sosoAmount) }}</span>
        <span class="lg"><i class="dot" style="background: var(--success)" />만족 {{ formatWon(stats.satisfiedAmount) }}</span>
      </div>
    </BaseCard>

    <div class="count-card">
      <button class="count-cell count-cell--regret" @click="goLabel('REGRET')">
        <span class="count-n">{{ stats.regretCount }}건</span>
        <span class="count-l">후회 ›</span>
      </button>
      <button class="count-cell count-cell--soso" @click="goLabel('SOSO')">
        <span class="count-n">{{ stats.sosoCount }}건</span>
        <span class="count-l">애매 ›</span>
      </button>
      <button class="count-cell count-cell--satisfied" @click="goLabel('SATISFIED')">
        <span class="count-n">{{ stats.satisfiedCount }}건</span>
        <span class="count-l">만족 ›</span>
      </button>
    </div>

    <BaseCard padding="14px">
      <div class="cal-head">
        <button class="cal-nav" type="button" aria-label="이전 달" @click="goPrevMonth">‹</button>
        <p class="sec cal-title">{{ calMonthLabel }} 후회 캘린더</p>
        <button
          class="cal-nav"
          type="button"
          aria-label="다음 달"
          :disabled="!canGoNextMonth"
          @click="goNextMonth"
        >›</button>
      </div>
      <div class="heat-head">
        <span>일</span><span>월</span><span>화</span><span>수</span><span>목</span><span>금</span><span>토</span>
      </div>
      <div class="heat">
        <div
          v-for="(c, i) in calendarCells"
          :key="i"
          class="cell"
          :class="c.void ? 'void' : `l${c.level}`"
          @click="goDay(c)"
        >
          {{ c.void ? '' : c.day }}
        </div>
      </div>
      <div class="heat-foot">
        <span>적음</span>
        <i style="background: var(--h0)" /><i style="background: var(--h1)" /><i style="background: var(--h2)" /><i style="background: var(--h3)" /><i style="background: var(--h4)" />
        <span>많음</span>
      </div>
    </BaseCard>

    <BaseCard v-if="stats.categoryRegrets?.length" padding="14px">
      <p class="sec">후회 많은 카테고리</p>
      <div v-for="(c, i) in stats.categoryRegrets" :key="i" class="crow">
        <span class="rank">{{ i + 1 }}</span>
        <span class="cname">{{ CATEGORY_LABEL[c.category] || c.category }}</span>
        <span class="ccount">{{ c.count }}건</span>
        <span class="camt">{{ formatWon(c.amount) }}</span>
      </div>
    </BaseCard>

    <BaseCard padding="14px">
      <div class="mb-head">
        <p class="sec">월별 비교</p>
        <span class="more">최근 4개월</span>
      </div>
      <template v-if="months.length">
        <div
          v-for="(m, i) in months"
          :key="i"
          class="mrow"
          :class="{ now: i === months.length - 1 }"
        >
          <span class="mn">{{ m.label }}</span>
          <span class="track">
            <i class="fs" :style="{ width: barWidth(m.satisfied, m.total) + '%' }" />
            <i class="fo" :style="{ width: barWidth(m.soso, m.total) + '%' }" />
            <i class="fr" :style="{ width: barWidth(m.regret, m.total) + '%' }" />
          </span>
          <span class="mv">{{ formatManwon(m.total) }}</span>
        </div>
      </template>
      <p v-else class="mb-empty">아직 월별 비교 데이터가 없어요</p>
      <div v-if="months.length" class="mb-foot">
        <span class="lg"><i class="dot" style="background: var(--success)" />만족</span>
        <span class="lg"><i class="dot" style="background: var(--soso)" />애매</span>
        <span class="lg"><i class="dot" style="background: var(--danger)" />후회</span>
      </div>
    </BaseCard>

    <!-- ── 이만큼 아끼면? (4개 로드맵 연결) ── -->
    <BaseCard padding="14px">
      <div class="save-head">
        <p class="sec">이만큼 아끼면?</p>
        <span class="more">1년이면 {{ formatManwon(saveYear) }}</span>
      </div>
      <p class="save-lead">
        이번달 후회 소비 <b>{{ formatManwon(saveMonthly) }}</b>을 아껴 로드맵 목표에 보태보세요
      </p>
      <div class="save-grid">
        <button
          v-for="c in roadmapCards"
          :key="c.key"
          class="save-card"
          @click="goRoadmap(c.route)"
        >
          <span class="sc-ico">{{ c.icon }}</span>
          <span class="sc-name">{{ c.name }}</span>
          <span class="sc-desc">{{ c.desc }}</span>
        </button>
      </div>
    </BaseCard>

    <!-- ── 리포트 / 절감목표 진입 ── -->
    <div class="nav-grid">
      <button class="nav-card" @click="goRoadmap('RegretReport')">
        <span class="nc-ico">📊</span>
        <span class="nc-tx">
          <b>이번달 리포트 보기</b>
          <span>월별 후회소비 자세히 분석</span>
        </span>
        <span class="nc-go">›</span>
      </button>
      <button class="nav-card" @click="goRoadmap('RegretChallenge')">
        <span class="nc-ico">🎯</span>
        <span class="nc-tx">
          <b>절감 목표 세우기</b>
          <span>이번달 절감 목표 + 적금 추천</span>
        </span>
        <span class="nc-go">›</span>
      </button>
    </div>
  </div>
  <p v-else-if="loading" class="loading">불러오는 중...</p>
  <EmptyState
    v-else
    title="아직 후회소비 데이터가 없어요"
    description="소비가 쌓이면 점호(만족/후회)를 통해 리포트를 채울 수 있어요"
  />
</template>

<style scoped>
.dash {
  padding: 20px 0 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* 히트맵 농담 (지출 적음 → 많음) */
  --h0: #ebedf0;
  --h1: #ffe9a8;
  --h2: #ffcf5c;
  --h3: #ff9f43;
  --h4: #e8544d;
  /* 만족/애매/후회 파스텔톤 (colors.css pastel 재사용) - 도넛·미니·월별 공통 */
  --success: #9cd495; /* 만족 파스텔 그린 */
  --soso: #fbd55b;    /* 애매 파스텔 옐로 */
  --danger: #f8a5a5;  /* 후회 파스텔 핑크 */
}
.head {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.cap {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  color: var(--text-muted);
}
.title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong);
}
.mb-empty {
  padding: 8px 0 2px;
  font-size: 12px;
  color: var(--text-hint);
  text-align: center;
}
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.hero-cap {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 4px;
}
.hero-amt {
  font-size: 26px;
  font-weight: 800;
  color: var(--text-strong);
}
.hero-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-muted);
}
.legend {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 14px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
}
.lg {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  color: var(--text-muted);
}
/* 범례를 버튼으로 (라벨별 모아보기 이동) */
button.lg {
  border: 0;
  background: transparent;
  padding: 0;
  font-family: inherit;
  cursor: pointer;
}
.lg-go {
  color: var(--text-hint);
  font-size: 13px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  display: inline-block;
}
/* 후회/애매/만족 건수 - 한 카드 3색 통합(세로 축소) */
.count-card {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--line);
}
.count-cell {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 7px 4px;
  border: 0;
  cursor: pointer;
  font-family: inherit;
}
.count-cell--regret {
  background: #f4d1d1;
}
.count-cell--soso {
  background: #ffffc3;
}
.count-cell--satisfied {
  background: #e1f3e0;
}
.count-n {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}
.count-l {
  font-size: 11px;
  color: var(--text-body);
}
.sec {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-body);
  margin-bottom: 10px;
}
/* ── 캘린더 월 이동 헤더 ── */
.cal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.cal-title {
  margin: 0;
}
.cal-nav {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 8px;
  background: var(--bg-subtle, #f2f2f5);
  color: var(--text-body);
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
}
.cal-nav:disabled {
  opacity: 0.3;
  cursor: default;
}

/* ── 캘린더 히트맵 ── */
.heat-head {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 4px;
}
.heat-head span {
  text-align: center;
  font-size: 9.5px;
  font-weight: 700;
  color: var(--text-hint);
}
.heat {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}
.heat .cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 5px;
  font-size: 10px;
  font-weight: 600;
  background: var(--h0);
  color: var(--text-hint);
  cursor: pointer;
}
.heat .cell.void {
  background: transparent;
  cursor: default;
}
.heat .cell.l1 { background: var(--h1); color: #8a6d1f; }
.heat .cell.l2 { background: var(--h2); color: #7a5a0f; }
.heat .cell.l3 { background: var(--h3); color: #fff; }
.heat .cell.l4 { background: var(--h4); color: #fff; }
.heat-foot {
  display: flex;
  align-items: center;
  gap: 5px;
  justify-content: flex-end;
  margin-top: 9px;
}
.heat-foot span {
  font-size: 9.5px;
  color: var(--text-hint);
  font-weight: 700;
}
.heat-foot i {
  width: 11px;
  height: 11px;
  border-radius: 3px;
  display: block;
}
/* ── 카테고리 순위 ── */
.crow {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-body);
  margin-bottom: 8px;
}
.rank {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: var(--kb-gray-pale);
  color: var(--text-muted);
  font-size: 10px;
  font-weight: 700;
}
.cname {
  flex: 1;
  min-width: 0;
}
.ccount {
  font-size: 11px;
  color: var(--text-hint);
}
.camt {
  font-weight: 600;
  color: var(--text-body);
}
/* ── 월별 비교 ── */
.mb-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.mb-head .sec {
  margin-bottom: 0;
}
.more {
  font-size: 10px;
  color: var(--text-hint);
}
.mrow {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.mn {
  width: 28px;
  font-size: 11px;
  color: var(--text-muted);
}
.track {
  flex: 1;
  height: 14px;
  border-radius: 999px;
  background: var(--gray-pale-bg);
  display: flex;
  overflow: hidden;
}
.track i {
  height: 100%;
  display: block;
}
.track .fs {
  background: var(--success);
}
.track .fo {
  background: var(--soso);
}
.track .fr {
  background: var(--danger);
}
.mv {
  width: 44px;
  text-align: right;
  font-size: 11px;
  font-weight: 700;
  color: var(--text-body);
  font-variant-numeric: tabular-nums;
}
.mrow.now .mn,
.mrow.now .mv {
  color: var(--text-strong);
  font-weight: 800;
}
.mb-foot {
  display: flex;
  gap: 14px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--line);
}
/* ── 이만큼 아끼면? (로드맵 연결) ── */
.save-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 8px;
}
.save-head .sec {
  margin-bottom: 0;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
}
.save-lead {
  font-size: 11px;
  color: var(--text-muted);
  margin-bottom: 12px;
  line-height: 1.5;
}
.save-lead b {
  color: var(--text-strong);
  font-weight: 800;
}
.save-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}
.save-card {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--kb-yellow-pale);
  text-align: left;
  font-family: inherit;
  cursor: pointer;
}
.sc-ico {
  font-size: 20px;
}
.sc-name {
  margin-top: 2px;
  font-size: 13px;
  font-weight: 800;
  color: var(--text-strong);
}
.sc-desc {
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.4;
}
/* ── 리포트 / 절감목표 진입 카드 ── */
.nav-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}
.nav-card {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 14px 16px;
  border: none;
  border-radius: 12px;
  background: #FFCC00; /* 점호박스와 통일(시연 지정색) */
  cursor: pointer;
  font-family: inherit;
  text-align: left;
}
.nc-ico {
  font-size: 20px;
}
.nc-tx {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.nc-tx b {
  font-size: 14px;
  font-weight: 800;
  color: var(--text-strong);
}
.nc-tx span {
  font-size: 11px;
  color: var(--text-muted);
}
.nc-go {
  font-size: 18px;
  color: var(--text-hint);
}
/* ── 점호 유도 배너 (주 액션) ── */
.review-invite {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 15px 16px;
  border: 0;
  border-radius: 12px;
  background: #FFCC00; /* 시연 요청 지정색 (R225 G204 B0) */
  cursor: pointer;
  font-family: inherit;
  text-align: left;
}
.ri-ico {
  font-size: 22px;
}
.ri-tx {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.ri-tx b {
  font-size: 14px;
  font-weight: 800;
  color: var(--text-strong);
}
.ri-tx span {
  font-size: 11px;
  color: var(--text-body);
}
.ri-go {
  font-size: 18px;
  color: var(--text-strong);
}
.loading {
  padding: 60px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
</style>
