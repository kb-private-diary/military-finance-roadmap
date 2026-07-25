<script setup>
// SCR-RENT-01 · step1) 자취 목표 등록  (담당: 수연 / 데모)
// 월세 조건 입력 → 지역 선택으로
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import StepIndicator from '@/components/common/StepIndicator.vue';
import CategoryButton from '@/components/common/CategoryButton.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();

const tradeType = ref('MONTHLY');
const estateType = ref('OFFICETEL');
const roomCount = ref('ONE');
const residenceTerm = ref('Y1');
const maxDeposit = ref('');
const maxMonthly = ref('');
const expectedFee = ref('');

const tradeTypes = [{ v: 'MONTHLY', l: '월세' }, { v: 'JEONSE', l: '전세' }];
const estateTypes = [
  { v: 'OFFICETEL', l: '오피스텔' },
  { v: 'VILLA', l: '빌라' },
  { v: 'ONEROOM', l: '원룸' },
  { v: 'APART', l: '아파트' },
];
const roomCounts = [{ v: 'ONE', l: '원룸' }, { v: 'SEP', l: '분리형' }, { v: 'TWO', l: '방 2개+' }];
const terms = [{ v: 'Y1', l: '1년' }, { v: 'Y2', l: '2년' }];

const goNext = () => router.push({ name: 'RentRegionSelect' });
</script>

<template>
  <div class="page">
    <StepIndicator :current="1" :total="4" />

    <h2 class="page__title">어떤 집을 찾고 계세요?</h2>

    <section class="field">
      <p class="field__label">거래유형</p>
      <div class="chips">
        <CategoryButton
          v-for="t in tradeTypes"
          :key="t.v"
          :label="t.l"
          :active="tradeType === t.v"
          @click="tradeType = t.v"
        />
      </div>
    </section>

    <section class="field">
      <p class="field__label">매물종류</p>
      <div class="chips chips--wrap">
        <CategoryButton
          v-for="e in estateTypes"
          :key="e.v"
          :label="e.l"
          :active="estateType === e.v"
          @click="estateType = e.v"
        />
      </div>
    </section>

    <section class="field">
      <BaseInput v-model="maxDeposit" type="number" label="보증금 한도" placeholder="0" suffix="만원" />
    </section>
    <section class="field">
      <BaseInput v-model="maxMonthly" type="number" label="월세 한도" placeholder="0" suffix="만원" />
    </section>

    <section class="field">
      <p class="field__label">방개수</p>
      <div class="chips">
        <CategoryButton
          v-for="r in roomCounts"
          :key="r.v"
          :label="r.l"
          :active="roomCount === r.v"
          @click="roomCount = r.v"
        />
      </div>
    </section>

    <section class="field">
      <p class="field__label">거주기간</p>
      <div class="chips">
        <CategoryButton
          v-for="t in terms"
          :key="t.v"
          :label="t.l"
          :active="residenceTerm === t.v"
          @click="residenceTerm = t.v"
        />
      </div>
    </section>

    <section class="field">
      <BaseInput v-model="expectedFee" type="number" label="예상 관리비" placeholder="0" suffix="만원" />
    </section>
  </div>

  <BottomButtonBar primary-label="다음" @primary-click="goNext" />
</template>

<style scoped>
.page {
  padding: 8px 0 96px;
}
.page__title {
  margin: 0 0 20px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-strong);
}
.field {
  margin-bottom: 20px;
}
.field__label {
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--kb-dark-gray);
}
.chips {
  display: flex;
  gap: 8px;
}
.chips--wrap {
  flex-wrap: wrap;
}
.chips--wrap :deep(.category-btn) {
  flex: 0 1 auto;
}
</style>
