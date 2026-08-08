<script setup>
// SCR-JOB-05 · 진로 로드맵 상세 조회 (담당: 지원)
// 저장된 진로 목표·준비항목·비용·정책/금융상품 조회

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import jobApi from '@/api/jobApi';

import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import BaseModal from '@/components/common/BaseModal.vue';

import calculatorImage from '@/assets/images/calculator.png';

import { useToast } from '@/composables/useToast';

const route = useRoute();
const router = useRouter();
const { show } = useToast();

const goalId = computed(() => Number(route.params.goalId));

const loading = ref(false);
const deleting = ref(false);
const detail = ref(null);
const loadError = ref('');

const isDeleteModalOpen = ref(false);

// ─────────────────────────────────────────────
// 탭 - 기존 구조 고정
// ─────────────────────────────────────────────
const TABS = [
  { key: 'goal', label: '선택한 목표' },
  { key: 'cost', label: '비용 계산' },
  { key: 'products', label: '금융상품' },
];

const activeTab = ref('goal');

// 한 번에 하나만 펼치는 준비항목 아코디언
const expandedItemKey = ref(null);

// 비용 상세 내역 아코디언
const isCostDetailExpanded = ref(false);

// TODO: 후회소비 담당 API 연동 후 실제 데이터로 교체
const regretAnalysis = ref(null);

const isValidGoalId = computed(
  () => Number.isInteger(goalId.value) && goalId.value > 0,
);

// ─────────────────────────────────────────────
// 상세 데이터
// ─────────────────────────────────────────────
const qualifications = computed(() => detail.value?.qualifications ?? []);

const courses = computed(() => detail.value?.courses ?? []);

const policies = computed(() => detail.value?.policies ?? []);

const financialProducts = computed(() => detail.value?.financialProducts ?? []);

// ─────────────────────────────────────────────
// 비용
// ─────────────────────────────────────────────

// 자격증·어학 비용
const qualificationCost = computed(() =>
  qualifications.value.reduce(
    (sum, item) => sum + Number(item.selectedCost ?? 0),
    0,
  ),
);

// 인강 비용
const courseCost = computed(() =>
  courses.value.reduce((sum, item) => sum + Number(item.selectedCost ?? 0), 0),
);

// 총 예상 준비 비용
const totalCost = computed(() => qualificationCost.value + courseCost.value);

// 선택 준비 항목 개수
const selectedItemCount = computed(
  () => qualifications.value.length + courses.value.length,
);

// 비용 비율
const qualificationRate = computed(() => {
  if (totalCost.value <= 0) {
    return 0;
  }

  return Math.round((qualificationCost.value / totalCost.value) * 100);
});

const courseRate = computed(() => {
  if (totalCost.value <= 0) {
    return 0;
  }

  return 100 - qualificationRate.value;
});

// ─────────────────────────────────────────────
// 목표 정보
// ─────────────────────────────────────────────

const goalTitle = computed(() => {
  if (!detail.value) {
    return '';
  }

  if (detail.value.goalType === 'J03') {
    if (detail.value.univName && detail.value.majorName) {
      return `${detail.value.univName} · ${detail.value.majorName}`;
    }

    return detail.value.majorName ?? detail.value.univName ?? '편입 목표';
  }

  return detail.value.categoryName ?? '진로 목표';
});

// YYYY-MM → YYYY.MM
const formattedExpectedDate = computed(() =>
  (detail.value?.expectedDate ?? '').replace('-', '.'),
);

// ─────────────────────────────────────────────
// 공통 포맷
// ─────────────────────────────────────────────

const formatAmount = (amount) => `${Number(amount ?? 0).toLocaleString()}원`;

const formatDate = (date) => {
  if (!date) {
    return '';
  }

  return String(date).replaceAll('-', '.');
};

const formatSchedulePeriod = (schedule) => {
  if (!schedule?.startDate) {
    return '';
  }

  const startDate = formatDate(schedule.startDate);
  const endDate = formatDate(schedule.endDate);

  if (!endDate || schedule.startDate === schedule.endDate) {
    return startDate;
  }

  return `${startDate} ~ ${endDate}`;
};

// ─────────────────────────────────────────────
// 자격증 시험 일정
// ─────────────────────────────────────────────

const getQualificationSchedules = (qualification) => {
  if (!qualification) {
    return [];
  }

  return [
    {
      key: 'written-reg',
      label: '필기 원서접수',
      startDate: qualification.writtenRegStartDate,
      endDate: qualification.writtenRegEndDate,
    },
    {
      key: 'written-exam',
      label: '필기시험',
      startDate: qualification.writtenExamStartDate,
      endDate: qualification.writtenExamEndDate,
    },
    {
      key: 'written-result',
      label: '필기 합격발표',
      startDate: qualification.writtenResultDate,
      endDate: qualification.writtenResultDate,
    },
    {
      key: 'practical-reg',
      label: '실기 원서접수',
      startDate: qualification.practicalRegStartDate,
      endDate: qualification.practicalRegEndDate,
    },
    {
      key: 'practical-exam',
      label: '실기시험',
      startDate: qualification.practicalExamStartDate,
      endDate: qualification.practicalExamEndDate,
    },
    {
      key: 'practical-result',
      label: '최종 합격발표',
      startDate: qualification.practicalResultDate,
      endDate: qualification.practicalResultDate,
    },
  ].filter((schedule) => schedule.startDate);
};

const getTodayDateString = () => {
  const today = new Date();

  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
};

// 현재 진행 중인 일정이 있으면 우선,
// 없으면 가장 가까운 미래 일정 반환
const getNextSchedule = (qualification) => {
  const schedules = getQualificationSchedules(qualification);

  if (schedules.length === 0) {
    return null;
  }

  const today = getTodayDateString();

  const activeSchedule = schedules.find((schedule) => {
    const endDate = schedule.endDate ?? schedule.startDate;

    return schedule.startDate <= today && today <= endDate;
  });

  if (activeSchedule) {
    return activeSchedule;
  }

  return schedules.find((schedule) => schedule.startDate > today) ?? null;
};

const getScheduleDday = (schedule) => {
  if (!schedule?.startDate) {
    return '';
  }

  const today = getTodayDateString();
  const endDate = schedule.endDate ?? schedule.startDate;

  if (schedule.startDate <= today && today <= endDate) {
    return '진행 중';
  }

  if (schedule.startDate < today) {
    return '';
  }

  const todayDate = new Date(`${today}T00:00:00`);
  const scheduleDate = new Date(`${schedule.startDate}T00:00:00`);

  const diffTime = scheduleDate.getTime() - todayDate.getTime();

  const diffDay = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (diffDay === 0) {
    return 'D-Day';
  }

  return `D-${diffDay}`;
};

const getExamRoundLabel = (qualification) => {
  if (!qualification?.examYear && !qualification?.examRound) {
    return '';
  }

  if (qualification.examYear && qualification.examRound) {
    return `${qualification.examYear}년 ${qualification.examRound}`;
  }

  if (qualification.examYear) {
    return `${qualification.examYear}년`;
  }

  return qualification.examRound;
};

// ─────────────────────────────────────────────
// 아코디언
// ─────────────────────────────────────────────

const toggleItemDetail = (itemKey) => {
  expandedItemKey.value = expandedItemKey.value === itemKey ? null : itemKey;
};

const isItemExpanded = (itemKey) => expandedItemKey.value === itemKey;

const toggleCostDetail = () => {
  isCostDetailExpanded.value = !isCostDetailExpanded.value;
};

// ─────────────────────────────────────────────
// 탭
// ─────────────────────────────────────────────

const selectTab = (key) => {
  activeTab.value = key;
};

// ─────────────────────────────────────────────
// 상세 조회
// ─────────────────────────────────────────────

const loadDetail = async () => {
  if (!isValidGoalId.value) {
    loadError.value = '올바른 진로 목표 정보가 아닙니다.';
    show(loadError.value, 'error');
    return;
  }

  try {
    loading.value = true;
    loadError.value = '';

    detail.value = await jobApi.findJobGoalDetail(goalId.value);
  } catch (error) {
    console.error('진로 목표 상세 조회 실패:', error);

    detail.value = null;
    loadError.value = '진로 로드맵을 불러오지 못했습니다.';

    show(loadError.value, 'error');
  } finally {
    loading.value = false;
  }
};

// ─────────────────────────────────────────────
// 외부 페이지
// ─────────────────────────────────────────────

const openExternalLink = (linkUrl) => {
  if (!linkUrl) {
    show('등록된 상세 페이지가 없습니다.', 'info');
    return;
  }

  window.open(linkUrl, '_blank', 'noopener,noreferrer');
};

// ─────────────────────────────────────────────
// 정책·금융상품 배지
// ─────────────────────────────────────────────

const getBadgeText = (product) =>
  (product?.badgeCode ?? '').replace(' 가능', '');

const getBadgeVariant = (product) => {
  const badgeCode = product?.badgeCode ?? '';

  if (badgeCode.includes('복무 중')) {
    return 'yellow';
  }

  if (badgeCode.includes('전역 후')) {
    return 'green';
  }

  return 'gray';
};

// ─────────────────────────────────────────────
// 삭제
// ─────────────────────────────────────────────

const openDeleteModal = () => {
  isDeleteModalOpen.value = true;
};

const handleDelete = async () => {
  if (deleting.value) {
    return;
  }

  try {
    deleting.value = true;

    await jobApi.deleteJobGoal(goalId.value);

    isDeleteModalOpen.value = false;

    show('진로 로드맵이 삭제되었습니다.', 'success');

    await router.push({
      name: 'RoadmapMain',
    });
  } catch (error) {
    console.error('진로 목표 삭제 실패:', error);

    show('진로 로드맵을 삭제하지 못했습니다.', 'error');
  } finally {
    deleting.value = false;
  }
};

const handleConfirm = async () => {
  await router.push({
    name: 'RoadmapMain',
  });
};

onMounted(async () => {
  await loadDetail();

  // TODO:
  // 후회소비 담당 API가 확정되면 여기에서 별도 API 호출 후
  // regretAnalysis.value에 결과 저장
});
</script>

<template>
  <div class="job-detail">
    <!-- 로딩 -->
    <div v-if="loading" class="job-detail__state text-caption">
      불러오는 중...
    </div>

    <!-- 조회 오류 -->
    <p v-else-if="loadError" class="form-error text-caption" role="alert">
      {{ loadError }}
    </p>

    <template v-else-if="detail">
      <!-- ─────────────────────────────
           상단 목표 요약
      ───────────────────────────── -->
      <BaseCard padding="18px" class="goal-summary-card">
        <div class="goal-summary-card__header">
          <span class="goal-summary-card__tag"> 진로 </span>

          <strong class="goal-summary-card__title">
            {{ goalTitle }}
          </strong>
        </div>

        <div v-if="formattedExpectedDate" class="goal-summary-card__date">
          <span>목표 시기</span>

          <strong>
            {{ formattedExpectedDate }}
          </strong>
        </div>

        <p class="goal-summary-card__items">
          자격증·어학 {{ qualifications.length }}개 · 인터넷강의
          {{ courses.length }}개
        </p>
      </BaseCard>

      <!-- ─────────────────────────────
           총 예상 준비 비용
      ───────────────────────────── -->
      <BaseCard padding="16px" class="total-cost-card">
        <div class="total-cost-card__icon" aria-hidden="true">
          <img
            :src="calculatorImage"
            alt=""
            class="total-cost-card__icon-image"
          />
        </div>

        <div class="total-cost-card__content">
          <p>총 예상 준비 비용</p>

          <strong>
            {{ formatAmount(totalCost) }}
          </strong>

          <span>
            선택한 준비 항목
            {{ selectedItemCount }}개 기준
          </span>
        </div>
      </BaseCard>

      <!-- ─────────────────────────────
           탭 - 기존 코드 유지
      ───────────────────────────── -->
      <div class="tab-row">
        <button
          v-for="tab in TABS"
          :key="tab.key"
          type="button"
          class="tab-button"
          :class="{
            'is-active': activeTab === tab.key,
          }"
          @click="selectTab(tab.key)"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- ==================================================
           1. 선택한 목표
      ================================================== -->
      <section v-if="activeTab === 'goal'" class="job-detail__tab-content">
        <!-- 자격증·어학 -->
        <div v-if="qualifications.length > 0" class="detail-section">
          <h2 class="detail-section__title text-label">자격증·어학</h2>

          <BaseCard
            v-for="qualification in qualifications"
            :key="qualification.qualId"
            padding="0"
            class="prep-item-card"
          >
            <!-- 아코디언 버튼 -->
            <button
              type="button"
              class="prep-item-card__toggle"
              :aria-expanded="
                isItemExpanded(`qualification-${qualification.qualId}`)
              "
              @click="toggleItemDetail(`qualification-${qualification.qualId}`)"
            >
              <div class="prep-item-card__header">
                <div class="prep-item-card__title-wrap">
                  <strong class="prep-item-card__title">
                    {{ qualification.qualName }}
                  </strong>
                </div>

                <span
                  class="prep-item-card__arrow"
                  :class="{
                    'is-expanded': isItemExpanded(
                      `qualification-${qualification.qualId}`,
                    ),
                  }"
                  aria-hidden="true"
                >
                  ⌄
                </span>
              </div>

              <!-- 다음 일정 -->
              <template v-if="getNextSchedule(qualification)">
                <div class="prep-item-card__next">
                  <span class="prep-item-card__next-label"> 다음 일정 </span>

                  <div class="prep-item-card__next-info">
                    <strong>
                      {{ getNextSchedule(qualification).label }}
                    </strong>

                    <span
                      v-if="getScheduleDday(getNextSchedule(qualification))"
                      class="prep-item-card__dday"
                    >
                      {{ getScheduleDday(getNextSchedule(qualification)) }}
                    </span>
                  </div>

                  <span class="prep-item-card__date">
                    {{ formatSchedulePeriod(getNextSchedule(qualification)) }}
                  </span>
                </div>
              </template>

              <!-- 일정이 없는 자격증/어학 -->
              <div v-else class="prep-item-card__next">
                <span class="prep-item-card__next-label"> 시험 정보 </span>

                <strong class="prep-item-card__fallback">
                  공식 사이트에서 시험 일정을 확인해주세요.
                </strong>
              </div>
            </button>

            <!-- 펼친 영역 -->
            <div
              v-if="isItemExpanded(`qualification-${qualification.qualId}`)"
              class="prep-item-card__detail"
            >
              <div
                v-if="
                  qualification.qualSummary || qualification.organizationName
                "
                class="prep-item-card__summary"
              >
                <p v-if="qualification.qualSummary">
                  {{ qualification.qualSummary }}
                </p>

                <span v-if="qualification.organizationName">
                  {{ qualification.organizationName }}
                </span>
              </div>

              <!-- 일정 있음 -->
              <template
                v-if="getQualificationSchedules(qualification).length > 0"
              >
                <div class="prep-item-card__detail-heading">
                  <strong> 시험 일정 </strong>

                  <span v-if="getExamRoundLabel(qualification)">
                    {{ getExamRoundLabel(qualification) }}
                  </span>
                </div>

                <div class="schedule-list">
                  <div
                    v-for="(schedule, index) in getQualificationSchedules(
                      qualification,
                    )"
                    :key="schedule.key"
                    class="schedule-list__item"
                  >
                    <div class="schedule-list__timeline" aria-hidden="true">
                      <span class="schedule-list__dot" />

                      <span
                        v-if="
                          index <
                          getQualificationSchedules(qualification).length - 1
                        "
                        class="schedule-list__line"
                      />
                    </div>

                    <div class="schedule-list__content">
                      <strong>
                        {{ schedule.label }}
                      </strong>

                      <span>
                        {{ formatSchedulePeriod(schedule) }}
                      </span>
                    </div>
                  </div>
                </div>

                <p class="prep-item-card__notice">
                  복무 중 시험 응시는 소속 부대 일정 및 외출·휴가 가능 여부를
                  확인해주세요.
                </p>
              </template>

              <!-- 일정 없음 -->
              <p v-else class="prep-item-card__empty">
                등록된 공식 시험 일정이 없습니다. 최신 일정은 공식 사이트에서
                확인해주세요.
              </p>

              <button
                v-if="qualification.detailUrl"
                type="button"
                class="prep-item-card__external"
                @click.stop="openExternalLink(qualification.detailUrl)"
              >
                공식 사이트 바로가기
                <span aria-hidden="true">↗</span>
              </button>
            </div>
          </BaseCard>
        </div>

        <!-- 인터넷 강의 -->
        <div v-if="courses.length > 0" class="detail-section">
          <h2 class="detail-section__title text-label">인터넷 강의</h2>

          <BaseCard
            v-for="course in courses"
            :key="course.courseId"
            padding="0"
            class="prep-item-card"
          >
            <button
              type="button"
              class="prep-item-card__toggle"
              :aria-expanded="isItemExpanded(`course-${course.courseId}`)"
              @click="toggleItemDetail(`course-${course.courseId}`)"
            >
              <div class="prep-item-card__header">
                <div class="prep-item-card__title-wrap">
                  <strong class="prep-item-card__title">
                    {{ course.courseName }}
                  </strong>
                </div>

                <span
                  class="prep-item-card__arrow"
                  :class="{
                    'is-expanded': isItemExpanded(`course-${course.courseId}`),
                  }"
                  aria-hidden="true"
                >
                  ⌄
                </span>
              </div>

              <div class="prep-item-card__next">
                <span class="prep-item-card__next-label"> 수강 정보 </span>

                <strong class="prep-item-card__fallback">
                  온라인 수강 가능
                </strong>

                <span v-if="course.providerName" class="prep-item-card__date">
                  {{ course.providerName }}
                </span>
              </div>
            </button>

            <!-- 펼친 강의 정보 -->
            <div
              v-if="isItemExpanded(`course-${course.courseId}`)"
              class="prep-item-card__detail"
            >
              <div class="prep-item-card__detail-heading">
                <strong>강의 정보</strong>
              </div>

              <div class="prep-info-list">
                <div v-if="course.providerName" class="prep-info-list__item">
                  <span>제공처</span>

                  <strong>
                    {{ course.providerName }}
                  </strong>
                </div>

                <div
                  v-if="course.benefitDetail"
                  class="prep-info-list__item prep-info-list__item--column"
                >
                  <span>혜택</span>

                  <strong>
                    {{ course.benefitDetail }}
                  </strong>
                </div>
              </div>

              <button
                v-if="course.detailUrl"
                type="button"
                class="prep-item-card__external"
                @click.stop="openExternalLink(course.detailUrl)"
              >
                강의 바로가기
                <span aria-hidden="true">↗</span>
              </button>
            </div>
          </BaseCard>
        </div>

        <!-- Empty -->
        <div
          v-if="qualifications.length === 0 && courses.length === 0"
          class="job-detail__empty"
        >
          선택한 준비 항목이 없습니다.
        </div>
      </section>

      <!-- ==================================================
           2. 비용 계산
      ================================================== -->
      <section v-else-if="activeTab === 'cost'" class="job-detail__tab-content">
        <!-- 비용 구성 -->
        <BaseCard padding="18px" class="cost-card">
          <h2 class="detail-section__title text-label">비용 구성</h2>

          <div v-if="totalCost > 0" class="cost-bar">
            <div
              class="cost-bar__qualification"
              :style="{
                width: `${qualificationRate}%`,
              }"
            ></div>

            <div
              class="cost-bar__course"
              :style="{
                width: `${courseRate}%`,
              }"
            ></div>
          </div>

          <div class="cost-list">
            <div class="cost-list__item">
              <div class="cost-list__label">
                <span class="cost-list__dot cost-list__dot--qualification" />

                <span>자격증·어학</span>
              </div>

              <div class="cost-list__amount">
                <strong>
                  {{ formatAmount(qualificationCost) }}
                </strong>

                <span> {{ qualificationRate }}% </span>
              </div>
            </div>

            <div class="cost-list__item">
              <div class="cost-list__label">
                <span class="cost-list__dot cost-list__dot--course" />

                <span>인터넷 강의</span>
              </div>

              <div class="cost-list__amount">
                <strong>
                  {{ formatAmount(courseCost) }}
                </strong>

                <span>{{ courseRate }}%</span>
              </div>
            </div>
          </div>

          <p v-if="totalCost > 0" class="cost-card__guide">
            {{
              qualificationCost >= courseCost
                ? '자격증·어학 준비 비용의 비중이 가장 커요'
                : '인터넷 강의 비용의 비중이 가장 커요'
            }}
          </p>
        </BaseCard>

        <!-- 상세 내역 -->
        <BaseCard padding="0" class="cost-detail-card">
          <button
            type="button"
            class="cost-detail-card__toggle"
            :aria-expanded="isCostDetailExpanded"
            @click="toggleCostDetail"
          >
            <div>
              <strong>상세 내역</strong>

              <span> {{ selectedItemCount }}건 </span>
            </div>

            <span
              class="cost-detail-card__arrow"
              :class="{
                'is-expanded': isCostDetailExpanded,
              }"
              aria-hidden="true"
            >
              ⌄
            </span>
          </button>

          <div v-if="isCostDetailExpanded" class="cost-detail-card__content">
            <!-- 자격증·어학 -->
            <div v-if="qualifications.length > 0" class="cost-detail-group">
              <strong class="cost-detail-group__title"> 자격증·어학 </strong>

              <div
                v-for="qualification in qualifications"
                :key="`cost-qualification-${qualification.qualId}`"
                class="cost-detail-row"
              >
                <span>
                  {{ qualification.qualName }}
                </span>

                <strong>
                  {{ formatAmount(qualification.selectedCost) }}
                </strong>
              </div>
            </div>

            <!-- 인터넷 강의 -->
            <div v-if="courses.length > 0" class="cost-detail-group">
              <strong class="cost-detail-group__title"> 인터넷 강의 </strong>

              <div
                v-for="course in courses"
                :key="`cost-course-${course.courseId}`"
                class="cost-detail-row"
              >
                <span>
                  {{ course.courseName }}
                </span>

                <strong>
                  {{ formatAmount(course.selectedCost) }}
                </strong>
              </div>
            </div>
          </div>
        </BaseCard>

        <!-- 준비비용 활용 분석 -->
        <BaseCard padding="18px" class="cost-analysis-card">
          <div class="cost-analysis-card__header">
            <div class="cost-analysis-card__icon" aria-hidden="true">💡</div>

            <div>
              <h2 class="cost-analysis-card__title">준비비용 활용 분석</h2>

              <p>소비 데이터를 준비비용과 비교해볼 수 있어요.</p>
            </div>
          </div>

          <!-- TODO: 후회소비 API 연동 후 표시 -->
          <template v-if="regretAnalysis">
            <div class="cost-analysis-card__amount">
              <span> 최근 1개월 후회 소비 금액 </span>

              <strong>
                {{ formatAmount(regretAnalysis.regretAmount) }}
              </strong>
            </div>

            <div
              v-if="regretAnalysis.affordableItems?.length > 0"
              class="cost-analysis-card__result"
            >
              <span>준비 가능한 항목</span>

              <div
                v-for="item in regretAnalysis.affordableItems"
                :key="item.itemId"
                class="cost-analysis-card__item"
              >
                <span aria-hidden="true">✓</span>

                <strong>
                  {{ item.itemName }}
                </strong>
              </div>

              <p v-if="regretAnalysis.affordableAmount">
                총
                <strong>
                  {{ formatAmount(regretAnalysis.affordableAmount) }}
                </strong>
                의 준비비용을 마련할 수 있어요.
              </p>
            </div>
          </template>

          <!-- 아직 타 도메인 연동 전 -->
          <div v-else class="cost-analysis-card__empty">
            후회소비 데이터 연동 후 준비 가능한 항목을 분석해드려요.
          </div>
        </BaseCard>
      </section>

      <!-- ==================================================
           3. 금융상품
      ================================================== -->
      <section v-else class="job-detail__tab-content">
        <!-- 지원 정책 -->
        <div v-if="policies.length > 0" class="detail-section">
          <h2 class="detail-section__title text-label">지원 정책</h2>

          <BaseCard
            v-for="policy in policies"
            :key="`${policy.productType}-${policy.productId}`"
            padding="16px"
            class="product-card"
          >
            <div class="product-card__header">
              <div class="product-card__content">
                <BaseTag
                  v-if="policy.badgeCode"
                  :label="getBadgeText(policy)"
                  :variant="getBadgeVariant(policy)"
                />

                <strong class="product-card__title">
                  {{ policy.productName }}
                </strong>

                <p v-if="policy.productDesc" class="product-card__description">
                  {{ policy.productDesc }}
                </p>
              </div>

              <button
                v-if="policy.linkUrl"
                type="button"
                class="external-link-button"
                :aria-label="`${policy.productName} 상세 페이지 열기`"
                @click="openExternalLink(policy.linkUrl)"
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
        </div>

        <!-- KB 서비스·상품 -->
        <div v-if="financialProducts.length > 0" class="detail-section">
          <h2 class="detail-section__title text-label">KB 서비스·상품</h2>

          <BaseCard
            v-for="product in financialProducts"
            :key="`${product.productType}-${product.productId}`"
            padding="16px"
            class="product-card"
          >
            <div class="product-card__header">
              <div class="product-card__content">
                <BaseTag
                  v-if="product.badgeCode"
                  :label="getBadgeText(product)"
                  :variant="getBadgeVariant(product)"
                />

                <strong class="product-card__title">
                  {{ product.productName }}
                </strong>

                <p v-if="product.productDesc" class="product-card__description">
                  {{ product.productDesc }}
                </p>
              </div>

              <button
                v-if="product.linkUrl"
                type="button"
                class="external-link-button"
                :aria-label="`${product.productName} 상세 페이지 열기`"
                @click="openExternalLink(product.linkUrl)"
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
        </div>

        <div
          v-if="policies.length === 0 && financialProducts.length === 0"
          class="job-detail__empty"
        >
          추천된 정책 및 금융상품이 없습니다.
        </div>
      </section>
    </template>

    <!-- 하단 버튼 -->
    <BottomButtonBar
      secondary-label="삭제"
      primary-label="확인"
      :secondary-disabled="loading || deleting"
      :primary-disabled="loading || deleting"
      @secondary-click="openDeleteModal"
      @primary-click="handleConfirm"
    />

    <!-- 삭제 확인 -->
    <BaseModal
      v-model="isDeleteModalOpen"
      title="진로 로드맵 삭제"
      confirm-text="삭제"
      @confirm="handleDelete"
    >
      <p class="job-detail__modal-message">
        이 진로 로드맵을 삭제하시겠습니까?
      </p>

      <p class="job-detail__modal-description">
        삭제한 로드맵은 목록에서 더 이상 확인할 수 없습니다.
      </p>
    </BaseModal>
  </div>
</template>

<style scoped>
.job-detail {
  padding: 0 20px 96px;
}

.job-detail__state {
  padding: 80px 0;
  color: var(--text-muted);
  text-align: center;
}

.job-detail__empty {
  padding: 44px 0;
  color: var(--text-muted);
  font-size: 13px;
  text-align: center;
}

/* ─────────────────────────────
   목표 요약
───────────────────────────── */

.goal-summary-card {
  margin: 16px 0 14px;
}

.goal-summary-card__header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.goal-summary-card__tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 5px 10px;

  background: var(--pastel-yellow);
  border-radius: 999px;

  color: var(--kb-dark-gray);
  font-size: 11px;
  font-weight: 700;
}

.goal-summary-card__title {
  color: var(--text-strong);
  font-size: 16px;
  font-weight: 700;
}

.goal-summary-card__date {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

.goal-summary-card__date span {
  color: var(--text-muted);
  font-size: 11px;
}

.goal-summary-card__date strong {
  color: var(--text-body);
  font-size: 13px;
  font-weight: 600;
}

.goal-summary-card__items {
  margin: 12px 0 0;
  color: var(--text-muted);
  font-size: 12px;
}

/* ─────────────────────────────
   총 예상 준비 비용
───────────────────────────── */

.total-cost-card {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
  background: var(--surface-cream);
  border: 0;
  box-shadow: none;
}

.total-cost-card__icon {
  display: flex;
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  background: var(--surface-default);
  border-radius: 10px;
}

.total-cost-card__icon-image {
  width: 26px;
  height: 26px;
  object-fit: contain;
}

.total-cost-card__content {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.total-cost-card__content p {
  margin: 0;
  color: var(--brand-gold);
  font-size: 12px;
  font-weight: 600;
}

.total-cost-card__content strong {
  margin-top: 3px;
  color: var(--kb-gray);
  font-size: 25px;
  font-weight: 700;
}

.total-cost-card__content span {
  margin-top: 2px;
  color: var(--text-muted);
  font-size: 10px;
}

/* ─────────────────────────────
   탭
───────────────────────────── */

.tab-row {
  display: flex;
  gap: 4px;
  border-bottom: 1px solid var(--line);
}

.tab-button {
  flex: 1;
  padding: 12px 0;
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 600;
  background: transparent;
  border: 0;
  border-bottom: 2px solid transparent;
  cursor: pointer;
}

.tab-button.is-active {
  color: var(--text-strong);
  border-bottom-color: var(--kb-yellow-deep);
}

.job-detail__tab-content {
  padding: 22px 0 16px;
}

.detail-section + .detail-section {
  margin-top: 24px;
}

.detail-section__title {
  margin: 0 0 12px;
  color: var(--kb-dark-gray);
}

/* ─────────────────────────────
   준비 항목 아코디언
───────────────────────────── */

.prep-item-card + .prep-item-card {
  margin-top: 12px;
}

.prep-item-card {
  overflow: hidden;
}

.prep-item-card__toggle {
  width: 100%;
  padding: 16px;
  color: inherit;
  text-align: left;
  background: transparent;
  border: 0;
  cursor: pointer;
}

.prep-item-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.prep-item-card__title-wrap {
  display: flex;
  min-width: 0;
  flex: 1;
  align-items: center;
  gap: 8px;
}

.prep-item-card__title {
  overflow: hidden;
  color: var(--text-strong);
  font-size: 14px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.prep-item-card__arrow,
.cost-detail-card__arrow {
  flex-shrink: 0;
  color: var(--text-muted);
  font-size: 18px;
  transition: transform 0.2s ease;
}

.prep-item-card__arrow.is-expanded,
.cost-detail-card__arrow.is-expanded {
  transform: rotate(180deg);
}

.prep-item-card__next {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
}

.prep-item-card__next-label {
  display: block;
  margin-bottom: 5px;
  color: var(--text-muted);
  font-size: 10px;
}

.prep-item-card__next-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.prep-item-card__next-info strong,
.prep-item-card__fallback {
  color: var(--text-strong);
  font-size: 12px;
  font-weight: 600;
}

.prep-item-card__dday {
  padding: 2px 6px;
  color: var(--brand-gold);
  font-size: 10px;
  font-weight: 700;
  background: var(--surface-cream);
  border-radius: 999px;
}

.prep-item-card__date {
  display: block;
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 10px;
}

/* 펼친 상세 */

.prep-item-card__detail {
  padding: 16px;
  border-top: 1px solid var(--line);
}

.prep-item-card__summary {
  margin-bottom: 18px;
}

.prep-item-card__summary p {
  margin: 0;
  color: var(--text-body);
  font-size: 11px;
  line-height: 1.6;
}

.prep-item-card__summary span {
  display: block;
  margin-top: 6px;
  color: var(--text-muted);
  font-size: 10px;
}

.prep-item-card__detail-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.prep-item-card__detail-heading strong {
  color: var(--text-strong);
  font-size: 13px;
  font-weight: 700;
}

.prep-item-card__detail-heading span {
  color: var(--text-muted);
  font-size: 10px;
}

.schedule-list {
  display: flex;
  flex-direction: column;
}

.schedule-list__item {
  display: flex;
  gap: 10px;
  min-height: 46px;
}

.schedule-list__timeline {
  position: relative;
  display: flex;
  width: 10px;
  flex-shrink: 0;
  justify-content: center;
}

.schedule-list__dot {
  position: relative;
  z-index: 1;
  width: 7px;
  height: 7px;
  margin-top: 4px;
  background: var(--brand-gold);
  border-radius: 50%;
}

.schedule-list__line {
  position: absolute;
  top: 11px;
  bottom: -4px;
  width: 1px;
  background: var(--line);
}

.schedule-list__content {
  display: flex;
  min-width: 0;
  flex: 1;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.schedule-list__content strong {
  color: var(--text-body);
  font-size: 11px;
  font-weight: 600;
}

.schedule-list__content span {
  flex-shrink: 0;
  color: var(--text-muted);
  font-size: 10px;
}

.prep-item-card__notice {
  margin: 8px 0 0;
  padding: 10px 12px;
  color: var(--text-muted);
  font-size: 10px;
  line-height: 1.5;
  background: var(--surface-cream);
  border-radius: 8px;
}

.prep-item-card__empty {
  margin: 0;
  color: var(--text-muted);
  font-size: 11px;
}

.prep-info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.prep-info-list__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.prep-info-list__item--column {
  align-items: flex-start;
  flex-direction: column;
  gap: 5px;
}

.prep-info-list__item span {
  color: var(--text-muted);
  font-size: 11px;
}

.prep-info-list__item strong {
  color: var(--text-body);
  font-size: 11px;
  font-weight: 600;
  line-height: 1.5;
}

.prep-item-card__external {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: flex-end;
  gap: 5px;
  margin-top: 16px;
  padding: 0;
  color: var(--text-body);
  font-size: 11px;
  font-weight: 600;
  background: transparent;
  border: 0;
  cursor: pointer;
}

/* ─────────────────────────────
   비용 구성
───────────────────────────── */

.cost-card {
  margin-bottom: 14px;
}

.cost-bar {
  display: flex;
  width: 100%;
  height: 22px;
  overflow: hidden;
  margin-top: 18px;
  border-radius: 8px;
}

.cost-bar__qualification,
.cost-bar__course {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
}

.cost-bar__qualification {
  color: var(--surface-default);
  background: var(--kb-gray);
}

.cost-bar__course {
  color: var(--text-body);
  background: var(--chart-4);
}

.cost-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.cost-list__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cost-list__label,
.cost-list__amount {
  display: flex;
  align-items: center;
}

.cost-list__label {
  gap: 8px;
  color: var(--text-body);
  font-size: 12px;
}

.cost-list__amount {
  gap: 7px;
}

.cost-list__amount strong {
  color: var(--text-strong);
  font-size: 12px;
}

.cost-list__amount span {
  color: var(--brand-gold);
  font-size: 11px;
}

.cost-list__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.cost-list__dot--qualification {
  background: var(--kb-gray);
}

.cost-list__dot--course {
  background: var(--chart-4);
}

.cost-card__guide {
  margin: 18px 0 0;
  padding: 10px 12px;
  color: var(--brand-gold);
  font-size: 11px;
  text-align: center;
  background: var(--surface-cream);
  border-radius: 8px;
}

/* ─────────────────────────────
   비용 상세 내역
───────────────────────────── */

.cost-detail-card {
  overflow: hidden;
  margin-bottom: 14px;
}

.cost-detail-card__toggle {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  color: inherit;
  background: transparent;
  border: 0;
  cursor: pointer;
}

.cost-detail-card__toggle > div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cost-detail-card__toggle strong {
  color: var(--text-strong);
  font-size: 13px;
}

.cost-detail-card__toggle span {
  color: var(--text-muted);
  font-size: 11px;
}

.cost-detail-card__content {
  padding: 0 18px 18px;
  border-top: 1px solid var(--line);
}

.cost-detail-group {
  padding-top: 16px;
}

.cost-detail-group + .cost-detail-group {
  margin-top: 4px;
  border-top: 1px solid var(--line);
}

.cost-detail-group__title {
  display: block;
  margin-bottom: 10px;
  color: var(--text-muted);
  font-size: 10px;
  font-weight: 600;
}

.cost-detail-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.cost-detail-row + .cost-detail-row {
  margin-top: 10px;
}

.cost-detail-row span {
  overflow: hidden;
  color: var(--text-body);
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cost-detail-row strong {
  flex-shrink: 0;
  color: var(--text-strong);
  font-size: 11px;
}

/* ─────────────────────────────
   준비비용 활용 분석
───────────────────────────── */

.cost-analysis-card {
  margin-bottom: 14px;
}

.cost-analysis-card__header {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.cost-analysis-card__icon {
  display: flex;
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  background: var(--surface-cream);
  border-radius: 8px;
}

.cost-analysis-card__title {
  margin: 0;
  color: var(--text-strong);
  font-size: 13px;
  font-weight: 700;
}

.cost-analysis-card__header p {
  margin: 4px 0 0;
  color: var(--text-muted);
  font-size: 10px;
}

.cost-analysis-card__amount {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--line);
}

.cost-analysis-card__amount span {
  color: var(--text-muted);
  font-size: 11px;
}

.cost-analysis-card__amount strong {
  color: var(--text-strong);
  font-size: 20px;
}

.cost-analysis-card__result {
  margin-top: 16px;
  padding: 14px;
  background: var(--surface-cream);
  border-radius: 10px;
}

.cost-analysis-card__result > span {
  display: block;
  margin-bottom: 10px;
  color: var(--text-muted);
  font-size: 10px;
}

.cost-analysis-card__item {
  display: flex;
  align-items: center;
  gap: 7px;
}

.cost-analysis-card__item + .cost-analysis-card__item {
  margin-top: 7px;
}

.cost-analysis-card__item span {
  color: var(--brand-gold);
  font-size: 11px;
  font-weight: 700;
}

.cost-analysis-card__item strong {
  color: var(--text-body);
  font-size: 11px;
}

.cost-analysis-card__result p {
  margin: 12px 0 0;
  color: var(--text-body);
  font-size: 11px;
  line-height: 1.5;
}

.cost-analysis-card__empty {
  margin-top: 16px;
  padding: 14px;
  color: var(--text-muted);
  font-size: 11px;
  line-height: 1.6;
  text-align: center;
  background: var(--surface-cream);
  border-radius: 10px;
}

/* ─────────────────────────────
   정책·금융상품
───────────────────────────── */

.product-card + .product-card {
  margin-top: 12px;
}

.product-card :deep(.base-tag) {
  padding: 2px 8px;
  font-size: 10px;
}

.product-card__header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.product-card__content {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  align-items: flex-start;
}

.product-card__title {
  margin-top: 8px;
  color: var(--text-strong);
  font-size: 14px;
  font-weight: 700;
}

.product-card__description {
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 11px;
  line-height: 1.5;
}

.external-link-button {
  display: flex;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  padding: 0;

  color: var(--kb-gray);
  background: transparent;
  border: 0;
  cursor: pointer;
}

.external-link-icon {
  width: 18px;
  height: 18px;

  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

/* ─────────────────────────────
   삭제 모달
───────────────────────────── */

.job-detail__modal-message {
  margin: 0;
  color: var(--text-strong);
  font-size: 14px;
  text-align: center;
}

.job-detail__modal-description {
  margin: 8px 0 0;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.5;
  text-align: center;
}
</style>
