<!--
  LikeButton - 관심 등록(하트) 토글 공통 컴포넌트

  사용법 (v-model로 바인딩):
    <script setup>
    import { ref } from 'vue';
    const isLiked = ref(false);
    </script>

    <LikeButton v-model="isLiked" />

  클릭할 때마다 isLiked 값이 true/false로 토글되고, 하트 모양도 같이 바뀝니다.
  (빈 하트=미등록/검정 테두리, 채워진 하트=등록됨/빨간색)
  실제 화면에서는 카드마다 각자의 관심 등록 상태(ref 또는 서버 데이터)를 넘겨서 사용하세요.
-->
<script setup>
// Props: 현재 관심 등록 여부 (컨벤션: camelCase, 타입/필수/기본값 명시)
const props = defineProps({
  modelValue: { type: Boolean, required: false, default: false },
});

// v-model 지원 (부모에서 <LikeButton v-model="isLiked" /> 형태로 사용)
const emit = defineEmits(['update:modelValue']);

const toggleLike = () => {
  emit('update:modelValue', !props.modelValue);
};
</script>

<template>
  <button
    class="like-button"
    type="button"
    :aria-pressed="modelValue"
    aria-label="관심 등록"
    @click="toggleLike"
  >
    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        d="M12 20.5C12 20.5 3 15.2 3 9.2C3 6.3 5.3 4 8.2 4C9.8 4 11.2 4.8 12 6C12.8 4.8 14.2 4 15.8 4C18.7 4 21 6.3 21 9.2C21 15.2 12 20.5 12 20.5Z"
        :fill="modelValue ? '#ef4444' : 'none'"
        :stroke="modelValue ? '#ef4444' : '#1b1b1b'"
        stroke-width="1.8"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </svg>
  </button>
</template>

<style scoped>
.like-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  background: none;
  border: none;
  cursor: pointer;
}

.like-button svg {
  width: 20px;
  height: 20px;
  transition: transform 0.15s ease;
}

.like-button:active svg {
  transform: scale(0.85);
}
</style>
