<script setup>
// SCR-DASH-01 · 대시보드  (담당: 석윤)
// D-Day·군적금 납입액·휴가·지출 위젯
// 휴가 API(DASH-API-02)는 아직 미구현이라 이 화면에서는 제외한다.
import { computed, onMounted, ref } from 'vue';
import dashboardApi from '@/api/dashboardApi';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import ProgressBar from '@/components/common/ProgressBar.vue';
import ToggleSwitch from '@/components/common/ToggleSwitch.vue';

// TODO: JWT 연동 후 SecurityContext(authStore)에서 userId 추출
// 백엔드가 아직 @RequestParam Long userId 임시 방식이라 프론트도 임시 고정값 사용
const TEMP_USER_ID = 1;

const DAYS_PER_MONTH = 30;
const DAYS_PER_WEEK = 7;

const basic = ref(null);
const savings = ref(null);
const isLoading = ref(true);
const hasError = ref(false);

// 휴가 API(DASH-API-02) 미구현 — 화면 구성만 유지하는 더미 상태(실제 동작 없음)
const isVacationOn = ref(true);

const dDay = computed(() => {
  if (!basic.value) {
    return 0;
  }
  const remain = basic.value.totalServiceDays - basic.value.currentServiceDays;
  return Math.max(remain, 0);
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

const formatWon = (amount) => `${(amount ?? 0).toLocaleString('ko-KR')}원`;

const fetchBasicInfo = async () => {
  basic.value = await dashboardApi.findBasicInfo(TEMP_USER_ID);
};

const fetchSavingsStatus = async () => {
  try {
    savings.value = await dashboardApi.findSavingsStatus(TEMP_USER_ID);
  } catch (error) {
    // 군적금 미가입(DASH_002) 등은 빈 상태로 처리
    savings.value = null;
  }
};

onMounted(async () => {
  isLoading.value = true;
  try {
    await Promise.all([fetchBasicInfo(), fetchSavingsStatus()]);
  } catch (error) {
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
            color="var(--kb-yellow-deep, #ffbc00)"
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

    <BaseCard class="savings-card">
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
  gap: 18px;
  padding: 22px 20px;
  background-color: #536349; /* base-tag--green 과 동일한 국방색 */
  border-radius: 20px;
  color: #ffffff;
}

.profile-card__intro {
  display: flex;
  flex-direction: column;
  gap: 10px;
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
  gap: 6px;
}

.profile-card__progress {
  /* ProgressBar 트랙 자체는 배경이 투명(테두리만)이라
     진한 카드 배경 위에서 잘 안 보이는 문제 방지 — 뒤에 옅은 트랙 배경을 깔아준다 */
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 25px;
}

.profile-card__progress-caption {
  margin: 0 4px 0 0;
  font-size: 12px;
  color: #e0e6dc;
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
  color: #e0e6dc;
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
  gap: 6px;
  padding: 14px 16px;
  background-color: #ffffff;
  border-radius: 14px;
  color: var(--text-strong, #000000);
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
  color: var(--text-body, #545045);
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
  color: var(--text-body, #545045);
}

.remain-box__dday {
  margin: 0;
  font-size: 40px;
  font-weight: 800;
  line-height: 1;
  color: var(--text-strong, #000000);
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
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong, #000000);
}

.savings-card__title::before {
  content: '';
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background-color: #536349; /* 위 복무 정보 카드와 같은 국방색으로 시각적 연결 */
}

.savings-card__row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 12px 0;
}

.savings-card__row + .savings-card__row {
  border-top: 1px dashed var(--line, #e0e0e0);
}

.savings-card__label {
  font-size: 13px;
  color: var(--text-hint, #999999);
}

.savings-card__value {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong, #000000);
}

.savings-card__value--highlight {
  font-size: 19px;
  color: #a9895a; /* 브랜드 골드 톤 — 예상 수령액을 강조 */
}

.savings-card__empty {
  margin: 0;
  padding: 12px 0;
  font-size: 13px;
  color: var(--text-hint, #999999);
  text-align: center;
}
</style>
