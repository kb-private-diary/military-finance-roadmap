<script setup>
// SCR-JOB-01 · step1) 진로 목표 등록 (담당: 지원)
// step1 - 목표 유형(취업/공무원/편입)·희망 직무·시기 입력

import { ref, computed, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import BaseInput from '@/components/common/BaseInput.vue';
import CascaderSelect from '@/components/common/CascaderSelect.vue';
import TabBar from '@/components/common/TabBar.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import jobApi from '@/api/jobApi';

const router = useRouter();

// ── goalType 토글 (취업/공무원/편입) ──
const GOAL_TYPES = [
  { value: 'J01', label: '취업' },
  { value: 'J02', label: '공무원' },
  { value: 'J03', label: '편입' },
];

const goalType = ref('J01');

// 이어쓰기 중인 DRAFT 목표 ID
const currentGoalId = ref(null);

const jobCodeLabel = computed(() => {
  if (goalType.value === 'J01') return '희망 직무';
  if (goalType.value === 'J02') return '희망 직렬';
  return '희망 계열';
});

const dateFieldLabel = computed(() => {
  if (goalType.value === 'J01') return '목표 취업 시기';
  if (goalType.value === 'J02') return '목표 합격 시기';
  return '목표 편입 시기';
});

// ── 취업·공무원 카테고리 ──
const categoryList = ref([]);
const parentCategoryId = ref('');
const categoryId = ref('');

// ── 편입 대학·학과 ──
const universityList = ref([]);
const majorList = ref([]);
const univId = ref('');
const majorId = ref('');

const optionLoading = ref(false);

// 이어쓰기 복원 중 watch에 의해 값이 초기화되는 것을 막기 위한 플래그
const isRestoring = ref(false);

// 대분류 목록
const parentCategories = computed(() =>
  categoryList.value.filter((category) => category.categoryLevel === 1),
);

const universityOptions = computed(() =>
  universityList.value.map((university) => ({
    value: university.univId,
    label: university.univName,
  })),
);

const majorOptions = computed(() =>
  majorList.value.map((major) => ({
    value: major.majorId,
    label: major.majorName,
  })),
);

// ── 공통 CascaderSelect(2단) 연동 ──
// 취업·공무원: 대분류(parentCategory) → 세부(childCategory)
// 편입: 대학(university) → 학과(major, 대학 선택 시 지연 로드)
const cascaderGroups = computed(() => {
  if (goalType.value === 'J03') {
    return universityOptions.value;
  }

  return parentCategories.value.map((category) => ({
    value: category.categoryId,
    label: category.categoryName,
  }));
});

const cascaderChildrenByGroup = computed(() => {
  const map = {};

  if (goalType.value === 'J03') {
    // 학과는 선택된 대학에 대해서만 로드됨
    if (univId.value) {
      map[univId.value] = majorOptions.value;
    }

    return map;
  }

  parentCategories.value.forEach((parent) => {
    map[parent.categoryId] = categoryList.value
      .filter(
        (category) =>
          category.categoryLevel === 2 &&
          category.parentId === parent.categoryId,
      )
      .map((category) => ({
        value: category.categoryId,
        label: category.categoryName,
      }));
  });

  return map;
});

// v-model = [그룹값, 자식값] ↔ 기존 개별 상태(refs) 매핑
const cascaderValue = computed({
  get() {
    if (goalType.value === 'J03') {
      return [univId.value || '', majorId.value || ''];
    }

    return [parentCategoryId.value || '', categoryId.value || ''];
  },
  set([group, child]) {
    if (goalType.value === 'J03') {
      if (group !== univId.value) {
        // 대학 변경 → 학과 재로드(watch)·선택 초기화
        univId.value = group;
        majorId.value = '';
      } else {
        majorId.value = child;
      }

      return;
    }

    if (group !== parentCategoryId.value) {
      parentCategoryId.value = group;
      categoryId.value = '';
    } else {
      categoryId.value = child;
    }
  },
});

// 취업·공무원 카테고리 조회
const loadCategories = async () => {
  optionLoading.value = true;

  try {
    categoryList.value = await jobApi.findCategoryList(goalType.value);

    if (!isRestoring.value) {
      parentCategoryId.value = '';
      categoryId.value = '';
    }
  } catch (error) {
    categoryList.value = [];
    console.error('진로 카테고리 조회 실패:', error);
  } finally {
    optionLoading.value = false;
  }
};

// 편입 대학 목록 조회
const loadUniversities = async () => {
  optionLoading.value = true;

  try {
    universityList.value = await jobApi.findTransferUniversityList();

    if (!isRestoring.value) {
      univId.value = '';
      majorList.value = [];
      majorId.value = '';
    }
  } catch (error) {
    universityList.value = [];
    console.error('편입 대학 목록 조회 실패:', error);
  } finally {
    optionLoading.value = false;
  }
};

// 편입 학과 목록 조회
const loadMajors = async () => {
  if (!isRestoring.value) {
    majorList.value = [];
    majorId.value = '';
  }

  if (!univId.value) return;

  optionLoading.value = true;

  try {
    majorList.value = await jobApi.findTransferMajorList(univId.value);
  } catch (error) {
    majorList.value = [];
    console.error('편입 학과 목록 조회 실패:', error);
  } finally {
    optionLoading.value = false;
  }
};

// 목표 유형에 맞는 선택지 조회
const loadGoalOptions = async () => {
  if (!isRestoring.value) {
    categoryList.value = [];
    parentCategoryId.value = '';
    categoryId.value = '';

    universityList.value = [];
    univId.value = '';
    majorList.value = [];
    majorId.value = '';
  }

  if (goalType.value === 'J03') {
    await loadUniversities();
    return;
  }

  await loadCategories();
};

// ── 목표 시기 (년 select + 월 select 조합) ──
const expectedYear = ref('');
const expectedMonth = ref('');

// 연도 옵션: 올해 ~ 5년 후
const currentYear = new Date().getFullYear();

const yearOptions = Array.from({ length: 6 }, (_, i) => {
  const year = currentYear + i;

  return {
    value: year,
    label: `${year}년`,
  };
});

// 월 옵션: 선택한 연도가 올해라면 현재 월부터 선택 가능
const currentMonth = new Date().getMonth() + 1;

const monthOptions = computed(() => {
  if (!expectedYear.value) {
    return [];
  }

  const startMonth =
    Number(expectedYear.value) === currentYear ? currentMonth : 1;

  return Array.from({ length: 13 - startMonth }, (_, index) => {
    const month = startMonth + index;

    return {
      value: month,
      label: `${month}월`,
    };
  });
});

const expectedDate = computed(() => {
  if (!expectedYear.value || !expectedMonth.value) return '';

  return `${expectedYear.value}-${String(expectedMonth.value).padStart(2, '0')}`;
});

// ── watch ──
watch(
  parentCategoryId,
  () => {
    if (isRestoring.value) return;

    categoryId.value = '';
  },
  { flush: 'sync' },
);

watch(
  univId,
  async () => {
    if (isRestoring.value) return;

    await loadMajors();
  },
  { flush: 'sync' },
);

watch(
  goalType,
  async () => {
    if (isRestoring.value) return;

    // 진로 유형 변경 시 기존 선택값 초기화
    expectedYear.value = '';
    expectedMonth.value = '';

    await loadGoalOptions();
  },
  { flush: 'sync' },
);

// ── 작성 중인 목표 이어쓰기 ──
const loadCurrentGoal = async () => {
  isRestoring.value = true;

  try {
    // JWT 기준 로그인 사용자의 DRAFT 목표 조회
    const currentGoal = await jobApi.findCurrentJobGoal();

    // 작성 중인 목표가 없으면 기본 J01 선택지만 조회
    if (!currentGoal) {
      isRestoring.value = false;
      await loadGoalOptions();
      return;
    }

    // 이어쓰기 중인 DRAFT 목표 ID 저장
    currentGoalId.value = currentGoal.goalId;

    // 기존 목표 유형 복원
    goalType.value = currentGoal.goalType;

    // 목표 유형에 맞는 선택지 조회
    if (currentGoal.goalType === 'J03') {
      await loadUniversities();

      univId.value = currentGoal.univId;

      if (currentGoal.univId) {
        majorList.value = await jobApi.findTransferMajorList(
          currentGoal.univId,
        );
      }

      majorId.value = currentGoal.majorId;
    } else {
      categoryList.value = await jobApi.findCategoryList(currentGoal.goalType);

      // 저장된 중분류를 기준으로 부모 대분류 찾기
      const selectedCategory = categoryList.value.find(
        (category) => category.categoryId === Number(currentGoal.categoryId),
      );

      if (selectedCategory) {
        parentCategoryId.value = selectedCategory.parentId;
        categoryId.value = currentGoal.categoryId;
      }
    }

    // YYYY-MM → 연도 / 월 복원
    if (currentGoal.expectedDate) {
      const [year, month] = currentGoal.expectedDate.split('-');

      expectedYear.value = Number(year);
      expectedMonth.value = Number(month);
    }
  } catch (error) {
    console.error('작성 중인 진로 목표 조회 실패:', error);
  } finally {
    isRestoring.value = false;
  }
};

onMounted(loadCurrentGoal);

// ── 제출 가능 여부 ──
const isFormValid = computed(() => {
  if (!expectedDate.value) return false;

  if (goalType.value === 'J03') {
    return !!univId.value && !!majorId.value;
  }

  return !!categoryId.value;
});

// ── 진로 목표 등록 / 이어쓰기 ──
const handleSubmit = async () => {
  if (!isFormValid.value) return;

  const payload = {
    goalType: goalType.value,
    categoryId: goalType.value === 'J03' ? null : Number(categoryId.value),
    univId: goalType.value === 'J03' ? Number(univId.value) : null,
    majorId: goalType.value === 'J03' ? Number(majorId.value) : null,
    expectedDate: expectedDate.value,
  };

  try {
    let goalId;

    if (currentGoalId.value) {
      // 이어쓰기 → 기존 DRAFT 수정
      await jobApi.updateJobGoal(currentGoalId.value, payload);

      goalId = currentGoalId.value;
    } else {
      // 신규 등록 → 새로운 DRAFT 생성
      const result = await jobApi.createJobGoal(payload);

      goalId = result.goalId;
    }

    router.push({
      name: 'JobRecommend',
      params: { goalId },
    });
  } catch (error) {
    console.error('진로 목표 저장 실패:', error);
  }
};
</script>

<template>
  <div class="job-goal-create">
    <RoadmapCharacterSlider :step="1" label="진로 로드맵" />

    <PageHeader title="무엇을 준비하고 싶습니까?" />

    <div class="job-goal-create__type-section">
      <p class="job-goal-create__type-label">진로 유형</p>

      <TabBar variant="fill" v-model="goalType" :tabs="GOAL_TYPES" />
    </div>

    <!--  기존 BaseInput 1개 대신 입력칸 아래 2단 드롭다운 -->
    <div class="job-goal-create__option-field">
      <div class="job-goal-create__option-label">
        {{ jobCodeLabel }}
      </div>

      <CascaderSelect
        v-model="cascaderValue"
        :groups="cascaderGroups"
        :children-by-group="cascaderChildrenByGroup"
        :placeholder="optionLoading ? '불러오는 중...' : '선택'"
        :disabled="optionLoading"
        group-empty-text="왼쪽에서 분류를 선택해주세요."
        child-empty-text="선택 가능한 항목이 없습니다."
      />
    </div>

    <div class="job-goal-create__date-field">
      <div class="job-goal-create__date-label">
        {{ dateFieldLabel }}
      </div>

      <div class="job-goal-create__date-row">
        <BaseInput
          v-model="expectedYear"
          type="select"
          :options="yearOptions"
          placeholder="년"
        />

        <BaseInput
          v-model="expectedMonth"
          type="select"
          :options="monthOptions"
          placeholder="월"
        />
      </div>
    </div>
    <BottomButtonBar
      secondary-label="이전"
      primary-label="진로 추천받기"
      :primary-disabled="!isFormValid"
      @secondary-click="router.push({ name: 'RoadmapMain' })"
      @primary-click="handleSubmit"
    />
  </div>
</template>

<style scoped>
.job-goal-create {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
  padding: 20px 20px 100px;
}

/* 진로 유형 (라벨 + 채움 버튼) */
.job-goal-create__type-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

/* 직무·직렬·학과 */
.job-goal-create__option-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.job-goal-create__type-label,
.job-goal-create__option-label,
.job-goal-create__date-label {
  color: var(--text-body);
  font-size: 14px;
  font-weight: 600;
}

/* 목표 시기 */
.job-goal-create__date-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.job-goal-create__date-row {
  display: flex;
  gap: 12px;
}

.job-goal-create__date-row > * {
  flex: 1;
  min-width: 0;
}
</style>
