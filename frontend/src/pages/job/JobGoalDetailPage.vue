<script setup>
// SCR-JOB-05 · 진로 로드맵 상세 조회 (담당: 지원)
// 저장된 진로 목표·준비항목·비용·정책/금융상품 조회

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import jobApi from '@/api/jobApi';
import regretApi from '@/api/regretApi';

import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import TabBar from '@/components/common/TabBar.vue';
import GoalSummaryCard from '@/components/common/GoalSummaryCard.vue';
import EstimatedCostCard from '@/components/common/EstimatedCostCard.vue';

import calculatorImage from '@/assets/images/calculator.png';
import jobSpendingAvgIcon from '@/assets/images/job-spending-avg.png';
import jobCostIcon from '@/assets/images/job-cost.png';

import { formatWon } from '@/util/format';
import { useToast } from '@/composables/useToast';

const route = useRoute();
const router = useRouter();
const { show } = useToast();

const goalId = computed(() => Number(route.params.goalId));

const loading = ref(false);
const detail = ref(null);
const loadError = ref('');

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

// 최근 1개월 후회소비 기반 준비비용 활용 분석
const regretAnalysis = ref(null);

const isValidGoalId = computed(
  () => Number.isInteger(goalId.value) && goalId.value > 0,
);

// ─────────────────────────────────────────────
// 상세 데이터
// ─────────────────────────────────────────────
const qualifications = computed(() => detail.value?.qualifications ?? []);

const courses = computed(() => detail.value?.courses ?? []);

const trainings = computed(() => detail.value?.trainings ?? []);

const isEmployment = computed(() => detail.value?.goalType === 'J01');

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

// 훈련과정 비용
const trainingCost = computed(() =>
  trainings.value.reduce((sum, item) => sum + Number(item.selfPayment ?? 0), 0),
);

// 총 예상 준비 비용
const totalCost = computed(
  () => qualificationCost.value + courseCost.value + trainingCost.value,
);

// 선택 준비 항목 개수
const selectedItemCount = computed(
  () =>
    qualifications.value.length + courses.value.length + trainings.value.length,
);

// 비용 비율
const qualificationRate = computed(() => {
  if (totalCost.value <= 0) {
    return 0;
  }

  return Math.round((qualificationCost.value / totalCost.value) * 100);
});

const courseRate = computed(() => {
  if (totalCost.value <= 0) return 0;

  return Math.round((courseCost.value / totalCost.value) * 100);
});

const trainingRate = computed(() => {
  if (totalCost.value <= 0) return 0;

  return Math.round((trainingCost.value / totalCost.value) * 100);
});

// 가장 비중이 큰 준비 비용 안내
const largestCostMessage = computed(() => {
  if (totalCost.value <= 0) {
    return '';
  }

  const items = [
    {
      label: '자격증·어학 준비 비용',
      amount: qualificationCost.value,
    },
    {
      label: '인터넷 강의 비용',
      amount: courseCost.value,
    },
  ];

  // 취업 목표일 때만 훈련과정 포함
  if (isEmployment.value) {
    items.push({
      label: '훈련과정 비용',
      amount: trainingCost.value,
    });
  }

  const maxAmount = Math.max(...items.map((item) => item.amount));

  const largestItems = items.filter((item) => item.amount === maxAmount);

  if (largestItems.length > 1) {
    return '준비 항목의 비용 비중이 같아요';
  }

  return `${largestItems[0].label}의 비중이 가장 커요`;
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

// 상단 요약 카드 칩: 목표 유형(취업/공무원/편입)
const goalTypeLabel = computed(() => {
  const type = detail.value?.goalType;

  if (type === 'J01') return '취업';
  if (type === 'J02') return '공무원';
  if (type === 'J03') return '편입';

  return '진로';
});

// YYYY-MM → YYYY.MM
const formattedExpectedDate = computed(() =>
  (detail.value?.expectedDate ?? '').replace('-', '.'),
);

// 대분류·중분류 계층 — 희망 직무/직렬(대분류)을 categoryId의 부모로 역참조
const categoryList = ref([]);
const leafCategory = computed(
  () =>
    categoryList.value.find(
      (c) => Number(c.categoryId) === Number(detail.value?.categoryId),
    ) || null,
);
const bigCategoryName = computed(() =>
  leafCategory.value
    ? categoryList.value.find(
        (c) => Number(c.categoryId) === Number(leafCategory.value.parentId),
      )?.categoryName || ''
    : '',
);

// 훈련 지역 — 선택한 훈련과정 주소의 시도 (step2에서 고른 지역)
const trainingRegion = computed(() => {
  const address = trainings.value[0]?.address;

  return address ? address.split(' ')[0] : '미정';
});

// 상단 요약 카드 스펙 (step3 요약 카드와 동일: 취업 6개 / 공무원·편입 4개)
const heroSpecs = computed(() => {
  const type = detail.value?.goalType;
  const qualSpec = { label: '자격증·어학', value: `${qualifications.value.length}개` };
  const courseSpec = { label: '인터넷 강의', value: `${courses.value.length}개` };

  if (type === 'J03') {
    return [
      { label: '편입 시기', value: formattedExpectedDate.value || '-' },
      { label: '희망 대학', value: detail.value?.univName || '미정' },
      qualSpec,
      courseSpec,
    ];
  }

  if (type === 'J02') {
    return [
      { label: '합격 시기', value: formattedExpectedDate.value || '-' },
      { label: '희망 직렬', value: bigCategoryName.value || goalTitle.value || '미정' },
      qualSpec,
      courseSpec,
    ];
  }

  // 취업 J01 — 6개
  return [
    { label: '취업 시기', value: formattedExpectedDate.value || '-' },
    { label: '희망 직무', value: bigCategoryName.value || goalTitle.value || '미정' },
    { label: '훈련 지역', value: trainingRegion.value },
    { label: '훈련 과정', value: `${trainings.value.length}개` },
    { label: '자격증', value: `${qualifications.value.length}개` },
    { label: '인강', value: `${courses.value.length}개` },
  ];
});

// ── step3와 동일한 비용 표·그래프 (3개월 비교 + 물통 + 알약 상세내역) ──
// 최근 3개월 월평균 지출 (준비비용 비교용)
const averageMonthlySpending = ref(0);
const loadAverageMonthlySpending = async () => {
  try {
    const summary = await regretApi.getSpendingSummary(3);
    averageMonthlySpending.value = Number(summary?.avgMonthlySpending ?? 0);
  } catch (error) {
    console.error('최근 3개월 월평균 지출 조회 실패:', error);
    averageMonthlySpending.value = 0;
  }
};

const spendingRate = computed(() =>
  averageMonthlySpending.value === 0
    ? 0
    : Math.round((totalCost.value / averageMonthlySpending.value) * 100),
);

// GoalSummaryCard 비교 (3개월 월평균 지출 vs 예상 준비비용)
const summaryCompare = computed(() => {
  const hasSpending = averageMonthlySpending.value > 0;
  const withinSpending =
    hasSpending && totalCost.value <= averageMonthlySpending.value;

  return {
    left: {
      label: '3개월 월평균 지출',
      value: formatWon(averageMonthlySpending.value),
      icon: jobSpendingAvgIcon,
    },
    right: {
      label: '예상 준비비용',
      value: formatWon(totalCost.value),
      icon: jobCostIcon,
    },
    badge: hasSpending
      ? { text: `지출의 ${spendingRate.value}%`, tone: withinSpending ? 'good' : 'bad' }
      : null,
  };
});

// 물통(EstimatedCostCard) 데이터
const COST_STYLES = [
  { color: '#6b5b4d', ink: '#ffffff' }, // 자격증·어학
  { color: '#a68b73', ink: '#ffffff' }, // 인터넷 강의
  { color: '#e5c558', ink: '#5b4b2e' }, // 훈련과정
];
const costCardItems = computed(() => {
  const base = [
    { label: '자격증·어학', amount: qualificationCost.value, percent: qualificationRate.value },
    { label: '인터넷 강의', amount: courseCost.value, percent: courseRate.value },
  ];
  if (isEmployment.value) {
    base.push({ label: '훈련과정', amount: trainingCost.value, percent: trainingRate.value });
  }
  return base.map((it, i) => ({
    label: it.label,
    amountText: formatWon(it.amount),
    percent: it.percent,
    color: COST_STYLES[i].color,
    ink: COST_STYLES[i].ink,
    showName: it.percent >= 25,
  }));
});
const costState = computed(() => ({
  headLabel: '총 예상 준비비용',
  amount: totalCost.value,
  unit: '원',
  items: costCardItems.value,
  hint: largestCostMessage.value,
  hintTone: 'g',
}));

// 상세 내역 그룹 (선택 항목)
const detailGroups = computed(() => {
  const groups = [];
  if (qualifications.value.length) {
    groups.push({
      title: '자격증·어학',
      items: qualifications.value.map((q) => ({ name: q.qualName, amount: Number(q.selectedCost ?? 0) })),
    });
  }
  if (courses.value.length) {
    groups.push({
      title: '인터넷 강의',
      items: courses.value.map((c) => ({ name: c.courseName, amount: Number(c.selectedCost ?? 0) })),
    });
  }
  if (isEmployment.value && trainings.value.length) {
    groups.push({
      title: '훈련과정',
      items: trainings.value.map((t) => ({ name: t.trainingName, amount: Number(t.selfPayment ?? 0) })),
    });
  }
  return groups;
});

// 비용 알약 탭
const COST_TABS = [
  { label: '준비 예상 비용', value: 'cost' },
  { label: '상세 내역', value: 'detail' },
];
const costTab = ref('cost');

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

    // 희망 직무/직렬(대분류) 표시용 카테고리 계층 조회 (편입 J03은 카테고리 없음)
    const type = detail.value?.goalType;
    if (type === 'J01' || type === 'J02') {
      try {
        categoryList.value = (await jobApi.findCategoryList(type)) ?? [];
      } catch {
        categoryList.value = [];
      }
    }
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
// 최근 1개월 후회소비 기반 준비비용 활용 분석
// ─────────────────────────────────────────────

const loadRegretAnalysis = async () => {
  try {
    // 로그인 사용자의 전체 지출 내역 조회
    const spendings = await regretApi.findSpendings();

    const today = new Date();
    const threeMonthsAgo = new Date(today);

    // 오늘 기준 3개월 전 날짜 계산
    threeMonthsAgo.setMonth(threeMonthsAgo.getMonth() - 3);

    // 최근 3개월 내 후회소비(REGRET)만 합산
    const regretAmount = spendings
      .filter((spending) => {
        const spentAt = new Date(spending.spentAt);

        return (
          spentAt >= threeMonthsAgo &&
          spentAt <= today &&
          spending.reviewType === 'REGRET'
        );
      })
      .reduce((total, spending) => total + Number(spending.amount ?? 0), 0);

    // 후회소비 금액으로 활용 가능한 준비비용 후보 구성
    const prepItems = [];

    // 자격증·어학
    qualifications.value.forEach((item) => {
      const writtenFee = Number(item.writtenFee ?? 0);
      const practicalFee = Number(item.practicalFee ?? 0);
      const selectedCost = Number(item.selectedCost ?? 0);

      // 1순위: 필기 응시료
      if (writtenFee > 0) {
        prepItems.push({
          itemId: `qualification-written-${item.qualId}`,
          itemName: `${item.qualName} 필기 응시료`,
          amount: writtenFee,
          priority: 1,
        });
      }

      // 2순위: 실기 응시료
      if (practicalFee > 0) {
        prepItems.push({
          itemId: `qualification-practical-${item.qualId}`,
          itemName: `${item.qualName} 실기 응시료`,
          amount: practicalFee,
          priority: 2,
        });
      }

      // 필기·실기 금액이 따로 없는 경우 선택한 전체 비용 사용
      if (writtenFee === 0 && practicalFee === 0 && selectedCost > 0) {
        prepItems.push({
          itemId: `qualification-${item.qualId}`,
          itemName: `${item.qualName} 준비비용`,
          amount: selectedCost,
          priority: 3,
        });
      }
    });

    // 인터넷 강의
    courses.value.forEach((item) => {
      const selectedCost = Number(item.selectedCost ?? 0);

      if (selectedCost > 0) {
        prepItems.push({
          itemId: `course-${item.courseId}`,
          itemName: `${item.courseName} 수강료`,
          amount: selectedCost,
          priority: 4,
        });
      }
    });

    // 후회소비 금액으로 전액 마련 가능한 항목만 조회
    // 필기 → 실기 → 자격증 전체비용 → 인강 순으로 우선 추천
    const affordableItems = prepItems
      .filter((item) => item.amount <= regretAmount)
      .sort((a, b) => {
        if (a.priority !== b.priority) {
          return a.priority - b.priority;
        }

        return a.amount - b.amount;
      });

    let recommendationMessage = '';
    let targetItemName = null;
    let targetItemAmount = 0;

    // ① 최근 1개월 후회소비가 없는 경우
    if (regretAmount === 0) {
      recommendationMessage =
        '지금의 소비 습관을 유지하면서 진로 준비를 이어가보세요.';
    }

    // ② 후회소비 금액으로 준비항목 하나 이상 마련 가능한 경우
    else if (affordableItems.length > 0) {
      const targetItem = affordableItems[0];

      targetItemName = targetItem.itemName;
      targetItemAmount = targetItem.amount;
    }

    // ③ 후회소비는 있지만 준비항목 비용보다 적은 경우
    else {
      recommendationMessage =
        '작은 금액도 모이면 진로 준비에 도움이 돼요.\n다음 준비비용을 위해 모아보는 건 어떨까요?';
    }

    regretAnalysis.value = {
      regretAmount,
      recommendationMessage,
      targetItemName,
      targetItemAmount,
    };
  } catch (error) {
    console.error('후회소비 준비비용 활용 분석 실패:', error);
    regretAnalysis.value = null;
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

const handleConfirm = async () => {
  await router.push({
    name: 'RoadmapMain',
  });
};

const goToRecommend = () =>
  router.push({ name: 'JobRecommend', params: { goalId: goalId.value } });

onMounted(async () => {
  // 진로 목표 상세 조회
  await loadDetail();

  if (detail.value) {
    // step3와 동일하게 최근 3개월 월평균 지출로 준비비용을 비교
    loadAverageMonthlySpending();
    // 최근 3개월 후회소비 기반 준비비용 활용 분석 복구
    loadRegretAnalysis();
  }
});
</script>

<template>
  <div class="job-detail">
    <!-- 헤더: 자취 상세와 동일하게 PageHeader + 도메인 태그 한 줄 -->
    <header class="job-detail__head">
      <PageHeader breadcrumb="저장한 로드맵" title="나의 진로 작전" />
      <BaseTag label="진로" variant="job" />
    </header>

    <!-- 로딩 -->
    <div v-if="loading" class="job-detail__state text-caption">
      불러오는 중...
    </div>

    <!-- 조회 오류 -->
    <p v-else-if="loadError" class="form-error text-caption" role="alert">
      {{ loadError }}
    </p>

    <template v-else-if="detail">
      <!-- 상단 요약 카드 (step3와 동일: 칩 + 제목 + 스펙 + 3개월 지출 비교) -->
      <GoalSummaryCard
        theme="job"
        :chip="goalTypeLabel"
        :title="goalTitle"
        :specs="heroSpecs"
        :compare="summaryCompare"
      />

      <TabBar
        v-model="activeTab"
        :tabs="TABS.map((tab) => ({ label: tab.label, value: tab.key }))"
      />

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

        <!-- 훈련과정 -->
        <div v-if="isEmployment && trainings.length > 0" class="detail-section">
          <h2 class="detail-section__title text-label">훈련과정</h2>

          <BaseCard
            v-for="training in trainings"
            :key="`${training.externalCode}-${training.trainingRound}`"
            padding="0"
            class="prep-item-card"
          >
            <button
              type="button"
              class="prep-item-card__toggle"
              :aria-expanded="
                isItemExpanded(
                  `training-${training.externalCode}-${training.trainingRound}`,
                )
              "
              @click="
                toggleItemDetail(
                  `training-${training.externalCode}-${training.trainingRound}`,
                )
              "
            >
              <div class="prep-item-card__header">
                <div class="prep-item-card__title-wrap">
                  <strong class="prep-item-card__title">
                    {{ training.trainingName }}
                  </strong>
                </div>

                <span
                  class="prep-item-card__arrow"
                  :class="{
                    'is-expanded': isItemExpanded(
                      `training-${training.externalCode}-${training.trainingRound}`,
                    ),
                  }"
                  aria-hidden="true"
                >
                  ⌄
                </span>
              </div>

              <div class="prep-item-card__next">
                <span class="prep-item-card__next-label"> 훈련 정보 </span>

                <strong class="prep-item-card__fallback">
                  {{ training.institutionName || '고용24 훈련과정' }}
                </strong>

                <span v-if="training.startDate" class="prep-item-card__date">
                  {{ formatDate(training.startDate) }}
                  <template v-if="training.endDate">
                    ~ {{ formatDate(training.endDate) }}
                  </template>
                </span>
              </div>
            </button>

            <div
              v-if="
                isItemExpanded(
                  `training-${training.externalCode}-${training.trainingRound}`,
                )
              "
              class="prep-item-card__detail"
            >
              <div class="prep-item-card__detail-heading">
                <strong>훈련과정 정보</strong>

                <span v-if="training.trainingType">
                  {{ training.trainingType }}
                </span>
              </div>

              <div class="prep-info-list">
                <div
                  v-if="training.institutionName"
                  class="prep-info-list__item"
                >
                  <span>훈련기관</span>
                  <strong>{{ training.institutionName }}</strong>
                </div>

                <div v-if="training.address" class="prep-info-list__item">
                  <span>훈련지역</span>
                  <strong>{{ training.address }}</strong>
                </div>

                <div class="prep-info-list__item">
                  <span>전체 훈련비</span>
                  <strong>{{ formatAmount(training.trainingCost) }}</strong>
                </div>

                <div class="prep-info-list__item">
                  <span>본인부담금</span>
                  <strong>{{ formatAmount(training.selfPayment) }}</strong>
                </div>
              </div>

              <button
                v-if="training.detailUrl"
                type="button"
                class="prep-item-card__external"
                @click.stop="openExternalLink(training.detailUrl)"
              >
                고용24에서 자세히 보기
                <span aria-hidden="true">↗</span>
              </button>
            </div>
          </BaseCard>
        </div>

        <!-- Empty -->
        <div
          v-if="
            qualifications.length === 0 &&
            courses.length === 0 &&
            trainings.length === 0
          "
          class="job-detail__empty"
        >
          선택한 준비 항목이 없습니다.
        </div>
      </section>

      <!-- ==================================================
           2. 비용 계산
      ================================================== -->
      <section
        v-else-if="activeTab === 'cost'"
        class="job-detail__tab-content job-detail__cost"
      >
        <!-- 물통(총 예상 준비비용) — 상세 내역을 같은 카드 하단에 합침 -->
        <EstimatedCostCard :state="costState" :dividers="false" note="">
          <template #footer>
            <div class="cost-detail-foot">
              <button
                type="button"
                class="cost-detail-foot__toggle"
                :aria-expanded="isCostDetailExpanded"
                @click="toggleCostDetail"
              >
                <span class="cost-detail-foot__label">
                  상세 내역 <em>{{ selectedItemCount }}건</em>
                </span>
                <span
                  class="cost-detail-foot__arrow"
                  :class="{ 'is-open': isCostDetailExpanded }"
                  aria-hidden="true"
                >⌄</span>
              </button>

              <div v-if="isCostDetailExpanded" class="cost-detail-foot__content">
                <div
                  v-for="group in detailGroups"
                  :key="group.title"
                  class="cost-detail-group"
                >
                  <strong class="cost-detail-group__title">{{ group.title }}</strong>
                  <div
                    v-for="(item, index) in group.items"
                    :key="index"
                    class="cost-detail-row"
                  >
                    <span>{{ item.name }}</span>
                    <strong>{{ formatAmount(item.amount) }}</strong>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </EstimatedCostCard>

        <!-- 준비비용 활용 분석 (최근 3개월 후회소비 연결) -->
        <BaseCard padding="18px" class="cost-analysis-card">
          <div class="cost-analysis-card__header">
            <div class="cost-analysis-card__icon" aria-hidden="true">💡</div>
            <div>
              <h2 class="cost-analysis-card__title">준비비용 활용 분석</h2>
              <p>소비 습관을 진로 준비와 연결해봤어요.</p>
            </div>
          </div>

          <template v-if="regretAnalysis">
            <div class="cost-analysis-card__amount">
              <p v-if="regretAnalysis.regretAmount > 0">
                최근 3개월간
                <strong>{{ formatAmount(regretAnalysis.regretAmount) }}</strong>
                을 후회소비로 사용했어요.
              </p>
              <p v-else>최근 3개월간 후회소비로 기록된 지출이 없어요.</p>
            </div>

            <div class="cost-analysis-card__result">
              <template v-if="regretAnalysis.targetItemName">
                <p class="cost-analysis-card__result-label">
                  다음에는 후회소비 대신
                </p>
                <div class="cost-analysis-card__target">
                  <strong class="cost-analysis-card__target-name">
                    {{ regretAnalysis.targetItemName }}
                  </strong>
                  <strong class="cost-analysis-card__target-amount">
                    {{ formatAmount(regretAnalysis.targetItemAmount) }}
                  </strong>
                </div>
                <p class="cost-analysis-card__result-message">
                  을 마련해보는 건 어떨까요?
                </p>
              </template>
              <p v-else class="cost-analysis-card__result-message">
                {{ regretAnalysis.recommendationMessage }}
              </p>
            </div>
          </template>

          <div v-else class="cost-analysis-card__empty">
            후회소비 데이터를 불러오지 못했어요.
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

      <p class="job-detail__back text-caption" @click="goToRecommend">
        추천 목록 다시 보기
      </p>
    </template>

    <!-- 하단 버튼 (자취 상세와 동일하게 확인만) -->
    <BottomButtonBar
      primary-label="확인"
      :primary-disabled="loading"
      @primary-click="handleConfirm"
    />
  </div>
</template>

<style scoped>
.job-detail {
  padding: 20px 20px 96px;
}

/* 헤더: PageHeader + 도메인 태그 한 줄 (자취 상세와 동일) */
.job-detail__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

/* 추천 목록 다시 보기 (오른쪽 정렬) */
.job-detail__back {
  margin: 4px 2px 0;
  color: var(--text-muted);
  text-align: right;
  text-decoration: underline;
  cursor: pointer;
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
   탭
───────────────────────────── */

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

.prep-item-card__arrow {
  flex-shrink: 0;
  color: var(--text-muted);
  font-size: 18px;
  transition: transform 0.2s ease;
}

.prep-item-card__arrow.is-expanded {
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
   비용 상세 내역
───────────────────────────── */

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
  font-size: 15px;
  font-weight: 700;
}

.cost-analysis-card__header p {
  margin: 4px 0 0;
  color: var(--text-muted);
  font-size: 10px;
}

.cost-analysis-card__amount {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--line);
}

.cost-analysis-card__amount p {
  margin: 0;
  color: var(--text-body);
  font-size: 12px;
  line-height: 1.6;
}

.cost-analysis-card__amount strong {
  color: var(--text-strong);
  font-size: 16px;
  font-weight: 700;
}

.cost-analysis-card__result {
  margin-top: 16px;
  padding: 16px;
  background: var(--surface-cream);
  border-radius: 10px;
}

.cost-analysis-card__result-label,
.cost-analysis-card__result-message {
  margin: 0;
  color: var(--text-body);
  font-size: 12px;
  font-weight: 500;
  line-height: 1.6;
  white-space: pre-line;
}

/* 추천 준비항목 */
.cost-analysis-card__target {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin: 8px 0;
}

.cost-analysis-card__target-name {
  color: var(--text-strong);
  font-size: 14px;
  font-weight: 700;
  line-height: 1.5;
}

.cost-analysis-card__target-amount {
  flex-shrink: 0;
  color: var(--brand-gold);
  font-size: 17px;
  font-weight: 700;
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

/* ── 비용 탭: 3개월 지출 비교 ── */
/* 상단 요약 카드(GoalSummaryCard) 여백 */
.job-detail :deep(.goal-summary) {
  margin: 16px 0 14px;
}
/* 비용 탭 카드 간격 */
.job-detail__cost {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.job-detail__cost .cost-analysis-card {
  margin-bottom: 0;
}

/* 물통 하단 상세 내역(footer) */
.cost-detail-foot {
  border-top: 1px solid var(--line);
}
.cost-detail-foot__toggle {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: transparent;
  border: 0;
  cursor: pointer;
  font-family: inherit;
}
.cost-detail-foot__label {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-strong);
}
.cost-detail-foot__label em {
  margin-left: 4px;
  font-style: normal;
  font-weight: 600;
  color: var(--text-muted);
}
.cost-detail-foot__arrow {
  color: var(--text-muted);
  font-size: 18px;
  transition: transform 0.2s ease;
}
.cost-detail-foot__arrow.is-open {
  transform: rotate(180deg);
}
.cost-detail-foot__content {
  padding: 0 16px 16px;
}
</style>
