import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { queueService } from '../services/queueService';

export const useBranchServicesQuery = (branchId = 1) => {
  return useQuery({
    queryKey: ['services', branchId],
    queryFn: () => queueService.fetchServices(branchId),
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

export const useJoinQueueMutation = (onSuccessCallback) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ serviceId, priorityClass }) => queueService.joinQueue(serviceId, priorityClass),
    onSuccess: (data) => {
      toast.success(`Joined queue! Token: ${data.tokenNumber}`);
      queryClient.invalidateQueries({ queryKey: ['tokenStatus', data.id] });
      if (onSuccessCallback) {
        onSuccessCallback(data);
      }
    },
    onError: (error) => {
      const msg = error.response?.data?.detail || error.message || 'Could not join queue.';
      toast.error(msg);
    },
  });
};

export const useTokenStatusQuery = (tokenId) => {
  return useQuery({
    queryKey: ['tokenStatus', tokenId],
    queryFn: () => queueService.fetchTokenStatus(tokenId),
    enabled: !!tokenId,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      if (status === 'WAITING' || status === 'CALLED') return 5000; // Poll every 5s while waiting/called
      return false;
    },
  });
};

export const useServiceEtaQuery = (serviceId, position = 1) => {
  return useQuery({
    queryKey: ['serviceETA', serviceId, position],
    queryFn: () => queueService.fetchServiceETA(serviceId, position),
    enabled: !!serviceId,
  });
};

export const useCheckInMutation = (onSuccessCallback) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ tokenId, qrSignature }) => queueService.checkInToken(tokenId, qrSignature),
    onSuccess: (data) => {
      toast.success('Successfully checked in at counter!');
      queryClient.invalidateQueries({ queryKey: ['tokenStatus', data.id] });
      if (onSuccessCallback) {
        onSuccessCallback(data);
      }
    },
    onError: (error) => {
      const msg = error.response?.data?.detail || error.message || 'Check-in failed.';
      toast.error(msg);
    },
  });
};
