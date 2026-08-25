<script setup>
import { computed } from 'vue';

// 도넛 차트. 원의 테두리를 비율만큼 나눠 칠한다.
// 외부 라이브러리 없이 SVG stroke-dasharray 로 그린다.
// 범례·제목·금액 등의 배치는 화면마다 다르므로 쓰는 쪽에서 처리한다.
// color 에는 assets/colors.css 의 변수를 넘긴다. 예: 'var(--chart-1)'
const props = defineProps({
  // [{ label: '숙소비', value: 264400, color: 'var(--chart-1)' }, ...]
  items: { type: Array, required: false, default: () => [] },
  size: { type: Number, required: false, default: 180 },
  thickness: { type: Number, required: false, default: 34 },
  chartLabel: { type: String, required: false, default: '항목별 비율' },
  // 진행률 링 모드(0~1). 지정하면 items 대신 트랙 + 그라데이션 아크(둥근 끝)로 그린다.
  progress: { type: Number, required: false, default: null },
  progressColor: { type: String, required: false, default: 'var(--kb-yellow)' },
  trackColor: { type: String, required: false, default: 'var(--surface-muted)' },
  // 진행 아크에 쓸 그라데이션 [시작색, 끝색]
  gradient: { type: Array, required: false, default: null },
  // 초과분 오버레이(0~1). 진행 아크 위에 다른 색으로 덧그린다. (예: 예산 초과분)
  overlay: { type: Number, required: false, default: null },
  overlayColor: { type: String, required: false, default: 'var(--military-green)' },
  // 오버레이용 그라데이션 [시작색, 끝색] - 노란 링과 자연스럽게 잇기 위함
  overlayGradient: { type: Array, required: false, default: null },
});

const RADIUS = 60;
const CIRCUMFERENCE = 2 * Math.PI * RADIUS;

// 여러 도넛이 한 화면에 있어도 그라데이션 id가 겹치지 않게 인스턴스마다 고유 id
let _donutSeq = 0;
const gradId = `donut-grad-${(_donutSeq += 1)}`;
const overlayGradId = `donut-ograd-${_donutSeq}`;

const progressLen = computed(
  () => Math.max(0, Math.min(1, props.progress ?? 0)) * CIRCUMFERENCE,
);

const overlayLen = computed(
  () => Math.max(0, Math.min(1, props.overlay ?? 0)) * CIRCUMFERENCE,
);

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
    <defs>
      <linearGradient v-if="gradient" :id="gradId" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" :stop-color="gradient[0]" />
        <stop offset="100%" :stop-color="gradient[1]" />
      </linearGradient>
      <linearGradient
        v-if="overlayGradient"
        :id="overlayGradId"
        x1="0"
        y1="0"
        x2="1"
        y2="1"
      >
        <stop offset="0%" :stop-color="overlayGradient[0]" />
        <stop offset="100%" :stop-color="overlayGradient[1]" />
      </linearGradient>
    </defs>

    <!-- 진행률 링 모드: 트랙 + 그라데이션 아크(둥근 끝) -->
    <template v-if="progress !== null">
      <circle
        cx="80"
        cy="80"
        :r="RADIUS"
        fill="none"
        :stroke="trackColor"
        :stroke-width="thickness"
      />
      <g transform="rotate(-90 80 80)">
        <circle
          cx="80"
          cy="80"
          :r="RADIUS"
          fill="none"
          :stroke="gradient ? `url(#${gradId})` : progressColor"
          :stroke-width="thickness"
          stroke-linecap="round"
          :stroke-dasharray="`${progressLen} ${CIRCUMFERENCE}`"
        />
        <!-- 초과분: 노란 링 위에 덧그린다 (그라데이션이면 노랑과 자연스럽게 이어짐) -->
        <circle
          v-if="overlay !== null"
          cx="80"
          cy="80"
          :r="RADIUS"
          fill="none"
          :stroke="overlayGradient ? `url(#${overlayGradId})` : overlayColor"
          :stroke-width="thickness"
          stroke-linecap="round"
          :stroke-dasharray="`${overlayLen} ${CIRCUMFERENCE}`"
        />
      </g>
    </template>

    <!-- 기존 비율 세그먼트 모드 (전우들 관심도 등) -->
    <g v-else transform="rotate(-90 80 80)">
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
