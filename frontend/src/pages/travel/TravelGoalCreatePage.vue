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
import BaseInput from '@/components/common/BaseInput.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import CascaderSelect from '@/components/common/CascaderSelect.vue';
import DateRangePicker from '@/components/common/DateRangePicker.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import { toIsoDate } from '@/util/format';
import {
  DOMESTIC_COUNTRY,
  DOMESTIC_REGIONS,
  findDomesticRegion,
} from './domesticRegions';

const router = useRouter();

const form = reactive({
  title: '',
  departureGroup: '',
  departure: '',
  destinationScope: '',
  destinationGroup: '',
  destination: '',
  style: 'common',
  startDate: '',
  endDate: '',
  totalBudget: '',
});

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

const toDomesticGroupValue = (region) => `domestic:${region}`;
const toCountryGroupValue = (country) => `country:${country}`;
const DOMESTIC_SCOPE = 'scope:domestic';
const FOREIGN_SCOPE = 'scope:foreign';

const destinationScopeOptions = [
  { label: '국내', value: DOMESTIC_SCOPE },
  { label: '해외', value: FOREIGN_SCOPE },
];

const domesticRegionOptions = computed(() => {
  const availableDomesticCities = new Set(
    cities.value
      .filter(({ country }) => country === DOMESTIC_COUNTRY)
      .map(({ city }) => city),
  );

  return Object.entries(DOMESTIC_REGIONS)
    .filter(([, regionCities]) =>
      regionCities.some((city) => availableDomesticCities.has(city)),
    )
    .map(([region]) => ({
      label: region,
      value: toDomesticGroupValue(region),
    }));
});

const foreignCountryOptions = computed(() =>
  [
    ...new Set(
      cities.value
        .map(({ country }) => country)
        .filter((country) => country && country !== DOMESTIC_COUNTRY),
    ),
  ]
    .sort((a, b) => a.localeCompare(b, 'ko'))
    .map((country) => ({
      label: country,
      value: toCountryGroupValue(country),
    })),
);

const destinationGroupOptions = computed(() => [
  ...domesticRegionOptions.value,
  ...foreignCountryOptions.value,
]);

const destinationGroupsByScope = computed(() => ({
  [DOMESTIC_SCOPE]: domesticRegionOptions.value,
  [FOREIGN_SCOPE]: foreignCountryOptions.value,
}));

const toOptionsByGroup = (groups, excludedCity = '') =>
  Object.fromEntries(
    groups.map(({ value }) => [
      value,
      findCitiesByGroup(value, excludedCity),
    ]),
  );

const findCitiesByGroup = (group, excludedCity = '') => {
  const [groupType, groupName] = group.split(':');
  const domesticCities = DOMESTIC_REGIONS[groupName] || [];

  return toOptions(
    cities.value
      .filter(({ country, city }) => {
        if (city === excludedCity) return false;
        if (groupType === 'domestic') {
          return (
            country === DOMESTIC_COUNTRY && domesticCities.includes(city)
          );
        }
        return groupType === 'country' && country === groupName;
      })
      .sort((a, b) => a.city.localeCompare(b.city, 'ko')),
  );
};

const departureOptionsByGroup = computed(() =>
  toOptionsByGroup(domesticRegionOptions.value),
);

const destinationOptionsByGroup = computed(() =>
  toOptionsByGroup(destinationGroupOptions.value, form.departure),
);

const departureLocation = computed({
  get: () => [form.departureGroup, form.departure],
  set: ([group = '', city = ''] = []) => {
    form.departureGroup = group;
    form.departure = city;
  },
});

const destinationLocation = computed({
  get: () => [
    form.destinationScope,
    form.destinationGroup,
    form.destination,
  ],
  set: ([scope = '', group = '', city = ''] = []) => {
    form.destinationScope = scope;
    form.destinationGroup = group;
    form.destination = city;
  },
});

const travelDates = computed({
  get: () => [form.startDate, form.endDate],
  set: ([startDate = '', endDate = ''] = []) => {
    form.startDate = startDate;
    form.endDate = endDate;
  },
});

watch(
  () => form.departureGroup,
  (group, previousGroup) => {
    if (previousGroup && group !== previousGroup) {
      form.departure = '';
    }
  },
);

watch(
  () => form.destinationGroup,
  (group, previousGroup) => {
    if (previousGroup && group !== previousGroup) {
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

  const departureCity = cities.value.find(
    ({ city }) => city === draft.departure,
  );
  const departureRegion = findDomesticRegion(departureCity?.city);
  const destinationCity = cities.value.find(
    ({ city }) => city === draft.destination,
  );
  const destinationRegion =
    destinationCity?.country === DOMESTIC_COUNTRY
      ? findDomesticRegion(destinationCity.city)
      : '';
  draftGoalId.value = draft.goalId;
  form.title = draft.title || '';
  form.departureGroup = departureRegion
    ? toDomesticGroupValue(departureRegion)
    : '';
  form.departure = draft.departure || '';
  form.destinationScope = destinationRegion
    ? DOMESTIC_SCOPE
    : destinationCity?.country
      ? FOREIGN_SCOPE
      : '';
  form.destinationGroup = destinationRegion
    ? toDomesticGroupValue(destinationRegion)
    : destinationCity?.country
      ? toCountryGroupValue(destinationCity.country)
      : '';
  form.destination = draft.destination || '';
  form.style = draft.style || 'common';
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

    if (!domesticRegionOptions.value.length || !cities.value.length) {
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
    form.departureGroup &&
    form.departure &&
    form.destinationGroup &&
    form.destination &&
    form.departure !== form.destination &&
    form.startDate &&
    form.startDate >= today &&
    form.endDate &&
    form.endDate >= today &&
    form.endDate >= form.startDate &&
    Number(form.totalBudget) > 0 &&
    !draftLoadFailed.value,
);

const submitGoal = async () => {
  if (!isFormValid.value || submitting.value) return;

  submitting.value = true;
  submitError.value = '';

  try {
    const request = toGoalRequest();
    let goalId = draftGoalId.value;
    let recalculate = false;
    let destinationChanged = false;

    if (goalId) {
      const changed =
        JSON.stringify(request) !==
        JSON.stringify(initialFormSnapshot.value);
      if (changed) {
        destinationChanged =
          request.destination !== initialFormSnapshot.value.destination;
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

    if (destinationChanged) {
      travelApi.clearPlaceCache(goalId);
    }
    void travelApi.prefetchPlaces(goalId);

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
    <RoadmapCharacterSlider :step="1" label="여행 로드맵" />

    <h2 class="travel-goal__title text-title">어디로 떠나고 싶습니까?</h2>

    <form class="travel-form" @submit.prevent="submitGoal">
      <BaseInput
        v-model="form.title"
        label="여행명"
        type="text"
        placeholder="졸업 여행, 전역 여행 ..."
      />

      <CascaderSelect
        v-model="departureLocation"
        label="출발지"
        :groups="domesticRegionOptions"
        :children-by-group="departureOptionsByGroup"
        :placeholder="loadingCities ? '불러오는 중...' : '출발지 선택'"
        group-empty-text="왼쪽에서 권역을 선택해주세요."
        child-empty-text="선택 가능한 출발지가 없습니다."
        :disabled="loadingCities"
      />

      <CascaderSelect
        v-model="destinationLocation"
        label="도착지"
        :groups="destinationScopeOptions"
        :middle-by-group="destinationGroupsByScope"
        :children-by-group="destinationOptionsByGroup"
        :placeholder="loadingCities ? '불러오는 중...' : '도착지 선택'"
        group-empty-text="왼쪽에서 국내 또는 해외를 선택해주세요."
        middle-empty-text="가운데에서 권역 또는 국가를 선택해주세요."
        child-empty-text="선택 가능한 도착지가 없습니다."
        :disabled="loadingCities"
      />

      <DateRangePicker
        v-model="travelDates"
        label="여행일정"
        :min="today"
      />

      <BaseInput
        v-model="form.totalBudget"
        label="여행예산"
        type="amount"
        placeholder="총 여행 예산"
      />

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

.travel-goal :deep(.character-slider) {
  margin-bottom: 28px;
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

.form-error {
  margin: -3px 0 0;
  color: var(--danger);
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
