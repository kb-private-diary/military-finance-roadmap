import api from '@/api';

const BASE_URL = '/api/travel';
const DEFAULT_TIMEOUT = 10000;
const COST_CALCULATION_TIMEOUT = 65000;

export default {
  findCities(country) {
    return api.get(`${BASE_URL}/cities`, {
      params: country ? { country } : undefined,
      timeout: DEFAULT_TIMEOUT,
    });
  },

  createGoal(request) {
    return api.post(`${BASE_URL}/goals`, request);
  },

  findCurrentGoal() {
    return api.get(`${BASE_URL}/goals/current`, {
      timeout: DEFAULT_TIMEOUT,
    });
  },

  updateGoal(goalId, request) {
    return api.patch(`${BASE_URL}/goals/${goalId}`, request);
  },

  createCost(goalId) {
    return api.post(
      `${BASE_URL}/goals/${goalId}/costs`,
      null,
      { timeout: COST_CALCULATION_TIMEOUT },
    );
  },

  findCost(goalId) {
    return api.get(`${BASE_URL}/goals/${goalId}/costs`, {
      timeout: DEFAULT_TIMEOUT,
    });
  },

  searchPlaces(goalId, category) {
    return api.get(`${BASE_URL}/goals/${goalId}/places`, {
      params: { category },
      timeout: DEFAULT_TIMEOUT,
    });
  },

  updatePlaces(goalId, places) {
    return api.patch(`${BASE_URL}/goals/${goalId}/places`, { places });
  },

  getSelectedPlaces(goalId) {
    return api.get(`${BASE_URL}/goals/${goalId}/places/selected`, {
      timeout: DEFAULT_TIMEOUT,
    });
  },
};
