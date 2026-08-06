<script setup>
// SCR-ROAD-01 · 로드맵 메인  (담당: 지원)
// 카테고리별 목표 진입 + 저장된 로드맵 게시판 (관심등록)
import { ref, computed } from 'vue';

import { useRouter } from 'vue-router';

import BaseCard from '@/components/common/BaseCard.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import LikeButton from '@/components/common/LikeButton.vue';

import rabbitImage from '@/assets/images/roadmap/rabbit.png';
import travelImage from '@/assets/images/roadmap/travel.png';
import carImage from '@/assets/images/roadmap/car.png';
import jobImage from '@/assets/images/roadmap/job.png';
import rentImage from '@/assets/images/roadmap/rent.png';

const router = useRouter();

const selectedCategory = ref('');
const selectedRoadmapCategory = ref('ALL');

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

const roadmaps = ref([
  {
    goalId: 1,
    categoryCode: 'JOB',
    categoryLabel: '진로',
    title: '웹개발',
    description: '2027.03',
    liked: false,
  },
  {
    goalId: 2,
    categoryCode: 'TRAVEL',
    categoryLabel: '여행',
    title: '후쿠오카 여행',
    description: '2026.12',
    liked: true,
  },
  {
    goalId: 3,
    categoryCode: 'CAR',
    categoryLabel: '자동차',
    title: '모닝',
    description: '2027.01',
    liked: false,
  },
  {
    goalId: 4,
    categoryCode: 'RENT',
    categoryLabel: '자취',
    title: '서울 원룸',
    description: '2026.11',
    liked: false,
  },
]);

const filteredRoadmaps = computed(() => {
  if (selectedRoadmapCategory.value === 'ALL') {
    return roadmaps.value;
  }

  return roadmaps.value.filter(
    (roadmap) => roadmap.categoryCode === selectedRoadmapCategory.value,
  );
});

const goRoadmapDetail = (roadmap) => {
  console.log(roadmap);

  // TODO API 연결
};

const selectCategory = (category) => {
  selectedCategory.value = category.code;

  router.push({
    name: category.routeName,
  });
};

const selectRoadmapCategory = (categoryCode) => {
  selectedRoadmapCategory.value = categoryCode;
};
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
        <BaseCard
          v-for="roadmap in filteredRoadmaps"
          :key="roadmap.goalId"
          padding="12px 14px"
          class="roadmap-main__saved-card"
          @click="goRoadmapDetail(roadmap)"
        >
          <div class="saved-card">
            <div class="saved-card__content">
              <span class="saved-card__category">
                {{ roadmap.categoryLabel }}
              </span>

              <strong class="saved-card__title">
                {{ roadmap.title }}
              </strong>

              <p class="saved-card__description">
                {{ roadmap.description }}
              </p>
            </div>

            <div @click.stop>
              <LikeButton v-model="roadmap.liked" />
            </div>
          </div>
        </BaseCard>
      </div>
    </section>
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

.saved-card__title {
  margin-top: 6px;
  color: var(--text-body);
  font-size: 14px;
  font-weight: 700;
  line-height: 1.35;
}

.saved-card__description {
  margin: 3px 0 0;
  color: var(--text-muted);
  font-size: 11px;
  line-height: 1.3;
}
</style>
