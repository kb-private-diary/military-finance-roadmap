<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import travelApi from '@/api/travelApi';
import saluteImage from '@/assets/images/salute.png';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';

const route = useRoute();
const router = useRouter();
const goalId = route.params.goalId;

const categories = [
  { value: 'attraction', label: '관광지' },
  { value: 'restaurant', label: '맛집' },
];

const selectedCategory = ref('attraction');
const places = ref([]);
const loading = ref(false);
const loadError = ref('');
const saveError = ref('');
const saving = ref(false);
const selectedPlaces = ref(new Map());
const loadedImages = ref(new Set());
const placeCache = {
  attraction: null,
  restaurant: null,
};
const placeRequests = {
  attraction: null,
  restaurant: null,
};
let scrollContainer = null;

const unwrap = (response) => response.data?.data ?? [];

const readErrorMessage = (error) =>
  error.response?.data?.message ||
  error.response?.data?.error?.message ||
  error.error ||
  '추천 정보를 불러오지 못했습니다.';

const searchPlaces = async (forceRefresh = false) => {
  const category = selectedCategory.value;
  if (!forceRefresh && placeCache[category] !== null) {
    places.value = placeCache[category];
    loadError.value = '';
    loading.value = false;
    return;
  }

  loading.value = true;
  loadError.value = '';
  places.value = [];

  try {
    if (forceRefresh || placeRequests[category] === null) {
      placeRequests[category] = travelApi
        .searchPlaces(goalId, category)
        .then(unwrap)
        .finally(() => {
          placeRequests[category] = null;
        });
    }
    const results = await placeRequests[category];
    placeCache[category] = results;
    if (selectedCategory.value === category) {
      places.value = results;
    }
  } catch (error) {
    if (selectedCategory.value === category) {
      loadError.value = readErrorMessage(error);
    }
  } finally {
    if (selectedCategory.value === category) {
      loading.value = false;
    }
  }
};

const ratingText = (place) => {
  if (!place.rating) return '평점 정보 없음';
  const reviews = place.reviews
    ? ` · 리뷰 ${place.reviews.toLocaleString('ko-KR')}개`
    : '';
  return `★ ${place.rating.toFixed(1)}${reviews}`;
};

const selectionType = (category) =>
  category === 'restaurant' ? 'food' : 'tour';

const placeKey = (place, category = selectedCategory.value) =>
  `${selectionType(category)}:${place.title}`;

const imageKey = (place) =>
  place.placeId || place.thumbnail || place.title;

const isImageLoaded = (place) =>
  loadedImages.value.has(imageKey(place));

const markImageLoaded = (place) => {
  const nextLoadedImages = new Set(loadedImages.value);
  nextLoadedImages.add(imageKey(place));
  loadedImages.value = nextLoadedImages;
};

const keepImagePlaceholder = (place) => {
  const nextLoadedImages = new Set(loadedImages.value);
  nextLoadedImages.delete(imageKey(place));
  loadedImages.value = nextLoadedImages;
};

const selectedPlaceKey = (place) =>
  `${place.type}:${place.name}`;

const loadSelectedPlaces = async () => {
  const response = await travelApi.getSelectedPlaces(goalId);
  const savedPlaces = unwrap(response);
  selectedPlaces.value = new Map(
    savedPlaces
      .filter((place) => place.type && place.name)
      .map((place) => [selectedPlaceKey(place), place]),
  );
};

const loadPage = async (forceRefresh = false) => {
  loading.value = true;
  loadError.value = '';
  try {
    await loadSelectedPlaces();
    await searchPlaces(forceRefresh);
  } catch (error) {
    loadError.value = readErrorMessage(error);
    loading.value = false;
  }
};

const isSelected = (place) =>
  selectedPlaces.value.has(placeKey(place));

const togglePlace = (place) => {
  const key = placeKey(place);
  if (selectedPlaces.value.has(key)) {
    selectedPlaces.value.delete(key);
    return;
  }

  selectedPlaces.value.set(key, {
    type: selectionType(selectedCategory.value),
    name: place.title,
    info: place.address || place.type || '',
    image: place.thumbnail || '',
  });
};

const goPrevious = () =>
  router.push({ name: 'TravelCost', params: { goalId } });
const goNext = async () => {
  if (saving.value) return;

  saving.value = true;
  saveError.value = '';
  try {
    await travelApi.updatePlaces(
      goalId,
      Array.from(selectedPlaces.value.values()),
    );
    await router.push({ name: 'TravelPackages', params: { goalId } });
  } catch (error) {
    saveError.value = readErrorMessage(error);
  } finally {
    saving.value = false;
  }
};

watch(selectedCategory, () => searchPlaces());
onMounted(() => {
  scrollContainer = document.querySelector('.app-content');
  scrollContainer?.classList.add('travel-scrollbar-hidden');
  loadPage();
});

onBeforeUnmount(() => {
  scrollContainer?.classList.remove('travel-scrollbar-hidden');
});
</script>

<template>
  <div class="travel-places">
    <RoadmapCharacterSlider :step="3" label="여행 로드맵" />

    <header class="page-header">
      <h1 class="text-title">어디를 둘러볼까요?</h1>
      <p class="text-caption">
        여행지의 인기 관광지와 맛집을 확인해보세요.
      </p>
    </header>

    <div class="category-tabs" role="tablist" aria-label="추천 유형">
      <CategoryButton
        v-for="category in categories"
        :key="category.value"
        variant="oval-yellow"
        :label="category.label"
        :active="selectedCategory === category.value"
        role="tab"
        :aria-selected="selectedCategory === category.value"
        @click="selectedCategory = category.value"
      />
    </div>

    <div v-if="loading" class="status-box text-caption" role="status">
      추천 정보를 불러오고 있습니다.
    </div>

    <EmptyState
      v-else-if="loadError"
      title="추천 정보를 불러오지 못했습니다."
      :description="loadError"
      role="alert"
    >
      <template #action>
        <button class="retry-button" type="button" @click="loadPage(true)">
          다시 시도
        </button>
      </template>
    </EmptyState>

    <EmptyState
      v-else-if="places.length === 0"
      title="추천 정보가 없습니다."
      description="다른 추천 유형을 확인하거나 잠시 후 다시 시도해주세요."
    />

    <p v-if="saveError" class="save-error text-caption" role="alert">
      {{ saveError }}
    </p>

    <ul v-if="!loading && !loadError && places.length" class="place-list">
      <li v-for="place in places" :key="place.placeId || place.title">
        <BaseCard
          class="place-card"
          padding="0"
          :class="{ 'is-selected': isSelected(place) }"
        >
          <button
            type="button"
            class="place-card__select"
            :aria-label="`${place.title} 관심 항목 선택`"
            :aria-pressed="isSelected(place)"
            @click="togglePlace(place)"
          >
            <span class="place-card__image">
              <img
                class="place-card__placeholder-image"
                :src="saluteImage"
                alt=""
                aria-hidden="true"
              />
              <img
                v-if="place.thumbnail"
                class="place-card__actual-image"
                :class="{ 'is-loaded': isImageLoaded(place) }"
                :src="place.thumbnail"
                :alt="`${place.title} 이미지`"
                loading="lazy"
                @load="markImageLoaded(place)"
                @error="keepImagePlaceholder(place)"
              />
            </span>

            <span class="place-card__content">
              <span
                v-if="place.type"
                class="place-card__type text-caption"
              >
                {{ place.type }}
              </span>
              <strong class="place-card__title text-label">
                {{ place.title }}
              </strong>
              <span class="place-card__rating text-caption">
                {{ ratingText(place) }}
              </span>
              <span
                v-if="place.address"
                class="place-card__address text-caption"
              >
                {{ place.address }}
              </span>
            </span>
          </button>
        </BaseCard>
      </li>
    </ul>

    <BottomButtonBar
      secondary-label="이 전"
      :primary-label="
        saving
          ? '저장 중...'
          : selectedPlaces.size
            ? '다 음'
            : '선택하지 않고 넘어가기'
      "
      :primary-disabled="loading || Boolean(loadError) || saving"
      @secondary-click="goPrevious"
      @primary-click="goNext"
    />
  </div>
</template>

<style scoped>
.travel-places {
  min-height: 100%;
  padding: 18px 0 88px;
  color: var(--text-strong);
}

.travel-places :deep(.character-slider) {
  margin-bottom: 28px;
}

.page-header {
  margin: 0 0 20px;
}

.page-header h1 {
  margin: 0;
  line-height: 1.35;
}

.page-header p {
  margin: 8px 0 0;
}

.category-tabs {
  display: flex;
  gap: 6px;
  margin-bottom: 18px;
}

.status-box {
  padding: 48px 12px;
  color: var(--text-muted);
  text-align: center;
}

.retry-button {
  padding: 8px 14px;
  border: 0;
  background: var(--travel-primary);
  color: var(--surface-default);
  font-family: inherit;
}

.place-list {
  display: grid;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.place-card {
  position: relative;
  min-height: 88px;
  overflow: hidden;
  border-radius: 10px;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease;
}

.place-card.is-selected {
  border-color: var(--kb-yellow-deep);
  box-shadow: 0 0 0 1px var(--kb-yellow-deep);
}

.place-card__select {
  position: relative;
  display: grid;
  grid-template-columns: 72px 1fr;
  width: 100%;
  min-height: 88px;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font-family: inherit;
  text-align: left;
  cursor: pointer;
}

.place-card__select:focus-visible {
  outline: 2px solid var(--kb-yellow-deep);
  outline-offset: -2px;
}

.place-card__image {
  position: relative;
  display: block;
  width: 72px;
  height: 100%;
  min-height: 88px;
  overflow: hidden;
  background: var(--surface-muted);
}

.place-card__placeholder-image,
.place-card__actual-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.place-card__placeholder-image {
  padding: 14px 10px;
  box-sizing: border-box;
  object-fit: contain;
}

.place-card__actual-image {
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.15s ease;
}

.place-card__actual-image.is-loaded {
  opacity: 1;
}

.place-card__content {
  display: flex;
  min-width: 0;
  padding: 8px 10px;
  flex-direction: column;
}

.place-card__type {
  color: var(--travel-primary);
}

.place-card__title {
  margin: 2px 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.place-card__rating,
.place-card__address {
  margin: 2px 0 0;
  font-size: 12px;
}

.place-card__rating {
  color: var(--brand-gold);
}

.place-card__address {
  display: -webkit-box;
  overflow: hidden;
  color: var(--text-muted);
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.save-error {
  margin: 0 0 10px;
  color: var(--danger);
  text-align: center;
}

:global(.app-content.travel-scrollbar-hidden) {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

:global(.app-content.travel-scrollbar-hidden::-webkit-scrollbar) {
  display: none;
  width: 0;
  height: 0;
}
</style>
