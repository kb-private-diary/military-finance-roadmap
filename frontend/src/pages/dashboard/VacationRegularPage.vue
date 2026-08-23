<script setup>
// SCR-DASH-04 · 정기 휴가 수정  (담당: 석윤)
// 정기 휴가(REGULAR) 잔여 현황 + 사용내역(차수) 등록·삭제
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
// 더미(디자인 확인용): dashboardApi 대신 로컬 상수 사용
import { useToast } from '@/composables/useToast';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseInput from '@/components/common/BaseInput.vue';
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
  isLoading.value = true;
  loadError.value = '';
  // 더미(디자인 확인용)
  detail.value = {
    vacationId: vacationId.value ?? 1,
    category: 'REGULAR',
    name: '정기휴가',
    days: 24,
    remainingDays: 8,
    usages: [
      {
        vacationId: 101,
        name: '1차 정기휴가',
        days: 10,
        acquiredDate: '2024-12-20',
      },
      {
        vacationId: 102,
        name: '2차 정기휴가',
        days: 6,
        acquiredDate: '2025-03-15',
      },
    ],
  };
  isLoading.value = false;
};

// "+ 사용 회차 추가" 눌렀을 때만 입력 행을 보여준다.
const isAddFormOpen = ref(false);
const newUsageDays = ref('');
const isSubmitting = ref(false);

const openAddForm = () => {
  isAddFormOpen.value = true;
};

const cancelAddForm = () => {
  isAddFormOpen.value = false;
  newUsageDays.value = '';
};

const registerUsage = async () => {
  const days = Number(newUsageDays.value);
  if (!days || days <= 0) {
    show('사용 일수를 입력해주세요.', 'error');
    return;
  }
  isSubmitting.value = true;
  // 더미(디자인 확인용): 사용 등록 성공 처리
  detail.value.usages = [
    ...detail.value.usages,
    {
      vacationId: Date.now(),
      name: `${detail.value.usages.length + 1}차 정기휴가`,
      days,
      acquiredDate: new Date().toISOString().slice(0, 10),
    },
  ];
  detail.value.remainingDays = Math.max(detail.value.remainingDays - days, 0);
  isAddFormOpen.value = false;
  newUsageDays.value = '';
  isSubmitting.value = false;
};

const deleteUsage = async (usage) => {
  if (!confirm(`${usage.name}을(를) 삭제하시겠습니까?`)) {
    return;
  }
  isSubmitting.value = true;
  // 더미(디자인 확인용): 삭제 후 잔여 목록에서 제거
  detail.value.remainingDays += usage.days;
  detail.value.usages = detail.value.usages.filter(
    (u) => u.vacationId !== usage.vacationId,
  );
  isSubmitting.value = false;
};

const goPrevious = () => router.back();
const goComplete = () => router.push({ name: 'Dashboard' });

onMounted(fetchDetail);
</script>

<template>
  <div class="vacation-regular py-4">
    <h2 class="vacation-regular__title">정기 휴가 수정</h2>

    <p v-if="loadError" class="vacation-regular__error">{{ loadError }}</p>
    <p v-else-if="isLoading" class="text-caption">불러오는 중...</p>

    <template v-else-if="detail">
      <div class="vacation-regular__summary">
        <p class="vacation-regular__summary-label">정기 휴가 잔여</p>
        <p class="vacation-regular__summary-value">
          {{ detail.remainingDays }}일
        </p>
        <div class="vacation-regular__progress">
          <ProgressBar
            :value="detail.remainingDays"
            :total="detail.days"
            color="var(--military-green)"
            :height="10"
          />
        </div>
        <p class="vacation-regular__summary-caption">
          {{ usedDays }}일 사용 / 총 {{ detail.days }}일
        </p>
      </div>

      <section class="vacation-regular__usages">
        <h3 class="vacation-regular__section-title">정기휴가 사용 내역</h3>

        <BaseCard
          v-for="usage in detail.usages"
          :key="usage.vacationId"
          class="usage-card"
          padding="14px"
        >
          <div class="usage-card__row">
            <p class="usage-card__name">
              {{ usage.name }} (-{{ usage.days }}일)
            </p>
            <button
              type="button"
              class="usage-card__delete-btn"
              aria-label="사용내역 삭제"
              :disabled="isSubmitting"
              @click="deleteUsage(usage)"
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
            등록일 {{ formatDate(usage.acquiredDate) }}
          </p>
        </BaseCard>

        <div class="vacation-regular__divider" />

        <div v-if="isAddFormOpen" class="add-usage-form">
          <BaseInput
            v-model="newUsageDays"
            type="number"
            label="사용 일수"
            suffix="일"
            placeholder="일수 입력"
          />
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
  </div>
</template>

<style scoped>
.vacation-regular {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 88px;
}

.vacation-regular__title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--text-strong);
}

.vacation-regular__error {
  color: var(--danger);
}

.vacation-regular__summary {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.vacation-regular__summary-label {
  margin: 0;
  font-size: 13px;
  color: var(--text-hint);
}

.vacation-regular__summary-value {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--text-strong);
}

.vacation-regular__summary-caption {
  margin: 0;
  font-size: 12px;
  color: var(--text-hint);
  text-align: right;
}

.vacation-regular__usages {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.vacation-regular__section-title {
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

.vacation-regular__divider {
  height: 1px;
  background-color: var(--line);
}

.add-usage-form {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.add-usage-form__actions {
  display: flex;
  flex-shrink: 0;
  gap: 6px;
  padding-bottom: 4px;
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
