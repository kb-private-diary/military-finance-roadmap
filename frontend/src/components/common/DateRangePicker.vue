<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { formatDate, toIsoDate } from '@/util/format';

const props = defineProps({
  modelValue: { type: Array, default: () => ['', ''] },
  label: { type: String, default: '' },
  min: { type: String, default: '' },
  max: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
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
  parseIsoDate(props.modelValue?.[0]) || parseIsoDate(props.min) || new Date();

const visibleMonth = ref(
  new Date(
    initialVisibleDate().getFullYear(),
    initialVisibleDate().getMonth(),
    1,
  ),
);

const startDate = computed(() => props.modelValue?.[0] || '');
const endDate = computed(() => props.modelValue?.[1] || '');

const triggerLabel = computed(() => {
  if (!startDate.value) return '출발일과 도착일을 선택해주세요.';
  if (!endDate.value) return `${formatDate(startDate.value)} → 도착일 선택`;
  return `${formatDate(startDate.value)} ~ ${formatDate(endDate.value)}`;
});

const selectionGuide = computed(() =>
  startDate.value && !endDate.value
    ? '도착일을 선택해주세요.'
    : '출발일을 먼저 선택해주세요.',
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
  const selected = parseIsoDate(startDate.value) || initialVisibleDate();
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

  if (!startDate.value || endDate.value || day.value < startDate.value) {
    emit('update:modelValue', [day.value, '']);
    return;
  }

  emit('update:modelValue', [startDate.value, day.value]);
  isOpen.value = false;
};

const clear = () => {
  emit('update:modelValue', ['', '']);
  visibleMonth.value = new Date(
    initialVisibleDate().getFullYear(),
    initialVisibleDate().getMonth(),
    1,
  );
};

const isInRange = (value) =>
  Boolean(
    startDate.value &&
      endDate.value &&
      value > startDate.value &&
      value < endDate.value,
  );

const closeOnOutsideClick = (event) => {
  if (rootRef.value && !rootRef.value.contains(event.target)) {
    isOpen.value = false;
  }
};

const closeOnEscape = (event) => {
  if (event.key === 'Escape') isOpen.value = false;
};

watch(
  () => props.min,
  (min) => {
    if (startDate.value && min && startDate.value < min) clear();
  },
);

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
  <div ref="rootRef" class="date-range-picker">
    <div v-if="label" class="date-range-picker__label">{{ label }}</div>

    <button
      type="button"
      class="date-range-picker__trigger"
      :class="{ 'date-range-picker__trigger--open': isOpen }"
      :disabled="disabled"
      :aria-expanded="isOpen"
      @click="open"
    >
      <span
        class="date-range-picker__trigger-text"
        :class="{
          'date-range-picker__trigger-text--placeholder': !startDate,
        }"
      >
        {{ triggerLabel }}
      </span>

      <svg
        class="date-range-picker__calendar-icon"
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
      class="date-range-picker__panel"
      :class="{ 'date-range-picker__panel--up': openUpward }"
    >
      <div class="date-range-picker__selection">
        <span>{{ selectionGuide }}</span>
        <button
          v-if="startDate"
          type="button"
          class="date-range-picker__clear"
          @click="clear"
        >
          초기화
        </button>
      </div>

      <div class="date-range-picker__header">
        <button
          type="button"
          class="date-range-picker__navigation"
          :disabled="!canGoPrevious"
          aria-label="이전 달"
          @click="moveMonth(-1)"
        >
          ‹
        </button>
        <strong>{{ calendarTitle }}</strong>
        <button
          type="button"
          class="date-range-picker__navigation"
          :disabled="!canGoNext"
          aria-label="다음 달"
          @click="moveMonth(1)"
        >
          ›
        </button>
      </div>

      <div class="date-range-picker__weekdays" aria-hidden="true">
        <span v-for="weekday in WEEKDAYS" :key="weekday">
          {{ weekday }}
        </span>
      </div>

      <div class="date-range-picker__days">
        <div
          v-for="day in calendarDays"
          :key="day.value"
          class="date-range-picker__day-cell"
          :class="{ 'date-range-picker__day-cell--range': isInRange(day.value) }"
        >
          <button
            type="button"
            class="date-range-picker__day"
            :class="{
              'date-range-picker__day--outside': !day.currentMonth,
              'date-range-picker__day--selected':
                day.value === startDate || day.value === endDate,
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
.date-range-picker {
  position: relative;
  display: flex;
  width: 100%;
  flex-direction: column;
  gap: 8px;
}

.date-range-picker__label {
  color: var(--kb-dark-gray);
  font-size: 15px;
  font-weight: 600;
}

.date-range-picker__trigger {
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

.date-range-picker__trigger:hover,
.date-range-picker__trigger:focus-visible,
.date-range-picker__trigger--open {
  border-bottom-color: var(--kb-yellow-deep);
  outline: none;
}

.date-range-picker__trigger:disabled {
  background: var(--surface-subtle);
  color: var(--text-disabled);
  cursor: not-allowed;
}

.date-range-picker__trigger-text {
  flex: 1;
  overflow: hidden;
  font-size: 15px;
  font-weight: 500;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.date-range-picker__trigger-text--placeholder {
  color: var(--placeholder);
  font-weight: 400;
}

.date-range-picker__calendar-icon {
  position: absolute;
  right: 4px;
  color: var(--gray-mid);
}

.date-range-picker__panel {
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
.date-range-picker__panel--up {
  top: auto;
  bottom: calc(100% + 6px);
}

.date-range-picker__selection,
.date-range-picker__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.date-range-picker__selection {
  min-height: 28px;
  margin-bottom: 6px;
  color: var(--text-muted);
  font-size: 12px;
}

.date-range-picker__clear {
  padding: 4px;
  border: 0;
  background: transparent;
  color: var(--kb-gold);
  font: inherit;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.date-range-picker__header {
  min-height: 38px;
  color: var(--text-strong);
  font-size: 15px;
}

.date-range-picker__navigation {
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

.date-range-picker__navigation:hover:not(:disabled) {
  background: var(--line);
}

.date-range-picker__navigation:disabled {
  color: var(--text-disabled);
  cursor: not-allowed;
}

.date-range-picker__weekdays,
.date-range-picker__days {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
}

.date-range-picker__weekdays {
  margin: 5px 0;
  color: var(--text-hint);
  font-size: 11px;
  text-align: center;
}

.date-range-picker__weekdays span:first-child {
  color: var(--danger);
}

.date-range-picker__day-cell {
  display: grid;
  min-width: 0;
  height: 36px;
  place-items: center;
}

.date-range-picker__day-cell--range {
  background: var(--kb-yellow-pale);
}

.date-range-picker__day {
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

.date-range-picker__day:hover:not(:disabled) {
  background: var(--line);
}

/* 선택 범위(연노랑) 안의 날짜는 호버 오버레이 없이 밴드를 균일하게 유지 */
.date-range-picker__day-cell--range .date-range-picker__day:hover:not(:disabled) {
  background: transparent;
}

.date-range-picker__day--outside {
  color: var(--text-hint);
}

.date-range-picker__day--selected,
.date-range-picker__day--selected:hover:not(:disabled) {
  background: var(--kb-yellow);
  color: var(--kb-dark-gray);
  font-weight: 700;
}

.date-range-picker__day:disabled {
  color: var(--text-disabled);
  cursor: not-allowed;
  opacity: 0.55;
}
</style>
