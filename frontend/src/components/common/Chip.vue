<script setup>
// 공통 컴포넌트: 종류 칩 (담당: 수연)
// 네모난 작은 종류 라벨. 차량 연료(가솔린 / EV), 매물 종류(오피스텔·원룸 등)에 사용.
// EV(친환경)는 tone="green" + 나뭇잎 아이콘.
defineProps({
  text: { type: String, required: true },
  tone: { type: String, default: 'gray' }, // 'gray' | 'green'
  icon: { type: String, default: '' }, // 아이콘 src (예: EV 나뭇잎)
});
</script>

<template>
  <span class="chip-tag" :class="`chip-tag--${tone}`">
    <img v-if="icon" class="chip-tag__icon" :src="icon" alt="" />
    {{ text }}
  </span>
</template>

<style scoped>
.chip-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  flex: none;
  padding: 3px 7px;
  border-radius: 5px;
  background: var(--tag-gray-mid);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  line-height: 1;
}
.chip-tag--green {
  background: var(--military-green);
}
.chip-tag__icon {
  width: 12px;
  height: 12px;
  object-fit: contain;
}
</style>
