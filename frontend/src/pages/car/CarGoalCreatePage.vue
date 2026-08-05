<script setup>
// SCR-CAR-01 · step1)  자동차 목표 등록  (담당: 호빈)
// step1 - 예산·차종·신차/중고·거주지역·운전경력 입력
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import carApi from '@/api/carApi';
import BaseInput from '@/components/common/BaseInput.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import { toIsoDate } from '@/util/format';

const router = useRouter();

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

const form = reactive({
  isNew: true,
  budget: '',
  targetDate: '',
  region: '',
  experienceYears: null,
});

const today = toIsoDate(new Date());

const submitError = ref('');
const submitting = ref(false);

const unwrap = (response) => response.data?.data;

const readErrorMessage = (error, fallback) =>
  error.response?.data?.message ||
  error.response?.data?.error?.message ||
  error.error ||
  fallback;

const isFormValid = computed(
  () =>
    Number(form.budget) > 0 &&
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
      budget: Number(form.budget),
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
    <RoadmapCharacterSlider :progress="0" label="자동차 로드맵" />

    <h2 class="car-goal__title text-title">무엇을 준비하고 싶으신가요?</h2>

    <form class="car-form" @submit.prevent="submitGoal">
      <fieldset class="field">
        <legend class="field__label text-label">신차 / 중고</legend>
        <div class="toggle-row">
          <CategoryButton
            variant="square-yellow"
            label="신차"
            :active="form.isNew === true"
            @click="selectIsNew(true)"
          />
          <CategoryButton
            variant="square-yellow"
            label="중고"
            :active="form.isNew === false"
            @click="selectIsNew(false)"
          />
        </div>
      </fieldset>

      <BaseInput
        v-model="form.budget"
        type="amount"
        label="예산"
        suffix="만원"
        placeholder="예: 2000"
      />

      <label class="field">
        <span class="field__label text-label">목표 구매 시기</span>
        <input
          v-model="form.targetDate"
          type="date"
          class="date-input"
          :min="today"
        />
      </label>

      <BaseInput
        v-model="form.region"
        type="select"
        label="거주 지역"
        placeholder="지역 선택"
        :options="REGION_OPTIONS"
      />

      <BaseInput
        v-model="form.experienceYears"
        type="select"
        label="운전 경력"
        placeholder="운전 경력 선택"
        :options="EXPERIENCE_OPTIONS"
      />

      <p v-if="submitError" class="form-error text-caption" role="alert">
        {{ submitError }}
      </p>
    </form>

    <BottomButtonBar
      :primary-label="submitting ? '등록 중...' : '차량 추천 받기'"
      :primary-disabled="!isFormValid || submitting"
      @primary-click="submitGoal"
    />
  </div>
</template>

<style scoped>
.car-goal {
  min-height: 100%;
  padding: 18px 0 88px;
  color: var(--text-strong);
}

.car-goal :deep(.character-slider) {
  margin-bottom: 28px;
}

.car-goal__title {
  margin: 0 0 22px;
  line-height: 1.35;
}

.car-form {
  display: flex;
  flex-direction: column;
  gap: 19px;
}

.field {
  display: flex;
  min-width: 0;
  margin: 0;
  padding: 0;
  flex-direction: column;
  gap: 7px;
  border: 0;
}

.field__label {
  margin: 0;
  padding: 0;
}

.toggle-row {
  display: flex;
  gap: 8px;
}

.date-input {
  width: 100%;
  height: 40px;
  padding: 0 6px;
  border: 0;
  border-bottom: 2px solid var(--military-green);
  border-radius: 0;
  outline: none;
  background: transparent;
  color: var(--text-body);
  font-family: inherit;
  font-size: 14px;
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
