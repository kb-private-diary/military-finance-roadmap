<script setup>
// SCR-TRV-01 · step1) 여행 목표 등록
import {
  computed,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
  watch,
} from 'vue';
import { useRouter } from 'vue-router';
import travelApi from '@/api/travelApi';
import CategoryButton from '@/components/common/CategoryButton.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import { toIsoDate } from '@/util/format';

const router = useRouter();

const STYLE_OPTIONS = [
  { value: 'saving', label: '알뜰' },
  { value: 'common', label: '일반' },
  { value: 'premium', label: '프리미엄' },
];

const form = reactive({
  title: '',
  departure: '',
  destinationCountry: '',
  destination: '',
  style: '',
  startDate: '',
  endDate: '',
  totalBudget: '',
});

const departureOptions = ref([]);
const cities = ref([]);
const loadingCities = ref(true);
const loadError = ref('');
const submitError = ref('');
const submitting = ref(false);
const draftGoalId = ref(null);
const initialFormSnapshot = ref(null);
const draftLoadFailed = ref(false);
let scrollContainer = null;

const today = toIsoDate(new Date());

const unwrap = (response) => response.data?.data;

const toOptions = (cities = []) =>
  cities.map(({ city }) => ({ label: city, value: city }));

const destinationCountryOptions = computed(() =>
  [...new Set(cities.value.map(({ country }) => country))]
    .filter(Boolean)
    .sort((a, b) => a.localeCompare(b, 'ko')),
);

const destinationCityOptions = computed(() =>
  toOptions(
    cities.value
      .filter(
        ({ country, city }) =>
          country === form.destinationCountry &&
          city !== form.departure,
      )
      .sort((a, b) => a.city.localeCompare(b.city, 'ko')),
  ),
);

watch(
  () => form.destinationCountry,
  (country, previousCountry) => {
    if (previousCountry && country !== previousCountry) {
      form.destination = '';
    }
  },
);

watch(
  () => form.departure,
  (departure) => {
    if (departure && departure === form.destination) {
      form.destination = '';
    }
  },
);

const readErrorMessage = (error, fallback) =>
  error.response?.data?.message ||
  error.response?.data?.error?.message ||
  error.error ||
  fallback;

const toGoalRequest = () => ({
  title: form.title.trim(),
  departure: form.departure,
  destination: form.destination,
  style: form.style,
  startDate: form.startDate,
  endDate: form.endDate,
  totalBudget: Number(form.totalBudget),
});

const toCostInputSnapshot = (request) =>
  JSON.stringify({
    departure: request.departure,
    destination: request.destination,
    style: request.style,
    startDate: request.startDate,
    endDate: request.endDate,
  });

const restoreDraft = (draft) => {
  if (!draft) return;

  const destinationCity = cities.value.find(
    ({ city }) => city === draft.destination,
  );
  draftGoalId.value = draft.goalId;
  form.title = draft.title || '';
  form.departure = draft.departure || '';
  form.destinationCountry = destinationCity?.country || '';
  form.destination = draft.destination || '';
  form.style = draft.style || '';
  form.startDate = draft.startDate || '';
  form.endDate = draft.endDate || '';
  form.totalBudget = draft.totalBudget ?? '';
  initialFormSnapshot.value = toGoalRequest();
};

onMounted(async () => {
  scrollContainer = document.querySelector('.app-content');
  scrollContainer?.classList.add('travel-scrollbar-hidden');

  try {
    const [citiesResult, draftResult] = await Promise.allSettled([
      travelApi.findCities(),
      travelApi.findCurrentGoal(),
    ]);
    if (citiesResult.status === 'rejected') {
      throw citiesResult.reason;
    }

    cities.value = unwrap(citiesResult.value) || [];

    departureOptions.value = toOptions(
      cities.value.filter(({ country }) => country === '대한민국'),
    );

    if (!departureOptions.value.length || !cities.value.length) {
      throw new Error('도시 데이터가 비어 있습니다.');
    }

    if (draftResult.status === 'fulfilled') {
      restoreDraft(unwrap(draftResult.value));
    } else {
      draftLoadFailed.value = true;
      loadError.value = readErrorMessage(
        draftResult.reason,
        '작성 중인 여행 목표를 확인하지 못했습니다. 잠시 후 다시 시도해주세요.',
      );
    }
  } catch (error) {
    loadError.value = readErrorMessage(
      error,
      '도시 목록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.',
    );
  } finally {
    loadingCities.value = false;
  }
});

onBeforeUnmount(() => {
  scrollContainer?.classList.remove('travel-scrollbar-hidden');
});

const isFormValid = computed(
  () =>
    form.title.trim() &&
    form.departure &&
    form.destinationCountry &&
    form.destination &&
    form.departure !== form.destination &&
    form.style &&
    form.startDate &&
    form.startDate >= today &&
    form.endDate &&
    form.endDate >= today &&
    form.endDate >= form.startDate &&
    Number(form.totalBudget) > 0 &&
    !draftLoadFailed.value,
);

const selectStyle = (style) => {
  form.style = style;
};

const submitGoal = async () => {
  if (!isFormValid.value || submitting.value) return;

  submitting.value = true;
  submitError.value = '';

  try {
    const request = toGoalRequest();
    let goalId = draftGoalId.value;
    let recalculate = false;

    if (goalId) {
      const changed =
        JSON.stringify(request) !==
        JSON.stringify(initialFormSnapshot.value);
      if (changed) {
        recalculate =
          toCostInputSnapshot(request) !==
          toCostInputSnapshot(initialFormSnapshot.value);
        await travelApi.updateGoal(goalId, request);
        initialFormSnapshot.value = request;
      }
    } else {
      const response = await travelApi.createGoal(request);
      goalId = unwrap(response);
    }

    await router.push({
      name: 'TravelCost',
      params: { goalId },
      query: recalculate ? { recalculate: 'true' } : undefined,
    });
  } catch (error) {
    submitError.value = readErrorMessage(
      error,
      '여행 목표를 등록하지 못했습니다.',
    );
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="travel-goal">
    <section class="roadmap-step" aria-label="여행 로드맵 1단계">
      <p class="roadmap-step__label text-overline">여행 로드맵</p>
      <div class="roadmap-step__progress">
        <span class="roadmap-step__number">1</span>
        <span class="roadmap-step__line">
          <span class="roadmap-step__line-fill" />
        </span>
      </div>
    </section>

    <h2 class="travel-goal__title text-title">어디로 떠나고 싶습니까?</h2>

    <form class="travel-form" @submit.prevent="submitGoal">
      <label class="field">
        <span class="field__label text-label">여행명</span>
        <input
          v-model="form.title"
          class="field__control field__control--box"
          type="text"
          placeholder="졸업 여행, 전역 여행 ..."
        />
      </label>

      <label class="field">
        <span class="field__label text-label">출발지</span>
        <span class="select-wrap">
          <select
            v-model="form.departure"
            class="field__control field__control--line"
            :disabled="loadingCities"
          >
            <option value="" disabled>
              {{ loadingCities ? '불러오는 중...' : '선택' }}
            </option>
            <option
              v-for="option in departureOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
        </span>
      </label>

      <fieldset class="field destination-field">
        <legend class="field__label text-label">도착지</legend>
        <div class="destination-field__row">
          <label class="destination-select">
            <span class="sr-only">도착 국가</span>
            <span class="select-wrap">
              <select
                v-model="form.destinationCountry"
                class="field__control field__control--line"
                :disabled="loadingCities"
              >
                <option value="" disabled>
                  {{ loadingCities ? '불러오는 중...' : '국가 선택' }}
                </option>
                <option
                  v-for="country in destinationCountryOptions"
                  :key="country"
                  :value="country"
                >
                  {{ country }}
                </option>
              </select>
            </span>
          </label>

          <label class="destination-select">
            <span class="sr-only">도착 도시</span>
            <span class="select-wrap">
              <select
                v-model="form.destination"
                class="field__control field__control--line"
                :disabled="loadingCities || !form.destinationCountry"
              >
                <option value="" disabled>
                  {{ form.destinationCountry ? '도시 선택' : '국가 먼저 선택' }}
                </option>
                <option
                  v-for="option in destinationCityOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
            </span>
          </label>
        </div>
      </fieldset>

      <fieldset class="style-field">
        <legend class="field__label text-label">여행 스타일</legend>
        <div class="style-field__buttons">
          <CategoryButton
            v-for="option in STYLE_OPTIONS"
            :key="option.value"
            variant="oval-green"
            :label="option.label"
            :active="form.style === option.value"
            @click="selectStyle(option.value)"
          />
        </div>
      </fieldset>

      <fieldset class="date-field">
        <legend class="field__label text-label">여행일정</legend>
        <div class="date-field__row">
          <label class="date-control">
            <span class="sr-only">출발일</span>
            <input
              v-model="form.startDate"
              type="date"
              :min="today"
              :max="form.endDate || undefined"
            />
          </label>
          <span class="date-field__separator">~</span>
          <label class="date-control">
            <span class="sr-only">도착일</span>
            <input
              v-model="form.endDate"
              type="date"
              :min="form.startDate || today"
            />
          </label>
        </div>
      </fieldset>

      <label class="field">
        <span class="field__label text-label">여행예산</span>
        <input
          v-model="form.totalBudget"
          class="field__control field__control--box"
          type="number"
          min="1"
          inputmode="numeric"
          placeholder="총 여행 예산"
        />
      </label>

      <p
        v-if="loadError || submitError"
        class="form-error text-caption"
        role="alert"
      >
        {{ submitError || loadError }}
      </p>
    </form>

    <BottomButtonBar
      :primary-label="submitting ? '등록 중...' : '여행 로드맵 추천 받기'"
      :primary-disabled="!isFormValid || submitting"
      @primary-click="submitGoal"
    />
  </div>
</template>

<style scoped>
.travel-goal {
  min-height: 100%;
  padding: 18px 0 88px;
  color: var(--text-strong);
}

.roadmap-step {
  margin-bottom: 34px;
}

.roadmap-step__label {
  margin: 0 0 15px;
}

.roadmap-step__progress {
  position: relative;
  display: flex;
  align-items: center;
  height: 22px;
}

.roadmap-step__number {
  position: relative;
  z-index: 2;
  display: grid;
  width: 22px;
  height: 22px;
  place-items: center;
  border: 2px solid var(--travel-primary);
  border-radius: 50%;
  background: var(--surface-default);
  color: var(--travel-primary-dark);
  font-size: 12px;
  font-weight: 700;
}

.roadmap-step__line {
  position: absolute;
  right: 10px;
  left: 10px;
  height: 4px;
  overflow: hidden;
  background: var(--line);
}

.roadmap-step__line-fill {
  display: block;
  width: 0;
  height: 100%;
  background: var(--travel-primary);
}

.travel-goal__title {
  margin: 0 0 22px;
  line-height: 1.35;
}

.travel-form {
  display: flex;
  flex-direction: column;
  gap: 19px;
}

.field,
.style-field,
.date-field {
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

.field__control {
  width: 100%;
  height: 40px;
  outline: none;
  font-family: inherit;
  font-size: 13px;
}

.field__control--box {
  padding: 0 14px;
  border: 0;
  background: var(--surface-subtle);
  color: var(--text-body);
}

.field__control::placeholder {
  color: var(--text-disabled);
}

.select-wrap {
  position: relative;
}

.select-wrap::after {
  position: absolute;
  top: 50%;
  right: 8px;
  width: 7px;
  height: 7px;
  border-right: 1px solid var(--text-disabled);
  border-bottom: 1px solid var(--text-disabled);
  content: '';
  pointer-events: none;
  transform: translateY(-70%) rotate(45deg);
}

.field__control--line {
  padding: 0 30px 0 12px;
  appearance: none;
  border: 0;
  border-bottom: 2px solid var(--travel-accent-line);
  border-radius: 0;
  background: transparent;
  color: var(--text-muted);
  text-align: center;
  text-align-last: center;
}

.style-field__buttons {
  display: flex;
  gap: 8px;
}

.destination-field__row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 14px;
}

.destination-select,
.destination-select .select-wrap {
  display: block;
  min-width: 0;
}

.destination-select .field__control {
  min-width: 0;
  padding-right: 24px;
  padding-left: 4px;
  font-size: 12px;
}

.destination-select .select-wrap::after {
  right: 5px;
}

.style-field__buttons :deep(.category-btn) {
  min-height: 36px;
  padding: 7px 10px;
  font-size: 12px;
}

.date-field__row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 18px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
}

.date-control input {
  width: 100%;
  height: 40px;
  padding: 0 6px;
  border: 0;
  border-bottom: 2px solid var(--travel-accent-line);
  border-radius: 0;
  outline: none;
  background: transparent;
  color: var(--text-hint);
  font-family: inherit;
  font-size: 11px;
}

.date-field__separator {
  color: var(--text-muted);
  font-size: 12px;
  text-align: center;
}

.form-error {
  margin: -3px 0 0;
  color: var(--danger);
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

:global(.app-content.travel-scrollbar-hidden) {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

:global(.app-content.travel-scrollbar-hidden::-webkit-scrollbar) {
  display: none;
  width: 0;
  height: 0;
}

.travel-goal :deep(.bottom-button-bar) {
  background: var(--surface-default);
}

.travel-goal :deep(.bottom-button-bar .bar-button.primary) {
  background: var(--kb-yellow);
  color: var(--text-strong);
}

.travel-goal :deep(.bottom-button-bar .bar-button.primary:disabled) {
  background: var(--kb-gray-pale);
  color: var(--text-hint);
  cursor: not-allowed;
}
</style>
