import { authApi } from '../api/authApi';
import { STORAGE_KEYS } from '../constants/config';

export const authService = {
  login: async (credentials) => {
    const res = await authApi.login(credentials);
    
    // Spring Boot returns ApiResponse wrapper: { success: true, data: { token, userId, role, ... } }
    if (res && res.success && res.data) {
      const authData = res.data;
      localStorage.setItem(STORAGE_KEYS.TOKEN, authData.token);
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(authData));
      if (authData.organizationId) {
        localStorage.setItem(STORAGE_KEYS.TENANT_ID, String(authData.organizationId));
      }
      return authData;
    }
    
    throw new Error(res?.message || 'Authentication failed');
  },

  logout: () => {
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
  },

  getStoredUser: () => {
    const userStr = localStorage.getItem(STORAGE_KEYS.USER);
    if (!userStr) return null;
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  },

  getStoredToken: () => {
    return localStorage.getItem(STORAGE_KEYS.TOKEN);
  },

  isAuthenticated: () => {
    return !!localStorage.getItem(STORAGE_KEYS.TOKEN);
  }
};
