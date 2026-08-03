<script setup>
// SCR-CAR-05 · 자동차 목표 상세  (담당: 호빈)
// 저장된 자동차 로드맵 상세 (스냅샷 포함)
import RoadmapProgress from '@/components/common/RoadmapProgress.vue';

// TODO: 실제 목표 데이터 연동 시 goalId 기준으로 API에서 조회한 값으로 교체
const steps = [
  { label: '목표 설정', status: 'done' },
  { label: '진행 중', status: 'done' },
  { label: '다음 목표 추가', status: 'addable' },
];
const stages = [
  { label: '1단계', active: true },
  { label: '2단계', active: false },
  { label: '3단계', active: false },
];
</script>

<template>
  <div class="container py-4">
    <h2 class="mb-2">자동차 목표 상세</h2>
    <p class="text-muted small">SCR-CAR-05 · <code>/car/goals/:goalId</code></p>

    <RoadmapProgress :steps="steps" :stages="stages" />

    <!-- TODO: 호빈 구현 (스냅샷, 비용 내역 등) -->
  </div>
</template>
