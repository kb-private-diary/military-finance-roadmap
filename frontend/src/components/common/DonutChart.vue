<script setup>
import { computed } from 'vue';

// 도넛 차트. 원의 테두리를 비율만큼 나눠 칠한다.
// 외부 라이브러리 없이 SVG stroke-dasharray 로 그린다.
// 범례·제목·금액 등의 배치는 화면마다 다르므로 쓰는 쪽에서 처리한다.
// color 에는 assets/colors.css 의 변수를 넘긴다. 예: 'var(--chart-1)'
const props = defineProps({
  // [{ label: '숙소비', value: 264400, color: 'var(--chart-1)' }, ...]
  items: { type: Array, required: true, default: () => [] },
  size: { type: Number, required: false, default: 180 },
  thickness: { type: Number, required: false, default: 34 },
  chartLabel: { type: String, required: false, default: '항목별 비율' },
});

const RADIUS = 60;
const CIRCUMFERENCE = 2 * Math.PI * RADIUS;

const total = computed(() =>
  props.items.reduce((sum, item) => sum + item.value, 0),
);

// 각 조각의 길이와 시작 위치를 누적해서 계산한다.
const segments = computed(() => {
  if (total.value === 0) {
    return [];
  }

  let accumulated = 0;

  return props.items.map((item) => {
    const ratio = item.value / total.value;
    const segment = {
      label: item.label,
      color: item.color,
      length: ratio * CIRCUMFERENCE,
      offset: -accumulated * CIRCUMFERENCE,
    };
    accumulated += ratio;
    return segment;
  });
});
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 160 160"
    role="img"
    :aria-label="chartLabel"
  >
    <!-- rotate(-90) 으로 12시 방향에서 시작하도록 맞춘다 -->
    <g transform="rotate(-90 80 80)">
      <circle
        v-for="segment in segments"
        :key="segment.label"
        cx="80"
        cy="80"
        :r="RADIUS"
        fill="none"
        :stroke="segment.color"
        :stroke-width="thickness"
        :stroke-dasharray="`${segment.length} ${CIRCUMFERENCE}`"
        :stroke-dashoffset="segment.offset"
      />
    </g>
  </svg>
</template>
