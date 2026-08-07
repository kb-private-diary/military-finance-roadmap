import api from '@/api';

const BASE_URL = '/api/car';

export default {
  createGoal(request) {
    return api.post(`${BASE_URL}/goals`, request);
  },

  findGoals(userId) {
    return api.get(`${BASE_URL}/goals`, { params: { userId } });
  },

  findGoalDetail(goalId) {
    return api.get(`${BASE_URL}/goals/${goalId}`);
  },

  findRecommendations(goalId) {
    return api.get(`${BASE_URL}/goals/${goalId}/recommendations`);
  },

  selectModel(goalId, request) {
    return api.patch(`${BASE_URL}/goals/${goalId}/select-model`, request);
  },

  findMaintenanceCost(goalId) {
    return api.get(`${BASE_URL}/goals/${goalId}/maintenance-cost`);
  },

  findAcquisitionTax(goalId) {
    return api.get(`${BASE_URL}/goals/${goalId}/acquisition-tax`);
  },

  findUsedPrice(goalId) {
    return api.get(`${BASE_URL}/goals/${goalId}/used-price`);
  },

  findEvSubsidy(goalId) {
    return api.get(`${BASE_URL}/goals/${goalId}/ev-subsidy`);
  },

  findBudgetStatus(goalId) {
    return api.get(`${BASE_URL}/goals/${goalId}/budget-status`);
  },
};
