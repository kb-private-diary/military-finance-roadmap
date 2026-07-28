<script setup>
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import travelApi from '@/api/travelApi';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

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
const placeCache = {
  attraction: null,
  restaurant: null,
};
const placeRequests = {
  attraction: null,
  restaurant: null,
};

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

const goPrevious = () => router.back();
const goNext = () =>
  router.push({ name: 'TravelPackages', params: { goalId } });

watch(selectedCategory, () => searchPlaces());
onMounted(searchPlaces);
</script>

<template>
  <div class="travel-places">
    <section class="roadmap-step" aria-label="여행 로드맵 3단계">
      <p class="roadmap-step__label text-overline">여행 로드맵</p>
      <div class="roadmap-step__progress">
        <span class="roadmap-step__number">3</span>
        <span class="roadmap-step__line">
          <span class="roadmap-step__line-fill" />
        </span>
      </div>
    </section>

    <header class="page-header">
      <h1 class="text-title">어디를 둘러볼까요?</h1>
      <p class="text-caption">
        여행지의 인기 관광지와 맛집을 확인해보세요.
      </p>
    </header>

    <div class="category-tabs" role="tablist" aria-label="추천 유형">
      <button
        v-for="category in categories"
        :key="category.value"
        type="button"
        role="tab"
        :aria-selected="selectedCategory === category.value"
        :class="{ active: selectedCategory === category.value }"
        @click="selectedCategory = category.value"
      >
        {{ category.label }}
      </button>
    </div>

    <div v-if="loading" class="status-box text-caption" role="status">
      추천 정보를 불러오고 있습니다.
    </div>

    <div
      v-else-if="loadError"
      class="status-box status-box--error text-caption"
      role="alert"
    >
      <p>{{ loadError }}</p>
      <button type="button" @click="searchPlaces(true)">다시 시도</button>
    </div>

    <div v-else-if="places.length === 0" class="status-box text-caption">
      조회된 추천 정보가 없습니다.
    </div>

    <ul v-else class="place-list">
      <li v-for="place in places" :key="place.placeId || place.title">
        <article class="place-card">
          <img
            v-if="place.thumbnail"
            :src="place.thumbnail"
            :alt="`${place.title} 이미지`"
            loading="lazy"
          />
          <div v-else class="place-card__placeholder" aria-hidden="true">
            {{ selectedCategory === 'restaurant' ? '맛집' : '여행' }}
          </div>

          <div class="place-card__content">
            <span
              v-if="place.type"
              class="place-card__type text-caption"
            >
              {{ place.type }}
            </span>
            <h2 class="text-label">{{ place.title }}</h2>
            <p class="place-card__rating text-caption">
              {{ ratingText(place) }}
            </p>
            <p
              v-if="place.address"
              class="place-card__address text-caption"
            >
              {{ place.address }}
            </p>
            <p v-if="place.price" class="place-card__price text-caption">
              가격대 {{ place.price }}
            </p>
          </div>
        </article>
      </li>
    </ul>

    <BottomButtonBar
      secondary-label="이전"
      primary-label="다음"
      :primary-disabled="loading || Boolean(loadError)"
      @secondary-click="goPrevious"
      @primary-click="goNext"
    />
  </div>
</template>

<style scoped>
.travel-places {
  min-height: 100%;
  padding: 18px 0 88px;
  color: #111;
}

.roadmap-step {
  margin-bottom: 34px;
}

.roadmap-step__label {
  margin: 0 0 15px;
}

.roadmap-step__progress {
  position: relative;
  display: flex;
  align-items: center;
  height: 22px;
}

.roadmap-step__line {
  position: absolute;
  right: 10px;
  left: 10px;
  height: 4px;
  overflow: hidden;
  background: #dedede;
}

.roadmap-step__line-fill {
  display: block;
  width: 66.6667%;
  height: 100%;
  background: #657052;
}

.roadmap-step__number {
  position: relative;
  z-index: 2;
  display: grid;
  width: 22px;
  height: 22px;
  margin-left: 66.6667%;
  place-items: center;
  border: 2px solid #657052;
  border-radius: 50%;
  background: #fff;
  color: #293020;
  font-size: 12px;
  font-weight: 700;
  transform: translateX(-50%);
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
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 18px;
}

.category-tabs button {
  padding: 10px;
  border: 0;
  border-radius: 18px;
  background: #e3e8df;
  color: #4d5945;
  font-size: 14px;
}

.category-tabs button.active {
  background: #536548;
  color: #fff;
  font-weight: 700;
}

.status-box {
  padding: 48px 12px;
  color: #777;
  text-align: center;
}

.status-box--error {
  color: #ef5350;
}

.status-box button {
  margin-top: 12px;
  padding: 8px 14px;
  border: 0;
  background: #617052;
  color: #fff;
}

.place-list {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.place-card {
  display: grid;
  grid-template-columns: 92px 1fr;
  min-height: 112px;
  overflow: hidden;
  border: 1px solid #dedede;
  border-radius: 12px;
  background: #fff;
}

.place-card img,
.place-card__placeholder {
  width: 92px;
  height: 100%;
  min-height: 112px;
  object-fit: cover;
}

.place-card__placeholder {
  display: grid;
  place-items: center;
  background: #edf0e9;
  color: #77816f;
  font-size: 12px;
}

.place-card__content {
  min-width: 0;
  padding: 12px;
}

.place-card__type {
  color: #78806f;
}

.place-card h2 {
  margin: 3px 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.place-card p {
  margin: 4px 0 0;
  font-size: 12px;
}

.place-card__rating {
  color: #9a752a;
}

.place-card__address {
  display: -webkit-box;
  overflow: hidden;
  color: #777;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.place-card__price {
  color: #536548;
  font-weight: 600;
}
</style>
