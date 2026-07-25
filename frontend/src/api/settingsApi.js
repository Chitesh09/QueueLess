import axiosClient from './axiosClient';

export const settingsApi = {
  getProfile: async () => {
    const response = await axiosClient.get('/api/v1/users/me');
    return response.data;
  },

  updateProfile: async (data) => {
    const response = await axiosClient.put('/api/v1/users/profile', data);
    return response.data;
  },

  changePassword: async (data) => {
    const response = await axiosClient.post('/api/v1/users/change-password', data);
    return response.data;
  },

  getPreferences: async () => {
    const response = await axiosClient.get('/api/v1/organization/preferences');
    return response.data;
  },

  updatePreferences: async (data) => {
    const response = await axiosClient.put('/api/v1/organization/preferences', data);
    return response.data;
  },

  getHealth: async () => {
    const response = await axiosClient.get('/actuator/health');
    return response.data;
  },
};
