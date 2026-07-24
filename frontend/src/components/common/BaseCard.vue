<script setup>
// 공통 카드 컨테이너. 테두리·그림자·여백을 제공하고 내용은 slot 으로 채운다.
// 높이는 내용에 따라 자동으로 늘어난다.
defineProps({
  // 안쪽 여백 (예: '28px')
  padding: { type: String, required: false, default: '20px' },
});
</script>

<template>
  <div class="base-card" :style="{ padding }">
    <slot />
  </div>
</template>

<style scoped>
.base-card {
  background-color: #ffffff;
  border: 1px solid #ececec;
  border-radius: 16px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
  box-sizing: border-box;
}
</style>
