import { ref } from 'vue';
import { defineStore } from 'pinia';

// 자동차 로드맵 위저드 '가방' — 목표설정(step1) 입력값을 화면을 옮겨도(이전 등) 유지
export const useCarStore = defineStore('car', () => {
  const draft = ref({
    isNew: true,
    budget: '',
    targetDate: '',
    region: '',
    experienceYears: null,
  });

  const setDraft = (payload) => Object.assign(draft.value, payload);

  const reset = () => {
    Object.assign(draft.value, {
      isNew: true,
      budget: '',
      targetDate: '',
      region: '',
      experienceYears: null,
    });
  };

  return { draft, setDraft, reset };
});
