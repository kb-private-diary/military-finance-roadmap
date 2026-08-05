import instance from '@/api'; // api/index.js

const BASE_URL = '/api/push';

export default {
  // 구독 등록 (PUSH-API: POST /api/push/subscriptions)
  async subscribe(subscriptionJson) {
    await instance.post(`${BASE_URL}/subscriptions`, subscriptionJson);
  },

  // 구독 해지 (PUSH-API: DELETE /api/push/subscriptions?endpoint=)
  async unsubscribe(endpoint) {
    await instance.delete(`${BASE_URL}/subscriptions`, { params: { endpoint } });
  },
};
