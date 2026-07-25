<script setup>
// SCR-COM-08c · 오픈뱅킹 연동 완료  (담당: 수연)
// 데모용 — 연동된 계좌는 Mock 데이터
import { useRouter } from 'vue-router';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();

const linkedAccounts = [
  { bank: 'KB국민은행', name: '군인적금', masked: '1234-**-5678' },
  { bank: 'KB국민은행', name: '입출금통장', masked: '9876-**-5432' },
];

const goHome = () => router.push({ name: 'Home' });
</script>

<template>
  <div class="done">
    <div class="done__badge">✓</div>
    <h1 class="done__title">연동 완료!</h1>
    <p class="done__desc">계좌가 안전하게 연동되었어요.</p>

    <ul class="done__list">
      <li v-for="acc in linkedAccounts" :key="acc.masked" class="done-acc">
        <div class="done-acc__info">
          <p class="done-acc__name">{{ acc.bank }} {{ acc.name }}</p>
          <p class="done-acc__num">{{ acc.masked }}</p>
        </div>
        <span class="done-acc__ok">연동됨</span>
      </li>
    </ul>
  </div>

  <BottomButtonBar primary-label="완료" @primary-click="goHome" />
</template>

<style scoped>
.done {
  padding: 48px 24px 96px;
  text-align: center;
}
.done__badge {
  width: 72px;
  height: 72px;
  margin: 0 auto 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--kb-yellow);
  color: var(--text-strong);
  font-size: 36px;
  font-weight: 700;
}
.done__title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-strong);
}
.done__desc {
  margin: 0 0 28px;
  font-size: 15px;
  color: var(--text-muted);
}
.done__list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: left;
}
.done-acc {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background-color: #ffffff;
}
.done-acc__name {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}
.done-acc__num {
  margin: 2px 0 0;
  font-size: 13px;
  color: var(--text-muted);
}
.done-acc__ok {
  font-size: 12px;
  font-weight: 700;
  color: var(--kb-yellow-deep);
}
</style>
