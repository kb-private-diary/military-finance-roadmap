<script setup>
// SCR-RENT-07 · 자취 목표 상세  (담당: 수연 / 데모)
// 저장된 자취 로드맵 요약 (스냅샷)
import { useRouter } from 'vue-router';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();

const goal = {
  title: '전역 후 부산 자취',
  status: 'CONFIRMED',
  conditions: '월세 · 오피스텔 · 원룸 · 1년',
  regions: ['부산진구 부전동', '부산진구 전포동'],
  listing: '부전현대 · 보증 500만 / 월 45만',
  totalRequired: '11,600,000원',
};

const goHome = () => router.push({ name: 'Home' });
</script>

<template>
  <div class="page">
    <div class="hero">
      <span class="hero__badge">저장 완료</span>
      <h2 class="hero__title">{{ goal.title }}</h2>
      <p class="hero__sub">게시판에 저장되었어요</p>
    </div>

    <dl class="summary">
      <div class="row">
        <dt>조건</dt>
        <dd>{{ goal.conditions }}</dd>
      </div>
      <div class="row">
        <dt>희망 지역</dt>
        <dd>{{ goal.regions.join(', ') }}</dd>
      </div>
      <div class="row">
        <dt>선택 매물</dt>
        <dd>{{ goal.listing }}</dd>
      </div>
      <div class="row">
        <dt>총 필요 자금</dt>
        <dd class="row__strong">{{ goal.totalRequired }}</dd>
      </div>
    </dl>
  </div>

  <BottomButtonBar primary-label="홈으로" @primary-click="goHome" />
</template>

<style scoped>
.page {
  padding: 16px 0 96px;
}
.hero {
  text-align: center;
  padding: 24px 0 28px;
}
.hero__badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  background-color: var(--kb-yellow);
  color: var(--text-strong);
  font-size: 12px;
  font-weight: 700;
}
.hero__title {
  margin: 12px 0 4px;
  font-size: 21px;
  font-weight: 700;
  color: var(--text-strong);
}
.hero__sub {
  margin: 0;
  font-size: 14px;
  color: var(--text-muted);
}
.summary {
  margin: 0;
  border: 1px solid var(--line);
  border-radius: 16px;
  overflow: hidden;
}
.row {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid var(--line);
}
.row:last-child {
  border-bottom: none;
}
.row dt {
  width: 84px;
  flex-shrink: 0;
  font-size: 13px;
  color: var(--text-muted);
}
.row dd {
  margin: 0;
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-strong);
}
.row__strong {
  color: var(--kb-yellow-deep);
}
</style>
