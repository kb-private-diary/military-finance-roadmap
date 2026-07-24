<script setup>
import { computed } from 'vue';

// 프로그래스 바.
// 라벨·퍼센트·금액 등의 배치는 화면마다 다르므로 쓰는 쪽에서 처리한다.
// color 에는 assets/colors.css 의 변수를 넘긴다. 예: 'var(--chart-1)'
const props = defineProps({
  value: { type: Number, required: true, default: 0 },
  total: { type: Number, required: true, default: 0 },
  color: { type: String, required: false, default: 'var(--chart-1)' },
  height: { type: Number, required: false, default: 16 },
});

// 0 ~ 100 범위를 벗어나지 않도록 자른다.
const percent = computed(() => {
  if (props.total === 0) {
    return 0;
  }
  const ratio = (props.value / props.total) * 100;
  return Math.min(Math.max(Math.round(ratio), 0), 100);
});
</script>

<template>
  <div
    class="bar-track"
    :style="{ height: `${height}px` }"
    role="progressbar"
    :aria-valuenow="percent"
    aria-valuemin="0"
    aria-valuemax="100"
  >
    <div
      class="bar-fill"
      :style="{ width: `${percent}%`, backgroundColor: color }"
    />
  </div>
</template>

<style scoped>
.bar-track {
  flex: 1;
  border: 1px solid var(--line-strong);
  border-radius: 25px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  transition: width 0.3s ease;
}
</style>
