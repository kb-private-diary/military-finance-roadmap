<script setup>
// SCR-JOB-03 · step3) 진로 비용 계산 (담당: 지원)
// step3 - 선택한 자격증·어학 및 인강의 총 예상 비용 확인
// 자동차·여행 step3와 동일한 공용 컴포넌트(GoalSummaryCard + EstimatedCostCard)로 통일.
// 등록한 목표 유형(취업 J01 / 공무원 J02 / 편입 J03)에 따라 요약 카드 제목·스펙이 달라진다.
// 큰 분류(대분류)는 categoryName이 세부만 오므로, findCategoryList의 parentId로 조회한다.

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import GoalSummaryCard from '@/components/common/GoalSummaryCard.vue';
import EstimatedCostCard from '@/components/common/EstimatedCostCard.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import BaseCard from '@/components/common/BaseCard.vue';
import TabBar from '@/components/common/TabBar.vue';
import jobApi from '@/api/jobApi';
import { formatWon } from '@/util/format';
import regretApi from '@/api/regretApi';
import jobSpendingAvgIcon from '@/assets/images/job-spending-avg.png';
import jobCostIcon from '@/assets/images/job-cost.png';

const route = useRoute();
const router = useRouter();

const goalId = computed(() => Number(route.params.goalId));

// ── 조회 상태 ─────────────────────────────────────────────────
const goalType = ref('');
const qualifications = ref([]);
const courses = ref([]);
const trainings = ref([]);
const isLoading = ref(false);

// 목표 정보: categoryName = 세부 직무/직렬(구조 경채 등), categoryId = 세부 카테고리 id, univName·majorName = 편입
const categoryName = ref('');
const categoryId = ref(null);
const univName = ref('');
const majorName = ref('');
const expectedDate = ref('');

// 대분류·중분류 계층 (findCategoryList) — 큰 분류 이름을 parentId로 역참조
const categoryList = ref([]);

const isEmployment = computed(() => goalType.value === 'J01');

const goalTypeLabel = computed(() => {
  if (goalType.value === 'J01') return '취업';
  if (goalType.value === 'J02') return '공무원';
  if (goalType.value === 'J03') return '편입';

  return '진로';
});

// 선택한 세부 카테고리 + 그 부모(큰 분류)
const leafCategory = computed(
  () =>
    categoryList.value.find(
      (c) => Number(c.categoryId) === Number(categoryId.value),
    ) || null,
);
const parentCategory = computed(() =>
  leafCategory.value
    ? categoryList.value.find(
        (c) => Number(c.categoryId) === Number(leafCategory.value.parentId),
      ) || null
    : null,
);
// 세부 직무·직렬 이름 (계층에서 못 찾으면 detail의 categoryName 폴백)
const subCategoryName = computed(
  () => leafCategory.value?.categoryName || categoryName.value || '',
);
// 큰 분류 이름 (IT·개발 / 소방 등)
const bigCategoryName = computed(
  () => parentCategory.value?.categoryName || '',
);

// 훈련 지역 — 선택한 훈련과정 주소의 시도 (step2에서 고른 지역)
const trainingRegion = computed(() => {
  const address = trainings.value[0]?.address;

  return address ? address.split(' ')[0] : '미정';
});

// 목표 시기 "2027-03" → "2027년 3월"
const formattedExpectedDate = computed(() => {
  const raw = expectedDate.value;
  if (!raw) return '미정';

  const [year, month] = String(raw).split('-');

  return month ? `${year}년 ${Number(month)}월` : String(year);
});

// 제목: 취업·공무원=세부 직무/직렬명 / 편입=학과
const goalName = computed(() => {
  if (goalType.value === 'J03') {
    return majorName.value || '학과 미정';
  }

  // 취업·공무원: 선택한 세부 직무/직렬명 (예: AI·빅데이터 / 구조 경채)
  return subCategoryName.value || '진로 목표';
});

// 최근 3개월 월평균 지출 금액
const averageMonthlySpending = ref(0);

// ── 비용 계산 ─────────────────────────────────────────────────

// 자격증·어학 예상비용
const qualificationTotal = computed(() =>
  qualifications.value.reduce(
    (sum, item) => sum + Number(item.selectedCost ?? 0),
    0,
  ),
);

// 인터넷 강의 예상비용
const courseTotal = computed(() =>
  courses.value.reduce((sum, item) => sum + Number(item.selectedCost ?? 0), 0),
);

// 훈련과정 예상비용 (전체 훈련비가 아닌 본인부담금)
const trainingTotal = computed(() =>
  trainings.value.reduce((sum, item) => sum + Number(item.selfPayment ?? 0), 0),
);

// 총 예상 준비비용
const totalAmount = computed(
  () => qualificationTotal.value + courseTotal.value + trainingTotal.value,
);

// 비용 비율
const rateOf = (amount) =>
  totalAmount.value === 0 ? 0 : Math.round((amount / totalAmount.value) * 100);

const qualificationRate = computed(() => rateOf(qualificationTotal.value));
const courseRate = computed(() => rateOf(courseTotal.value));
const trainingRate = computed(() => rateOf(trainingTotal.value));

// 최근 3개월 월평균 지출 대비 예상 준비비용 비율
const spendingRate = computed(() => {
  if (averageMonthlySpending.value === 0) return 0;

  return Math.round((totalAmount.value / averageMonthlySpending.value) * 100);
});

// ── GoalSummaryCard 데이터 ────────────────────────────────────
// 스펙: 유형별로 시기·희망 직렬/대학·준비 항목 개수 (취업만 훈련과정 포함)
const summarySpecs = computed(() => {
  const qualSpec = {
    label: '자격증·어학',
    value: `${qualifications.value.length}개`,
  };
  const courseSpec = {
    label: '인터넷 강의',
    value: `${courses.value.length}개`,
  };

  if (goalType.value === 'J03') {
    return [
      { label: '편입 시기', value: formattedExpectedDate.value },
      { label: '희망 대학', value: univName.value || '미정' },
      qualSpec,
      courseSpec,
    ];
  }

  if (goalType.value === 'J02') {
    return [
      { label: '합격 시기', value: formattedExpectedDate.value },
      {
        label: '희망 직렬',
        value: bigCategoryName.value || subCategoryName.value || '미정',
      },
      qualSpec,
      courseSpec,
    ];
  }

  // 취업 (J01) — 세부 직무 제목 + 희망 직무(큰 분류)·훈련 지역
  return [
    { label: '취업 시기', value: formattedExpectedDate.value },
    {
      label: '희망 직무',
      value: bigCategoryName.value || subCategoryName.value || '미정',
    },
    { label: '훈련 지역', value: trainingRegion.value },
    { label: '훈련 과정', value: `${trainings.value.length}개` },
    { label: '자격증', value: `${qualifications.value.length}개` },
    { label: '인강', value: `${courses.value.length}개` },
  ];
});

// 비교: 최근 3개월 월평균 지출 vs 예상 준비비용 (지출 0이면 뱃지만 생략, 카드는 항상 표시)
const summaryCompare = computed(() => {
  const hasSpending = averageMonthlySpending.value > 0;
  const withinSpending =
    hasSpending && totalAmount.value <= averageMonthlySpending.value;

  return {
    left: {
      label: '3개월 월평균 지출',
      value: formatWon(averageMonthlySpending.value),
      icon: jobSpendingAvgIcon,
    },
    right: {
      label: '예상 준비비용',
      value: formatWon(totalAmount.value),
      icon: jobCostIcon,
    },
    badge: hasSpending
      ? {
          text: `지출의 ${spendingRate.value}%`,
          tone: withinSpending ? 'good' : 'bad',
        }
      : null,
  };
});

// ── EstimatedCostCard 데이터 (물통) ───────────────────────────
// 자취 비율바 팔레트 재사용 — 취업이면 훈련과정 추가
const COST_STYLES = [
  { color: '#6b5b4d', ink: '#ffffff' }, // 자격증·어학
  { color: '#a68b73', ink: '#ffffff' }, // 인터넷 강의
  { color: '#e5c558', ink: '#5b4b2e' }, // 훈련과정
];

const costCardItems = computed(() => {
  const base = [
    {
      label: '자격증·어학',
      amount: qualificationTotal.value,
      percent: qualificationRate.value,
    },
    {
      label: '인터넷 강의',
      amount: courseTotal.value,
      percent: courseRate.value,
    },
  ];

  if (isEmployment.value) {
    base.push({
      label: '훈련과정',
      amount: trainingTotal.value,
      percent: trainingRate.value,
    });
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
  amount: totalAmount.value,
  unit: '원',
  items: costCardItems.value,
}));

// ── 비용 탭 (알약형 세그먼트): 준비 예상 비용 / 상세 내역 ──────
const COST_TABS = [
  { label: '준비 예상 비용', value: 'cost' },
  { label: '상세 내역', value: 'detail' },
];
const costTab = ref('cost');

const detailGroups = computed(() => {
  const groups = [];

  if (qualifications.value.length) {
    groups.push({
      title: '자격증·어학',
      items: qualifications.value.map((q) => ({
        name: q.qualName,
        amount: Number(q.selectedCost ?? 0),
      })),
    });
  }

  if (courses.value.length) {
    groups.push({
      title: '인터넷 강의',
      items: courses.value.map((c) => ({
        name: c.courseName,
        amount: Number(c.selectedCost ?? 0),
      })),
    });
  }

  if (isEmployment.value && trainings.value.length) {
    groups.push({
      title: '훈련과정',
      items: trainings.value.map((t) => ({
        name: t.trainingName,
        amount: Number(t.selfPayment ?? 0),
      })),
    });
  }

  return groups;
});

// ── 목표 상세 조회 ────────────────────────────────────────────
const fetchJobGoalDetail = async () => {
  if (!goalId.value) {
    router.replace({ name: 'JobGoalCreate' });
    return;
  }

  try {
    isLoading.value = true;

    const detail = await jobApi.findJobGoalDetail(goalId.value);

    goalType.value = detail?.goalType ?? '';
    qualifications.value = detail?.qualifications ?? [];
    courses.value = detail?.courses ?? [];
    trainings.value = detail?.trainings ?? [];
    categoryName.value = detail?.categoryName ?? '';
    categoryId.value = detail?.categoryId ?? null;
    univName.value = detail?.univName ?? '';
    majorName.value = detail?.majorName ?? '';
    expectedDate.value = detail?.expectedDate ?? '';
  } catch (error) {
    console.error('진로 목표 상세 조회 실패:', error);

    router.replace({
      name: 'JobRecommend',
      params: { goalId: goalId.value },
    });
  } finally {
    isLoading.value = false;
  }
};

// ── 대분류·중분류 카테고리 계층 조회 (큰 분류 이름 역참조용) ────
const fetchCategoryList = async () => {
  // 편입(J03)은 대학·학과 체계라 직무 카테고리 없음
  if (!goalType.value || goalType.value === 'J03') {
    categoryList.value = [];
    return;
  }

  try {
    categoryList.value = (await jobApi.findCategoryList(goalType.value)) ?? [];
  } catch (error) {
    console.error('직무 카테고리 조회 실패:', error);
    categoryList.value = [];
  }
};

// ── 최근 3개월 월평균 지출 조회 ──────────────────────────────────
const fetchAverageMonthlySpending = async () => {
  try {
    const summary = await regretApi.getSpendingSummary(3);

    averageMonthlySpending.value = Number(summary?.avgMonthlySpending ?? 0);
  } catch (error) {
    console.error('최근 3개월 월평균 지출 조회 실패:', error);
    averageMonthlySpending.value = 0;
  }
};

// ── 화면 이동 ─────────────────────────────────────────────────
const handleNext = () => {
  router.push({
    name: 'JobProducts',
    params: { goalId: goalId.value },
  });
};

const handlePrev = () => {
  router.back();
};

onMounted(async () => {
  await fetchJobGoalDetail();
  fetchCategoryList();
  fetchAverageMonthlySpending();
});
</script>

<template>
  <div class="job-cost">
    <RoadmapCharacterSlider :step="3" label="진로 로드맵" />

    <div v-if="isLoading" class="job-cost__loading">
      비용 정보를 불러오고 있습니다.
    </div>

    <template v-else>
      <PageHeader
        title="진로 로드맵의 예상 비용 내역입니다."
        description="선택한 준비 항목의 총 비용을 분석해드립니다."
      />

      <GoalSummaryCard
        theme="job"
        :chip="goalTypeLabel"
        :title="goalName"
        :specs="summarySpecs"
        :compare="summaryCompare"
      />

      <!-- 알약형 탭: 준비 예상 비용 / 상세 내역 (자동차·여행 step3와 통일) -->
      <div class="job-cost__tabs">
        <TabBar variant="segment" v-model="costTab" :tabs="COST_TABS" />
      </div>

      <!-- 준비 예상 비용 (물통) -->
      <EstimatedCostCard
        v-if="costTab === 'cost'"
        :state="costState"
        :dividers="false"
        note=""
      />

      <!-- 상세 내역 (선택한 준비 항목 목록) -->
      <BaseCard v-else padding="16px 18px 18px" class="cost-detail-pane">
        <template v-if="detailGroups.length">
          <div
            v-for="group in detailGroups"
            :key="group.title"
            class="cost-detail__group"
          >
            <strong class="cost-detail__group-title">{{ group.title }}</strong>

            <div
              v-for="(item, index) in group.items"
              :key="index"
              class="cost-detail__row"
            >
              <span class="cost-detail__name">{{ item.name }}</span>
              <strong class="cost-detail__amount">{{
                formatWon(item.amount)
              }}</strong>
            </div>
          </div>
        </template>

        <p v-else class="cost-detail__empty">선택한 준비 항목이 없어요.</p>
      </BaseCard>
    </template>

    <BottomButtonBar
      primaryLabel="다음"
      secondaryLabel="이전"
      @primary-click="handleNext"
      @secondary-click="handlePrev"
    />
  </div>
</template>

<style scoped>
.job-cost {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px 20px 110px;
}

.job-cost__loading {
  padding: 60px 0;
  color: var(--text-muted);
  font-size: 14px;
  text-align: center;
}

/* ── 상세 내역 탭 (선택 항목 목록) ── */
.cost-detail__empty {
  margin: 0;
  padding: 8px 0;
  color: var(--text-muted);
  font-size: 13px;
  text-align: center;
}

.cost-detail__group + .cost-detail__group {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--line);
}

/* 분류(그룹) 제목 — 항목과 구분되게 진하게 */
.cost-detail__group-title {
  display: block;
  margin-bottom: 10px;
  color: var(--text-strong);
  font-size: 13px;
  font-weight: 700;
}

.cost-detail__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.cost-detail__row + .cost-detail__row {
  margin-top: 10px;
}

.cost-detail__name {
  overflow: hidden;
  color: var(--text-body);
  font-size: 13px;
}

.cost-detail__amount {
  flex-shrink: 0;
  color: var(--text-strong);
  font-size: 13px;
}
</style>
