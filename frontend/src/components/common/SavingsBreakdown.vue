<!--
  SavingsBreakdown - 군적금 수령액 구성비(납입원금·이자·정부매칭지원금) 시각화 공통 컴포넌트

  SimulatorPage 안에서 실제 계좌 기준 상세내역과 모의 계산(바텀시트) 결과가
  동일한 3색 막대 + 범례 + 총액 UI를 쓰기 때문에 공통 컴포넌트로 분리했다.

  사용법:
    <SavingsBreakdown
      :principal="9350000"
      :interest="350625"
      :matching-fund="9350000"
      :total="19050625"
    />
-->
<script setup>
defineProps({
  principal: { type: Number, required: true, default: 0 },
  interest: { type: Number, required: true, default: 0 },
  matchingFund: { type: Number, required: true, default: 0 },
  total: { type: Number, required: true, default: 0 },
  interestLabel: { type: String, required: false, default: '이자 (연 5.0%)' },
});

const formatWon = (amount) => `${(amount ?? 0).toLocaleString('ko-KR')}원`;
</script>

<template>
  <div class="savings-breakdown">
    <div class="savings-breakdown__bar">
      <div
        class="savings-breakdown__segment savings-breakdown__segment--principal"
        :style="{ flexGrow: principal }"
      />
      <div
        class="savings-breakdown__segment savings-breakdown__segment--interest"
        :style="{ flexGrow: interest }"
      />
      <div
        class="savings-breakdown__segment savings-breakdown__segment--matching"
        :style="{ flexGrow: matchingFund }"
      />
    </div>

    <ul class="savings-breakdown__legend">
      <li class="savings-breakdown__legend-row">
        <span
          class="savings-breakdown__dot savings-breakdown__dot--principal"
        />
        <span class="savings-breakdown__label">납입 원금</span>
        <span class="savings-breakdown__value">{{ formatWon(principal) }}</span>
      </li>
      <li class="savings-breakdown__legend-row">
        <span class="savings-breakdown__dot savings-breakdown__dot--interest" />
        <span class="savings-breakdown__label">{{ interestLabel }}</span>
        <span class="savings-breakdown__value">{{ formatWon(interest) }}</span>
      </li>
      <li class="savings-breakdown__legend-row">
        <span class="savings-breakdown__dot savings-breakdown__dot--matching" />
        <span class="savings-breakdown__label">정부 매칭지원금</span>
        <span class="savings-breakdown__value">{{
          formatWon(matchingFund)
        }}</span>
      </li>
    </ul>

    <div class="savings-breakdown__total">
      <span class="savings-breakdown__total-label">총 수령액</span>
      <span class="savings-breakdown__total-value">{{ formatWon(total) }}</span>
    </div>
  </div>
</template>

<style scoped>
.savings-breakdown {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.savings-breakdown__bar {
  display: flex;
  height: 12px;
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--kb-gray-pale, #e8e8e8);
}

.savings-breakdown__segment {
  flex-shrink: 0;
  flex-basis: 0;
}

.savings-breakdown__segment--principal {
  background-color: #f2a56d;
}

.savings-breakdown__segment--interest {
  background-color: #bfe3a0;
}

.savings-breakdown__segment--matching {
  background-color: #aedff5;
}

.savings-breakdown__legend {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.savings-breakdown__legend-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.savings-breakdown__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.savings-breakdown__dot--principal {
  background-color: #f2a56d;
}

.savings-breakdown__dot--interest {
  background-color: #bfe3a0;
}

.savings-breakdown__dot--matching {
  background-color: #aedff5;
}

.savings-breakdown__label {
  flex: 1;
  font-size: 13px;
  color: var(--text-body, #545045);
}

.savings-breakdown__value {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong, #000000);
}

.savings-breakdown__total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 14px;
  border-top: 1px solid var(--line, #e0e0e0);
}

.savings-breakdown__total-label {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong, #000000);
}

.savings-breakdown__total-value {
  font-size: 18px;
  font-weight: 800;
  color: #a9895a;
}
</style>
