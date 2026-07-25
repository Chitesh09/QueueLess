import { queueApi } from '../api/queueApi';

const ACTIVE_TOKEN_KEY = 'queueless_active_token_id';

export const queueService = {
  fetchServices: async (branchId = 1) => {
    const res = await queueApi.getServices(branchId);
    return res?.data || [];
  },

  joinQueue: async (serviceId, priorityClass) => {
    const res = await queueApi.joinQueue(serviceId, priorityClass);
    if (res && res.success && res.data) {
      localStorage.setItem(ACTIVE_TOKEN_KEY, String(res.data.id));
      return res.data;
    }
    throw new Error(res?.message || 'Failed to join queue');
  },

  fetchTokenStatus: async (tokenId) => {
    if (!tokenId) return null;
    const res = await queueApi.getTokenStatus(tokenId);
    return res?.data || null;
  },

  fetchServiceETA: async (serviceId, position) => {
    const res = await queueApi.getServiceETA(serviceId, position);
    return res?.data || null;
  },

  checkInToken: async (tokenId, qrSignature) => {
    const res = await queueApi.checkInToken(tokenId, qrSignature);
    if (res && res.success && res.data) {
      return res.data;
    }
    throw new Error(res?.message || 'Check-in failed');
  },

  getStoredTokenId: () => {
    const saved = localStorage.getItem(ACTIVE_TOKEN_KEY);
    return saved ? Number(saved) : null;
  },

  clearStoredTokenId: () => {
    localStorage.removeItem(ACTIVE_TOKEN_KEY);
  },
};
