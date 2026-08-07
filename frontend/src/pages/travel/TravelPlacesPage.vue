<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import travelApi from '@/api/travelApi';
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
          <label class="place-card__select">
            <input
              class="place-card__checkbox"
              type="checkbox"
              :checked="isSelected(place)"
              :aria-label="`${place.title} 관심 항목 선택`"
              @change="togglePlace(place)"
            />
            <img
              v-if="place.thumbnail"
              :src="place.thumbnail"
              :alt="`${place.title} 이미지`"
              loading="lazy"
            />
            <span v-else class="place-card__placeholder" aria-hidden="true">
              {{ selectedCategory === 'restaurant' ? '맛집' : '여행' }}
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
          </label>
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
  min-height: 88px;
  color: inherit;
  cursor: pointer;
}

.place-card__checkbox {
  position: absolute;
  top: 7px;
  right: 7px;
  z-index: 1;
  width: 16px;
  height: 16px;
  margin: 0;
  accent-color: var(--kb-yellow-deep);
}

.place-card img,
.place-card__placeholder {
  width: 72px;
  height: 100%;
  min-height: 88px;
  object-fit: cover;
}

.place-card__placeholder {
  display: grid;
  place-items: center;
  background: var(--surface-muted);
  color: var(--text-muted);
  font-size: 12px;
}

.place-card__content {
  display: flex;
  min-width: 0;
  padding: 8px 30px 8px 10px;
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
