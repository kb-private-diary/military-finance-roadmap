<script setup>
// SCR-COM-03 · 회원가입 - 기본정보 (담당: 호빈)
// 회원가입 2단계 - 이메일(아이디) 중복확인 + 비밀번호(확인) + 이름 + 전화번호
// 약관동의는 이 플로우의 1단계(TermsPage, /terms)에서 이미 받는다.
// 중복확인은 별도 버튼 없이 '다음'을 누를 때 자동으로 수행한다.
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import memberApi from '@/api/memberApi';
import { useSignupStore } from '@/stores/signup';
import BaseInput from '@/components/common/BaseInput.vue';
import SignupStepHeader from '@/components/common/SignupStepHeader.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();
const signupStore = useSignupStore();

const DOMAIN_OPTIONS = ['naver.com', 'gmail.com', 'daum.net', 'kakao.com', 'nate.com'];
const EMAIL_PATTERN = /^[\w.+-]+@[\w-]+\.[a-zA-Z]{2,}$/;
const PASSWORD_MIN_LENGTH = 8;

const form = reactive({
  localPart: '',
  domainText: '',
  password: '',
  passwordConfirm: '',
  name: '',
  phone: '',
});

const email = computed(() => `${form.localPart}@${form.domainText}`);

// 도메인 콤보박스 (직접 타이핑 + ▾ 목록 선택을 한 칸에)
const domainOpen = ref(false);
const domainComboRef = ref(null);
const pickDomain = (d) => {
  form.domainText = d;
  domainOpen.value = false;
  resetIdCheck();
};
const onDomainOutside = (e) => {
  if (domainComboRef.value && !domainComboRef.value.contains(e.target)) {
    domainOpen.value = false;
  }
};
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
  if (!form.localPart || !form.domainText) {
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
  document.addEventListener('click', onDomainOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', onDomainOutside);
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
          <div ref="domainComboRef" class="signup-form__domain-combo">
            <input
              class="signup-form__domain-field"
              v-model="form.domainText"
              placeholder="직접 입력"
              @input="resetIdCheck"
              @focus="domainOpen = true"
            />
            <button
              type="button"
              class="signup-form__domain-caret"
              :class="{ 'is-open': domainOpen }"
              aria-label="도메인 목록 열기"
              @click.stop="domainOpen = !domainOpen"
            >
              <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
                <path
                  d="M7 10l5 5 5-5"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
            <ul v-if="domainOpen" class="signup-form__domain-menu">
              <li
                v-for="d in DOMAIN_OPTIONS"
                :key="d"
                class="signup-form__domain-option"
                @click="pickDomain(d)"
              >
                {{ d }}
              </li>
            </ul>
          </div>
        </div>
        <button
          type="button"
          class="signup-form__check-btn"
          :disabled="checkingId"
          @click="checkUserId"
        >
          {{ checkingId ? '확인 중...' : '중복확인' }}
        </button>
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
  gap: 20px;
}

.signup-form__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.signup-form__label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-body);
}

.signup-form__email-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.signup-form__email-row .base-input {
  flex: 1;
  min-width: 0;
}

/* 도메인 콤보박스: 타이핑 + ▾ 목록 선택을 한 칸에 */
.signup-form__email-row .signup-form__domain-combo {
  position: relative;
  flex: 1;
  min-width: 0;
}

.signup-form__domain-field {
  width: 100%;
  padding: 10px 34px 10px 12px;
  border: 1.5px solid var(--input-border, #d1d5db);
  border-radius: 10px;
  background-color: #f8f9fb;
  font-size: 14px;
  color: var(--text-body);
  outline: none;
  font-family: inherit;
  transition: border-color 0.15s ease, background-color 0.15s ease;
}

.signup-form__domain-field::placeholder {
  color: var(--placeholder);
}

.signup-form__domain-field:focus {
  border-color: var(--kb-yellow-deep, #ffbc00);
  background-color: #fff;
}

.signup-form__domain-caret {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
}

.signup-form__domain-caret svg {
  transition: transform 0.2s ease;
}

.signup-form__domain-caret.is-open svg {
  transform: rotate(180deg);
}

.signup-form__domain-menu {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  z-index: 20;
  margin: 0;
  padding: 4px;
  list-style: none;
  background: #fff;
  border: 1px solid var(--line, #e5e7ea);
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  max-height: 210px;
  overflow-y: auto;
}

.signup-form__domain-option {
  padding: 9px 10px;
  font-size: 13px;
  color: var(--text-body);
  cursor: pointer;
  border-radius: 6px;
}

.signup-form__domain-option:hover {
  background: var(--line, #f0f1f3);
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
  width: 100%;
  height: 44px;
  padding: 0 14px;
  border: 1.5px solid var(--kb-yellow-deep, #ffbc00);
  border-radius: 10px;
  background-color: #fff;
  color: var(--text-body);
  font-weight: 700;
  font-size: 14px;
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
