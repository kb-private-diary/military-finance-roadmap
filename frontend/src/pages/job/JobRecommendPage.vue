<script setup>
// SCR-JOB-02 · step2) 진로 준비 항목 추천
// 취업: 자격증 선택 시 연결 인강 펼침 + 훈련과정 탭
// 공무원·편입: 자격증·어학 / 인터넷 강의 탭

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import { useToast } from '@/composables/useToast';
import { formatWon } from '@/util/format';
import jobApi from '@/api/jobApi';

const route = useRoute();
const router = useRouter();
const { show } = useToast();

const goalId = Number(route.params.goalId);

// ── 탭 ──
const TAB_TYPES = {
  QUALIFICATION: 'qualification',
  SECONDARY: 'secondary',
};

const activeTab = ref(TAB_TYPES.QUALIFICATION);

// ── 조회 데이터 ──
const loading = ref(false);
const submitting = ref(false);

const goalType = ref('');
const qualifications = ref([]);
const courses = ref([]);

// ── 선택 상태 ──
const selectedQualIds = ref(new Set());
const selectedCourseIds = ref(new Set());

const isEmployment = computed(() => goalType.value === 'J01');

const secondTabLabel = computed(() =>
  isEmployment.value ? '훈련과정' : '인터넷 강의',
);

const goalTypeLabel = computed(() => {
  if (goalType.value === 'J01') return '취업';
  if (goalType.value === 'J02') return '공무원';
  if (goalType.value === 'J03') return '편입';

  return '진로';
});

const selectedQualCount = computed(() => selectedQualIds.value.size);
const selectedCourseCount = computed(() => selectedCourseIds.value.size);

const totalSelectedCount = computed(
  () => selectedQualCount.value + selectedCourseCount.value,
);

const isFormValid = computed(
  () => totalSelectedCount.value > 0 && !submitting.value,
);

// ── 추천 조회 ──
const loadRecommend = async () => {
  loading.value = true;

  try {
    const result = await jobApi.findPrepItemRecommend(goalId);

    goalType.value = result.goalType ?? '';
    qualifications.value = result.qualifications ?? [];
    courses.value = result.courses ?? [];
  } catch (error) {
    goalType.value = '';
    qualifications.value = [];
    courses.value = [];

    show('추천 정보를 불러오지 못했습니다.', 'error');
  } finally {
    loading.value = false;
  }
};

onMounted(loadRecommend);

// ── 선택 처리 ──
const isQualificationSelected = (qualId) =>
  selectedQualIds.value.has(Number(qualId));

const isCourseSelected = (courseId) =>
  selectedCourseIds.value.has(Number(courseId));

const getRelatedCourses = (qualId) =>
  courses.value.filter((course) => Number(course.qualId) === Number(qualId));

const toggleQualification = (qualId) => {
  const normalizedQualId = Number(qualId);
  const next = new Set(selectedQualIds.value);

  if (next.has(normalizedQualId)) {
    next.delete(normalizedQualId);

    if (isEmployment.value) {
      const nextCourseIds = new Set(selectedCourseIds.value);

      getRelatedCourses(normalizedQualId).forEach((course) => {
        nextCourseIds.delete(Number(course.courseId));
      });

      selectedCourseIds.value = nextCourseIds;
    }
  } else {
    next.add(normalizedQualId);
  }

  selectedQualIds.value = next;
};

const toggleCourse = (courseId) => {
  const normalizedCourseId = Number(courseId);
  const next = new Set(selectedCourseIds.value);

  if (next.has(normalizedCourseId)) {
    next.delete(normalizedCourseId);
  } else {
    next.add(normalizedCourseId);
  }

  selectedCourseIds.value = next;
};

const formatAmount = (amount) =>
  amount != null ? `${amount.toLocaleString()}원` : '';

// ── 금액 ──
const getQualificationFee = (qualification) => {
  if (qualification.militaryFee != null) {
    return qualification.militaryFee;
  }

  return (qualification.writtenFee ?? 0) + (qualification.practicalFee ?? 0);
};

const getCoursePrice = (course) =>
  course.militaryPrice ?? course.discountPrice ?? course.originalPrice ?? 0;

// ── 시험 일정 D-Day ──
const parseLocalDate = (dateValue) => {
  if (!dateValue) return null;

  const [year, month, day] = dateValue.split('-').map(Number);

  return new Date(year, month - 1, day);
};

const getToday = () => {
  const now = new Date();

  return new Date(now.getFullYear(), now.getMonth(), now.getDate());
};

const getDayDifference = (targetDate) => {
  const millisecondsPerDay = 1000 * 60 * 60 * 24;

  return Math.ceil(
    (targetDate.getTime() - getToday().getTime()) / millisecondsPerDay,
  );
};

const getScheduleLabel = (qualification) => {
  const schedules = [
    {
      label: '필기 접수',
      startDate: qualification.writtenRegStartDate,
      endDate: qualification.writtenRegEndDate,
    },
    {
      label: '필기시험',
      startDate: qualification.writtenExamStartDate,
      endDate: qualification.writtenExamEndDate,
    },
    {
      label: '필기 합격발표',
      startDate: qualification.writtenResultDate,
      endDate: qualification.writtenResultDate,
    },
    {
      label: '실기 접수',
      startDate: qualification.practicalRegStartDate,
      endDate: qualification.practicalRegEndDate,
    },
    {
      label: '실기시험',
      startDate: qualification.practicalExamStartDate,
      endDate: qualification.practicalExamEndDate,
    },
    {
      label: '최종 합격발표',
      startDate: qualification.practicalResultDate,
      endDate: qualification.practicalResultDate,
    },
  ].filter((schedule) => schedule.startDate);

  const today = getToday();

  for (const schedule of schedules) {
    const startDate = parseLocalDate(schedule.startDate);
    const endDate = parseLocalDate(schedule.endDate ?? schedule.startDate);

    // 현재 진행 중인 일정
    if (startDate <= today && today <= endDate) {
      return `${schedule.label} 진행 중`;
    }

    // 가장 가까운 미래 일정
    if (startDate > today) {
      const difference = getDayDifference(startDate);

      return `${schedule.label} D-${difference}`;
    }
  }

  return '올해 시험 일정 종료';
};

const hasSchedule = (qualification) =>
  Boolean(
    qualification.writtenRegStartDate ||
    qualification.writtenExamStartDate ||
    qualification.writtenResultDate ||
    qualification.practicalRegStartDate ||
    qualification.practicalExamStartDate ||
    qualification.practicalResultDate,
  );

// ── 외부 링크 ──
const openExternalLink = (url) => {
  if (!url) return;

  window.open(url, '_blank', 'noopener,noreferrer');
};

// ── 저장 ──
const handleConfirm = async () => {
  if (!isFormValid.value) return;

  submitting.value = true;

  try {
    const result = await jobApi.createJobPlans(goalId, {
      qualIds: Array.from(selectedQualIds.value),
      courseIds: Array.from(selectedCourseIds.value),
    });

    show('준비 항목을 저장했어요.', 'success');

    router.push({
      name: 'JobCost',
      params: { goalId },
      state: {
        costResult: result,
      },
    });
  } catch (error) {
    show('준비 항목을 저장하지 못했습니다.', 'error');
  } finally {
    submitting.value = false;
  }
};

const handlePrev = () => {
  router.back();
};
</script>

<template>
  <div class="job-recommend">
    <RoadmapCharacterSlider :step="2" label="진로 로드맵" />

    <div class="job-recommend__heading">
      <h2 class="job-recommend__title">로드맵을 선택해주세요</h2>

      <p v-if="goalType" class="job-recommend__description">
        {{ goalTypeLabel }} 준비에 필요한 항목을 선택할 수 있어요.
      </p>
    </div>

    <div class="job-recommend__tabs">
      <button
        type="button"
        class="job-recommend__tab"
        :class="{
          'job-recommend__tab--active': activeTab === TAB_TYPES.QUALIFICATION,
        }"
        @click="activeTab = TAB_TYPES.QUALIFICATION"
      >
        자격증·어학
      </button>

      <button
        type="button"
        class="job-recommend__tab"
        :class="{
          'job-recommend__tab--active': activeTab === TAB_TYPES.SECONDARY,
        }"
        @click="activeTab = TAB_TYPES.SECONDARY"
      >
        {{ secondTabLabel }}
      </button>
    </div>

    <div v-if="loading" class="job-recommend__loading">
      추천 정보를 불러오는 중입니다.
    </div>

    <template v-else>
      <!-- 자격증·어학 탭 -->
      <section
        v-if="activeTab === TAB_TYPES.QUALIFICATION"
        class="job-recommend__list"
      >
        <EmptyState
          v-if="qualifications.length === 0"
          title="추천 자격증·어학이 없습니다."
          description="현재 목표에 등록된 추천 정보가 없습니다."
        />

        <template v-else>
          <BaseCard
            v-for="qualification in qualifications"
            :key="qualification.qualId"
            padding="18px"
            class="qualification-card"
            :class="{
              'qualification-card--selected': isQualificationSelected(
                qualification.qualId,
              ),
            }"
            @click="toggleQualification(qualification.qualId)"
          >
            <div class="qualification-card__header">
              <div class="qualification-card__content">
                <div class="qualification-card__title-row">
                  <h3 class="qualification-card__title">
                    {{ qualification.qualName }}
                  </h3>

                  <span
                    v-if="hasSchedule(qualification)"
                    class="qualification-card__dday"
                  >
                    {{ getScheduleLabel(qualification) }}
                  </span>
                </div>

                <p
                  v-if="qualification.organizationName"
                  class="qualification-card__organization"
                >
                  {{ qualification.organizationName }}
                </p>

                <p
                  v-if="qualification.qualSummary"
                  class="qualification-card__summary"
                >
                  {{ qualification.qualSummary }}
                </p>

                <div class="qualification-card__meta">
                  <span v-if="qualification.examRound">
                    {{ qualification.examRound }}
                  </span>

                  <span
                    v-if="
                      qualification.writtenFee != null ||
                      qualification.practicalFee != null
                    "
                    class="qualification-card__fee"
                  >
                    <template v-if="qualification.writtenFee != null">
                      필기 {{ formatWon(qualification.writtenFee) }}
                    </template>

                    <template
                      v-if="
                        qualification.writtenFee != null &&
                        qualification.practicalFee != null
                      "
                    >
                      ·
                    </template>

                    <template v-if="qualification.practicalFee != null">
                      실기 {{ formatWon(qualification.practicalFee) }}
                    </template>
                  </span>
                </div>
              </div>

              <button
                v-if="qualification.detailUrl"
                type="button"
                class="external-link-button"
                aria-label="자격증 상세 페이지 열기"
                @click.stop="openExternalLink(qualification.detailUrl)"
              >
                <svg
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                  class="external-link-icon"
                >
                  <path
                    d="M14 5h5v5M19 5l-8 8M19 13v5a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h5"
                  />
                </svg>
              </button>
            </div>

            <!-- 취업일 때만 자격증 내부에 연결 인강 표시 -->
            <div
              v-if="
                isEmployment &&
                isQualificationSelected(qualification.qualId) &&
                getRelatedCourses(qualification.qualId).length > 0
              "
              class="qualification-card__courses"
              @click.stop
            >
              <div class="qualification-card__divider" />

              <h4 class="qualification-card__course-title">자격증 대비 인강</h4>

              <div class="qualification-card__course-list">
                <button
                  v-for="course in getRelatedCourses(qualification.qualId)"
                  :key="course.courseId"
                  type="button"
                  class="course-item"
                  :class="{
                    'course-item--selected': isCourseSelected(course.courseId),
                  }"
                  @click="toggleCourse(course.courseId)"
                >
                  <span class="course-item__content">
                    <strong class="course-item__name">
                      {{ course.courseName }}
                    </strong>

                    <span
                      v-if="course.providerName"
                      class="course-item__provider"
                    >
                      {{ course.providerName }}
                    </span>

                    <span class="course-item__price">
                      {{ formatWon(getCoursePrice(course)) }}
                    </span>
                  </span>

                  <span
                    v-if="course.detailUrl"
                    class="course-item__link"
                    role="button"
                    tabindex="0"
                    aria-label="인강 상세 페이지 열기"
                    @click.stop="openExternalLink(course.detailUrl)"
                    @keydown.enter.stop="openExternalLink(course.detailUrl)"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      aria-hidden="true"
                      class="external-link-icon"
                    >
                      <path
                        d="M14 5h5v5M19 5l-8 8M19 13v5a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h5"
                      />
                    </svg>
                  </span>
                </button>
              </div>
            </div>
          </BaseCard>
        </template>
      </section>

      <!-- 취업: 훈련과정 탭 -->
      <section v-else-if="isEmployment" class="job-recommend__list">
        <EmptyState
          title="추천 훈련과정이 없습니다."
          description="정부지원 훈련과정 추천은 추후 제공될 예정입니다."
        />
      </section>

      <!-- 공무원·편입: 인터넷 강의 탭 -->
      <section v-else class="job-recommend__list">
        <EmptyState
          v-if="courses.length === 0"
          title="추천 인터넷 강의가 없습니다."
          description="현재 목표에 등록된 추천 정보가 없습니다."
        />

        <template v-else>
          <BaseCard
            v-for="course in courses"
            :key="course.courseId"
            padding="18px"
            class="standalone-course-card"
            :class="{
              'standalone-course-card--selected': isCourseSelected(
                course.courseId,
              ),
            }"
            @click="toggleCourse(course.courseId)"
          >
            <div class="standalone-course-card__header">
              <button
                type="button"
                class="selection-button"
                :class="{
                  'selection-button--selected': isCourseSelected(
                    course.courseId,
                  ),
                }"
                :aria-label="`${course.courseName} 선택`"
                @click.stop="toggleCourse(course.courseId)"
              >
                <svg
                  v-if="isCourseSelected(course.courseId)"
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path d="m6 12 4 4 8-8" />
                </svg>

                <svg v-else viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M12 5v14M5 12h14" />
                </svg>
              </button>

              <div class="standalone-course-card__content">
                <h3 class="standalone-course-card__title">
                  {{ course.courseName }}
                </h3>

                <p
                  v-if="course.providerName"
                  class="standalone-course-card__provider"
                >
                  {{ course.providerName }}
                </p>

                <p
                  v-if="course.benefitDetail"
                  class="standalone-course-card__benefit"
                >
                  {{ course.benefitDetail }}
                </p>

                <p class="standalone-course-card__price">
                  {{ formatWon(getCoursePrice(course)) }}
                </p>
              </div>

              <button
                v-if="course.detailUrl"
                type="button"
                class="external-link-button"
                aria-label="인강 상세 페이지 열기"
                @click.stop="openExternalLink(course.detailUrl)"
              >
                <svg
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                  class="external-link-icon"
                >
                  <path
                    d="M14 5h5v5M19 5l-8 8M19 13v5a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h5"
                  />
                </svg>
              </button>
            </div>
          </BaseCard>
        </template>
      </section>
    </template>

    <div class="job-recommend__selection-summary">
      <span>
        자격증·어학
        <strong>{{ selectedQualCount }}</strong
        >개
      </span>

      <span class="job-recommend__selection-divider">·</span>

      <span>
        인터넷 강의
        <strong>{{ selectedCourseCount }}</strong
        >개
      </span>
    </div>

    <BottomButtonBar
      secondary-label="이전"
      primary-label="선택 완료"
      :primary-disabled="!isFormValid"
      @secondary-click="handlePrev"
      @primary-click="handleConfirm"
    />
  </div>
</template>

<style scoped>
.job-recommend {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px 20px 120px;
}

.job-recommend__heading {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.job-recommend__title {
  margin: 0;
  color: var(--text-strong);
  font-size: 22px;
  font-weight: 700;
}

.job-recommend__description {
  margin: 0;
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.5;
}

.job-recommend__tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  overflow: hidden;
  border: 1px solid var(--line-strong);
  border-radius: 10px;
}

.job-recommend__tab {
  min-height: 48px;
  padding: 0 12px;
  background: var(--surface-muted);
  border: 0;
  color: var(--gray-mid);
  font: inherit;
  font-size: 14px;
  cursor: pointer;
}

.job-recommend__tab + .job-recommend__tab {
  border-left: 1px solid var(--line-strong);
}

.job-recommend__tab--active {
  background: var(--surface-default);
  color: var(--text-strong);
  font-weight: 700;
}

.job-recommend__loading {
  padding: 56px 20px;
  color: var(--text-muted);
  font-size: 14px;
  text-align: center;
}

.job-recommend__list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.qualification-card {
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.qualification-card--selected {
  border-color: var(--kb-yellow);
  box-shadow: 0 0 0 1px var(--kb-yellow);
}

.qualification-card__header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.qualification-card__content {
  min-width: 0;
  flex: 1;
}

.qualification-card__title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.qualification-card__title {
  margin: 0;
  color: var(--text-strong);
  font-size: 17px;
  font-weight: 700;
}

.qualification-card__dday {
  padding: 4px 8px;
  background: var(--kb-yellow-pale);
  border-radius: 999px;
  color: var(--kb-gray);
  font-size: 11px;
  font-weight: 700;
}

.qualification-card__organization,
.qualification-card__summary {
  margin: 5px 0 0;
  font-size: 13px;
  line-height: 1.5;
}

.qualification-card__organization {
  color: var(--text-body);
  font-weight: 600;
}

.qualification-card__summary {
  color: var(--text-muted);
}

.qualification-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  margin-top: 8px;
  color: var(--text-body);
  font-size: 12px;
  font-weight: 600;
}

.selection-button,
.course-item__selection {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  padding: 0;
  background: var(--surface-default);
  border: 1.5px solid var(--line-strong);
  border-radius: 50%;
  color: var(--gray-mid);
  cursor: pointer;
}

.selection-button--selected,
.course-item__selection--selected {
  background: var(--kb-yellow);
  border-color: var(--kb-yellow);
  color: var(--kb-dark-gray);
}

.selection-button svg,
.course-item__selection svg {
  width: 18px;
  height: 18px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.external-link-button {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  padding: 4px;
  background: transparent;
  border: 0;
  color: var(--text-body);
  cursor: pointer;
}

.external-link-icon {
  width: 19px;
  height: 19px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.qualification-card__courses {
  margin-top: 16px;
}

.qualification-card__divider {
  height: 1px;
  margin-bottom: 14px;
  background: var(--line);
}

.qualification-card__course-title {
  margin: 0 0 10px;
  color: var(--text-body);
  font-size: 13px;
  font-weight: 700;
}

.qualification-card__course-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.course-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 12px;
  background: var(--surface-default);
  border: 1px solid var(--line);
  border-radius: 10px;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.course-item--selected {
  background: var(--kb-yellow-pale);
  border-color: var(--kb-yellow);
}

.course-item__selection {
  width: 30px;
  height: 30px;
}

.course-item__selection svg {
  width: 16px;
  height: 16px;
}

.course-item__content {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 3px;
}

.course-item__name {
  overflow: hidden;
  color: var(--text-strong);
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-item__provider {
  color: var(--text-muted);
  font-size: 12px;
}

.course-item__price {
  color: var(--text-body);
  font-size: 13px;
  font-weight: 600;
}

.course-item__link {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  padding: 4px;
  color: var(--text-body);
}

.standalone-course-card {
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.standalone-course-card--selected {
  border-color: var(--kb-yellow);
  box-shadow: 0 0 0 1px var(--kb-yellow);
}

.standalone-course-card__header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.standalone-course-card__content {
  min-width: 0;
  flex: 1;
}

.standalone-course-card__title {
  margin: 0;
  color: var(--text-strong);
  font-size: 16px;
  font-weight: 700;
}

.standalone-course-card__provider,
.standalone-course-card__benefit,
.standalone-course-card__price {
  margin: 5px 0 0;
  font-size: 13px;
  line-height: 1.5;
}

.standalone-course-card__provider {
  color: var(--text-body);
  font-weight: 600;
}

.standalone-course-card__benefit {
  color: var(--text-muted);
}

.standalone-course-card__price {
  color: var(--text-body);
  font-weight: 700;
}

.job-recommend__selection-summary {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 5px;
  color: var(--text-muted);
  font-size: 13px;
}

.job-recommend__selection-summary strong {
  color: var(--text-strong);
}

.job-recommend__selection-divider {
  color: var(--line-strong);
}
</style>
