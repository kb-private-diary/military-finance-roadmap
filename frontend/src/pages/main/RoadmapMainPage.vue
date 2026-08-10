<script setup>
// SCR-ROAD-01 · 로드맵 메인  (담당: 지원)
// 카테고리별 목표 진입 + 저장된 로드맵 게시판 (관심등록)
import { ref, computed, onMounted } from 'vue';

import { useRouter } from 'vue-router';
import { useToast } from '@/composables/useToast';

import BaseCard from '@/components/common/BaseCard.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import LikeButton from '@/components/common/LikeButton.vue';
import BaseModal from '@/components/common/BaseModal.vue';

import rabbitImage from '@/assets/images/roadmap/rabbit.png';
import travelImage from '@/assets/images/roadmap/travel.png';
import carImage from '@/assets/images/roadmap/car.png';
import jobImage from '@/assets/images/roadmap/job.png';
import rentImage from '@/assets/images/roadmap/rent.png';
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

const categories = [
  {
    code: 'TRAVEL',
    title: '추억 쌓으러',
    description: '전역하고 어디부터 가지?',
    image: travelImage,

    routeName: 'TravelGoalCreate',
  },
  {
    code: 'CAR',
    title: '차 뽑으러',
    description: '전역하고 무슨 차 타지?',
    image: carImage,

    routeName: 'CarGoalCreate',
  },
  {
    code: 'JOB',
    title: '꿈 찾으러',
    description: '전역하고 뭐부터 준비하지?',
    image: jobImage,

    routeName: 'JobGoalCreate',
  },
  {
    code: 'RENT',
    title: '자취 하러',
    description: '전역하고 어디에 살지?',
    image: rentImage,

    routeName: 'RentGoalCreate',
  },
];

const roadmapFilters = [
  {
    code: 'ALL',
    label: '전체',
    variant: 'pastel-purple',
  },
  {
    code: 'TRAVEL',
    label: '여행',
    variant: 'pastel-blue',
  },
  {
    code: 'RENT',
    label: '자취',
    variant: 'pastel-pink',
  },
  {
    code: 'CAR',
    label: '자동차',
    variant: 'pastel-green',
  },
  {
    code: 'JOB',
    label: '진로',
    variant: 'pastel-yellow',
  },
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

  return {
    goalId: item.goalId,
    bookmarkId: item.bookmarkId,
    categoryId: item.categoryId,
    categoryCode: category.code,
    categoryLabel: category.label,
    title: item.title,
    description: item.targetDate,
    detail: item.detail,
    liked: item.bookmarked,
  };
};

const fetchRoadmaps = async (category = 'ALL') => {
  isLoading.value = true;

  try {
    const result = await roadmapApi.findRoadmapList(category.toLowerCase());

    roadmaps.value = result.map(mapRoadmapItem);
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
      <div class="roadmap-main__heading">
        <p class="text-overline roadmap-main__eyebrow">전역하면 뭐하지?</p>

        <h2 class="text-title roadmap-main__title">군적금 로드맵</h2>
      </div>

      <img :src="rabbitImage" alt="로드맵 토끼" class="roadmap-main__rabbit" />
    </section>

    <section class="roadmap-main__categories">
      <BaseCard
        v-for="category in categories"
        :key="category.code"
        padding="10px"
        class="roadmap-main__category-card"
        @click="selectCategory(category)"
      >
        <img
          :src="category.image"
          :alt="category.title"
          class="roadmap-main__category-image"
        />

        <p class="text-label roadmap-main__category-title">
          {{ category.title }}
        </p>

        <p class="text-caption roadmap-main__category-description">
          {{ category.description }}
        </p>
      </BaseCard>
    </section>

    <section class="roadmap-main__saved">
      <h2 class="text-title roadmap-main__saved-title">나의 로드맵</h2>

      <div class="roadmap-main__filters">
        <CategoryButton
          v-for="category in roadmapFilters"
          :key="category.code"
          :label="category.label"
          :variant="category.variant"
          :active="selectedRoadmapCategory === category.code"
          class="roadmap-main__filter-button"
          @click="selectRoadmapCategory(category.code)"
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
                <span class="saved-card__category">
                  {{ roadmap.categoryLabel }}
                </span>

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

                <p v-if="roadmap.description" class="saved-card__description">
                  {{ roadmap.description }}
                </p>

                <p v-if="roadmap.detail" class="saved-card__description">
                  {{ roadmap.detail }}
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

.roadmap-main__heading {
  padding-top: 8px;
}

.roadmap-main__eyebrow,
.roadmap-main__title {
  margin: 0;
}

.roadmap-main__title {
  margin-top: 8px;
}

.roadmap-main__rabbit {
  width: 58px;
  height: auto;
  flex-shrink: 0;
}

.roadmap-main__categories {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
  margin-top: 24px;
}

.roadmap-main__category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  aspect-ratio: 1 / 1;
  gap: 4px;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease;
}

.roadmap-main__category-card:active {
  transform: scale(0.98);
}

.roadmap-main__category-image {
  width: 48px;
  height: 48px;
  margin: 4px 0;
  object-fit: contain;
}

.roadmap-main__category-title,
.roadmap-main__category-description {
  margin: 0;
  text-align: center;
}

.roadmap-main__category-description {
  color: var(--text-muted);
  line-height: 1.4;
  word-break: keep-all;
}

:deep(.roadmap-main__category-card.base-card) {
  border-color: var(--line);
}

:deep(.roadmap-main__category-card:hover.base-card) {
  border: 2px solid var(--kb-yellow);
}

.roadmap-main__saved {
  margin-top: 40px;
}

.roadmap-main__saved-title {
  margin: 0;
}

.roadmap-main__filters {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 4px;
  width: 100%;
  margin-top: 14px;
}

:deep(.roadmap-main__filter-button) {
  width: 100%;
  min-width: 0;
  padding: 5px 2px;
  font-size: 10px;
  letter-spacing: -0.5px;
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

.saved-card__category {
  font-size: 11px;
  line-height: 1.2;
}

.saved-card__title-row {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
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
  margin-top: 12px;
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fff;
  color: var(--text-body);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.roadmap-main__more-button:hover {
  background: var(--background);
}
</style>
