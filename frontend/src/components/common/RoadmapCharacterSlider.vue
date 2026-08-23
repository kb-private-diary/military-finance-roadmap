<script setup>
// 공통 컴포넌트: 로드맵 진행도 - 캐릭터 슬라이더 (담당: 호빈)
import { computed } from 'vue';
import lyingImg from '@/assets/images/lying.png';
import sitdownImg from '@/assets/images/sitdown.png';
import runImg from '@/assets/images/run.png';
import saluteImg from '@/assets/images/salute.png';

const props = defineProps({
  step: {
    type: Number, // 현재 단계 (1 ~ totalSteps)
    default: 1,
  },
  totalSteps: {
    type: Number,
    default: 4,
  },
  label: {
    type: String,
    default: '',
  },
});

// 유효 범위로 보정한 현재 단계
const currentStep = computed(() =>
  Math.min(props.totalSteps, Math.max(1, props.step)),
);

// 곰돌이 가로 위치 / 트랙 채움 비율 (step1 = 0%, 마지막 step = 100%)
const percent = computed(() => {
  if (props.totalSteps <= 1) return 100;
  return ((currentStep.value - 1) / (props.totalSteps - 1)) * 100;
});

// 단계별 캐릭터 이미지 (4단계 기준)
const stepImages = [lyingImg, sitdownImg, runImg, saluteImg];
const characterImg = computed(() => {
  const idx = Math.min(stepImages.length, currentStep.value) - 1;
  return stepImages[idx];
});

// 포즈별 가로세로 비율이 달라, 각 이미지의 '가장 긴 변'이 비슷하게(≈46px) 보이도록 폭을 개별 지정
// (step1 lying은 납작해서 넓게, 서 있는 포즈는 좁게 — 시각적 크기 균일화)
const STEP_WIDTHS = [33, 25, 28, 24]; // lying, sitdown, run, salute (전체적으로 축소)
const characterSize = computed(
  () => STEP_WIDTHS[currentStep.value - 1] ?? 38,
);

const characterStyle = computed(() => ({
  left: `${percent.value}%`,
  bottom: `calc(100% - 4px)`,
  width: `${characterSize.value}px`,
}));

// n번째 단계의 트랙상 위치 (%) — step1 = 0%, 마지막 = 100% (4등분)
const tickPercent = (n) => {
  if (props.totalSteps <= 1) return 0;
  return ((n - 1) / (props.totalSteps - 1)) * 100;
};

// STEP 라벨 위치 — 양 끝은 트랙 밖으로 안 넘치게 정렬 보정
const labelStyle = (n) => {
  if (n === 1) return { left: '0%', transform: 'translateX(0)' };
  if (n === props.totalSteps) return { left: '100%', transform: 'translateX(-100%)' };
  return { left: `${tickPercent(n)}%`, transform: 'translateX(-50%)' };
};
</script>

<template>
  <div class="character-slider">
    <div v-if="label" class="character-slider__label">{{ label }}</div>
    <div class="character-slider__track">
      <div class="character-slider__fill" :style="{ width: `${percent}%` }" />
      <span
        v-for="n in totalSteps"
        :key="`tick-${n}`"
        class="character-slider__tick"
        :class="{ 'is-reached': n <= currentStep }"
        :style="{ left: `${tickPercent(n)}%` }"
      />
      <img
        :src="characterImg"
        class="character-slider__character"
        :style="characterStyle"
        alt="진행 상황 캐릭터"
      />
    </div>
    <div class="character-slider__steps">
      <span
        v-for="n in totalSteps"
        :key="`label-${n}`"
        class="character-slider__step-label"
        :class="{ 'is-current': n === currentStep }"
        :style="labelStyle(n)"
        >STEP {{ n }}</span
      >
    </div>
  </div>
</template>

<style scoped>
.character-slider {
  margin-bottom: 0.5rem;
}

.character-slider__label {
  font-size: 0.85rem;
  color: var(--bs-secondary-color);
  margin-bottom: 0.25rem;
}

.character-slider__track {
  position: relative;
  height: 6px;
  /* 양옆 20px씩 줄여(인셋) 곰돌이가 중앙 정렬이어도 트랙 밖으로 안 나가게 */
  /* margin-top 42px: 곰돌이가 트랙 위에 서므로 캐릭터 높이만큼 위 여백 확보 */
  margin: 42px 20px 0;
  border-radius: 999px;
  background-color: var(--kb-gray-pale);
}

.character-slider__fill {
  height: 100%;
  border-radius: 999px;
  background-color: var(--military-green);
  transition: width 0.25s ease;
}

/* 4등분 눈금 (0 / 33 / 66 / 100%) */
.character-slider__tick {
  position: absolute;
  top: 50%;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background-color: var(--kb-gray-pale);
  border: 2px solid var(--surface-default, #fff);
  transform: translate(-50%, -50%);
  z-index: 1;
}

.character-slider__tick.is-reached {
  background-color: var(--military-green);
}

.character-slider__character {
  position: absolute;
  height: auto;
  transform: translateX(-50%);
  transition: left 0.25s ease;
  pointer-events: none;
  z-index: 2;
}

/* STEP 라벨 전부 표시 (눈금 위치에 맞춰) */
.character-slider__steps {
  position: relative;
  height: 1.1rem;
  /* 트랙과 동일하게 양옆 20px 인셋 → 라벨이 눈금 위치와 계속 정렬됨 */
  margin: 0.5rem 20px 0;
}

.character-slider__step-label {
  position: absolute;
  font-size: 0.64rem; /* STEP 글자 살짝 축소 */
  font-weight: 500;
  color: var(--text-gray-light); /* 비현재 STEP은 연한 회색 */
  white-space: nowrap;
}

.character-slider__step-label.is-current {
  color: var(--military-green);
  font-weight: 700;
}
</style>
