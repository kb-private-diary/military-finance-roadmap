import { ref } from 'vue';
import { defineStore } from 'pinia';

// 회원가입 플로우(약관동의 → 1단계 → 2단계) 사이에서 입력값을 넘기기 위한 임시 저장소.
// 계정 생성이 끝나면(또는 플로우를 벗어나면) reset() 으로 비운다.
export const useSignupStore = defineStore('signup', () => {
  const agreedTermsIds = ref(null); // 약관동의(1단계 전) 결과. null이면 아직 약관동의를 안 거친 것
  const basic = ref(null); // { userId, password, passwordConfirm, name, phone, agreedTermsIds }

  const setAgreedTermsIds = (value) => {
    agreedTermsIds.value = value;
  };

  const setBasic = (value) => {
    basic.value = value;
  };

  const reset = () => {
    agreedTermsIds.value = null;
    basic.value = null;
  };

  return { agreedTermsIds, basic, setAgreedTermsIds, setBasic, reset };
});
