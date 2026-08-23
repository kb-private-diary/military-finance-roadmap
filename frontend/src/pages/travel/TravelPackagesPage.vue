<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import travelApi from '@/api/travelApi';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import { formatWon } from '@/util/format';

const route = useRoute();
const router = useRouter();
const goalId = route.params.goalId;

const packages = ref([]);
const selectedPackageId = ref(null);
const initialSelectedPackageId = ref(null);
const loading = ref(false);
const saving = ref(false);
const loadError = ref('');
const saveError = ref('');
let scrollContainer = null;

const readErrorMessage = (error) =>
  error.response?.data?.message ||
  error.response?.data?.error?.message ||
  '패키지 상품을 불러오지 못했습니다.';

const loadPackages = async () => {
  loading.value = true;
  loadError.value = '';

  try {
    const response = await travelApi.findPackages(goalId);
    packages.value = response.data?.data ?? [];
    selectedPackageId.value =
      packages.value.find((travelPackage) => travelPackage.selected)
        ?.packageId ?? null;
    initialSelectedPackageId.value = selectedPackageId.value;
  } catch (error) {
    loadError.value = readErrorMessage(error);
  } finally {
    loading.value = false;
  }
};

const selectPackage = (packageId) => {
  selectedPackageId.value =
    selectedPackageId.value === packageId ? null : packageId;
  saveError.value = '';
};

const goPrevious = () =>
  router.push({ name: 'TravelPlaces', params: { goalId } });

const goNext = async () => {
  if (saving.value) return;

  saving.value = true;
  saveError.value = '';
  try {
    if (
      selectedPackageId.value !== initialSelectedPackageId.value
    ) {
      await travelApi.updatePackage(goalId, selectedPackageId.value);
    }
    await router.push({ name: 'TravelProducts', params: { goalId } });
  } catch (error) {
    saveError.value = readErrorMessage(error);
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  scrollContainer = document.querySelector('.app-content');
  scrollContainer?.classList.add('travel-scrollbar-hidden');
  loadPackages();
});

onBeforeUnmount(() => {
  scrollContainer?.classList.remove('travel-scrollbar-hidden');
});
</script>

<template>
  <div class="travel-packages">
    <RoadmapCharacterSlider :step="3" label="여행 로드맵" />

    <PageHeader
      title="패키지 여행은 어떠십니까?"
      description="자유 여행보다 저렴하고 일정에 맞는 상품들을 추천합니다."
    />

    <div v-if="loading" class="status-box text-caption" role="status">
      패키지 상품을 불러오고 있습니다.
    </div>

    <div
      v-else-if="loadError"
      class="status-box status-box--error text-caption"
      role="alert"
    >
      <p>{{ loadError }}</p>
      <button type="button" @click="loadPackages">다시 시도</button>
    </div>

    <EmptyState
      v-else-if="packages.length === 0"
      title="추천 가능한 패키지가 없습니다."
      description="개별 여행 예상 경비보다 저렴한 패키지 상품이 없거나 해당 여행지 관련 패키지 상품이 없습니다."
    />

    <ul v-else class="package-list">
      <li
        v-for="travelPackage in packages"
        :key="travelPackage.packageId"
      >
        <BaseCard
          class="package-card"
          padding="0"
          :class="{
            'is-selected':
              selectedPackageId === travelPackage.packageId,
          }"
        >
          <button
            type="button"
            class="package-card__select"
            :aria-label="`${travelPackage.name} 선택`"
            :aria-pressed="
              selectedPackageId === travelPackage.packageId
            "
            @click="selectPackage(travelPackage.packageId)"
          >
            <img
              v-if="travelPackage.imageUrl"
              :src="travelPackage.imageUrl"
              :alt="`${travelPackage.name} 상품 이미지`"
              loading="lazy"
            />
            <span v-else class="package-card__placeholder">
              패키지
            </span>

            <span class="package-card__content">
              <span class="package-card__region text-caption">
                {{ travelPackage.country }} ·
                {{ travelPackage.regionName }}
              </span>
              <strong class="package-card__name text-label">
                {{ travelPackage.name }}
              </strong>
              <span
                v-if="travelPackage.description"
                class="package-card__description text-caption"
              >
                {{ travelPackage.description }}
              </span>
              <span class="package-card__period text-caption">
                출발 일정 {{ travelPackage.departurePeriod }}
              </span>
              <span class="package-card__price">
                {{ formatWon(travelPackage.minPrice) }}
              </span>
            </span>
          </button>

          <a
            class="package-card__external"
            :href="travelPackage.detailUrl"
            target="_blank"
            rel="noopener noreferrer"
            :aria-label="`${travelPackage.name} 외부 상품 페이지 열기`"
          >
            <svg
              viewBox="0 0 24 24"
              width="20"
              height="20"
              aria-hidden="true"
            >
              <path
                d="M14 4h6v6M20 4l-9 9M18 13v5a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h5"
              />
            </svg>
          </a>
        </BaseCard>
      </li>
    </ul>

    <p v-if="saveError" class="save-error text-caption" role="alert">
      {{ saveError }}
    </p>

    <BottomButtonBar
      secondary-label="이전"
      :primary-label="
        saving
          ? '저장 중...'
          : selectedPackageId
            ? '선택완료'
            : '선택하지 않고 넘어가기'
      "
      :primary-disabled="
        loading ||
        saving
      "
      @secondary-click="goPrevious"
      @primary-click="goNext"
    />
  </div>
</template>

<style scoped>
/* 제목 아래 컨텐츠 간격 통일(20px) */
.travel-packages :deep(.page-header) {
  margin-bottom: 20px;
}
.travel-packages {
  min-height: 100%;
  padding: 18px 0 88px;
  color: var(--text-strong);
}

.travel-packages :deep(.character-slider) {
  margin-bottom: 28px;
}

.status-box {
  padding: 48px 12px;
  color: var(--text-muted);
  text-align: center;
}

.status-box p {
  margin: 0;
}

.status-box--error {
  color: var(--danger);
}

.status-box button {
  margin-top: 12px;
  padding: 8px 14px;
  border: 0;
  background: var(--travel-primary);
  color: var(--surface-default);
}

.package-list {
  display: grid;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.package-card {
  position: relative;
  overflow: hidden;
  border-radius: 10px;
}

.package-card.is-selected {
  border-color: var(--kb-yellow-deep);
  box-shadow: 0 0 0 1px var(--kb-yellow-deep);
}

.package-card__select {
  position: relative;
  display: grid;
  grid-template-columns: 94px 1fr;
  width: 100%;
  min-height: 112px;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font-family: inherit;
  text-align: left;
  cursor: pointer;
}

.package-card__select:focus-visible {
  outline: 2px solid var(--kb-yellow-deep);
  outline-offset: -2px;
}

.package-card__select img,
.package-card__placeholder {
  align-self: stretch;
  width: 94px;
  height: 100%;
  min-height: 112px;
  object-fit: cover;
}

.package-card__placeholder {
  display: grid;
  place-items: center;
  background: var(--surface-muted);
  color: var(--text-muted);
}

.package-card__content {
  display: flex;
  min-width: 0;
  padding: 9px 46px 9px 10px;
  flex-direction: column;
}

.package-card__region {
  color: var(--travel-primary);
}

.package-card__name {
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.package-card__description {
  display: -webkit-box;
  margin-top: 4px;
  overflow: hidden;
  color: var(--text-muted);
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

.package-card__period {
  margin-top: auto;
  color: var(--text-hint);
}

.package-card__price {
  margin-top: 2px;
  color: var(--travel-primary-dark);
  font-size: 14px;
  font-weight: 700;
}

.package-card__external {
  position: absolute;
  z-index: 3;
  top: 50%;
  right: 13px;
  display: grid;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  color: var(--text-strong);
  text-decoration: none;
  transform: translateY(-50%);
  place-items: center;
}

.package-card__external:hover,
.package-card__external:focus-visible {
  background: var(--surface-muted);
}

.package-card__external svg {
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.save-error {
  margin: 12px 0 0;
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
