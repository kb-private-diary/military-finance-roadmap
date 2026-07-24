<script setup>
// 화면 중앙에 뜨는 알림/확인 모달. v-model 로 열고 닫는다.
// 내용은 slot 으로 채우며 높이는 내용에 따라 자동으로 늘어난다.
defineProps({
  // 열림 여부 (v-model)
  modelValue: { type: Boolean, required: true, default: false },
  // 상단 제목 (비우면 제목 줄 숨김)
  title: { type: String, required: false, default: '' },
  // 확인 버튼 글자
  confirmText: { type: String, required: false, default: '확인' },
  // 취소 버튼 글자 (비우면 확인 버튼만 표시)
  cancelText: { type: String, required: false, default: '' },
  // 확인 버튼 위험(빨강) 강조
  danger: { type: Boolean, required: false, default: false },
});

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel']);

const close = () => emit('update:modelValue', false);

const onConfirm = () => {
  emit('confirm');
  close();
};

const onCancel = () => {
  emit('cancel');
  close();
};
</script>

<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="modelValue" class="modal-overlay" @click.self="onCancel">
        <div class="modal-box" role="dialog" aria-modal="true">
          <div class="modal-content">
            <h3 v-if="title" class="modal-title">{{ title }}</h3>
            <div class="modal-body">
              <slot />
            </div>
          </div>

          <div class="modal-footer">
            <button
              v-if="cancelText"
              type="button"
              class="modal-btn modal-btn--cancel"
              @click="onCancel"
            >
              {{ cancelText }}
            </button>
            <button
              type="button"
              class="modal-btn modal-btn--confirm"
              :class="{ 'modal-btn--danger': danger }"
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
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 1000;
}

.modal-box {
  width: 100%;
  max-width: 320px;
  background-color: #ffffff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.18);
  box-sizing: border-box;
}

.modal-content {
  padding: 24px 20px 20px;
}

.modal-title {
  margin: 0 0 10px;
  font-size: 17px;
  font-weight: 700;
  color: #1a1a1a;
}

.modal-body {
  font-size: 14px;
  line-height: 1.6;
  color: #555555;
}

.modal-footer {
  display: flex;
  border-top: 1px solid #eeeeee;
}

.modal-btn {
  flex: 1;
  padding: 16px 0;
  border: none;
  background: none;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}

.modal-btn--cancel {
  background-color: #f2f2f4;
  color: #444444;
}

.modal-btn--confirm {
  background-color: #ffcc00;
  color: #1a1a1a;
}

.modal-btn--danger {
  background-color: #ff5b5b;
  color: #ffffff;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
