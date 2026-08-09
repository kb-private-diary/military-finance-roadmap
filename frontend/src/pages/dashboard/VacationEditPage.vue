<script setup>
// SCR-DASH-03 · 휴가 추가/수정  (담당: 석윤)
// 휴가 부여(grant) 등록·수정 — REGULAR(정기)는 가입 시 자동 부여라 여기 없음.
// 실제 사용내역(회차) 등록·삭제는 VacationUsagePage 소관.
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import dashboardApi from '@/api/dashboardApi';
import { useToast } from '@/composables/useToast';
import BaseInput from '@/components/common/BaseInput.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const CATEGORY_OPTIONS = [
  { label: '포상휴가', value: 'REWARD' },
  { label: '위로휴가', value: 'CONSOLATION' },
  { label: '청원휴가', value: 'PETITION' },
  { label: '기타', value: 'ETC' },
];

const route = useRoute();
const router = useRouter();
const { show } = useToast();

const vacationId = computed(() =>
  route.query.vacationId ? Number(route.query.vacationId) : null,
);
const isEditMode = computed(() => vacationId.value != null);

const category = ref('');
const name = ref('');
const acquiredDate = ref('');
const days = ref('');
// 수정 모드에서 이미 사용한 일수(=총 일수-잔여). 이보다 적은 일수로는 변경 못 하게 막는 하한선.
const usedDays = ref(0);

// 수정 모드는 최초 렌더부터 로딩 상태로 시작 (fetchDetail 완료 전 빈 폼이 잠깐 보이는 것 방지)
const isLoading = ref(isEditMode.value);
const loadError = ref('');
const isSubmitting = ref(false);

const fetchDetail = async () => {
  if (!isEditMode.value) {
    return;
  }
  isLoading.value = true;
  loadError.value = '';
  try {
    const detail = await dashboardApi.findVacationDetail(vacationId.value);
    category.value = detail.category;
    name.value = detail.name;
    acquiredDate.value = detail.acquiredDate ?? '';
    days.value = detail.days;
    usedDays.value = detail.days - detail.remainingDays;
  } catch (error) {
    console.error(error);
    loadError.value = '휴가 정보를 불러오지 못했습니다.';
  } finally {
    isLoading.value = false;
  }
};

// 이미 사용한 일수보다 적게는 못 바꾼다 (등록 모드에선 usedDays=0이라 항상 false)
const isBelowUsedDays = computed(() => Number(days.value) < usedDays.value);

const isFormValid = computed(
  () =>
    category.value !== '' &&
    name.value.trim() !== '' &&
    acquiredDate.value !== '' &&
    Number(days.value) > 0 &&
    !isBelowUsedDays.value,
);

const goPrevious = () => router.back();

const deleteVacation = async () => {
  if (!confirm('이 휴가를 삭제하시겠습니까?')) {
    return;
  }
  isSubmitting.value = true;
  try {
    await dashboardApi.deleteVacation(vacationId.value);
    router.push({ name: 'Dashboard' });
  } catch (error) {
    console.error(error);
    show(error.response?.data?.message ?? '휴가 삭제에 실패했습니다.', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

const submit = async () => {
  if (isBelowUsedDays.value) {
    show(`이미 ${usedDays.value}일 사용해서 그보다 적게는 설정할 수 없어요.`, 'error');
    return;
  }
  if (!isFormValid.value) {
    show('입력값을 모두 채워주세요.', 'error');
    return;
  }
  const payload = {
    category: category.value,
    name: name.value,
    acquiredDate: acquiredDate.value,
    days: Number(days.value),
  };
  isSubmitting.value = true;
  try {
    if (isEditMode.value) {
      await dashboardApi.updateVacation(vacationId.value, payload);
    } else {
      await dashboardApi.createVacation(payload);
    }
    router.push({ name: 'Dashboard' });
  } catch (error) {
    console.error(error);
    show(error.response?.data?.message ?? '휴가 저장에 실패했습니다.', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(fetchDetail);
</script>

<template>
  <div class="vacation-edit container py-4">
    <div class="vacation-edit__header">
      <h2 class="vacation-edit__title">
        {{ isEditMode ? '휴가 수정' : '휴가 추가' }}
      </h2>
      <button
        v-if="isEditMode"
        type="button"
        class="vacation-edit__delete-btn"
        aria-label="휴가 삭제"
        :disabled="isSubmitting"
        @click="deleteVacation"
      >
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>
    </div>

    <p v-if="loadError" class="vacation-edit__error">{{ loadError }}</p>
    <p v-else-if="isLoading" class="text-caption">불러오는 중...</p>

    <template v-else>
      <BaseInput
        v-model="category"
        type="select"
        label="휴가 종류"
        placeholder="선택"
        :options="CATEGORY_OPTIONS"
      />
      <BaseInput
        v-model="name"
        type="text"
        label="휴가 이름"
        placeholder="휴가 이름 입력"
      />
      <BaseInput v-model="acquiredDate" type="date" label="획득일" />
      <div class="vacation-edit__field">
        <BaseInput
          v-model="days"
          type="number"
          label="일수"
          placeholder="일수 입력"
        />
        <p
          v-if="usedDays > 0"
          class="vacation-edit__hint"
          :class="{ 'vacation-edit__hint--error': isBelowUsedDays }"
        >
          이미 {{ usedDays }}일 사용했어요{{
            isBelowUsedDays ? ' — 그보다 적게는 설정할 수 없어요.' : '.'
          }}
        </p>
      </div>
    </template>

    <BottomButtonBar
      primary-label="완료"
      secondary-label="이전"
      :primary-disabled="isSubmitting"
      @primary-click="submit"
      @secondary-click="goPrevious"
    />
  </div>
</template>

<style scoped>
.vacation-edit {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 88px;
}

.vacation-edit__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.vacation-edit__title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--text-strong);
}

.vacation-edit__delete-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: none;
  background: none;
  color: var(--danger);
  cursor: pointer;
}

.vacation-edit__delete-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.vacation-edit__delete-btn svg {
  width: 22px;
  height: 22px;
}

.vacation-edit__error {
  color: var(--danger);
}

.vacation-edit__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.vacation-edit__hint {
  margin: 0;
  color: var(--text-hint);
  font-size: 12px;
}

.vacation-edit__hint--error {
  color: var(--danger);
}
</style>
