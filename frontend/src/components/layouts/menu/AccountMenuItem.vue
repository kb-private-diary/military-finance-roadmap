<script setup>
// username 문자열 props 받음
const props = defineProps({ username: String });
const avatar = `/api/member/${props.username}/avatar`;
</script>
<template>
  <!-- 네비게이션 항목 -->
  <li class="nav-item">
    <!-- 프로필 페이지로 이동하는 링크 -->
    <router-link class="nav-link" to="/auth/profile">
      <!-- 사용자 아이콘 -->
      <img :src="avatar" class="avatar avatar-sm" />
      <!-- 사용자 이름 표시 -->
      {{ username }}
    </router-link>
  </li>
</template>
