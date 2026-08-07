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

const CHARACTER_BASE_SIZE = 32;
const characterSize = computed(() =>
  currentStep.value <= 1 ? CHARACTER_BASE_SIZE * 1.2 : CHARACTER_BASE_SIZE * 1.15,
);

const characterStyle = computed(() => ({
  left: `${percent.value}%`,
  bottom: `calc(100% - 4px)`,
  width: `${characterSize.value}px`,
}));
</script>

<template>
  <div class="character-slider">
    <div v-if="label" class="character-slider__label">{{ label }}</div>
    <div class="character-slider__track">
      <div class="character-slider__fill" :style="{ width: `${percent}%` }" />
      <img
        :src="characterImg"
        class="character-slider__character"
        :style="characterStyle"
        alt="진행 상황 캐릭터"
      />
    </div>
    <div class="character-slider__step">step {{ currentStep }}</div>
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
  width: 88%;
  /* 곰돌이가 트랙 위에 서므로 캐릭터 높이만큼만 위 여백 확보 */
  margin-top: 42px;
  border-radius: 999px;
  background-color: var(--kb-gray-pale);
}

.character-slider__fill {
  height: 100%;
  border-radius: 999px;
  background-color: var(--military-green);
  transition: width 0.25s ease;
}

.character-slider__character {
  position: absolute;
  height: auto;
  transform: translateX(-50%);
  transition: left 0.25s ease;
  pointer-events: none;
}

.character-slider__step {
  margin-top: 0.35rem;
  font-size: 0.8rem;
  font-weight: 600;
  color: #96817c;
  text-align: right;
}
</style>
