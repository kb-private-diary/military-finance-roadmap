<script setup>
// SCR-COM-07 · 비밀번호 찾기 (담당: 호빈)
// 본인 확인(아이디+이름+전화번호) 후 비밀번호 재설정
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import memberApi from '@/api/memberApi';
import BaseInput from '@/components/common/BaseInput.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();
const PASSWORD_MIN_LENGTH = 8;

const form = reactive({
  userId: '',
  name: '',
  phone: '',
  newPassword: '',
  newPasswordConfirm: '',
});

const submitting = ref(false);
const errorMessage = ref('');
const done = ref(false);

const passwordHasDigit = computed(() => /\d/.test(form.newPassword));
const passwordHasSpecial = computed(() => /[^A-Za-z0-9]/.test(form.newPassword));
const passwordPolicyValid = computed(
  () =>
    form.newPassword.length >= PASSWORD_MIN_LENGTH &&
    passwordHasDigit.value &&
    passwordHasSpecial.value,
);
const passwordConfirmMessage = computed(() => {
  if (!form.newPasswordConfirm) return '';
  if (form.newPassword !== form.newPasswordConfirm) return '비밀번호가 일치하지 않습니다.';
  return '비밀번호가 일치합니다.';
});
const passwordConfirmOk = computed(
  () => form.newPasswordConfirm.length > 0 && form.newPassword === form.newPasswordConfirm,
);

const canSubmit = computed(
  () =>
    form.userId &&
    form.name &&
    form.phone &&
    passwordPolicyValid.value &&
    passwordConfirmOk.value &&
    !submitting.value,
);

const submit = async () => {
  errorMessage.value = '';
  submitting.value = true;
  try {
    await memberApi.resetPassword({ ...form });
    done.value = true;
  } catch (e) {
    errorMessage.value = e.response?.data?.message || '비밀번호 재설정에 실패했습니다.';
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="find-password-page">
    <PageHeader title="비밀번호 찾기" />

    <template v-if="!done">
      <form class="find-password-form" @submit.prevent="submit">
        <BaseInput v-model="form.userId" label="아이디" placeholder="이메일 아이디를 입력하세요" />
        <BaseInput v-model="form.name" label="이름" placeholder="이름을 입력하세요" />
        <BaseInput v-model="form.phone" type="phone" label="전화번호" placeholder="010-0000-0000" />

        <div class="find-password-form__field">
          <BaseInput
            v-model="form.newPassword"
            type="password"
            label="새 비밀번호"
            placeholder="8자 이상, 숫자·특수문자 포함"
          />
          <p
            v-if="form.newPassword && !passwordPolicyValid"
            class="text-caption find-password-form__message is-error"
          >
            비밀번호는 8자 이상, 숫자와 특수문자를 포함해야 합니다.
          </p>
        </div>

        <div class="find-password-form__field">
          <BaseInput
            v-model="form.newPasswordConfirm"
            type="password"
            label="새 비밀번호 확인"
            placeholder="비밀번호를 다시 입력하세요"
          />
          <p
            v-if="passwordConfirmMessage"
            class="text-caption find-password-form__message"
            :class="{ 'is-ok': passwordConfirmOk, 'is-error': !passwordConfirmOk }"
          >
            {{ passwordConfirmMessage }}
          </p>
        </div>

        <p v-if="errorMessage" class="find-password-form__message is-error text-caption">
          {{ errorMessage }}
        </p>
      </form>

      <BottomButtonBar
        primary-label="비밀번호 재설정"
        :primary-disabled="!canSubmit"
        @primary-click="submit"
      />
    </template>

    <template v-else>
      <div class="find-password-result">
        <p class="find-password-result__title">비밀번호가 변경되었습니다.</p>
        <p class="text-caption">새 비밀번호로 다시 로그인해주세요.</p>
      </div>

      <BottomButtonBar primary-label="로그인하러 가기" @primary-click="router.push({ name: 'Login' })" />
    </template>
  </div>
</template>

<style scoped>
.find-password-page {
  padding-bottom: 80px;
}

.find-password-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.find-password-form__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.find-password-form__message {
  margin: 0;
}

.find-password-form__message.is-ok {
  color: #2e9e5b;
}

.find-password-form__message.is-error {
  color: var(--danger);
}

.find-password-result {
  margin-top: 60px;
  text-align: center;
}

.find-password-result__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong);
  margin-bottom: 4px;
}
</style>
