<script setup>
// SCR-REG-08 · 라벨별 모아보기 (후회/애매/만족)  담당: 수연
// 대시보드 요약(범례·건수)에서 라벨 클릭 → 해당 라벨 소비만 모아보는 페이지
// 상단 세그먼트로 라벨 전환 가능. 데이터 없어도 SAMPLE 로 항상 미리보기.
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import regretApi from '@/api/regretApi';
import { formatWon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const route = useRoute();
const router = useRouter();

// 라벨 메타 (색·이름). 후회=핑크, 애매=옐로, 만족=그린 — 다른 regret 화면과 통일
const LABELS = {
  REGRET: { name: '후회', cls: 'reg', hint: '안 썼어도 됐던 소비' },
  SOSO: { name: '애매', cls: 'soso', hint: '잘 모르겠는 소비' },
  SATISFIED: { name: '만족', cls: 'sat', hint: '잘 쓴 필요 소비' },
};
const LABEL_KEYS = ['REGRET', 'SOSO', 'SATISFIED'];

// route.params.label 검증 (잘못된 값이면 REGRET 로)
const label = computed(() =>
  LABEL_KEYS.includes(route.params.label) ? route.params.label : 'REGRET',
);
const labelMeta = computed(() => LABELS[label.value]);

const CATEGORY = {
  FOOD: { label: '식비', icon: '🍚' },
  CAFE: { label: '카페/간식', icon: '☕' },
  SHOPPING: { label: '쇼핑', icon: '🛍️' },
  TRANSPORT: { label: '교통', icon: '🚕' },
  CULTURE: { label: '문화/여가', icon: '🎬' },
  CONVENIENCE: { label: '편의점', icon: '🏪' },
  GAME: { label: '게임/결제', icon: '🎮' },
  TELECOM: { label: '통신', icon: '📱' },
  ETC: { label: '기타', icon: '💳' },
};
const cat = (c) => CATEGORY[c] || { label: c, icon: '💳' };

// TODO(백엔드): 라벨별 지출 목록 API 나오면 SAMPLE 걷어내고 연동
//   현재는 findSpendings() 결과에서 reviewType 으로 필터, 없으면 SAMPLE 폴백
// 군인 소비 현실성: 후회=충동(야식/쇼핑/게임결제/택시), 만족=필요(통신/이발/생필품)
const SAMPLE = {
  REGRET: [
    { spendingId: 101, merchantName: '배달의민족', category: 'FOOD', amount: 23000, spentAt: '2026-08-05T23:10:00' },
    { spendingId: 102, merchantName: '무신사 스토어', category: 'SHOPPING', amount: 32900, spentAt: '2026-08-05T14:20:00' },
    { spendingId: 103, merchantName: '리니지M 인앱결제', category: 'GAME', amount: 55000, spentAt: '2026-08-04T22:40:00' },
    { spendingId: 104, merchantName: '카카오T 택시', category: 'TRANSPORT', amount: 13000, spentAt: '2026-08-03T23:55:00' },
    { spendingId: 105, merchantName: 'GS25 위수지점', category: 'CONVENIENCE', amount: 8400, spentAt: '2026-08-02T21:30:00' },
    { spendingId: 106, merchantName: '스타벅스 서면점', category: 'CAFE', amount: 6300, spentAt: '2026-08-01T15:10:00' },
  ],
  SOSO: [
    { spendingId: 201, merchantName: 'BBQ 치킨', category: 'FOOD', amount: 18000, spentAt: '2026-08-05T20:00:00' },
    { spendingId: 202, merchantName: 'CU 편의점', category: 'CONVENIENCE', amount: 5500, spentAt: '2026-08-04T12:30:00' },
    { spendingId: 203, merchantName: '이디야커피', category: 'CAFE', amount: 5100, spentAt: '2026-08-03T16:20:00' },
    { spendingId: 204, merchantName: '넷플릭스', category: 'CULTURE', amount: 13500, spentAt: '2026-08-01T09:00:00' },
  ],
  SATISFIED: [
    { spendingId: 301, merchantName: 'SKT 통신요금', category: 'TELECOM', amount: 33000, spentAt: '2026-08-06T09:00:00' },
    { spendingId: 302, merchantName: '위수지역 이발소', category: 'ETC', amount: 10000, spentAt: '2026-08-05T13:00:00' },
    { spendingId: 303, merchantName: '다이소 생필품', category: 'SHOPPING', amount: 7700, spentAt: '2026-08-04T18:40:00' },
    { spendingId: 304, merchantName: '김밥천국', category: 'FOOD', amount: 6500, spentAt: '2026-08-03T12:10:00' },
    { spendingId: 305, merchantName: '온누리약국', category: 'ETC', amount: 4500, spentAt: '2026-08-02T17:20:00' },
    { spendingId: 306, merchantName: '빨래방 코인세탁', category: 'ETC', amount: 6000, spentAt: '2026-08-01T19:00:00' },
  ],
};

const allItems = ref(null); // 실 API 로 받은 전체 지출(태깅 포함). null 이면 SAMPLE 사용
const loading = ref(true);

const load = async () => {
  loading.value = true;
  try {
    const d = await regretApi.findSpendings();
    // reviewType 이 실제로 채워진 데이터가 있을 때만 실데이터로 인정
    allItems.value = d?.some((s) => s.reviewType) ? d : null;
  } catch {
    allItems.value = null; // TODO: 폴백 제거
  } finally {
    loading.value = false;
  }
};
onMounted(load);

// 현재 라벨 항목: 실데이터 있으면 필터, 없으면 SAMPLE
const usingSample = computed(() => !allItems.value);
const items = computed(() => {
  if (allItems.value) {
    return allItems.value.filter((s) => s.reviewType === label.value);
  }
  return SAMPLE[label.value] || [];
});

const totalAmount = computed(() =>
  items.value.reduce((acc, s) => acc + (s.amount || 0), 0),
);

// 세그먼트용 라벨별 건수 (실데이터/샘플 동일 로직)
const countOf = (key) =>
  allItems.value
    ? allItems.value.filter((s) => s.reviewType === key).length
    : (SAMPLE[key] || []).length;

const timeOf = (spentAt) => {
  const m = String(spentAt || '').match(/T(\d{2}):(\d{2})/);
  return m ? `${m[1]}:${m[2]}` : '';
};
const dateOf = (spentAt) => {
  const t = String(spentAt || '');
  const m = t.match(/(\d{4})-(\d{2})-(\d{2})/);
  return m ? `${Number(m[2])}/${Number(m[3])}` : '';
};

// 세그먼트 전환 = 같은 라우트의 param 만 교체 (replace 로 히스토리 안 쌓기)
const switchLabel = (key) => {
  if (key === label.value) return;
  router.replace({ name: 'RegretLabelList', params: { label: key } });
};

const goBack = () => router.push({ name: 'RegretDashboard' });

// param 이 바뀌면 스크롤 상단으로(세그먼트 전환 UX)
watch(label, () => window.scrollTo({ top: 0 }));
</script>

<template>
  <div class="label-list">
    <header class="head">
      <button class="back" aria-label="뒤로" @click="goBack">‹</button>
      <div class="htx">
        <p class="cap">
          모아보기
          <span v-if="usingSample" class="preview-tag">미리보기</span>
        </p>
        <h2 class="title">{{ labelMeta.name }}한 소비</h2>
      </div>
    </header>

    <!-- 라벨 전환 세그먼트 -->
    <div class="segment">
      <button
        v-for="key in LABEL_KEYS"
        :key="key"
        class="seg-btn"
        :class="[LABELS[key].cls, { on: key === label }]"
        @click="switchLabel(key)"
      >
        {{ LABELS[key].name }}
        <span class="seg-n">{{ countOf(key) }}</span>
      </button>
    </div>

    <p v-if="loading" class="loading">불러오는 중...</p>

    <template v-else>
      <!-- 라벨 요약 -->
      <BaseCard padding="16px 14px">
        <div class="sum" :class="labelMeta.cls">
          <div class="sum-tx">
            <span class="sum-badge" :class="labelMeta.cls">{{ labelMeta.name }}</span>
            <p class="sum-hint">{{ labelMeta.hint }}</p>
          </div>
          <div class="sum-num">
            <p class="sum-amt">{{ formatWon(totalAmount) }}</p>
            <p class="sum-cnt">{{ items.length }}건</p>
          </div>
        </div>
      </BaseCard>

      <!-- 해당 라벨 소비 목록 -->
      <BaseCard v-if="items.length" padding="6px 14px">
        <div v-for="s in items" :key="s.spendingId" class="srow">
          <span class="ci">{{ cat(s.category).icon }}</span>
          <span class="sinfo">
            <span class="smer">{{ s.merchantName }}</span>
            <span class="smeta">
              <template v-if="dateOf(s.spentAt)">{{ dateOf(s.spentAt) }} · </template>
              <template v-if="timeOf(s.spentAt)">{{ timeOf(s.spentAt) }} · </template>{{ cat(s.category).label }}
            </span>
          </span>
          <span class="samt">{{ formatWon(s.amount) }}</span>
        </div>
      </BaseCard>

      <EmptyState
        v-else
        :title="`${labelMeta.name}한 소비가 없어요`"
        description="다른 라벨을 눌러보거나 점호를 더 해보세요"
      />
    </template>

    <BottomButtonBar secondary-label="이전" @secondary-click="goBack" />
  </div>
</template>

<style scoped>
.label-list {
  padding: 20px 0 88px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* 만족/애매/후회 파스텔톤 (colors.css pastel 재사용) */
  --sat: #9cd495;
  --sat-bg: #eef7ea;
  --sat-text: #3f9d63;
  --soso: #fbd55b;
  --soso-bg: #fdf6da;
  --soso-text: #c99a1f;
  --reg: #f8a5a5;
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
  gap: 6px;
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
/* ── 세그먼트 ── */
.segment {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  padding: 4px;
  border-radius: 12px;
  background: var(--gray-pale-bg);
}
.seg-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 9px 0;
  border: 0;
  border-radius: 9px;
  background: transparent;
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
}
.seg-btn .seg-n {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 999px;
  background: var(--kb-gray-pale);
  color: var(--text-muted);
}
.seg-btn.on {
  background: #fff;
  color: var(--text-strong);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
.seg-btn.on.reg { color: var(--reg-text); }
.seg-btn.on.soso { color: var(--soso-text); }
.seg-btn.on.sat { color: var(--sat-text); }
.seg-btn.on .seg-n { background: var(--gray-pale-bg); }
/* ── 라벨 요약 ── */
.sum {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.sum-tx {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.sum-badge {
  align-self: flex-start;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 999px;
}
.sum-badge.reg { background: var(--reg-bg); color: var(--reg-text); }
.sum-badge.soso { background: var(--soso-bg); color: var(--soso-text); }
.sum-badge.sat { background: var(--sat-bg); color: var(--sat-text); }
.sum-hint {
  font-size: 12px;
  color: var(--text-muted);
}
.sum-num {
  text-align: right;
  flex-shrink: 0;
}
.sum-amt {
  font-size: 22px;
  font-weight: 800;
  color: var(--text-strong);
  font-variant-numeric: tabular-nums;
}
.sum-cnt {
  margin-top: 2px;
  font-size: 12px;
  color: var(--text-muted);
}
/* ── 지출 목록 ── */
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
.samt {
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-body);
  font-variant-numeric: tabular-nums;
}
.loading {
  padding: 60px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
</style>
