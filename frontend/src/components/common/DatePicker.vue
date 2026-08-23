<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { formatDate, toIsoDate } from '@/util/format';

const props = defineProps({
  modelValue: { type: String, default: '' },
  label: { type: String, default: '' },
  min: { type: String, default: '' },
  max: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  placeholder: { type: String, default: '날짜를 선택해주세요' },
});

const emit = defineEmits(['update:modelValue']);

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];

const rootRef = ref(null);
const isOpen = ref(false);
const openUpward = ref(false); // 아래 공간 부족하면 위로 펼침 (하단 고정버튼 가림 방지)

const parseIsoDate = (value) => {
  if (!value) return null;
  const [year, month, day] = value.split('-').map(Number);
  if (!year || !month || !day) return null;
  return new Date(year, month - 1, day);
};

const initialVisibleDate = () =>
  parseIsoDate(props.modelValue) || parseIsoDate(props.min) || new Date();

const visibleMonth = ref(
  new Date(
    initialVisibleDate().getFullYear(),
    initialVisibleDate().getMonth(),
    1,
  ),
);

const selectedDate = computed(() => props.modelValue || '');

const triggerLabel = computed(() =>
  selectedDate.value ? formatDate(selectedDate.value) : props.placeholder,
);

const calendarTitle = computed(
  () =>
    `${visibleMonth.value.getFullYear()}년 ${visibleMonth.value.getMonth() + 1}월`,
);

const calendarDays = computed(() => {
  const year = visibleMonth.value.getFullYear();
  const month = visibleMonth.value.getMonth();
  const firstWeekday = new Date(year, month, 1).getDay();

  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(year, month, index - firstWeekday + 1);
    const value = toIsoDate(date);
    return {
      value,
      day: date.getDate(),
      currentMonth: date.getMonth() === month,
      disabled:
        Boolean(props.min && value < props.min) ||
        Boolean(props.max && value > props.max),
    };
  });
});

const monthKey = (date) => date.getFullYear() * 12 + date.getMonth();

const canGoPrevious = computed(() => {
  const minDate = parseIsoDate(props.min);
  return !minDate || monthKey(visibleMonth.value) > monthKey(minDate);
});

const canGoNext = computed(() => {
  const maxDate = parseIsoDate(props.max);
  return !maxDate || monthKey(visibleMonth.value) < monthKey(maxDate);
});

const open = () => {
  if (props.disabled) return;
  const selected = parseIsoDate(selectedDate.value) || initialVisibleDate();
  visibleMonth.value = new Date(
    selected.getFullYear(),
    selected.getMonth(),
    1,
  );
  // 열기 직전: 아래 공간이 달력(약 380px)보다 좁으면 위로 펼쳐 하단 버튼 가림 방지
  if (!isOpen.value) {
    const rect = rootRef.value?.getBoundingClientRect();
    if (rect) {
      const PANEL_HEIGHT = 380;
      const SAFETY = 60;
      openUpward.value = window.innerHeight - rect.bottom < PANEL_HEIGHT + SAFETY;
    }
  }
  isOpen.value = !isOpen.value;
};

const moveMonth = (amount) => {
  visibleMonth.value = new Date(
    visibleMonth.value.getFullYear(),
    visibleMonth.value.getMonth() + amount,
    1,
  );
};

const selectDate = (day) => {
  if (day.disabled) return;
  emit('update:modelValue', day.value);
  isOpen.value = false;
};

const closeOnOutsideClick = (event) => {
  if (rootRef.value && !rootRef.value.contains(event.target)) {
    isOpen.value = false;
  }
};

const closeOnEscape = (event) => {
  if (event.key === 'Escape') isOpen.value = false;
};

onMounted(() => {
  document.addEventListener('click', closeOnOutsideClick);
  document.addEventListener('keydown', closeOnEscape);
});

onUnmounted(() => {
  document.removeEventListener('click', closeOnOutsideClick);
  document.removeEventListener('keydown', closeOnEscape);
});
</script>

<template>
  <div ref="rootRef" class="date-picker">
    <div v-if="label" class="date-picker__label">{{ label }}</div>

    <button
      type="button"
      class="date-picker__trigger"
      :class="{ 'date-picker__trigger--open': isOpen }"
      :disabled="disabled"
      :aria-expanded="isOpen"
      @click="open"
    >
      <span
        class="date-picker__trigger-text"
        :class="{
          'date-picker__trigger-text--placeholder': !selectedDate,
        }"
      >
        {{ triggerLabel }}
      </span>

      <svg
        class="date-picker__calendar-icon"
        viewBox="0 0 24 24"
        width="18"
        height="18"
        aria-hidden="true"
      >
        <path
          d="M7 3v3m10-3v3M4.5 9h15M6 5h12a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2Z"
          fill="none"
          stroke="currentColor"
          stroke-width="1.8"
          stroke-linecap="round"
        />
      </svg>
    </button>

    <div
      v-if="isOpen"
      class="date-picker__panel"
      :class="{ 'date-picker__panel--up': openUpward }"
    >
      <div class="date-picker__header">
        <button
          type="button"
          class="date-picker__navigation"
          :disabled="!canGoPrevious"
          aria-label="이전 달"
          @click="moveMonth(-1)"
        >
          ‹
        </button>
        <strong>{{ calendarTitle }}</strong>
        <button
          type="button"
          class="date-picker__navigation"
          :disabled="!canGoNext"
          aria-label="다음 달"
          @click="moveMonth(1)"
        >
          ›
        </button>
      </div>

      <div class="date-picker__weekdays" aria-hidden="true">
        <span v-for="weekday in WEEKDAYS" :key="weekday">
          {{ weekday }}
        </span>
      </div>

      <div class="date-picker__days">
        <div
          v-for="day in calendarDays"
          :key="day.value"
          class="date-picker__day-cell"
        >
          <button
            type="button"
            class="date-picker__day"
            :class="{
              'date-picker__day--outside': !day.currentMonth,
              'date-picker__day--selected': day.value === selectedDate,
            }"
            :disabled="day.disabled"
            :aria-label="day.value"
            @click="selectDate(day)"
          >
            {{ day.day }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.date-picker {
  position: relative;
  display: flex;
  width: 100%;
  flex-direction: column;
  gap: 8px;
}

.date-picker__label {
  color: var(--kb-dark-gray);
  font-size: 15px;
  font-weight: 600;
}

.date-picker__trigger {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 44px;
  padding: 10px 34px 10px 12px;
  border: 0;
  border-bottom: 2px solid var(--kb-gold);
  border-radius: 0;
  background: transparent;
  color: var(--text-body);
  font: inherit;
  font-size: 15px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.date-picker__trigger:hover,
.date-picker__trigger:focus-visible,
.date-picker__trigger--open {
  border-bottom-color: var(--kb-yellow-deep);
  outline: none;
}

.date-picker__trigger:disabled {
  background: var(--surface-subtle);
  color: var(--text-disabled);
  cursor: not-allowed;
}

.date-picker__trigger-text {
  flex: 1;
  overflow: hidden;
  font-size: 15px;
  font-weight: 500;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.date-picker__trigger-text--placeholder {
  color: var(--placeholder);
  font-weight: 400;
}

.date-picker__calendar-icon {
  position: absolute;
  right: 4px;
  color: var(--gray-mid);
}

.date-picker__panel {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  left: 0;
  z-index: 998;
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--surface-default);
  box-shadow: 0 4px 12px var(--shadow-dropdown);
}

/* 아래 공간 부족 시 위로 펼침 (하단 고정 버튼 가림 방지) */
.date-picker__panel--up {
  top: auto;
  bottom: calc(100% + 6px);
}

.date-picker__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 38px;
  color: var(--text-strong);
  font-size: 15px;
}

.date-picker__navigation {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: var(--text-body);
  font: inherit;
  font-size: 24px;
  cursor: pointer;
}

.date-picker__navigation:hover:not(:disabled) {
  background: var(--line);
}

.date-picker__navigation:disabled {
  color: var(--text-disabled);
  cursor: not-allowed;
}

.date-picker__weekdays,
.date-picker__days {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
}

.date-picker__weekdays {
  margin: 5px 0;
  color: var(--text-hint);
  font-size: 11px;
  text-align: center;
}

.date-picker__weekdays span:first-child {
  color: var(--danger);
}

.date-picker__day-cell {
  display: grid;
  min-width: 0;
  height: 36px;
  place-items: center;
}

.date-picker__day {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: var(--text-body);
  font: inherit;
  font-size: 12px;
  cursor: pointer;
}

.date-picker__day:hover:not(:disabled) {
  background: var(--line);
}

.date-picker__day--outside {
  color: var(--text-hint);
}

.date-picker__day--selected,
.date-picker__day--selected:hover:not(:disabled) {
  background: var(--kb-yellow);
  color: var(--kb-dark-gray);
  font-weight: 700;
}

.date-picker__day:disabled {
  color: var(--text-disabled);
  cursor: not-allowed;
  opacity: 0.55;
}
</style>
