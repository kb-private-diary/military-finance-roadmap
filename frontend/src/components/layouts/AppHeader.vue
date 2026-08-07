<!--
  AppHeader - 모든 화면 공통 고정 헤더 (챗봇 포함, 예외 없음)

  탭바 유무(showTabNav)와 무관하게 헤더 내용은 항상 동일합니다.
    왼쪽: < (미니앱 나가기) + "텅장일병구하기"
    오른쪽: 챗봇 / 마이페이지 / 홈 3아이콘 고정
-->
<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

// 앱 타이틀 고정값
const APP_NAME = '텅장일병구하기';

const router = useRouter();

// 읽지 않은 알림 존재 여부
// true  : 종 아이콘 우측 상단에 빨간 점 표시
// false : 빨간 점 숨김
// TODO: 알림 API 연동 후 unreadCount > 0 결과로 변경 예정
const hasNotification = ref(true);

const goExit = () => {
  // TODO: 실제 KB Star Banking 앱 연동 시 네이티브 브릿지(예: KBBridge.exit())로 교체 예정.
  // 현재는 독립 웹앱 시뮬레이션이라 시작 화면(Welcome)으로 이동시킴.
  router.push({ path: '/' });
};

// 알림 화면 이동
const goNotification = () => {
  router.push({ name: 'WebPush' });
};

const goChat = () => {
  // 라우트 정의서 SCR-CHAT-01 (/chat) 기준
  router.push({ name: 'Chat' });
};

const goMyPage = () => {
  // 라우트 정의서 SCR-MYP-01 (/mypage) 기준
  router.push({ name: 'MyPage' });
};

const goHome = () => {
  router.push({ name: 'Home' });
};
</script>

<template>
  <header class="app-header">
    <div class="app-header__left">
      <button
        class="app-header__icon-btn"
        type="button"
        aria-label="미니앱 나가기"
        @click="goExit"
      >
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M15 18L9 12L15 6"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>

      <h1 class="app-header__title">{{ APP_NAME }}</h1>
    </div>

    <div class="app-header__right">
      <button
        class="app-header__icon-btn"
        type="button"
        aria-label="알림"
        @click="goNotification"
      >
        <span v-if="hasNotification" class="notification-badge"></span>

        <svg
          class="notification-icon"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M6.5 10V8.5C6.5 5.46 8.96 3 12 3C15.04 3 17.5 5.46 17.5 8.5V10C17.5 12.35 18.2 13.75 19.25 15.05C19.72 15.63 19.31 16.5 18.56 16.5H5.44C4.69 16.5 4.28 15.63 4.75 15.05C5.8 13.75 6.5 12.35 6.5 10Z"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path
            d="M10 19C10.45 19.62 11.16 20 12 20C12.84 20 13.55 19.62 14 19"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
        </svg>
      </button>

      <button
        class="app-header__icon-btn"
        type="button"
        aria-label="챗봇"
        @click="goChat"
      >
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M12 5V7.5"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
          <circle cx="12" cy="4.2" r="1" fill="currentColor" />
          <rect
            x="4.5"
            y="7.5"
            width="15"
            height="12"
            rx="4"
            stroke="currentColor"
            stroke-width="2"
          />
          <circle cx="9.5" cy="13.5" r="1.2" fill="currentColor" />
          <circle cx="14.5" cy="13.5" r="1.2" fill="currentColor" />
          <path
            d="M2.5 12V15"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
          <path
            d="M21.5 12V15"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
        </svg>
      </button>

      <button
        class="app-header__icon-btn"
        type="button"
        aria-label="마이페이지"
        @click="goMyPage"
      >
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle
            cx="12"
            cy="8"
            r="3.4"
            stroke="currentColor"
            stroke-width="2"
          />
          <path
            d="M5 20C5 16.5 8 14 12 14C16 14 19 16.5 19 20"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
        </svg>
      </button>

      <button
        class="app-header__icon-btn"
        type="button"
        aria-label="홈으로"
        @click="goHome"
      >
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M4 11L12 4L20 11V19C20 19.5523 19.5523 20 19 20H15V14H9V20H5C4.44772 20 4 19.5523 4 19V11Z"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 20px;
  background-color: var(--surface-default);
  flex-shrink: 0;
}

.app-header__left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.app-header__right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.app-header__icon-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  background: none;
  border: none;
  color: var(--text-strong);
  cursor: pointer;
  flex-shrink: 0;
}

.notification-icon {
  transform: translateY(1px);
}

.notification-badge {
  position: absolute;
  top: 0px;
  right: -2px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--notification);
  border: 2px solid var(--surface-default);
}

.app-header__icon-btn svg {
  display: block;
  width: 23px;
  height: 23px;
}

.app-header__icon-btn:hover {
  color: var(--kb-yellow-deep);
}

.app-header__title {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 15px;
  font-weight: 600;
  line-height: 1;
  color: var(--text-strong);
}
</style>
