<script setup>
// SCR-REG-07 · 절감 챌린지 + 적금 추천  담당: 수연
// ⚠️ 백엔드 미구현 화면 - 전부 SAMPLE 하드코딩. 실 API 나오면 아래 SAMPLE 걷어내고 연동할 것
//    TODO(백엔드): 절감 목표 CRUD, 진행률 집계, 카카오 알림 발송, 적금 상품 추천 API
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { formatWon, formatManwon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import ProgressBar from '@/components/common/ProgressBar.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();
const goBack = () => router.push({ name: 'RegretDashboard' });

// TODO(백엔드): 아래 전부 SAMPLE. 실 API 연동 시 교체
const SAMPLE = {
  currentRegret: 120000, // 이번달 후회 소비 (regret stats 에서 가져올 값)
  goal: 80000,           // 절감 목표 (사용자가 설정)
  savedSoFar: 40000,     // 목표까지 아낀 금액 (currentRegret 감소분)
  monthLabel: '8월',
};

// 추천 적금 (외부 상품 링크 톤). TODO(백엔드): 상품 추천 API 로 교체
const SAMPLE_SAVINGS = [
  {
    name: 'KB 두근두근 적금',
    rate: '연 4.5%',
    desc: '군 장병 우대금리 · 월 최대 30만원',
    tag: '군인 우대',
  },
  {
    name: 'KB 청년도약 적금',
    rate: '연 4.0%',
    desc: '만 19~34세 · 자유적립식',
    tag: '청년 전용',
  },
];

const goal = ref(SAMPLE.goal);
const currentRegret = ref(SAMPLE.currentRegret);
const savedSoFar = ref(SAMPLE.savedSoFar);

// 목표 절감액 = 현재 후회 - 목표치 (양수면 이만큼 줄여야 함)
const targetCut = computed(() => Math.max(currentRegret.value - goal.value, 0));
// 진행률: 목표 대비 지금까지 아낀 비율
const progress = computed(() =>
  targetCut.value > 0
    ? Math.min(Math.round((savedSoFar.value / targetCut.value) * 100), 100)
    : 0,
);
// 절감분으로 1년 적금하면? (간단 환산, 이자 제외 SAMPLE)
const yearlySaving = computed(() => targetCut.value * 12);

// TODO(백엔드): 목표 조정 슬라이더/입력 붙이면 실제 저장 연동. 지금은 로컬 상태만
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
      <h2 class="title">{{ SAMPLE.monthLabel }} 후회 소비 줄이기</h2>
      <p class="dev-note">⚠️ 준비 중인 기능이에요 (미리보기)</p>
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
      v-for="(p, i) in SAMPLE_SAVINGS"
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
