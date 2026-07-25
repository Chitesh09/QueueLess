import axiosClient from './axiosClient';

export const analyticsApi = {
  getSummary: async (branchId = 1) => {
    const response = await axiosClient.get(`/api/v1/analytics/summary?branchId=${branchId}`);
    return response.data;
  },

  getPeakHours: async (branchId = 1) => {
    const response = await axiosClient.get(`/api/v1/analytics/peak-hours?branchId=${branchId}`);
    return response.data;
  },

  getThroughput: async (branchId = 1) => {
    const response = await axiosClient.get(`/api/v1/analytics/throughput?branchId=${branchId}`);
    return response.data;
  },

  getSlaCompliance: async (branchId = 1) => {
    const response = await axiosClient.get(`/api/v1/analytics/sla?branchId=${branchId}`);
    return response.data;
  },
};
