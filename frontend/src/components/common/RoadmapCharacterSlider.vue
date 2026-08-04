<script setup>
// 공통 컴포넌트: 로드맵 진행도 - 캐릭터 슬라이더 (담당: 호빈)
import { computed } from 'vue';
import lyingImg from '@/assets/images/lying.png';
import sitdownImg from '@/assets/images/sitdown.png';
import runImg from '@/assets/images/run.png';
import saluteImg from '@/assets/images/salute.png';

const props = defineProps({
  progress: {
    type: Number, // 0~100
    default: 0,
  },
  label: {
    type: String,
    default: '',
  },
});

const clampedProgress = computed(() => Math.min(100, Math.max(0, props.progress)));

const characterImg = computed(() => {
  if (clampedProgress.value >= 100) return saluteImg;
  if (clampedProgress.value >= 75) return runImg;
  if (clampedProgress.value >= 50) return sitdownImg;
  return lyingImg;
});

const CHARACTER_BASE_SIZE = 32;
const characterSize = computed(() => {
  if (clampedProgress.value >= 100) return CHARACTER_BASE_SIZE * 1.15; // 경례
  if (clampedProgress.value >= 75) return CHARACTER_BASE_SIZE * 1.15; // 뜀
  if (clampedProgress.value >= 50) return CHARACTER_BASE_SIZE * 1.15; // 앉음
  return CHARACTER_BASE_SIZE * 1.2; // 누움
});

const characterStyle = computed(() => ({
  left: `${clampedProgress.value}%`,
  bottom: `calc(100% - 4px)`,
  width: `${characterSize.value}px`,
}));
</script>

<template>
  <div class="character-slider">
    <div v-if="label" class="character-slider__label">{{ label }}</div>
    <div class="character-slider__track">
      <div class="character-slider__fill" :style="{ width: `${clampedProgress}%` }" />
      <img
        :src="characterImg"
        class="character-slider__character"
        :style="characterStyle"
        alt="진행 상황 캐릭터"
      />
    </div>
    <div class="character-slider__percent">{{ Math.round(clampedProgress) }}%</div>
  </div>
</template>

<style scoped>
.character-slider {
  margin-bottom: 1rem;
}

.character-slider__label {
  font-size: 0.85rem;
  color: var(--bs-secondary-color);
  margin-bottom: 0.5rem;
}

.character-slider__track {
  position: relative;
  height: 6px;
  margin-top: 90px;
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

.character-slider__percent {
  margin-top: 0.5rem;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--brand-gold);
  text-align: right;
}
</style>
