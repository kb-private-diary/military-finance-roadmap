<script setup>
// SCR-RENT-04 · step2) 자취 매물 상세  (담당: 수연 / 데모)
// 매물 리스트에서 탭 → 상세 → "이 매물로 계산하기" → 비용 계산
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import StepIndicator from '@/components/common/StepIndicator.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const route = useRoute();
const router = useRouter();

// 데모용 매물 (리스트와 동일 데이터)
const LISTINGS = {
  1: { name: '부전현대', type: '오피스텔', deposit: 500, monthly: 45, fee: 10, area: 23.1, floor: 5, built: 2015, addr: '부산 부산진구 부전동', market: '주변 시세 대비 약 5% 저렴' },
  2: { name: '한양빌라', type: '빌라', deposit: 300, monthly: 38, fee: 8, area: 33.2, floor: 3, built: 2009, addr: '부산 부산진구 부전동', market: '주변 시세와 비슷' },
  3: { name: '전포에스케이', type: '오피스텔', deposit: 700, monthly: 52, fee: 12, area: 28.5, floor: 8, built: 2019, addr: '부산 부산진구 전포동', market: '신축 · 시세 대비 약간 높음' },
};

const item = computed(() => LISTINGS[route.params.listingId] || LISTINGS[1]);

const goCost = () =>
  router.push({ name: 'RentCost', params: { goalId: route.query.goalId || 1 } });
</script>

<template>
  <div class="page">
    <StepIndicator :current="2" :total="4" />

    <!-- 사진 자리 (이미지 없음) -->
    <div class="photo"></div>

    <h2 class="name">{{ item.name }}</h2>
    <p class="addr">{{ item.addr }}</p>

    <div class="price">
      <span class="price__main">보증 {{ item.deposit }}만 / 월 {{ item.monthly }}만</span>
      <span class="price__fee">관리비 {{ item.fee }}만원</span>
    </div>

    <dl class="spec">
      <div><dt>매물종류</dt><dd>{{ item.type }}</dd></div>
      <div><dt>전용면적</dt><dd>{{ item.area }}㎡</dd></div>
      <div><dt>층</dt><dd>{{ item.floor }}층</dd></div>
      <div><dt>건축년도</dt><dd>{{ item.built }}년</dd></div>
    </dl>

    <div class="market">
      <span class="market__tag">실거래 기준</span>
      {{ item.market }}
    </div>

    <div class="map">지도 영역 (준비 중)</div>
  </div>

  <BottomButtonBar primary-label="이 매물로 계산하기" @primary-click="goCost" />
</template>

<style scoped>
.page {
  padding: 8px 0 96px;
}
.photo {
  width: 100%;
  height: 180px;
  border-radius: 16px;
  background-color: var(--kb-gray-pale);
  margin-bottom: 16px;
}
.name {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
}
.addr {
  margin: 0 0 14px;
  font-size: 13px;
  color: var(--text-muted);
}
.price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 14px 16px;
  border-radius: 12px;
  background-color: #fffdf3;
  border: 1px solid var(--kb-yellow);
  margin-bottom: 18px;
}
.price__main {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}
.price__fee {
  font-size: 13px;
  color: var(--text-muted);
}
.spec {
  margin: 0 0 18px;
  border: 1px solid var(--line);
  border-radius: 12px;
  overflow: hidden;
}
.spec > div {
  display: flex;
  padding: 14px 16px;
  border-bottom: 1px solid var(--line);
}
.spec > div:last-child {
  border-bottom: none;
}
.spec dt {
  width: 90px;
  flex-shrink: 0;
  font-size: 13px;
  color: var(--text-muted);
}
.spec dd {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-strong);
}
.market {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  border-radius: 12px;
  background-color: #f5f5f7;
  font-size: 13px;
  color: var(--kb-dark-gray);
  margin-bottom: 18px;
}
.market__tag {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 6px;
  background-color: var(--kb-dark-gray);
  color: #ffffff;
  font-size: 10px;
  font-weight: 700;
}
.map {
  height: 120px;
  border-radius: 12px;
  background-color: var(--kb-gray-pale);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-hint);
  font-size: 13px;
}
</style>
