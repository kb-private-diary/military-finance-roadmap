import { ref } from 'vue';
import pushApi from '@/api/pushApi';
import { useAuthStore } from '@/stores/auth';

// 헤더 종 아이콘의 빨간 점. 상태를 '모듈 스코프'에 두어 앱 전체가 공유한다 (useToast.js와 동일 패턴).
// 서버가 읽음 여부를 저장하지 않으므로, "마지막으로 알림함을 연 시각"을 로컬에 저장해두고
// 가장 최근 알림(sent_at DESC 1번째)이 그 시각 이후면 안 읽은 알림이 있다고 본다.
const LAST_SEEN_KEY_PREFIX = 'lastSeenNotificationAt';

const hasUnread = ref(false);

// 로그아웃 후 다른 계정으로 로그인해도 이전 사용자가 "마지막으로 본 시각"이
// 섞이지 않도록 사용자별로 키를 분리한다.
const getLastSeenKey = () => {
  const authStore = useAuthStore();
  return `${LAST_SEEN_KEY_PREFIX}:${authStore.userId}`;
};

export function useNotificationBadge() {
  // 헤더 마운트 시 + 라우트 이동마다 호출해 최신 알림 유무를 다시 확인한다.
  // 로그인 전 화면(로그인/회원가입 등)에도 헤더가 항상 떠 있으므로, 토큰 없이 API를 호출해
  // 서버에 401만 남기지 않도록 로그인 상태일 때만 조회한다.
  const refreshUnreadStatus = async () => {
    const authStore = useAuthStore();
    if (!authStore.isLogin) {
      hasUnread.value = false;
      return;
    }
    try {
      const history = await pushApi.findHistoryList();
      if (history.length === 0) {
        hasUnread.value = false;
        return;
      }
      const lastSeen = localStorage.getItem(getLastSeenKey());
      hasUnread.value =
        !lastSeen || new Date(history[0].sentAt) > new Date(lastSeen);
    } catch (error) {
      console.error(error);
    }
  };

  // 알림함(WebPushPage)을 열람하면 호출 — 지금 시각을 "마지막으로 본 시각"으로 저장해 빨간 점을 지운다.
  const markNotificationsSeen = () => {
    localStorage.setItem(getLastSeenKey(), new Date().toISOString());
    hasUnread.value = false;
  };

  return { hasUnread, refreshUnreadStatus, markNotificationsSeen };
}
