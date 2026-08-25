<script setup>
// SCR-COM-01 · 미니앱 진입 (로그인 전 완전 진입 페이지)  담당: 수연
// 서비스 홍보 → [텅장일병구하기 시작하기] → 로그인
// 로그인 전 단계라 공통 헤더(서비스 3아이콘 메뉴)는 hideHeader로 끄고
// 진입 전용 최소 헤더(나가기 + 앱명)만 노출함
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import welcomeImg from '@/assets/images/welcome-kbfriends.png';

const router = useRouter();
const auth = useAuthStore();

const goExit = () => {
  // TODO: KB Star Banking 네이티브 브릿지(KBBridge.exit())로 교체 예정, 지금은 웹 시뮬레이션임
  if (window.history.length > 1) router.back();
};
// 로그인 상태면 바로 홈, 아니면 로그인 화면으로
const goStart = () => router.push({ name: auth.isLogin ? 'Home' : 'Login' });

// 진입 화면에서 홍보할 핵심 기능 (하단 네비게이터 메뉴 기반)
const features = [
  {
    icon: '🎯',
    tint: 'roadmap',
    title: '전역까지 목돈 로드맵',
    desc: '여행·자취·내 차, 목표별 D-Day 작전을 자동으로 세워줘요',
  },
  {
    icon: '🧮',
    tint: 'simulator',
    title: '목돈작전 시뮬레이션',
    desc: '적금·목표 금액으로 얼마나 모을 수 있는지 미리 계산해요',
  },
  {
    icon: '💸',
    tint: 'regret',
    title: '후회소비 점호',
    desc: '매일 새는 돈을 점호로 잡아 저축으로 돌려세워요',
  },
  {
    icon: '🎖️',
    tint: 'social',
    title: '전우 저축 전황',
    desc: '동기들과 저축률을 비교하며 1등 자리에 도전해요',
  },
  {
    icon: '⏳',
    tint: 'dday',
    title: '전역 D-Day 현황',
    desc: '전역까지 남은 날과 복무 현황을 한눈에 확인해요',
  },
  {
    icon: '🤖',
    tint: 'chat',
    title: 'AI 금융 비서',
    desc: '군 생활 돈 고민, AI 전우에게 언제든 물어보세요',
  },
];
</script>

<template>
  <div class="welcome">
    <!-- 진입 전용 최소 헤더 (서비스 메뉴 없음) -->
    <header class="top">
      <button class="top__back" type="button" aria-label="나가기" @click="goExit">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round" />
        </svg>
      </button>
      <span class="top__title">텅장일병구하기</span>
    </header>

    <!-- 서비스 홍보 -->
    <div class="promo">
      <div class="promo__hero">
        <img :src="welcomeImg" alt="텅장일병 마스코트" class="promo__mascot" />
        <h1 class="promo__name">텅장일병구하기</h1>
        <p class="promo__tagline">전역까지, 텅장 탈출 대작전<br />군적금부터 자취까지 한 번에</p>
      </div>

      <ul class="promo__features">
        <li v-for="f in features" :key="f.title" class="promo__feature">
          <span class="promo__feature-icon" :class="`is-${f.tint}`">{{ f.icon }}</span>
          <div class="promo__feature-text">
            <p class="promo__feature-title">{{ f.title }}</p>
            <p class="promo__feature-desc">{{ f.desc }}</p>
          </div>
        </li>
      </ul>
    </div>

    <BottomButtonBar primary-label="텅장일병구하기 시작하기" @primary-click="goStart" />
  </div>
</template>

<style scoped>
.welcome {
  display: flex;
  flex-direction: column;
  /* app-content 좌우 패딩(20px)을 상쇄해 헤더/홍보 영역을 프레임 폭 전체로 */
  margin: 0 -20px;
  min-height: 100%;
}
.top {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 56px;
  padding: 0 20px;
  flex-shrink: 0;
}
.top__back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  border: 0;
  background: none;
  color: var(--text-strong);
  cursor: pointer;
}
.top__back svg {
  width: 23px;
  height: 23px;
}
.top__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-strong);
}
.promo {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding: 80px 20px 96px;
}
.promo__hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  text-align: center;
}
.promo__mascot {
  width: 100%;
  max-width: 300px;
  height: auto;
}
.promo__name {
  font-size: 27px;
  font-weight: 800;
  color: var(--text-strong);
}
.promo__tagline {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-muted);
}

.promo__features {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}
.promo__feature {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 15px 16px;
  background-color: var(--surface-subtle);
  border-radius: 14px;
}
.promo__feature-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  font-size: 22px;
}
.promo__feature-icon.is-roadmap {
  background-color: var(--military-green-light);
}
.promo__feature-icon.is-simulator {
  background-color: var(--brown-pale-bg);
}
.promo__feature-icon.is-regret {
  background-color: #fce4e4;
}
.promo__feature-icon.is-social {
  background-color: #e3eef7;
}
.promo__feature-icon.is-dday {
  background-color: var(--kb-yellow-pale);
}
.promo__feature-icon.is-chat {
  background-color: #ece7f9;
}
.promo__feature-title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}
.promo__feature-desc {
  margin: 4px 0 0;
  font-size: 12.5px;
  line-height: 1.5;
  color: var(--text-muted);
}
</style>
