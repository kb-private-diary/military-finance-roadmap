<script setup>
// SCR-JOB-01 · step1) 진로 목표 등록 (담당: 지원)
// step1 - 목표 유형(취업/공무원/편입)·희망 직무·시기 입력

import { ref, computed, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import BaseInput from '@/components/common/BaseInput.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
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

// 선택한 대분류의 중분류 목록
const childCategories = computed(() =>
  categoryList.value.filter(
    (category) =>
      category.categoryLevel === 2 &&
      category.parentId === Number(parentCategoryId.value),
  ),
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

// ── 드롭다운 표시 ──
const isOptionDropdownOpen = ref(false);

const selectedOptionLabel = computed(() => {
  if (goalType.value === 'J03') {
    const university = universityList.value.find(
      (item) => item.univId === Number(univId.value),
    );

    const major = majorList.value.find(
      (item) => item.majorId === Number(majorId.value),
    );

    if (!university || !major) return '';

    return `${university.univName} > ${major.majorName}`;
  }

  const parentCategory = parentCategories.value.find(
    (item) => item.categoryId === Number(parentCategoryId.value),
  );

  const childCategory = childCategories.value.find(
    (item) => item.categoryId === Number(categoryId.value),
  );

  if (!parentCategory || !childCategory) return '';

  return `${parentCategory.categoryName} > ${childCategory.categoryName}`;
});

// ── 선택 처리 ──
const selectParentCategory = (selectedId) => {
  parentCategoryId.value = selectedId;
  categoryId.value = '';
};

const selectChildCategory = (selectedId) => {
  categoryId.value = selectedId;
  isOptionDropdownOpen.value = false;
};

const selectUniversity = (selectedId) => {
  univId.value = selectedId;
  majorId.value = '';
};

const selectMajor = (selectedId) => {
  majorId.value = selectedId;
  isOptionDropdownOpen.value = false;
};

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
    isOptionDropdownOpen.value = false;

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

    <h2 class="job-goal-create__title text-title">무엇을 준비하고 싶습니까?</h2>

    <div class="job-goal-create__type-section">
      <p class="job-goal-create__type-label">진로 유형</p>

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
    </div>

    <!--  기존 BaseInput 1개 대신 입력칸 아래 2단 드롭다운 -->
    <div class="job-goal-create__option-field">
      <div class="job-goal-create__option-label">
        {{ jobCodeLabel }}
      </div>

      <div class="job-cascader">
        <button
          type="button"
          class="job-cascader__trigger"
          :class="{ 'job-cascader__trigger--open': isOptionDropdownOpen }"
          :disabled="optionLoading"
          @click="isOptionDropdownOpen = !isOptionDropdownOpen"
        >
          <span
            class="job-cascader__trigger-text"
            :class="{
              'job-cascader__trigger-text--placeholder': !selectedOptionLabel,
            }"
          >
            {{
              optionLoading ? '불러오는 중...' : selectedOptionLabel || '선택'
            }}
          </span>

          <svg
            class="job-cascader__arrow"
            :class="{ 'job-cascader__arrow--open': isOptionDropdownOpen }"
            viewBox="0 0 24 24"
            width="16"
            height="16"
          >
            <path
              d="M7 10l5 5 5-5"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
            />
          </svg>
        </button>

        <div v-if="isOptionDropdownOpen" class="job-cascader__panel">
          <!-- 취업·공무원 -->
          <template v-if="goalType !== 'J03'">
            <div class="job-cascader__column">
              <button
                v-for="parentCategory in parentCategories"
                :key="parentCategory.categoryId"
                type="button"
                class="job-cascader__option"
                :class="{
                  'job-cascader__option--active':
                    Number(parentCategoryId) === parentCategory.categoryId,
                }"
                @click="selectParentCategory(parentCategory.categoryId)"
              >
                <span>{{ parentCategory.categoryName }}</span>
                <span class="job-cascader__option-arrow">›</span>
              </button>
            </div>

            <div class="job-cascader__column">
              <div v-if="!parentCategoryId" class="job-cascader__empty">
                왼쪽에서 분류를 선택해주세요.
              </div>

              <template v-else>
                <button
                  v-for="childCategory in childCategories"
                  :key="childCategory.categoryId"
                  type="button"
                  class="job-cascader__option"
                  :class="{
                    'job-cascader__option--active':
                      Number(categoryId) === childCategory.categoryId,
                  }"
                  @click="selectChildCategory(childCategory.categoryId)"
                >
                  {{ childCategory.categoryName }}
                </button>
              </template>
            </div>
          </template>

          <!-- 편입 -->
          <template v-else>
            <div class="job-cascader__column">
              <button
                v-for="university in universityList"
                :key="university.univId"
                type="button"
                class="job-cascader__option"
                :class="{
                  'job-cascader__option--active':
                    Number(univId) === university.univId,
                }"
                @click="selectUniversity(university.univId)"
              >
                <span>{{ university.univName }}</span>
                <span class="job-cascader__option-arrow">›</span>
              </button>
            </div>

            <div class="job-cascader__column">
              <div v-if="!univId" class="job-cascader__empty">
                왼쪽에서 대학을 선택해주세요.
              </div>

              <div v-else-if="optionLoading" class="job-cascader__empty">
                학과를 불러오는 중입니다.
              </div>

              <div
                v-else-if="majorList.length === 0"
                class="job-cascader__empty"
              >
                등록된 학과가 없습니다.
              </div>

              <template v-else>
                <button
                  v-for="major in majorList"
                  :key="major.majorId"
                  type="button"
                  class="job-cascader__option"
                  :class="{
                    'job-cascader__option--active':
                      Number(majorId) === major.majorId,
                  }"
                  @click="selectMajor(major.majorId)"
                >
                  {{ major.majorName }}
                </button>
              </template>
            </div>
          </template>
        </div>
      </div>
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
      primary-label="진로 로드맵 추천 받기"
      :primary-disabled="!isFormValid"
      @primary-click="handleSubmit"
    />
  </div>
</template>

<style scoped>
.job-goal-create {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px 20px 100px;
}

.job-goal-create__title {
  margin: 0 0 22px;
  line-height: 1.35;
}

.job-goal-create__type-toggle {
  display: flex;
  gap: 8px;
}

/* 직무·직렬·학과 */
.job-goal-create__option-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.job-goal-create__type-label,
.job-goal-create__option-label,
.job-goal-create__date-label {
  color: var(--kb-dark-gray);
  font-size: 15px;
  font-weight: 600;
}

/* 2단 드롭다운 */
.job-cascader {
  position: relative;
  width: 100%;
}

.job-cascader__trigger {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 44px;
  padding: 10px 28px 10px 12px;

  background: transparent;
  border: none;
  border-bottom: 2px solid var(--kb-gold);
  border-radius: 0;

  color: var(--text-body);
  font: inherit;
  font-size: 15px;
  font-weight: 500;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.2s;
}

.job-cascader__trigger:hover {
  border-bottom-color: var(--kb-yellow-deep);
}

.job-cascader__trigger:focus-visible,
.job-cascader__trigger--open {
  border-bottom-color: var(--kb-yellow-deep);
  outline: none;
}

.job-cascader__trigger:disabled {
  background: var(--surface-subtle);
  cursor: not-allowed;
}

.job-cascader__trigger-text {
  flex: 1;
  overflow: visible;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.4;
  text-align: center;
  white-space: normal;
  word-break: keep-all;
}

.job-cascader__trigger-text--placeholder {
  color: var(--placeholder);
  font-weight: 400;
}

.job-cascader__arrow {
  position: absolute;
  right: 4px;
  flex-shrink: 0;
  color: var(--gray-mid);
  font-size: 14px;
  transition:
    transform 0.3s ease,
    color 0.2s ease;
}

.job-cascader__arrow--open {
  color: var(--kb-yellow-deep);
  transform: rotate(180deg);
}

/* 드롭다운 패널 */
.job-cascader__panel {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  z-index: 999;

  display: grid;
  grid-template-columns: 1fr 1fr;

  height: 280px;
  overflow: hidden;

  background: var(--surface-default);
  border: 1px solid var(--line);
  border-radius: 10px;
  box-shadow: 0 4px 12px var(--shadow-dropdown);
}

.job-cascader__column {
  overflow-y: auto;
  background: var(--surface-default);
}

.job-cascader__column + .job-cascader__column {
  border-left: 1px solid var(--line);
}

.job-cascader__option {
  display: flex;
  align-items: center;
  justify-content: space-between;

  width: 100%;
  min-height: 44px;
  padding: 10px 12px;

  border: 0;
  background: var(--surface-default);

  color: var(--kb-dark-gray);
  font-size: 14px;
  text-align: left;
  cursor: pointer;
}

.job-cascader__option:hover {
  background: var(--kb-yellow-pale);
}

.job-cascader__option--active {
  background: var(--kb-yellow);
  color: var(--kb-dark-gray);
  font-weight: 600;
}

.job-cascader__option-arrow {
  margin-left: 8px;
  color: var(--kb-gray);
  font-size: 16px;
}

.job-cascader__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 20px;
  color: var(--text-muted);
  font-size: 13px;
  text-align: center;
}

/* 목표 시기 */
.job-goal-create__date-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
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
