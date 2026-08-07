<script setup>
import { computed, reactive, ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter, useRoute } from 'vue-router';
import BaseInput from '@/components/common/BaseInput.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const member = reactive({
  username: '',
  password: '',
});

const error = ref('');
const submitting = ref(false);
const disableSubmit = computed(
  () => !(member.username && member.password) || submitting.value,
);

const login = async () => {
  error.value = '';
  submitting.value = true;
  try {
    await auth.login(member);
    router.push(route.query.redirect ? String(route.query.redirect) : '/home');
  } catch (e) {
    const data = e.response?.data;
    error.value = typeof data === 'string' && data ? data : '로그인에 실패했습니다.';
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="login-page">
    <h1 class="text-title mt-4 mb-4">로그인</h1>

    <form class="login-form" @submit.prevent="login" @keydown.enter.prevent="login">
      <BaseInput
        v-model="member.username"
        label="아이디"
        placeholder="아이디를 입력하세요"
      />
      <BaseInput
        v-model="member.password"
        type="password"
        label="비밀번호"
        placeholder="비밀번호를 입력하세요"
      />

      <p v-if="error" class="login-form__error text-caption">{{ error }}</p>

      <div class="login-form__links text-caption">
        <router-link to="/find/id">아이디 찾기</router-link>
        <span class="login-form__divider">|</span>
        <router-link to="/find/password">비밀번호 찾기</router-link>
        <span class="login-form__divider">|</span>
        <router-link to="/signup/info">회원가입</router-link>
      </div>
    </form>

    <BottomButtonBar
      primary-label="로그인"
      :primary-disabled="disableSubmit"
      @primary-click="login"
    />
  </div>
</template>

<style scoped>
.login-page {
  padding-bottom: 80px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.login-form__error {
  color: var(--danger);
  margin: 0;
}

.login-form__links {
  display: flex;
  justify-content: center;
  gap: 8px;
  color: var(--text-muted);
}

.login-form__links a {
  color: var(--text-muted);
  text-decoration: none;
}

.login-form__divider {
  color: var(--line-strong);
}
</style>
