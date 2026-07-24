<script setup>
import { RouterView, useRoute } from 'vue-router';
import AppLayout from '@/components/layouts/AppLayout.vue';

const route = useRoute();
</script>

<template>
  <!--
    탭바 노출은 각 라우트의 meta(showTabNav, requiresAuth)로 제어됩니다.
    헤더는 모든 화면(챗봇 포함) 공통 고정이라 별도 제어값이 없습니다.
    자세한 사용법과 로직 설명은 components/layouts/AppLayout.vue 상단 주석 참고.
  -->
  <AppLayout
    :show-tab-nav="route.meta.showTabNav ?? route.meta.requiresAuth ?? false"
  >
    <RouterView />
  </AppLayout>
</template>

<style scoped></style>
