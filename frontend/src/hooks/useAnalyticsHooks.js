import { useQuery } from '@tanstack/react-query';
import { analyticsService } from '../services/analyticsService';

export const useAnalyticsSummaryQuery = (branchId = 1) => {
  return useQuery({
    queryKey: ['analyticsSummary', branchId],
    queryFn: () => analyticsService.fetchSummary(branchId),
    staleTime: 5 * 60 * 1000,
  });
};

export const usePeakHoursQuery = (branchId = 1) => {
  return useQuery({
    queryKey: ['peakHours', branchId],
    queryFn: () => analyticsService.fetchPeakHours(branchId),
    staleTime: 5 * 60 * 1000,
  });
};

export const useDepartmentThroughputQuery = (branchId = 1) => {
  return useQuery({
    queryKey: ['throughput', branchId],
    queryFn: () => analyticsService.fetchThroughput(branchId),
    staleTime: 5 * 60 * 1000,
  });
};

export const useSlaComplianceQuery = (branchId = 1) => {
  return useQuery({
    queryKey: ['slaCompliance', branchId],
    queryFn: () => analyticsService.fetchSlaCompliance(branchId),
    staleTime: 5 * 60 * 1000,
  });
};
