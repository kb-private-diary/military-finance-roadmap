<script setup>
/**
 * ToggleSwitch
 * @description iOS 스타일 토글 스위치. v-model(Boolean) 바인딩 지원.
 *
 * 사용 예시:
 *   <ToggleSwitch v-model="isOn" label="알림 받기" />
 */
const props = defineProps({
  /** v-model 바인딩 값 */
  modelValue: {
    type: Boolean,
    required: true,
    default: false,
  },
  /** 토글 옆에 표시할 라벨 텍스트 */
  label: {
    type: String,
    required: false,
    default: '',
  },
  /** 비활성화 여부 */
  disabled: {
    type: Boolean,
    required: false,
    default: false,
  },
  /** 라벨 위치 ('left' | 'top') */
  labelPosition: {
    type: String,
    required: false,
    default: 'left',
  },
  /** 토글 내부 텍스트 */
  innerLabel: {
    type: String,
    required: false,
    default: '',
  },
});

const emit = defineEmits(['update:modelValue']);

const toggle = () => {
  if (!props.disabled) {
    emit('update:modelValue', !props.modelValue);
  }
};
</script>

<template>
  <label
      :class="[
      'toggle-wrapper',
      `toggle-wrapper--${labelPosition}`,
      { 'toggle-wrapper--disabled': disabled },
    ]"
  >
    <!-- 라벨 텍스트 -->
    <span v-if="label" class="toggle-label">{{ label }}</span>

    <!-- 토글 트랙 -->
    <span
        :class="[
        'toggle-track',
        { 'toggle-track--on': modelValue, 'toggle-track--inner': innerLabel },
      ]"
        role="switch"
        :aria-checked="modelValue"
        tabindex="0"
        @click="toggle"
        @keydown.space.prevent="toggle"
        @keydown.enter.prevent="toggle"
    >
      <span v-if="innerLabel" class="toggle-inner-text">{{ innerLabel }}</span>
      <!-- 토글 썸(원형 버튼) -->
      <span :class="['toggle-thumb', { 'toggle-thumb--on': modelValue }]" />
    </span>
  </label>
</template>

<style scoped>
/* ── 래퍼 ── */
.toggle-wrapper {
  display: flex;
  width: 100%;
  cursor: pointer;
  user-select: none;
}

.toggle-wrapper--left {
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.toggle-wrapper--top {
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.toggle-wrapper--disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* ── 트랙 ── */
.toggle-track {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 26px;
  border-radius: 999px;
  background-color: #d9d9d9;
  transition: background-color 0.25s ease;
  outline: none;
  flex-shrink: 0;
  border: 1px solid transparent;
}

.toggle-track:focus-visible {
  box-shadow: 0 0 0 3px rgba(255, 179, 0, 0.4);
}

.toggle-track--on {
  background-color: #ffbc00; /* KB Yellow Positive */
}

/* ── 트랙 (Inner Label 사용 시) ── */
.toggle-track--inner {
  width: 80px;
  height: 34px;
  background-color: #e5e5e5;
  border: 1px solid #d9d9d9;
  transition: all 0.25s ease;
}
.toggle-track--inner.toggle-track--on {
  background-color: #ffffff;
}

/* ── 썸(원형) ── */
.toggle-thumb {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.25s ease;
}

.toggle-thumb--on {
  transform: translateX(22px);
}

/* ── 썸 (Inner Label 사용 시) ── */
.toggle-track--inner .toggle-thumb {
  top: 3px;
  left: 4px;
  width: 26px;
  height: 26px;
  background-color: #60584c; /* 어두운 회색 */
  box-shadow: none;
}
.toggle-track--inner.toggle-track--on .toggle-thumb {
  background-color: #ffbc00;
  transform: translateX(44px);
}

/* ── 이너 라벨 (텍스트) ── */
.toggle-inner-text {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  font-size: 14px;
  font-weight: 600;
  color: #545045;
  transition: all 0.25s ease;
}
.toggle-track--inner:not(.toggle-track--on) .toggle-inner-text {
  right: 14px;
}
.toggle-track--inner.toggle-track--on .toggle-inner-text {
  left: 14px;
}

/* ── 외부 라벨 ── */
.toggle-label {
  font-size: 16px;
  color: #545045; /* KB Dark Gray */
  font-weight: 500;
}
</style>
