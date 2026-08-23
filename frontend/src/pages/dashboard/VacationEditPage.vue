<script setup>
// SCR-DASH-03 · 휴가 추가/수정  (담당: 석윤)
// 휴가 부여(grant) 등록·수정 — REGULAR(정기)는 가입 시 자동 부여라 여기 없음.
// 실제 사용내역(회차) 등록·삭제는 VacationUsagePage 소관.
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import dashboardApi from '@/api/dashboardApi';
import { useToast } from '@/composables/useToast';
import BaseInput from '@/components/common/BaseInput.vue';
import BaseModal from '@/components/common/BaseModal.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import DatePicker from '@/components/common/DatePicker.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import TabBar from '@/components/common/TabBar.vue';

// 버튼(TabBar)으로 4개 선택 - 짧은 라벨
const CATEGORY_OPTIONS = [
  { label: '포상', value: 'REWARD' },
  { label: '위로', value: 'CONSOLATION' },
  { label: '청원', value: 'PETITION' },
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

const isDeleteModalOpen = ref(false);
const openDeleteModal = () => {
  isDeleteModalOpen.value = true;
};

const deleteVacation = async () => {
  isSubmitting.value = true;
  try {
    await dashboardApi.deleteVacation(vacationId.value);
    router.back();
  } catch (error) {
    console.error(error);
    show(error.response?.data?.message ?? '휴가 삭제에 실패했습니다.', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

const submit = async () => {
  if (isBelowUsedDays.value) {
    show(
      `이미 ${usedDays.value}일 사용해서 그보다 적게는 설정할 수 없어요.`,
      'error',
    );
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
    router.back();
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
      <PageHeader
        :breadcrumb="isEditMode ? '휴가 수정' : '휴가 추가'"
        :title="isEditMode ? '휴가 정보 재정비' : '새 휴가 전력 보충'"
      />
      <button
        v-if="isEditMode"
        type="button"
        class="vacation-edit__delete-btn"
        aria-label="휴가 삭제"
        :disabled="isSubmitting"
        @click="openDeleteModal"
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
      <!-- 휴가 종류 - 버튼으로 선택 (디폴트 없음) -->
      <div class="vacation-edit__field">
        <span class="vacation-edit__label text-label">휴가 종류</span>
        <TabBar
          variant="fill"
          :model-value="category"
          :tabs="CATEGORY_OPTIONS"
          @update:model-value="category = $event"
        />
      </div>
      <BaseInput
        v-model="name"
        type="text"
        label="휴가 이름"
        placeholder="휴가 이름 입력"
      />
      <!-- 획득일 - 우리 DatePicker 컴포넌트 -->
      <DatePicker
        v-model="acquiredDate"
        label="획득일"
        placeholder="획득일 선택"
      />
      <div class="vacation-edit__field">
        <BaseInput
          v-model="days"
          type="number"
          label="휴가 일수"
          suffix="일"
          placeholder="휴가 일수 입력"
          class="vacation-edit__days"
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

    <BaseModal
      v-model="isDeleteModalOpen"
      title="휴가 삭제"
      confirm-text="삭제"
      cancel-text="취소"
      @confirm="deleteVacation"
    >
      <p class="vacation-edit__modal-message">이 휴가를 삭제하시겠습니까?</p>
    </BaseModal>
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

/* 획득일 DatePicker - 갈색 밑줄로 (기본 골드 대신) */
.vacation-edit :deep(.date-picker__trigger) {
  border-bottom-color: var(--chart-1);
}

/* 휴가 일수 입력 - 군적금 시뮬레이션 입력창처럼 값 오른쪽 정렬 + '일' 접미사 자리 확보 */
.vacation-edit__days :deep(.base-input__field) {
  text-align: right;
  padding-right: 44px;
}
/* number 입력 스피너(위아래 화살표) 숨김 */
.vacation-edit__days :deep(input[type='number'])::-webkit-inner-spin-button,
.vacation-edit__days :deep(input[type='number'])::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.vacation-edit__days :deep(input[type='number']) {
  -moz-appearance: textfield;
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
