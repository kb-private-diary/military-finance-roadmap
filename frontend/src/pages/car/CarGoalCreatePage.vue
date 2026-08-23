<script setup>
// SCR-CAR-01 · step1)  자동차 목표 등록  (담당: 호빈)
// step1 - 예산·차종·신차/중고·거주지역·운전경력 입력
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import carApi from '@/api/carApi';
import { useCarStore } from '@/stores/car';
import BaseInput from '@/components/common/BaseInput.vue';
import TabBar from '@/components/common/TabBar.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import DatePicker from '@/components/common/DatePicker.vue';
import { toIsoDate } from '@/util/format';

const router = useRouter();
const carStore = useCarStore();

const REGION_OPTIONS = [
  { value: '서울특별시', label: '서울특별시' },
  { value: '부산광역시', label: '부산광역시' },
  { value: '대구광역시', label: '대구광역시' },
  { value: '인천광역시', label: '인천광역시' },
  { value: '광주광역시', label: '광주광역시' },
  { value: '대전광역시', label: '대전광역시' },
  { value: '울산광역시', label: '울산광역시' },
  { value: '세종특별자치시', label: '세종특별자치시' },
  { value: '경기도', label: '경기도' },
  { value: '강원특별자치도', label: '강원특별자치도' },
  { value: '충청북도', label: '충청북도' },
  { value: '충청남도', label: '충청남도' },
  { value: '전북특별자치도', label: '전북특별자치도' },
  { value: '전라남도', label: '전라남도' },
  { value: '경상북도', label: '경상북도' },
  { value: '경상남도', label: '경상남도' },
  { value: '제주특별자치도', label: '제주특별자치도' },
];

// 서버는 experienceYears(정수)를 받아 3년미만/3~5년/5년이상 구간으로 환산한다.
// 5년 이상은 구간이 동일하므로 하나로 묶는다.
const EXPERIENCE_OPTIONS = [
  { value: 1, label: '1년' },
  { value: 2, label: '2년' },
  { value: 3, label: '3년' },
  { value: 4, label: '4년' },
  { value: 5, label: '5년 이상' },
];

// 스토어에 담긴 값(위저드 '가방')을 그대로 폼으로 쓴다 — "이전"으로 되돌아와도 입력값이 유지된다.
const form = carStore.draft;

const today = toIsoDate(new Date());

// 수동 예산 입력(자취 로드맵의 "반경 조정하기"와 동일한 토글 방식)
const budgetOpen = ref(false);

const submitError = ref('');
const submitting = ref(false);

const unwrap = (response) => response.data?.data;

const readErrorMessage = (error, fallback) =>
  error.response?.data?.message ||
  error.response?.data?.error?.message ||
  error.error ||
  fallback;

// 예산은 선택 입력 — 안 넣으면 군적금 만기예상액 기준으로 추천된다.
// 단, 값을 넣었다면 0보다는 커야 한다.
const isFormValid = computed(
  () =>
    (form.budget === '' || Number(form.budget) > 0) &&
    !!form.targetDate &&
    form.targetDate >= today &&
    !!form.region &&
    form.experienceYears !== null,
);

const selectIsNew = (value) => {
  form.isNew = value;
};

const submitGoal = async () => {
  if (!isFormValid.value || submitting.value) return;

  submitting.value = true;
  submitError.value = '';

  try {
    const request = {
      budget: form.budget === '' ? null : Number(form.budget),
      isNew: form.isNew,
      experienceYears: form.experienceYears,
      targetDate: form.targetDate,
      region: form.region,
    };

    const response = await carApi.createGoal(request);
    const goalId = unwrap(response)?.goalId;

    await router.push({ name: 'CarRecommend', params: { goalId } });
  } catch (error) {
    submitError.value = readErrorMessage(
      error,
      '자동차 목표를 등록하지 못했습니다.',
    );
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="car-goal">
    <RoadmapCharacterSlider :step="1" label="자동차 로드맵" />

    <PageHeader title="어떤 자동차를 타고 싶습니까?" />

    <form class="car-form" @submit.prevent="submitGoal">
      <!-- 신차/중고 + 수동예산: 한 그룹(내부 간격 좁게) -->
      <div class="field-group">
        <fieldset class="field">
          <legend class="field__label text-label">신차 / 중고</legend>
        <TabBar
          variant="fill"
          :model-value="form.isNew"
          :tabs="[
            { label: '신차', value: true },
            { label: '중고', value: false },
          ]"
          @update:model-value="selectIsNew"
        />
      </fieldset>

      <div class="field">
        <div class="row-between">
          <span class="budget-toggle">
            <button type="button" class="link-toggle" @click="budgetOpen = !budgetOpen">
              수동 예산 입력 (선택) {{ budgetOpen ? '▲' : '▼' }}
            </button>
          </span>
          <span class="field__hint-inline">
            {{ form.budget ? `${form.budget}만원` : '군적금 만기예상액 기준' }}
          </span>
        </div>
        <BaseInput
          v-if="budgetOpen"
          v-model="form.budget"
          type="amount"
          suffix="만원"
          placeholder="예산 입력"
        />
        <p v-if="budgetOpen" class="budget-help">
          군적금 만기액 기준이 아닌 원하는 한도를 입력하세요
        </p>
        </div>
      </div>

      <div class="field">
        <span class="field__label text-label">목표 구매 시기</span>
        <DatePicker
          v-model="form.targetDate"
          :min="today"
          placeholder="목표 구매 시기 선택"
        />
      </div>

      <div class="field">
        <BaseInput
          v-model="form.region"
          type="select"
          label="거주 지역"
          placeholder="지역 선택"
          :options="REGION_OPTIONS"
        />
        <p class="field__hint">전기차 구매 시 지역별 보조금 계산에 활용돼요</p>
      </div>

      <div class="field">
        <BaseInput
          v-model="form.experienceYears"
          type="select"
          label="운전 경력"
          placeholder="운전 경력 선택"
          :options="EXPERIENCE_OPTIONS"
        />
        <p class="field__hint">예상 보험료 계산에 활용돼요</p>
      </div>

      <p v-if="submitError" class="form-error text-caption" role="alert">
        {{ submitError }}
      </p>
    </form>

    <BottomButtonBar
      secondary-label="이전"
      :primary-label="submitting ? '등록 중...' : '자동차 추천받기'"
      :primary-disabled="!isFormValid || submitting"
      @secondary-click="router.push({ name: 'RoadmapMain' })"
      @primary-click="submitGoal"
    />
  </div>
</template>

<style scoped>
.car-goal {
  min-height: 100%;
  padding: 18px 0 88px;
  color: var(--text-strong);
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}
/* 제목 아래 컨텐츠 간격 통일(20px): flex gap 32 - 12 = 20 */
.car-goal :deep(.page-header) {
  margin-bottom: -12px;
}


.car-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

/* 신차/중고 + 수동예산 = 한 그룹 (내부는 좁게, 다음 필드와는 --space-8) */
.field-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.field {
  display: flex;
  min-width: 0;
  margin: 0;
  padding: 0;
  flex-direction: column;
  gap: var(--space-2);
  border: 0;
}

.field__label {
  margin: 0;
  padding: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-body);
}

.field__hint {
  margin: 2px 0 0;
  color: var(--text-hint);
  font-size: 12px;
  line-height: 1.5;
}

.row-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.link-toggle {
  align-self: flex-start;
  padding: 2px 0;
  border: 0;
  background: transparent;
  color: var(--text-muted);
  font-size: 13px;
  font-weight: 600;
  text-decoration: underline;
  cursor: pointer;
  font-family: inherit;
}

/* 수동 예산 토글 */
.budget-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

/* 수동 예산 안내 (한 줄, 회색) */
.budget-help {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: var(--text-muted);
  word-break: keep-all;
}

.field__hint-inline {
  font-size: 11px;
  color: var(--text-hint);
}

.form-error {
  margin: -3px 0 0;
  color: var(--danger);
}

.car-goal :deep(.bottom-button-bar) {
  background: var(--surface-default);
}

.car-goal :deep(.bottom-button-bar .bar-button.primary) {
  background: var(--kb-yellow);
  color: var(--text-strong);
}

.car-goal :deep(.bottom-button-bar .bar-button.primary:disabled) {
  background: var(--kb-gray-pale);
  color: var(--text-hint);
  cursor: not-allowed;
}
</style>
