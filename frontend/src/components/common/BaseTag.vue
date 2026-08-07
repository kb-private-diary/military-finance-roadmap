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
  padding: clamp(3px, 1.2vw, 5px) clamp(7px, 1.8vw, 10px);
  border-radius: 999px;
  font-size: clamp(12px, 3.5vw, 14px);
  letter-spacing: -0.5px;
  font-weight: 600;
  font-family: inherit;
  white-space: nowrap;
}

/* 진한 국방색 (버튼 활성화 상태와 동일) */
.base-tag--green {
  background-color: var(--military-green);
  color: var(--surface-default);
}

/* 연한 국방색 (버튼 비활성화 상태와 동일) */
.base-tag--green-light {
  background-color: var(--military-green-light);
  color: var(--text-strong);
}

/* 옐로우 포인트 컬러 배지용 */
.base-tag--yellow {
  background-color: var(--kb-yellow-deep);
  color: var(--text-strong);
}

/* 회색 바탕 배지용 */
.base-tag--gray {
  background-color: var(--gray-pale-bg);
  color: var(--text-strong);
}

/* 파랑 포인트 컬러 배지용 (휴가 카테고리 CONSOLATION 등) */
.base-tag--blue {
  background-color: var(--pastel-blue);
  color: var(--text-strong);
}

/* 보라 포인트 컬러 배지용 (휴가 카테고리 PETITION 등) */
.base-tag--purple {
  background-color: var(--pastel-purple);
  color: var(--text-strong);
}

/* 브라운 포인트 컬러 배지용 (휴가 카테고리 ETC 등) */
.base-tag--brown {
  background-color: var(--brown-active-bg);
  color: var(--text-strong);
}

/* 자취(rent) 테마 핑크 배지용 */
.base-tag--pink {
  background-color: var(--rent-pink);
  color: var(--surface-default);
}
</style>
