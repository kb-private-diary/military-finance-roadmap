<script setup>
// SCR-COM-03 · 회원가입 - 기본정보 (담당: 호빈)
// 회원가입 2단계 - 이메일(아이디) 중복확인 + 비밀번호(확인) + 이름 + 전화번호
// 약관동의는 이 플로우의 1단계(TermsPage, /terms)에서 이미 받는다.
// 중복확인은 별도 버튼 없이 '다음'을 누를 때 자동으로 수행한다.
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import memberApi from '@/api/memberApi';
import { useSignupStore } from '@/stores/signup';
import BaseInput from '@/components/common/BaseInput.vue';
import SignupStepHeader from '@/components/common/SignupStepHeader.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();
const signupStore = useSignupStore();

const EMAIL_DOMAINS = [
  { label: '네이버 (naver.com)', value: 'naver.com' },
  { label: '구글 (gmail.com)', value: 'gmail.com' },
  { label: '다음 (daum.net)', value: 'daum.net' },
  { label: '카카오 (kakao.com)', value: 'kakao.com' },
  { label: '네이트 (nate.com)', value: 'nate.com' },
  { label: '직접입력', value: 'custom' },
];
const EMAIL_PATTERN = /^[\w.+-]+@[\w-]+\.[a-zA-Z]{2,}$/;
const PASSWORD_MIN_LENGTH = 8;

const form = reactive({
  localPart: '',
  domain: 'naver.com',
  customDomain: '',
  password: '',
  passwordConfirm: '',
  name: '',
  phone: '',
});

const email = computed(() =>
  form.domain === 'custom' ? `${form.localPart}@${form.customDomain}` : `${form.localPart}@${form.domain}`,
);
const isValidEmailFormat = computed(() => EMAIL_PATTERN.test(email.value));

const idCheck = ref(null); // null: 미확인, true: 사용가능, false: 사용불가/중복
const idCheckMessage = ref('');
const checkingId = ref(false);

const passwordHasDigit = computed(() => /\d/.test(form.password));
const passwordHasSpecial = computed(() => /[^A-Za-z0-9]/.test(form.password));
const passwordPolicyValid = computed(
  () => form.password.length >= PASSWORD_MIN_LENGTH && passwordHasDigit.value && passwordHasSpecial.value,
);
const passwordConfirmMessage = computed(() => {
  if (!form.passwordConfirm) return '';
  if (form.password !== form.passwordConfirm) return '비밀번호가 일치하지 않습니다.';
  return '비밀번호가 일치합니다.';
});
const passwordConfirmOk = computed(
  () => form.passwordConfirm.length > 0 && form.password === form.passwordConfirm,
);

const submitting = ref(false);
const errorMessage = ref('');

const canSubmit = computed(
  () =>
    idCheck.value === true &&
    isValidEmailFormat.value &&
    passwordPolicyValid.value &&
    passwordConfirmOk.value &&
    form.name &&
    form.phone &&
    !submitting.value,
);

// 아이디(이메일)를 다시 고치면 이전 중복확인 결과를 지운다
const resetIdCheck = () => {
  idCheck.value = null;
  idCheckMessage.value = '';
};

// 이메일(아이디) 중복확인
const checkUserId = async () => {
  if (!form.localPart || (form.domain === 'custom' && !form.customDomain)) {
    idCheck.value = false;
    idCheckMessage.value = '아이디를 입력하세요.';
    return;
  }
  if (!isValidEmailFormat.value) {
    idCheck.value = false;
    idCheckMessage.value = '사용할 수 없는 아이디입니다.';
    return;
  }
  checkingId.value = true;
  try {
    const isDuplicate = await memberApi.checkDuplicate(email.value);
    idCheck.value = !isDuplicate;
    idCheckMessage.value = isDuplicate ? '이미 사용중인 아이디입니다.' : '사용 가능한 아이디입니다.';
  } catch (e) {
    idCheck.value = false;
    idCheckMessage.value = '중복확인에 실패했습니다. 잠시 후 다시 시도해주세요.';
  } finally {
    checkingId.value = false;
  }
};

const goNext = async () => {
  errorMessage.value = '';
  submitting.value = true;
  try {
    const basic = {
      userId: email.value,
      password: form.password,
      passwordConfirm: form.passwordConfirm,
      name: form.name,
      phone: form.phone,
    };
    await memberApi.checkJoinBasic(basic);
    signupStore.setBasic({ ...basic, agreedTermsIds: signupStore.agreedTermsIds });
    router.push({ name: 'SignupMilitary' });
  } catch (e) {
    errorMessage.value = e.response?.data?.message || '요청 처리 중 오류가 발생했습니다.';
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  // 약관동의(1단계, /terms)를 안 거치고 바로 들어온 경우 되돌린다
  if (!signupStore.agreedTermsIds) {
    router.replace({ name: 'Terms' });
  }
});
</script>

<template>
  <div class="signup-info-page">
    <SignupStepHeader eyebrow="회원가입" step="2 / 3" title="회원정보 입력" />

    <form class="signup-form" @submit.prevent="goNext">
      <div class="signup-form__field">
        <div class="signup-form__label">이메일</div>
        <div class="signup-form__email-row">
          <BaseInput
            v-model="form.localPart"
            placeholder="아이디"
            @input="resetIdCheck"
          />
          <span class="signup-form__at">@</span>
          <BaseInput
            v-if="form.domain === 'custom'"
            v-model="form.customDomain"
            placeholder="도메인 직접입력"
            @input="resetIdCheck"
          />
          <BaseInput
            v-else
            type="select"
            v-model="form.domain"
            placeholder="선택"
            :options="EMAIL_DOMAINS"
            @update:modelValue="resetIdCheck"
          />
        </div>
        <button
          type="button"
          class="signup-form__check-btn"
          :disabled="checkingId"
          @click="checkUserId"
        >
          {{ checkingId ? '확인 중...' : '중복확인' }}
        </button>
        <p class="signup-form__hint">
          이메일은 로그인 아이디로 사용되어 추후 변경이 어렵습니다.
        </p>
        <p
          v-if="idCheckMessage"
          class="signup-form__message"
          :class="{ 'is-ok': idCheck === true, 'is-error': idCheck === false }"
        >
          {{ idCheckMessage }}
        </p>
      </div>

      <div class="signup-form__field">
        <div class="signup-form__label">비밀번호</div>
        <BaseInput
          v-model="form.password"
          type="password"
          placeholder="8자 이상, 숫자·특수문자 포함"
        />
        <p
          v-if="form.password && !passwordPolicyValid"
          class="signup-form__message is-error"
        >
          비밀번호는 8자 이상, 숫자와 특수문자를 포함해야 합니다.
        </p>
      </div>

      <div class="signup-form__field">
        <div class="signup-form__label">비밀번호 확인</div>
        <BaseInput
          v-model="form.passwordConfirm"
          type="password"
          placeholder="비밀번호를 다시 입력하세요"
        />
        <p
          v-if="passwordConfirmMessage"
          class="signup-form__message"
          :class="{ 'is-ok': passwordConfirmOk, 'is-error': !passwordConfirmOk }"
        >
          {{ passwordConfirmMessage }}
        </p>
      </div>

      <div class="signup-form__field">
        <div class="signup-form__label">이름</div>
        <BaseInput v-model="form.name" placeholder="이름을 입력하세요" />
      </div>

      <div class="signup-form__field">
        <div class="signup-form__label">휴대폰번호</div>
        <BaseInput v-model="form.phone" placeholder="010-0000-0000" />
      </div>

      <p v-if="errorMessage" class="signup-form__message is-error">{{ errorMessage }}</p>
    </form>

    <BottomButtonBar
      secondary-label="이전"
      primary-label="다음"
      :primary-disabled="!canSubmit"
      @secondary-click="router.push({ name: 'Terms' })"
      @primary-click="goNext"
    />
  </div>
</template>

<style scoped>
.signup-info-page {
  padding-bottom: 80px;
}

.signup-form {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.signup-form__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.signup-form__label {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
}

.signup-form__email-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.signup-form__email-row .base-input {
  flex: 1;
  min-width: 0;
}

.signup-form__at {
  flex-shrink: 0;
  color: var(--text-muted);
  font-weight: 600;
}

/* 도메인 선택 드롭다운의 갈색(골드) 밑줄 제거 → 다른 입력창과 같은 회색 박스로 */
.signup-form__email-row :deep(.dropdown__button) {
  border: 1.5px solid var(--input-border);
  border-radius: 10px;
  background-color: #f8f9fb;
  padding: 10px 12px;
  font-size: 14px;
  color: var(--text-body);
  font-weight: 400;
}

.signup-form__email-row :deep(.dropdown__button--placeholder) {
  color: var(--placeholder);
}

.signup-form__email-row :deep(.dropdown__button:hover),
.signup-form__email-row :deep(.dropdown__button.is-open) {
  border-color: var(--kb-yellow-deep, #ffbc00);
}

.signup-form__email-row :deep(.dropdown__text) {
  text-align: left;
}

.signup-form__check-btn {
  align-self: flex-end;
  height: 34px;
  padding: 0 14px;
  border: 1.5px solid var(--kb-yellow-deep, #ffbc00);
  border-radius: 8px;
  background-color: #fff;
  color: var(--text-body);
  font-weight: 600;
  font-size: 13px;
  white-space: nowrap;
  cursor: pointer;
}

.signup-form__check-btn:disabled {
  opacity: 0.5;
  cursor: default;
}

.signup-form__hint {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
}

.signup-form__message {
  margin: 0;
  font-size: 12px;
}

.signup-form__message.is-ok {
  color: #2e9e5b;
}

.signup-form__message.is-error {
  color: var(--danger);
}
</style>
