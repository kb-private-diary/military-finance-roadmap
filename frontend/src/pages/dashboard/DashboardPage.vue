<script setup>
// SCR-DASH-01 · 대시보드  (담당: 석윤)
// D-Day·군적금 납입액·휴가·지출 위젯
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import dashboardApi from '@/api/dashboardApi';
import { useToast } from '@/composables/useToast';
import { formatWon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import ProgressBar from '@/components/common/ProgressBar.vue';
import ToggleSwitch from '@/components/common/ToggleSwitch.vue';

const DAYS_PER_MONTH = 30;
const DAYS_PER_WEEK = 7;

// 휴가 카테고리 코드 -> 배지 라벨/색상
const VACATION_CATEGORY_META = {
  REGULAR: { label: '정기', variant: 'green' },
  REWARD: { label: '포상', variant: 'yellow' },
  CONSOLATION: { label: '위로', variant: 'blue' },
  PETITION: { label: '청원', variant: 'purple' },
  ETC: { label: '기타', variant: 'brown' },
};

const router = useRouter();
const { show } = useToast();

const basic = ref(null);
const savings = ref(null);
const vacationSummary = ref(null);
const isLoading = ref(true);
const hasError = ref(false);

// ON이면 D-Day/개월·주 계산에서 잔여 휴가일수를 뺀다 (dDay computed 참고)
const isVacationOn = ref(false);

const dDay = computed(() => {
  if (!basic.value) {
    return 0;
  }
  const remain = basic.value.totalServiceDays - basic.value.currentServiceDays;
  // 토글 ON이면 잔여 휴가일수만큼 D-Day에서 미리 뺀다.
  const vacationDays = isVacationOn.value
    ? (vacationSummary.value?.remainingDays ?? 0)
    : 0;
  return Math.max(remain - vacationDays, 0);
});

// 전역까지 남은 일수를 개월/주 단위로 환산 (30일=1개월, 7일=1주 기준)
const remainMonths = computed(() => (dDay.value / DAYS_PER_MONTH).toFixed(1));
const remainWeeks = computed(() => (dDay.value / DAYS_PER_WEEK).toFixed(1));

// 'yyyy-MM-dd' -> 'yy.MM.dd'
const formatDate = (dateStr) => {
  if (!dateStr) {
    return '-';
  }
  return dateStr.slice(2).replace(/-/g, '.');
};

const fetchBasicInfo = async () => {
  basic.value = await dashboardApi.findBasicInfo();
};

const fetchSavingsStatus = async () => {
  try {
    savings.value = await dashboardApi.findSavingsStatus();
  } catch (error) {
    // 군적금 미가입(DASH_002) 등은 빈 상태로 처리
    savings.value = null;
  }
};

const fetchVacations = async () => {
  try {
    vacationSummary.value = await dashboardApi.findVacations();
  } catch (error) {
    console.error(error);
    vacationSummary.value = null;
  }
};

const vacationCategoryMeta = (category) =>
  VACATION_CATEGORY_META[category] ?? { label: category, variant: 'gray' };

const goAddVacation = () => router.push({ name: 'VacationEdit' });

const goVacationDetail = (item) => {
  if (item.category === 'REGULAR') {
    router.push({ name: 'VacationRegular' });
  } else {
    router.push({
      name: 'VacationEdit',
      query: { vacationId: item.vacationId },
    });
  }
};

// 정기(REGULAR)는 "사용 등록"이 곧 새 사용내역(차수) 등록이라 일수 입력이 필요 —
// 원클릭 토글이 아니라 VacationRegularPage로 보낸다. 그 외 카테고리만 즉시 isUsed 처리.
const toggleVacationUsed = async (item) => {
  if (item.category === 'REGULAR') {
    router.push({ name: 'VacationRegular' });
    return;
  }
  try {
    await dashboardApi.updateVacation(item.vacationId, {
      category: item.category,
      name: item.name,
      acquiredDate: item.acquiredDate,
      days: item.days,
      isUsed: !item.isUsed,
    });
    await fetchVacations();
  } catch (error) {
    console.error(error);
    show('휴가 사용 처리에 실패했습니다.', 'error');
  }
};

onMounted(async () => {
  isLoading.value = true;
  try {
    await Promise.all([
      fetchBasicInfo(),
      fetchSavingsStatus(),
      fetchVacations(),
    ]);
  } catch (error) {
    console.error(error);
    hasError.value = true;
  } finally {
    isLoading.value = false;
  }
});
</script>

<template>
  <div class="dashboard-page container py-4">
    <p v-if="isLoading" class="text-caption">불러오는 중...</p>
    <p v-else-if="hasError" class="text-caption">정보를 불러오지 못했습니다.</p>

    <section v-if="basic" class="profile-card">
      <div class="profile-card__intro">
        <p class="profile-card__name">
          {{ basic.name }} {{ basic.rankName }}님
        </p>
        <div class="profile-card__badges">
          <BaseTag
            v-if="basic.typeName"
            :label="basic.typeName"
            variant="green-light"
          />
          <BaseTag
            v-if="basic.unitName"
            :label="basic.unitName"
            variant="green-light"
          />
        </div>
      </div>

      <div class="profile-card__progress-wrap">
        <div class="profile-card__progress">
          <ProgressBar
            :value="basic.serviceRate"
            :total="100"
            color="var(--kb-yellow-deep)"
            :height="18"
          />
        </div>
        <p class="profile-card__progress-caption">
          {{ basic.currentServiceDays }}/{{ basic.totalServiceDays }}일 ({{
            basic.serviceRate
          }}%)
        </p>
      </div>

      <div class="profile-card__dates">
        <div class="profile-card__date">
          <p class="profile-card__date-label">입대</p>
          <p class="profile-card__date-value">
            {{ formatDate(basic.enlistDate) }}
          </p>
        </div>
        <div class="profile-card__date profile-card__date--right">
          <p class="profile-card__date-label">전역</p>
          <p class="profile-card__date-value">
            {{ formatDate(basic.dischargeDate) }}
          </p>
        </div>
      </div>

      <div class="remain-box">
        <div class="remain-box__row">
          <p class="remain-box__stat">
            <span class="remain-box__number">{{ remainMonths }}</span
            ><span class="remain-box__unit">개월</span>
          </p>
          <ToggleSwitch
            v-model="isVacationOn"
            label="휴가"
            label-position="left"
          />
        </div>
        <div class="remain-box__row remain-box__row--bottom">
          <p class="remain-box__stat">
            <span class="remain-box__number">{{ remainWeeks }}</span
            ><span class="remain-box__unit">주</span>
          </p>
          <p class="remain-box__dday">D-{{ dDay }}</p>
        </div>
      </div>
    </section>

    <BaseCard class="savings-card" padding="16px">
      <p class="savings-card__title">군적금 현황</p>
      <template v-if="savings">
        <div class="savings-card__row">
          <span class="savings-card__label">현재 납입액</span>
          <span class="savings-card__value">{{
            formatWon(savings.currentTotalSavings)
          }}</span>
        </div>
        <div class="savings-card__row">
          <span class="savings-card__label">예상 만기 수령액</span>
          <span class="savings-card__value savings-card__value--highlight">{{
            formatWon(savings.expectedMaturityTotal)
          }}</span>
        </div>
      </template>
      <p v-else class="savings-card__empty">아직 군적금 가입 내역이 없어요</p>
    </BaseCard>

    <section v-if="vacationSummary" class="vacation-section">
      <div class="vacation-section__header">
        <div>
          <p class="vacation-section__eyebrow">모으고 쓰는 또 하나의 자산</p>
          <h2 class="vacation-section__title">휴가 관리</h2>
        </div>
        <CategoryButton
          class="vacation-section__add-btn"
          variant="oval-yellow"
          active
          label="휴가 추가"
          @click="goAddVacation"
        />
      </div>

      <div class="vacation-section__total">
        <p class="vacation-section__total-label">총 잔여</p>
        <p class="vacation-section__total-value">
          {{ vacationSummary.remainingDays }}일
        </p>
        <div class="vacation-section__progress">
          <ProgressBar
            :value="vacationSummary.remainingDays"
            :total="vacationSummary.totalDays"
            color="var(--military-green)"
            :height="10"
          />
        </div>
        <p class="vacation-section__total-caption">
          {{ vacationSummary.usedDays }}일 사용 / 총
          {{ vacationSummary.totalDays }}일
        </p>
      </div>

      <div class="vacation-section__grid">
        <BaseCard
          v-for="item in vacationSummary.vacations"
          :key="item.vacationId"
          class="vacation-card"
          padding="14px"
          @click="goVacationDetail(item)"
        >
          <div class="vacation-card__top">
            <BaseTag
              :label="vacationCategoryMeta(item.category).label"
              :variant="vacationCategoryMeta(item.category).variant"
            />
            <span class="vacation-card__days">{{ item.days }}일</span>
          </div>
          <p class="vacation-card__name">{{ item.name }}</p>
          <p class="vacation-card__meta">
            <template v-if="item.category === 'REGULAR'"
              >잔여 일수 {{ item.remainingDays }}일</template
            >
            <template v-else>획득 {{ formatDate(item.acquiredDate) }}</template>
          </p>
          <button
            type="button"
            class="vacation-card__action"
            :class="{ 'vacation-card__action--done': item.isUsed }"
            @click.stop="toggleVacationUsed(item)"
          >
            {{ item.isUsed ? '사용 완료' : '사용 등록' }}
          </button>
        </BaseCard>
      </div>
    </section>
  </div>
</template>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ── 복무 정보 카드 ── */
.profile-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 18px;
  background-color: var(
    --military-green
  ); /* base-tag--green 과 동일한 국방색 */
  border-radius: 20px;
  color: var(--surface-default);
}

.profile-card__intro {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.profile-card__name {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.profile-card__badges {
  display: flex;
  gap: 8px;
}

.profile-card__progress-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile-card__progress {
  /* ProgressBar 트랙 자체는 배경이 투명(테두리만)이라
     진한 카드 배경 위에서 잘 안 보이는 문제 방지 — 뒤에 옅은 트랙 배경을 깔아준다 */
  background-color: var(--overlay-white-30);
  border-radius: 25px;
}

.profile-card__progress-caption {
  margin: 0 4px 0 0;
  font-size: 12px;
  color: var(--military-green-light);
  text-align: right;
}

.profile-card__dates {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.profile-card__date--right {
  text-align: right;
}

.profile-card__date-label {
  margin: 0 0 2px;
  font-size: 12px;
  color: var(--military-green-light);
}

.profile-card__date-value {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

/* ── 남은 기간(개월/주) 박스 ── */
.remain-box {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 14px;
  background-color: var(--surface-default);
  border-radius: 14px;
  color: var(--text-strong);
}

.remain-box__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.remain-box__row--bottom {
  align-items: flex-end;
}

.remain-box__row :deep(.toggle-wrapper) {
  width: auto;
  gap: 8px;
}

.remain-box__row :deep(.toggle-label) {
  font-size: 13px;
  color: var(--text-body);
}

.remain-box__stat {
  margin: 0;
}

.remain-box__number {
  font-size: 26px;
  font-weight: 700;
}

.remain-box__unit {
  margin-left: 4px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-body);
}

.remain-box__dday {
  margin: 0;
  font-size: 40px;
  font-weight: 800;
  line-height: 1;
  color: var(--text-strong);
}

/* ── 군적금 현황 카드 ── */
.savings-card {
  display: flex;
  flex-direction: column;
}

.savings-card__title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 6px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}

.savings-card__title::before {
  content: '';
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background-color: var(
    --military-green
  ); /* 위 복무 정보 카드와 같은 국방색으로 시각적 연결 */
}

.savings-card__row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 8px 0;
}

.savings-card__row + .savings-card__row {
  border-top: 1px dashed var(--line);
}

.savings-card__label {
  font-size: 13px;
  color: var(--text-hint);
}

.savings-card__value {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}

.savings-card__value--highlight {
  font-size: 19px;
  color: var(--brand-gold); /* 브랜드 골드 톤 — 예상 수령액을 강조 */
}

.savings-card__empty {
  margin: 0;
  padding: 12px 0;
  font-size: 13px;
  color: var(--text-hint);
  text-align: center;
}

/* ── 휴가 관리 ── */
.vacation-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.vacation-section__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

:deep(.vacation-section__add-btn) {
  flex: none;
  width: auto;
}

.vacation-section__eyebrow {
  margin: 0 0 4px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-hint);
}

.vacation-section__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
}

.vacation-section__total {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.vacation-section__total-label {
  margin: 0;
  font-size: 13px;
  color: var(--text-hint);
}

.vacation-section__total-value {
  margin: 0;
  font-size: 15px;
  font-weight: 800;
  color: var(--text-strong);
}

.vacation-section__total-caption {
  margin: 0;
  font-size: 12px;
  color: var(--text-hint);
  text-align: right;
}

.vacation-section__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.vacation-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  cursor: pointer;
}

.vacation-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.vacation-card__days {
  font-size: 16px;
  font-weight: 800;
  color: var(--text-strong);
}

.vacation-card__name {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}

.vacation-card__meta {
  margin: 0;
  font-size: 12px;
  color: var(--text-hint);
}

.vacation-card__action {
  margin-top: auto;
  padding: 8px;
  border: none;
  border-radius: 10px;
  background-color: var(--military-green);
  color: var(--surface-default);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.vacation-card__action--done {
  background-color: var(--gray-pale-bg);
  color: var(--gray-mid);
}
</style>
