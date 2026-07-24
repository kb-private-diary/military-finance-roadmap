<script setup>
// SCR-COM-02 · 약관 안내  (담당: 호빈)
// 이용약관·개인정보 처리방침 동의 화면
import { ref, computed, onMounted } from 'vue';
import termsApi from '@/api/termsApi';
import TermsAccordion from '@/components/common/TermsAccordion.vue';

const terms = ref([]);
const agreedIds = ref([]);
const loading = ref(true);
const errorMessage = ref('');

const requiredIds = computed(() =>
  terms.value.filter((term) => term.required).map((term) => term.termsId),
);

const allRequiredAgreed = computed(() =>
  requiredIds.value.every((id) => agreedIds.value.includes(id)),
);

const fetchTerms = async () => {
  loading.value = true;
  try {
    terms.value = await termsApi.findTerms();
  } catch (e) {
    console.error(e);
    errorMessage.value = '약관 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.';
  } finally {
    loading.value = false;
  }
};

const confirm = () => {
  if (!allRequiredAgreed.value) {
    alert('필수 약관에 모두 동의해주세요.');
    return;
  }
  // 회원가입 흐름에서 이어서 쓸 수 있도록 동의 결과를 세션에 보관해둔다.
  sessionStorage.setItem('agreedTermsIds', JSON.stringify(agreedIds.value));
  alert('약관에 동의하셨습니다.');
};

onMounted(fetchTerms);
</script>

<template>
  <div class="container py-4" style="max-width: 640px">
    <h2 class="mb-2">약관 안내</h2>
    <p class="text-muted small">SCR-COM-02 · <code>/terms</code></p>

    <div v-if="loading" class="text-center text-muted py-5">불러오는 중...</div>
    <div v-else-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
    <template v-else>
      <TermsAccordion v-model="agreedIds" :terms="terms" />

      <button
        type="button"
        class="btn btn-warning w-100 mt-4 fw-semibold"
        :disabled="!allRequiredAgreed"
        @click="confirm"
      >
        확인
      </button>
    </template>
  </div>
</template>
