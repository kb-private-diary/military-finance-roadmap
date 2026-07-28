<script setup>
// SCR-COM-05 · 회원가입 - 군정보 (담당: 호빈)
// 회원가입 2단계 - 군종·계급·부대·입대일·전역일 입력 → 계정 생성
// 약관동의는 1단계(SignupInfoPage)에서 이미 받았으므로 여기서는 다루지 않는다.
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import memberApi from '@/api/memberApi';
import { useSignupStore } from '@/stores/signup';
import BaseInput from '@/components/common/BaseInput.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();
const signupStore = useSignupStore();

// TODO: 군종목록조회/계급조회 API 나오면 아래 하드코딩된 목록을 API 조회로 교체
// serviceMonths는 병역 정책 문서 기준 복무기간(육군·해병대 18개월 / 해군 20개월 / 공군·공익 21개월 / 기타 24개월)
const MILITARY_TYPES = [
  { typeId: 1, name: '육군', serviceMonths: 18 },
  { typeId: 2, name: '해군', serviceMonths: 20 },
  { typeId: 3, name: '공군', serviceMonths: 21 },
  { typeId: 4, name: '해병대', serviceMonths: 18 },
  { typeId: 5, name: '공익', serviceMonths: 21 },
  { typeId: 6, name: '기타', serviceMonths: 24 },
];
const MILITARY_RANKS = [
  { rankId: 1, name: '이병' },
  { rankId: 2, name: '일병' },
  { rankId: 3, name: '상병' },
  { rankId: 4, name: '병장' },
];

const typeOptions = MILITARY_TYPES.map((t) => ({ label: t.name, value: t.typeId }));
const rankOptions = MILITARY_RANKS.map((r) => ({ label: r.name, value: r.rankId }));

const form = reactive({
  typeId: null,
  rankId: null,
  unitName: '',
  unitCode: '',
  enlistDate: '',
  dischargeDate: '',
});

const submitting = ref(false);
const errorMessage = ref('');

const canSubmit = computed(
  () =>
    form.typeId &&
    form.rankId &&
    form.unitName &&
    form.enlistDate &&
    form.dischargeDate &&
    !submitting.value,
);

// 군종 + 입대일이 정해지면 복무기간만큼 더한 전역예정일을 자동으로 채워준다 (수동으로 다시 고칠 수도 있음)
// 입대일 당일도 복무 1일차로 치므로, 개월수를 더한 날짜에서 하루를 뺀다 (예: 3/1 입대 + 18개월 → 8/31 전역)
watch(
  () => [form.typeId, form.enlistDate],
  ([typeId, enlistDate]) => {
    if (!typeId || !enlistDate) return;
    const months = MILITARY_TYPES.find((t) => t.typeId === typeId)?.serviceMonths;
    if (!months) return;
    const discharge = new Date(enlistDate);
    discharge.setMonth(discharge.getMonth() + months);
    discharge.setDate(discharge.getDate() - 1);
    form.dischargeDate = discharge.toISOString().slice(0, 10);
  },
);

const submit = async () => {
  // 1단계를 건너뛰고 바로 들어온 경우 1단계부터 다시 하도록 되돌린다
  if (!signupStore.basic) {
    router.replace({ name: 'SignupInfo' });
    return;
  }
  errorMessage.value = '';
  submitting.value = true;
  try {
    await memberApi.createMember({
      ...signupStore.basic,
      ...form,
    });
    signupStore.reset();
    router.push({ name: 'Login' });
  } catch (e) {
    errorMessage.value = e.response?.data?.message || '가입 처리 중 오류가 발생했습니다.';
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  if (!signupStore.basic) {
    router.replace({ name: 'SignupInfo' });
  }
});
</script>

<template>
  <div class="signup-military-page">
    <p class="text-overline mt-4">회원가입 3/3</p>
    <h1 class="text-title mb-4">군 정보 입력</h1>

    <div class="signup-form">
      <BaseInput
        type="select"
        v-model="form.typeId"
        label="군종"
        placeholder="군종을 선택하세요"
        :options="typeOptions"
      />

      <BaseInput
        type="select"
        v-model="form.rankId"
        label="계급"
        placeholder="계급을 선택하세요"
        :options="rankOptions"
      />

      <BaseInput v-model="form.unitName" label="부대명" placeholder="예: 수도방위사령부" />
      <BaseInput
        v-model="form.unitCode"
        label="부대코드"
        placeholder="부대코드 (선택)"
      />
      <BaseInput v-model="form.enlistDate" type="date" label="입대일" />
      <div class="signup-form__field">
        <BaseInput v-model="form.dischargeDate" type="date" label="전역예정일" />
        <p class="text-caption signup-form__hint">군종·입대일을 입력하면 자동으로 계산돼요. 필요하면 직접 수정할 수 있어요.</p>
      </div>

      <p v-if="errorMessage" class="signup-form__error text-caption">{{ errorMessage }}</p>
    </div>

    <BottomButtonBar
      primary-label="가입하기"
      :primary-disabled="!canSubmit"
      @primary-click="submit"
    />
  </div>
</template>

<style scoped>
.signup-military-page {
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

.signup-form__hint {
  margin: 0;
  color: var(--text-muted);
}

.signup-form__error {
  color: var(--danger);
  margin: 0;
}
</style>
