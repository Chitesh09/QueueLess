import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { organizationService } from '../services/organizationService';

export const useOrganizationsQuery = () => {
  return useQuery({
    queryKey: ['organizations'],
    queryFn: () => organizationService.fetchOrganizations(),
    staleTime: 5 * 60 * 1000,
  });
};

export const useOrganizationQuery = (orgId = 1) => {
  return useQuery({
    queryKey: ['organization', orgId],
    queryFn: () => organizationService.fetchOrganization(orgId),
    staleTime: 5 * 60 * 1000,
  });
};

export const useOrganizationBranchesQuery = (orgId = 1) => {
  return useQuery({
    queryKey: ['branches', orgId],
    queryFn: () => organizationService.fetchBranches(orgId),
    staleTime: 5 * 60 * 1000,
  });
};

export const useBranchDepartmentsQuery = (branchId = 1) => {
  return useQuery({
    queryKey: ['departments', branchId],
    queryFn: () => organizationService.fetchDepartments(branchId),
    enabled: !!branchId,
  });
};

export const useBranchCountersQuery = (branchId = 1) => {
  return useQuery({
    queryKey: ['counters', branchId],
    queryFn: () => organizationService.fetchCounters(branchId),
    enabled: !!branchId,
  });
};

export const useCreateOrganizationMutation = (onSuccessCallback) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data) => organizationService.createOrganization(data),
    onSuccess: (data) => {
      toast.success(`Organization "${data.name}" created successfully!`);
      queryClient.invalidateQueries({ queryKey: ['organizations'] });
      if (onSuccessCallback) onSuccessCallback(data);
    },
    onError: (error) => {
      const msg = error.response?.data?.detail || error.message || 'Failed to create organization.';
      toast.error(msg);
    },
  });
};

export const useCreateBranchMutation = (onSuccessCallback) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ orgId = 1, name, address, code }) =>
      organizationService.createBranch(orgId, { name, address, code }),
    onSuccess: (data) => {
      toast.success(`Branch "${data.name}" created successfully!`);
      queryClient.invalidateQueries({ queryKey: ['branches'] });
      if (onSuccessCallback) onSuccessCallback(data);
    },
    onError: (error) => {
      const msg = error.response?.data?.detail || error.message || 'Failed to create branch.';
      toast.error(msg);
    },
  });
};

export const useCreateCounterMutation = (onSuccessCallback) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ branchId = 1, name, departmentId }) =>
      organizationService.createCounter(branchId, { name, departmentId }),
    onSuccess: (data) => {
      toast.success(`Counter "${data.name}" created successfully!`);
      queryClient.invalidateQueries({ queryKey: ['counters'] });
      if (onSuccessCallback) onSuccessCallback(data);
    },
    onError: (error) => {
      const msg = error.response?.data?.detail || error.message || 'Failed to create counter.';
      toast.error(msg);
    },
  });
};
