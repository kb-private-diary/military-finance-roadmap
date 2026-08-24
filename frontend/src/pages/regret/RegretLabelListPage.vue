<script setup>
// SCR-REG-08 · 라벨별 모아보기 (후회/애매/만족)  담당: 수연
// 대시보드 요약(범례·건수)에서 라벨 클릭 → 해당 라벨 소비만 모아보는 페이지
// 상단 세그먼트로 라벨 전환 가능. 실데이터의 reviewType 으로 필터, 없으면 빈 상태.
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import regretApi from '@/api/regretApi';
import { formatWon } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import { useToast } from '@/composables/useToast';

const route = useRoute();
const router = useRouter();
const { show: showToast } = useToast();

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
  PX: { label: 'PX·군마트', icon: '🎖️' },
  DELIVERY: { label: '배달', icon: '🛵' },
  GAME: { label: '게임/결제', icon: '🎮' },
  TELECOM: { label: '통신', icon: '📱' },
  VACATION: { label: '휴가/여행', icon: '✈️' },
  ETC: { label: '기타', icon: '💳' },
};
const cat = (c) => CATEGORY[c] || { label: c, icon: '💳' };

// findSpendings() 결과에서 reviewType 으로 라벨 필터. 해당 라벨 없으면 빈 상태.
const allItems = ref([]); // 실 API 로 받은 전체 지출(태깅 포함)
const loading = ref(true);

const load = async () => {
  loading.value = true;
  try {
    const d = await regretApi.findSpendings();
    allItems.value = d || [];
  } catch {
    allItems.value = [];
    showToast('소비 내역을 불러오지 못했어요', 'error');
  } finally {
    loading.value = false;
  }
};
onMounted(load);

// 현재 라벨 항목: 실데이터에서 reviewType 필터
const items = computed(() =>
  allItems.value.filter((s) => s.reviewType === label.value),
);

const totalAmount = computed(() =>
  items.value.reduce((acc, s) => acc + (s.amount || 0), 0),
);

// 세그먼트용 라벨별 건수
const countOf = (key) =>
  allItems.value.filter((s) => s.reviewType === key).length;

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
    <PageHeader breadcrumb="모아보기" :title="`${labelMeta.name}한 소비`" size="lg" />

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

    <BottomButtonBar primary-label="확인" @primary-click="goBack" />
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
