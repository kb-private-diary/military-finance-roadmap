<script setup>
// SCR-REG-07 · 절감 챌린지 + 적금 추천  담당: 수연
// 이번달 후회 소비(getMonthlyStats)만 실데이터로 연동. 목표·진행·적금추천 API는 미구현 → 0·빈 상태.
//    TODO(백엔드): 절감 목표 CRUD, 진행률 집계, 카카오 알림 발송, 적금 상품 추천 API
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import regretApi from '@/api/regretApi';
import { formatWon, formatManwon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import ProgressBar from '@/components/common/ProgressBar.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import { useToast } from '@/composables/useToast';

const router = useRouter();
const { show: showToast } = useToast();
const goBack = () => router.push({ name: 'RegretDashboard' });

const now = new Date();
const yearMonth = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`;
const monthLabel = `${now.getMonth() + 1}월`;

// 이번달 후회 소비(실데이터). 목표·아낀 금액은 저장 API 미구현 → 0
const currentRegret = ref(0);
const goal = ref(0);
const savedSoFar = ref(0);
const loading = ref(true);

// TODO(백엔드): 적금 상품 추천 API 나오면 연동. 현재는 빈 상태
const savings = ref([]);

const load = async () => {
  loading.value = true;
  try {
    const d = await regretApi.getMonthlyStats(yearMonth);
    currentRegret.value = d?.regretAmount ?? 0;
  } catch {
    currentRegret.value = 0;
    showToast('후회 소비를 불러오지 못했어요', 'error');
  } finally {
    loading.value = false;
  }
};
onMounted(load);

// 목표 절감액 = 현재 후회 - 목표치 (양수면 이만큼 줄여야 함)
const targetCut = computed(() => Math.max(currentRegret.value - goal.value, 0));
// 진행률: 목표 대비 지금까지 아낀 비율
const progress = computed(() =>
  targetCut.value > 0
    ? Math.min(Math.round((savedSoFar.value / targetCut.value) * 100), 100)
    : 0,
);
// 절감분으로 1년 적금하면? (간단 환산, 이자 제외)
const yearlySaving = computed(() => targetCut.value * 12);

// TODO(백엔드): 목표 저장 API 붙이면 연동. 지금은 로컬 상태만
const adjustGoal = (delta) => {
  const next = goal.value + delta;
  if (next < 0 || next >= currentRegret.value) return;
  goal.value = next;
};
</script>

<template>
  <div class="challenge">
    <header class="head">
      <p class="cap">절감 챌린지</p>
      <h2 class="title">{{ monthLabel }} 후회 소비 줄이기</h2>
      <p class="dev-note">⚠️ 준비 중인 기능이에요</p>
    </header>

    <!-- 절감 목표 카드 -->
    <BaseCard padding="18px 16px">
      <div class="goal-top">
        <div>
          <p class="g-cap">이번달 후회 소비</p>
          <p class="g-cur">{{ formatManwon(currentRegret) }}</p>
        </div>
        <span class="g-arrow">→</span>
        <div class="g-target">
          <p class="g-cap">목표</p>
          <p class="g-goal">{{ formatManwon(goal) }}</p>
        </div>
      </div>

      <div class="adjust">
        <button @click="adjustGoal(-10000)">−1만</button>
        <span class="adj-tx">목표 {{ formatWon(goal) }}</span>
        <button @click="adjustGoal(10000)">+1만</button>
      </div>

      <div class="prog-wrap">
        <ProgressBar
          :value="savedSoFar"
          :total="targetCut"
          color="var(--success)"
          :height="14"
        />
        <div class="prog-foot">
          <span>{{ formatWon(savedSoFar) }} 아꼈어요</span>
          <span class="prog-pct">{{ progress }}%</span>
        </div>
      </div>

      <p class="goal-hint">
        후회 소비를 <b>{{ formatWon(targetCut) }}</b> 줄이면 목표 달성!
      </p>
    </BaseCard>

    <!-- 절감분으로 추천 적금 -->
    <div class="sec-head">
      <p class="sec">아낀 돈으로 적금 어때요?</p>
      <span class="sec-sub">1년이면 {{ formatManwon(yearlySaving) }}</span>
    </div>
    <BaseCard
      v-for="(p, i) in savings"
      :key="i"
      padding="16px"
    >
      <div class="sv">
        <div class="sv-tx">
          <div class="sv-name-row">
            <span class="sv-name">{{ p.name }}</span>
            <span class="sv-tag">{{ p.tag }}</span>
          </div>
          <span class="sv-desc">{{ p.desc }}</span>
        </div>
        <div class="sv-right">
          <span class="sv-rate">{{ p.rate }}</span>
          <span class="sv-go">자세히 ›</span>
        </div>
      </div>
    </BaseCard>
    <BaseCard v-if="!savings.length" padding="16px">
      <p class="sv-empty">추천 적금 상품은 준비 중이에요</p>
    </BaseCard>

    <!-- 카카오 알림 안내 배너 -->
    <div class="kakao">
      <span class="kk-ico">💬</span>
      <span class="kk-tx">
        <b>매주 절감 현황을 카톡으로 알려드려요</b>
        <span>알림 설정은 준비 중이에요</span>
      </span>
    </div>

    <BottomButtonBar primary-label="확인" @primary-click="goBack" />
  </div>
</template>

<style scoped>
.challenge {
  padding: 20px 0 88px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* 만족/애매/후회 파스텔톤 (colors.css pastel 재사용) - 진행바 공통 */
  --success: #9cd495; /* 만족 파스텔 그린 */
  --soso: #fbd55b;    /* 애매 파스텔 옐로 */
  --danger: #f8a5a5;  /* 후회 파스텔 핑크 */
}
.head {
  display: flex;
  flex-direction: column;
  gap: 3px;
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
.dev-note {
  margin-top: 2px;
  font-size: 11px;
  color: var(--text-hint);
}
/* ── 절감 목표 ── */
.goal-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.g-cap {
  font-size: 11px;
  color: var(--text-muted);
}
.g-cur {
  margin-top: 3px;
  font-size: 22px;
  font-weight: 800;
  color: var(--danger-text, #d97676);
}
.g-arrow {
  font-size: 18px;
  color: var(--text-hint);
}
.g-target {
  text-align: right;
}
.g-goal {
  margin-top: 3px;
  font-size: 22px;
  font-weight: 800;
  color: var(--text-strong);
}
.adjust {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin: 16px 0;
}
.adjust button {
  padding: 6px 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  color: var(--text-body);
  font-size: 12px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
}
.adj-tx {
  font-size: 12px;
  color: var(--text-muted);
  min-width: 96px;
  text-align: center;
}
.prog-wrap {
  display: flex;
  flex-direction: column;
  gap: 7px;
}
.prog-foot {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-muted);
}
.prog-pct {
  font-weight: 800;
  color: var(--text-strong);
}
.goal-hint {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--gray-pale-bg);
  font-size: 12px;
  color: var(--text-body);
  text-align: center;
}
.goal-hint b {
  color: var(--text-strong);
  font-weight: 800;
}
/* ── 추천 적금 ── */
.sec-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 4px;
}
.sec {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
}
.sec-sub {
  font-size: 11px;
  color: var(--text-muted);
}
.sv {
  display: flex;
  align-items: center;
  gap: 12px;
}
.sv-empty {
  font-size: 12px;
  color: var(--text-hint);
  text-align: center;
}
.sv-ico {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 11px;
  background: var(--kb-yellow-pale);
  font-size: 20px;
}
.sv-tx {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.sv-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}
.sv-name {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
}
.sv-tag {
  font-size: 9px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 999px;
  background: var(--military-green-light);
  color: var(--military-green);
}
.sv-desc {
  font-size: 11px;
  color: var(--text-muted);
}
.sv-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 3px;
  flex-shrink: 0;
}
.sv-rate {
  font-size: 14px;
  font-weight: 800;
  color: var(--brand-gold);
}
.sv-go {
  font-size: 10px;
  color: var(--text-hint);
}
/* ── 카카오 알림 배너 ── */
.kakao {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 12px;
  background: #fef7cd;
  border: 1px solid #fbe884;
}
.kk-ico {
  font-size: 22px;
}
.kk-tx {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.kk-tx b {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
}
.kk-tx span {
  font-size: 11px;
  color: var(--text-muted);
}
</style>
