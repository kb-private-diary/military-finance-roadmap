<script setup>
// 회원가입 플로우 전용 스텝 헤더 (약관동의 / 회원정보 / 추가정보 공통)
// eyebrow(플로우명) → step(단계 표시) → title(큰 제목) 3단 구성
defineProps({
  eyebrow: { type: String, required: false, default: '' },
  step: { type: String, required: false, default: '' },
  title: { type: String, required: true },
});
</script>

<template>
  <header class="signup-step-header">
    <p v-if="eyebrow" class="signup-step-header__eyebrow">{{ eyebrow }}</p>
    <p v-if="step" class="signup-step-header__step">{{ step }}</p>
    <h1 class="signup-step-header__title">{{ title }}</h1>
  </header>
</template>

<style scoped>
.signup-step-header {
  margin: 6px 0 28px;
}

.signup-step-header__eyebrow {
  margin: 0 0 36px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
}

.signup-step-header__step {
  margin: 0 0 14px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  color: var(--text-body);
}

.signup-step-header__title {
  margin: 0;
  font-size: 23px;
  font-weight: 800;
  color: var(--text-strong);
}
</style>
