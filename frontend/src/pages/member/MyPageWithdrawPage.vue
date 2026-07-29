<script setup>
// SCR-MYP-04 · 회원 탈퇴 (담당: 호빈)
// 탈퇴 사유 선택 + 비밀번호 확인 후 소프트 삭제, 성공 시 로그아웃 처리
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import memberApi from '@/api/memberApi';
import { useAuthStore } from '@/stores/auth';
import { useToast } from '@/composables/useToast';
import BaseInput from '@/components/common/BaseInput.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();
const auth = useAuthStore();
const { show } = useToast();

const REASONS = [
  { label: '더 이상 이용하지 않아요', value: 'NOT_USING' },
  { label: '원하는 정보가 없어요', value: 'NO_INFO' },
  { label: '사용이 불편해요', value: 'INCONVENIENT' },
  { label: '개인정보가 걱정돼요', value: 'PRIVACY' },
  { label: '기타', value: 'ETC' },
];

const form = reactive({
  reason: '',
  password: '',
});

const submitting = ref(false);
const errorMessage = ref('');

const canSubmit = computed(() => form.reason && form.password && !submitting.value);

const submit = async () => {
  if (!confirm('정말 탈퇴하시겠습니까? 탈퇴 후에는 계정을 되돌릴 수 없습니다.')) return;

  errorMessage.value = '';
  submitting.value = true;
  try {
    await memberApi.withdraw({ ...form });
    show('탈퇴가 완료되었습니다.', 'success');
    auth.logout();
    router.push({ name: 'Login' });
  } catch (e) {
    errorMessage.value = e.response?.data?.message || '탈퇴 처리 중 오류가 발생했습니다.';
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="mypage-withdraw-page">
    <h1 class="text-title mt-4 mb-4">회원 탈퇴</h1>
    <p class="text-caption mypage-withdraw-page__notice">
      탈퇴 시 저축 목표, 시뮬레이션 등 모든 데이터에 더 이상 접근할 수 없습니다.
    </p>

    <form class="mypage-withdraw-form" @submit.prevent="submit">
      <div class="mypage-withdraw-form__field">
        <p class="base-input__label">탈퇴 사유</p>
        <div class="mypage-withdraw-form__reasons">
          <button
            v-for="opt in REASONS"
            :key="opt.value"
            type="button"
            class="mypage-withdraw-form__reason"
            :class="{ 'is-selected': form.reason === opt.value }"
            @click="form.reason = opt.value"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>

      <BaseInput
        v-model="form.password"
        type="password"
        label="비밀번호 확인"
        placeholder="본인 확인을 위해 비밀번호를 입력하세요"
      />

      <p v-if="errorMessage" class="mypage-withdraw-form__error text-caption">{{ errorMessage }}</p>
    </form>

    <BottomButtonBar
      primary-label="탈퇴하기"
      primary-variant="danger"
      :primary-disabled="!canSubmit"
      @primary-click="submit"
    />
  </div>
</template>

<style scoped>
.mypage-withdraw-page {
  padding-bottom: 80px;
}

.mypage-withdraw-page__notice {
  margin: 0 0 24px;
  color: var(--text-muted);
}

.mypage-withdraw-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.mypage-withdraw-form__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mypage-withdraw-form__reasons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mypage-withdraw-form__reason {
  text-align: left;
  padding: 14px 16px;
  border: 1.5px solid var(--line);
  border-radius: 12px;
  background-color: #ffffff;
  font-size: 15px;
  color: var(--kb-dark-gray);
  cursor: pointer;
}

.mypage-withdraw-form__reason.is-selected {
  border-color: var(--kb-yellow-deep);
  background-color: #fff9e6;
  font-weight: 600;
}

.mypage-withdraw-form__error {
  color: var(--danger);
  margin: 0;
}
</style>
