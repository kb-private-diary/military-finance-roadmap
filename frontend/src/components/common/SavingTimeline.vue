<script setup>
// 공통 컴포넌트: 군적금 여정 타임라인 (담당: 수연)
// 가입 → 오늘(중도해지 갈림길) → 만기 → 만기수령. 실제 현재 시점 기준으로 보여주기만 함.
// 계산식은 details/savingLoss에서 파생(적금 단리 가정). 실제 공식이 있으면 interestAt만 교체하면 됨.
import BaseCard from '@/components/common/BaseCard.vue';
import soldierIcon from '@/assets/images/soldier.png';

const props = defineProps({
  // { monthlySaveTotal, joinableMonths, currentPaidMonths, currentPaidAmount,
  //   expectedPrincipal, expectedMatchingFund, expectedInterest, totalReceiptAmount } (원 단위)
  details: { type: Object, required: true },
  // { withdrawalAmount, lossAmount } — 없으면 갈림길/손실 카드 숨김
  loss: { type: Object, default: null },
  // 상품 종류명 (장병내일준비적금은 최대 2개 은행까지 동시 가입 가능)
  productName: { type: String, default: '장병내일준비적금' },
  // 가입한 은행별 적금 목록 (1~2개). monthly 합계 = details.monthlySaveTotal.
  // 예) KB 40만원 + IBK 15만원 = 월 55만원 (장병적금 월 납입 한도)
  banks: {
    type: Array,
    default: () => [
      { name: 'KB국민은행', monthly: 400000 },
      { name: 'IBK기업은행', monthly: 150000 },
    ],
  },
});

const man = (won) => `${Math.round((won ?? 0) / 10000).toLocaleString()}만원`;
const man1 = (won) =>
  `${(Math.round((won ?? 0) / 1000) / 10).toLocaleString()}만원`; // 소수 1자리

const d = props.details;
const monthly = d.monthlySaveTotal ?? 0;
const totalMonths = d.joinableMonths ?? 0;
const realMonth = d.currentPaidMonths ?? 0;
const fullInterest = d.expectedInterest ?? 0;
const fullPrincipal = d.expectedPrincipal ?? monthly * totalMonths;
const matching = d.expectedMatchingFund ?? 0;
const maturityTotal = d.totalReceiptAmount ?? 0;

// 적금 단리: 이자 = 월납입 × (m(m+1)/2) × 연이율/12  → 이율을 실제값에서 역산
const triangular = (m) => (m * (m + 1)) / 2;
const rate =
  monthly && totalMonths
    ? (fullInterest * 12) / (monthly * triangular(totalMonths))
    : 0;
// 중도해지 이율: 실제 현재의 중도해지 수령액에서 역산
const cancelRate = (() => {
  if (!props.loss || !monthly || !realMonth) return 0;
  const cancelInt = (props.loss.withdrawalAmount ?? 0) - monthly * realMonth;
  return Math.max(0, (cancelInt * 12) / (monthly * triangular(realMonth)));
})();
const interestAt = (m, r) => monthly * triangular(m) * (r / 12);

// ── 현재 시점(실제 납입 개월차 고정) ──
const curMonth = realMonth;
const paid = monthly * curMonth;
const curInterest = interestAt(curMonth, rate);
const remainMonths = Math.max(0, totalMonths - curMonth);
const remainAmount = monthly * remainMonths;
const cancelInterest = interestAt(curMonth, cancelRate);
const cancelReceive = paid + cancelInterest;
const lostInterest = Math.max(0, fullInterest - cancelInterest);
const fillPct = totalMonths
  ? Math.min(100, Math.round((curMonth / totalMonths) * 100))
  : 0;

// 시점별 대략 날짜 (가입일 = 오늘 − 실제개월차 가정). "YYYY.MM"
const today = new Date();
const ymLabel = (offsetMonths) => {
  const dt = new Date(today.getFullYear(), today.getMonth() + offsetMonths, 1);
  return `${dt.getFullYear()}.${String(dt.getMonth() + 1).padStart(2, '0')}`;
};
const joinLabel = ymLabel(-realMonth);
const curDateLabel = ymLabel(0);
const maturityLabel = ymLabel(-realMonth + totalMonths);
</script>

<template>
  <div class="saving-tl">
    <BaseCard padding="18px 16px">
      <div class="saving-tl__head">
        <b>내 적금 여정</b>
        <span
          >{{ productName
          }}<template v-if="banks.length > 1"> · {{ banks.length }}건</template></span
        >
      </div>
      <div v-if="banks.length" class="saving-tl__banks">
        <span v-for="b in banks" :key="b.name" class="saving-tl__bank">
          {{ b.name }} <b>{{ man(b.monthly) }}</b>
        </span>
      </div>

      <div class="saving-tl__line">
        <div class="saving-tl__fill" :style="{ height: `${fillPct}%` }"></div>

        <!-- 가입 -->
        <div class="saving-tl__node saving-tl__node--done">
          <span class="saving-tl__dot"></span>
          <div class="saving-tl__date">가입 · {{ joinLabel }}</div>
          <div class="saving-tl__grid">
            <div>
              <span class="saving-tl__g-label">월 납입액</span>
              <span class="saving-tl__g-value">{{ man(monthly) }}</span>
            </div>
            <div>
              <span class="saving-tl__g-label">약정 기간</span>
              <span class="saving-tl__g-value">{{ totalMonths }}개월</span>
            </div>
          </div>
        </div>

        <!-- 오늘(이동) -->
        <div class="saving-tl__node saving-tl__node--now">
          <span class="saving-tl__dot saving-tl__dot--soldier">
            <img :src="soldierIcon" alt="" />
          </span>
          <div class="saving-tl__date">
            오늘 · {{ curDateLabel }} · {{ curMonth }}개월차
          </div>
          <div class="saving-tl__grid saving-tl__grid--3">
            <div>
              <span class="saving-tl__g-label">누적 납입액</span>
              <span class="saving-tl__g-value">{{ man(paid) }}</span>
            </div>
            <div>
              <span class="saving-tl__g-label">현재 이자</span>
              <span class="saving-tl__g-value">{{ man1(curInterest) }}</span>
            </div>
            <div>
              <span class="saving-tl__g-label">만기까지</span>
              <span class="saving-tl__g-value">{{ remainMonths }}개월</span>
            </div>
          </div>
          <div v-if="loss" class="saving-tl__branch">
            <div class="saving-tl__branch-label">⚠️ 이 시점에 해지하면</div>
            <div class="saving-tl__branch-row">
              <span>중도해지 수령액</span><b>{{ man1(cancelReceive) }}</b>
            </div>
            <div class="saving-tl__branch-row">
              <span>손해보는 이자</span
              ><b class="saving-tl__branch-loss">-{{ man1(lostInterest) }}</b>
            </div>
          </div>
        </div>

        <!-- 만기 시점 -->
        <div class="saving-tl__node saving-tl__node--goal">
          <span class="saving-tl__dot"></span>
          <div class="saving-tl__date">만기 · {{ maturityLabel }}</div>
          <div class="saving-tl__mat-label">만기 총액</div>
          <div class="saving-tl__title saving-tl__title--goal">
            {{ man(maturityTotal) }}
          </div>
          <div class="saving-tl__mat-break">
            <span><em>납입</em>{{ man(fullPrincipal) }}</span>
            <span><em>이자</em>{{ man1(fullInterest) }}</span>
            <span class="saving-tl__mat-gov"><em>정부지원</em>{{ man(matching) }}</span>
          </div>
          <div class="saving-tl__desc">
            지금 시점 기준 남은
            <b>{{ remainMonths }}회차 · {{ man(remainAmount) }}</b> 납입해야 해요.
          </div>
        </div>
      </div>
    </BaseCard>
  </div>
</template>

<style scoped>
.saving-tl__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 8px;
}
/* 가입 은행별 적금 (장병적금 최대 2건) */
.saving-tl__banks {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 14px;
}
.saving-tl__bank {
  padding: 3px 9px;
  border-radius: 999px;
  background: var(--camo-green-tint);
  font-size: 10.5px;
  font-weight: 600;
  color: var(--camo-green);
}
.saving-tl__bank b {
  margin-left: 3px;
  font-weight: 700;
  color: var(--camo-forest);
}
.saving-tl__bank:nth-child(2) {
  background: var(--camo-sand-tint);
  color: #6a5a24;
}
.saving-tl__bank:nth-child(2) b {
  color: var(--camo-rust);
}
.saving-tl__head b {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-strong);
}
.saving-tl__head span {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-muted);
}
/* 타임라인 */
.saving-tl__line {
  position: relative;
  padding-left: 38px;
}
.saving-tl__line::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 6px;
  bottom: 14px;
  width: 2px;
  background: var(--divider-thin);
}
.saving-tl__fill {
  position: absolute;
  left: 8px;
  top: 6px;
  width: 2px;
  background: var(--military-green);
  transition: height 0.25s ease;
}
.saving-tl__node {
  position: relative;
  padding-bottom: 20px;
}
.saving-tl__node:last-child {
  padding-bottom: 0;
}
.saving-tl__dot {
  position: absolute;
  left: -36px;
  top: 2px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #fff;
  border: 2.5px solid var(--divider-thin);
  z-index: 1;
}
.saving-tl__node--done .saving-tl__dot {
  background: var(--camo-green);
  border-color: var(--camo-green);
}
.saving-tl__node--now .saving-tl__dot {
  background: var(--kb-dark-gray);
  border-color: var(--kb-dark-gray);
  box-shadow: 0 0 0 4px rgba(84, 80, 69, 0.13);
}
/* 이동한 시점 = 군인 프로필 원 */
.saving-tl__node--now .saving-tl__dot--soldier {
  width: 28px;
  height: 28px;
  left: -41px;
  top: -3px;
  background: var(--camo-sand);
  border: 2px solid #fff;
  overflow: hidden;
  box-shadow: 0 0 0 3px rgba(83, 99, 73, 0.22);
}
.saving-tl__dot--soldier img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.saving-tl__node--goal .saving-tl__dot {
  background: var(--military-green);
  border-color: var(--military-green);
}
.saving-tl__date {
  font-size: 10.5px;
  font-weight: 700;
  color: var(--text-muted);
}
.saving-tl__title {
  margin-top: 3px;
  font-size: 13.5px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--text-strong);
}
.saving-tl__node--now .saving-tl__title {
  color: var(--kb-dark-gray);
}
.saving-tl__title--goal {
  font-size: 22px;
  color: var(--military-green);
}
.saving-tl__mat-label {
  margin-top: 2px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted);
}
.saving-tl__mat-break {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 14px;
  margin-top: 8px;
}
.saving-tl__mat-break span {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-strong);
}
.saving-tl__mat-break em {
  margin-right: 4px;
  font-style: normal;
  font-weight: 600;
  color: var(--text-muted);
}
.saving-tl__mat-gov,
.saving-tl__mat-gov em {
  color: var(--camo-olive) !important;
}
.saving-tl__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-top: 8px;
}
.saving-tl__g-label {
  display: block;
  font-size: 10.5px;
  color: var(--text-muted);
}
.saving-tl__g-value {
  display: block;
  margin-top: 1px;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
}
.saving-tl__desc {
  margin-top: 4px;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.5;
  color: var(--text-body);
}
.saving-tl__desc b {
  font-weight: 700;
  color: var(--text-strong);
}
.saving-tl__branch {
  margin-top: 10px;
  padding: 10px 11px;
  border-radius: 10px;
  background: var(--camo-rust-tint);
}
.saving-tl__branch-label {
  margin-bottom: 6px;
  font-size: 10px;
  font-weight: 700;
  color: var(--camo-rust);
}
.saving-tl__branch-row {
  display: flex;
  justify-content: space-between;
  font-size: 11.5px;
  color: #5c3620;
}
.saving-tl__branch-row + .saving-tl__branch-row {
  margin-top: 4px;
}
.saving-tl__branch-row b {
  font-weight: 700;
  color: var(--text-strong);
}
.saving-tl__branch-loss {
  color: var(--camo-rust) !important;
}
</style>
