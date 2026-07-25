<script setup>
// SCR-ROAD-01 · 로드맵 메인  (담당: 지원 / 데모)
// 4개 카테고리 진입 카드 + 저장된 나의 로드맵(관심등록·필터)
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import CategoryButton from '@/components/common/CategoryButton.vue';
import LikeButton from '@/components/common/LikeButton.vue';
import EmptyState from '@/components/common/EmptyState.vue';

const router = useRouter();

// 4개 카테고리 카드
const categories = [
  { key: 'travel', icon: '✈️', title: '추억 쌓으러', desc: '전역하고 어디부터 가지?', to: 'TravelGoalCreate', accent: true },
  { key: 'car', icon: '🚗', title: '차 뽑으러', desc: '전역하고 무슨 차 타지?', to: 'CarGoalCreate' },
  { key: 'job', icon: '🧭', title: '꿈 찾으러', desc: '전역하고 뭐부터 준비하지?', to: 'JobGoalCreate' },
  { key: 'rent', icon: '🏠', title: '자취 하러', desc: '전역하고 어디에 살지?', to: 'RentGoalCreate' },
];

// 필터 칩 (선택된 것만 파스텔 색, 나머지 회색)
const filters = [
  { key: 'all', label: '전체', variant: 'pastel-purple' },
  { key: '여행', label: '여행', variant: 'pastel-blue' },
  { key: '진로', label: '진로', variant: 'pastel-yellow' },
  { key: '자동차', label: '자동차', variant: 'pastel-green' },
  { key: '자취', label: '자취', variant: 'pastel-pink' },
];
const activeFilter = ref('all');

// 저장된 로드맵 (데모용 mock). color = 카테고리 태그 색
const roadmaps = ref([
  { id: 1, cat: '여행', color: '#5bc3ff', title: '여행명', date: '2026.01.01', extra: '', liked: true },
  { id: 2, cat: '진로', color: '#fbd55b', title: '공무원 (전산 직렬)', date: '2026.01.01', extra: '', liked: false },
  { id: 3, cat: '자동차', color: '#9cd495', title: '모닝 (2023)', date: '2026.01.01', extra: '저축진행률 00%', liked: false },
  { id: 4, cat: '자취', color: '#f8a5a5', title: '매물명', date: '2026.01.01', extra: '부산 > 부산진구', liked: false },
]);

const filtered = computed(() =>
  activeFilter.value === 'all'
    ? roadmaps.value
    : roadmaps.value.filter((r) => r.cat === activeFilter.value),
);

const emptyText = computed(() => {
  const map = {
    all: ['아직 저장한 목표가 없어요', '위의 관심있는 카테고리를 눌러 나만의 로드맵을 시작해보세요'],
    여행: ['여행 목표가 아직 없어요', '여행 카드에서 목표를 등록하면 여기에 표시돼요'],
    진로: ['진로 목표가 아직 없어요', '진로 카드에서 목표를 등록하면 여기에 표시돼요'],
    자동차: ['자동차 목표가 아직 없어요', '자동차 카드에서 목표를 등록하면 여기에 표시돼요'],
    자취: ['자취 목표가 아직 없어요', '자취 카드에서 목표를 등록하면 여기에 표시돼요'],
  };
  return map[activeFilter.value];
});

const goCategory = (c) => router.push({ name: c.to });
</script>

<template>
  <div class="page">
    <!-- 헤더 -->
    <header class="head">
      <div>
        <p class="head__sub">전역하면 뭐하지?</p>
        <h2 class="head__title">군적금 로드맵</h2>
      </div>
      <div class="head__char">🐰</div>
    </header>

    <!-- 4개 카테고리 카드 -->
    <div class="cat-grid">
      <button
        v-for="c in categories"
        :key="c.key"
        type="button"
        class="cat-card"
        :class="{ 'cat-card--accent': c.accent }"
        @click="goCategory(c)"
      >
        <span class="cat-card__icon">{{ c.icon }}</span>
        <p class="cat-card__title">{{ c.title }}</p>
        <p class="cat-card__desc">{{ c.desc }}</p>
      </button>
    </div>

    <!-- 나의 로드맵 -->
    <p class="head__sub head__sub--mt">이 추천은 저장!</p>
    <h2 class="head__title">나의 로드맵</h2>

    <div class="chips">
      <CategoryButton
        v-for="f in filters"
        :key="f.key"
        :variant="f.variant"
        :label="f.label"
        :active="activeFilter === f.key"
        @click="activeFilter = f.key"
      />
    </div>

    <!-- 저장된 로드맵 리스트 / 빈 상태 -->
    <div v-if="filtered.length" class="rm-list">
      <div v-for="r in filtered" :key="r.id" class="rm-card">
        <div class="rm-card__top">
          <span class="rm-tag" :style="{ backgroundColor: r.color }">{{ r.cat }}</span>
          <LikeButton v-model="r.liked" />
        </div>
        <p class="rm-card__title">{{ r.title }}</p>
        <p class="rm-card__meta">
          {{ r.date }}<span v-if="r.extra"> · {{ r.extra }}</span>
        </p>
      </div>
      <button type="button" class="more-btn">+ 더 보기</button>
    </div>

    <EmptyState v-else :title="emptyText[0]" :description="emptyText[1]" />
  </div>
</template>

<style scoped>
.page {
  padding: 16px 0 24px;
}

/* 헤더 */
.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}
.head__sub {
  margin: 0 0 2px;
  font-size: 14px;
  color: var(--text-muted);
}
.head__sub--mt {
  margin-top: 28px;
}
.head__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-strong);
}
.head__char {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background-color: #fffdf3;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  flex-shrink: 0;
}

/* 4개 카테고리 카드 */
.cat-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.cat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 22px 12px;
  border: none;
  border-radius: 16px;
  background-color: #f7f2e6;
  cursor: pointer;
}
.cat-card--accent {
  background-color: #ffe08a;
}
.cat-card__icon {
  font-size: 30px;
}
.cat-card__title {
  margin: 12px 0 4px;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}
.cat-card__desc {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
}

/* 필터 칩 */
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 14px 0 16px;
}
.chips :deep(.category-btn) {
  flex: 0 1 auto;
}

/* 저장된 로드맵 리스트 */
.rm-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.rm-card {
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 16px;
  background-color: #ffffff;
}
.rm-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.rm-tag {
  padding: 4px 12px;
  border-radius: 999px;
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
}
.rm-card__title {
  margin: 12px 0 4px;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}
.rm-card__meta {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
}
.more-btn {
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background-color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-body);
  cursor: pointer;
}
</style>
