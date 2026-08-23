<script setup>
// 공통 컴포넌트: 상태 뱃지 (담당: 수연)
// 아이콘 + 짧은 라벨의 알약형 뱃지. tone으로 배경/글자색 결정.
// 예산 상태(딱 맞아요/빠듯/초과), 교통(도보/버스/지하철), 시세 비교(저렴/평균/비쌈) 등.
defineProps({
  text: { type: String, required: true },
  icon: { type: String, default: '' }, // 아이콘 이미지 src
  iconSize: { type: Number, default: 12 }, // 아이콘 크기(px) — 아이콘별 시각 보정
  // 'green' | 'yellow' | 'red' | 'blue' | 'cream'
  tone: { type: String, default: 'green' },
});
</script>

<template>
  <span class="badge" :class="`badge--${tone}`">
    <img
      v-if="icon"
      class="badge__icon"
      :src="icon"
      :style="{ width: `${iconSize}px`, height: `${iconSize}px` }"
      alt=""
    />
    <span class="badge__text">{{ text }}</span>
  </span>
</template>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 9px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 600;
  white-space: nowrap;
  color: #2b2b2b; /* 글자는 검정 계열 (tone 공통) */
}
.badge__icon {
  flex: none;
  width: 16px;
  height: 16px;
  object-fit: contain;
}
/* tone — 배경색만 (파스텔 톤으로 보정: 노랑 순도 낮추고 전반 통일) */
.badge--green {
  background: #e4f4e2;
}
.badge--blue {
  background: #dbe9ff;
}
.badge--yellow {
  background: #f9f2b6;
}
.badge--cream {
  background: #ffe9d2;
}
.badge--red {
  background: #f7d9d9;
}
</style>
