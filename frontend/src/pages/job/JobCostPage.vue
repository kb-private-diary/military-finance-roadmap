<script setup>
// SCR-JOB-03 · step3) 진로 비용 계산 (담당: 지원)
// step3 - 선택한 자격증·어학 및 인강의 총 예상 비용 확인

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import calculatorIcon from '@/assets/images/calculator.png';
import jobApi from '@/api/jobApi';
import { formatWon } from '@/util/format';

const route = useRoute();
const router = useRouter();

const goalId = computed(() => Number(route.params.goalId));

// ── 조회 상태 ─────────────────────────────────────────────────
const qualifications = ref([]);
const courses = ref([]);
const isLoading = ref(false);

// TODO: 오픈뱅킹 최근 1개월 지출 조회 API 연동 후 실제 값으로 교체
// 세미 시연용 임시 데이터
const monthlySpending = ref(1250000);

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

// 총 예상 준비비용
const totalAmount = computed(
  () => qualificationTotal.value + courseTotal.value,
);

// 선택한 전체 준비항목 수
const selectedItemCount = computed(
  () => qualifications.value.length + courses.value.length,
);

// 자격증·어학 비용 비율
const qualificationRate = computed(() => {
  if (totalAmount.value === 0) return 0;

  return Math.round((qualificationTotal.value / totalAmount.value) * 100);
});

// 인터넷 강의 비용 비율
const courseRate = computed(() => {
  if (totalAmount.value === 0) return 0;

  return 100 - qualificationRate.value;
});

// 최근 1개월 지출 대비 예상 준비비용 비율
const spendingRate = computed(() => {
  if (monthlySpending.value === 0) return 0;

  return Math.round((totalAmount.value / monthlySpending.value) * 100);
});

// 진행바는 100%를 넘지 않도록 제한
const spendingProgressRate = computed(() => Math.min(spendingRate.value, 100));

// 가장 비용 비중이 큰 항목 문구
const largestCostMessage = computed(() => {
  if (totalAmount.value === 0) {
    return '선택한 준비 항목의 비용 정보가 없습니다.';
  }

  if (qualificationTotal.value === courseTotal.value) {
    return '두 준비 항목의 비용 비중이 같아요.';
  }

  return qualificationTotal.value > courseTotal.value
    ? '자격증·어학 비용의 비중이 가장 커요.'
    : '인터넷 강의 비용의 비중이 가장 커요.';
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

    qualifications.value = detail?.qualifications ?? [];
    courses.value = detail?.courses ?? [];
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

onMounted(() => {
  fetchJobGoalDetail();
});
</script>

<template>
  <div class="job-cost">
    <RoadmapCharacterSlider :step="3" label="진로 로드맵" />

    <div v-if="isLoading" class="job-cost__loading">
      비용 정보를 불러오고 있습니다.
    </div>

    <template v-else>
      <!-- 총 예상 준비비용 -->
      <section class="cost-summary">
        <div class="cost-summary__icon" aria-hidden="true">
          <img :src="calculatorIcon" alt="" class="cost-summary__icon-image" />
        </div>

        <div class="cost-summary__content">
          <p class="cost-summary__label">총 예상 준비비용</p>

          <strong class="cost-summary__amount">
            {{ formatWon(totalAmount) }}
          </strong>

          <p class="cost-summary__description">
            선택한 준비 항목 {{ selectedItemCount }}개 기준
          </p>
        </div>
      </section>

      <!-- 비용 구성 -->
      <BaseCard padding="16px">
        <section class="cost-composition">
          <h2 class="section-title">비용 구성</h2>

          <div
            class="stacked-bar"
            role="img"
            :aria-label="`자격증·어학 ${qualificationRate}%, 인터넷 강의 ${courseRate}%`"
          >
            <div
              v-if="qualificationRate > 0"
              class="stacked-bar__item stacked-bar__item--qualification"
              :style="{ width: `${qualificationRate}%` }"
            >
              <span v-if="qualificationRate >= 15">
                {{ qualificationRate }}%
              </span>
            </div>

            <div
              v-if="courseRate > 0"
              class="stacked-bar__item stacked-bar__item--course"
              :style="{ width: `${courseRate}%` }"
            >
              <span v-if="courseRate >= 15"> {{ courseRate }}% </span>
            </div>
          </div>

          <ul class="cost-composition__list">
            <li class="cost-row">
              <span class="cost-row__dot cost-row__dot--qualification"></span>

              <span class="cost-row__name">자격증·어학</span>

              <strong class="cost-row__amount">
                {{ formatWon(qualificationTotal) }}
              </strong>

              <span class="cost-row__rate"> {{ qualificationRate }}% </span>
            </li>

            <li class="cost-row">
              <span class="cost-row__dot cost-row__dot--course"></span>

              <span class="cost-row__name">인터넷 강의</span>

              <strong class="cost-row__amount">
                {{ formatWon(courseTotal) }}
              </strong>

              <span class="cost-row__rate"> {{ courseRate }}% </span>
            </li>
          </ul>

          <p class="cost-composition__message">
            {{ largestCostMessage }}
          </p>

          <div class="cost-composition__count">
            상세 내역 {{ selectedItemCount }}건
          </div>
        </section>
      </BaseCard>

      <!-- 최근 1개월 지출 대비 -->
      <BaseCard padding="20px">
        <section class="spending-compare">
          <div class="spending-compare__head">
            <div>
              <h2 class="section-title">최근 1개월 지출 대비</h2>

              <p class="spending-compare__description">
                최근 지출 대비 준비비용을 비교했어요.
              </p>
            </div>

            <strong class="spending-compare__rate">
              {{ spendingRate }}%
            </strong>
          </div>

          <div class="spending-progress">
            <div
              class="spending-progress__bar"
              :style="{ width: `${spendingProgressRate}%` }"
            ></div>
          </div>

          <dl class="spending-compare__list">
            <div class="spending-compare__row">
              <dt>최근 1개월 지출</dt>
              <dd>{{ formatWon(monthlySpending) }}</dd>
            </div>

            <div class="spending-compare__row">
              <dt>예상 준비비용</dt>
              <dd>{{ formatWon(totalAmount) }}</dd>
            </div>
          </dl>

          <p class="spending-compare__notice">
            현재는 세미 시연용 임시 지출 데이터를 사용하고 있습니다.
          </p>
        </section>
      </BaseCard>

      <!-- 안내 -->
      <section class="cost-guide">
        <h2 class="cost-guide__title">안내</h2>

        <p>· 계산 결과는 예상 금액이며 실제 비용과 다를 수 있습니다.</p>

        <p>· 할인 및 지원 조건에 따라 실제 부담금이 달라질 수 있습니다.</p>
      </section>
    </template>

    <BottomButtonBar
      primaryLabel="다 음"
      secondaryLabel="이 전"
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

/* ── 총 예상 준비비용 ─────────────────────────────────────── */

.cost-summary {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 16px;
  background: var(--kb-yellow-pale);
}

.cost-summary__icon {
  display: flex;
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: var(--chart-4);
  color: var(--text-body);
  font-size: 24px;
  font-weight: 700;
}

.cost-summary__icon-image {
  width: 24px;
  height: 24px;
  object-fit: contain;
  display: block;
}

.cost-summary__content {
  min-width: 0;
}

.cost-summary__label {
  margin: 0;
  color: var(--brand-gold);
  font-size: 14px;
  font-weight: 600;
}

.cost-summary__amount {
  display: block;
  margin-top: 4px;
  color: var(--text-body);
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: -0.8px;
}

.cost-summary__description {
  margin: 5px 0 0;
  color: var(--text-muted);
  font-size: 13px;
}

/* ── 공통 제목 ───────────────────────────────────────────── */

.section-title {
  margin: 0;
  color: var(--text-body);
  font-size: 15px;
  font-weight: 700;
}

/* ── 비용 구성 ───────────────────────────────────────────── */

.cost-composition {
  width: 100%;
}

.stacked-bar {
  display: flex;
  width: 100%;
  height: 24px;
  margin-top: 20px;
  overflow: hidden;
  border-radius: 10px;
  background: var(--gray-track-light);
}

.stacked-bar__item {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: center;
  color: var(--surface-default);
  font-size: 12px;
  font-weight: 600;
  transition: width 0.25s ease;
}

.stacked-bar__item--qualification {
  background: var(--chart-1);
}

.stacked-bar__item--course {
  background: var(--chart-2);
}

.cost-composition__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 20px 0 0;
  padding: 0;
  list-style: none;
}

.cost-row {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr) auto 42px;
  gap: 12px;
  align-items: center;
}

.cost-row__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.cost-row__dot--qualification {
  background: var(--chart-1);
}

.cost-row__dot--course {
  background: var(--chart-2);
}

.cost-row__name {
  overflow: hidden;
  color: var(--text-body);
  font-size: 13px;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cost-row__amount {
  color: var(--text-body);
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.cost-row__rate {
  color: var(--brand-gold);
  font-size: 13px;
  font-weight: 600;
  text-align: right;
}

.cost-composition__message {
  margin: 18px 0 0;
  padding: 8px 10px;
  border-radius: 10px;
  background: var(--kb-yellow-pale);
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.5;
  text-align: center;
}

.cost-composition__count {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--line);
  color: var(--text-hint);
  font-size: 12px;
  text-align: center;
}

/* ── 최근 1개월 지출 대비 ────────────────────────────────── */

.spending-compare {
  width: 100%;
}

.spending-compare__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 22px 0 0;
}

.spending-compare__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.spending-compare__row dt {
  color: var(--text-muted);
  font-size: 13px;
}

.spending-compare__row dd {
  margin: 0;
  color: var(--text-body);
  font-size: 13px;
  font-weight: 600;
}

.spending-progress {
  width: 100%;
  height: 10px;
  margin-top: 24px;
  overflow: hidden;
  border-radius: 999px;
  background: var(--gray-track-light);
}

.spending-progress__bar {
  height: 100%;
  border-radius: inherit;
  background: var(--kb-yellow);
  transition: width 0.3s ease;
}

.spending-compare__summary {
  margin: 14px 0 0;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.5;
  text-align: center;
}

.spending-compare__summary strong {
  color: var(--kb-yellow-deep);
  font-weight: 700;
}

.spending-compare__notice {
  margin: 18px 0 0;
  padding-top: 16px;
  border-top: 1px solid var(--line);
  color: var(--text-hint);
  font-size: 11px;
  line-height: 1.5;
}

.spending-compare__description {
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 400;
  line-height: 1.5;
}

/* ── 안내 ─────────────────────────────────────────────────── */

.cost-guide {
  padding: 18px;
  border-radius: 16px;
  background: var(--surface-subtle);
}

.cost-guide__title {
  margin: 0 0 12px;
  color: var(--text-body);
  font-size: 15px;
  font-weight: 700;
}

.cost-guide p {
  margin: 0;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.7;
}

.cost-guide p + p {
  margin-top: 10px;
}
</style>
