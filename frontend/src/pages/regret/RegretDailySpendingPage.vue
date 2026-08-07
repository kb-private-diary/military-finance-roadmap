<script setup>
// SCR-REG-04 · 일자별 지출  담당: 수연
// 캘린더에서 고른 날짜(route.params.date)의 지출 목록 + 태깅 상태 뱃지 (담백 버전)
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import regretApi from '@/api/regretApi';
import { formatWon, formatDate } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const route = useRoute();
const router = useRouter();

// 조회 날짜 (없으면 오늘). ISO 'YYYY-MM-DD' 로 다룸
const dateParam =
  route.params.date || new Date().toISOString().slice(0, 10);

const CATEGORY = {
  FOOD: { label: '식비', icon: '🍚' },
  CAFE: { label: '카페/간식', icon: '☕' },
  SHOPPING: { label: '쇼핑', icon: '🛍️' },
  TRANSPORT: { label: '교통', icon: '🚕' },
  CULTURE: { label: '문화/여가', icon: '🎬' },
  CONVENIENCE: { label: '편의점', icon: '🏪' },
  ETC: { label: '기타', icon: '💳' },
};
const cat = (c) => CATEGORY[c] || { label: c, icon: '💳' };

// 태깅 상태 → 뱃지 라벨/클래스 (파스텔)
const REVIEW = {
  SATISFIED: { label: '만족', cls: 'sat' },
  SOSO: { label: '애매', cls: 'soso' },
  REGRET: { label: '후회', cls: 'reg' },
};
const badge = (t) => REVIEW[t] || { label: '미점호', cls: 'none' };

// TODO: 백엔드 지출 API 연동 확인되면 샘플 폴백 제거
const SAMPLE = [
  { spendingId: 1, merchantName: '무신사 스토어', category: 'SHOPPING', amount: 38500, spentAt: `${dateParam}T13:20:00`, reviewType: 'REGRET' },
  { spendingId: 2, merchantName: '배달의민족', category: 'FOOD', amount: 21000, spentAt: `${dateParam}T19:05:00`, reviewType: 'SATISFIED' },
  { spendingId: 3, merchantName: '스타벅스 장전점', category: 'CAFE', amount: 6300, spentAt: `${dateParam}T10:40:00`, reviewType: 'SOSO' },
  { spendingId: 4, merchantName: 'GS25 부산대점', category: 'CONVENIENCE', amount: 4800, spentAt: `${dateParam}T22:15:00`, reviewType: null },
  { spendingId: 5, merchantName: '카카오T 택시', category: 'TRANSPORT', amount: 11000, spentAt: `${dateParam}T23:40:00`, reviewType: null },
];

const items = ref([]);
const loading = ref(true);
// 실 지출 API 성공 여부 (SAMPLE 폴백 시 "미리보기" 뱃지 표시)
const usingSample = ref(false);

// 그날 지출만 필터 (spentAt 앞 10자리 == dateParam)
const load = async () => {
  loading.value = true;
  try {
    const d = await regretApi.findSpendings();
    const list = d?.length ? d : SAMPLE;
    usingSample.value = !d?.length; // 실데이터 없으면 SAMPLE
    items.value = list.filter(
      (s) => (s.spentAt || '').slice(0, 10) === dateParam,
    );
    // 실데이터에 해당 날짜가 하나도 없으면 화면 확인용 샘플로 폴백
    if (!items.value.length) {
      items.value = SAMPLE; // TODO: 폴백 제거
      usingSample.value = true;
    }
  } catch {
    items.value = SAMPLE; // TODO: 폴백 제거
    usingSample.value = true;
  } finally {
    loading.value = false;
  }
};
onMounted(load);

// 시각 표기 HH:mm (spentAt 이 날짜만 있으면 빈 문자열)
const timeOf = (spentAt) => {
  const t = String(spentAt || '');
  const m = t.match(/T(\d{2}):(\d{2})/);
  return m ? `${m[1]}:${m[2]}` : '';
};

const dayTotal = computed(() =>
  items.value.reduce((acc, s) => acc + (s.amount || 0), 0),
);
const untaggedCount = computed(
  () => items.value.filter((s) => !s.reviewType).length,
);

const goReview = () => router.push({ name: 'RegretReview' });
const goBack = () => router.push({ name: 'RegretDashboard' });
</script>

<template>
  <div class="daily">
    <header class="head">
      <div class="htx">
        <p class="cap">
          일자별 지출
          <span v-if="usingSample" class="preview-tag">미리보기 · 샘플 데이터예요</span>
        </p>
        <h2 class="title">{{ formatDate(dateParam) }}</h2>
      </div>
    </header>

    <p v-if="loading" class="loading">불러오는 중...</p>

    <template v-else>
      <!-- 그날 총지출 -->
      <BaseCard padding="16px 14px">
        <p class="sum-cap">이 날 총지출</p>
        <p class="sum-amt">{{ formatWon(dayTotal) }}</p>
        <p class="sum-sub">{{ items.length }}건 · 미점호 {{ untaggedCount }}건</p>
      </BaseCard>

      <!-- 지출 카드 목록 -->
      <BaseCard v-if="items.length" padding="6px 14px">
        <div v-for="s in items" :key="s.spendingId" class="srow">
          <span class="ci">{{ cat(s.category).icon }}</span>
          <span class="sinfo">
            <span class="smer">{{ s.merchantName }}</span>
            <span class="smeta">
              <template v-if="timeOf(s.spentAt)">{{ timeOf(s.spentAt) }} · </template>{{ cat(s.category).label }}
            </span>
          </span>
          <span class="sright">
            <span class="samt">{{ formatWon(s.amount) }}</span>
            <span class="badge" :class="badge(s.reviewType).cls">
              {{ badge(s.reviewType).label }}
            </span>
          </span>
        </div>
      </BaseCard>

      <EmptyState
        v-else
        title="이 날은 지출이 없어요"
        description="다른 날짜를 골라보거나 대시보드로 돌아가세요"
      />

      <!-- 미점호 유도 배너 -->
      <button v-if="untaggedCount" class="review-invite" @click="goReview">
        <span class="ri-ico">📮</span>
        <span class="ri-tx">
          <b>아직 점호 안 한 소비 {{ untaggedCount }}건</b>
          <span>지금 점호하러 갈까요?</span>
        </span>
        <span class="ri-go">›</span>
      </button>

    </template>

    <BottomButtonBar primary-label="확인" @primary-click="goBack" />
  </div>
</template>

<style scoped>
.daily {
  padding: 20px 0 88px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* 만족/애매/후회 파스텔톤 (colors.css pastel 재사용) - 뱃지 공통 */
  --success: #9cd495; /* 만족 파스텔 그린 */
  --soso: #fbd55b;    /* 애매 파스텔 옐로 */
  --danger: #f8a5a5;  /* 후회 파스텔 핑크 */
  --sat-bg: #eef7ea;
  --sat-text: #3f9d63;
  --soso-bg: #fdf6da;
  --soso-text: #c99a1f;
  --reg-bg: #fdeeee;
  --reg-text: #d97676;
}
.head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.back {
  width: 30px;
  height: 30px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: var(--text-body);
  font-size: 17px;
  line-height: 1;
  cursor: pointer;
  font-family: inherit;
  flex-shrink: 0;
}
.htx {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.cap {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  color: var(--text-muted);
}
.preview-tag {
  font-size: 9px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 999px;
  background: var(--kb-yellow-pale);
  color: var(--brand-gold);
}
.title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong);
}
/* ── 총지출 요약 ── */
.sum-cap {
  font-size: 12px;
  color: var(--text-muted);
}
.sum-amt {
  margin-top: 4px;
  font-size: 26px;
  font-weight: 800;
  color: var(--text-strong);
}
.sum-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-muted);
}
/* ── 지출 카드 목록 ── */
.srow {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 0;
  border-top: 1px solid var(--line);
}
.srow:first-of-type {
  border-top: 0;
}
.ci {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: var(--gray-pale-bg);
  font-size: 18px;
}
.sinfo {
  display: flex;
  flex-direction: column;
  gap: 3px;
  flex: 1;
  min-width: 0;
}
.smer {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-body);
}
.smeta {
  font-size: 11px;
  color: var(--text-hint);
}
.sright {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}
.samt {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-body);
  font-variant-numeric: tabular-nums;
}
.badge {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--gray-pale-bg);
  color: var(--text-muted);
}
.badge.sat {
  background: var(--sat-bg);
  color: var(--sat-text);
}
.badge.soso {
  background: var(--soso-bg);
  color: var(--soso-text);
}
.badge.reg {
  background: var(--reg-bg);
  color: var(--reg-text);
}
.badge.none {
  background: var(--kb-gray-pale);
  color: var(--text-muted);
}
/* ── 미점호 유도 배너 ── */
.review-invite {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 15px 16px;
  border: 0;
  border-radius: 12px;
  background: var(--kb-yellow);
  cursor: pointer;
  font-family: inherit;
  text-align: left;
}
.ri-ico {
  font-size: 22px;
}
.ri-tx {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.ri-tx b {
  font-size: 14px;
  font-weight: 800;
  color: var(--text-strong);
}
.ri-tx span {
  font-size: 11px;
  color: var(--text-body);
}
.ri-go {
  font-size: 18px;
  color: var(--text-strong);
}
.loading {
  padding: 60px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
</style>
