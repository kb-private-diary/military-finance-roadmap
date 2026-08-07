<script setup>
// SCR-REG-05 · 소비 점호 태깅 (킬러 인터랙션)  담당: 수연
// 지금 점호할 1건을 크게, 나머지는 아래 대기 리스트로 - 이모지 + 초록/빨강 색분리 (담백 버전)
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import regretApi from '@/api/regretApi';
import { formatWon, formatDate } from '@/util/format';
import BaseCard from '@/components/common/BaseCard.vue';

const router = useRouter();

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

// TODO: 백엔드 지출 API 연동 확인되면 샘플 폴백 제거
const SAMPLE = [
  { spendingId: 1, merchantName: '무신사 스토어', category: 'SHOPPING', amount: 38500, spentAt: '2026-08-05', reviewType: null },
  { spendingId: 2, merchantName: '배달의민족', category: 'FOOD', amount: 21000, spentAt: '2026-08-05', reviewType: null },
  { spendingId: 3, merchantName: '카카오T 택시', category: 'TRANSPORT', amount: 11000, spentAt: '2026-08-05', reviewType: null },
  { spendingId: 4, merchantName: '스타벅스 장전점', category: 'CAFE', amount: 6300, spentAt: '2026-08-04', reviewType: null },
  { spendingId: 5, merchantName: 'CGV 서면', category: 'CULTURE', amount: 15000, spentAt: '2026-08-04', reviewType: null },
];

const spendings = ref([]);
const loading = ref(true);
const recent = ref([]); // 방금 점호한 [{ id, type }] (되돌리기·요약 점 표시)

const load = async () => {
  loading.value = true;
  try {
    const d = await regretApi.findSpendings();
    spendings.value = d?.length ? d : SAMPLE;
  } catch {
    spendings.value = SAMPLE; // TODO: 폴백 제거
  } finally {
    loading.value = false;
  }
};
onMounted(load);

// 미점호 큐 (첫 건이 "지금 점호할 카드", 나머지는 대기)
const queue = computed(() => spendings.value.filter((s) => !s.reviewType));
const current = computed(() => queue.value[0] || null);
const waiting = computed(() => queue.value.slice(1));
const total = computed(() => spendings.value.length);
const done = computed(() => total.value - queue.value.length);
const progress = computed(() =>
  total.value ? Math.round((done.value / total.value) * 100) : 0,
);

// "이번 달 OO N번째 · 누적" 힌트 (같은 카테고리 기준)
const hint = computed(() => {
  const s = current.value;
  if (!s) return '';
  const same = spendings.value.filter((x) => x.category === s.category);
  const order = same.indexOf(s) + 1;
  const sum = same.reduce((acc, x) => acc + (x.amount || 0), 0);
  return `이번 달 ${cat(s.category).label} ${order}번째 · 누적 ${formatWon(sum)}`;
});

// 만족/후회 태깅 → 큐에서 빠지고 다음 카드로 (낙관적, 실패 시 롤백)
const tag = async (type) => {
  const s = current.value;
  if (!s) return;
  s.reviewType = type;
  recent.value.unshift({ id: s.spendingId, type });
  try {
    await regretApi.tagReview(s.spendingId, type);
  } catch {
    s.reviewType = null;
    recent.value.shift();
  }
};

// 나중에: 현재 카드를 큐 맨 뒤로 (태깅 안 함)
const skip = () => {
  const s = current.value;
  if (!s) return;
  const idx = spendings.value.indexOf(s);
  spendings.value.splice(idx, 1);
  spendings.value.push(s);
};

// 되돌리기: 직전 태깅 취소 (화면 표시만, 백엔드 해제는 재태깅으로 대체)
const undo = () => {
  const last = recent.value.shift();
  if (!last) return;
  const s = spendings.value.find((x) => x.spendingId === last.id);
  if (s) s.reviewType = null;
};

const goDashboard = () => router.push({ name: 'RegretDashboard' });
</script>

<template>
  <div class="review">
    <header class="head">
      <div>
        <p class="cap">소비 점호</p>
        <h2 class="title">이 소비, 만족했나요?</h2>
      </div>
      <span class="left">{{ done }} / {{ total }}</span>
    </header>
    <div v-if="total" class="roll-bar"><i :style="{ width: progress + '%' }" /></div>

    <p v-if="loading" class="loading">불러오는 중...</p>

    <!-- 다 점호했을 때 -->
    <BaseCard v-else-if="!current" padding="32px 16px">
      <div class="finish">
        <span class="fin-e">🎉</span>
        <p class="fin-t">점호 완료!</p>
        <p class="fin-s">{{ total }}건 소비를 모두 돌아봤어요</p>
        <button class="cta" @click="goDashboard">리포트 보기</button>
      </div>
    </BaseCard>

    <template v-else>
      <!-- ▍지금 점호할 1건 (크게) -->
      <BaseCard padding="22px 18px">
        <div class="roll">
          <span class="ci-big">{{ cat(current.category).icon }}</span>
          <p class="mer">{{ current.merchantName }}</p>
          <p class="when">{{ formatDate(current.spentAt) }} · {{ cat(current.category).label }}</p>
          <p class="price">{{ formatWon(current.amount) }}</p>
          <p class="hint">{{ hint }}</p>
        </div>
        <div class="tag-btns">
          <button class="tag-btn sat" @click="tag('SATISFIED')">
            <span class="e">👍</span><span class="l">만족</span><span class="k">잘 썼다</span>
          </button>
          <button class="tag-btn reg" @click="tag('REGRET')">
            <span class="e">😩</span><span class="l">후회</span><span class="k">안 썼어도 됐다</span>
          </button>
        </div>
        <div class="skip-row">
          <button class="soso" @click="tag('SOSO')">🤔 애매</button>
          <button @click="skip">⏭ 나중에</button>
          <button :disabled="!recent.length" @click="undo">↩ 되돌리기</button>
        </div>
      </BaseCard>

      <!-- 방금 점호한 요약 점 -->
      <div v-if="recent.length" class="done-strip">
        <span class="dt">방금 점호한 {{ recent.length }}건</span>
        <span class="pills">
          <i
            v-for="(r, i) in recent.slice(0, 8)"
            :key="i"
            :class="r.type === 'REGRET' ? 'r' : r.type === 'SOSO' ? 'o' : 's'"
          />
        </span>
      </div>

      <!-- ▍대기 리스트 -->
      <BaseCard v-if="waiting.length" padding="14px">
        <div class="wait-head">
          <span class="sec">점호 대기</span>
          <span class="chip">{{ waiting.length }}건 남음</span>
        </div>
        <div v-for="w in waiting" :key="w.spendingId" class="wrow">
          <span class="ci">{{ cat(w.category).icon }}</span>
          <span class="winfo">
            <span class="wmer">{{ w.merchantName }}</span>
            <span class="wmeta">{{ formatDate(w.spentAt) }} · {{ cat(w.category).label }}</span>
          </span>
          <span class="wamt">{{ formatWon(w.amount) }}</span>
        </div>
      </BaseCard>
    </template>
  </div>
</template>

<style scoped>
.review {
  padding: 20px 0 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* 만족/애매/후회 파스텔톤 (colors.css pastel 재사용) */
  --sat: #9cd495;      /* 파스텔 그린 */
  --sat-bg: #eef7ea;
  --reg: #f8a5a5;      /* 파스텔 핑크 */
  --reg-bg: #fdeeee;
  --soso: #fbd55b;     /* 파스텔 옐로 */
  --sat-text: #3f9d63; /* 라벨 가독성용 진한 버전 */
  --reg-text: #d97676;
}
.head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}
.cap {
  font-size: 12px;
  color: var(--text-muted);
}
.title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-strong);
}
.left {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-muted);
  font-variant-numeric: tabular-nums;
}
.roll-bar {
  height: 6px;
  border-radius: 999px;
  background: var(--kb-gray-pale);
  overflow: hidden;
}
.roll-bar i {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: var(--kb-yellow);
  transition: width 0.25s ease;
}
.loading {
  padding: 40px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
/* 지금 점호할 카드 */
.roll {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 4px;
}
.ci-big {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  background: var(--gray-pale-bg);
  font-size: 32px;
  margin-bottom: 6px;
}
.mer {
  font-size: 17px;
  font-weight: 800;
  color: var(--text-strong);
}
.when {
  font-size: 12px;
  color: var(--text-muted);
}
.price {
  margin-top: 6px;
  font-size: 30px;
  font-weight: 800;
  color: var(--text-strong);
  letter-spacing: -0.02em;
}
.hint {
  margin-top: 8px;
  padding: 7px 12px;
  border-radius: 999px;
  background: var(--gray-pale-bg);
  font-size: 11px;
  color: var(--text-muted);
}
.tag-btns {
  display: flex;
  gap: 10px;
  margin-top: 18px;
}
.tag-btn {
  flex: 1;
  padding: 14px 0;
  border: 1.5px solid var(--line);
  border-radius: 14px;
  background: #fff;
  cursor: pointer;
  font-family: inherit;
  transition: transform 0.1s ease;
}
.tag-btn:active {
  transform: scale(0.97);
}
.tag-btn .e {
  font-size: 26px;
  display: block;
  line-height: 1;
}
.tag-btn .l {
  display: block;
  margin-top: 7px;
  font-size: 14px;
  font-weight: 800;
}
.tag-btn .k {
  display: block;
  margin-top: 3px;
  font-size: 10px;
  font-weight: 600;
  color: var(--text-hint);
}
.tag-btn.sat {
  border-color: var(--sat);
  background: var(--sat-bg);
}
.tag-btn.sat .l {
  color: var(--sat-text);
}
.tag-btn.reg {
  border-color: var(--reg);
  background: var(--reg-bg);
}
.tag-btn.reg .l {
  color: var(--reg-text);
}
.skip-row {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
.skip-row button {
  flex: 1;
  padding: 9px 0;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: transparent;
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
}
.skip-row button:disabled {
  opacity: 0.4;
  cursor: default;
}
.skip-row button.soso {
  color: #c99a1f;
  border-color: var(--soso);
}
/* 방금 점호 요약 */
.done-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
}
.dt {
  font-size: 11px;
  color: var(--text-muted);
}
.pills {
  display: flex;
  gap: 4px;
}
.pills i {
  width: 18px;
  height: 6px;
  border-radius: 999px;
  display: block;
  background: var(--sat);
}
.pills i.r {
  background: var(--reg);
}
.pills i.o {
  background: var(--soso);
}
/* 대기 리스트 */
.wait-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.sec {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-body);
}
.chip {
  font-size: 10px;
  font-weight: 700;
  padding: 3px 9px;
  border-radius: 999px;
  background: var(--kb-gray-pale);
  color: var(--text-muted);
}
.wrow {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-top: 1px solid var(--line);
}
.wrow:first-of-type {
  border-top: 0;
}
.ci {
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  background: var(--gray-pale-bg);
  font-size: 17px;
}
.winfo {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}
.wmer {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-body);
}
.wmeta {
  font-size: 10px;
  color: var(--text-hint);
}
.wamt {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-body);
}
/* 완료 */
.finish {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.fin-e {
  font-size: 40px;
}
.fin-t {
  font-size: 17px;
  font-weight: 800;
  color: var(--text-strong);
}
.fin-s {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 8px;
}
.cta {
  padding: 11px 22px;
  border: 0;
  border-radius: 8px;
  background: var(--kb-yellow);
  color: var(--text-strong);
  font-size: 13px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
}
</style>
