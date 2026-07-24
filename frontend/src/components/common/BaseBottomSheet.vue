<script setup>
// 화면 아래에서 위로 올라오는 바텀시트. 추가 입력·선택 등에 쓴다.
// 구성: [제목 + 닫기(X)] · [내용 slot] · [아래 버튼]. 내용에 따라 높이가 늘어난다.
defineProps({
  // 열림 여부 (v-model)
  modelValue: { type: Boolean, required: true, default: false },
  // 상단 제목 (비우면 제목 줄 숨김)
  title: { type: String, required: false, default: '' },
  // 주요 버튼 글자
  confirmText: { type: String, required: false, default: '확인' },
  // 취소 버튼 글자 (비우면 확인 버튼만 표시)
  cancelText: { type: String, required: false, default: '' },
  // 우측 상단 닫기(X) 버튼 표시 여부
  showClose: { type: Boolean, required: false, default: true },
});

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel', 'close']);

const close = () => emit('update:modelValue', false);

const onConfirm = () => {
  emit('confirm');
  close();
};

const onCancel = () => {
  emit('cancel');
  close();
};

const onClose = () => {
  emit('close');
  close();
};
</script>

<template>
  <Teleport to="body">
    <Transition name="sheet">
      <div v-if="modelValue" class="sheet-overlay" @click.self="onClose">
        <div class="sheet-panel" role="dialog" aria-modal="true">
          <header class="sheet-header">
            <h3 v-if="title" class="sheet-title">{{ title }}</h3>
            <button
              v-if="showClose"
              type="button"
              class="sheet-close"
              aria-label="닫기"
              @click="onClose"
            >
              ✕
            </button>
          </header>

          <div class="sheet-body">
            <slot />
          </div>

          <div class="sheet-footer">
            <button
              v-if="cancelText"
              type="button"
              class="sheet-btn sheet-btn--cancel"
              @click="onCancel"
            >
              {{ cancelText }}
            </button>
            <button
              type="button"
              class="sheet-btn sheet-btn--confirm"
              @click="onConfirm"
            >
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.sheet-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.sheet-panel {
  width: 100%;
  max-width: 480px;
  max-height: 90vh; /* 화면을 넘으면 내부에서만 스크롤 */
  background-color: #ffffff;
  border-radius: 20px 20px 0 0;
  display: flex;
  flex-direction: column;
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.12);
  box-sizing: border-box;
  overflow: hidden;
}

.sheet-header {
  position: relative;
  padding: 18px 20px;
  text-align: center;
  flex-shrink: 0;
}

.sheet-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #1a1a1a;
}

.sheet-close {
  position: absolute;
  top: 14px;
  right: 16px;
  width: 28px;
  height: 28px;
  border: none;
  background: none;
  font-size: 18px;
  color: #888888;
  cursor: pointer;
  line-height: 1;
}

.sheet-body {
  padding: 8px 20px 20px;
  overflow-y: auto;
}

.sheet-footer {
  display: flex;
  gap: 10px;
  padding: 12px 20px calc(20px + env(safe-area-inset-bottom, 0px)); /* 하단 홈 인디케이터 여백 */
  flex-shrink: 0;
}

.sheet-btn {
  flex: 1;
  padding: 15px 0;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}

.sheet-btn--cancel {
  background-color: #ffffff;
  border: 1px solid #dddddd;
  color: #333333;
}

.sheet-btn--confirm {
  background-color: #ffcc00;
  border: none;
  color: #1a1a1a;
}

.sheet-enter-active,
.sheet-leave-active {
  transition: opacity 0.25s ease;
}
.sheet-enter-from,
.sheet-leave-to {
  opacity: 0;
}
.sheet-enter-active .sheet-panel,
.sheet-leave-active .sheet-panel {
  transition: transform 0.25s ease;
}
.sheet-enter-from .sheet-panel,
.sheet-leave-to .sheet-panel {
  transform: translateY(100%);
}
</style>
