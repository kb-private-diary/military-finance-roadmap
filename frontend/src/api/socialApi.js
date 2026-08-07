import instance from '@/api';

const BASE_URL = '/api/social';

export default {
  async findStats(scope) {
    const { data } = await instance.get(`${BASE_URL}/stats`, {
      params: { scope },
    });
    return data.data;
  },

  async findDistributionList(scope) {
    const { data } = await instance.get(`${BASE_URL}/distributions`, {
      params: { scope },
    });
    return data.data;
  },

  async findRanking(scope) {
    const { data } = await instance.get(`${BASE_URL}/ranking`, {
      params: { scope },
    });
    return data.data;
  },

  async findBadgeList() {
    const { data } = await instance.get(`${BASE_URL}/badges`);
    return data.data;
  },
};
