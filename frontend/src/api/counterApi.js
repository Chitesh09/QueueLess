import axiosClient from './axiosClient';

export const counterApi = {
  getCounters: async (branchId = 1) => {
    const response = await axiosClient.get(`/api/v1/branches/${branchId}/counters`);
    return response.data;
  },

  callNext: async (counterId) => {
    const response = await axiosClient.post(`/api/v1/counters/${counterId}/call-next`);
    return response.data;
  },

  completeService: async (counterId, tokenId) => {
    const response = await axiosClient.post(`/api/v1/counters/${counterId}/complete/${tokenId}`);
    return response.data;
  },

  skipToken: async (counterId, tokenId) => {
    const response = await axiosClient.post(`/api/v1/counters/${counterId}/skip/${tokenId}`);
    return response.data;
  },

  pauseCounter: async (counterId) => {
    const response = await axiosClient.post(`/api/v1/counters/${counterId}/pause`);
    return response.data;
  },

  resumeCounter: async (counterId) => {
    const response = await axiosClient.post(`/api/v1/counters/${counterId}/resume`);
    return response.data;
  },
};
