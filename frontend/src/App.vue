<script setup>
import { RouterView, useRoute } from 'vue-router';
import AppLayout from '@/components/layouts/AppLayout.vue';
import BaseToast from '@/components/common/BaseToast.vue';

const route = useRoute();
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
    <RouterView />
  </AppLayout>
  <!-- 공통 토스트 호스트: 어디서든 useToast().show() 로 띄우면 여기서 렌더 -->
  <BaseToast />
</template>

<style scoped></style>
