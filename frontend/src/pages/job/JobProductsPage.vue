<script setup>
// SCR-JOB-04 · step4) 진로 금융상품 추천  (담당: 지원)
// step4 - 청년 지원 정책·KB 예적금 추천 → 저장
// 자취 step4와 동일한 구조로 통일: 정책/KB 탭 + 상품 카드 + 저장 완료 모달.
// (자취의 감당도·조언·저장안내는 자취 전용이라 진로엔 넣지 않는다.)
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import jobApi from '@/api/jobApi';
import BaseTag from '@/components/common/BaseTag.vue';
import BaseModal from '@/components/common/BaseModal.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import RoadmapCharacterSlider from '@/components/common/RoadmapCharacterSlider.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import policyTabIcon from '@/assets/images/rent-policy-product.png';
import kbTabIcon from '@/assets/images/rent-kb-product.png';
import { useToast } from '@/composables/useToast';

const route = useRoute();
const router = useRouter();
const { show } = useToast();

const goalId = computed(() => Number(route.params.goalId));

const loading = ref(false);
const saving = ref(false);
const isCompleteModalOpen = ref(false);
const policies = ref([]);
const financialProducts = ref([]);

// 탭: 정책상품(정부·지역 정책) / KB상품(예적금 등)
const activeTab = ref('POLICY'); // 'POLICY' | 'KB'

const isValidGoalId = computed(
  () => Number.isInteger(goalId.value) && goalId.value > 0,
);

// 현재 탭에 노출할 상품 목록
const currentProducts = computed(() =>
  activeTab.value === 'POLICY' ? policies.value : financialProducts.value,
);

const emptyMessage = computed(() =>
  activeTab.value === 'POLICY'
    ? '추천된 정책이 없어요'
    : '추천된 금융상품이 없어요',
);

const loadRecommendedProducts = async () => {
  if (!isValidGoalId.value) {
    show('올바른 진로 목표 정보가 아닙니다.', 'error');
    return;
  }

  try {
    loading.value = true;

    const result = await jobApi.findServiceRecommend(goalId.value);

    policies.value = result?.policies ?? [];
    financialProducts.value = result?.financialProducts ?? [];
  } catch (error) {
    console.error('정책·금융상품 추천 조회 실패:', error);
    show('추천 정보를 불러오지 못했습니다.', 'error');
  } finally {
    loading.value = false;
  }
};

// 뱃지: '복무 중 가능' → '복무 중', 색은 시점별
const getBadgeText = (product) => (product?.badgeCode ?? '').replace(' 가능', '');

const getBadgeVariant = (product) => {
  const badgeText = product?.badgeCode ?? '';

  if (badgeText.includes('복무 중')) return 'yellow';
  if (badgeText.includes('전역 후')) return 'green';

  return 'gray';
};

const goPrevious = () => {
  router.back();
};

const handleSave = async () => {
  if (saving.value) {
    return;
  }

  try {
    saving.value = true;

    await jobApi.confirmJobGoal(goalId.value);

    isCompleteModalOpen.value = true;
  } catch (error) {
    console.error('진로 목표 저장 실패:', error);
    show('진로 로드맵을 저장하지 못했습니다.', 'error');
  } finally {
    saving.value = false;
  }
};

const goJobGoalDetail = () => {
  router.push({
    name: 'JobGoalDetail',
    params: {
      goalId: goalId.value,
    },
  });
};

onMounted(() => {
  loadRecommendedProducts();
});
</script>

<template>
  <div class="products">
    <RoadmapCharacterSlider :step="4" label="진로 로드맵" />
    <PageHeader title="이 금융상품 어떠십니까?" />

    <p v-if="loading" class="loading">불러오는 중...</p>

    <template v-else>
      <!-- 탭 -->
      <div class="tab-row">
        <button
          type="button"
          class="tab-item"
          :class="{ 'is-active': activeTab === 'POLICY' }"
          @click="activeTab = 'POLICY'"
        >
          정책상품 <img :src="policyTabIcon" class="tab-icon" alt="" />
        </button>
        <button
          type="button"
          class="tab-item"
          :class="{ 'is-active': activeTab === 'KB' }"
          @click="activeTab = 'KB'"
        >
          KB상품 <img :src="kbTabIcon" class="tab-icon" alt="" />
        </button>
      </div>

      <p class="pick-hint">
        목표에 맞는 추천 상품이에요. 카드를 눌러 상세 정보를 확인하세요
      </p>

      <!-- 상품 카드 -->
      <section class="group">
        <template v-if="currentProducts.length">
          <div
            v-for="product in currentProducts"
            :key="`${product.productType}-${product.productId}`"
            class="prod"
          >
            <span class="prod__body">
              <BaseTag
                v-if="product.badgeCode"
                class="prod__tag"
                :label="getBadgeText(product)"
                :variant="getBadgeVariant(product)"
              />
              <span class="prod__name">{{ product.productName }}</span>
              <span class="prod__desc">{{ product.productDesc }}</span>
            </span>

            <a
              v-if="product.linkUrl"
              :href="product.linkUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="prod__link"
              :aria-label="`${product.productName} 상세 페이지 열기`"
              @click.stop
            >↗</a>
            <span
              v-else
              class="prod__link prod__link--off"
              aria-hidden="true"
            >↗</span>
          </div>
        </template>
        <p v-else class="empty">{{ emptyMessage }}</p>
      </section>

      <p class="foot-note">추천 상품을 확인하고 로드맵을 저장하세요</p>
    </template>

    <BottomButtonBar
      secondary-label="이전"
      :primary-label="saving ? '저장 중...' : '저장'"
      :primary-disabled="loading || saving"
      @secondary-click="goPrevious"
      @primary-click="handleSave"
    />

    <BaseModal
      v-model="isCompleteModalOpen"
      title="알림"
      confirm-text="확인"
      @confirm="goJobGoalDetail"
      @cancel="goJobGoalDetail"
    >
      <p class="complete-modal__message">진로 로드맵이 저장되었습니다.</p>
    </BaseModal>
  </div>
</template>

<style scoped>
.products {
  padding: 20px 20px 96px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.loading {
  padding: 60px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}

/* ── 탭 ───────────────────────────────────────────────── */
.tab-row {
  display: flex;
  gap: 6px;
  margin-top: 4px;
}
.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 0;
  border: 1px solid transparent;
  border-radius: 14px 14px 4px 4px;
  background: var(--kb-gray-pale);
  color: var(--text-hint);
  font-size: 14px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s ease;
}
.tab-item.is-active {
  background: #fff;
  border-color: var(--line-strong);
  color: var(--text-strong);
  font-weight: 700;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
.tab-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

/* ── 상품 그룹 ────────────────────────────────────────── */
.pick-hint {
  margin-top: 2px;
  font-size: 11px;
  color: var(--text-muted);
}
.group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* ── 상품 카드 ────────────────────────────────────────── */
.prod {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fff;
}
.prod__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 3px;
}
:deep(.prod__tag.base-tag) {
  margin-bottom: 4px;
  padding: 2px 8px;
  font-size: 10px;
  line-height: 1.2;
}
.prod__name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
}
.prod__desc {
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.4;
  word-break: keep-all;
}
/* 외부링크 - 회색 원 없이 화살표만 */
.prod__link {
  flex: none;
  color: var(--text-muted);
  font-size: 16px;
  font-weight: 700;
  text-decoration: none;
  padding: 4px;
}
.prod__link--off {
  color: var(--text-hint);
  opacity: 0.6;
}
.empty {
  padding: 18px 0;
  text-align: center;
  font-size: 12px;
  color: var(--text-hint);
  border: 1px dashed var(--line);
  border-radius: 12px;
}

.foot-note {
  text-align: center;
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.6;
}

.complete-modal__message {
  margin: 0;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.6;
  color: var(--text-body);
}
</style>
