// 웹푸시 공통 Service Worker. 페이지 무관하게 origin 전체에서 동작한다.
// (등록은 usePush.js가 알림 켜기 시점에 처음 한 번 함, 여기선 이벤트 처리만)

// 새 버전 배포 시 기존 탭을 다 닫지 않아도 바로 활성화되도록
self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', (event) => event.waitUntil(clients.claim()));

self.addEventListener('push', (event) => {
  const data = event.data ? event.data.json() : {};
  const title = data.title || '알림';
  const options = {
    body: data.body || '',
    // icon: '/icon.png', // TODO: public/에 알림 아이콘 추가되면 경로 지정
  };
  event.waitUntil(self.registration.showNotification(title, options));
});

// 알림 클릭 시 앱 열기
self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  event.waitUntil(clients.openWindow('/'));
});
