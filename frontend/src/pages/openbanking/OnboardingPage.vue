<script setup>
// SCR-COM-08 · 오픈뱅킹 온보딩 (신규+미연동 사용자)  담당: 수연
// 흐름: 회원가입 → 로그인 → (여기) 환영 → 약관동의 → 계좌선택 → 연동완료 → 서비스 시작
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import openbankingApi from '@/api/openbankingApi';
import { useToast } from '@/composables/useToast';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import BaseCard from '@/components/common/BaseCard.vue';
import SignupStepHeader from '@/components/common/SignupStepHeader.vue';
import TermsAccordion from '@/components/common/TermsAccordion.vue';
import soldierImg from '@/assets/images/soldier-salute.png';

const router = useRouter();
const { show } = useToast();

const step = ref(1); // 1 환영 · 2 약관 · 3 계좌선택 · 4 완료

// ── Step1 서비스 소개 ─────────────────────────────────────
const FEATURES = [
  '실시간 군적금 납입 현황',
  '자동 지출 분석 · 후회 회고',
  '맞춤 목표 · 금융상품 추천',
  '절감 목표 · 적금 추천',
];

// ── Step2 약관 (공용 TermsAccordion 재사용, 약관 1개라 전체동의 박스 숨김) ──
const OB_TERMS = [
  {
    termsId: 1,
    name: '약관 필수 동의',
    required: true,
    content:
      '이용 목적\n군적금 관리 · 후회소비 회고 · 목표 로드맵\n\n동의 항목\n계좌 조회 (필수)\n잔액 조회 (필수)\n거래내역 조회 (필수)\n\n이용 기간\n1년 (자동 갱신 가능)',
  },
];
const agreedTermsIds = ref([]);
const agreed = computed(() => agreedTermsIds.value.includes(1));

// ── Step3 계좌 선택 ───────────────────────────────────────
// 실제 오픈뱅킹처럼 인증 후 계좌 목록을 API(getAccounts)로 받아옴 (하드코딩 샘플 제거)
const accounts = ref([]);            // 연동 가능한 계좌 목록 (서버 조회)
const selected = ref(new Set());     // 선택된 fintechUseNum (기본: 전체 선택)
const accountsLoading = ref(false);

// 계좌 목록 조회 (약관 동의 후 step3 진입 시). 실패하면 빈 목록 + 에러 토스트
const loadAccounts = async () => {
  accountsLoading.value = true;
  try {
    const list = (await openbankingApi.getAccounts()) || [];
    accounts.value = list;
    selected.value = new Set(list.map((a) => a.fintechUseNum)); // 기본 전체 선택
  } catch {
    accounts.value = [];
    show('계좌 목록을 불러오지 못했어요.', 'error');
  } finally {
    accountsLoading.value = false;
  }
};

const toggleAccount = (a) => {
  const s = new Set(selected.value);
  s.has(a.fintechUseNum) ? s.delete(a.fintechUseNum) : s.add(a.fintechUseNum);
  selected.value = s;
};
const linkedAccounts = ref([]);
const linking = ref(false);

// 잔액/거래 표시 (적금=잔액, 입출금=거래내역 수집)
const balanceText = (a) =>
  a.accountType === 'CHECKING'
    ? '지출을 자동으로 정리해요'
    : `잔액 ${((a.balance ?? 0) / 10000).toLocaleString('ko-KR')}만원`;

// 적금 금리·정부매칭·만기 표시 (적금 계좌만) — "연 5.0% · 정부매칭 100% · 만기 2027-09-09"
const rateText = (a) => {
  if (a.accountType !== 'SAVING') return '';
  const parts = [];
  if (a.interestRate != null) parts.push(`연 ${a.interestRate}%`);
  if (a.govMatchRate != null) parts.push(`정부매칭 ${a.govMatchRate}%`);
  if (a.maturityDate) parts.push(`만기 ${a.maturityDate}`);
  return parts.join(' · ');
};

// 연동 계좌 요약 안내 (적금 = 군장병적금, 입출금 = 지출 분석용)
const accountSummary = computed(() => {
  const saving = accounts.value.filter((a) => a.accountType === 'SAVING').length;
  const checking = accounts.value.filter((a) => a.accountType === 'CHECKING').length;
  const parts = [];
  if (saving) parts.push('군장병적금');
  if (checking) parts.push(`입출금 계좌 ${checking}개`);
  return parts.length ? `(필수) ${parts.join(', ')}` : '';
});

// ── 단계 이동 ─────────────────────────────────────────────
const goAgree = () => (step.value = 2);
const confirmAgree = () => {
  if (!agreed.value) return show('필수 약관에 동의해주세요.', 'error');
  step.value = 3;
  loadAccounts(); // 실제 오픈뱅킹처럼 인증 후 계좌 목록 조회
};
const linkAccounts = async () => {
  if (linking.value) return;
  linking.value = true;
  const nums = [...selected.value];
  if (!nums.length) {
    linking.value = false;
    return show('연동할 계좌를 하나 이상 선택해주세요.', 'error');
  }
  try {
    const linked = await openbankingApi.link({
      code: 'mock',
      state: 'mock',
      selectedFintechNums: nums,
    });
    linkedAccounts.value = linked || [];
    step.value = 4; // 성공 시에만 완료 화면으로 (하드코딩 폴백 제거)
  } catch {
    show('계좌 연동에 실패했어요. 다시 시도해주세요.', 'error');
  } finally {
    linking.value = false;
  }
};
const startService = async () => {
  try {
    await openbankingApi.sync(); // 연동 후 거래내역·급여 동기화 (spending/income 적재)
  } catch {
    // 동기화 실패해도 진행 (홈에서 다시 시도 가능)
  }
  sessionStorage.setItem('ob_linked', 'Y'); // 연동 완료 → 라우터 가드 통과
  router.push({ name: 'Home' });
};
</script>

<template>
  <div class="ob">
    <!-- STEP 1 · 환영 -->
    <template v-if="step === 1">
      <p class="ob-eyebrow">오픈뱅킹 연동</p>
      <img :src="soldierImg" alt="텅장일병 마스코트" class="mascot" />
      <h2 class="ob-title">충성! 환영합니다</h2>
      <p class="ob-desc">전역까지 남은 자산을 똑똑하게 관리해요</p>
      <BaseCard padding="16px" class="feat-card">
        <div v-for="f in FEATURES" :key="f" class="feat">
          <span class="feat__check">✅</span>{{ f }}
        </div>
      </BaseCard>
      <p class="ob-note">위 기능을 위해 계좌 연동이 필요해요</p>
      <BottomButtonBar primary-label="계좌 연동하기" @primary-click="goAgree" />
    </template>

    <!-- STEP 2 · 약관 동의 (공용 TermsAccordion) -->
    <template v-else-if="step === 2">
      <SignupStepHeader
        eyebrow="오픈뱅킹 연동"
        step="1 / 3"
        title="오픈뱅킹 이용 동의"
      />
      <TermsAccordion
        v-model="agreedTermsIds"
        :terms="OB_TERMS"
        :show-select-all="false"
      />
      <p class="ob-note">위 기능을 위해 계좌 연동이 필요해요</p>
      <BottomButtonBar
        primary-label="동의"
        :primary-disabled="!agreed"
        @primary-click="confirmAgree"
      />
    </template>

    <!-- STEP 3 · 계좌 선택 -->
    <template v-else-if="step === 3">
      <SignupStepHeader
        eyebrow="오픈뱅킹 연동"
        step="2 / 3"
        title="연동할 계좌 선택"
      />
      <p v-if="accountSummary" class="acc-hint">{{ accountSummary }}</p>
      <p v-if="accountsLoading" class="ob-sub">계좌 불러오는 중…</p>
      <div class="acc-list">
        <button
          v-for="a in accounts"
          :key="a.fintechUseNum"
          class="acc"
          :class="{ 'is-on': selected.has(a.fintechUseNum) }"
          @click="toggleAccount(a)"
        >
          <span class="acc__mark" aria-hidden="true"></span>
          <span class="acc__body">
            <span class="acc__name">{{ a.bankName }} {{ a.productName }}</span>
            <span class="acc__bal">{{ balanceText(a) }}</span>
            <span v-if="rateText(a)" class="acc__rate">{{ rateText(a) }}</span>
          </span>
        </button>
      </div>
      <p v-if="!accountsLoading && !accounts.length" class="ob-sub">연동 가능한 계좌가 없어요.</p>
      <BottomButtonBar
        :primary-label="linking ? '연동 중…' : '연동하기'"
        :primary-disabled="linking"
        @primary-click="linkAccounts"
      />
    </template>

    <!-- STEP 4 · 연동 완료 -->
    <template v-else>
      <div class="ob-hero-head">
        <p class="ob-eyebrow">오픈뱅킹 연동</p>
        <p class="ob-step">3 / 3</p>
      </div>
      <img :src="soldierImg" alt="텅장일병 마스코트" class="mascot" />
      <h2 class="ob-title">연동 완료!</h2>
      <p class="ob-desc">총 계좌 {{ linkedAccounts.length }}개가 연동됐어요</p>
      <div class="acc-list">
        <div v-for="a in linkedAccounts" :key="a.fintechUseNum" class="acc is-on">
          <span class="acc__mark" aria-hidden="true"></span>
          <span class="acc__body">
            <span class="acc__name">{{ a.bankName }} {{ a.productName }}</span>
            <span class="acc__bal">{{ balanceText(a) }}</span>
          </span>
        </div>
      </div>
      <BottomButtonBar primary-label="시작하기" @primary-click="startService" />
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
/* 인트로·완료 화면 상단 좌측 라벨(오픈뱅킹 연동) + 단계 표시 */
.ob-eyebrow {
  align-self: flex-start;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
}
.ob-step {
  align-self: flex-start;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  color: var(--text-body);
}
.ob-hero-head {
  align-self: stretch;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.feat-card {
  margin-top: 6px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
/* BaseCard 기본보다 살짝 진한 그림자 (잘 보이되 과하지 않게) */
.ob .feat-card {
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.06);
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
.acc-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.acc-hint {
  margin: -2px 0 2px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
}
.acc {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 14px;
  border: 1px solid #ececec;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
  text-align: left;
  cursor: pointer;
  font-family: inherit;
  transition: border-color 0.15s ease;
}
/* 선택 시 노란 테두리 */
.acc.is-on {
  border-color: var(--kb-yellow-deep);
}
/* 초록 원형 체크: 미선택=회색 테두리 원, 선택=초록 원+흰 체크 */
.acc__mark {
  position: relative;
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 1.5px solid #cdd1d6;
  background: #fff;
}
.acc__mark::after {
  content: '';
  position: absolute;
  left: 50%;
  top: 50%;
  width: 5px;
  height: 9px;
  border: solid #cdd1d6;
  border-width: 0 2px 2px 0;
  transform: translate(-50%, -60%) rotate(45deg);
}
.acc.is-on .acc__mark {
  background: #22a45d;
  border-color: #22a45d;
}
.acc.is-on .acc__mark::after {
  border-color: #fff;
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
.acc__rate {
  font-size: 11px;
  color: var(--kb-yellow-dark, #b8860b);
  font-weight: 600;
}
.ob :deep(.bottom-button-bar .bar-button.primary) {
  background: var(--kb-yellow);
  color: var(--text-strong);
}
</style>
