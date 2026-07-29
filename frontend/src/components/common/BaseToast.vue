<script setup>
// 공통 토스트 표시기. App.vue 에 딱 한 번 <BaseToast /> 로 마운트한다.
// 띄우는 건 어디서든 useToast().show('메시지'). 이 컴포넌트는 그 큐를 그려주기만 함.
import { useToast } from '@/composables/useToast';

const { toasts } = useToast();
</script>

<template>
  <transition-group name="toast" tag="div" class="toast-host">
    <div
      v-for="toast in toasts"
      :key="toast.id"
      class="toast"
      :class="`toast--${toast.type}`"
    >
      {{ toast.message }}
    </div>
  </transition-group>
</template>

<style scoped>
/* 393px 프레임 하단 중앙에 쌓이도록. 탭바/버튼바 위로 살짝 띄운다. */
.toast-host {
  position: fixed;
  bottom: 88px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: 100%;
  max-width: 393px;
  padding: 0 16px;
  z-index: 1000;
  pointer-events: none;
}
.toast {
  max-width: 100%;
  padding: 12px 18px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
  background-color: rgba(0, 0, 0, 0.82);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  word-break: keep-all;
}
.toast--success {
  background-color: var(--kb-yellow-deep);
  color: var(--text-strong);
}
.toast--error {
  background-color: var(--danger);
}

/* 나타날 때 아래에서 살짝 올라오고, 사라질 때 페이드 */
.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
