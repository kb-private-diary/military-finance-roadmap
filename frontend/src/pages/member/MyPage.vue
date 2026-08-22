<script setup>
// SCR-MYP-01 · 마이페이지 - 내 정보 조회 (담당: 호빈)
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import memberApi from '@/api/memberApi';
import { formatDate } from '@/util/format';
import { usePush } from '@/composables/usePush';
import ToggleSwitch from '@/components/common/ToggleSwitch.vue';
import bearSalute from '@/assets/images/bear-salute.png';

const router = useRouter();
const {
  isSupported: pushSupported,
  isSubscribed: pushSubscribed,
  loading: pushLoading,
  checkSubscription,
  subscribe,
  unsubscribe,
} = usePush();
const handlePushToggle = (next) => (next ? subscribe() : unsubscribe());

// TODO: 계급조회 API 나오면 아래 하드코딩된 목록을 API 조회로 교체
const MILITARY_RANKS = [
  { rankId: 1, name: '이병' },
  { rankId: 2, name: '일병' },
  { rankId: 3, name: '상병' },
  { rankId: 4, name: '병장' },
];

const member = ref(null);
const militaryTypes = ref([]);
const loading = ref(true);
const errorMessage = ref('');

const typeName = (typeId) => militaryTypes.value.find((t) => t.typeId === typeId)?.typeName || '-';
const rankName = (rankId) => MILITARY_RANKS.find((r) => r.rankId === rankId)?.name || '';

// 입대일 기준 며칠째 복무 중인지 (입대 당일도 1일차로 센다)
const daysSinceEnlist = computed(() => {
  if (!member.value?.enlistDate) return null;
  const diff = Math.floor(
    (new Date(new Date().toDateString()) - new Date(member.value.enlistDate)) / 86400000,
  );
  return diff >= 0 ? diff + 1 : null;
});

const load = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    const [memberResult, typesResult] = await Promise.all([
      memberApi.getMyInfo(),
      memberApi.findMilitaryTypes(),
    ]);
    member.value = memberResult;
    militaryTypes.value = typesResult;
  } catch (e) {
    errorMessage.value = e.response?.data?.message || '내 정보를 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  load();
  checkSubscription();
});
</script>

<template>
  <div class="mypage">
    <p v-if="loading" class="text-caption">불러오는 중...</p>
    <p v-else-if="errorMessage" class="mypage__error text-caption">{{ errorMessage }}</p>

    <template v-else-if="member">
      <section class="mypage__hero">
        <div v-if="daysSinceEnlist !== null" class="mypage__badge">
          <svg class="mypage__badge-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 13C4 9.13401 7.13401 6 11 6H13C16.866 6 20 9.13401 20 13V14H4V13Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round" />
            <path d="M2 14H22" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
            <path d="M12 6V4" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
            <circle cx="12" cy="2.6" r="1" fill="currentColor" />
          </svg>
          <span>입대한 지 {{ daysSinceEnlist }}일 째</span>
        </div>

        <p class="mypage__name">{{ member.name }} {{ rankName(member.rankId) }}님</p>

        <img :src="bearSalute" alt="경례하는 곰돌이 캐릭터" class="mypage__mascot" />
      </section>

      <section class="mypage__info">
        <div class="mypage__row">
          <span class="mypage__label">이메일</span>
          <span class="mypage__value">{{ member.userId }}</span>
        </div>
        <div class="mypage__row">
          <span class="mypage__label">연락처</span>
          <span class="mypage__value">{{ member.phone }}</span>
        </div>
        <div class="mypage__row">
          <span class="mypage__label">군종</span>
          <span class="mypage__value">{{ typeName(member.typeId) }}</span>
        </div>
        <div class="mypage__row">
          <span class="mypage__label">사단</span>
          <span class="mypage__value">{{ member.unitName || '-' }}</span>
        </div>
        <div class="mypage__row">
          <span class="mypage__label">입대일</span>
          <span class="mypage__value">{{ member.enlistDate ? formatDate(member.enlistDate) : '-' }}</span>
        </div>
        <div class="mypage__row">
          <span class="mypage__label">전역예정일</span>
          <span class="mypage__value">{{ member.dischargeDate ? formatDate(member.dischargeDate) : '-' }}</span>
        </div>
      </section>

      <section class="mypage__settings">
        <ToggleSwitch
          :model-value="pushSubscribed"
          label="푸시 알림 받기"
          :disabled="!pushSupported || pushLoading"
          @update:model-value="handlePushToggle"
        />
        <button type="button" class="mypage__ob-btn" @click="router.push({ name: 'RegretLink' })">
          <span>오픈뱅킹 연동 관리</span>
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 6L15 12L9 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </button>
      </section>

      <button type="button" class="mypage__edit-btn" @click="router.push({ name: 'MyPageEdit' })">
        정보 수정
      </button>

      <button type="button" class="mypage__password-btn" @click="router.push({ name: 'MyPagePassword' })">
        비밀번호 변경
      </button>

      <button type="button" class="mypage__withdraw-link" @click="router.push({ name: 'MyPageWithdraw' })">
        KB 텅장일병일기 서비스 탈퇴
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M9 6L15 12L9 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
      </button>
    </template>
  </div>
</template>

<style scoped>
.mypage {
  padding-bottom: 40px;
}

.mypage__error {
  color: var(--danger);
}

.mypage__hero {
  position: relative;
  padding: 20px 100px 24px 20px;
  margin: 4px 0 4px;
  background-color: var(--kb-gray-pale);
  border-radius: 16px;
  overflow: hidden;
}

.mypage__badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  border-radius: 999px;
  background-color: var(--kb-yellow-deep);
  color: var(--text-strong);
  font-size: 12px;
  font-weight: 700;
}

.mypage__badge-icon {
  width: 14px;
  height: 14px;
}

.mypage__name {
  margin: 12px 0 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-strong);
}

.mypage__mascot {
  position: absolute;
  right: 8px;
  bottom: 0;
  width: 84px;
  height: auto;
  pointer-events: none;
}

.mypage__info {
  display: flex;
  flex-direction: column;
}

.mypage__row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 4px;
  border-bottom: 1px solid var(--line);
}

.mypage__row:last-child {
  border-bottom: none;
}

.mypage__label {
  font-size: 14px;
  color: var(--text-muted);
}

.mypage__value {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-strong);
}

.mypage__settings {
  margin-top: 20px;
  padding: 14px 4px;
  border-top: 1px solid var(--line);
}

.mypage__ob-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  margin-top: 14px;
  padding: 12px 0 2px;
  border: none;
  border-top: 1px solid var(--line);
  background: none;
  color: var(--text-body);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.mypage__edit-btn {
  display: block;
  width: 100%;
  margin-top: 20px;
  padding: 14px;
  border: none;
  border-radius: 999px;
  background-color: var(--kb-yellow-deep);
  color: var(--text-strong);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.mypage__password-btn {
  display: block;
  width: 100%;
  margin-top: 10px;
  padding: 14px;
  border: 1.5px solid var(--kb-yellow-deep);
  border-radius: 999px;
  background-color: #ffffff;
  color: var(--text-strong);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.mypage__withdraw-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  width: 100%;
  margin-top: 20px;
  padding: 8px;
  border: none;
  background: none;
  color: var(--text-hint);
  font-size: 13px;
  cursor: pointer;
}
</style>
