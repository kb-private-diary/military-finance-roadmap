<script setup>
// SCR-JOB-01 · step1) 진로 목표 등록  (담당: 지원)
// step1 - 목표 유형 (취업/공무원/편입)·희망 직무·시기 입력
import { ref, computed, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import BaseInput from '@/components/common/BaseInput.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import jobApi from '@/api/jobApi';

const router = useRouter();

// — 진행바 (step1/4)
const currentStep = 1;
const progress = computed(() => (currentStep / 4) * 100);

// ── goalType 토글 (취업/공무원/편입) ──
const GOAL_TYPES = [
  { value: 'J01', label: '취업' },
  { value: 'J02', label: '공무원' },
  { value: 'J03', label: '편입' },
];
const goalType = ref('J01');

const jobCodeLabel = computed(() => {
  if (goalType.value === 'J01') return '희망 직무';
  if (goalType.value === 'J02') return '희망 직렬';
  return '희망 학과';
});

const dateFieldLabel = computed(() => {
  if (goalType.value === 'J01') return '목표 취업 시기';
  if (goalType.value === 'J02') return '목표 합격 시기';
  return '목표 편입 시기';
});

// ── 직무·직렬·학과 드롭다운 ──
const jobCodeOptions = ref([]);
const jobCodeId = ref('');

const loadJobCodes = async () => {
  jobCodeId.value = '';
  const jobCodes = await jobApi.findJobCodes(goalType.value);
  jobCodeOptions.value = jobCodes.map((code) => ({
    value: code.jobCodeId,
    label: code.codeName,
  }));
};

watch(goalType, loadJobCodes);
onMounted(loadJobCodes);

// ── 목표 시기 (년 select + 월 select 조합) ──
const expectedYear = ref('');
const expectedMonth = ref('');

// 연도 옵션: 올해 ~ 5년 후
const currentYear = new Date().getFullYear();
const yearOptions = Array.from({ length: 6 }, (_, i) => {
  const year = currentYear + i;
  return { value: year, label: `${year}년` };
});

// 월 옵션: 1~12월
const monthOptions = Array.from({ length: 12 }, (_, i) => {
  const month = i + 1;
  return { value: month, label: `${month}월` };
});

// job_goal.expected_date는 단일 컬럼이라 "YYYY-MM" 형태로 조합해서 저장
const expectedDate = computed(() => {
  if (!expectedYear.value || !expectedMonth.value) return '';
  return `${expectedYear.value}-${String(expectedMonth.value).padStart(2, '0')}`;
});

// ── 준비항목 (복수 선택) ──
const PREP_ITEMS = [
  { value: 'P01', label: '자격증·어학', desc: '국가기술자격 + 토익/토스/오픽' },
  { value: 'P02', label: '인터넷 강의', desc: '' },
  {
    value: 'P03',
    label: '정부지원 훈련과정',
    desc: '국민내일배움카드 기반, 자기부담금 기준',
    onlyFor: 'J01',
  },
];
const itemTypes = ref([]);

watch(goalType, (newType) => {
  if (newType !== 'J01') {
    itemTypes.value = itemTypes.value.filter((t) => t !== 'P03');
  }
});

const visiblePrepItems = computed(() =>
  PREP_ITEMS.filter((item) => !item.onlyFor || item.onlyFor === goalType.value),
);

const toggleItemType = (value) => {
  if (itemTypes.value.includes(value)) {
    itemTypes.value = itemTypes.value.filter((v) => v !== value);
  } else {
    itemTypes.value = [...itemTypes.value, value];
  }
};

// ── 제출 가능 여부 ──
const isFormValid = computed(
  () => !!jobCodeId.value && !!expectedDate.value && itemTypes.value.length > 0,
);

// ── 등록 ──
const handleSubmit = async () => {
  if (!isFormValid.value) return;

  // TODO: JWT 미연동으로 userId 임시 하드코딩. 추후 auth store에서 가져오도록 교체
  const payload = {
    userId: 1,
    goalType: goalType.value,
    jobCodeId: jobCodeId.value,
    expectedDate: expectedDate.value,
    itemTypes: itemTypes.value,
  };

  const result = await jobApi.createJobGoal(payload);
  const goalId = result.goalId;

  router.push({ name: 'JobRecommend', params: { goalId } });
};
</script>

<template>
  <div class="job-goal-create">
    <RoadmapCharacterSlider :progress="progress" label="진로 로드맵" />

    <h2 class="job-goal-create__title">무엇을 준비하고 싶습니까?</h2>

    <div class="job-goal-create__type-toggle">
      <CategoryButton
        v-for="type in GOAL_TYPES"
        :key="type.value"
        :label="type.label"
        variant="oval-yellow"
        :active="goalType === type.value"
        @click="goalType = type.value"
      />
    </div>

    <BaseInput
      type="select"
      :label="jobCodeLabel"
      v-model="jobCodeId"
      :options="jobCodeOptions"
      placeholder="선택"
    />

    <div class="job-goal-create__date-field">
      <div class="job-goal-create__date-label">{{ dateFieldLabel }}</div>
      <div class="job-goal-create__date-row">
        <BaseInput
          type="select"
          v-model="expectedYear"
          :options="yearOptions"
          placeholder="년"
        />
        <BaseInput
          type="select"
          v-model="expectedMonth"
          :options="monthOptions"
          placeholder="월"
        />
      </div>
    </div>

    <div class="job-goal-create__prep-section">
      <div class="job-goal-create__prep-label">준비 항목 (복수 선택 가능)</div>

      <BaseCard
        v-for="item in visiblePrepItems"
        :key="item.value"
        padding="16px"
        class="prep-card"
        :class="{ 'prep-card--active': itemTypes.includes(item.value) }"
        @click="toggleItemType(item.value)"
      >
        <div class="prep-card__title">{{ item.label }}</div>
        <div v-if="item.desc" class="prep-card__desc">{{ item.desc }}</div>
      </BaseCard>
    </div>

    <BottomButtonBar
      primaryLabel="진로 로드맵 추천 받기"
      :primaryDisabled="!isFormValid"
      @primary-click="handleSubmit"
    />
  </div>
</template>

<style scoped>
.job-goal-create {
  padding: 20px 20px 100px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.job-goal-create__title {
  font-size: 20px;
  font-weight: 700;
  color: #545045;
  margin: 0;
}

.job-goal-create__type-toggle {
  display: flex;
  gap: 8px;
}

.job-goal-create__date-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.job-goal-create__date-label {
  font-size: 15px;
  font-weight: 600;
  color: #545045;
}

.job-goal-create__date-row {
  display: flex;
  gap: 12px;
}

.job-goal-create__date-row > * {
  flex: 1;
}

.job-goal-create__prep-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.job-goal-create__prep-label {
  font-size: 14px;
  color: #9e9e9e;
}

.prep-card {
  cursor: pointer;
  transition: all 0.2s ease;
}

.prep-card--active {
  background-color: #ffbc00;
  border-color: #ffbc00;
}

.prep-card__title {
  font-size: 15px;
  font-weight: 600;
  color: #545045;
}

.prep-card--active .prep-card__title {
  color: #60584c;
}

.prep-card__desc {
  font-size: 12px;
  color: #9e9e9e;
  margin-top: 4px;
}

.prep-card--active .prep-card__desc {
  color: #60584c;
}
</style>
