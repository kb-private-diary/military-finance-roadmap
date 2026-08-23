<script setup>
// 공통 컴포넌트: 범용 검색 선택 (담당: 수연)
// 어떤 목록이든 검색 → 실시간 결과 드롭다운 → 선택. (학교/지역/상품 등 다양하게 재사용)
// 사용 예: <SearchSelect v-model="picked" :items="schools" label-key="schoolName" sub-key="address" placeholder="학교 이름 검색" />
import { ref, computed } from 'vue';

const props = defineProps({
  modelValue: { type: Object, default: null }, // 선택된 항목
  items: { type: Array, default: () => [] }, // 검색 대상 목록
  labelKey: { type: String, default: 'label' }, // 결과 주 텍스트 키
  subKey: { type: String, default: '' }, // 결과 보조 텍스트 키 (주소 등, 없으면 생략)
  searchKeys: { type: Array, default: () => [] }, // 검색할 키들 (비우면 labelKey로 검색)
  placeholder: { type: String, default: '검색' },
  minChars: { type: Number, default: 2 }, // 최소 검색 글자 수
  emptyText: { type: String, default: '검색 결과가 없어요' },
});

const emit = defineEmits(['update:modelValue', 'select']);

const keyword = ref('');

const keys = computed(() =>
  props.searchKeys.length ? props.searchKeys : [props.labelKey],
);

const results = computed(() => {
  const q = keyword.value.trim();
  if (q.length < props.minChars) return [];
  return props.items.filter((item) =>
    keys.value.some((k) => String(item[k] ?? '').includes(q)),
  );
});

const showEmpty = computed(
  () =>
    keyword.value.trim().length >= props.minChars && results.value.length === 0,
);

const pick = (item) => {
  emit('update:modelValue', item);
  emit('select', item);
  keyword.value = '';
};
</script>

<template>
  <div class="search-select">
    <input
      v-model="keyword"
      type="text"
      class="search-select__input"
      :placeholder="placeholder"
    />

    <ul v-if="results.length" class="search-select__dropdown">
      <li
        v-for="(item, idx) in results"
        :key="idx"
        class="search-select__item"
        @click="pick(item)"
      >
        <strong>{{ item[labelKey] }}</strong>
        <span v-if="subKey">{{ item[subKey] }}</span>
      </li>
    </ul>

    <p v-else-if="showEmpty" class="search-select__hint">{{ emptyText }}</p>
  </div>
</template>

<style scoped>
.search-select {
  position: relative;
}

/* 미입력 = 회색, 포커스 = 흰색 (BaseInput과 동일 규칙) */
.search-select__input {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid var(--input-border);
  border-radius: 10px;
  background: #f8f9fb;
  font-size: 14px;
  color: var(--text-body);
  font-family: inherit;
  outline: none;
  transition:
    border-color 0.15s ease,
    background 0.15s ease;
}
.search-select__input::placeholder {
  color: var(--text-hint);
}
.search-select__input:focus {
  border-color: var(--kb-yellow-deep);
  background: var(--surface-default, #fff);
}

.search-select__dropdown {
  margin: 8px 0 0;
  padding: 6px;
  list-style: none;
  border: 1px solid var(--line);
  border-radius: 12px;
  max-height: 210px;
  overflow-y: auto;
}
.search-select__item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
}
.search-select__item:hover {
  background: var(--line); /* 연한 회색 호버 */
}
.search-select__item strong {
  font-size: 13px;
  color: var(--text-body);
}
.search-select__item span {
  font-size: 11px;
  color: var(--text-hint);
}

.search-select__hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-hint);
}
</style>
