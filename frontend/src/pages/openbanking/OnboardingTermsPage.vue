<script setup>
// SCR-COM-08b · 오픈뱅킹 이용 동의  (담당: 수연)
// 데모용 — 약관 상세 내용은 생략, 동의 체크 큰 틀만
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();

const items = ref([
  { id: 'use', label: '오픈뱅킹 서비스 이용약관 (필수)', required: true, checked: false },
  { id: 'privacy', label: '개인정보 수집·이용 동의 (필수)', required: true, checked: false },
  { id: 'third', label: '제3자 정보제공 동의 (필수)', required: true, checked: false },
  { id: 'marketing', label: '마케팅 정보 수신 (선택)', required: false, checked: false },
]);

const allChecked = computed(() => items.value.every((i) => i.checked));
const requiredAllChecked = computed(() =>
  items.value.filter((i) => i.required).every((i) => i.checked),
);

const toggleAll = () => {
  const next = !allChecked.value;
  items.value.forEach((i) => (i.checked = next));
};

const submit = () => router.push({ name: 'OnboardingDone' });
</script>

<template>
  <div class="terms">
    <h1 class="terms__title">오픈뱅킹 이용 동의</h1>

    <button
      type="button"
      class="terms__all"
      :class="{ on: allChecked }"
      @click="toggleAll"
    >
      <span class="terms__check">✓</span> 약관 전체 동의
    </button>

    <ul class="terms__list">
      <li v-for="item in items" :key="item.id" class="terms__item">
        <label>
          <input type="checkbox" v-model="item.checked" />
          <span>{{ item.label }}</span>
        </label>
      </li>
    </ul>
  </div>

  <BottomButtonBar
    primary-label="동의하고 연동하기"
    :primary-disabled="!requiredAllChecked"
    @primary-click="submit"
  />
</template>

<style scoped>
.terms {
  padding: 24px 20px 96px;
}
.terms__title {
  margin: 0 0 20px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
}
.terms__all {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background-color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
  cursor: pointer;
}
.terms__all.on {
  border-color: var(--kb-yellow);
  background-color: #fffdf3;
}
.terms__check {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background-color: var(--kb-gray-pale);
  color: #ffffff;
  font-size: 13px;
}
.terms__all.on .terms__check {
  background-color: var(--kb-yellow-deep);
}
.terms__list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.terms__item label {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 4px;
  font-size: 14px;
  color: var(--text-body);
  cursor: pointer;
}
.terms__item input {
  width: 18px;
  height: 18px;
  accent-color: var(--kb-yellow-deep);
}
</style>
