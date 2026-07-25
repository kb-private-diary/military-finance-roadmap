<script setup>
// SCR-RENT-03 · step2) 자취 매물 리스트 (로드맵 추천)  (담당: 수연 / 데모)
// 조건 매칭 매물 추천 → 카드 탭 → 매물 상세보기
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import StepIndicator from '@/components/common/StepIndicator.vue';

const route = useRoute();
const router = useRouter();

// 데모용 매물 (실제로는 GET /api/rent/goals/{goalId}/listings)
const listings = ref([
  { id: 1, name: '부전현대', type: '오피스텔', deposit: 500, monthly: 45, fee: 10, area: 23.1, best: true },
  { id: 2, name: '한양빌라', type: '빌라', deposit: 300, monthly: 38, fee: 8, area: 33.2, best: false },
  { id: 3, name: '전포에스케이', type: '오피스텔', deposit: 700, monthly: 52, fee: 12, area: 28.5, best: false },
]);

// 카드 탭 → 매물 상세보기 (goalId는 이후 비용 계산으로 이어지도록 query 로 전달)
const openDetail = (item) =>
  router.push({
    name: 'RentListingDetail',
    params: { listingId: item.id },
    query: { goalId: route.params.goalId },
  });
</script>

<template>
  <div class="page">
    <StepIndicator :current="2" :total="4" />

    <h2 class="page__title">추천 매물을 골라보세요</h2>
    <p class="page__sub">조건에 맞는 매물 {{ listings.length }}건 · 탭하면 상세보기</p>

    <ul class="list">
      <li
        v-for="item in listings"
        :key="item.id"
        class="card"
        @click="openDetail(item)"
      >
        <div class="card__thumb"></div>
        <div class="card__body">
          <p class="card__name">
            {{ item.name }}
            <span v-if="item.best" class="card__badge">추천</span>
          </p>
          <p class="card__meta">{{ item.type }} · {{ item.area }}㎡</p>
          <p class="card__price">
            보증 {{ item.deposit }}만 / 월 {{ item.monthly }}만
            <span class="card__fee">관리비 {{ item.fee }}만</span>
          </p>
        </div>
        <span class="card__arrow">›</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.page {
  padding: 8px 0 24px;
}
.page__title {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
}
.page__sub {
  margin: 0 0 20px;
  font-size: 14px;
  color: var(--text-muted);
}
.list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border: 1.5px solid var(--line);
  border-radius: 16px;
  background-color: #ffffff;
  cursor: pointer;
}
.card:hover {
  border-color: var(--kb-yellow);
}
.card__thumb {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  border-radius: 12px;
  background-color: var(--kb-gray-pale);
}
.card__body {
  flex: 1;
  min-width: 0;
}
.card__name {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}
.card__badge {
  padding: 1px 6px;
  border-radius: 6px;
  background-color: var(--kb-yellow);
  color: var(--text-strong);
  font-size: 10px;
  font-weight: 700;
}
.card__meta {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--text-muted);
}
.card__price {
  margin: 4px 0 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--kb-dark-gray);
}
.card__fee {
  margin-left: 6px;
  font-size: 12px;
  font-weight: 400;
  color: var(--text-hint);
}
.card__arrow {
  font-size: 22px;
  color: var(--text-hint);
  flex-shrink: 0;
}
</style>
