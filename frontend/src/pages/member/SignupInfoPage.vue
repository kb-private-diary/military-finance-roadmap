<script setup>
// SCR-COM-03 · 회원가입 - 기본정보 (담당: 호빈)
// 회원가입 2단계 - 이메일(아이디) 중복확인 + 비밀번호(확인) + 이름 + 전화번호
// 약관동의는 이 플로우의 1단계(TermsPage, /terms)에서 이미 받는다.
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import memberApi from '@/api/memberApi';
import { useSignupStore } from '@/stores/signup';
import BaseInput from '@/components/common/BaseInput.vue';
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
    passwordPolicyValid.value &&
    passwordConfirmOk.value &&
    form.name &&
    form.phone &&
    !submitting.value,
);

// 아이디(이메일)를 다시 고치면 중복확인을 다시 받아야 한다
const resetIdCheck = () => {
  idCheck.value = null;
  idCheckMessage.value = '';
};

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
    <p class="text-overline mt-4">회원가입 2/3</p>
    <h1 class="text-title mb-4">기본정보 입력</h1>

    <form class="signup-form" @submit.prevent="goNext">
      <div class="signup-form__field">
        <div class="text-label mb-2">
          이메일<span class="signup-form__required">*</span>
        </div>
        <div class="signup-form__email-row">
          <BaseInput
            v-model="form.localPart"
            placeholder="이메일 아이디"
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
          중복확인
        </button>
        <p
          v-if="idCheckMessage"
          class="text-caption signup-form__message"
          :class="{ 'is-ok': idCheck === true, 'is-error': idCheck === false }"
        >
          {{ idCheckMessage }}
        </p>
      </div>

      <div class="signup-form__field">
        <BaseInput
          v-model="form.password"
          type="password"
          label="비밀번호"
          placeholder="8자 이상, 숫자·특수문자 포함"
        />
        <p
          v-if="form.password && !passwordPolicyValid"
          class="text-caption signup-form__message is-error"
        >
          비밀번호는 8자 이상, 숫자와 특수문자를 포함해야 합니다.
        </p>
      </div>

      <div class="signup-form__field">
        <BaseInput
          v-model="form.passwordConfirm"
          type="password"
          label="비밀번호 확인"
          placeholder="비밀번호를 다시 입력하세요"
        />
        <p
          v-if="passwordConfirmMessage"
          class="text-caption signup-form__message"
          :class="{ 'is-ok': passwordConfirmOk, 'is-error': !passwordConfirmOk }"
        >
          {{ passwordConfirmMessage }}
        </p>
      </div>

      <BaseInput v-model="form.name" label="이름" placeholder="이름을 입력하세요" />
      <BaseInput v-model="form.phone" type="phone" label="전화번호" placeholder="010-0000-0000" />

      <p v-if="errorMessage" class="signup-form__message is-error text-caption">{{ errorMessage }}</p>
    </form>

    <BottomButtonBar
      primary-label="다음"
      :primary-disabled="!canSubmit"
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
  gap: 20px;
}

.signup-form__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.signup-form__required {
  color: var(--kb-yellow-deep, #ffbc00);
  margin-left: 2px;
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

.signup-form__check-btn {
  align-self: flex-start;
  height: 40px;
  padding: 0 14px;
  border: 1.5px solid var(--kb-yellow-deep, #ffbc00);
  border-radius: 10px;
  background-color: #fff;
  color: var(--text-body);
  font-weight: 600;
  font-size: 13px;
  white-space: nowrap;
}

.signup-form__check-btn:disabled {
  opacity: 0.5;
}

.signup-form__message {
  margin: 0;
}

.signup-form__message.is-ok {
  color: #2e9e5b;
}

.signup-form__message.is-error {
  color: var(--danger);
}
</style>
