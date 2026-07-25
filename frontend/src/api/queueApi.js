import axiosClient from './axiosClient';

export const queueApi = {
  getServices: async (branchId = 1) => {
    const response = await axiosClient.get(`/api/v1/branches/${branchId}/services`);
    return response.data;
  },

  joinQueue: async (serviceId, priorityClass = 'STANDARD') => {
    const response = await axiosClient.post(`/api/v1/services/${serviceId}/tokens`, {
      serviceId,
      priorityClass,
    });
    return response.data;
  },

  getTokenStatus: async (tokenId) => {
    const response = await axiosClient.get(`/api/v1/tokens/${tokenId}`);
    return response.data;
  },

  getServiceETA: async (serviceId, position = 1) => {
    const response = await axiosClient.get(`/api/v1/services/${serviceId}/eta?position=${position}`);
    return response.data;
  },

  checkInToken: async (tokenId, qrSignature) => {
    const response = await axiosClient.post(`/api/v1/tokens/${tokenId}/check-in`, {
      qrSignature,
    });
    return response.data;
  },
};
