import api from '@/api';

const BASE_URL = '/api/travel';
const DEFAULT_TIMEOUT = 10000;
const COST_CALCULATION_TIMEOUT = 65000;
const PACKAGE_SEARCH_TIMEOUT = 120000;
const PLACE_CACHE_TTL = 6 * 60 * 60 * 1000;
const PLACE_CATEGORIES = ['attraction', 'restaurant'];
const placeRequests = new Map();
const packageRequests = new Map();

const toPlaceCacheKey = (goalId, category) =>
  `travel:places:${goalId}:${category}`;

const findCachedPlaces = (goalId, category) => {
  try {
    const cacheKey = toPlaceCacheKey(goalId, category);
    const cachedValue = sessionStorage.getItem(cacheKey);
    if (!cachedValue) return null;

    const cached = JSON.parse(cachedValue);
    if (Date.now() - cached.savedAt >= PLACE_CACHE_TTL) {
      sessionStorage.removeItem(cacheKey);
      return null;
    }

    return { data: cached.data };
  } catch {
    return null;
  }
};

const cachePlaces = (goalId, category, response) => {
  try {
    sessionStorage.setItem(
      toPlaceCacheKey(goalId, category),
      JSON.stringify({ savedAt: Date.now(), data: response.data }),
    );
  } catch {
    // 저장 공간이 부족해도 장소 조회 자체는 정상적으로 동작해야 한다.
  }
};

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

  getGoalDetail(goalId) {
    return api.get(`${BASE_URL}/goals/${goalId}`, {
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

  updateCostStyle(goalId, style) {
    return api.patch(`${BASE_URL}/goals/${goalId}/costs/style`, {
      style,
    });
  },

  searchPlaces(goalId, category) {
    const cached = findCachedPlaces(goalId, category);
    if (cached) return Promise.resolve(cached);

    const requestKey = toPlaceCacheKey(goalId, category);
    if (!placeRequests.has(requestKey)) {
      const request = api
        .get(`${BASE_URL}/goals/${goalId}/places`, {
          params: { category },
          timeout: DEFAULT_TIMEOUT,
        })
        .then((response) => {
          cachePlaces(goalId, category, response);
          return response;
        })
        .finally(() => placeRequests.delete(requestKey));
      placeRequests.set(requestKey, request);
    }

    return placeRequests.get(requestKey);
  },

  prefetchPlaces(goalId) {
    return Promise.allSettled(
      PLACE_CATEGORIES.map((category) => this.searchPlaces(goalId, category)),
    );
  },

  clearPlaceCache(goalId) {
    PLACE_CATEGORIES.forEach((category) => {
      sessionStorage.removeItem(toPlaceCacheKey(goalId, category));
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

  findPackages(goalId) {
    if (!packageRequests.has(goalId)) {
      const request = api
        .get(`${BASE_URL}/goals/${goalId}/packages`, {
          timeout: PACKAGE_SEARCH_TIMEOUT,
        })
        .finally(() => packageRequests.delete(goalId));
      packageRequests.set(goalId, request);
    }

    return packageRequests.get(goalId);
  },

  prefetchPackages(goalId) {
    return this.findPackages(goalId).catch(() => null);
  },

  updatePackage(goalId, packageId) {
    return api.patch(`${BASE_URL}/goals/${goalId}/package`, {
      packageId,
    });
  },

  findProducts(goalId) {
    return api.get(`${BASE_URL}/goals/${goalId}/products`, {
      timeout: DEFAULT_TIMEOUT,
    });
  },

  confirmGoal(goalId) {
    return api.post(`${BASE_URL}/goals/${goalId}/confirm`);
  },

  deleteGoal(goalId) {
    return api.delete(`${BASE_URL}/goals/${goalId}`);
  },
};
