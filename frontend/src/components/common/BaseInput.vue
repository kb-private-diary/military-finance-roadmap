<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { formatAmountInput, parseAmountInput } from '@/util/format';

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
  disabled: { type: Boolean, required: false, default: false },
  min: { type: String, required: false, default: '' },
  max: { type: String, required: false, default: '' },
});
const emit = defineEmits(['update:modelValue']);

const rangeInputType = computed(() =>
  props.type === 'month-range' ? 'month' : 'date',
);

const updateRange = (index, value) => {
  const newVal = Array.isArray(props.modelValue)
    ? [...props.modelValue]
    : ['', ''];
  newVal[index] = value;

  if (props.type === 'date-range') {
    if (index === 0 && newVal[1] && newVal[1] < value) {
      newVal[1] = '';
    }
    if (index === 1 && newVal[0] && value < newVal[0]) {
      return;
    }
  }

  emit('update:modelValue', newVal);
};

// ==========================================
// select-range 전용 로직 (예: 시작 개월차 ~ 종료 개월차)
// 종료 드롭다운은 시작에서 고른 값 이상만 보여줘서, 시작이 종료보다 늦게
// 선택되는 경우 자체를 UI에서 막는다.
// 겉모양은 일반 select 타입과 동일한 커스텀 드롭다운(버튼+오버레이 메뉴)을 그대로 쓴다.
// ==========================================
const endRangeOptions = computed(() => {
  const start = Array.isArray(props.modelValue) ? props.modelValue[0] : '';
  if (start === '') return props.options;
  return props.options.filter((opt) => Number(opt.value) >= Number(start));
});

const updateRangeStart = (value) => {
  const currentEnd = Array.isArray(props.modelValue) ? props.modelValue[1] : '';
  const stillValid = currentEnd !== '' && Number(currentEnd) >= Number(value);
  emit('update:modelValue', [value, stillValid ? currentEnd : '']);
};

const isStartOpen = ref(false);
const isEndOpen = ref(false);
const startDropdownRef = ref(null);
const endDropdownRef = ref(null);

const isRangeEndDisabled = computed(
  () => !Array.isArray(props.modelValue) || props.modelValue[0] === '',
);

const toggleStart = () => {
  isStartOpen.value = !isStartOpen.value;
  isEndOpen.value = false;
};

const toggleEnd = () => {
  if (isRangeEndDisabled.value) {
    return;
  }
  isEndOpen.value = !isEndOpen.value;
  isStartOpen.value = false;
};

const selectRangeStart = (option) => {
  updateRangeStart(option.value);
  isStartOpen.value = false;
};

const selectRangeEnd = (option) => {
  updateRange(1, option.value);
  isEndOpen.value = false;
};

const rangeStartLabel = computed(() => {
  const start = Array.isArray(props.modelValue) ? props.modelValue[0] : '';
  const found = props.options.find((opt) => opt.value === start);
  return found ? found.label : '선택';
});

const rangeEndLabel = computed(() => {
  const end = Array.isArray(props.modelValue) ? props.modelValue[1] : '';
  const found = props.options.find((opt) => opt.value === end);
  return found ? found.label : '선택';
});

// ==========================================
// Select (Dropdown) 전용 로직
// ==========================================
const isSelectOpen = ref(false);
const dropdownRef = ref(null);

const toggleSelect = () => {
  if (props.disabled) return;
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
  if (
    startDropdownRef.value &&
    !startDropdownRef.value.contains(event.target)
  ) {
    isStartOpen.value = false;
  }
  if (endDropdownRef.value && !endDropdownRef.value.contains(event.target)) {
    isEndOpen.value = false;
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
// 값을 실제로 골랐는지 (아니면 placeholder 표시 → 회색)
const isValueSelected = computed(() =>
  props.options.some((opt) => opt.value === props.modelValue),
);

// ==========================================
// Amount(금액) 전용 로직 — 콤마 실시간 포맷 + 커서 유지
// modelValue는 항상 순수 숫자로 유지하고, 콤마 붙은 표시값은 내부에서만 관리한다.
// ==========================================
const amountDisplay = ref(formatAmountInput(props.modelValue));

watch(
  () => props.modelValue,
  (val) => {
    const formatted = formatAmountInput(val);
    if (formatted !== amountDisplay.value) amountDisplay.value = formatted;
  },
);

// 콤마 삽입/삭제로 문자열 길이가 바뀌어도 커서가 입력 위치에 그대로 머물도록,
// 길이 변화량만큼 커서 위치를 보정한다 (그대로 두면 콤마 추가 시 커서가 맨 끝으로 튐).
const handleAmountInput = (event) => {
  const input = event.target;
  const prevLength = input.value.length;
  const prevCaret = input.selectionStart ?? prevLength;

  const formatted = formatAmountInput(input.value);
  amountDisplay.value = formatted;
  emit('update:modelValue', parseAmountInput(input.value));

  nextTick(() => {
    const caret = Math.max(0, prevCaret + (formatted.length - prevLength));
    input.setSelectionRange(caret, caret);
  });
};
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
            :class="{ 'is-open': isSelectOpen, 'dropdown__button--placeholder': !isValueSelected }"
            :disabled="disabled"
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
            :type="rangeInputType"
            class="base-input__field"
            :class="{ 'is-error': error }"
            :value="Array.isArray(modelValue) ? modelValue[0] : ''"
            :disabled="disabled"
            :min="min || null"
            :max="
              (Array.isArray(modelValue) && modelValue[1]) || max || null
            "
            @input="updateRange(0, $event.target.value)"
          />
          <span class="base-input__range-sep">~</span>
          <input
            :type="rangeInputType"
            class="base-input__field"
            :class="{ 'is-error': error }"
            :value="Array.isArray(modelValue) ? modelValue[1] : ''"
            :disabled="disabled"
            :min="
              (Array.isArray(modelValue) && modelValue[0]) || min || null
            "
            :max="max || null"
            @input="updateRange(1, $event.target.value)"
          />
        </div>
      </template>

      <!-- SELECT RANGE (예: 시작 개월차 ~ 종료 개월차, 종료 옵션은 시작값 이상만) -->
      <!-- 겉모양은 일반 select 타입과 동일한 커스텀 드롭다운을 그대로 재사용한다. -->
      <template v-else-if="type === 'select-range'">
        <div class="base-input__range">
          <div class="dropdown" ref="startDropdownRef">
            <button
              class="dropdown__button"
              type="button"
              :class="{ 'is-open': isStartOpen }"
              @click="toggleStart"
            >
              <span class="dropdown__text">{{ rangeStartLabel }}</span>
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
              <ul v-if="isStartOpen" class="dropdown__menu">
                <li
                  v-for="opt in options"
                  :key="opt.value"
                  class="dropdown__item"
                  :class="{
                    'is-selected':
                      (Array.isArray(modelValue) ? modelValue[0] : '') ===
                      opt.value,
                  }"
                  @click="selectRangeStart(opt)"
                >
                  {{ opt.label }}
                </li>
              </ul>
            </transition>
          </div>

          <span class="base-input__range-sep">~</span>

          <div class="dropdown" ref="endDropdownRef">
            <button
              class="dropdown__button"
              type="button"
              :class="{ 'is-open': isEndOpen }"
              :disabled="isRangeEndDisabled"
              @click="toggleEnd"
            >
              <span class="dropdown__text">{{ rangeEndLabel }}</span>
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
              <ul v-if="isEndOpen" class="dropdown__menu">
                <li
                  v-for="opt in endRangeOptions"
                  :key="opt.value"
                  class="dropdown__item"
                  :class="{
                    'is-selected':
                      (Array.isArray(modelValue) ? modelValue[1] : '') ===
                      opt.value,
                  }"
                  @click="selectRangeEnd(opt)"
                >
                  {{ opt.label }}
                </li>
              </ul>
            </transition>
          </div>
        </div>
      </template>

      <!-- AMOUNT (금액, 실시간 콤마 포맷) -->
      <template v-else-if="type === 'amount'">
        <span v-if="icon" class="base-input__icon">{{ icon }}</span>
        <input
          type="text"
          inputmode="numeric"
          class="base-input__field base-input__field--amount"
          :class="{ 'is-error': error, 'has-icon': icon, 'has-suffix': suffix }"
          :value="amountDisplay"
          @input="handleAmountInput"
          :placeholder="placeholder"
        />
        <span v-if="suffix" class="base-input__suffix">{{ suffix }}</span>
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
  gap: var(--space-2);
  width: 100%;
}
.base-input__label {
  font-size: 14px;
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
  padding: 10px 14px;
  border: 1.5px solid var(--input-border);
  border-radius: 10px;
  font-size: 14px;
  color: var(--text-body);
  background-color: var(--surface-default);
  outline: none;
  transition: all 0.2s ease;
  font-family: inherit;
}
.base-input__field::placeholder {
  color: var(--placeholder);
}
/* 금액 입력은 숫자를 오른쪽 정렬 (자릿수 읽기 쉽게) */
.base-input__field--amount {
  text-align: right;
}
/* 학교이름 검색처럼: 미입력(placeholder 보임)=아주 연한 회색, 포커스/입력=흰색 */
.base-input__field:placeholder-shown {
  background-color: #f8f9fb;
}
.base-input--underline .base-input__field:placeholder-shown {
  background-color: transparent;
}
.base-input__field:focus {
  border-color: var(--kb-yellow-deep);
  background-color: var(--surface-default); /* 포커스하면 흰 배경 */
  box-shadow: 0 0 0 3px var(--focus-ring-yellow);
}
/* 크롬 자동완성(autofill) 연파랑 배경 제거 → 우리 흰 배경/글자색 유지 */
.base-input__field:-webkit-autofill,
.base-input__field:-webkit-autofill:hover,
.base-input__field:-webkit-autofill:focus {
  -webkit-text-fill-color: var(--text-body);
  -webkit-box-shadow: 0 0 0 1000px var(--surface-default) inset;
  caret-color: var(--text-body);
  /* 자동완성 배경이 다시 파랗게 칠해지는 애니메이션 지연으로 사실상 무효화 */
  transition: background-color 9999s ease-in-out 0s;
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

.base-input__range .dropdown {
  flex: 1;
  min-width: 0;
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
/* 금액(우측 정렬)은 숫자와 suffix('만원' 등)가 붙지 않도록 오른쪽 여백 확대 */
.base-input__field--amount.has-suffix {
  padding-right: 56px;
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
  font-size: 15px;
  color: var(--text-body);
  font-weight: 500;
  cursor: pointer;
  transition: border-color 0.2s;
  position: relative;
}

/* 아직 선택 안 한 select은 placeholder 회색으로 */
.dropdown__button--placeholder {
  color: var(--placeholder);
}

.dropdown__button:hover,
.dropdown__button.is-open {
  border-bottom-color: var(--kb-yellow-deep);
  box-shadow: none;
}

.dropdown__button:disabled {
  color: var(--text-hint);
  cursor: not-allowed;
}

.dropdown__button:disabled:hover {
  border-bottom-color: var(--kb-gold);
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

/* 드롭다운 목록 = 캐스케이드 Select(섹션4) 패널과 동일한 스타일 */
.dropdown__menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  left: auto;
  min-width: 100%;
  max-height: 252px;
  overflow-y: auto;
  margin: 0;
  padding: 0;
  background-color: var(--surface-default);
  border: 1px solid var(--line);
  border-radius: 10px;
  box-shadow: 0 4px 12px var(--shadow-dropdown);
  list-style: none;
  z-index: 100;
}

.dropdown__item {
  display: flex;
  align-items: center;
  min-height: 44px;
  padding: 10px 12px;
  font-size: 14px;
  color: var(--kb-dark-gray);
  cursor: pointer;
  transition:
    background-color 0.2s,
    color 0.2s;
  white-space: nowrap;
}

/* 선택 안 된 항목 호버 = 회색 (연노랑 X) */
.dropdown__item:not(.is-selected):hover {
  background-color: var(--line);
}

.dropdown__item.is-selected {
  background-color: var(--kb-yellow);
  color: var(--kb-dark-gray);
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
