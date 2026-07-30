<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';

const props = defineProps({
  type: {
    type: String,
    required: false,
    default: 'text',
  },
  modelValue: {
    type: [String, Number, Array],
    required: false,
    default: '',
  },
  variant: {
    type: String,
    required: false,
    default: 'box',
  },
  label: { type: String, required: false, default: '' },
  required: { type: Boolean, required: false, default: false },
  placeholder: { type: String, required: false, default: '' },
  error: { type: String, required: false, default: '' },
  icon: { type: String, required: false, default: '' },
  maxLength: { type: Number, required: false, default: 0 },
  suffix: { type: String, required: false, default: '' },
  options: { type: Array, required: false, default: () => [] },
});
const emit = defineEmits(['update:modelValue']);

const updateRange = (index, value) => {
  const newVal = Array.isArray(props.modelValue)
    ? [...props.modelValue]
    : ['', ''];
  newVal[index] = value;
  emit('update:modelValue', newVal);
};

// ==========================================
// Select (Dropdown) 전용 로직
// ==========================================
const isSelectOpen = ref(false);
const dropdownRef = ref(null);

const toggleSelect = () => {
  isSelectOpen.value = !isSelectOpen.value;
};

const selectOption = (option) => {
  emit('update:modelValue', option.value);
  isSelectOpen.value = false;
};

const handleOutsideClick = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    isSelectOpen.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleOutsideClick);
});

onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick);
});

const currentSelectLabel = computed(() => {
  const selected = props.options.find((opt) => opt.value === props.modelValue);
  return selected ? selected.label : props.placeholder || '선택';
});
</script>

<template>
  <div
    :class="['base-input', type === 'select' ? '' : `base-input--${variant}`]"
  >
    <div v-if="label" class="base-input__label">
      {{ label }}
      <span v-if="required" class="base-input__required">*</span>
    </div>

    <div class="base-input__wrapper">
      <!-- SELECT -->
      <template v-if="type === 'select'">
        <div class="dropdown" ref="dropdownRef">
          <button
            class="dropdown__button"
            type="button"
            @click="toggleSelect"
            :class="{ 'is-open': isSelectOpen }"
          >
            <span class="dropdown__text">{{ currentSelectLabel }}</span>
            <svg
              class="dropdown__icon"
              viewBox="0 0 24 24"
              width="16"
              height="16"
            >
              <path
                d="M7 10l5 5 5-5"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
          </button>

          <transition name="dropdown-fade">
            <ul v-if="isSelectOpen" class="dropdown__menu">
              <li
                v-for="opt in options"
                :key="opt.value"
                class="dropdown__item"
                :class="{ 'is-selected': modelValue === opt.value }"
                @click="selectOption(opt)"
              >
                {{ opt.label }}
              </li>
            </ul>
          </transition>
        </div>
      </template>

      <!-- RANGE (MONTH / DATE) -->
      <template v-else-if="type === 'month-range' || type === 'date-range'">
        <div class="base-input__range">
          <input
            :type="type === 'month-range' ? 'month' : 'date'"
            class="base-input__field"
            :class="{ 'is-error': error }"
            :value="Array.isArray(modelValue) ? modelValue[0] : ''"
            @input="updateRange(0, $event.target.value)"
          />
          <span class="base-input__range-sep">~</span>
          <input
            :type="type === 'month-range' ? 'month' : 'date'"
            class="base-input__field"
            :class="{ 'is-error': error }"
            :value="Array.isArray(modelValue) ? modelValue[1] : ''"
            @input="updateRange(1, $event.target.value)"
          />
        </div>
      </template>

      <!-- TEXT / PASSWORD / NUMBER / DATE -->
      <template v-else>
        <span v-if="icon" class="base-input__icon">{{ icon }}</span>
        <input
          :type="type"
          class="base-input__field"
          :class="{ 'is-error': error, 'has-icon': icon, 'has-suffix': suffix }"
          :value="modelValue"
          @input="emit('update:modelValue', $event.target.value)"
          :placeholder="placeholder"
          :maxlength="maxLength || null"
        />
        <span v-if="suffix" class="base-input__suffix">{{ suffix }}</span>
      </template>
    </div>

    <!-- 에러 & 글자수 -->
    <div class="base-input__footer" v-if="error || maxLength">
      <span class="base-input__error">{{ error }}</span>
      <span v-if="maxLength" class="base-input__counter">
        {{ String(modelValue).length }} / {{ maxLength }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.base-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}
.base-input__label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-body);
  display: flex;
  align-items: center;
  gap: 4px;
}
.base-input__required {
  color: var(--kb-yellow-deep);
  font-size: 16px;
}

.base-input__wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}
.base-input__field {
  width: 100%;
  padding: 14px 16px;
  border: 1.5px solid var(--line);
  border-radius: 12px;
  font-size: 16px;
  color: var(--text-body);
  background-color: var(--surface-default);
  outline: none;
  transition: all 0.2s ease;
  font-family: inherit;
}
.base-input__field::placeholder {
  color: var(--placeholder);
}
.base-input__field:focus {
  border-color: var(--kb-yellow-deep);
  box-shadow: 0 0 0 3px var(--focus-ring-yellow);
}
.base-input__field.is-error {
  border-color: var(--danger);
}
.base-input__field.is-error:focus {
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--danger) 15%, transparent);
}

.base-input__range {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}
.base-input__range .base-input__field {
  flex: 1;
  min-width: 0;
  padding: 8px 4px;
  font-size: 12px;
  letter-spacing: -0.5px;
}
.base-input__range-sep {
  color: var(--gray-mid);
  font-weight: 500;
  flex-shrink: 0;
}

.has-icon {
  padding-left: 40px;
}
.base-input__icon {
  position: absolute;
  left: 14px;
  font-size: 18px;
  color: var(--gray-mid);
  pointer-events: none;
}

.has-suffix {
  padding-right: 40px;
}
.base-input__suffix {
  position: absolute;
  right: 16px;
  font-size: 14px;
  color: var(--gray-mid);
  font-weight: 500;
  pointer-events: none;
}

/* input type="date", "month" 전용 달력 아이콘 커스텀 */
input[type='date']::-webkit-calendar-picker-indicator,
input[type='month']::-webkit-calendar-picker-indicator {
  background-image: url('data:image/svg+xml;utf8,<svg fill="%239e9e9e" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path d="M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20a2 2 0 0 0 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V10h14v10zm0-12H5V6h14v2z"/></svg>');
  background-repeat: no-repeat;
  background-position: center, right;
  background-size: 20px;
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.2s;
}
input[type='date']::-webkit-calendar-picker-indicator:hover,
input[type='month']::-webkit-calendar-picker-indicator:hover {
  opacity: 1;
}

.base-input__footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  min-height: 20px;
  font-size: 12px;
  padding: 0 4px;
}
.base-input__error {
  color: var(--danger);
  font-weight: 500;
}
.base-input__counter {
  color: var(--gray-mid);
  margin-left: auto;
}

/* ── Underline Variant ── */
.base-input--underline .base-input__field {
  border: none;
  border-bottom: 2px solid var(--kb-gold);
  border-radius: 0;
  padding: 12px 28px 12px 12px;
  background-color: transparent;
  text-align: center;
  font-size: 16px;
}

/* underline 모드일 때 month-range 내부 field의 폰트/패딩 유지 */
.base-input--underline .base-input__range .base-input__field {
  padding: 8px 4px;
  font-size: 12px;
}
.base-input--underline .base-input__field:focus {
  border-bottom-color: var(--kb-yellow-deep);
  box-shadow: none;
}

/* ── Select (Dropdown) 전용 스타일 ── */
.dropdown {
  position: relative;
  display: block;
  width: 100%;
}

.dropdown__button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 10px 28px 10px 12px;
  background-color: transparent;
  border: none;
  border-bottom: 2px solid var(--kb-gold);
  border-radius: 0;
  font-size: 16px;
  color: var(--text-body);
  font-weight: 500;
  cursor: pointer;
  transition: border-color 0.2s;
  position: relative;
}

.dropdown__button:hover,
.dropdown__button.is-open {
  border-bottom-color: var(--kb-yellow-deep);
  box-shadow: none;
}

.dropdown__text {
  flex: 1;
  text-align: center;
}

.dropdown__icon {
  position: absolute;
  right: 4px;
  color: var(--gray-mid);
  transition:
    transform 0.3s ease,
    color 0.2s ease;
}

.dropdown__button.is-open .dropdown__icon {
  transform: rotate(180deg);
  color: var(--kb-yellow-deep);
}

.dropdown__menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  left: auto;
  min-width: 100%;
  max-height: 240px;
  overflow-y: auto;
  margin: 0;
  padding: 8px 0;
  background-color: var(--surface-default);
  border: 1px solid var(--dropdown-border);
  border-radius: 8px;
  box-shadow: 0 4px 12px var(--shadow-dropdown);
  list-style: none;
  z-index: 100;
}

.dropdown__item {
  padding: 12px 16px;
  font-size: 14px;
  color: var(--kb-gray);
  cursor: pointer;
  transition:
    background-color 0.2s,
    color 0.2s;
  white-space: nowrap;
}

.dropdown__item:hover {
  background-color: var(--kb-yellow-pale);
}

.dropdown__item.is-selected {
  background-color: var(--kb-yellow-pale);
  color: var(--kb-yellow-deep);
  font-weight: 600;
}

.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}
.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
