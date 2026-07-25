<script setup>
// SCR-JOB-02 · step2) 진로 로드맵 추천  (담당: 지원)
// step2 - 자격증·인턴십·교육 추천
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import BaseCard from '@/components/common/BaseCard.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import jobApi from '@/api/jobApi';

const route = useRoute();
const router = useRouter();
const goalId = route.params.goalId;

// ── 진행바 (step2/4) ──
const currentStep = 2;
const progress = computed(() => (currentStep / 4) * 100);

// item_type(P01/P02/P03) → 화면에 보여줄 섹션 제목
const SECTION_LABELS = {
  P01: '추천 자격증 · 어학',
  P02: '추천 인강',
  P03: '추천 정부지원 훈련과정',
};

// 백엔드 응답: { goalId, items: { P01: [...], P02: [...], P03: [...] } }
const groupedItems = ref({});
const selectedIds = ref(new Set());

const loadRecommend = async () => {
  const result = await jobApi.findPrepItemRecommend(goalId);
  groupedItems.value = result.items;
};

onMounted(loadRecommend);

// 표시할 섹션 순서 고정 (P01 → P02 → P03), 데이터 없는 항목구분은 자동 제외
const sections = computed(() =>
  Object.keys(SECTION_LABELS)
    .filter((type) => groupedItems.value[type]?.length)
    .map((type) => ({
      type,
      label: SECTION_LABELS[type],
      items: groupedItems.value[type],
    })),
);

const toggleSelect = (prepCritId) => {
  const next = new Set(selectedIds.value);
  if (next.has(prepCritId)) {
    next.delete(prepCritId);
  } else {
    next.add(prepCritId);
  }
  selectedIds.value = next;
};

const isSelected = (prepCritId) => selectedIds.value.has(prepCritId);

const formatAmount = (amount) =>
  amount != null ? `${amount.toLocaleString()}원` : '';

const isFormValid = computed(() => selectedIds.value.size > 0);

// ── 선택 완료 → JOB-API-05 저장 → 비용 계산 화면으로 이동 ──
const handleConfirm = async () => {
  if (!isFormValid.value) return;

  await jobApi.createJobPlans(goalId, Array.from(selectedIds.value));

  router.push({ name: 'JobCost', params: { goalId } });
};

const handlePrev = () => {
  router.back();
};
</script>

<template>
  <div class="job-recommend">
    <RoadmapCharacterSlider :progress="progress" label="진로 로드맵" />

    <h2 class="job-recommend__title">로드맵을 선택해주십니까?</h2>

    <div
      v-for="section in sections"
      :key="section.type"
      class="job-recommend__section"
    >
      <div class="job-recommend__section-label">{{ section.label }}</div>

      <BaseCard
        v-for="item in section.items"
        :key="item.prepCritId"
        padding="16px"
        class="recommend-card"
        :class="{ 'recommend-card--active': isSelected(item.prepCritId) }"
        @click="toggleSelect(item.prepCritId)"
      >
        <div class="recommend-card__check">
          <span v-if="isSelected(item.prepCritId)">✓</span>
          <span v-else>+</span>
        </div>

        <div class="recommend-card__body">
          <div class="recommend-card__name">{{ item.itemName }}</div>
          <div class="recommend-card__amount">
            {{ formatAmount(item.amount) }}
          </div>
        </div>

        <a
          v-if="item.infoUrl"
          :href="item.infoUrl"
          target="_blank"
          rel="noopener noreferrer"
          class="recommend-card__link"
          @click.stop
        >
          ↗
        </a>
      </BaseCard>
    </div>

    <BottomButtonBar
      primaryLabel="선택 완료"
      secondaryLabel="이전"
      :primaryDisabled="!isFormValid"
      @primary-click="handleConfirm"
      @secondary-click="handlePrev"
    />
  </div>
</template>

<style scoped>
.job-recommend {
  padding: 20px 20px 100px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.job-recommend__title {
  font-size: 20px;
  font-weight: 700;
  color: #545045;
  margin: 0;
}

.job-recommend__section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.job-recommend__section-label {
  font-size: 14px;
  color: #9e9e9e;
}

.recommend-card {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.2s ease;
}

.recommend-card--active {
  border-color: #ffbc00;
  box-shadow: 0 0 0 1px #ffbc00;
}

.recommend-card__check {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 1.5px solid #d0d0d0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: #9e9e9e;
  flex-shrink: 0;
}

.recommend-card--active .recommend-card__check {
  background-color: #ffbc00;
  border-color: #ffbc00;
  color: #60584c;
  font-weight: 700;
}

.recommend-card__body {
  flex: 1;
}

.recommend-card__name {
  font-size: 15px;
  font-weight: 600;
  color: #545045;
}

.recommend-card__amount {
  font-size: 12px;
  color: #9e9e9e;
  margin-top: 2px;
}

.recommend-card__link {
  font-size: 16px;
  color: #9e9e9e;
  text-decoration: none;
  flex-shrink: 0;
}
</style>
