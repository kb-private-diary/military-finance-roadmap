<script setup>
// SCR-MYP-03 · 비밀번호 변경 (담당: 호빈)
// 로그인 상태에서 현재 비밀번호 확인 후 새 비밀번호로 변경
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import memberApi from '@/api/memberApi';
import BaseInput from '@/components/common/BaseInput.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();
const PASSWORD_MIN_LENGTH = 8;

const form = reactive({
  currentPassword: '',
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
const isSameAsCurrentPassword = computed(
  () =>
    form.currentPassword.length > 0 &&
    form.newPassword.length > 0 &&
    form.currentPassword === form.newPassword,
);

const canSubmit = computed(
  () =>
    form.currentPassword &&
    passwordPolicyValid.value &&
    passwordConfirmOk.value &&
    !isSameAsCurrentPassword.value &&
    !submitting.value,
);

const submit = async () => {
  errorMessage.value = '';
  submitting.value = true;
  try {
    await memberApi.changePassword({ ...form });
    done.value = true;
  } catch (e) {
    errorMessage.value = e.response?.data?.message || '비밀번호 변경에 실패했습니다.';
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="mypage-password-page">
    <PageHeader title="비밀번호 변경" />

    <template v-if="!done">
      <form class="mypage-password-form" @submit.prevent="submit">
        <BaseInput
          v-model="form.currentPassword"
          type="password"
          label="현재 비밀번호"
          placeholder="현재 비밀번호를 입력하세요"
        />

        <div class="mypage-password-form__field">
          <BaseInput
            v-model="form.newPassword"
            type="password"
            label="새 비밀번호"
            placeholder="8자 이상, 숫자·특수문자 포함"
          />
          <p
            v-if="form.newPassword && !passwordPolicyValid"
            class="text-caption mypage-password-form__message is-error"
          >
            비밀번호는 8자 이상, 숫자와 특수문자를 포함해야 합니다.
          </p>
          <p
            v-else-if="isSameAsCurrentPassword"
            class="text-caption mypage-password-form__message is-error"
          >
            새 비밀번호는 이전 비밀번호와 달라야 합니다.
          </p>
        </div>

        <div class="mypage-password-form__field">
          <BaseInput
            v-model="form.newPasswordConfirm"
            type="password"
            label="새 비밀번호 확인"
            placeholder="비밀번호를 다시 입력하세요"
          />
          <p
            v-if="passwordConfirmMessage"
            class="text-caption mypage-password-form__message"
            :class="{ 'is-ok': passwordConfirmOk, 'is-error': !passwordConfirmOk }"
          >
            {{ passwordConfirmMessage }}
          </p>
        </div>

        <p v-if="errorMessage" class="mypage-password-form__message is-error text-caption">
          {{ errorMessage }}
        </p>
      </form>

      <BottomButtonBar
        secondary-label="이전"
        primary-label="비밀번호 변경"
        :primary-disabled="!canSubmit"
        @secondary-click="router.push({ name: 'MyPage' })"
        @primary-click="submit"
      />
    </template>

    <template v-else>
      <div class="mypage-password-result">
        <p class="mypage-password-result__title">비밀번호가 변경되었습니다.</p>
      </div>

      <BottomButtonBar primary-label="마이페이지로" @primary-click="router.push({ name: 'MyPage' })" />
    </template>
  </div>
</template>

<style scoped>
.mypage-password-page {
  padding-bottom: 80px;
}

.mypage-password-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.mypage-password-form__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mypage-password-form__message {
  margin: 0;
}

.mypage-password-form__message.is-ok {
  color: var(--success);
}

.mypage-password-form__message.is-error {
  color: var(--danger);
}

.mypage-password-result {
  margin-top: 60px;
  text-align: center;
}

.mypage-password-result__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong);
}
</style>
