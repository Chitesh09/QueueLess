import { counterApi } from '../api/counterApi';

const ACTIVE_COUNTER_KEY = 'queueless_active_counter_id';

export const counterService = {
  fetchCounters: async (branchId = 1) => {
    const res = await counterApi.getCounters(branchId);
    return res?.data || [];
  },

  callNextToken: async (counterId) => {
    const res = await counterApi.callNext(counterId);
    if (res && res.success) {
      return res.data; // Can be null if queue is empty
    }
    throw new Error(res?.message || 'Failed to call next customer');
  },

  completeService: async (counterId, tokenId) => {
    const res = await counterApi.completeService(counterId, tokenId);
    if (res && res.success && res.data) {
      return res.data;
    }
    throw new Error(res?.message || 'Failed to complete service');
  },

  skipToken: async (counterId, tokenId) => {
    const res = await counterApi.skipToken(counterId, tokenId);
    if (res && res.success && res.data) {
      return res.data;
    }
    throw new Error(res?.message || 'Failed to skip token');
  },

  pauseCounter: async (counterId) => {
    const res = await counterApi.pauseCounter(counterId);
    return res?.data || 'PAUSED';
  },

  resumeCounter: async (counterId) => {
    const res = await counterApi.resumeCounter(counterId);
    return res?.data || 'ONLINE';
  },

  getStoredCounterId: () => {
    const saved = localStorage.getItem(ACTIVE_COUNTER_KEY);
    return saved ? Number(saved) : 1;
  },

  setStoredCounterId: (counterId) => {
    localStorage.setItem(ACTIVE_COUNTER_KEY, String(counterId));
  },
};
