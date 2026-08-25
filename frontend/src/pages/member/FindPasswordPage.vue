<script setup>
// SCR-COM-07 · 비밀번호 찾기 (담당: 호빈)
// 1단계: 아이디·이름·연락처 확인 → 2단계: 새 비밀번호 설정 (서버가 본인확인 후 재설정)
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

const verified = ref(false); // 본인확인 입력 완료 → 비밀번호 변경칸 노출
const done = ref(false);
const submitting = ref(false);
const errorMessage = ref('');

const identityFilled = computed(() => form.userId && form.name && form.phone);

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

const canReset = computed(
  () => passwordPolicyValid.value && passwordConfirmOk.value && !submitting.value,
);

// 1단계: 아이디·이름·연락처를 입력하면 아래 비밀번호 변경 섹션을 연다
// (실제 본인 일치 검증은 '비밀번호 변경' 시 서버에서 수행)
const confirmIdentity = () => {
  errorMessage.value = '';
  if (!identityFilled.value) {
    errorMessage.value = '아이디·이름·연락처를 모두 입력해주세요.';
    return;
  }
  verified.value = true;
};

// 본인확인 정보를 다시 고치면 확인 상태 초기화
const resetVerify = () => {
  if (verified.value) verified.value = false;
};

// 2단계: 새 비밀번호로 재설정 (서버가 아이디+이름+연락처 일치 확인 후 변경)
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

    <!-- 1단계: 본인 확인 -->
    <form class="find-password-form" @submit.prevent="confirmIdentity">
      <BaseInput
        v-model="form.userId"
        label="아이디"
        placeholder="가입 시 등록한 아이디를 입력하세요."
        @input="resetVerify"
      />
      <BaseInput
        v-model="form.name"
        label="이름"
        placeholder="이름을 입력하세요."
        @input="resetVerify"
      />
      <BaseInput
        v-model="form.phone"
        type="phone"
        label="연락처"
        placeholder="가입 시 등록한 전화번호를 입력하세요."
        @input="resetVerify"
      />

      <button
        v-if="!verified"
        type="submit"
        class="find-password-submit"
        :disabled="!identityFilled"
      >
        비밀번호 찾기
      </button>
    </form>

    <!-- 섹션 구분 굵은 회색선 (금융계산기와 동일 스타일) -->
    <div class="find-password-divider" aria-hidden="true"></div>

    <!-- 2단계: 새 비밀번호 설정 (본인확인 후 노출) -->
    <template v-if="verified && !done">
      <form class="find-password-form" @submit.prevent="submit">
        <p class="find-password-section-title">새 비밀번호 설정</p>

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

        <button
          type="submit"
          class="find-password-submit"
          :disabled="!canReset"
        >
          비밀번호 변경
        </button>
      </form>
    </template>

    <!-- 완료 -->
    <div v-if="done" class="find-password-result">
      <p class="find-password-result__title">비밀번호가 변경됐어요</p>
      <p class="text-caption">새 비밀번호로 다시 로그인해주세요.</p>
    </div>

    <p
      v-if="errorMessage"
      class="find-password-form__message is-error text-caption find-password-error"
    >
      {{ errorMessage }}
    </p>

    <BottomButtonBar
      secondary-label="이전"
      primary-label="로그인 하기"
      @secondary-click="router.back()"
      @primary-click="router.push({ name: 'Login' })"
    />
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

.find-password-section-title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}

/* 폼 안 노란 버튼 (아이디 찾기와 동일 톤) */
.find-password-submit {
  width: 100%;
  height: 50px;
  margin-top: 4px;
  border: 0;
  border-radius: 12px;
  background: var(--kb-yellow);
  color: var(--text-strong);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.find-password-submit:disabled {
  background: var(--kb-gray-pale);
  color: var(--text-disabled);
  cursor: not-allowed;
}

/* 섹션 구분 굵은 회색선 (금융계산기 product-calc__divider와 동일) */
.find-password-divider {
  height: 8px;
  margin: 24px -24px;
  background-color: var(--bg-gray);
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

.find-password-error {
  margin-top: 16px;
}

.find-password-result {
  margin-top: 8px;
  text-align: center;
}

.find-password-result__title {
  margin-bottom: 4px;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong);
}
</style>
