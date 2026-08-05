<script setup>
// SCR-JOB-01 · step1) 진로 목표 등록  (담당: 지원)
// step1 - 목표 유형 (취업/공무원/편입)·희망 직무·시기 입력
import { ref, computed, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import BaseInput from '@/components/common/BaseInput.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
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

// 대분류 목록
const parentCategories = computed(() =>
  categoryList.value.filter((category) => category.categoryLevel === 1),
);

// 선택한 대분류의 중분류 목록
const childCategories = computed(() =>
  categoryList.value.filter(
    (category) =>
      category.categoryLevel === 2 &&
      category.parentId === parentCategoryId.value,
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

const loadCategories = async () => {
  optionLoading.value = true;

  try {
    categoryList.value = await jobApi.findCategoryList(goalType.value);
    parentCategoryId.value = '';
    categoryId.value = '';
  } catch (error) {
    categoryList.value = [];
  } finally {
    optionLoading.value = false;
  }
};

watch(parentCategoryId, () => {
  categoryId.value = '';
});

const loadUniversities = async () => {
  optionLoading.value = true;

  try {
    universityList.value = await jobApi.findTransferUniversityList();

    univId.value = '';
    majorList.value = [];
    majorId.value = '';
  } catch (error) {
    universityList.value = [];
  } finally {
    optionLoading.value = false;
  }
};

const loadMajors = async () => {
  majorList.value = [];
  majorId.value = '';

  if (!univId.value) return;

  optionLoading.value = true;

  try {
    majorList.value = await jobApi.findTransferMajorList(univId.value);
  } catch (error) {
    majorList.value = [];
  } finally {
    optionLoading.value = false;
  }
};

const loadGoalOptions = async () => {
  categoryList.value = [];
  parentCategoryId.value = '';
  categoryId.value = '';

  universityList.value = [];
  univId.value = '';
  majorList.value = [];
  majorId.value = '';

  if (goalType.value === 'J03') {
    await loadUniversities();
    return;
  }

  await loadCategories();
};

watch(parentCategoryId, () => {
  categoryId.value = '';
});

watch(univId, loadMajors);

watch(goalType, loadCategories);

onMounted(loadCategories);

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

const expectedDate = computed(() => {
  if (!expectedYear.value || !expectedMonth.value) return '';

  return `${expectedYear.value}-${String(expectedMonth.value).padStart(2, '0')}`;
});

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

// ── 제출 가능 여부 ──
const isFormValid = computed(() => {
  if (!expectedDate.value) return false;

  if (goalType.value === 'J03') {
    return !!univId.value && !!majorId.value;
  }

  return !!categoryId.value;
});

// ── 등록 ──
const handleSubmit = async () => {
  if (!isFormValid.value) return;

  // TODO: JWT 미연동으로 userId 임시 하드코딩. 추후 auth store에서 가져오도록 교체
  const payload = {
    userId: 1,
    goalType: goalType.value,
    categoryId: goalType.value === 'J03' ? null : Number(categoryId.value),
    univId: goalType.value === 'J03' ? Number(univId.value) : null,
    majorId: goalType.value === 'J03' ? Number(majorId.value) : null,

    expectedDate: expectedDate.value,
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
  margin: 0;
  color: var(--kb-dark-gray);
  font-size: 20px;
  font-weight: 700;
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
  font-size: 16px;
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
  overflow: hidden;
  font-size: 16px;
  font-weight: 500;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
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
