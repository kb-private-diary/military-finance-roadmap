<script setup>
// SCR-RENT-02 · step1) 자취 지역 선택  (담당: 수연 / 데모)
// 시/도 → 시/군/구 → 읍/면/동 선택, 최대 5개
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import StepIndicator from '@/components/common/StepIndicator.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();
const MAX = 5;

// 데모용 지역 옵션 (실제로는 GET /api/rent/regions 3단계 조회)
const sidoOptions = [
  { value: '부산', label: '부산광역시' },
  { value: '서울', label: '서울특별시' },
];
const sigunguOptions = [
  { value: '부산진구', label: '부산진구' },
  { value: '해운대구', label: '해운대구' },
];
const umdOptions = [
  { value: '부전동', label: '부전동' },
  { value: '전포동', label: '전포동' },
  { value: '중동', label: '중동' },
];

const sido = ref('부산');
const sigungu = ref('부산진구');
const umd = ref('');
const selected = ref([]);

const canAdd = computed(() => umd.value && selected.value.length < MAX);

const addRegion = () => {
  if (!canAdd.value) return;
  const name = `${sigungu.value} ${umd.value}`;
  if (!selected.value.includes(name)) selected.value.push(name);
  umd.value = '';
};

const removeRegion = (name) => {
  selected.value = selected.value.filter((r) => r !== name);
};

const goNext = () => router.push({ name: 'RentListingList', params: { goalId: 1 } });
</script>

<template>
  <div class="page">
    <StepIndicator :current="1" :total="4" />

    <h2 class="page__title">희망 지역을 선택해주세요</h2>
    <p class="page__sub">최대 {{ MAX }}개까지 선택할 수 있어요</p>

    <div class="selects">
      <BaseInput v-model="sido" type="select" :options="sidoOptions" placeholder="시/도" />
      <BaseInput v-model="sigungu" type="select" :options="sigunguOptions" placeholder="시/군/구" />
      <BaseInput v-model="umd" type="select" :options="umdOptions" placeholder="읍/면/동" />
    </div>

    <button type="button" class="add-btn" :disabled="!canAdd" @click="addRegion">
      + 지역 추가
    </button>

    <div v-if="selected.length" class="tags">
      <span v-for="r in selected" :key="r" class="tag">
        {{ r }}
        <button type="button" class="tag__x" @click="removeRegion(r)">✕</button>
      </span>
    </div>
  </div>

  <BottomButtonBar
    primary-label="다음"
    :primary-disabled="selected.length === 0"
    @primary-click="goNext"
  />
</template>

<style scoped>
.page {
  padding: 8px 0 96px;
}
.page__title {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
}
.page__sub {
  margin: 0 0 24px;
  font-size: 14px;
  color: var(--text-muted);
}
.selects {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.add-btn {
  width: 100%;
  margin-top: 20px;
  padding: 14px;
  border: 1px dashed var(--kb-yellow-deep);
  border-radius: 12px;
  background-color: #fffdf3;
  color: var(--kb-dark-gray);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}
.add-btn:disabled {
  border-color: var(--line);
  background-color: #fafafa;
  color: var(--text-disabled);
  cursor: not-allowed;
}
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 20px;
}
.tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 999px;
  background-color: var(--kb-yellow);
  color: var(--text-strong);
  font-size: 13px;
  font-weight: 600;
}
.tag__x {
  border: none;
  background: none;
  color: var(--kb-dark-gray);
  cursor: pointer;
  font-size: 11px;
  padding: 0;
}
</style>
