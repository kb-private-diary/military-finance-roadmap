<script setup>
import { RouterView, useRoute } from 'vue-router';
import AppLayout from '@/components/layouts/AppLayout.vue';
import BaseToast from '@/components/common/BaseToast.vue';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const auth = useAuthStore();
</script>

<template>
  <!--
    탭바 노출은 각 라우트의 meta(showTabNav, requiresAuth)로 제어됩니다.
    헤더는 원칙적으로 모든 화면 공통 고정이며, meta.hideHeader로 끄는 예외는
    챗봇(SCR-CHAT-01)만 팀 협의로 허용됨.
    자세한 사용법과 로직 설명은 components/layouts/AppLayout.vue 상단 주석 참고.
  -->
  <AppLayout
    :show-tab-nav="route.meta.showTabNav ?? route.meta.requiresAuth ?? false"
    :hide-header="route.meta.hideHeader ?? false"
  >
    <!-- 챗봇(ChatPage)만 keep-alive - 버튼으로 진행한 상담(목돈 상담/청약 목록 등)은 서버에 저장되지 않는
         화면 전용 상태라, 다른 페이지(자취 준비 등)로 이동했다가 돌아오면 컴포넌트가 통째로 새로 마운트되면서
         그 상태가 다 날아갔었음(대화가 처음으로 리셋됨). 챗봇 컴포넌트를 메모리에 살려두면 대화 상태가 유지됨.
         단, key를 로그인한 유저 id로 걸어둬야 함 - 안 그러면 로그아웃 후 다른 계정으로 로그인해도
         (SPA 네비게이션이라 페이지 전체가 새로고침되지 않으므로) 캐시된 이전 유저의 대화가 그대로 남아있게 됨. -->
    <RouterView v-slot="{ Component }">
      <KeepAlive include="ChatPage">
        <component :is="Component" :key="auth.state.user.id ?? 'anon'" />
      </KeepAlive>
    </RouterView>
  </AppLayout>
  <!-- 공통 토스트 호스트: 어디서든 useToast().show() 로 띄우면 여기서 렌더 -->
  <BaseToast />
</template>

<style scoped></style>
