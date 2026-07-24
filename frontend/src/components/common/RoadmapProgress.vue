<script setup>
// 공통 컴포넌트: 로드맵 진행도 (담당: 호빈)
// 상단 - 완료/추가/예정 단계를 잇는 체크·+ 스텝 표시줄
// 하단 - 단계(목표)별 이동 버튼 목록. 현재 진행 중인 단계만 강조된다.
const props = defineProps({
  steps: {
    // status: 'done' | 'addable' | 'upcoming'
    type: Array, // [{ label, status }]
    default: () => [],
  },
  stages: {
    type: Array, // [{ label, active }]
    default: () => [],
  },
});

const emit = defineEmits(['add-step', 'select-stage']);
</script>

<template>
  <div class="roadmap-progress">
    <ol class="roadmap-progress__steps list-unstyled">
      <li
        v-for="(step, index) in steps"
        :key="index"
        class="roadmap-progress__step"
        :class="`is-${step.status}`"
      >
        <button
          v-if="step.status === 'addable'"
          type="button"
          class="roadmap-progress__marker"
          :aria-label="step.label || '다음 단계 추가'"
          @click="emit('add-step', index)"
        >
          +
        </button>
        <span v-else class="roadmap-progress__marker" :aria-label="step.label">
          <template v-if="step.status === 'done'">✓</template>
        </span>

        <span v-if="step.label" class="roadmap-progress__label">{{ step.label }}</span>

        <span
          v-if="index < steps.length - 1"
          class="roadmap-progress__connector"
          :class="{ 'is-filled': step.status === 'done' }"
        />
      </li>
    </ol>

    <div class="roadmap-progress__stages">
      <button
        v-for="(stage, index) in stages"
        :key="index"
        type="button"
        class="roadmap-progress__stage-btn"
        :class="{ 'is-active': stage.active }"
        @click="emit('select-stage', index)"
      >
        {{ stage.label }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.roadmap-progress__steps {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.roadmap-progress__step {
  display: flex;
  align-items: center;
  flex: 1 1 auto;
}

.roadmap-progress__step:last-child {
  flex: 0 0 auto;
}

.roadmap-progress__marker {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  border: 2px solid var(--bs-border-color);
  background-color: #fff;
  color: var(--bs-secondary-color);
  font-weight: 700;
  flex-shrink: 0;
  padding: 0;
}

.roadmap-progress__step.is-done .roadmap-progress__marker {
  border-color: #f0ad2e;
  background-color: #f0ad2e;
  color: #fff;
}

.roadmap-progress__step.is-addable .roadmap-progress__marker {
  border-color: #f0ad2e;
  color: #f0ad2e;
  cursor: pointer;
}

.roadmap-progress__step.is-addable .roadmap-progress__marker:hover {
  background-color: #fff8e6;
}

.roadmap-progress__label {
  margin-left: 0.4rem;
  margin-right: 0.4rem;
  font-size: 0.85rem;
  color: var(--bs-secondary-color);
  white-space: nowrap;
}

.roadmap-progress__connector {
  flex: 1 1 auto;
  height: 2px;
  background-color: var(--bs-border-color);
  margin: 0 0.25rem;
}

.roadmap-progress__connector.is-filled {
  background-color: #f0ad2e;
}

.roadmap-progress__stages {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
  gap: 0.5rem;
}

.roadmap-progress__stage-btn {
  border: none;
  border-radius: 0.5rem;
  padding: 0.6rem 0.5rem;
  background-color: #f5e9cf;
  color: #a9895a;
  font-weight: 600;
  transition: background-color 0.15s ease, color 0.15s ease;
}

.roadmap-progress__stage-btn.is-active {
  background-color: #f0ad2e;
  color: #fff;
}
</style>
