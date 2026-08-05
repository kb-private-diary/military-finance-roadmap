<script setup>
// SCR-COM-08 · 오픈뱅킹 온보딩 (신규+미연동 사용자)  담당: 수연
// 흐름: 회원가입 → 로그인 → (여기) 환영 → 약관동의 → 계좌선택 → 연동완료 → 서비스 시작
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import openbankingApi from '@/api/openbankingApi';
import { useToast } from '@/composables/useToast';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import soldierImg from '@/assets/images/soldier-salute.png';

const router = useRouter();
const { show } = useToast();
const TEMP_USER_ID = 1; // TODO: JWT 연동 후 제거

const step = ref(1); // 1 환영 · 2 약관 · 3 계좌선택 · 4 완료

// ── Step1 서비스 소개 ─────────────────────────────────────
const FEATURES = [
  '실시간 군적금 납입 현황',
  '자동 지출 분석 · 후회 회고',
  '맞춤 목표 · 금융상품 추천',
  '절감 목표 · 적금 추천',
];

// ── Step2 약관 ────────────────────────────────────────────
const termsOpen = ref(true);
const agreed = ref(false);
const TERMS = [
  '이용 목적: 군적금 현황 관리 · 후회소비 회고 · 목표 로드맵',
  '계좌 조회 (필수)',
  '잔액 조회 (필수)',
  '거래내역 조회 (필수)',
  '이용 기간: 1년 (자동 갱신 가능)',
];

// ── Step3 계좌 선택 ───────────────────────────────────────
// TODO: 실제 계좌 목록은 오픈뱅킹 인증(auth-url) 콜백 후 받아옴, 지금은 샘플임
const ACCOUNTS = [
  { fintechUseNum: 'F001', bankName: '신한은행', accountType: 'SAVING', productName: '군장병적금', balance: 1000000, required: true },
  { fintechUseNum: 'F002', bankName: 'KB국민은행', accountType: 'SAVING', productName: '군장병적금', balance: 1000000, required: true },
  { fintechUseNum: 'F003', bankName: 'KB국민은행', accountType: 'CHECKING', productName: '입출금계좌', balance: null, required: true },
  { fintechUseNum: 'F004', bankName: '신한은행', accountType: 'SAVING', productName: '군장병적금', balance: 1000000, required: false },
];
const selected = ref(new Set(ACCOUNTS.filter((a) => a.required).map((a) => a.fintechUseNum)));
const toggleAccount = (a) => {
  if (a.required) return; // 필수는 해제 불가
  const s = new Set(selected.value);
  s.has(a.fintechUseNum) ? s.delete(a.fintechUseNum) : s.add(a.fintechUseNum);
  selected.value = s;
};
const linkedAccounts = ref([]);
const linking = ref(false);

const balanceText = (a) =>
  a.accountType === 'CHECKING'
    ? '거래내역 수집 중'
    : `잔액 ${((a.balance ?? 0) / 10000).toLocaleString('ko-KR')}만원`;

// ── 단계 이동 ─────────────────────────────────────────────
const goAgree = () => (step.value = 2);
const confirmAgree = () => {
  if (!agreed.value) return show('필수 약관에 동의해주세요.', 'error');
  step.value = 3;
};
const linkAccounts = async () => {
  if (linking.value) return;
  linking.value = true;
  const nums = [...selected.value];
  try {
    const linked = await openbankingApi.link(TEMP_USER_ID, {
      code: 'mock',
      state: 'mock',
      selectedFintechNums: nums,
    });
    linkedAccounts.value = linked?.length
      ? linked
      : ACCOUNTS.filter((a) => selected.value.has(a.fintechUseNum));
  } catch {
    // 백엔드 미기동/실패 시 선택 계좌로 표시 (TODO: 폴백 제거)
    linkedAccounts.value = ACCOUNTS.filter((a) => selected.value.has(a.fintechUseNum));
  } finally {
    linking.value = false;
    step.value = 4;
  }
};
const startService = () => router.push({ name: 'Home' });
</script>

<template>
  <div class="ob">
    <!-- STEP 1 · 환영 -->
    <template v-if="step === 1">
      <img :src="soldierImg" alt="텅장일병 마스코트" class="mascot" />
      <h2 class="ob-title">충성! 환영합니다</h2>
      <p class="ob-desc">전역까지 남은 자산을 똑똑하게 관리해요</p>
      <div class="feat-card">
        <div v-for="f in FEATURES" :key="f" class="feat">
          <span class="feat__check">✅</span>{{ f }}
        </div>
      </div>
      <p class="ob-note">위 기능을 위해 계좌 연동이 필요해요</p>
      <BottomButtonBar primary-label="계좌 연동하기" @primary-click="goAgree" />
    </template>

    <!-- STEP 2 · 약관 동의 -->
    <template v-else-if="step === 2">
      <h2 class="ob-title sm">오픈뱅킹 이용 동의</h2>
      <div class="terms">
        <button class="terms__head" @click="termsOpen = !termsOpen">
          약관 필수 동의 <span>{{ termsOpen ? '▲' : '▼' }}</span>
        </button>
        <ul v-if="termsOpen" class="terms__body">
          <li v-for="t in TERMS" :key="t">{{ t }}</li>
        </ul>
      </div>
      <label class="agree">
        <input type="checkbox" v-model="agreed" />
        위 약관에 모두 동의합니다 (필수)
      </label>
      <p class="ob-note">위 기능을 위해 계좌 연동이 필요해요</p>
      <BottomButtonBar
        primary-label="동의"
        :primary-disabled="!agreed"
        @primary-click="confirmAgree"
      />
    </template>

    <!-- STEP 3 · 계좌 선택 -->
    <template v-else-if="step === 3">
      <h2 class="ob-title sm">연동할 계좌 선택</h2>
      <p class="ob-sub">(필수) 군장병적금, 입출금 계좌 1개</p>
      <div class="acc-list">
        <button
          v-for="a in ACCOUNTS.filter((x) => x.required)"
          :key="a.fintechUseNum"
          class="acc"
          :class="{ 'is-on': selected.has(a.fintechUseNum) }"
        >
          <span class="acc__check">{{ selected.has(a.fintechUseNum) ? '✅' : '⬜' }}</span>
          <span class="acc__body">
            <span class="acc__name">{{ a.bankName }} {{ a.productName }}</span>
            <span class="acc__bal">{{ balanceText(a) }}</span>
          </span>
        </button>
      </div>
      <p class="ob-sub" style="margin-top: 12px">(선택)</p>
      <div class="acc-list">
        <button
          v-for="a in ACCOUNTS.filter((x) => !x.required)"
          :key="a.fintechUseNum"
          class="acc"
          :class="{ 'is-on': selected.has(a.fintechUseNum) }"
          @click="toggleAccount(a)"
        >
          <span class="acc__check">{{ selected.has(a.fintechUseNum) ? '✅' : '⬜' }}</span>
          <span class="acc__body">
            <span class="acc__name">{{ a.bankName }} {{ a.productName }}</span>
            <span class="acc__bal">{{ balanceText(a) }}</span>
          </span>
        </button>
      </div>
      <BottomButtonBar
        :primary-label="linking ? '연동 중…' : '완료'"
        :primary-disabled="linking"
        @primary-click="linkAccounts"
      />
    </template>

    <!-- STEP 4 · 연동 완료 -->
    <template v-else>
      <img :src="soldierImg" alt="텅장일병 마스코트" class="mascot" />
      <h2 class="ob-title">연동 완료!</h2>
      <p class="ob-desc">총 계좌 {{ linkedAccounts.length }}개가 연동됐어요</p>
      <div class="acc-list">
        <div v-for="a in linkedAccounts" :key="a.fintechUseNum" class="acc is-on">
          <span class="acc__check">✅</span>
          <span class="acc__body">
            <span class="acc__name">{{ a.bankName }} {{ a.productName }}</span>
            <span class="acc__bal">{{ balanceText(a) }}</span>
          </span>
        </div>
      </div>
      <BottomButtonBar primary-label="완료" @primary-click="startService" />
    </template>
  </div>
</template>

<style scoped>
.ob {
  padding: 24px 0 88px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 100%;
}
.mascot {
  align-self: center;
  width: 120px;
  height: auto;
  margin-top: 12px;
}
.ob-title {
  align-self: center;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
}
.ob-title.sm {
  align-self: flex-start;
  font-size: 18px;
}
.ob-desc {
  align-self: center;
  font-size: 13px;
  color: var(--text-muted);
}
.ob-sub {
  font-size: 12px;
  color: var(--text-muted);
}
.ob-note {
  align-self: center;
  margin-top: auto;
  font-size: 11px;
  color: var(--text-hint);
}
.feat-card {
  margin-top: 6px;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.feat {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-body);
}
.feat__check {
  font-size: 14px;
}
.terms {
  border: 1px solid var(--line);
  border-radius: 12px;
  overflow: hidden;
}
.terms__head {
  width: 100%;
  padding: 14px;
  border: 0;
  background: #fff9e6;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-body);
  display: flex;
  justify-content: space-between;
  cursor: pointer;
  font-family: inherit;
}
.terms__body {
  list-style: none;
  margin: 0;
  padding: 12px 16px;
}
.terms__body li {
  font-size: 12px;
  color: var(--text-muted);
  padding: 4px 0;
}
.agree {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-body);
}
.acc-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.acc {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 14px;
  border: 1.5px solid var(--line);
  border-radius: 12px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
}
.acc.is-on {
  border-color: var(--kb-yellow-deep);
  background: #fff9e6;
}
.acc__check {
  font-size: 16px;
}
.acc__body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.acc__name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-body);
}
.acc__bal {
  font-size: 11px;
  color: var(--text-muted);
}
.ob :deep(.bottom-button-bar .bar-button.primary) {
  background: var(--kb-yellow);
  color: var(--text-strong);
}
</style>
