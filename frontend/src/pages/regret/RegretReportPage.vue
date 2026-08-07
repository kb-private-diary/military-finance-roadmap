<script setup>
// SCR-REG-06 · 월별 리포트 상세  담당: 수연
// 월 이동 + 이번달 요약(도넛) + 카테고리별 후회/만족 비중 + 인사이트 (담백 버전)
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import regretApi from '@/api/regretApi';
import { formatManwon, formatWon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import DonutChart from '@/components/common/DonutChart.vue';

const router = useRouter();

const CATEGORY_LABEL = {
  FOOD: '식비',
  CAFE: '카페/간식',
  SHOPPING: '쇼핑',
  TRANSPORT: '교통',
  CULTURE: '문화/여가',
  CONVENIENCE: '편의점',
  ETC: '기타',
};

// 조회 중인 달 (오늘 기준, ◀ ▶ 로 이동)
const now = new Date();
const cursor = ref(new Date(now.getFullYear(), now.getMonth(), 1));
const yearMonth = computed(
  () =>
    `${cursor.value.getFullYear()}${String(cursor.value.getMonth() + 1).padStart(2, '0')}`,
);
const monthLabel = computed(() => `${cursor.value.getMonth() + 1}월`);
// 다음달로는 못 넘어가게(미래 데이터 없음)
const isThisMonth = computed(
  () =>
    cursor.value.getFullYear() === now.getFullYear() &&
    cursor.value.getMonth() === now.getMonth(),
);

// TODO: 백엔드 통계 API 연동 확인되면 샘플 폴백 제거
const SAMPLE = {
  totalSpending: 430000,
  regretAmount: 120000,
  sosoAmount: 60000,
  satisfiedAmount: 250000,
  regretCount: 5,
  sosoCount: 2,
  satisfiedCount: 9,
  categoryRegrets: [
    { category: 'SHOPPING', amount: 58000, count: 2, satisfied: 12000 },
    { category: 'CAFE', amount: 32000, count: 2, satisfied: 8000 },
    { category: 'FOOD', amount: 18000, count: 1, satisfied: 96000 },
    { category: 'ETC', amount: 30000, count: 1, satisfied: 14000 },
    { category: 'CULTURE', amount: 0, count: 0, satisfied: 45000 },
  ],
};

// 인사이트 문구용 지난달 대비(샘플). TODO: 실연동 시 전월 stats 조회로 계산
const SAMPLE_PREV_REGRET = 150000;

const stats = ref(null);
const prevRegret = ref(SAMPLE_PREV_REGRET);
const loading = ref(true);

const load = async () => {
  loading.value = true;
  stats.value = null;
  try {
    const d = await regretApi.getMonthlyStats(yearMonth.value);
    stats.value = d && d.totalSpending != null ? d : SAMPLE;
  } catch {
    stats.value = SAMPLE; // TODO: 폴백 제거
  } finally {
    loading.value = false;
  }
};

// 전월 후회금액 (인사이트 "N% 줄었어요" 계산용)
const loadPrev = async () => {
  const p = new Date(cursor.value.getFullYear(), cursor.value.getMonth() - 1, 1);
  const ym = `${p.getFullYear()}${String(p.getMonth() + 1).padStart(2, '0')}`;
  try {
    const d = await regretApi.getMonthlyStats(ym);
    prevRegret.value =
      d && d.regretAmount != null ? d.regretAmount : SAMPLE_PREV_REGRET;
  } catch {
    prevRegret.value = SAMPLE_PREV_REGRET;
  }
};

const move = (delta) => {
  if (delta > 0 && isThisMonth.value) return;
  cursor.value = new Date(
    cursor.value.getFullYear(),
    cursor.value.getMonth() + delta,
    1,
  );
  load();
  loadPrev();
};

onMounted(() => {
  load();
  loadPrev();
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

// 카테고리별 후회/만족 비중 막대 (후회 금액 큰 순 정렬)
const catRows = computed(() => {
  const list = stats.value?.categoryRegrets || [];
  return [...list]
    .map((c) => {
      const regret = c.amount || 0;
      const satisfied = c.satisfied || 0;
      const sum = regret + satisfied;
      return {
        category: c.category,
        count: c.count || 0,
        regret,
        satisfied,
        regretPct: sum > 0 ? Math.round((regret / sum) * 100) : 0,
      };
    })
    .sort((a, b) => b.regret - a.regret);
});

// 인사이트: 전월 대비 후회 소비 증감
const insight = computed(() => {
  const cur = stats.value?.regretAmount ?? 0;
  const prev = prevRegret.value ?? 0;
  if (!prev) {
    return { tone: 'flat', text: '전월 데이터가 없어 비교할 수 없어요' };
  }
  const diff = cur - prev;
  const pct = Math.round((Math.abs(diff) / prev) * 100);
  if (diff < 0) {
    return {
      tone: 'good',
      text: `지난달보다 후회 소비가 ${pct}% 줄었어요`,
      sub: `${formatWon(Math.abs(diff))} 아꼈어요`,
    };
  }
  if (diff > 0) {
    return {
      tone: 'bad',
      text: `지난달보다 후회 소비가 ${pct}% 늘었어요`,
      sub: `${formatWon(diff)} 더 후회했어요`,
    };
  }
  return { tone: 'flat', text: '지난달과 후회 소비가 같아요' };
});

const goReview = () => router.push({ name: 'RegretReview' });
const goBack = () => router.push({ name: 'RegretDashboard' });
</script>

<template>
  <div class="report">
    <header class="head">
      <p class="cap">후회소비 리포트</p>
      <div class="month-nav">
        <button class="nav-btn" aria-label="이전 달" @click="move(-1)">‹</button>
        <h2 class="title">{{ monthLabel }} 리포트</h2>
        <button
          class="nav-btn"
          :disabled="isThisMonth"
          aria-label="다음 달"
          @click="move(1)"
        >
          ›
        </button>
      </div>
    </header>

    <p v-if="loading" class="loading">불러오는 중...</p>

    <template v-else-if="stats">
      <!-- 이번달 요약 -->
      <BaseCard padding="16px 14px">
        <div class="hero">
          <div class="hero-num">
            <p class="hero-cap">{{ monthLabel }} 총지출</p>
            <p class="hero-amt">{{ formatManwon(stats.totalSpending) }}</p>
            <p class="hero-sub">후회 {{ formatWon(stats.regretAmount) }}</p>
          </div>
          <DonutChart
            :items="donutItems"
            :size="110"
            :thickness="22"
            chart-label="후회 대 만족 비율"
          />
        </div>
        <div class="legend">
          <span class="lg"><i class="dot" style="background: var(--danger)" />후회 {{ stats.regretCount }}건 · {{ formatWon(stats.regretAmount) }}</span>
          <span class="lg"><i class="dot" style="background: var(--soso)" />애매 {{ stats.sosoCount }}건 · {{ formatWon(stats.sosoAmount) }}</span>
          <span class="lg"><i class="dot" style="background: var(--success)" />만족 {{ stats.satisfiedCount }}건 · {{ formatWon(stats.satisfiedAmount) }}</span>
        </div>
      </BaseCard>

      <!-- 인사이트 -->
      <div class="insight" :class="insight.tone">
        <span class="in-ico">
          {{ insight.tone === 'good' ? '🌱' : insight.tone === 'bad' ? '🔥' : '📊' }}
        </span>
        <span class="in-tx">
          <b>{{ insight.text }}</b>
          <span v-if="insight.sub">{{ insight.sub }}</span>
        </span>
      </div>

      <!-- 카테고리별 후회/만족 비중 -->
      <BaseCard padding="14px">
        <p class="sec">카테고리별 후회 비중</p>
        <div v-for="c in catRows" :key="c.category" class="crow">
          <div class="crow-top">
            <span class="cname">{{ CATEGORY_LABEL[c.category] || c.category }}</span>
            <span class="ccount">{{ c.count }}건 후회</span>
            <span class="cpct">{{ c.regretPct }}%</span>
          </div>
          <span class="track">
            <i class="fr" :style="{ width: c.regretPct + '%' }" />
          </span>
          <div class="crow-amt">
            <span>후회 {{ formatWon(c.regret) }}</span>
            <span>만족 {{ formatWon(c.satisfied) }}</span>
          </div>
        </div>
        <p class="cat-note">채워진 만큼이 후회 비중이에요</p>
      </BaseCard>

      <button class="review-invite" @click="goReview">
        <span class="ri-ico">📮</span>
        <span class="ri-tx">
          <b>점호 안 한 소비가 있다면</b>
          <span>지금 점호하면 리포트가 더 정확해져요</span>
        </span>
        <span class="ri-go">›</span>
      </button>

      <button class="back-line" @click="goBack">이전</button>
    </template>
  </div>
</template>

<style scoped>
.report {
  padding: 20px 0 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* 만족/애매/후회 파스텔톤 (colors.css pastel 재사용) - 도넛·막대 공통 */
  --success: #9cd495; /* 만족 파스텔 그린 */
  --soso: #fbd55b;    /* 애매 파스텔 옐로 */
  --danger: #f8a5a5;  /* 후회 파스텔 핑크 */
}
.head {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.cap {
  font-size: 12px;
  color: var(--text-muted);
}
.month-nav {
  display: flex;
  align-items: center;
  gap: 14px;
}
.title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong);
  min-width: 78px;
  text-align: center;
}
.nav-btn {
  width: 30px;
  height: 30px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: var(--text-body);
  font-size: 17px;
  line-height: 1;
  cursor: pointer;
  font-family: inherit;
}
.nav-btn:disabled {
  opacity: 0.35;
  cursor: default;
}
/* ── 요약 ── */
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.hero-cap {
  font-size: 12px;
  color: var(--text-muted);
}
.hero-amt {
  margin-top: 2px;
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
  flex-direction: column;
  gap: 7px;
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
.dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  display: inline-block;
}
/* ── 인사이트 ── */
.insight {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 12px;
  border: 1px solid var(--line);
  background: var(--gray-pale-bg);
}
.insight.good {
  background: #eef7ea;
  border-color: var(--success);
}
.insight.bad {
  background: #fdeeee;
  border-color: var(--danger);
}
.in-ico {
  font-size: 22px;
}
.in-tx {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.in-tx b {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
}
.in-tx span {
  font-size: 11px;
  color: var(--text-muted);
}
/* ── 카테고리 막대 ── */
.sec {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-body);
  margin-bottom: 12px;
}
.crow {
  margin-bottom: 14px;
}
.crow:last-of-type {
  margin-bottom: 6px;
}
.crow-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.cname {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-body);
}
.ccount {
  font-size: 10px;
  color: var(--text-hint);
}
.cpct {
  font-size: 12px;
  font-weight: 700;
  color: var(--danger-text, #d97676);
  font-variant-numeric: tabular-nums;
}
.track {
  height: 8px;
  border-radius: 999px;
  background: var(--gray-pale-bg);
  display: flex;
  overflow: hidden;
}
.track .fr {
  height: 100%;
  display: block;
  border-radius: 999px;
  background: var(--danger);
}
.crow-amt {
  display: flex;
  justify-content: space-between;
  margin-top: 6px;
  font-size: 10px;
  color: var(--text-hint);
}
.cat-note {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid var(--line);
  font-size: 10px;
  color: var(--text-hint);
  text-align: center;
}
/* ── 점호 유도 배너 ── */
.review-invite {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 15px 16px;
  border: 0;
  border-radius: 12px;
  background: var(--kb-yellow);
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
.back-line {
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  color: var(--text-body);
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
}
</style>
