<script setup>
// 공통 컴포넌트: 군적금 여정 타임라인 (담당: 수연)
// 가입 → 오늘(중도해지 갈림길) → 만기 → 만기수령. 실제 현재 시점 기준으로 보여주기만 함.
// 현재 이자·중도해지 수령액·손실액은 전부 details/loss(API 응답)에 있는 값을 그대로 쓴다 (프론트 재계산 금지).
import { computed, ref } from 'vue';
import BaseCard from '@/components/common/BaseCard.vue';
import ToggleSwitch from '@/components/common/ToggleSwitch.vue';
import soldierIcon from '@/assets/images/soldier.png';

const props = defineProps({
  // { monthlySaveTotal, joinableMonths, currentPaidMonths, currentPaidAmount, currentPaidInterest,
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

// 금액 단위 토글: off = 만원(기본), on = 원 단위 전체
const showWon = ref(false);
const won = (v) => `${Math.round(v ?? 0).toLocaleString()}원`;
// 토글 대상 값들(현재이자·중도해지·손실·만기총액·이자)만 이 함수로 감싼다
const amt = (v) => (showWon.value ? won(v) : man(v)); // 정수 만원 or 원
const amtD = (v) => (showWon.value ? won(v) : man1(v)); // 소수 만원 or 원

const d = props.details;
const monthly = d.monthlySaveTotal ?? 0;
const totalMonths = d.joinableMonths ?? 0;
const realMonth = d.currentPaidMonths ?? 0;
const fullInterest = d.expectedInterest ?? 0;
const fullPrincipal = d.expectedPrincipal ?? monthly * totalMonths;
const matching = d.expectedMatchingFund ?? 0;
const maturityTotal = d.totalReceiptAmount ?? 0;

// ── 현재 시점(실제 납입 개월차 고정) ──
const curMonth = realMonth;
const paid = monthly * curMonth;
// 아래 세 값 모두 API가 이미 계산해서 주는 실제 값(회차별 실제 납입일·날짜 기반)을 그대로 쓴다 — 프론트에서 다시 추정하지 않는다.
const curInterest = d.currentPaidInterest ?? 0;
const remainMonths = Math.max(0, totalMonths - curMonth);
const remainAmount = monthly * remainMonths;
// loss는 details와 별도의 API 호출로 채워지고 details보다 늦게 도착할 수 있어(부모의 병렬 fetch),
// 마운트 시점에 고정되는 일반 const 대신 computed로 둬서 나중에 도착해도 반영되게 한다.
const cancelReceive = computed(() => props.loss?.withdrawalAmount ?? 0);

// 중도해지로 실제 깎이는 이자(withdrawalAmount에서 이미 낸 원금을 뺀 몫) — 전부 API 필드 조합.
const withdrawalInterest = computed(() =>
  props.loss ? cancelReceive.value - (d.currentPaidAmount ?? 0) : 0,
);
// "진짜 손실액" = 못 받는 정부매칭지원금 + (만기이자 - 중도해지이자).
// 백엔드 loss.lossAmount는 여기에 "미래에 안 내도 되는 원금"까지 포함돼있어 손실치고 과대평가되므로 그대로 안 쓴다.
const lossTotal = computed(() =>
  props.loss ? matching + (fullInterest - withdrawalInterest.value) : 0,
);
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
        <ToggleSwitch
          v-model="showWon"
          label="원 단위"
          class="saving-tl__unit-toggle"
        />
      </div>
      <div class="saving-tl__product">
        {{ productName
        }}<template v-if="banks.length > 1"> · {{ banks.length }}건</template>
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
              <span class="saving-tl__g-value">{{ amtD(curInterest) }}</span>
            </div>
            <div>
              <span class="saving-tl__g-label">만기까지</span>
              <span class="saving-tl__g-value">{{ remainMonths }}개월</span>
            </div>
          </div>
          <div v-if="loss" class="saving-tl__branch">
            <div class="saving-tl__branch-label">⚠️ 이 시점에 해지하면</div>
            <div class="saving-tl__branch-row">
              <span>중도해지 수령액</span><b>{{ amtD(cancelReceive) }}</b>
            </div>
            <div class="saving-tl__branch-row">
              <span>손실액</span
              ><b class="saving-tl__branch-loss">-{{ amtD(lossTotal) }}</b>
            </div>
          </div>
        </div>

        <!-- 만기 시점 -->
        <div class="saving-tl__node saving-tl__node--goal">
          <span class="saving-tl__dot"></span>
          <div class="saving-tl__date">만기 · {{ maturityLabel }}</div>
          <div class="saving-tl__mat-label">만기 총액</div>
          <div class="saving-tl__title saving-tl__title--goal">
            {{ amt(maturityTotal) }}
          </div>
          <div class="saving-tl__mat-break">
            <span><em>납입</em>{{ man(fullPrincipal) }}</span>
            <span><em>이자</em>{{ amtD(fullInterest) }}</span>
          </div>
          <div class="saving-tl__mat-gov-line">
            <span class="saving-tl__mat-gov"
              ><em>정부지원</em>{{ man(matching) }}</span
            >
          </div>
          <div class="saving-tl__desc">
            지금 시점 기준 남은
            <b>{{ remainMonths }}회차 · {{ man(remainAmount) }}</b> 납입해야
            해요.
          </div>
        </div>
      </div>
    </BaseCard>
  </div>
</template>

<style scoped>
.saving-tl__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 6px;
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
/* 상품명 (제목 아래 왼쪽 줄) */
.saving-tl__product {
  margin: 0 0 12px;
  font-size: 11px;
  font-weight: 700;
  color: var(--text-muted);
}

/* 공용 토글 wrapper가 width:100% + space-between이라 라벨↔토글이 벌어짐 → 내용폭·작은 간격으로 */
.saving-tl__unit-toggle {
  width: auto;
  gap: 6px;
  flex-shrink: 0;
}
.saving-tl__unit-toggle :deep(.toggle-label) {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
}

/* 정부지원 (납입 밑으로 내리고 살짝 크게) */
.saving-tl__mat-gov-line {
  margin-top: 7px;
}
.saving-tl__mat-gov-line .saving-tl__mat-gov {
  font-size: 13px;
  font-weight: 700;
}
.saving-tl__mat-gov-line em {
  margin-right: 4px;
  font-style: normal;
  font-weight: 600;
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
