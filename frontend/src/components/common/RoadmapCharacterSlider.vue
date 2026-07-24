<script setup>
// 공통 컴포넌트: 로드맵 진행도 - 캐릭터 슬라이더 (담당: 호빈)
// 목표 진행률(0~100)에 따라 트랙 위 캐릭터의 위치와 포즈가 바뀐다.
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

// 진행률 구간별 캐릭터 포즈: 시작 전(누움) → 조금 진행(앉음) → 열심히 진행 중(뜀) → 목표 달성(경례)
const characterImg = computed(() => {
  if (clampedProgress.value >= 100) return saluteImg;
  if (clampedProgress.value >= 67) return runImg;
  if (clampedProgress.value >= 34) return sitdownImg;
  return lyingImg;
});

// 캐릭터 이미지가 트랙 중앙에 오도록 살짝 보정 (이미지 폭 절반만큼)
const characterStyle = computed(() => ({
  left: `${clampedProgress.value}%`,
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
  padding-top: 2.5rem;
}

.character-slider__label {
  font-size: 0.85rem;
  color: var(--bs-secondary-color);
  margin-bottom: 0.5rem;
}

.character-slider__track {
  position: relative;
  height: 6px;
  border-radius: 999px;
  background-color: #e9e2d5;
}

.character-slider__fill {
  height: 100%;
  border-radius: 999px;
  background-color: #f0ad2e;
  transition: width 0.25s ease;
}

.character-slider__character {
  position: absolute;
  top: 50%;
  width: 44px;
  height: 44px;
  object-fit: contain;
  transform: translate(-50%, -75%);
  transition: left 0.25s ease;
  pointer-events: none;
}

.character-slider__percent {
  margin-top: 0.5rem;
  font-size: 0.8rem;
  font-weight: 600;
  color: #a9895a;
  text-align: right;
}
</style>
