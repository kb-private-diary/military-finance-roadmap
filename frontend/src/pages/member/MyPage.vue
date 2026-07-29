<script setup>
// SCR-MYP-01 · 마이페이지 - 내 정보 조회 (담당: 호빈)
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import memberApi from '@/api/memberApi';

const router = useRouter();

// TODO: 군종목록조회/계급조회 API 나오면 하드코딩 목록을 API 조회로 교체 (SignupMilitaryPage와 동일한 임시 목록)
const MILITARY_TYPES = [
  { typeId: 1, name: '육군' },
  { typeId: 2, name: '해군' },
  { typeId: 3, name: '공군' },
  { typeId: 4, name: '해병대' },
  { typeId: 5, name: '공익' },
  { typeId: 6, name: '기타' },
];
const MILITARY_RANKS = [
  { rankId: 1, name: '이병' },
  { rankId: 2, name: '일병' },
  { rankId: 3, name: '상병' },
  { rankId: 4, name: '병장' },
];

const member = ref(null);
const loading = ref(true);
const errorMessage = ref('');

const typeName = (typeId) => MILITARY_TYPES.find((t) => t.typeId === typeId)?.name || '-';
const rankName = (rankId) => MILITARY_RANKS.find((r) => r.rankId === rankId)?.name || '-';

const dischargeDday = (dischargeDate) => {
  if (!dischargeDate) return '';
  const diff = Math.ceil((new Date(dischargeDate) - new Date(new Date().toDateString())) / 86400000);
  return diff >= 0 ? `D-${diff}` : `D+${Math.abs(diff)}`;
};

const load = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    member.value = await memberApi.getMyInfo();
  } catch (e) {
    errorMessage.value = e.response?.data?.message || '내 정보를 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

onMounted(load);
</script>

<template>
  <div class="mypage">
    <h1 class="text-title mt-4 mb-4">마이페이지</h1>

    <p v-if="loading" class="text-caption">불러오는 중...</p>
    <p v-else-if="errorMessage" class="mypage__error text-caption">{{ errorMessage }}</p>

    <template v-else-if="member">
      <section class="mypage__card">
        <p class="mypage__name">{{ member.name }} 님</p>
        <p class="text-caption mypage__id">{{ member.userId }}</p>
      </section>

      <section class="mypage__info">
        <div class="mypage__row">
          <span class="mypage__label">전화번호</span>
          <span class="mypage__value">{{ member.phone }}</span>
        </div>
        <div class="mypage__row">
          <span class="mypage__label">군종 / 계급</span>
          <span class="mypage__value">{{ typeName(member.typeId) }} / {{ rankName(member.rankId) }}</span>
        </div>
        <div class="mypage__row">
          <span class="mypage__label">부대</span>
          <span class="mypage__value">{{ member.unitName || '-' }}</span>
        </div>
        <div class="mypage__row">
          <span class="mypage__label">입대일</span>
          <span class="mypage__value">{{ member.enlistDate || '-' }}</span>
        </div>
        <div class="mypage__row">
          <span class="mypage__label">전역예정일</span>
          <span class="mypage__value">
            {{ member.dischargeDate || '-' }}
            <span v-if="member.dischargeDate" class="mypage__dday">{{ dischargeDday(member.dischargeDate) }}</span>
          </span>
        </div>
      </section>

      <section class="mypage__menu">
        <button type="button" class="mypage__menu-item" @click="router.push({ name: 'MyPageEdit' })">
          회원정보 수정
        </button>
        <button type="button" class="mypage__menu-item" @click="router.push({ name: 'MyPagePassword' })">
          비밀번호 변경
        </button>
        <button type="button" class="mypage__menu-item mypage__menu-item--danger" @click="router.push({ name: 'MyPageWithdraw' })">
          회원 탈퇴
        </button>
      </section>
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

.mypage__card {
  padding: 20px 4px;
  border-bottom: 8px solid var(--kb-gray-pale);
}

.mypage__name {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0 0 4px;
}

.mypage__id {
  margin: 0;
}

.mypage__info {
  padding: 20px 4px;
  border-bottom: 8px solid var(--kb-gray-pale);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.mypage__row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mypage__label {
  font-size: 14px;
  color: var(--text-muted, #9e9e9e);
}

.mypage__value {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-strong);
}

.mypage__dday {
  margin-left: 6px;
  font-size: 12px;
  font-weight: 700;
  color: var(--kb-yellow-deep);
}

.mypage__menu {
  display: flex;
  flex-direction: column;
  padding: 8px 0;
}

.mypage__menu-item {
  text-align: left;
  padding: 16px 4px;
  border: none;
  background: none;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-strong);
  cursor: pointer;
}

.mypage__menu-item--danger {
  color: var(--danger);
}
</style>
