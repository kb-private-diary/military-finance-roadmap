<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';

const props = defineProps({
  modelValue: { type: Array, default: () => ['', ''] },
  label: { type: String, default: '' },
  groups: { type: Array, default: () => [] },
  middleByGroup: { type: Object, default: () => ({}) },
  childrenByGroup: { type: Object, default: () => ({}) },
  placeholder: { type: String, default: '선택' },
  groupEmptyText: { type: String, default: '왼쪽에서 분류를 선택해주세요.' },
  middleEmptyText: { type: String, default: '가운데에서 분류를 선택해주세요.' },
  childEmptyText: { type: String, default: '선택 가능한 항목이 없습니다.' },
  disabled: { type: Boolean, default: false },
});

const emit = defineEmits(['update:modelValue']);

const rootRef = ref(null);
const isOpen = ref(false);
const openUpward = ref(false); // 아래 공간 부족하면 위로 펼침 (하단 고정버튼 가림 방지)

const hasMiddleLevel = computed(
  () => Object.keys(props.middleByGroup).length > 0,
);
const selectedGroup = computed(() => props.modelValue?.[0] || '');
const selectedMiddle = computed(() =>
  hasMiddleLevel.value ? props.modelValue?.[1] || '' : '',
);
const selectedChild = computed(() =>
  hasMiddleLevel.value
    ? props.modelValue?.[2] || ''
    : props.modelValue?.[1] || '',
);

const middleOptions = computed(
  () => props.middleByGroup[selectedGroup.value] || [],
);

const childOptions = computed(
  () =>
    props.childrenByGroup[
      hasMiddleLevel.value ? selectedMiddle.value : selectedGroup.value
    ] || [],
);

const selectedLabel = computed(() => {
  const group = props.groups.find(
    ({ value }) => value === selectedGroup.value,
  );
  const middle = middleOptions.value.find(
    ({ value }) => value === selectedMiddle.value,
  );
  const child = childOptions.value.find(
    ({ value }) => value === selectedChild.value,
  );

  if (!child) return '';
  return [group?.label, middle?.label, child.label].filter(Boolean).join(' · ');
});

const toggle = () => {
  if (props.disabled) return;
  // 열기 직전: 아래 공간이 패널(252px)+여유보다 좁으면 위로 펼쳐 하단 고정버튼 가림 방지
  if (!isOpen.value) {
    const rect = rootRef.value?.getBoundingClientRect();
    if (rect) {
      const PANEL_HEIGHT = 252;
      const SAFETY = 90; // 하단 고정 버튼 등 여유
      const spaceBelow = window.innerHeight - rect.bottom;
      openUpward.value = spaceBelow < PANEL_HEIGHT + SAFETY;
    }
  }
  isOpen.value = !isOpen.value;
};

const selectGroup = (groupValue) => {
  if (groupValue === selectedGroup.value) return;
  emit(
    'update:modelValue',
    hasMiddleLevel.value ? [groupValue, '', ''] : [groupValue, ''],
  );
};

const selectMiddle = (middleValue) => {
  if (middleValue === selectedMiddle.value) return;
  emit('update:modelValue', [selectedGroup.value, middleValue, '']);
};

const selectChild = (childValue) => {
  emit(
    'update:modelValue',
    hasMiddleLevel.value
      ? [selectedGroup.value, selectedMiddle.value, childValue]
      : [selectedGroup.value, childValue],
  );
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
  <div ref="rootRef" class="cascader-select">
    <div v-if="label" class="cascader-select__label">{{ label }}</div>

    <button
      type="button"
      class="cascader-select__trigger"
      :class="{ 'cascader-select__trigger--open': isOpen }"
      :disabled="disabled"
      :aria-expanded="isOpen"
      @click="toggle"
    >
      <span
        class="cascader-select__trigger-text"
        :class="{
          'cascader-select__trigger-text--placeholder': !selectedLabel,
        }"
      >
        {{ selectedLabel || placeholder }}
      </span>

      <svg
        class="cascader-select__arrow"
        :class="{ 'cascader-select__arrow--open': isOpen }"
        viewBox="0 0 24 24"
        width="16"
        height="16"
        aria-hidden="true"
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

    <div
      v-if="isOpen"
      class="cascader-select__panel"
      :class="{
        'cascader-select__panel--three': hasMiddleLevel,
        'cascader-select__panel--up': openUpward,
      }"
    >
      <div class="cascader-select__column">
        <button
          v-for="group in groups"
          :key="group.value"
          type="button"
          class="cascader-select__option"
          :class="{
            'cascader-select__option--active':
              selectedGroup === group.value,
          }"
          @click="selectGroup(group.value)"
        >
          <span>{{ group.label }}</span>
          <span class="cascader-select__option-arrow">›</span>
        </button>
      </div>

      <div class="cascader-select__column">
        <div v-if="!selectedGroup" class="cascader-select__empty">
          {{ groupEmptyText }}
        </div>

        <div
          v-else-if="hasMiddleLevel && middleOptions.length === 0"
          class="cascader-select__empty"
        >
          {{ childEmptyText }}
        </div>

        <template v-else-if="hasMiddleLevel">
          <button
            v-for="middle in middleOptions"
            :key="middle.value"
            type="button"
            class="cascader-select__option"
            :class="{
              'cascader-select__option--active':
                selectedMiddle === middle.value,
            }"
            @click="selectMiddle(middle.value)"
          >
            <span>{{ middle.label }}</span>
            <span class="cascader-select__option-arrow">›</span>
          </button>
        </template>

        <div
          v-else-if="childOptions.length === 0"
          class="cascader-select__empty"
        >
          {{ childEmptyText }}
        </div>

        <template v-else>
          <button
            v-for="child in childOptions"
            :key="child.value"
            type="button"
            class="cascader-select__option"
            :class="{
              'cascader-select__option--active':
                selectedChild === child.value,
            }"
            @click="selectChild(child.value)"
          >
            {{ child.label }}
          </button>
        </template>
      </div>

      <div v-if="hasMiddleLevel" class="cascader-select__column">
        <div v-if="!selectedMiddle" class="cascader-select__empty">
          {{ middleEmptyText }}
        </div>

        <div
          v-else-if="childOptions.length === 0"
          class="cascader-select__empty"
        >
          {{ childEmptyText }}
        </div>

        <template v-else>
          <button
            v-for="child in childOptions"
            :key="child.value"
            type="button"
            class="cascader-select__option"
            :class="{
              'cascader-select__option--active':
                selectedChild === child.value,
            }"
            @click="selectChild(child.value)"
          >
            {{ child.label }}
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cascader-select {
  position: relative;
  display: flex;
  width: 100%;
  flex-direction: column;
  gap: 8px;
}

.cascader-select__label {
  color: var(--kb-dark-gray);
  font-size: 15px;
  font-weight: 600;
}

.cascader-select__trigger {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 44px;
  padding: 10px 28px 10px 12px;
  border: 0;
  border-bottom: 2px solid var(--kb-gold);
  border-radius: 0;
  background: transparent;
  color: var(--text-body);
  font: inherit;
  cursor: pointer;
  transition: border-color 0.2s;
}

.cascader-select__trigger:hover,
.cascader-select__trigger:focus-visible,
.cascader-select__trigger--open {
  border-bottom-color: var(--kb-yellow-deep);
  outline: none;
}

.cascader-select__trigger:disabled {
  background: var(--surface-subtle);
  color: var(--text-disabled);
  cursor: not-allowed;
}

.cascader-select__trigger-text {
  flex: 1;
  overflow: hidden;
  font-size: 16px;
  font-weight: 500;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cascader-select__trigger-text--placeholder {
  color: var(--placeholder);
  font-weight: 400;
}

.cascader-select__arrow {
  position: absolute;
  right: 4px;
  color: var(--gray-mid);
  transition:
    color 0.2s,
    transform 0.3s ease;
}

.cascader-select__arrow--open {
  color: var(--kb-yellow-deep);
  transform: rotate(180deg);
}

.cascader-select__panel {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  left: 0;
  z-index: 999;
  display: grid;
  height: 252px;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: var(--surface-default);
  box-shadow: 0 4px 12px var(--shadow-dropdown);
}

.cascader-select__panel--three {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

/* 아래 공간 부족 시 위로 펼침 (하단 고정 버튼 가림 방지) */
.cascader-select__panel--up {
  top: auto;
  bottom: calc(100% + 6px);
}

.cascader-select__column {
  overflow-y: auto;
  background: var(--surface-default);
  scrollbar-width: none;
}

.cascader-select__column::-webkit-scrollbar {
  display: none;
}

.cascader-select__column + .cascader-select__column {
  border-left: 1px solid var(--line);
}

.cascader-select__option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-height: 44px;
  padding: 10px 12px;
  border: 0;
  background: var(--surface-default);
  color: var(--kb-dark-gray);
  font: inherit;
  font-size: 14px;
  text-align: left;
  cursor: pointer;
}

/* 우리 확정 디자인: 활성 아닌 옵션만 회색 호버 (F7F7F8) */
.cascader-select__option:not(.cascader-select__option--active):hover {
  background: var(--line);
}

.cascader-select__option--active {
  background: var(--kb-yellow);
  color: var(--kb-dark-gray);
  font-weight: 600;
}

.cascader-select__option-arrow {
  margin-left: 8px;
  color: var(--kb-gray);
  font-size: 16px;
}

.cascader-select__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 20px;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.5;
  text-align: center;
}
</style>
