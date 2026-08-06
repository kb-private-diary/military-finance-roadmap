import { ref } from 'vue';
import pushApi from '@/api/pushApi';
import { useToast } from '@/composables/useToast';

const VAPID_PUBLIC_KEY = import.meta.env.VITE_VAPID_PUBLIC_KEY;

// 이 브라우저가 웹푸시를 지원하는지 (iOS Safari는 홈화면 추가 전엔 미지원)
const isSupported = 'serviceWorker' in navigator && 'PushManager' in window;

// Service Worker 등록은 앱 전체에서 한 번만 하면 되므로 모듈 스코프에 캐시해 재사용한다.
let swRegistrationPromise = null;
const getRegistration = () => {
  if (!swRegistrationPromise) {
    swRegistrationPromise = navigator.serviceWorker.register('/sw.js');
  }
  return swRegistrationPromise;
};

// VAPID 공개키(base64url 문자열) → PushManager가 요구하는 Uint8Array로 변환 (웹푸시 표준 절차)
const urlBase64ToUint8Array = (base64String) => {
  const padding = '='.repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  const rawData = window.atob(base64);
  return Uint8Array.from([...rawData].map((char) => char.charCodeAt(0)));
};

// 어느 화면에서든 import { usePush } from '@/composables/usePush' 해서 쓰면 됨.
export function usePush() {
  const isSubscribed = ref(false);
  const loading = ref(false);
  const { show } = useToast();

  // 지금 이 브라우저가 이미 구독 중인지 확인 (토글 초기값 세팅용, onMounted에서 호출)
  const checkSubscription = async () => {
    if (!isSupported) return;
    const registration = await getRegistration();
    const subscription = await registration.pushManager.getSubscription();
    isSubscribed.value = !!subscription;
  };

  const subscribe = async () => {
    if (!isSupported) {
      show('이 브라우저는 알림을 지원하지 않아요', 'error');
      return;
    }
    loading.value = true;
    try {
      const permission = await Notification.requestPermission();
      if (permission !== 'granted') {
        show('알림 권한이 거부됐어요', 'error');
        return;
      }
      const registration = await getRegistration();
      const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(VAPID_PUBLIC_KEY),
      });
      try {
        await pushApi.subscribe(subscription.toJSON());
      } catch (e) {
        // 백엔드 저장 실패 시 브라우저 쪽 구독도 롤백한다.
        // 안 하면 브라우저엔 구독이 남아서 다음에 켰을 때 "이미 켜짐"으로 잘못 보임.
        await subscription.unsubscribe();
        throw e;
      }
      isSubscribed.value = true;
      show('알림을 켰어요', 'success');
    } catch (e) {
      show('알림 설정 중 문제가 발생했어요', 'error');
    } finally {
      loading.value = false;
    }
  };

  const unsubscribe = async () => {
    loading.value = true;
    try {
      const registration = await getRegistration();
      const subscription = await registration.pushManager.getSubscription();
      if (subscription) {
        try {
          await pushApi.unsubscribe(subscription.endpoint);
        } catch (e) {
          // 서버 DB엔 이미 없는 구독(PUSH_001)이면 무시하고 로컬 정리는 계속 진행한다.
          // 브라우저-서버 상태가 어긋나 있던 것뿐, 목표(구독 해지됨)는 어차피 달성되는 상황.
          if (e.response?.data?.code !== 'PUSH_001') {
            throw e;
          }
        }
        await subscription.unsubscribe();
      }
      isSubscribed.value = false;
      show('알림을 껐어요', 'info');
    } catch (e) {
      show('알림 해지 중 문제가 발생했어요', 'error');
    } finally {
      loading.value = false;
    }
  };

  return { isSupported, isSubscribed, loading, checkSubscription, subscribe, unsubscribe };
}
