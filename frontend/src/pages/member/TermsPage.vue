<script setup>
// SCR-COM-02 · 약관 안내  (담당: 호빈)
// 회원가입 플로우의 첫 단계: 약관 동의 → 회원가입 1단계(기본정보)로 이동
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import termsApi from '@/api/termsApi';
import { useSignupStore } from '@/stores/signup';
import TermsAccordion from '@/components/common/TermsAccordion.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();
const signupStore = useSignupStore();

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
  signupStore.setAgreedTermsIds(agreedIds.value);
  router.push({ name: 'SignupInfo' });
};

onMounted(fetchTerms);
</script>

<template>
  <div class="container py-4" style="max-width: 640px">
    <p class="text-overline mt-2">회원가입 1/3</p>
    <h2 class="mb-2">약관 안내</h2>

    <div v-if="loading" class="text-center text-muted py-5">불러오는 중...</div>
    <div v-else-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
    <template v-else>
      <TermsAccordion v-model="agreedIds" :terms="terms" />

      <BottomButtonBar
        primary-label="다음"
        :primary-disabled="!allRequiredAgreed"
        @primary-click="confirm"
      />
    </template>
  </div>
</template>
