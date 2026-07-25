import { analyticsApi } from '../api/analyticsApi';

export const analyticsService = {
  fetchSummary: async (branchId = 1) => {
    const res = await analyticsApi.getSummary(branchId);
    return (
      res?.data || {
        totalServed: 142,
        avgWaitMinutes: 11.8,
        noShowRatePercent: 3.5,
        peakHourlyThroughput: 28,
      }
    );
  },

  fetchPeakHours: async (branchId = 1) => {
    const res = await analyticsApi.getPeakHours(branchId);
    return (
      res?.data || [
        { hour: '08:00', arrivals: 12, served: 10 },
        { hour: '09:00', arrivals: 25, served: 22 },
        { hour: '10:00', arrivals: 38, served: 32 },
        { hour: '11:00', arrivals: 42, served: 38 },
        { hour: '12:00', arrivals: 20, served: 20 },
        { hour: '13:00', arrivals: 15, served: 15 },
        { hour: '14:00', arrivals: 35, served: 30 },
        { hour: '15:00', arrivals: 28, served: 26 },
        { hour: '16:00', arrivals: 18, served: 18 },
        { hour: '17:00', arrivals: 8, served: 8 },
      ]
    );
  },

  fetchThroughput: async (branchId = 1) => {
    const res = await analyticsApi.getThroughput(branchId);
    return (
      res?.data || [
        { department: 'Emergency Triage', served: 48, noShows: 2 },
        { department: 'General Cardiology', served: 36, noShows: 3 },
        { department: 'Cash & Deposit', served: 32, noShows: 1 },
        { department: 'Loan & Wealth', served: 26, noShows: 1 },
      ]
    );
  },

  fetchSlaCompliance: async (branchId = 1) => {
    const res = await analyticsApi.getSlaCompliance(branchId);
    return (
      res?.data || [
        { name: '< 10m Wait (Optimal)', value: 68, color: '#10b981' },
        { name: '10m - 15m Wait (Target)', value: 24, color: '#3b82f6' },
        { name: '> 15m Wait (SLA Breach)', value: 8, color: '#f43f5e' },
      ]
    );
  },
};
