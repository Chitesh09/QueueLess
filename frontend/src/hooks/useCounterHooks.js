import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { counterService } from '../services/counterService';

export const useBranchCountersQuery = (branchId = 1) => {
  return useQuery({
    queryKey: ['counters', branchId],
    queryFn: () => counterService.fetchCounters(branchId),
    staleTime: 2 * 60 * 1000,
  });
};

export const useCallNextMutation = (onSuccessCallback) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (counterId) => counterService.callNextToken(counterId),
    onSuccess: (data) => {
      if (data) {
        toast.success(`Token ${data.tokenNumber} called!`);
      } else {
        toast('Queue is empty. No waiting customers.', { icon: 'ℹ️' });
      }
      queryClient.invalidateQueries({ queryKey: ['tokenStatus'] });
      if (onSuccessCallback) {
        onSuccessCallback(data);
      }
    },
    onError: (error) => {
      const msg = error.response?.data?.detail || error.message || 'Failed to call next token.';
      toast.error(msg);
    },
  });
};

export const useCompleteServiceMutation = (onSuccessCallback) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ counterId, tokenId }) => counterService.completeService(counterId, tokenId),
    onSuccess: (data) => {
      toast.success(`Service completed for token ${data.tokenNumber}`);
      queryClient.invalidateQueries({ queryKey: ['tokenStatus'] });
      if (onSuccessCallback) {
        onSuccessCallback(data);
      }
    },
    onError: (error) => {
      const msg = error.response?.data?.detail || error.message || 'Failed to complete service.';
      toast.error(msg);
    },
  });
};

export const useSkipTokenMutation = (onSuccessCallback) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ counterId, tokenId }) => counterService.skipToken(counterId, tokenId),
    onSuccess: (data) => {
      toast.success(`Token ${data.tokenNumber} skipped.`);
      queryClient.invalidateQueries({ queryKey: ['tokenStatus'] });
      if (onSuccessCallback) {
        onSuccessCallback(data);
      }
    },
    onError: (error) => {
      const msg = error.response?.data?.detail || error.message || 'Failed to skip token.';
      toast.error(msg);
    },
  });
};

export const usePauseCounterMutation = (onSuccessCallback) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (counterId) => counterService.pauseCounter(counterId),
    onSuccess: (status, counterId) => {
      toast('Counter status: PAUSED', { icon: '⏸️' });
      queryClient.invalidateQueries({ queryKey: ['counters'] });
      if (onSuccessCallback) onSuccessCallback(status);
    },
  });
};

export const useResumeCounterMutation = (onSuccessCallback) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (counterId) => counterService.resumeCounter(counterId),
    onSuccess: (status, counterId) => {
      toast.success('Counter status: ONLINE');
      queryClient.invalidateQueries({ queryKey: ['counters'] });
      if (onSuccessCallback) onSuccessCallback(status);
    },
  });
};
