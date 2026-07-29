<script setup>
// SCR-MYP-02 · 회원정보 수정 (담당: 호빈)
// 이름·전화번호·부대정보 수정 (군종/계급/입대일 등은 여기서 다루지 않음)
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import memberApi from '@/api/memberApi';
import BaseInput from '@/components/common/BaseInput.vue';
import BottomButtonBar from '@/components/common/BottomButtonBar.vue';

const router = useRouter();

const form = reactive({
  name: '',
  phone: '',
  unitName: '',
  unitCode: '',
});

const loading = ref(true);
const submitting = ref(false);
const errorMessage = ref('');

const canSubmit = computed(() => form.name && form.phone && !submitting.value);

const load = async () => {
  loading.value = true;
  try {
    const member = await memberApi.getMyInfo();
    form.name = member.name;
    form.phone = member.phone;
    form.unitName = member.unitName || '';
    form.unitCode = member.unitCode || '';
  } catch (e) {
    errorMessage.value = e.response?.data?.message || '내 정보를 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

const submit = async () => {
  errorMessage.value = '';
  submitting.value = true;
  try {
    await memberApi.updateMyInfo({ ...form });
    router.push({ name: 'MyPage' });
  } catch (e) {
    errorMessage.value = e.response?.data?.message || '수정 중 오류가 발생했습니다.';
  } finally {
    submitting.value = false;
  }
};

onMounted(load);
</script>

<template>
  <div class="mypage-edit-page">
    <h1 class="text-title mt-4 mb-4">회원정보 수정</h1>

    <p v-if="loading" class="text-caption">불러오는 중...</p>

    <template v-else>
      <form class="mypage-edit-form" @submit.prevent="submit">
        <BaseInput v-model="form.name" label="이름" placeholder="이름을 입력하세요" />
        <BaseInput v-model="form.phone" label="전화번호" placeholder="010-0000-0000" />
        <BaseInput v-model="form.unitName" label="부대명" placeholder="예: 수도방위사령부" />
        <BaseInput v-model="form.unitCode" label="부대코드" placeholder="부대코드 (선택)" />

        <p v-if="errorMessage" class="mypage-edit-form__error text-caption">{{ errorMessage }}</p>
      </form>

      <BottomButtonBar primary-label="수정 완료" :primary-disabled="!canSubmit" @primary-click="submit" />
    </template>
  </div>
</template>

<style scoped>
.mypage-edit-page {
  padding-bottom: 80px;
}

.mypage-edit-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.mypage-edit-form__error {
  color: var(--danger);
  margin: 0;
}
</style>
