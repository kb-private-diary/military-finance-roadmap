<script setup>
// SCR-COM-05 · 회원가입 - 군정보 (담당: 호빈)
// 회원가입 2단계 - 군종·부대·입대일·전역일 입력 → 계정 생성 (계급은 입대일 기준으로 서버가 자동 산정)
// 약관동의는 1단계(SignupInfoPage)에서 이미 받았으므로 여기서는 다루지 않는다.
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import memberApi from '@/api/memberApi';
import { useSignupStore } from '@/stores/signup';
import BaseInput from '@/components/common/BaseInput.vue';
import SignupStepHeader from '@/components/common/SignupStepHeader.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();
const signupStore = useSignupStore();

// 계급은 입력받지 않는다 — 입대일 기준으로 서버 스케줄러(RankPromotionScheduler)가
// military_rank.service_months 구간에 맞춰 자동으로 진급시켜준다 (수동 선택은 어차피 다음 배치 때 덮어써짐).

// 복무기간(월). 군종 API 응답엔 없는 값이라 별도 유지 (병역 정책 문서 기준: 육군·해병대 18 / 해군 20 / 공군·공익 21 / 기타 24)
const SERVICE_MONTHS_BY_TYPE_ID = { 1: 18, 2: 20, 3: 21, 4: 18, 5: 21, 6: 24 };

const typeOptions = ref([]);
const loadMilitaryTypes = async () => {
  const types = await memberApi.findMilitaryTypes();
  typeOptions.value = types.map((t) => ({ label: t.typeName, value: t.typeId }));
};

// 부대명은 군종 선택에 따라 목록이 바뀌는 종속 드롭다운 (부대코드는 화면에 안 보여주고 내부에서만 같이 들고 있는다)
const unitOptions = ref([]);
const loadMilitaryUnits = async (typeId) => {
  form.unitName = '';
  form.unitCode = '';
  unitOptions.value = [];
  if (!typeId) return;
  const units = await memberApi.findMilitaryUnits(typeId);
  unitOptions.value = units.map((u) => ({ label: u.unitName, value: u.unitCode }));
};
const form = reactive({
  typeId: null,
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
    form.unitCode &&
    form.enlistDate &&
    form.dischargeDate &&
    !submitting.value,
);

// 군종이 바뀌면 부대 목록을 다시 불러온다 (해군 선택 시 해군 부대만, 육군 선택 시 육군 부대만)
watch(
  () => form.typeId,
  (typeId) => loadMilitaryUnits(typeId),
);

// 부대명은 화면에 안 보이지만 선택된 부대코드에서 그대로 파생시켜 같이 제출한다
watch(
  () => form.unitCode,
  (unitCode) => {
    const unit = unitOptions.value.find((u) => u.value === unitCode);
    form.unitName = unit ? unit.label : '';
  },
);

// 군종 + 입대일이 정해지면 복무기간만큼 더한 전역예정일을 자동으로 채워준다 (수동으로 다시 고칠 수도 있음)
// 입대일 당일도 복무 1일차로 치므로, 개월수를 더한 날짜에서 하루를 뺀다 (예: 3/1 입대 + 18개월 → 8/31 전역)
watch(
  () => [form.typeId, form.enlistDate],
  ([typeId, enlistDate]) => {
    if (!typeId || !enlistDate) return;
    const months = SERVICE_MONTHS_BY_TYPE_ID[typeId];
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
    return;
  }
  loadMilitaryTypes();
});
</script>

<template>
  <div class="signup-military-page">
    <SignupStepHeader eyebrow="회원가입" step="3 / 3" title="추가정보 입력" />

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
        v-model="form.unitCode"
        label="부대명"
        :placeholder="form.typeId ? '부대를 선택하세요' : '군종을 먼저 선택하세요'"
        :options="unitOptions"
      />

      <BaseInput v-model="form.enlistDate" type="date" label="입대일" />
      <div class="signup-form__field">
        <BaseInput v-model="form.dischargeDate" type="date" label="전역예정일" />
        <p class="text-caption signup-form__hint">군종·입대일을 입력하면 자동으로 계산돼요. 필요하면 직접 수정할 수 있어요.</p>
      </div>

      <p v-if="errorMessage" class="signup-form__error text-caption">{{ errorMessage }}</p>
    </div>

    <BottomButtonBar
      secondary-label="이전"
      primary-label="가입하기"
      :primary-disabled="!canSubmit"
      @secondary-click="router.push({ name: 'SignupInfo' })"
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
