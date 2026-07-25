<script setup>
// 로드맵 상단 진행도 — 곰돌이 캐릭터 슬라이더(RoadmapCharacterSlider) 재사용.
// 현재 STEP / 전체 STEP 을 진행률(%)로 바꿔 넘긴다. 스텝마다 곰돌이 포즈가 바뀐다.
//   step1 25%(누움) · step2 50%(앉음) · step3 75%(뜀) · step4 100%(경례)
import { computed } from 'vue';
import RoadmapCharacterSlider from './RoadmapCharacterSlider.vue';

const props = defineProps({
  current: { type: Number, required: true, default: 1 }, // 현재 STEP (1-based)
  total: { type: Number, required: false, default: 4 },  // 전체 STEP 수
});

const progress = computed(() => Math.round((props.current / props.total) * 100));
</script>

<template>
  <RoadmapCharacterSlider :progress="progress" />
</template>
