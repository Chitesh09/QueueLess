import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { settingsService } from '../services/settingsService';

export const useUserProfileQuery = () => {
  return useQuery({
    queryKey: ['userProfile'],
    queryFn: () => settingsService.fetchProfile(),
    staleTime: 5 * 60 * 1000,
  });
};

export const useTenantPreferencesQuery = () => {
  return useQuery({
    queryKey: ['tenantPreferences'],
    queryFn: () => settingsService.fetchPreferences(),
    staleTime: 5 * 60 * 1000,
  });
};

export const useBackendHealthQuery = () => {
  return useQuery({
    queryKey: ['systemHealth'],
    queryFn: () => settingsService.fetchSystemHealth(),
    refetchInterval: 30000, // Refetch health every 30s
  });
};

export const useUpdateProfileMutation = (onSuccessCallback) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (profileData) => settingsService.updateProfile(profileData),
    onSuccess: (data) => {
      toast.success('User profile updated successfully!');
      queryClient.invalidateQueries({ queryKey: ['userProfile'] });
      if (onSuccessCallback) onSuccessCallback(data);
    },
    onError: (error) => {
      const msg = error.response?.data?.detail || error.message || 'Failed to update profile.';
      toast.error(msg);
    },
  });
};

export const useChangePasswordMutation = (onSuccessCallback) => {
  return useMutation({
    mutationFn: (passwordData) => settingsService.changePassword(passwordData),
    onSuccess: () => {
      toast.success('Password changed successfully!');
      if (onSuccessCallback) onSuccessCallback();
    },
    onError: (error) => {
      const msg = error.response?.data?.detail || error.message || 'Failed to change password.';
      toast.error(msg);
    },
  });
};

export const useUpdateTenantPreferencesMutation = (onSuccessCallback) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (preferencesData) => settingsService.updatePreferences(preferencesData),
    onSuccess: (data) => {
      toast.success('Tenant preferences saved successfully!');
      queryClient.invalidateQueries({ queryKey: ['tenantPreferences'] });
      if (onSuccessCallback) onSuccessCallback(data);
    },
    onError: (error) => {
      const msg = error.response?.data?.detail || error.message || 'Failed to save preferences.';
      toast.error(msg);
    },
  });
};
