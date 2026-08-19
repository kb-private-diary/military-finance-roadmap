<script setup>
// 알림함 (웹푸시 발송 이력) - 담당: 석윤
import { computed, onMounted, ref } from 'vue';
import pushApi from '@/api/pushApi';
import { formatMonthDay, formatTime } from '@/util/format';
import { useNotificationBadge } from '@/composables/useNotificationBadge';
import EmptyState from '@/components/common/EmptyState.vue';

import savingIcon from '@/assets/images/push/saving.png';
import ddayIcon from '@/assets/images/push/dday.png';
import defaultIcon from '@/assets/images/push/default.png';

// 카테고리 → 아이콘 매핑. push_history.category는 백엔드가 검증 안 하는 자유 문자열이라
// 여기 없는 카테고리가 오면 그냥 defaultIcon으로 대체된다 (새 도메인 추가돼도 안 깨짐).
const CATEGORY_ICON_MAP = {
  SAVING: savingIcon,
  DDAY: ddayIcon,
};
const getIcon = (category) => CATEGORY_ICON_MAP[category] || defaultIcon;

const PAGE_SIZE_FIRST = 5;
const PAGE_SIZE_MORE = 10;

const historyList = ref([]);
const loading = ref(true);
const errorMessage = ref('');
const visibleCount = ref(PAGE_SIZE_FIRST);

const { markNotificationsSeen } = useNotificationBadge();

const load = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    historyList.value = await pushApi.findHistoryList();
    // 알림함을 열어 목록을 확인했으니 헤더의 빨간 점을 지운다.
    markNotificationsSeen();
  } catch (e) {
    errorMessage.value =
      e.response?.data?.message || '알림 이력을 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

onMounted(load);

const visibleList = computed(() =>
  historyList.value.slice(0, visibleCount.value),
);
const hasMore = computed(() => visibleCount.value < historyList.value.length);
const loadMore = () => {
  visibleCount.value += PAGE_SIZE_MORE;
};

// sentAt 날짜별로 묶기. visibleList가 늘어날 때마다 다시 계산돼서
// "더보기"로 새로 드러난 항목도 알맞은 날짜 그룹에 자연스럽게 들어간다.
const groupedList = computed(() => {
  const groups = [];
  const groupByDate = new Map();
  visibleList.value.forEach((item) => {
    const dateKey = formatMonthDay(item.sentAt);
    if (!groupByDate.has(dateKey)) {
      const group = { dateKey, items: [] };
      groupByDate.set(dateKey, group);
      groups.push(group);
    }
    groupByDate.get(dateKey).items.push(item);
  });
  return groups;
});
</script>

<template>
  <div class="webpush-page">
    <h1 class="webpush-page__title">알림함</h1>

    <p v-if="loading" class="text-caption">불러오는 중...</p>
    <p v-else-if="errorMessage" class="webpush-page__error text-caption">
      {{ errorMessage }}
    </p>

    <EmptyState
      v-else-if="historyList.length === 0"
      title="아직 받은 알림이 없어요"
      description="알림을 받으면 여기에 표시돼요"
    />

    <template v-else>
      <section
        v-for="group in groupedList"
        :key="group.dateKey"
        class="webpush-group"
      >
        <h2 class="webpush-group__date">{{ group.dateKey }}</h2>

        <div
          v-for="item in group.items"
          :key="item.historyId"
          class="webpush-item"
        >
          <img
            :src="getIcon(item.category)"
            alt=""
            class="webpush-item__icon"
          />
          <div class="webpush-item__body">
            <p class="webpush-item__title">{{ item.title }}</p>
            <p class="webpush-item__desc">{{ item.body }}</p>
          </div>
          <span class="webpush-item__time">{{ formatTime(item.sentAt) }}</span>
        </div>
      </section>

      <button
        v-if="hasMore"
        type="button"
        class="webpush-page__more"
        @click="loadMore"
      >
        + 더보기
      </button>
      <p v-else class="webpush-page__end text-caption">마지막 알림입니다</p>
    </template>
  </div>
</template>

<style scoped>
.webpush-page {
  padding-bottom: 40px;
}

.webpush-page__title {
  margin: 24px 0 20px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
}

.webpush-page__error {
  color: var(--danger);
}

.webpush-group {
  margin-bottom: 20px;
}

.webpush-group__date {
  margin: 0 0 8px;
  padding-bottom: 8px;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
  border-bottom: 1px solid var(--line);
}

.webpush-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
}

.webpush-item__icon {
  width: 36px;
  height: 36px;
  object-fit: contain;
  flex-shrink: 0;
}

.webpush-item__body {
  flex: 1;
  min-width: 0;
}

.webpush-item__title {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}

.webpush-item__desc {
  margin: 0;
  font-size: 13px;
  color: var(--text-muted);
}

.webpush-item__time {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--text-hint);
}

.webpush-page__more {
  display: block;
  width: 100%;
  margin-top: 12px;
  padding: 12px;
  border: none;
  background: none;
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.webpush-page__end {
  margin-top: 12px;
  text-align: center;
  color: var(--text-hint);
}
</style>
