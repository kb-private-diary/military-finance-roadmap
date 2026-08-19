// 앱 전체에서 동작하는 공통 웹푸시 Service Worker.
self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', (event) => event.waitUntil(clients.claim()));

const normalizeInternalUrl = (url) => {
  if (typeof url !== 'string' || !url.startsWith('/') || url.startsWith('//')) {
    return '/';
  }
  return url;
};

self.addEventListener('push', (event) => {
  const data = event.data ? event.data.json() : {};
  const title = data.title || '알림';
  const options = {
    body: data.body || '',
    data: {
      url: normalizeInternalUrl(data.url),
    },
  };
  event.waitUntil(self.registration.showNotification(title, options));
});

// 알림 클릭 시 이미 열린 앱을 재사용하고, 없으면 새 창을 연다.
self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  const targetPath = normalizeInternalUrl(event.notification.data?.url);
  const targetUrl = new URL(targetPath, self.location.origin).href;

  event.waitUntil(
    clients
      .matchAll({ type: 'window', includeUncontrolled: true })
      .then(async (windowClients) => {
        const appClient = windowClients.find(
          (client) => new URL(client.url).origin === self.location.origin,
        );

        if (appClient) {
          await appClient.navigate(targetUrl);
          return appClient.focus();
        }
        return clients.openWindow(targetUrl);
      }),
  );
});
