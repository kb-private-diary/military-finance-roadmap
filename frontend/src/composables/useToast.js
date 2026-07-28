import { ref } from 'vue';

// 공통 토스트. 라이브러리 없이 자체 구현.
// 상태를 '모듈 스코프'에 두어(= 파일 최상단) 앱 전체가 같은 큐를 공유한다.
// 화면 어디서든  const { show } = useToast(); show('저장했어요');  로 띄운다.
// 실제 렌더는 App.vue 에 한 번 마운트한 <BaseToast /> 가 담당한다.

const toasts = ref([]); // { id, message, type }
let seq = 0;

export function useToast() {
  const remove = (id) => {
    toasts.value = toasts.value.filter((toast) => toast.id !== id);
  };

  // type: 'info'(기본) | 'success' | 'error'
  const show = (message, type = 'info', duration = 2500) => {
    const id = ++seq;
    toasts.value.push({ id, message, type });
    setTimeout(() => remove(id), duration);
  };

  return { toasts, show, remove };
}
