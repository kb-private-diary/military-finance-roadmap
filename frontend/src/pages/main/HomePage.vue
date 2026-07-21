<script setup>
import config from '@/config';
</script>

<template>
  <div class="py-4">
    <h2>{{ config.title }}</h2>
    <p class="text-muted">{{ config.subtitle }}</p>
    <hr />
    <p>메인(요약 카드) 화면 준비 중입니다. (담당: 지원)</p>
    <div class="row g-3">
      <div class="col-6 col-md-4" v-for="menu in config.menus" :key="menu.url">
        <router-link :to="menu.url" class="btn btn-outline-primary w-100 py-3">
          <i :class="menu.icon" class="me-1"></i> {{ menu.title }}
        </router-link>
      </div>
    </div>
  </div>
</template>
