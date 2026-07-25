import axiosClient from './axiosClient';

export const authApi = {
  login: async (credentials) => {
    const response = await axiosClient.post('/api/v1/auth/login', credentials);
    return response.data;
  },
};
