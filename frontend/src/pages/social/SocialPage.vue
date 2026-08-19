<script setup>
import { computed, onMounted, ref } from 'vue';
import socialApi from '@/api/socialApi';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseModal from '@/components/common/BaseModal.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import DonutChart from '@/components/common/DonutChart.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import ProgressBar from '@/components/common/ProgressBar.vue';
import { formatWon } from '@/util/format';

import progress50Image from '@/assets/badge/prog_50.png';
import progress75Image from '@/assets/badge/prog_75.png';
import progress100Image from '@/assets/badge/prog_100.png';
import rank1Image from '@/assets/badge/rank_1.png';
import rank5Image from '@/assets/badge/rank_5.png';
import rank10Image from '@/assets/badge/rank_10.png';
import rank30Image from '@/assets/badge/rank_30.png';
import ranking1Image from '@/assets/badge/ranking_1.png';
import ranking2Image from '@/assets/badge/ranking_2.png';
import ranking3Image from '@/assets/badge/ranking_3.png';
import savingMasterImage from '@/assets/badge/saving_master.png';

const SCOPE_OPTIONS = [
  { value: 'ALL', label: '전체' },
  { value: 'TYPE', label: '군종' },
  { value: 'UNIT', label: '부대' },
];

const BADGE_IMAGES = {
  1: rank1Image,
  2: rank5Image,
  3: rank10Image,
  4: rank30Image,
  5: progress50Image,
  6: progress75Image,
  7: progress100Image,
  8: savingMasterImage,
};

const BADGE_NAMES = {
  1: '상위 1%',
  2: '상위 5%',
  3: '상위 10%',
  4: '상위 30%',
  5: '적금 진행 50%',
  6: '적금 진행 75%',
  7: '적금 진행 100%',
  8: '절약 달인',
};

const BADGE_DESCRIPTIONS = {
  1: '같은 계급 장병 중 월급 대비 적금 납입 비율 상위 1% 달성',
  2: '같은 계급 장병 중 월급 대비 적금 납입 비율 상위 5% 달성',
  3: '같은 계급 장병 중 월급 대비 적금 납입 비율 상위 10% 달성',
  4: '같은 계급 장병 중 월급 대비 적금 납입 비율 상위 30% 달성',
  5: '가입한 군적금의 평균 납입 진행률 50% 달성',
  6: '가입한 군적금의 평균 납입 진행률 75% 달성',
  7: '가입한 군적금의 평균 납입 진행률 100% 달성',
  8: '소비 리뷰를 작성하고 지난달 후회 소비를 전월보다 감소',
};

const BADGE_GUIDE_GROUPS = [
  { title: '저축률 뱃지', badgeIds: [1, 2, 3, 4] },
  { title: '적금 진행 뱃지', badgeIds: [5, 6, 7] },
  { title: '소비 습관 뱃지', badgeIds: [8] },
];

// 프로필 카드 대표 뱃지 3칸. 각 배열은 상위 등급 → 하위 등급 순으로 적는다.
// 랭킹은 id가 작을수록 상위(1=상위 1%)이고 진행률은 id가 클수록 상위(7=100%)라 순서가 반대다.
const REPRESENTATIVE_BADGE_TIERS = [
  [1, 2, 3, 4],
  [7, 6, 5],
  [8],
];

const RANKING_IMAGES = [ranking1Image, ranking2Image, ranking3Image];
const ROADMAP_CATEGORY_COLORS = {
  1: 'var(--pastel-blue)',
  2: 'var(--pastel-yellow)',
  3: 'var(--pastel-green)',
  4: 'var(--pastel-pink)',
};

const activeScope = ref('ALL');
const stats = ref(null);
const distribution = ref([]);
const ranking = ref(null);
const badges = ref([]);
const isBadgeGuideOpen = ref(false);
const loading = ref(true);
const errorMessage = ref('');
const badgeErrorMessage = ref('');
const rankingErrorMessage = ref('');
let requestSequence = 0;

const readErrorMessage = (error) =>
  error.response?.data?.message || '저축 비교 정보를 불러오지 못했습니다.';

const badgeList = computed(() => {
  return badges.value.map((badge) => ({
    ...badge,
    name: badge.badgeName.replace(/\s*뱃지$/, ''),
    image: BADGE_IMAGES[badge.badgeId],
    description: BADGE_DESCRIPTIONS[badge.badgeId],
  }));
});

// 계열마다 대표 1개씩. 달성한 것 중 가장 높은 등급을 보여주고,
// 하나도 달성하지 않았으면 그 계열의 최하위 등급을 잠금 상태로 보여준다.
const representativeBadges = computed(() =>
  REPRESENTATIVE_BADGE_TIERS.map((tierIds) => {
    const tiers = tierIds
      .map((tierId) => badgeList.value.find((badge) => badge.badgeId === tierId))
      .filter(Boolean);
    return (
      tiers.find((badge) => badge.achieved) ?? tiers[tiers.length - 1]
    );
  }).filter(Boolean),
);

const chartItems = computed(() =>
  distribution.value
    .filter((item) => item.interestCount > 0)
    .map((item) => ({
      label: item.categoryName,
      value: item.interestCount,
      color:
        ROADMAP_CATEGORY_COLORS[item.categoryId] ?? 'var(--pastel-purple)',
      percentage: item.percentage,
    })),
);

const hasInterestData = computed(() => chartItems.value.length > 0);
const isVeteran = computed(() => stats.value?.veteran === true);
const profileName = computed(() => {
  if (!stats.value) return '';
  if (isVeteran.value || !stats.value.rankName) return `${stats.value.name}님`;
  return `${stats.value.name} ${stats.value.rankName}님`;
});

const savingsPercentileLabel = computed(() => {
  const memberCount = stats.value?.comparisonMemberCount;
  const higherCount = stats.value?.higherSavingsCount;
  const lowerCount = stats.value?.lowerSavingsCount;
  if (!memberCount || higherCount == null || lowerCount == null) return null;
  if (memberCount < 5) return '비교 인원 부족';

  const topPercent = Math.ceil(((higherCount + 1) * 100) / memberCount);
  if (topPercent <= 80) return `상위 ${topPercent}%`;

  const bottomPercent = Math.ceil(((lowerCount + 1) * 100) / memberCount);
  return `하위 ${bottomPercent}%`;
});

const badgeGuideGroups = computed(() =>
  BADGE_GUIDE_GROUPS.map((group) => ({
    ...group,
    badges: group.badgeIds.map((badgeId) => {
      const userBadge = badgeList.value.find(
        (badge) => badge.badgeId === badgeId,
      );
      return {
        badgeId,
        name: BADGE_NAMES[badgeId],
        image: BADGE_IMAGES[badgeId],
        description: BADGE_DESCRIPTIONS[badgeId],
        achieved: userBadge?.achieved ?? null,
      };
    }),
  })),
);

// 비교 모수는 "선택한 범위 + 나와 같은 계급"이다. 계급을 빼고 적으면
// 화면의 순위·평균이 어느 집단 기준인지 오해하게 되므로 문구에 함께 드러낸다.
// 계급 미등록 회원은 서버도 계급 조건을 걸지 않으므로 '장병'으로 되돌린다.
const scopeDescription = computed(() => {
  if (isVeteran.value) {
    if (activeScope.value === 'TYPE') {
      return `${stats.value?.typeName ?? '같은 군종'} 전역자와 비교`;
    }
    if (activeScope.value === 'UNIT') {
      return `${stats.value?.unitName ?? '같은 부대'} 전역자와 비교`;
    }
    return '전체 전역자와 비교';
  }

  const peerLabel = stats.value?.rankName ?? '장병';
  if (activeScope.value === 'TYPE') {
    return `${stats.value?.typeName ?? '같은 군종'} ${peerLabel}과 비교`;
  }
  if (activeScope.value === 'UNIT') {
    return `${stats.value?.unitName ?? '같은 부대'} ${peerLabel}과 비교`;
  }
  return `전체 ${peerLabel}과 비교`;
});

// 로드맵 관심도 섹션 문구. 비교 범위에 따라 집계 대상을 그대로 읽어주도록 바꾼다.
// 군종 미등록 회원은 typeName 이 없으므로 군종명만 빼고 기본 문구로 되돌린다.
const interestOverline = computed(() => {
  if (activeScope.value === 'TYPE' && stats.value?.typeName) {
    return `${stats.value.typeName} ${isVeteran.value ? '전역자' : '장병'}들이 저장한 목표`;
  }
  if (activeScope.value === 'UNIT') {
    return isVeteran.value
      ? '같은 부대 전역자들이 저장한 목표'
      : '부대원들이 저장한 목표';
  }
  return `${isVeteran.value ? '전역자' : '장병'}들이 저장한 목표`;
});

const rankingMetricValue = (item) =>
  item.metricValue ?? item.savingsRate ?? 0;

// 뱃지는 이 화면의 보조 정보라, 실패해도 비교 통계는 계속 보여준다.
const loadBadges = async () => {
  badgeErrorMessage.value = '';
  try {
    badges.value = (await socialApi.findBadgeList()) ?? [];
  } catch (error) {
    badgeErrorMessage.value =
      error.response?.data?.message || '뱃지 정보를 불러오지 못했습니다.';
  }
};

// 하단 랭킹은 비교 범위와 무관하게 항상 같은 결과라 최초 1회만 불러온다.
const loadRanking = async () => {
  rankingErrorMessage.value = '';
  try {
    ranking.value = await socialApi.findRanking(activeScope.value);
  } catch (error) {
    rankingErrorMessage.value =
      error.response?.data?.message || '랭킹 정보를 불러오지 못했습니다.';
  }
};

const loadComparison = async (scope) => {
  const currentSequence = ++requestSequence;
  loading.value = true;
  errorMessage.value = '';

  try {
    const [statsResult, distributionResult] = await Promise.all([
      socialApi.findStats(scope),
      socialApi.findDistributionList(scope),
    ]);

    if (currentSequence !== requestSequence) return true;
    stats.value = statsResult;
    distribution.value = distributionResult ?? [];
    return true;
  } catch (error) {
    if (currentSequence !== requestSequence) return true;
    errorMessage.value = readErrorMessage(error);
    return false;
  } finally {
    if (currentSequence === requestSequence) {
      loading.value = false;
    }
  }
};


const selectScope = async (scope) => {
  if (activeScope.value === scope) return;
  const previousScope = activeScope.value;
  activeScope.value = scope;
  if (!(await loadComparison(scope))) {
    activeScope.value = previousScope;
  }
};

const retry = async () => {
  await Promise.all([
    loadBadges(),
    loadRanking(),
    loadComparison(activeScope.value),
  ]);
};

onMounted(retry);
</script>

<template>
  <main class="social-page">
    <header class="page-header">
      <p class="text-overline">
        {{ isVeteran ? '복무 중 쌓은 저축 기록' : '함께 만드는 저축 습관' }}
      </p>
      <h1 class="text-title">
        {{ isVeteran ? '복무 저축 리포트' : '저축 비교' }}
      </h1>
    </header>

    <div v-if="loading && !stats" class="status text-caption" role="status">
      저축 비교 정보를 불러오고 있습니다.
    </div>

    <EmptyState
      v-else-if="errorMessage && !stats"
      title="정보를 불러오지 못했습니다."
      :description="errorMessage"
    >
      <template #action>
        <button type="button" class="retry-button" @click="retry">
          다시 시도
        </button>
      </template>
    </EmptyState>

    <template v-else-if="stats">
      <BaseCard class="profile-card" padding="20px 18px">
        <div class="profile-card__headline">
          <div>
            <p class="profile-card__name">
              {{ profileName }}
            </p>
            <p class="profile-card__unit text-caption">
              {{ stats.typeName || '군종 미등록' }} ·
              {{ stats.unitName || '부대 미등록' }}
              <template v-if="isVeteran"> · 전역</template>
            </p>
          </div>
          <div class="profile-card__aside">
            <button
              type="button"
              class="badge-help-button"
              aria-label="뱃지 종류와 획득 방법 보기"
              title="뱃지 안내"
              @click="isBadgeGuideOpen = true"
            >
              ?
            </button>
            <div class="profile-card__saving">
              <span>{{ isVeteran ? '복무 중 누적 납입액' : '현재 납입액' }}</span>
              <strong>
                {{
                  formatWon(
                    isVeteran
                      ? stats.totalContribution
                      : stats.currentSavings,
                  )
                }}
              </strong>
            </div>
          </div>
        </div>

        <div class="profile-card__badges" aria-label="대표 뱃지">
          <div
            v-for="badge in representativeBadges"
            :key="badge.badgeId"
            class="profile-badge"
            :class="{ 'is-locked': !badge.achieved }"
          >
            <div class="profile-badge__image-wrap">
              <img :src="badge.image" :alt="badge.name" />
              <span v-if="!badge.achieved" aria-hidden="true">잠금</span>
            </div>
            <p>{{ badge.name }}</p>
          </div>
        </div>

        <p
          v-if="badgeErrorMessage"
          class="profile-card__badge-error"
          role="alert"
        >
          {{ badgeErrorMessage }}
        </p>
      </BaseCard>

      <nav class="scope-tabs" aria-label="저축 비교 범위">
        <CategoryButton
          v-for="option in SCOPE_OPTIONS"
          :key="option.value"
          variant="oval-green"
          :label="option.label"
          :active="activeScope === option.value"
          @click="selectScope(option.value)"
        />
      </nav>

      <p class="scope-description text-caption">{{ scopeDescription }}</p>

      <section class="comparison-section" aria-labelledby="comparison-title">
        <div class="section-heading">
          <div>
            <p class="text-overline">
              {{ isVeteran ? '복무 기간 동안의 결과' : '나와 평균의 차이' }}
            </p>
            <h2 id="comparison-title" class="text-title">
              {{ isVeteran ? '복무 저축 성과' : '저축률 비교' }}
            </h2>
          </div>
          <p v-if="savingsPercentileLabel" class="rank-summary">
            <strong>{{ savingsPercentileLabel }}</strong>
          </p>
        </div>

        <BaseCard class="comparison-card" padding="18px">
          <template v-if="isVeteran">
            <div class="veteran-comparison-row">
              <p>누적 납입액</p>
              <div>
                <span>나</span>
                <strong>{{ formatWon(stats.totalContribution) }}</strong>
              </div>
              <div>
                <span>그룹 평균</span>
                <strong>{{ formatWon(stats.peerAverageTotalContribution) }}</strong>
              </div>
            </div>

            <div class="veteran-comparison-row">
              <p>월평균 납입액</p>
              <div>
                <span>나</span>
                <strong>{{ formatWon(stats.averageMonthlyContribution) }}</strong>
              </div>
              <div>
                <span>그룹 평균</span>
                <strong>
                  {{ formatWon(stats.peerAverageMonthlyContribution) }}
                </strong>
              </div>
            </div>

            <div class="comparison-row">
              <div class="comparison-row__label">
                <span>나의 군적금 완주율</span>
                <strong>{{ stats.savingsCompletionRate.toFixed(1) }}%</strong>
              </div>
              <ProgressBar
                :value="stats.savingsCompletionRate"
                :total="100"
                color="var(--military-green)"
                :height="12"
              />
            </div>
            <div class="comparison-row">
              <div class="comparison-row__label">
                <span>비교 그룹 평균 완주율</span>
                <strong>{{ stats.peerAverageCompletionRate.toFixed(1) }}%</strong>
              </div>
              <ProgressBar
                :value="stats.peerAverageCompletionRate"
                :total="100"
                color="var(--chart-3)"
                :height="12"
              />
            </div>
            <p class="veteran-account-summary">
              만기 완료 계좌
              <strong>{{ stats.maturedAccountCount }}개</strong>
            </p>
            <p class="comparison-card__caption text-caption">
              완주율은 복무 중 유지한 군적금의 납입 회차와 만기 여부를
              기준으로 계산합니다.
            </p>
          </template>

          <template v-else>
            <div class="comparison-row">
              <div class="comparison-row__label">
                <span>나</span>
                <strong>{{ stats.savingsRate.toFixed(1) }}%</strong>
              </div>
              <ProgressBar
                :value="stats.savingsRate"
                :total="100"
                color="var(--military-green)"
                :height="12"
              />
            </div>
            <div class="comparison-row">
              <div class="comparison-row__label">
                <span>비교 그룹 평균</span>
                <strong>{{ stats.averageSavingsRate.toFixed(1) }}%</strong>
              </div>
              <ProgressBar
                :value="stats.averageSavingsRate"
                :total="100"
                color="var(--chart-3)"
                :height="12"
              />
            </div>
            <p class="comparison-card__caption text-caption">
              현재 월 적금 납입액을 계급 월급으로 나눈 비율입니다.
            </p>
          </template>
        </BaseCard>
      </section>

      <section class="interest-section" aria-labelledby="interest-title">
        <div class="section-heading">
          <div>
            <p class="text-overline">{{ interestOverline }}</p>
            <h2 id="interest-title" class="text-title">로드맵 관심도</h2>
          </div>
        </div>

        <BaseCard v-if="hasInterestData" class="interest-card" padding="18px">
          <DonutChart
            :items="chartItems"
            :size="164"
            :thickness="30"
            chart-label="로드맵 카테고리별 관심도"
          />
          <ul class="interest-legend">
            <li v-for="item in chartItems" :key="item.label">
              <span
                class="interest-legend__dot"
                :style="{ backgroundColor: item.color }"
              />
              <span>{{ item.label }}</span>
              <strong>{{ item.percentage.toFixed(1) }}%</strong>
            </li>
          </ul>
        </BaseCard>
        <BaseCard v-else padding="22px">
          <p class="empty-caption text-caption">
            아직 비교할 로드맵 관심 데이터가 없습니다.
          </p>
        </BaseCard>
      </section>

      <section class="ranking-section" aria-labelledby="ranking-title">
        <div class="section-heading">
          <div>
            <p class="text-overline">함께 모으는 사람들</p>
            <h2 id="ranking-title" class="text-title">
              {{ ranking?.title }}
            </h2>
          </div>
          <p v-if="ranking?.myUnitRank" class="rank-summary">
            <span>내 부대 </span>
            <strong>{{ ranking.myUnitRank }}위</strong>
          </p>
        </div>

        <ol v-if="ranking?.rankings?.length" class="ranking-list">
          <li
            v-for="item in ranking.rankings"
            :key="`${item.rank}-${item.label}`"
          >
            <BaseCard
              class="ranking-card"
              :class="{ 'is-me': item.me }"
              padding="12px 14px"
            >
              <img
                :src="RANKING_IMAGES[item.rank - 1]"
                :alt="`${item.rank}위`"
              />
              <div class="ranking-card__content">
                <strong>{{ item.label }}</strong>
                <span v-if="item.me">내가 속한 그룹</span>
              </div>
              <p>{{ rankingMetricValue(item).toFixed(1) }}%</p>
            </BaseCard>
          </li>
        </ol>
        <BaseCard v-else padding="22px">
          <p
            class="empty-caption text-caption"
            :class="{ 'inline-error': rankingErrorMessage }"
            :role="rankingErrorMessage ? 'alert' : null"
          >
            {{ rankingErrorMessage || '아직 랭킹 데이터가 없습니다.' }}
          </p>
        </BaseCard>
      </section>

      <p v-if="loading" class="refreshing text-caption" role="status">
        선택한 범위의 정보를 갱신하고 있습니다.
      </p>
      <p v-if="errorMessage" class="inline-error text-caption" role="alert">
        {{ errorMessage }}
      </p>
    </template>

    <BaseModal
      v-model="isBadgeGuideOpen"
      title="뱃지 종류와 획득 방법"
      confirm-text="확인"
    >
      <div class="badge-guide">
        <section
          v-for="group in badgeGuideGroups"
          :key="group.title"
          class="badge-guide__group"
        >
          <h4>{{ group.title }}</h4>
          <ul>
            <li v-for="badge in group.badges" :key="badge.badgeId">
              <img :src="badge.image" :alt="badge.name" />
              <div>
                <div class="badge-guide__heading">
                  <strong>{{ badge.name }}</strong>
                  <span :class="{ 'is-achieved': badge.achieved }">
                    {{
                      badge.achieved == null
                        ? '상태 확인 불가'
                        : badge.achieved
                          ? '획득'
                          : '미획득'
                    }}
                  </span>
                </div>
                <p>{{ badge.description }}</p>
              </div>
            </li>
          </ul>
        </section>
        <p class="badge-guide__note text-caption">
          중도 해지한 적금은 납입 진행률 뱃지 계산에서 제외됩니다.
        </p>
      </div>
    </BaseModal>
  </main>
</template>

<style scoped>
.social-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
  padding: 20px 0 40px;
  color: var(--text-strong);
}

.page-header p,
.page-header h1,
.section-heading p,
.section-heading h2 {
  margin: 0;
}

.page-header h1,
.section-heading h2 {
  margin-top: 4px;
}

.status,
.empty-caption,
.refreshing,
.inline-error {
  margin: 0;
  text-align: center;
}

.status {
  padding: 72px 16px;
}

.inline-error {
  color: var(--danger);
}

.retry-button {
  padding: 9px 18px;
  border: 0;
  border-radius: 999px;
  background: var(--military-green);
  color: var(--surface-default);
  font: inherit;
  cursor: pointer;
}

.profile-card {
  background: var(--military-green);
  color: var(--surface-default);
}

.profile-card__headline {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.profile-card__aside {
  display: flex;
  flex: none;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.badge-help-button {
  display: grid;
  width: 24px;
  height: 24px;
  padding: 0;
  border: 1px solid rgba(255, 255, 255, 0.75);
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.14);
  color: var(--surface-default);
  font: inherit;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  place-items: center;
}

.badge-help-button:focus-visible {
  outline: 2px solid var(--surface-default);
  outline-offset: 2px;
}

.profile-card__name {
  margin: 0;
  font-size: 19px;
  font-weight: 700;
}

.profile-card__unit {
  margin: 5px 0 0;
  color: var(--military-green-light);
}

.profile-card__saving {
  display: flex;
  flex: none;
  flex-direction: column;
  text-align: right;
}

.profile-card__saving span {
  color: var(--military-green-light);
  font-size: 11px;
}

.profile-card__saving strong {
  margin-top: 3px;
  font-size: 16px;
}

/* 뱃지 조회만 실패한 경우의 안내. 비교 통계는 정상이므로 카드 안에서만 알린다. */
.profile-card__badge-error {
  margin: 12px 0 0;
  color: var(--military-green-light);
  font-size: 11px;
  text-align: center;
}

.profile-card__badges {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-top: 18px;
}

.profile-badge {
  min-width: 0;
  text-align: center;
}

.profile-badge__image-wrap {
  position: relative;
  margin: 0 auto;
}

.profile-badge__image-wrap {
  width: 64px;
  height: 64px;
}

.profile-badge img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.profile-badge p {
  margin: 5px 0 0;
  overflow: hidden;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-badge.is-locked img {
  filter: grayscale(1);
  opacity: 0.35;
}

.profile-badge__image-wrap span {
  position: absolute;
  inset: 0;
  display: grid;
  color: var(--surface-default);
  font-size: 10px;
  font-weight: 700;
  place-items: center;
}

.badge-guide {
  max-height: 58vh;
  padding-right: 2px;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.badge-guide::-webkit-scrollbar {
  display: none;
}

.badge-guide__group + .badge-guide__group {
  margin-top: 18px;
}

.badge-guide__group h4 {
  margin: 0 0 8px;
  color: var(--text-strong);
  font-size: 14px;
}

.badge-guide__group ul {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.badge-guide__group li {
  display: grid;
  grid-template-columns: 46px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  padding: 9px;
  border-radius: 10px;
  background: var(--surface-subtle);
}

.badge-guide__group img {
  width: 46px;
  height: 46px;
  object-fit: contain;
}

.badge-guide__heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.badge-guide__heading strong {
  color: var(--text-strong);
  font-size: 13px;
}

.badge-guide__heading span {
  flex: none;
  color: var(--text-hint);
  font-size: 10px;
}

.badge-guide__heading span.is-achieved {
  color: var(--military-green);
  font-weight: 700;
}

.badge-guide__group p {
  margin: 3px 0 0;
  color: var(--text-muted);
  font-size: 11px;
  line-height: 1.45;
}

.badge-guide__note {
  margin: 14px 0 0;
}

.scope-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.scope-description {
  margin: -14px 0 0;
  text-align: center;
}

.comparison-section,
.interest-section,
.ranking-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.rank-summary {
  margin: 0;
  color: var(--text-hint);
  font-size: 12px;
  text-align: right;
  white-space: nowrap;
}

.rank-summary strong {
  color: var(--military-green);
  font-size: 17px;
}

.comparison-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.comparison-row {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.comparison-row__label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

.comparison-row__label strong {
  font-size: 15px;
}

.comparison-card__caption {
  margin: -2px 0 0;
}

.veteran-comparison-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  align-items: end;
  gap: 12px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--line);
}

.veteran-comparison-row p,
.veteran-comparison-row span,
.veteran-comparison-row strong,
.veteran-account-summary {
  margin: 0;
}

.veteran-comparison-row p {
  align-self: center;
  color: var(--text-strong);
  font-size: 13px;
  font-weight: 500;
}

.veteran-comparison-row div {
  display: flex;
  flex-direction: column;
  gap: 3px;
  text-align: right;
}

.veteran-comparison-row span {
  color: var(--text-hint);
  font-size: 10px;
}

.veteran-comparison-row strong {
  color: var(--text-strong);
  font-size: 13px;
}

.veteran-account-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11px 13px;
  border-radius: 10px;
  background: var(--surface-subtle);
  color: var(--text-body);
  font-size: 12px;
}

.veteran-account-summary strong {
  color: var(--military-green);
  font-size: 15px;
}

.interest-card {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 14px;
}

.interest-legend {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 9px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.interest-legend li {
  display: grid;
  grid-template-columns: 10px 1fr auto;
  align-items: center;
  gap: 7px;
  color: var(--text-body);
  font-size: 12px;
}

.interest-legend__dot {
  width: 10px;
  height: 10px;
  border-radius: 3px;
}

.interest-legend strong {
  color: var(--text-strong);
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 9px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.ranking-card {
  display: grid;
  grid-template-columns: 44px 1fr auto;
  align-items: center;
  gap: 11px;
}

.ranking-card.is-me {
  border-color: var(--military-green);
  background: var(--military-green-light);
}

.ranking-card img {
  width: 44px;
  height: 44px;
  object-fit: contain;
}

.ranking-card__content {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.ranking-card__content strong {
  overflow: hidden;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ranking-card__content span {
  margin-top: 2px;
  color: var(--military-green);
  font-size: 10px;
}

.ranking-card > p {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
}

@media (max-width: 380px) {
  .profile-card__headline {
    flex-direction: column;
  }

  .profile-card__saving {
    text-align: left;
  }

  .interest-card {
    grid-template-columns: 1fr;
    justify-items: center;
  }

  .interest-legend {
    width: 100%;
  }
}
</style>

<style>
.app-content:has(.social-page) {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.app-content:has(.social-page)::-webkit-scrollbar {
  display: none;
}
</style>
