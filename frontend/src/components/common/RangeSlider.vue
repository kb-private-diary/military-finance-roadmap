<script setup>
// 공통 컴포넌트: 범위 슬라이더 (담당: 수연)
// 상단 라벨+현재값, 노랑 채움 트랙, 하단 눈금, 설명문 구성 — 자취 월예산·거주기간 등에서 사용
// 사용:
//   <RangeSlider v-model="budget" :min="30" :max="150" :step="5"
//     label="월 예산 (월세 + 관리비)" unit="만원"
//     :ticks="['30만원','90만원','150만원']"
//     hint="월세와 관리비를 합친 실질 월부담을 기준으로 매물을 추천해드려요" />
import { computed } from 'vue';

const props = defineProps({
  modelValue: { type: Number, default: 0 },
  min: { type: Number, default: 0 },
  max: { type: Number, default: 100 },
  step: { type: Number, default: 1 },
  label: { type: String, default: '' },
  unit: { type: String, default: '' },
  // 값 표시를 직접 지정(예: '36,000km'처럼 포맷이 필요할 때). 지정 시 unit 무시.
  displayValue: { type: String, default: '' },
  showValue: { type: Boolean, default: true },
  ticks: { type: Array, default: () => [] }, // 하단 눈금 라벨 배열
  hint: { type: String, default: '' },
  // 있으면 thumb(핸들)에 이미지(예: 캐릭터 얼굴)를 올린다
  thumbIcon: { type: String, default: '' },
  // 채움 트랙 색 (기본 KB 노랑). 예: 연국방색
  fillColor: { type: String, default: '' },
});

const emit = defineEmits(['update:modelValue']);

// 현재값 위치까지 노랑, 이후 회색으로 채운 트랙
const fillStyle = computed(() => {
  const ratio = (props.modelValue - props.min) / (props.max - props.min || 1);
  const pct = Math.min(100, Math.max(0, ratio * 100));
  const c = props.fillColor || 'var(--kb-yellow)';
  return {
    background: `linear-gradient(to right, ${c} 0%, ${c} ${pct}%, var(--kb-gray-pale) ${pct}%, var(--kb-gray-pale) 100%)`,
  };
});

const onInput = (event) => {
  emit('update:modelValue', Number(event.target.value));
};
</script>

<template>
  <div class="range-slider">
    <div v-if="label || showValue" class="range-slider__top">
      <span v-if="label" class="range-slider__label">{{ label }}</span>
      <span v-if="showValue" class="range-slider__value">
        <template v-if="displayValue">{{ displayValue }}</template>
        <template v-else
          >{{ modelValue }}<span v-if="unit" class="range-slider__unit">{{
            unit
          }}</span></template
        >
      </span>
    </div>

    <input
      type="range"
      class="range-slider__input"
      :class="{ 'range-slider__input--thumb-icon': thumbIcon }"
      :min="min"
      :max="max"
      :step="step"
      :value="modelValue"
      :style="[fillStyle, thumbIcon ? { '--thumb-img': `url(${thumbIcon})` } : null]"
      @input="onInput"
    />

    <div v-if="ticks.length" class="range-slider__scale">
      <span v-for="tick in ticks" :key="tick">{{ tick }}</span>
    </div>

    <p v-if="hint" class="range-slider__hint">{{ hint }}</p>
  </div>
</template>

<style scoped>
.range-slider {
  width: 100%;
}

.range-slider__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.range-slider__label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-body);
}
.range-slider__value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-strong);
}
.range-slider__unit {
  margin-left: 2px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
}

.range-slider__input {
  -webkit-appearance: none;
  appearance: none;
  width: 100%;
  height: 6px;
  border-radius: 3px;
  margin: 12px 0 8px;
  background: var(--kb-gray-pale);
  cursor: pointer;
}
.range-slider__input::-webkit-slider-runnable-track {
  height: 6px;
  border-radius: 3px;
  background: transparent;
}
.range-slider__input::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 18px;
  height: 18px;
  margin-top: -6px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid var(--kb-yellow-deep);
  box-shadow: 0 1px 3px var(--shadow-thumb);
  cursor: pointer;
}
.range-slider__input::-moz-range-track {
  height: 6px;
  border-radius: 3px;
  background: transparent;
}
.range-slider__input::-moz-range-thumb {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid var(--kb-yellow-deep);
  box-shadow: 0 1px 3px var(--shadow-thumb);
  cursor: pointer;
}
/* thumbIcon 있을 때: 핸들에 캐릭터 얼굴 (흰 원 없이 이미지만) */
.range-slider__input--thumb-icon::-webkit-slider-thumb {
  width: 32px;
  height: 32px;
  margin-top: -13px;
  border: none;
  border-radius: 0;
  background: var(--thumb-img) center / contain no-repeat;
  box-shadow: none;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.25));
}
.range-slider__input--thumb-icon::-moz-range-thumb {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 0;
  background: var(--thumb-img) center / contain no-repeat;
  box-shadow: none;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.25));
}

.range-slider__scale {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: var(--text-hint);
}

.range-slider__hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-hint);
}
</style>
