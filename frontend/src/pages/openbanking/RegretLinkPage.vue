<script setup>
// SCR-REG-01 · 오픈뱅킹 연동 관리 (담당: 수연)
// 연동 현황 조회 + 연동 계좌 목록 + 연동 해제 + (미연동 시) 연동하기
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import openbankingApi from '@/api/openbankingApi';
import dashboardApi from '@/api/dashboardApi';
import PageHeader from '@/components/common/PageHeader.vue';
import { useToast } from '@/composables/useToast';

const router = useRouter();
const { show } = useToast();

const linked = ref(false);      // 오픈뱅킹 연동 여부
const accounts = ref([]);       // 연동 계좌 목록 (Mock)
const savings = ref(null);      // 군적금 만기금 (석윤 dashboard API - 만기 예상 수령액)
const loading = ref(true);
const unlinking = ref(false);

// 잔액/거래 표시 (적금=잔액, 입출금=거래내역 수집)
const balanceText = (a) =>
  a.accountType === 'CHECKING'
    ? '지출을 자동으로 정리해요'
    : `잔액 ${((a.balance ?? 0) / 10000).toLocaleString('ko-KR')}만원`;

// 적금 금리·정부매칭·만기 표시 (적금 계좌만)
const rateText = (a) => {
  if (a.accountType !== 'SAVING') return '';
  const parts = [];
  if (a.interestRate != null) parts.push(`연 ${a.interestRate}%`);
  if (a.govMatchRate != null) parts.push(`정부매칭 ${a.govMatchRate}%`);
  if (a.maturityDate) parts.push(`만기 ${a.maturityDate}`);
  return parts.join(' · ');
};

const load = async () => {
  loading.value = true;
  try {
    linked.value = await openbankingApi.getStatus();
    if (linked.value) {
      accounts.value = (await openbankingApi.getAccounts()) || [];
      // 멘토 조언: 적금 파트(석윤 dashboard) API를 끌어다 만기 예상 수령액 표시
      try {
        savings.value = await dashboardApi.findSavingsStatus();
      } catch {
        savings.value = null; // 만기금 조회 실패해도 연동 현황은 보여줌
      }
    }
  } catch {
    show('연동 현황을 불러오지 못했어요.', 'error');
  } finally {
    loading.value = false;
  }
};

// 적금 만기까지 남은 납입 회차 (만기일 기준 프론트 계산) - 멘토: 미래 회차 표시
const remainingText = (a) => {
  if (a.accountType !== 'SAVING' || !a.maturityDate) return '';
  const m = new Date(a.maturityDate);
  const now = new Date();
  const months = (m.getFullYear() - now.getFullYear()) * 12 + (m.getMonth() - now.getMonth());
  return months > 0 ? `만기까지 ${months}회 남음` : '만기 도래';
};

// 연동/재연동은 온보딩 흐름 재사용
const goLink = () => router.push({ name: 'Onboarding' });

const handleUnlink = async () => {
  if (unlinking.value) return;
  if (!confirm('오픈뱅킹 연동을 해제할까요? 다시 이용하려면 재연동이 필요해요.')) return;
  unlinking.value = true;
  try {
    await openbankingApi.unlink();
    sessionStorage.removeItem('ob_linked'); // 라우터 가드 캐시 해제 → 다음 진입 시 온보딩 유도
    show('오픈뱅킹 연동을 해제했어요.', 'success');
    linked.value = false;
    accounts.value = [];
  } catch {
    show('연동 해제에 실패했어요. 다시 시도해주세요.', 'error');
  } finally {
    unlinking.value = false;
  }
};

onMounted(load);
</script>

<template>
  <div class="oblink">
    <PageHeader title="오픈뱅킹 연동 관리" />
    <p v-if="loading" class="oblink__sub">불러오는 중…</p>

    <!-- 연동된 경우: 계좌 목록 + 해제 -->
    <template v-else-if="linked">
      <p class="oblink__status oblink__status--on">✅ 오픈뱅킹이 연결됐어요</p>
      <div v-if="savings" class="oblink__maturity">
        <span class="oblink__maturity-label">💰 만기 예상 수령액</span>
        <b class="oblink__maturity-amt">{{ (savings.expectedMaturityTotal ?? 0).toLocaleString('ko-KR') }}원</b>
      </div>
      <div class="oblink__list">
        <div v-for="a in accounts" :key="a.fintechUseNum" class="oblink__acc">
          <span class="oblink__acc-name">{{ a.bankName }} {{ a.productName }}</span>
          <span class="oblink__acc-bal">{{ balanceText(a) }}</span>
          <span v-if="rateText(a)" class="oblink__acc-rate">{{ rateText(a) }}</span>
          <span v-if="remainingText(a)" class="oblink__acc-remain">{{ remainingText(a) }}</span>
        </div>
      </div>
      <button type="button" class="oblink__unlink" :disabled="unlinking" @click="handleUnlink">
        {{ unlinking ? '해제 중…' : '오픈뱅킹 연동 해제' }}
      </button>
    </template>

    <!-- 미연동인 경우: 연동 유도 -->
    <template v-else>
      <p class="oblink__status">아직 연동 전이에요</p>
      <p class="oblink__desc">연동하면 군적금 현황·지출 분석을 한눈에 볼 수 있어요</p>
      <button type="button" class="oblink__link" @click="goLink">오픈뱅킹 연동하기</button>
    </template>
  </div>
</template>

<style scoped>
.oblink {
  padding: 16px;
}
.oblink__sub,
.oblink__status {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 12px;
}
.oblink__status--on {
  color: var(--text-strong);
  font-weight: 600;
}
.oblink__desc {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 20px;
}
.oblink__maturity {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  margin-bottom: 16px;
  background: var(--kb-gray-pale, #f5f5f7);
  border-radius: 12px;
}
.oblink__maturity-label {
  font-size: 13px;
  color: var(--text-muted);
}
.oblink__maturity-amt {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-strong);
}
.oblink__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 24px;
}
.oblink__acc {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
}
.oblink__acc-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-body);
}
.oblink__acc-bal {
  font-size: 12px;
  color: var(--text-muted);
}
.oblink__acc-rate {
  font-size: 12px;
  color: var(--kb-yellow-dark, #b8860b);
  font-weight: 600;
}
.oblink__acc-remain {
  font-size: 12px;
  color: var(--text-muted);
}
.oblink__unlink {
  width: 100%;
  padding: 14px;
  border: 1px solid var(--danger);
  border-radius: 999px;
  background: none;
  color: var(--danger);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
.oblink__link {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 999px;
  background: var(--kb-yellow-deep);
  color: var(--text-strong);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}
</style>
