<script setup>
// SCR-COM-06 · 아이디 찾기 (담당: 호빈)
// 이름·전화번호로 아이디 찾기 (마스킹된 아이디 반환)
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import memberApi from '@/api/memberApi';
import BaseInput from '@/components/common/BaseInput.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();

const form = reactive({
  name: '',
  phone: '',
});

const submitting = ref(false);
const errorMessage = ref('');
const maskedUserId = ref('');
const copied = ref(false);

const canSubmit = computed(() => form.name && form.phone && !submitting.value);

const find = async () => {
  errorMessage.value = '';
  maskedUserId.value = '';
  submitting.value = true;
  try {
    const result = await memberApi.findUserId({ name: form.name, phone: form.phone });
    maskedUserId.value = result.maskedUserId;
  } catch (e) {
    errorMessage.value = e.response?.data?.message || '아이디를 찾을 수 없습니다.';
  } finally {
    submitting.value = false;
  }
};

// 찾은 아이디 복사 (잠깐 체크 아이콘으로 피드백)
const copyId = async () => {
  if (!maskedUserId.value) return;
  try {
    await navigator.clipboard.writeText(maskedUserId.value);
    copied.value = true;
    setTimeout(() => (copied.value = false), 1500);
  } catch (e) {
    // 클립보드 미지원 환경은 무시
  }
};
</script>

<template>
  <div class="find-id-page">
    <PageHeader title="아이디 찾기" />

    <form class="find-id-form" @submit.prevent="find">
      <BaseInput v-model="form.name" label="이름" placeholder="이름을 입력하세요." />
      <BaseInput
        v-model="form.phone"
        type="phone"
        label="연락처"
        placeholder="가입 시 등록한 전화번호를 입력하세요."
      />

      <p v-if="errorMessage" class="find-id-form__error text-caption">
        {{ errorMessage }}
      </p>

      <button type="submit" class="find-id-submit" :disabled="!canSubmit">
        아이디 찾기
      </button>
    </form>

    <!-- 섹션 구분 굵은 회색선 (금융계산기와 동일 스타일) -->
    <div class="find-id-divider" aria-hidden="true"></div>

    <!-- 결과 (찾은 뒤 폼 아래에 노출) -->
    <div v-if="maskedUserId" class="find-id-result">
      <p class="find-id-result__label">가입하신 아이디</p>
      <div class="find-id-result__box">
        <span class="find-id-result__value">{{ maskedUserId }}</span>
        <button
          type="button"
          class="find-id-result__copy"
          :aria-label="copied ? '복사됨' : '아이디 복사'"
          @click="copyId"
        >
          <svg
            v-if="!copied"
            viewBox="0 0 24 24"
            width="18"
            height="18"
            fill="none"
            aria-hidden="true"
          >
            <rect
              x="9"
              y="9"
              width="11"
              height="11"
              rx="2"
              stroke="currentColor"
              stroke-width="1.8"
            />
            <path
              d="M5 15V5a2 2 0 0 1 2-2h8"
              stroke="currentColor"
              stroke-width="1.8"
              stroke-linecap="round"
            />
          </svg>
          <svg
            v-else
            viewBox="0 0 24 24"
            width="18"
            height="18"
            fill="none"
            aria-hidden="true"
          >
            <path
              d="M5 12l4 4 10-10"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>
      </div>
    </div>

    <BottomButtonBar
      secondary-label="이전"
      primary-label="비밀번호 찾기"
      @secondary-click="router.back()"
      @primary-click="router.push({ name: 'FindPassword' })"
    />
  </div>
</template>

<style scoped>
.find-id-page {
  padding-bottom: 80px;
}

.find-id-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.find-id-form__error {
  margin: 0;
  color: var(--danger);
}

/* 섹션 구분 굵은 회색선 (금융계산기 product-calc__divider와 동일: 8px, 풀블리드, --bg-gray) */
.find-id-divider {
  height: 8px;
  margin: 24px -24px;
  background-color: var(--bg-gray);
}

/* 폼 안 노란 제출 버튼 */
.find-id-submit {
  width: 100%;
  height: 50px;
  margin-top: 4px;
  border: 0;
  border-radius: 12px;
  background: var(--kb-yellow);
  color: var(--text-strong);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.15s ease;
}

.find-id-submit:disabled {
  background: var(--kb-gray-pale);
  color: var(--text-disabled);
  cursor: not-allowed;
}

/* 결과 */
.find-id-result {
  margin-top: 36px;
}

.find-id-result__label {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-body);
}

.find-id-result__box {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 56px;
  padding: 14px 46px;
  border: 1px solid var(--input-border, #d1d5db);
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}

.find-id-result__value {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-strong);
}

.find-id-result__copy {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: 0;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
}
</style>
