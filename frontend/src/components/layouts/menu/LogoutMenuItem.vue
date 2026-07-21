<script setup>
// 라우터 인스턴스 가져옴
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

// 라우터 객체 생성
const router = useRouter();
const auth = useAuthStore();

// 로그아웃 시 홈으로 이동
const logout = (e) => {
  // 로그아웃
  auth.logout();
  router.push('/');
};
</script>
<template>
  <!-- 로그아웃 링크 -->
  <a href="#" class="nav-link" @click.prevent="logout">
    <!-- 로그아웃 아이콘 -->
    <i class="fa-solid fa-right-from-bracket"></i>
    로그아웃
  </a>
</template>
