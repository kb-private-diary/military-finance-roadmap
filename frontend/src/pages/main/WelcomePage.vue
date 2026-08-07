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
      <img :src="welcomeImg" alt="텅장일병 마스코트" class="promo__mascot" />
      <h1 class="promo__name">텅장일병구하기</h1>
      <p class="promo__tagline">전역까지, 텅장 탈출 대작전<br />군적금부터 자취까지 한 번에</p>
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
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 0 20px 96px;
  text-align: center;
}
.promo__mascot {
  width: 160px;
  height: auto;
}
.promo__name {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-strong);
}
.promo__tagline {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-muted);
}
</style>
