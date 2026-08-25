<script setup>
// SCR-ROAD-01 · 로드맵 메인  (담당: 지원)
// 카테고리별 목표 진입 + 저장된 로드맵 게시판 (관심등록)
import { ref, computed, onMounted } from 'vue';

import { useRouter } from 'vue-router';
import { useToast } from '@/composables/useToast';

import BaseCard from '@/components/common/BaseCard.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import CategoryFilter from '@/components/common/CategoryFilter.vue';
import LikeButton from '@/components/common/LikeButton.vue';
import BaseModal from '@/components/common/BaseModal.vue';
import BaseTag from '@/components/common/BaseTag.vue';

import travelImage from '@/assets/images/roadmap/travel-1.png';
import carImage from '@/assets/images/roadmap/car-2.png';
import jobImage from '@/assets/images/roadmap/job-1.png';
import rentImage from '@/assets/images/roadmap/rent-1.png';
import roadmapApi from '@/api/roadmapApi';
import EmptyState from '@/components/common/EmptyState.vue';
import bookmarkApi from '@/api/bookmarkApi';

const router = useRouter();

const selectedCategory = ref('');
const selectedRoadmapCategory = ref('ALL');

const roadmaps = ref([]);
const isLoading = ref(false);
const visibleCount = ref(4);
const isDeleteModalOpen = ref(false);
const deleteTarget = ref(null);
const isDeleting = ref(false);

const { show } = useToast();

const emptyStateTitle = computed(
  () => emptyStateMap[selectedRoadmapCategory.value].title,
);

const emptyStateDescription = computed(
  () => emptyStateMap[selectedRoadmapCategory.value].description,
);

const emptyStateImage = computed(
  () => emptyStateMap[selectedRoadmapCategory.value].image,
);

const visibleRoadmaps = computed(() =>
  roadmaps.value.slice(0, visibleCount.value),
);

const hasMoreRoadmaps = computed(
  () => visibleCount.value < roadmaps.value.length,
);

const emptyStateMap = {
  ALL: {
    title: '아직 저장한 목표가 없어요',
    description: '위의 관심있는 카테고리를 눌러 나만의 로드맵을 시작해보세요',
    image: null,
  },
  TRAVEL: {
    title: '여행 목표가 아직 없어요',
    description: '여행 카드에서 목표를 등록하면 여기에 표시돼요',
    image: travelImage,
  },
  JOB: {
    title: '진로 목표가 아직 없어요',
    description: '진로 카드에서 목표를 등록하면 여기에 표시돼요',
    image: jobImage,
  },
  CAR: {
    title: '자동차 목표가 아직 없어요',
    description: '자동차 카드에서 목표를 등록하면 여기에 표시돼요',
    image: carImage,
  },
  RENT: {
    title: '자취 목표가 아직 없어요',
    description: '자취 카드에서 목표를 등록하면 여기에 표시돼요',
    image: rentImage,
  },
};

// 타이밍별 그룹: 여행·진로는 복무 중에 시작 가능, 자동차·자취는 전역 후
const categoryGroups = [
  {
    key: 'IN_SERVICE',
    items: [
      {
        code: 'TRAVEL',
        title: '추억 쌓으러',
        description: '복무 중 설레는 계획',
        image: travelImage,
        routeName: 'TravelGoalCreate',
      },
      {
        code: 'JOB',
        title: '꿈 찾으러',
        description: '복무 중 미래 그리기',
        image: jobImage,
        routeName: 'JobGoalCreate',
      },
    ],
  },
  {
    key: 'AFTER_DISCHARGE',
    items: [
      {
        code: 'CAR',
        title: '차 뽑으러',
        description: '전역 후 드디어 내 차',
        image: carImage,
        routeName: 'CarGoalCreate',
      },
      {
        code: 'RENT',
        title: '자취 하러',
        description: '전역 후 나만의 공간',
        image: rentImage,
        routeName: 'RentGoalCreate',
      },
    ],
  },
];

const roadmapFilters = [
  { code: 'ALL', label: '전체', theme: 'roadmap' },
  { code: 'TRAVEL', label: '여행', theme: 'travel' },
  { code: 'RENT', label: '자취', theme: 'rent' },
  { code: 'CAR', label: '자동차', theme: 'car' },
  { code: 'JOB', label: '진로', theme: 'job' },
];

const categoryMap = {
  1: {
    code: 'TRAVEL',
    label: '여행',
  },
  2: {
    code: 'JOB',
    label: '진로',
  },
  3: {
    code: 'CAR',
    label: '자동차',
  },
  4: {
    code: 'RENT',
    label: '자취',
  },
};

const mapRoadmapItem = (item) => {
  const category = categoryMap[item.categoryId];
  // categoryMap 에 없는 카테고리는 스킵 (전체 목록 조회 시 map 에러로 리스트 전체가 사라지는 것 방지)
  if (!category) return null;

  return {
    goalId: item.goalId,
    bookmarkId: item.bookmarkId,
    categoryId: item.categoryId,
    categoryCode: category.code,
    categoryLabel: category.label,
    title: item.title,
    description: item.createdDate,
    detail: item.detail,
    liked: item.bookmarked,
  };
};

const fetchRoadmaps = async (category = 'ALL') => {
  isLoading.value = true;

  try {
    const result = await roadmapApi.findRoadmapList(category.toLowerCase());

    roadmaps.value = result
      .map(mapRoadmapItem)
      .filter(Boolean)
      .sort((a, b) => Number(b.liked) - Number(a.liked));
      
    visibleCount.value = 4;
  } catch (e) {
    console.error(e);
    roadmaps.value = [];
  } finally {
    isLoading.value = false;
  }
};

const toggleBookmark = async (roadmap) => {
  try {
    if (roadmap.liked) {
      await bookmarkApi.deleteBookmark(roadmap.bookmarkId);

      roadmap.liked = false;
      roadmap.bookmarkId = null;
    } else {
      const bookmarkId = await bookmarkApi.createBookmark({
        categoryId: roadmap.categoryId,
        goalId: roadmap.goalId,
      });

      roadmap.bookmarkId = bookmarkId;
      roadmap.liked = true;
    }
  } catch (error) {
    console.error('관심 로드맵 처리 실패:', error);
  } finally {
    // 별도 로딩 상태 없음
  }
};

// 삭제할 로드맵 선택
const openDeleteModal = (roadmap) => {
  deleteTarget.value = roadmap;
  isDeleteModalOpen.value = true;
};

// 로드맵 삭제
const handleDeleteRoadmap = async () => {
  if (!deleteTarget.value || isDeleting.value) {
    return;
  }

  try {
    isDeleting.value = true;

    await roadmapApi.deleteRoadmapGoal(
      deleteTarget.value.goalId,
      deleteTarget.value.categoryId,
    );

    isDeleteModalOpen.value = false;
    deleteTarget.value = null;

    show('로드맵이 삭제되었습니다.', 'success');

    await fetchRoadmaps(selectedRoadmapCategory.value);
  } catch (error) {
    console.error('로드맵 삭제 실패:', error);

    show('로드맵을 삭제하지 못했습니다.', 'error');
  } finally {
    isDeleting.value = false;
  }
};

const showMoreRoadmaps = () => {
  visibleCount.value += 4;
};

// 카테고리 → 도메인 칩 색상 (여행/진로/자동차/자취)
const CHIP_VARIANT = { TRAVEL: 'travel', JOB: 'job', CAR: 'car', RENT: 'rent' };
const chipVariant = (roadmap) => CHIP_VARIANT[roadmap.categoryCode] || 'gray';

const goRoadmapDetail = (roadmap) => {
  switch (roadmap.categoryCode) {
    case 'TRAVEL':
      router.push({
        name: 'TravelGoalDetail',
        params: { goalId: roadmap.goalId },
      });
      break;

    case 'JOB':
      router.push({
        name: 'JobGoalDetail',
        params: { goalId: roadmap.goalId },
      });
      break;

    case 'CAR':
      router.push({
        name: 'CarGoalDetail',
        params: { goalId: roadmap.goalId },
      });
      break;

    case 'RENT':
      router.push({
        name: 'RentGoalDetail',
        params: { goalId: roadmap.goalId },
      });
      break;

    default:
      console.warn('지원하지 않는 카테고리입니다.', roadmap);
  }
};

const selectCategory = (category) => {
  selectedCategory.value = category.code;

  router.push({
    name: category.routeName,
  });
};

const selectRoadmapCategory = async (categoryCode) => {
  selectedRoadmapCategory.value = categoryCode;

  visibleCount.value = 4;

  await fetchRoadmaps(categoryCode);
};

onMounted(async () => {
  await fetchRoadmaps('ALL');
});
</script>

<template>
  <div class="roadmap-main">
    <section class="roadmap-main__intro">
      <PageHeader
        eyebrow="군월급·군적금, 어디에 쓸까?"
        title="전역 로드맵 작전"
        size="lg"
      />
    </section>

    <section
      v-for="group in categoryGroups"
      :key="group.key"
      class="roadmap-main__category-group"
    >
      <div class="roadmap-main__categories">
        <BaseCard
          v-for="category in group.items"
          :key="category.code"
          padding="10px"
          class="roadmap-main__category-card"
          @click="selectCategory(category)"
        >
          <!-- 전술 코너 마크 (HUD/조준경 프레임) -->
          <span
            class="roadmap-main__corner roadmap-main__corner--tl"
            aria-hidden="true"
          ></span>
          <span
            class="roadmap-main__corner roadmap-main__corner--tr"
            aria-hidden="true"
          ></span>
          <span
            class="roadmap-main__corner roadmap-main__corner--bl"
            aria-hidden="true"
          ></span>
          <span
            class="roadmap-main__corner roadmap-main__corner--br"
            aria-hidden="true"
          ></span>

          <!-- 아이콘 조준경 링 (타겟 조준) -->
          <div class="roadmap-main__scope">
            <span
              class="roadmap-main__scope-tick roadmap-main__scope-tick--t"
              aria-hidden="true"
            ></span>
            <span
              class="roadmap-main__scope-tick roadmap-main__scope-tick--b"
              aria-hidden="true"
            ></span>
            <span
              class="roadmap-main__scope-tick roadmap-main__scope-tick--l"
              aria-hidden="true"
            ></span>
            <span
              class="roadmap-main__scope-tick roadmap-main__scope-tick--r"
              aria-hidden="true"
            ></span>
            <img
              :src="category.image"
              :alt="category.title"
              class="roadmap-main__category-image"
            />
          </div>

          <p class="text-label roadmap-main__category-title">
            {{ category.title }}
          </p>

          <p class="text-caption roadmap-main__category-description">
            {{ category.description }}
          </p>
        </BaseCard>
      </div>
    </section>

    <section class="roadmap-main__saved">
      <div class="roadmap-main__saved-heading">
        <p class="text-overline roadmap-main__saved-eyebrow">
          어떤 작전 세웠지?
        </p>
        <h2 class="text-title roadmap-main__saved-title">나의 로드맵 작전</h2>
      </div>

      <div class="roadmap-main__filters">
        <CategoryFilter
          :model-value="selectedRoadmapCategory"
          :categories="roadmapFilters"
          @update:model-value="selectRoadmapCategory"
        />
      </div>

      <div class="roadmap-main__saved-list">
        <p v-if="isLoading" class="roadmap-main__status">불러오는 중...</p>

        <EmptyState
          v-else-if="roadmaps.length === 0"
          :title="emptyStateTitle"
          :description="emptyStateDescription"
        >
          <template v-if="selectedRoadmapCategory !== 'ALL'" #icon>
            <img
              :src="emptyStateImage"
              :alt="`${emptyStateTitle} 아이콘`"
              class="roadmap-main__empty-image"
            />
          </template>
        </EmptyState>

        <template v-else>
          <BaseCard
            v-for="roadmap in visibleRoadmaps"
            :key="`${roadmap.categoryId}-${roadmap.goalId}`"
            padding="12px 14px"
            class="roadmap-main__saved-card"
            @click="goRoadmapDetail(roadmap)"
          >
            <div class="saved-card">
              <div class="saved-card__content">
                <BaseTag
                  class="saved-card__chip"
                  :label="roadmap.categoryLabel"
                  :variant="chipVariant(roadmap)"
                />

                <div class="saved-card__title-row">
                  <strong class="saved-card__title">
                    {{ roadmap.title }}
                  </strong>

                  <!-- 관심 등록 -->
                  <div class="saved-card__like" @click.stop>
                    <LikeButton
                      :model-value="roadmap.liked"
                      @update:model-value="toggleBookmark(roadmap)"
                    />
                  </div>
                </div>

                <p
                  v-if="roadmap.description || roadmap.detail"
                  class="saved-card__description"
                >
                  {{
                    [roadmap.description, roadmap.detail]
                      .filter(Boolean)
                      .join(' · ')
                  }}
                </p>
              </div>

              <!-- 삭제 -->
              <button
                type="button"
                class="saved-card__delete"
                aria-label="로드맵 삭제"
                @click.stop="openDeleteModal(roadmap)"
              >
                <svg
                  class="saved-card__delete-icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  aria-hidden="true"
                >
                  <path
                    d="M3 6h18"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                  />
                  <path
                    d="M8 6V4.5C8 3.67 8.67 3 9.5 3h5c.83 0 1.5.67 1.5 1.5V6"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                  <path
                    d="M19 6l-.7 13a2 2 0 0 1-2 1.9H7.7a2 2 0 0 1-2-1.9L5 6"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                  <path
                    d="M10 10v7M14 10v7"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                  />
                </svg>
              </button>
            </div>
          </BaseCard>
          <button
            v-if="hasMoreRoadmaps"
            type="button"
            class="roadmap-main__more-button"
            @click="showMoreRoadmaps"
          >
            + 더보기
          </button>
        </template>
      </div>
    </section>
    <!-- 로드맵 삭제 확인 -->
    <BaseModal
      v-model="isDeleteModalOpen"
      title="로드맵 삭제"
      confirm-text="삭제"
      :confirm-disabled="isDeleting"
      @confirm="handleDeleteRoadmap"
    >
      <p class="roadmap-main__modal-message">이 로드맵을 삭제하시겠습니까?</p>

      <p class="roadmap-main__modal-description">
        삭제한 로드맵은 목록에서 더 이상 확인할 수 없습니다.
      </p>
    </BaseModal>
  </div>
</template>

<style scoped>
.roadmap-main {
  padding: 16px 20px 96px;
}

.roadmap-main__intro {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

/* 타이밍 그룹 (복무 중에 시작 / 전역 후를 준비) */
.roadmap-main__category-group {
  margin-top: 26px;
}

/* 두 번째 그룹은 위 카드 줄과 좌우 간격(18px)과 동일하게 → 균일한 2x2 그리드 */
.roadmap-main__category-group + .roadmap-main__category-group {
  margin-top: 18px;
}

.roadmap-main__categories {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
}

.roadmap-main__category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 22px 12px; /* 정사각(aspect-ratio) 제거 → 내용 기준 높이 + 적당한 여백 */
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.roadmap-main__category-card:active {
  transform: scale(0.98);
}

/* 호버/선택 - 노란 테두리(box-shadow 링이라 레이아웃 안 밀림, 타겟 락온 느낌) */
:deep(.roadmap-main__category-card:hover.base-card),
:deep(.roadmap-main__category-card:active.base-card) {
  box-shadow:
    0 0 0 2px var(--kb-yellow),
    0 2px 12px rgba(0, 0, 0, 0.06);
}

.roadmap-main__category-image {
  width: 28px;
  height: 28px;
  margin: 0;
  object-fit: contain;
}

.roadmap-main__category-title,
.roadmap-main__category-description {
  margin: 0;
  text-align: center;
}

/* 제목 조금 키움 (text-label 14px → 16px) */
.roadmap-main__category-title {
  font-size: 16px;
  font-weight: 700;
}

.roadmap-main__category-description {
  color: var(--text-muted);
  line-height: 1.4;
  word-break: keep-all;
}

/* 카드: 흰 배경 + 노란 테두리. hover/선택 시 테두리만 진하게.
   테두리 두께를 1.5px로 고정해 hover 시 크기 변화로 스크롤이 흔들리던 문제 방지. */
:deep(.roadmap-main__category-card.base-card) {
  position: relative;
  border: none;
  border-radius: 8px;
  background-color: #fff;
}

/* 전술 코너 마크 - 카드 네 모서리 국방색 꺾쇠 (조준 프레임) */
.roadmap-main__corner {
  position: absolute;
  width: 14px;
  height: 14px;
  border: 2.5px solid #8b9a80;
  z-index: 1;
  pointer-events: none;
}
.roadmap-main__corner--tl {
  top: 4px;
  left: 4px;
  border-right: 0;
  border-bottom: 0;
}
.roadmap-main__corner--tr {
  top: 4px;
  right: 4px;
  border-left: 0;
  border-bottom: 0;
}
.roadmap-main__corner--bl {
  bottom: 4px;
  left: 4px;
  border-right: 0;
  border-top: 0;
}
.roadmap-main__corner--br {
  bottom: 4px;
  right: 4px;
  border-left: 0;
  border-top: 0;
}

/* 아이콘 조준경 링 (타겟 조준) - 원 + 십자 눈금 */
.roadmap-main__scope {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  margin: 4px auto;
}
.roadmap-main__scope::before {
  content: '';
  position: absolute;
  inset: 0;
  border: 2px solid #8b9a80;
  border-radius: 50%;
  pointer-events: none;
}
.roadmap-main__scope-tick {
  position: absolute;
  background: #8b9a80;
  pointer-events: none;
}
.roadmap-main__scope-tick--t {
  top: -4px;
  left: 50%;
  width: 2px;
  height: 8px;
  transform: translateX(-50%);
}
.roadmap-main__scope-tick--b {
  bottom: -4px;
  left: 50%;
  width: 2px;
  height: 8px;
  transform: translateX(-50%);
}
.roadmap-main__scope-tick--l {
  left: -4px;
  top: 50%;
  width: 8px;
  height: 2px;
  transform: translateY(-50%);
}
.roadmap-main__scope-tick--r {
  right: -4px;
  top: 50%;
  width: 8px;
  height: 2px;
  transform: translateY(-50%);
}

.roadmap-main__saved {
  margin-top: 40px;
}

.roadmap-main__saved-eyebrow {
  margin: 0;
  /* 소제목(eyebrow) 목돈작전 PageHeader 기준(13px/600/muted)으로 통일 */
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
}

.roadmap-main__saved-title {
  margin: 8px 0 0;
}

/* CategoryFilter 컴포넌트가 자체 grid(5등분)를 가지므로 래퍼는 폭·여백만 담당 */
.roadmap-main__filters {
  width: 100%;
  margin-top: 14px;
}

.roadmap-main__saved-card {
  min-height: 0;
  cursor: pointer;
  transition: transform 0.15s ease;
}

.roadmap-main__saved-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 16px;
}

.saved-card {
  display: flex;
  min-height: 74px;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.saved-card__content {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  align-items: flex-start;
}

/* 표시용 태그(cat-tag): 도메인색 + 흰 글자, 사진처럼 둥근 pill, 작고 버튼형 */
.saved-card .saved-card__chip {
  align-self: flex-start;
  margin-bottom: 2px;
  padding: 3px 18px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 999px;
}

.saved-card__title-row {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 6px;
  margin-top: 0;
}

.saved-card__title {
  margin: 0;
  color: var(--text-body);
  font-size: 14px;
  font-weight: 700;
  line-height: 1.35;
}

.saved-card__like {
  display: flex;
  flex-shrink: 0;
  align-items: center;
}

.saved-card__delete {
  display: flex;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  padding: 0;
  border: 0;
  align-items: center;
  justify-content: center;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
}

.saved-card__delete-icon {
  width: 17px;
  height: 17px;
}

.saved-card__description {
  margin: 3px 0 0;
  color: var(--text-muted);
  font-size: 11px;
  line-height: 1.3;
}

.roadmap-main__empty-image {
  width: 24px;
  height: 24px;
}

.roadmap-main__more-button {
  width: 100%;
  margin-top: 6px;
  padding: 12px;
  border: 0;
  background: transparent;
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.roadmap-main__more-button:hover {
  color: var(--text-body);
}
</style>

<style>
/* 로드맵 메인 화면 전체 배경 - D-Day·목돈작전과 같은 은은한 세이지 그린(연 카키) */
.app-content:has(.roadmap-main) {
  background-color: rgba(120, 152, 130, 0.06);
}
</style>
