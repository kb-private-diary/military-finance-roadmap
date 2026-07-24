<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },
  variant: {
    type: String,
    required: false,
    default: 'green',
  },
});
</script>

<template>
  <span :class="['base-tag', `base-tag--${variant}`]">
    {{ label }}
  </span>
</template>

<style scoped>
.base-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: clamp(4px, 1.5vw, 6px) clamp(8px, 2vw, 12px);
  border-radius: 999px;
  font-size: clamp(11px, 3.5vw, 13px);
  letter-spacing: -0.5px;
  font-weight: 600;
  font-family: inherit;
  white-space: nowrap;
}

/* 진한 국방색 (버튼 활성화 상태와 동일) */
.base-tag--green {
  background-color: #536349;
  color: #ffffff;
}

/* 연한 국방색 (버튼 비활성화 상태와 동일) */
.base-tag--green-light {
  background-color: #e0e6dc;
  color: #536349;
}

/* 옐로우 포인트 컬러 배지용 */
.base-tag--yellow {
  background-color: #ffbc00;
  color: #60584c;
}

/* 회색 바탕 배지용 */
.base-tag--gray {
  background-color: #f5f5f5;
  color: #9e9e9e;
}
</style>
