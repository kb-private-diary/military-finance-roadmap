<script setup>
// SCR-COM-06 · 아이디 찾기 (담당: 호빈)
// 이름·전화번호로 아이디 찾기 (마스킹된 아이디 반환)
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import memberApi from '@/api/memberApi';
import BaseInput from '@/components/common/BaseInput.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();

const form = reactive({
  name: '',
  phone: '',
});

const submitting = ref(false);
const errorMessage = ref('');
const maskedUserId = ref('');

const canSubmit = computed(() => form.name && form.phone && !submitting.value);

const find = async () => {
  errorMessage.value = '';
  maskedUserId.value = '';
  submitting.value = true;
  try {
    const result = await memberApi.findUserId({ name: form.name, phone: form.phone });
    maskedUserId.value = result.maskedUserId;
  } catch (e) {
    errorMessage.value = e.response?.data?.message || '아이디를 찾을 수 없습니다.';
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="find-id-page">
    <h1 class="text-title mt-4 mb-4">아이디 찾기</h1>

    <template v-if="!maskedUserId">
      <form class="find-id-form" @submit.prevent="find">
        <BaseInput v-model="form.name" label="이름" placeholder="이름을 입력하세요" />
        <BaseInput v-model="form.phone" label="전화번호" placeholder="010-0000-0000" />

        <p v-if="errorMessage" class="find-id-form__error text-caption">{{ errorMessage }}</p>
      </form>

      <BottomButtonBar
        primary-label="아이디 찾기"
        :primary-disabled="!canSubmit"
        @primary-click="find"
      />
    </template>

    <template v-else>
      <div class="find-id-result">
        <p class="text-caption">회원님의 아이디입니다</p>
        <p class="find-id-result__value">{{ maskedUserId }}</p>
      </div>

      <BottomButtonBar
        primary-label="로그인하러 가기"
        secondary-label="비밀번호 찾기"
        @primary-click="router.push({ name: 'Login' })"
        @secondary-click="router.push({ name: 'FindPassword' })"
      />
    </template>
  </div>
</template>

<style scoped>
.find-id-page {
  padding-bottom: 80px;
}

.find-id-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.find-id-form__error {
  color: var(--danger);
  margin: 0;
}

.find-id-result {
  margin-top: 60px;
  text-align: center;
}

.find-id-result__value {
  margin-top: 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
}
</style>
