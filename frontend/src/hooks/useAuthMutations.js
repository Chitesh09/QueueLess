import { useMutation } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { authService } from '../services/authService';

export const useLoginMutation = (onSuccessCallback) => {
  return useMutation({
    mutationFn: (credentials) => authService.login(credentials),
    onSuccess: (data) => {
      toast.success(`Welcome back, ${data.name || 'User'}!`);
      if (onSuccessCallback) {
        onSuccessCallback(data);
      }
    },
    onError: (error) => {
      const errorMsg = error.response?.data?.detail || error.message || 'Login failed. Please check your credentials.';
      toast.error(errorMsg);
    },
  });
};
