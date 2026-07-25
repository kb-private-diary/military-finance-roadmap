<script setup>
// SCR-RENT-06 · step4) 자취 금융상품 추천  (담당: 수연 / 데모)
// 주거 금융상품 추천 → 로드맵 저장(완료 모달) → 목표 상세로
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import StepIndicator from '@/components/common/StepIndicator.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const route = useRoute();
const router = useRouter();

const products = [
  { id: 1, name: 'KB 청년전용 버팀목전세자금', rate: '연 1.5~2.1%', limit: '최대 2억', tag: '군필 우대' },
  { id: 2, name: 'KB 주거안정 월세대출', rate: '연 1.0~1.3%', limit: '월 40만원', tag: '' },
  { id: 3, name: 'KB Young Youth 적금', rate: '연 4.0%', limit: '월 50만원', tag: '' },
];

const showDone = ref(false);
const save = () => (showDone.value = true);
const confirmDone = () => {
  showDone.value = false;
  router.push({ name: 'RentGoalDetail', params: { goalId: route.params.goalId } });
};
</script>

<template>
  <div class="page">
    <StepIndicator :current="4" :total="4" />

    <h2 class="page__title">이런 금융상품은 어때요?</h2>
    <p class="page__sub">부족한 자금을 채워줄 주거 금융상품이에요</p>

    <ul class="list">
      <li v-for="p in products" :key="p.id" class="prod">
        <div class="prod__head">
          <p class="prod__name">{{ p.name }}</p>
          <span v-if="p.tag" class="prod__tag">{{ p.tag }}</span>
        </div>
        <div class="prod__meta">
          <span>금리 {{ p.rate }}</span>
          <span>한도 {{ p.limit }}</span>
        </div>
      </li>
    </ul>

    <p class="notice">전역예정일·현재 자산 기준으로 추천된 상품이에요.</p>
  </div>

  <BottomButtonBar primary-label="로드맵 저장" @primary-click="save" />

  <div v-if="showDone" class="modal-overlay" @click.self="showDone = false">
    <div class="modal">
      <h3 class="modal__title">알림</h3>
      <p class="modal__msg">선택한 지역에 목표가 저장되었습니다.</p>
      <button type="button" class="modal__btn" @click="confirmDone">확인</button>
    </div>
  </div>
</template>

<style scoped>
.page {
  padding: 8px 0 96px;
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
  margin: 0 0 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.prod {
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 16px;
  background-color: #ffffff;
}
.prod__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.prod__name {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}
.prod__tag {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 8px;
  background-color: #536349;
  color: #ffffff;
  font-size: 10px;
  font-weight: 700;
}
.prod__meta {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  font-size: 13px;
  color: var(--kb-dark-gray);
}
.notice {
  font-size: 12px;
  color: var(--text-hint);
  text-align: center;
}

/* 완료 모달 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  z-index: 1000;
}
.modal {
  width: 100%;
  max-width: 280px;
  background-color: #ffffff;
  border-radius: 16px;
  padding: 24px 20px 16px;
  text-align: center;
}
.modal__title {
  margin: 0 0 10px;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}
.modal__msg {
  margin: 0 0 20px;
  font-size: 14px;
  color: var(--text-body);
}
.modal__btn {
  width: 100%;
  padding: 13px;
  border: none;
  border-radius: 10px;
  background-color: var(--kb-yellow);
  color: var(--text-strong);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}
</style>
