import axiosClient from './axiosClient';

export const organizationApi = {
  getOrganizations: async () => {
    const response = await axiosClient.get('/api/v1/organizations');
    return response.data;
  },

  getOrganization: async (id = 1) => {
    const response = await axiosClient.get(`/api/v1/organizations/${id}`);
    return response.data;
  },

  createOrganization: async (data) => {
    const response = await axiosClient.post('/api/v1/organizations', data);
    return response.data;
  },

  getBranches: async (organizationId = 1) => {
    const response = await axiosClient.get(`/api/v1/organizations/${organizationId}/branches`);
    return response.data;
  },

  createBranch: async (organizationId = 1, data) => {
    const response = await axiosClient.post(`/api/v1/organizations/${organizationId}/branches`, data);
    return response.data;
  },

  getDepartments: async (branchId = 1) => {
    const response = await axiosClient.get(`/api/v1/branches/${branchId}/departments`);
    return response.data;
  },

  getCounters: async (branchId = 1) => {
    const response = await axiosClient.get(`/api/v1/branches/${branchId}/counters`);
    return response.data;
  },

  createCounter: async (branchId = 1, data) => {
    const response = await axiosClient.post(`/api/v1/branches/${branchId}/counters`, data);
    return response.data;
  },
};
