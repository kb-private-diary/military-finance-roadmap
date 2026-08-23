<script setup>
// SCR-DASH-01 · 대시보드  (담당: 석윤)
// D-Day·군적금 납입액·휴가·지출 위젯
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import dashboardApi from '@/api/dashboardApi';
import { formatWon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseTag from '@/components/common/BaseTag.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import ProgressBar from '@/components/common/ProgressBar.vue';
import TabBar from '@/components/common/TabBar.vue';
import ToggleSwitch from '@/components/common/ToggleSwitch.vue';
import ageoIcon from '@/assets/images/char-ageo.png';

const DAYS_PER_MONTH = 30;
const DAYS_PER_WEEK = 7;

// 휴가 카테고리 코드 -> 태그 라벨
const VACATION_CATEGORY_META = {
  REGULAR: { label: '정기' },
  REWARD: { label: '포상' },
  CONSOLATION: { label: '위로' },
  PETITION: { label: '청원' },
  ETC: { label: '기타' },
};

// 카테고리별 군대색 (앞쪽 태그 + 총잔여 바 세그먼트 공통 사용)
const VACATION_CATEGORY_ORDER = [
  'REGULAR',
  'REWARD',
  'CONSOLATION',
  'PETITION',
  'ETC',
];
const VACATION_CATEGORY_COLOR = {
  REGULAR: '#3f9d5f', // 그린
  REWARD: '#d99e2b', // 골드
  CONSOLATION: '#de6553', // 코랄
  PETITION: '#4a90d9', // 블루
  ETC: '#9268c9', // 퍼플
};
const categoryColor = (category) =>
  VACATION_CATEGORY_COLOR[category] ?? 'var(--text-hint)';

const router = useRouter();

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
  VACATION_CATEGORY_META[category] ?? { label: category };

// 사용 가능 / 사용 완료 분리 - 상단 탭 + '전체'에선 두 섹션으로 함께 표시
const VACATION_TABS = [
  { value: 'all', label: '전체' },
  { value: 'available', label: '사용 가능' },
  { value: 'used', label: '사용 완료' },
];
const vacationTab = ref('all');

const availableVacations = computed(() =>
  (vacationSummary.value?.vacations ?? []).filter((v) => !v.isUsed),
);
const usedVacations = computed(() =>
  (vacationSummary.value?.vacations ?? []).filter((v) => v.isUsed),
);

// 화면에 그릴 그룹. '전체'는 두 섹션(소제목 표시), 개별 탭은 한 그룹(소제목 없음).
const vacationGroups = computed(() => {
  if (vacationTab.value === 'available') {
    return [{ key: 'available', items: availableVacations.value, dimmed: false, header: '' }];
  }
  if (vacationTab.value === 'used') {
    return [{ key: 'used', items: usedVacations.value, dimmed: true, header: '' }];
  }
  const groups = [];
  if (availableVacations.value.length) {
    groups.push({ key: 'available', items: availableVacations.value, dimmed: false, header: '사용 가능' });
  }
  if (usedVacations.value.length) {
    groups.push({ key: 'used', items: usedVacations.value, dimmed: true, header: '사용 완료' });
  }
  return groups;
});

// 총 잔여 바를 카테고리(태그)별로 쪼갠 세그먼트 - 각 카테고리의 사용 가능(잔여) 휴가일
const vacationBarSegments = computed(() => {
  const total = vacationSummary.value?.totalDays || 0;
  if (!total) return [];
  const byCategory = {};
  for (const v of vacationSummary.value?.vacations ?? []) {
    byCategory[v.category] = (byCategory[v.category] || 0) + (v.remainingDays || 0);
  }
  return VACATION_CATEGORY_ORDER.filter((cat) => byCategory[cat] > 0).map(
    (cat) => ({
      key: cat,
      label: vacationCategoryMeta(cat).label,
      days: byCategory[cat],
      pct: (byCategory[cat] / total) * 100,
      color: categoryColor(cat),
    }),
  );
});

const goAddVacation = () => router.push({ name: 'VacationEdit' });

// 카드 본문 탭 = 사용 등록(사용내역 관리). 카테고리 상관없이 항상 동일하게 동작한다.
const openVacationUsage = (item) => {
  router.push({
    name: 'VacationUsage',
    query: { vacationId: item.vacationId },
  });
};

// 연필(✏️) = 휴가 수정 페이지로. 정기휴가는 자동 부여라 아이콘 자체를 숨긴다.
const editVacation = (item) => {
  router.push({
    name: 'VacationEdit',
    query: { vacationId: item.vacationId },
  });
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
    <!-- 복무현황·군적금을 감싸는 풀블리드 군색 히어로 -->
    <div class="dashboard-hero">
      <p v-if="isLoading" class="text-caption">불러오는 중...</p>
      <p v-else-if="hasError" class="text-caption">정보를 불러오지 못했습니다.</p>

    <section v-if="basic" class="profile-card">
      <!-- 이름 + 휴가 토글 -->
      <div class="profile-card__top">
        <p class="profile-card__name">
          {{ basic.name }}
          <span class="profile-card__rank">{{ basic.rankName }}님</span>
        </p>
        <ToggleSwitch
          v-model="isVacationOn"
          label="휴가 중"
          label-position="left"
        />
      </div>

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

      <!-- D-Day + 전역까지 남은 개월·주 + 복무 진행률 배지 -->
      <div class="profile-card__dday-row">
        <p class="profile-card__dday">D-{{ dDay }}</p>
        <div class="profile-card__dday-sub">
          <span class="profile-card__dday-label">전역까지</span>
          <span class="profile-card__dday-detail">
            {{ remainMonths }}개월 · {{ remainWeeks }}주
          </span>
        </div>
        <div class="profile-card__dday-sub">
          <span class="profile-card__dday-label">현재 복무일</span>
          <span class="profile-card__dday-detail">
            {{ basic.currentServiceDays }} / {{ basic.totalServiceDays }}일 ·
            {{ basic.serviceRate }}%
          </span>
        </div>
      </div>

      <!-- 입대 → 전역 복무 타임라인 (아거가 진행 위치에) -->
      <div class="profile-card__timeline">
        <div class="profile-card__progress">
          <ProgressBar
            :value="basic.serviceRate"
            :total="100"
            color="var(--kb-yellow-deep)"
            :height="8"
          />
          <img
            :src="ageoIcon"
            alt=""
            aria-hidden="true"
            class="profile-card__progress-marker"
            :style="{ left: `${basic.serviceRate}%` }"
          />
        </div>
        <div class="profile-card__timeline-ends">
          <div class="profile-card__timeline-end">
            <span class="profile-card__timeline-label">입대</span>
            <strong>{{ formatDate(basic.enlistDate) }}</strong>
          </div>
          <div
            class="profile-card__timeline-end profile-card__timeline-end--right"
          >
            <span class="profile-card__timeline-label">전역</span>
            <strong>{{ formatDate(basic.dischargeDate) }}</strong>
          </div>
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
    </div>

    <template v-if="vacationSummary">
      <PageHeader
        eyebrow="하루하루가 전력이다"
        title="휴가 자산 브리핑"
        size="lg"
      />

      <section class="vacation-section">

      <BaseCard class="vacation-total-card" padding="16px 16px 14px">
        <div class="vacation-total-card__head">
          <div class="vacation-total-card__head-main">
            <span class="vacation-total-card__label">총 잔여 휴가</span>
            <span class="vacation-total-card__value">
              {{ vacationSummary.remainingDays }}<em>일</em>
            </span>
          </div>
          <button
            type="button"
            class="vacation-add-btn"
            @click="goAddVacation"
          >
            + 휴가 추가
          </button>
        </div>
        <div class="vacation-bar">
          <div
            v-for="seg in vacationBarSegments"
            :key="seg.key"
            class="vacation-bar__seg"
            :style="{ width: `${seg.pct}%`, backgroundColor: seg.color }"
          />
        </div>
        <ul v-if="vacationBarSegments.length" class="vacation-bar__legend">
          <li v-for="seg in vacationBarSegments" :key="seg.key">
            <span
              class="vacation-bar__dot"
              :style="{ backgroundColor: seg.color }"
            />
            {{ seg.label }} <strong>{{ seg.days }}일</strong>
          </li>
        </ul>
        <p class="vacation-total-card__caption">
          {{ vacationSummary.usedDays }}일 사용 / 총
          {{ vacationSummary.totalDays }}일
        </p>
      </BaseCard>

      <TabBar
        v-model="vacationTab"
        variant="segment"
        :tabs="VACATION_TABS"
      />

      <div class="vacation-lists">
        <template v-for="group in vacationGroups" :key="group.key">
        <h3 v-if="group.header" class="vacation-group__title">
          {{ group.header }}
        </h3>
        <div
          v-if="group.items.length"
          class="vacation-card-list"
          :class="{ 'vacation-card-list--dimmed': group.dimmed }"
        >
          <BaseCard
            v-for="item in group.items"
            :key="item.vacationId"
            class="vacation-card"
            padding="12px 14px"
            @click="openVacationUsage(item)"
          >
            <span
              class="vacation-tag"
              :style="{ backgroundColor: categoryColor(item.category) }"
            >
              {{ vacationCategoryMeta(item.category).label }}
            </span>
            <div class="vacation-card__body">
              <p class="vacation-card__name">{{ item.name }}</p>
              <p class="vacation-card__sub">
                잔여 {{ item.remainingDays }}일 / 총 {{ item.days }}일
              </p>
            </div>
            <!-- 정기휴가는 자동 부여라 수정할 정보가 없어 연필 숨김 -->
            <button
              v-if="item.category !== 'REGULAR'"
              type="button"
              class="vacation-card__edit"
              aria-label="휴가 정보 수정"
              @click.stop="editVacation(item)"
            >
              <svg
                viewBox="0 0 24 24"
                width="15"
                height="15"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                aria-hidden="true"
              >
                <path d="M12 20h9" />
                <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z" />
              </svg>
            </button>
            <span
              class="vacation-card__cta"
              :class="{ 'vacation-card__cta--done': item.isUsed }"
            >
              {{ item.isUsed ? '사용 완료' : '사용 등록' }}
            </span>
            <span class="vacation-card__chevron" aria-hidden="true">›</span>
          </BaseCard>
        </div>
        <p v-else class="vacation-empty text-caption">
          {{
            group.key === 'used'
              ? '사용 완료한 휴가가 없어요'
              : '사용 가능한 휴가가 없어요'
          }}
        </p>
      </template>

        <p
          v-if="!vacationGroups.length"
          class="vacation-empty text-caption"
        >
          등록된 휴가가 없어요
        </p>
      </div>
    </section>
    </template>
  </div>
</template>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 복무현황·군적금을 감싸는 풀블리드 군색 히어로 */
.dashboard-hero {
  display: flex;
  flex-direction: column;
  gap: 14px;
  /* 위: 페이지 상단 패딩(24px) 상쇄해 네비 밑 맨 위까지 / 좌우: 앱 거터(24px) 풀블리드 */
  margin: -24px -24px 0;
  padding: 26px 24px 22px;
  background:
    /* 우드랜드 카모 - 진한 초록·갈색·카키 반점 여러 개가 겹쳐 군복 무늬 */
    radial-gradient(112px 82px at 10% 18%, rgba(56, 40, 24, 0.55), transparent 60%),
    radial-gradient(132px 96px at 86% 30%, rgba(28, 40, 21, 0.5), transparent 58%),
    radial-gradient(96px 72px at 58% 87%, rgba(70, 50, 30, 0.5), transparent 60%),
    radial-gradient(90px 68px at 2% 82%, rgba(28, 40, 21, 0.48), transparent 60%),
    radial-gradient(86px 64px at 40% 47%, rgba(138, 118, 80, 0.42), transparent 58%),
    radial-gradient(80px 60px at 95% 85%, rgba(70, 50, 30, 0.44), transparent 60%),
    radial-gradient(72px 56px at 73% 60%, rgba(28, 40, 21, 0.42), transparent 60%),
    radial-gradient(64px 50px at 27% 73%, rgba(138, 118, 80, 0.34), transparent 58%),
    /* 우상단 하이라이트 */
    radial-gradient(
      130% 70% at 88% -8%,
      rgba(255, 255, 255, 0.1) 0%,
      rgba(255, 255, 255, 0) 46%
    ),
    /* 연해진 대각선 군색→갈색 그라데이션 */
    linear-gradient(158deg, #738262 0%, #5d6b4e 42%, #47412c 100%);
  color: var(--surface-default);
}

/* 로딩·에러 안내도 초록 위에서 보이게 */
.dashboard-hero > .text-caption {
  color: var(--military-green-light);
}

/* ── 복무 정보 카드 ── */
.profile-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* 배경은 감싸는 .dashboard-hero(초록)가 담당. 좌우 16px 인셋으로 군적금 카드 글자와 정렬 */
  padding: 0 16px;
  color: var(--surface-default);
}

.profile-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.profile-card__top :deep(.toggle-wrapper) {
  width: auto;
  gap: 8px;
}

.profile-card__top :deep(.toggle-label) {
  font-size: 13px;
  color: var(--military-green-light);
}

.profile-card__name {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

/* 계급·님은 이름보다 작게 */
.profile-card__rank {
  font-size: 14px;
  font-weight: 500;
  color: var(--military-green-light);
}

/* D-Day + 전역까지 남은 기간 */
.profile-card__dday-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  margin-top: 2px;
}

.profile-card__dday {
  margin: 0;
  font-size: 31px;
  font-weight: 800;
  line-height: 1;
  white-space: nowrap; /* D-182 한 덩어리로 (줄바꿈 방지) */
}

.profile-card__dday-sub {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding-bottom: 4px;
}

/* '전역까지'와 '현재 복무일' 두 컬럼 사이 간격 */
.profile-card__dday-sub + .profile-card__dday-sub {
  margin-left: 8px;
}

.profile-card__dday-label {
  font-size: 11px;
  color: var(--military-green-light);
}

.profile-card__dday-detail {
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap; /* '26.0주' '67%' 줄바꿈 방지 */
}

.profile-card__badges {
  display: flex;
  gap: 6px;
}

/* 이름(18px)보다 작게 - 카드 안 태그만 컴팩트한 작은 알약으로 */
.profile-card__badges :deep(.base-tag) {
  padding: 2px 9px;
  font-size: 11px;
  font-weight: 600;
}

/* 입대 → 전역 복무 타임라인 (얇은 바 + 중앙의 아거 + 양 끝 날짜) */
.profile-card__timeline {
  display: flex;
  flex-direction: column;
  gap: 18px; /* 아거 발이 놓일 아래 여유 */
  margin-top: 20px; /* 아거 머리가 놓일 위 여유 */
}

.profile-card__progress {
  /* ProgressBar 트랙 자체는 배경이 투명(테두리만)이라
     진한 카드 배경 위에서 잘 안 보이는 문제 방지 — 뒤에 옅은 트랙 배경을 깔아준다 */
  position: relative;
  background-color: var(--overlay-white-30);
  border-radius: 25px;
}

/* 진행 위치의 아거 캐릭터 (바 중앙에서 살짝 위로, 정적 표시) */
.profile-card__progress-marker {
  position: absolute;
  top: 50%;
  width: 54px;
  height: 54px;
  transform: translate(-50%, calc(-50% - 7px));
  object-fit: contain;
  pointer-events: none;
  filter: drop-shadow(0 2px 3px rgba(0, 0, 0, 0.35));
}

.profile-card__timeline-ends {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.profile-card__timeline-end {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.profile-card__timeline-end--right {
  text-align: right;
}

.profile-card__timeline-label {
  font-size: 12px;
  color: var(--military-green-light);
}

.profile-card__timeline-end strong {
  font-size: 14px;
  font-weight: 600;
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

/* ── 휴가 관리 (로드맵처럼 개별 카드) ── */
.vacation-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}


/* 구간 등록 버튼과 동일 - 갈색(chart-1) 알약, 카드 안 우상단 */
.vacation-add-btn {
  flex: none;
  padding: 6px 14px;
  border: none;
  border-radius: 999px;
  background-color: var(--chart-1);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  white-space: nowrap;
  cursor: pointer;
}

.vacation-add-btn:active {
  opacity: 0.85;
}

/* 탭 전환 시 리스트 수에 따라 높이가 확 바뀌어 화면이 흔들리는 것 완화 */
.vacation-lists {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 220px;
}

/* 총 잔여 그래프 카드 - 흰 배경 + 테두리·그림자로 요약 카드 강조 */
.vacation-total-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  border: 1px solid var(--line);
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.08);
}

.vacation-total-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.vacation-total-card__head-main {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.vacation-total-card__label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-hint);
}

.vacation-total-card__value {
  font-size: 26px;
  font-weight: 800;
  line-height: 1;
  color: var(--military-green);
}
.vacation-total-card__value em {
  margin-left: 2px;
  font-size: 15px;
  font-weight: 700;
  font-style: normal;
}

.vacation-total-card__caption {
  margin: 0;
  font-size: 12px;
  color: var(--text-hint);
  text-align: right;
}

/* '전체' 탭에서 두 섹션을 나누는 소제목 (사용 가능 / 사용 완료) */
.vacation-group__title {
  margin: 4px 0 0;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-body);
}

.vacation-empty {
  margin: 0;
  padding: 18px 0;
  text-align: center;
}

/* 로드맵처럼 세로로 쌓인 개별 휴가 카드 */
.vacation-card-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 사용 완료 그룹은 흐리게 */
.vacation-card-list--dimmed {
  opacity: 0.55;
}

.vacation-card {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 0;
  cursor: pointer;
}

.vacation-card__body {
  flex: 1;
  min-width: 0;
}

/* 앞쪽 카테고리 태그 - 작은 군대색 알약 (기존 큰 BaseTag 대신) */
.vacation-tag {
  flex: none;
  min-width: 42px;
  padding: 4px 9px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  text-align: center;
}
/* 총 잔여 바 - 카테고리별 세그먼트 (태그와 같은 군대색, 남은 트랙 = 사용분) */
.vacation-bar {
  display: flex;
  gap: 2px;
  height: 14px;
  border-radius: 999px;
  overflow: hidden;
  background-color: var(--line);
}
.vacation-bar__seg {
  height: 100%;
}

/* 태그별 사용 가능 휴가일 범례 */
.vacation-bar__legend {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  margin: 10px 0 0;
  padding: 0;
  list-style: none;
}
.vacation-bar__legend li {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--text-body);
}
.vacation-bar__legend strong {
  color: var(--text-strong);
  font-weight: 700;
}
.vacation-bar__dot {
  width: 9px;
  height: 9px;
  border-radius: 3px;
}

.vacation-card__name {
  margin: 0;
  overflow: hidden;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vacation-card__sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--text-hint);
}

/* 수정(연필) - 카드 탭(사용 등록)과 구분되게 우상단 고스트 아이콘 */
.vacation-card__edit {
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  border-radius: 8px;
  background-color: var(--surface-subtle);
  color: var(--text-hint);
  cursor: pointer;
}
.vacation-card__edit:active {
  background-color: var(--line);
}

.vacation-card__cta {
  flex: none;
  font-size: 13px;
  font-weight: 700;
  color: var(--military-green);
  white-space: nowrap;
}
.vacation-card__cta--done {
  color: var(--text-hint);
  font-weight: 600;
}

.vacation-card__chevron {
  flex: none;
  color: var(--text-hint);
  font-size: 18px;
  line-height: 1;
}
</style>

<style>
/* D-Day 화면 전체 배경 - 채도 낮춘 부드러운 세이지 그린 (쨍하지 않게, 맑고 은은하게) */
.app-content:has(.dashboard-page) {
  background-color: rgba(120, 152, 130, 0.06);
}
</style>
