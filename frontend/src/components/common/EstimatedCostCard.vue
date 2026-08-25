<script setup>
// 공통 컴포넌트: 예상 비용 내역 카드 (담당: 수연)
// "금액(헤더) + 물통(비율 바) + 리스트" 단일 상태 카드. 탭 전환은 부모가 카드 밖에서 처리.
// 물통 층 높이 = 항목 비율(%). 10% 미만이면 내부 텍스트 숨기고 리스트에서 읽는다.
defineProps({
  // {
  //   headLabel, amount(number), unit('만원'), range?, subNote?,
  //   items:[{label,sub,amountText,percent,color,ink,showName}],
  //   nested?:[{label,value}], hint?, hintTone('g'|'y'), hintIcon?
  // }
  state: { type: Object, required: true },
  // 있으면 금액 헤더 대신 좌측 정렬 제목바를 표시 (예: '세부 비용 내역')
  title: { type: String, default: '' },
  // 카드 안 구분선(제목바 밑줄·항목 사이 선) 표시 여부
  dividers: { type: Boolean, default: true },
  // 카드 하단 안내 문구
  note: {
    type: String,
    default: '실제 비용은 조건에 따라 달라질 수 있어요.',
  },
});
</script>

<template>
  <section class="ecost" :class="{ 'ecost--flat': !dividers }">
    <slot name="head">
      <div v-if="title" class="ecost__titlebar">{{ title }}</div>
      <div v-else class="ecost__headamt">
        <div class="ecost__hl">{{ state.headLabel }}</div>
        <div class="ecost__hv">
          {{ state.amount.toLocaleString() }}<em>{{ state.unit || '만원' }}</em>
        </div>
        <div v-if="state.range || state.subNote" class="ecost__hr">
          {{ state.range || state.subNote }}
        </div>
      </div>
    </slot>

    <div class="ecost__body">
      <div class="ecost__tankrow">
        <div class="ecost__tk">
          <i
            v-for="(s, i) in state.items"
            :key="i"
            :style="{ height: `${s.percent}%`, background: s.color, color: s.ink }"
          >
            <b v-if="s.percent >= 10">{{ s.percent }}%</b>
            <span v-if="s.showName">{{ s.label }}</span>
          </i>
        </div>
        <div class="ecost__ilist">
          <div v-for="(s, i) in state.items" :key="i" class="ecost__itm">
            <span class="ecost__dt" :style="{ background: s.color }"></span>
            <span class="ecost__nm"
              >{{ s.label }}<em v-if="s.sub">{{ s.sub }}</em></span
            >
            <span class="ecost__amt">{{ s.amountText }}</span>
            <span class="ecost__pc">{{ s.percent }}%</span>
          </div>
        </div>
      </div>

      <div v-if="state.nested && state.nested.length" class="ecost__subin">
        <div v-for="(n, i) in state.nested" :key="i" class="ecost__s">
          <span class="ecost__ln">└</span>
          <span class="ecost__sn">{{ n.label }}</span>
          <span class="ecost__sv">{{ n.value }}</span>
        </div>
      </div>

      <div
        v-if="state.hint"
        class="ecost__hint"
        :class="state.hintTone || 'g'"
      >
        <span aria-hidden="true">{{ state.hintIcon || '💡' }}</span>
        <span>{{ state.hint }}</span>
      </div>
    </div>

    <div v-if="note" class="ecost__note">💡 {{ note }}</div>

    <slot name="footer" />
  </section>
</template>

<style scoped>
.ecost {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #ececec;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}
/* 좌측 제목바 (세부 비용 내역 등) */
.ecost__titlebar {
  padding: 15px 16px;
  border-bottom: 1px solid var(--divider-thin);
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
}
/* 선 없는 카드 (dividers=false) — 항목 사이 선·들여쓰기 선 제거, 헤더 선은 연하게 */
.ecost--flat .ecost__titlebar {
  border-bottom: none;
}
.ecost--flat .ecost__headamt {
  border-bottom-color: #e7e9ec;
}
.ecost--flat .ecost__itm + .ecost__itm {
  border-top: none;
}
.ecost--flat .ecost__subin {
  border-top: none;
}
/* 금액 헤더 */
.ecost__headamt {
  padding: 18px 16px 16px;
  text-align: center;
  border-bottom: 1px solid var(--divider-thin);
}
.ecost__hl {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-muted);
}
.ecost__hv {
  margin-top: 6px;
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
  color: var(--text-strong);
}
.ecost__hv em {
  margin-left: 3px;
  font-style: normal;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-body);
}
.ecost__hr {
  margin-top: 7px;
  font-size: 11px;
  font-weight: 700;
  color: var(--text-muted);
}
.ecost__body {
  padding: 16px;
}
/* 물통 + 리스트 */
.ecost__tankrow {
  display: flex;
  gap: 14px;
  align-items: stretch;
}
.ecost__tk {
  width: 78px;
  flex: none;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}
.ecost__tk i {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0;
  width: 100%;
  min-height: 0;
  padding: 0 2px;
  overflow: hidden;
  font-style: normal;
  line-height: 1;
}
.ecost__tk i b {
  font-size: 11px;
  font-weight: 800;
  line-height: 1.15;
}
.ecost__tk i span {
  max-width: 100%;
  overflow: hidden;
  font-size: 8px;
  font-weight: 700;
  line-height: 1.15;
  opacity: 0.78;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ecost__ilist {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.ecost__itm {
  display: flex;
  flex: 1;
  align-items: center;
  gap: 9px;
  min-height: 0;
  padding: 10px 0;
}
.ecost__itm + .ecost__itm {
  border-top: 1px solid var(--divider-thin);
}
.ecost__dt {
  flex: none;
  width: 10px;
  height: 10px;
  border-radius: 3px;
}
.ecost__nm {
  flex: 1;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-strong);
}
.ecost__nm em {
  display: block;
  margin-top: 3px;
  font-style: normal;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted);
}
.ecost__amt {
  flex: none;
  font-size: 14px;
  font-weight: 800;
  color: var(--text-strong);
}
.ecost__pc {
  flex: none;
  width: 34px;
  text-align: right;
  font-size: 12px;
  font-weight: 800;
  color: var(--kb-yellow-deep);
}
/* 들여쓰기 세부 */
.ecost__subin {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
  padding: 12px 0 0;
  border-top: 1px dashed var(--divider-thin);
}
.ecost__s {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: 6px;
  font-size: 12px;
}
.ecost__ln {
  flex: none;
  color: var(--text-muted);
  font-weight: 700;
}
.ecost__sn {
  flex: 1;
  color: var(--text-body);
  font-weight: 600;
}
.ecost__sv {
  font-weight: 800;
  color: var(--text-body);
}
/* 힌트 */
.ecost__hint {
  display: flex;
  gap: 8px;
  margin-top: 13px;
  padding: 12px 13px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.6;
}
.ecost__hint.g {
  background: var(--bg-gray);
  color: var(--text-body);
}
.ecost__hint.y {
  background: #fff9e0;
  border: 1px solid #ffe9a8;
  color: #a9762a;
}
.ecost__note {
  padding: 13px 3px 0;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.55;
  text-align: center;
  color: var(--text-muted);
}
</style>
