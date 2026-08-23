<!--
  AppLayout - 큰 프레임(헤더+탭바+컨텐츠) 공통 레이아웃

  App.vue에서 전역으로 이미 감싸고 있으므로, 각 페이지 컴포넌트에서
  AppLayout을 직접 import할 필요는 없습니다.
  탭바 노출은 해당 라우트 파일의 meta로 제어합니다.

  헤더(AppHeader)는 원칙적으로 모든 화면에서 항상 동일한 고정 내용입니다.
  (< 미니앱 나가기 + "텅장일병일기" + 챗봇/마이페이지/홈 3아이콘)
  showTabNav는 그 아래 탭 네비게이션(AppTabNav)의 노출 여부만 결정합니다.

  예외: 챗봇 화면(SCR-CHAT-01)은 다른 은행 챗봇 UX를 참고해 자체 헤더
  (뒤로가기/이전 기록/닫기)를 쓰기로 팀 협의됨 → hideHeader로 공통 헤더를 끔.
  다른 화면은 절대 hideHeader를 쓰지 않습니다 (팀 협의 없이 추가 금지).

  탭바(showTabNav) 결정 로직 (App.vue 기준):
    route.meta.showTabNav ?? route.meta.requiresAuth ?? false
    1순위: meta.showTabNav가 명시되어 있으면 그 값을 그대로 사용 (예외 화면용)
    2순위: 없으면 meta.requiresAuth 값을 그대로 사용
           (로그인 후 화면은 보통 탭바 있음, 로그인 전은 없음)
    3순위: 둘 다 없으면 false

  사용법 (router/도메인.js):
    meta: { requiresAuth: true }
      → 대부분의 로그인 후 화면 (예: 홈, D-Day, 로드맵메인, 진로목표설정 등)
      → showTabNav를 따로 안 넣어도 requiresAuth: true 값을 그대로 이어받아
        자동으로 탭바가 나옵니다.

    meta: { requiresAuth: true, showTabNav: false }
      → 로그인 후인데 탭바가 없어야 하는 화면
        (온보딩, 회원가입 추가정보, 탈퇴, 챗봇 등 순서가 정해진 플로우/모달)
      → requiresAuth만으로는 자동 판별이 안 되니 반드시 명시해주세요.

    meta: { requiresAuth: false }
      → 로그인 전 화면 (예: 로그인, 시작화면) → 자동으로 탭바 없음

  주의: requiresAuth: true인데 탭바가 없어야 하는 화면(온보딩/탈퇴/챗봇 등)은
  꼭 showTabNav: false를 명시해주세요. 안 넣으면 requiresAuth 값을 그대로 이어받아
  탭바가 실수로 나타날 수 있습니다.
-->
<script setup>
import AppHeader from './AppHeader.vue';
import AppTabNav from './AppTabNav.vue';

// Props: 탭바 노출 여부 (컨벤션: camelCase, 타입/필수/기본값 명시)
// 헤더는 모든 화면 공통 고정이라 별도 prop 없이 항상 렌더링함
const props = defineProps({
  showTabNav: { type: Boolean, required: false, default: true },
  hideHeader: { type: Boolean, required: false, default: false },
});
</script>

<template>
  <div class="app-layout">
    <AppHeader v-if="!hideHeader" />
    <AppTabNav v-if="showTabNav" />
    <main class="app-content">
      <slot />
    </main>
  </div>
</template>

<style scoped>
/* 반응형 프레임: 폰 화면을 꽉 채우고(width 100%), 큰 화면(데스크탑)에선 --app-max-width 로 상한 */
.app-layout {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: var(--app-max-width);
  height: 100vh; /* fallback */
  height: 100dvh; /* 모바일 브라우저 UI 고려한 실제 높이 */
  margin: 0 auto;
  background-color: #ffffff;
  overflow: hidden;
}

.app-content {
  flex: 1;
  padding: 0 24px 20px; /* 우리 처음 맞춘 좌우 gutter 24px */
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
