<script setup>
// SCR-COM-08 · 웰컴 온보딩 (오픈뱅킹 필수 연동)  (담당: 수연)
// 로그인 직후 서비스 소개 + 오픈뱅킹 연동 유도 → 이용 동의 화면으로
import { useRouter } from 'vue-router';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();
const goTerms = () => router.push({ name: 'OnboardingTerms' });
</script>

<template>
  <div class="onboarding">
    <div class="onboarding__char">🫡</div>
    <h1 class="onboarding__title">충성! 환영합니다</h1>
    <p class="onboarding__desc">
      전역 후 목돈 관리를 위해<br />
      오픈뱅킹 계좌 연동이 필요해요.
    </p>

    <ul class="onboarding__points">
      <li>군적금 만기금·잔액 자동 조회</li>
      <li>거래내역 기반 소비 분석</li>
      <li>자취·목표 자금 계산에 활용</li>
    </ul>
  </div>

  <BottomButtonBar primary-label="오픈뱅킹 이용 동의" @primary-click="goTerms" />
</template>

<style scoped>
.onboarding {
  padding: 40px 24px 96px;
  text-align: center;
}
.onboarding__char {
  width: 96px;
  height: 96px;
  margin: 0 auto 24px;
  border-radius: 50%;
  background-color: var(--kb-gray-pale);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44px;
}
.onboarding__title {
  margin: 0 0 12px;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-strong);
}
.onboarding__desc {
  margin: 0 0 28px;
  font-size: 15px;
  line-height: 1.6;
  color: var(--text-muted);
}
.onboarding__points {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: left;
}
.onboarding__points li {
  padding: 14px 16px;
  border: 1px solid var(--kb-yellow);
  border-radius: 12px;
  background-color: #fffdf3;
  font-size: 14px;
  color: var(--text-body);
}
</style>
