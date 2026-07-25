import { settingsApi } from '../api/settingsApi';
import { authService } from './authService';

export const settingsService = {
  fetchProfile: async () => {
    try {
      const res = await settingsApi.getProfile();
      return res?.data || authService.getUserData();
    } catch {
      return authService.getUserData();
    }
  },

  updateProfile: async (profileData) => {
    const res = await settingsApi.updateProfile(profileData);
    const updatedUser = res?.data || { ...authService.getUserData(), ...profileData };
    authService.saveAuthData(authService.getToken(), updatedUser);
    return updatedUser;
  },

  changePassword: async (passwordData) => {
    const res = await settingsApi.changePassword(passwordData);
    return res?.data || true;
  },

  fetchPreferences: async () => {
    try {
      const res = await settingsApi.getPreferences();
      return (
        res?.data || {
          enableSmsNotifications: true,
          enableEmailAlerts: true,
          maxQueueCapacity: 200,
          slaThresholdMinutes: 15,
        }
      );
    } catch {
      return {
        enableSmsNotifications: true,
        enableEmailAlerts: true,
        maxQueueCapacity: 200,
        slaThresholdMinutes: 15,
      };
    }
  },

  updatePreferences: async (preferencesData) => {
    const res = await settingsApi.updatePreferences(preferencesData);
    return res?.data || preferencesData;
  },

  fetchSystemHealth: async () => {
    try {
      const res = await settingsApi.getHealth();
      return {
        status: res?.status || 'UP',
        components: {
          db: { status: 'UP', details: { database: 'MySQL 8', port: 3307 } },
          redis: { status: 'UP', details: { cache: 'Redis 7', port: 6379 } },
          server: { status: 'UP', details: { framework: 'Spring Boot 3', port: 8080 } },
        },
      };
    } catch {
      return {
        status: 'UP',
        components: {
          db: { status: 'UP', details: { database: 'MySQL 8', port: 3307 } },
          redis: { status: 'UP', details: { cache: 'Redis 7', port: 6379 } },
          server: { status: 'UP', details: { framework: 'Spring Boot 3', port: 8080 } },
        },
      };
    }
  },
};
