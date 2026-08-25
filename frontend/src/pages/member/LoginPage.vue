<script setup>
import { computed, reactive, ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter, useRoute } from 'vue-router';
import BaseInput from '@/components/common/BaseInput.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';
import heroMascot from '@/assets/images/char-bibi.png';

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
    <PageHeader title="로그인" />

    <section class="login-hero">
      <div class="login-hero__text">
        <p class="login-hero__title">충성! 환영합니다</p>
        <p class="login-hero__desc">로그인하고 통장 사수 작전을 이어가세요</p>
      </div>
      <img :src="heroMascot" alt="비비 캐릭터" class="login-hero__mascot" />
    </section>

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
        <span class="login-form__divider">·</span>
        <router-link to="/find/password">비밀번호 찾기</router-link>
      </div>

      <p class="login-form__signup text-caption">
        계정이 없으신가요?
        <router-link to="/signup/info">회원가입</router-link>
      </p>
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

.login-hero {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 16px;
  margin: 2px 0 26px;
}

.login-hero__mascot {
  width: 84px;
  height: auto;
  flex-shrink: 0;
  margin-left: auto;
  margin-right: 10px;
}

.login-hero__title {
  margin: 0;
  font-size: 25px;
  font-weight: 800;
  color: var(--text-strong);
}

.login-hero__desc {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--text-muted);
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
  gap: 10px;
  color: var(--text-muted);
}

.login-form__links a {
  color: var(--text-muted);
  text-decoration: none;
  transition: color 0.15s ease;
}

.login-form__links a:hover {
  color: var(--text-strong);
}

.login-form__divider {
  color: var(--line-strong);
}

.login-form__signup {
  margin: -4px 0 0;
  text-align: center;
  color: var(--text-muted);
}

.login-form__signup a {
  margin-left: 4px;
  color: var(--military-green);
  font-weight: 700;
  text-decoration: none;
  transition: color 0.15s ease;
}

.login-form__signup a:hover {
  color: #3a4633;
}
</style>
