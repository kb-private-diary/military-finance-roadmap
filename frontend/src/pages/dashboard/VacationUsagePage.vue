<script setup>
// SCR-DASH-04 · 휴가 사용내역 관리 (담당: 석윤)
// 휴가 부여(vacationId) 1건의 잔여 현황 + 사용내역(회차) 등록·삭제.
// REGULAR 전용이 아니라 카테고리 상관없이 공통으로 쓰인다.
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import dashboardApi from '@/api/dashboardApi';
import { useToast } from '@/composables/useToast';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import BaseModal from '@/components/common/BaseModal.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import ProgressBar from '@/components/common/ProgressBar.vue';
import { formatDate } from '@/util/format';

const route = useRoute();
const router = useRouter();
const { show } = useToast();

const vacationId = computed(() =>
  route.query.vacationId ? Number(route.query.vacationId) : null,
);

const detail = ref(null);
const isLoading = ref(true);
const loadError = ref('');

const usedDays = computed(() =>
  detail.value ? detail.value.days - detail.value.remainingDays : 0,
);

const fetchDetail = async () => {
  if (!vacationId.value) {
    loadError.value = '휴가 정보를 찾을 수 없습니다.';
    isLoading.value = false;
    return;
  }
  isLoading.value = true;
  loadError.value = '';
  try {
    detail.value = await dashboardApi.findVacationDetail(vacationId.value);
  } catch (error) {
    console.error(error);
    loadError.value = '휴가 정보를 불러오지 못했습니다.';
  } finally {
    isLoading.value = false;
  }
};

const today = () => new Date().toISOString().slice(0, 10);

// "+ 사용 회차 추가" 눌렀을 때만 입력 행을 보여준다.
const isAddFormOpen = ref(false);
const newUsageDate = ref('');
const newUsageDays = ref('');
const isSubmitting = ref(false);

const openAddForm = () => {
  newUsageDate.value = today();
  isAddFormOpen.value = true;
};

const cancelAddForm = () => {
  isAddFormOpen.value = false;
  newUsageDate.value = '';
  newUsageDays.value = '';
};

// 잔여일수보다 많은 일수를 입력했는지 (입력 안 했으면 항상 false)
const isOverRemaining = computed(() => {
  const days = Number(newUsageDays.value);
  return detail.value && days > 0 && days > detail.value.remainingDays;
});

// 사용일이 휴가 획득일보다 이전인지 (미래 날짜는 허용 — 미리 예약 등록 가능)
const isBeforeAcquired = computed(
  () =>
    detail.value &&
    newUsageDate.value &&
    newUsageDate.value < detail.value.acquiredDate,
);

// 등록 버튼 아래에 보여줄 에러 문구 (해당되는 게 없으면 null)
const usageFormError = computed(() => {
  if (isBeforeAcquired.value) {
    return `획득일(${detail.value.acquiredDate}) 이후 날짜만 등록할 수 있어요.`;
  }
  if (isOverRemaining.value) {
    return `잔여 ${detail.value.remainingDays}일보다 많이 등록할 수 없어요.`;
  }
  return null;
});

const registerUsage = async () => {
  const days = Number(newUsageDays.value);
  if (!newUsageDate.value || !days || days <= 0) {
    show('사용일과 사용 일수를 입력해주세요.', 'error');
    return;
  }
  if (usageFormError.value) {
    show(usageFormError.value, 'error');
    return;
  }
  isSubmitting.value = true;
  try {
    await dashboardApi.createVacationUsage(vacationId.value, {
      usedDate: newUsageDate.value,
      days,
    });
    cancelAddForm();
    await fetchDetail();
  } catch (error) {
    console.error(error);
    show(error.response?.data?.message ?? '사용 등록에 실패했습니다.', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

// 삭제 확인 모달이 어떤 사용내역을 대상으로 열렸는지 기억해둔다 (모달 자체는 화면에 하나뿐).
const isDeleteModalOpen = ref(false);
const usageToDelete = ref(null);

const openDeleteModal = (usage) => {
  usageToDelete.value = usage;
  isDeleteModalOpen.value = true;
};

const deleteUsage = async () => {
  if (!usageToDelete.value) {
    return;
  }
  isSubmitting.value = true;
  try {
    await dashboardApi.deleteVacationUsage(
      vacationId.value,
      usageToDelete.value.historyId,
    );
    await fetchDetail();
  } catch (error) {
    console.error(error);
    show(
      error.response?.data?.message ?? '사용내역 삭제에 실패했습니다.',
      'error',
    );
  } finally {
    isSubmitting.value = false;
    usageToDelete.value = null;
  }
};

const goPrevious = () => router.back();
const goComplete = () => router.push({ name: 'Dashboard' });

onMounted(fetchDetail);
</script>

<template>
  <div class="vacation-usage container py-4">
    <h2 class="vacation-usage__title">{{ detail?.name ?? '휴가' }} 사용내역</h2>

    <p v-if="loadError" class="vacation-usage__error">{{ loadError }}</p>
    <p v-else-if="isLoading" class="text-caption">불러오는 중...</p>

    <template v-else-if="detail">
      <div class="vacation-usage__summary">
        <p class="vacation-usage__summary-label">잔여</p>
        <p class="vacation-usage__summary-value">
          {{ detail.remainingDays }}일
        </p>
        <div class="vacation-usage__progress">
          <ProgressBar
            :value="detail.remainingDays"
            :total="detail.days"
            color="var(--military-green)"
            :height="10"
          />
        </div>
        <p class="vacation-usage__summary-caption">
          {{ usedDays }}일 사용 / 총 {{ detail.days }}일
        </p>
        <p class="vacation-usage__summary-caption">
          획득일 {{ formatDate(detail.acquiredDate) }}
        </p>
      </div>

      <section class="vacation-usage__usages">
        <h3 class="vacation-usage__section-title">사용 내역</h3>

        <BaseCard
          v-for="(usage, index) in detail.usages"
          :key="usage.historyId"
          class="usage-card"
          padding="14px"
        >
          <div class="usage-card__row">
            <p class="usage-card__name">
              {{ index + 1 }}차 사용 (-{{ usage.days }}일)
            </p>
            <button
              type="button"
              class="usage-card__delete-btn"
              aria-label="사용내역 삭제"
              :disabled="isSubmitting"
              @click="openDeleteModal(usage)"
            >
              <svg
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
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
          <p class="usage-card__date">
            사용일 {{ formatDate(usage.usedDate) }}
          </p>
        </BaseCard>

        <div class="vacation-usage__divider" />

        <div v-if="isAddFormOpen" class="add-usage-form">
          <div class="add-usage-form__fields">
            <BaseInput v-model="newUsageDate" type="date" label="사용일" />
            <BaseInput
              v-model="newUsageDays"
              type="number"
              label="사용 일수"
              suffix="일"
              placeholder="일수 입력"
            />
          </div>
          <div class="add-usage-form__footer">
            <p v-if="usageFormError" class="add-usage-form__hint">
              {{ usageFormError }}
            </p>
            <div class="add-usage-form__actions">
              <button
                type="button"
                class="add-usage-form__btn add-usage-form__btn--confirm"
                :disabled="isSubmitting"
                @click="registerUsage"
              >
                등록
              </button>
              <button
                type="button"
                class="add-usage-form__btn add-usage-form__btn--cancel"
                :disabled="isSubmitting"
                @click="cancelAddForm"
              >
                취소
              </button>
            </div>
          </div>
        </div>
        <button
          v-else
          type="button"
          class="add-usage-trigger"
          @click="openAddForm"
        >
          + 사용 회차 추가
        </button>
      </section>
    </template>

    <BottomButtonBar
      primary-label="완료"
      secondary-label="이전"
      @primary-click="goComplete"
      @secondary-click="goPrevious"
    />

    <BaseModal
      v-model="isDeleteModalOpen"
      title="사용내역 삭제"
      confirm-text="삭제"
      cancel-text="취소"
      @confirm="deleteUsage"
    >
      <p class="vacation-usage__modal-message">
        이 사용내역을 삭제하시겠습니까?
      </p>
    </BaseModal>
  </div>
</template>

<style scoped>
.vacation-usage {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 88px;
}

.vacation-usage__title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--text-strong);
}

.vacation-usage__error {
  color: var(--danger);
}

.vacation-usage__summary {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.vacation-usage__summary-label {
  margin: 0;
  font-size: 13px;
  color: var(--text-hint);
}

.vacation-usage__summary-value {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--text-strong);
}

.vacation-usage__summary-caption {
  margin: 0;
  font-size: 12px;
  color: var(--text-hint);
  text-align: right;
}

.vacation-usage__usages {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.vacation-usage__section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}

.usage-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.usage-card__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.usage-card__name {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}

.usage-card__date {
  margin: 0;
  font-size: 12px;
  color: var(--text-hint);
}

.usage-card__delete-btn {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: none;
  background: none;
  color: var(--danger);
  cursor: pointer;
}

.usage-card__delete-btn svg {
  width: 20px;
  height: 20px;
}

.usage-card__delete-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.vacation-usage__divider {
  height: 1px;
  background-color: var(--line);
}

.add-usage-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.add-usage-form__fields {
  display: flex;
  gap: 8px;
}

.add-usage-form__footer {
  display: flex;
  align-items: center;
  gap: 8px;
}

.add-usage-form__hint {
  flex: 1;
  margin: 0;
  color: var(--danger);
  font-size: 12px;
}

.add-usage-form__actions {
  display: flex;
  flex-shrink: 0;
  gap: 6px;
  margin-left: auto;
}

.add-usage-form__btn {
  padding: 10px 14px;
  border: none;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.add-usage-form__btn--confirm {
  background-color: var(--brown-active-bg);
  color: var(--surface-default);
}

.add-usage-form__btn--cancel {
  background-color: var(--gray-pale-bg);
  color: var(--gray-mid);
}

.add-usage-form__btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.add-usage-trigger {
  padding: 12px;
  border: 1px dashed var(--line-strong);
  border-radius: 12px;
  background: none;
  color: var(--text-body);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
</style>
