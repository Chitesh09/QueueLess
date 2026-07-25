import React, { useState } from 'react';
import {
  useAnalyticsSummaryQuery,
  usePeakHoursQuery,
  useDepartmentThroughputQuery,
  useSlaComplianceQuery,
} from '../hooks/useAnalyticsHooks';
import { AnalyticsKpiCards } from '../components/analytics/AnalyticsKpiCards';
import { PeakHoursAreaChart } from '../components/analytics/PeakHoursAreaChart';
import { ThroughputBarChart } from '../components/analytics/ThroughputBarChart';
import { SlaPieChart } from '../components/analytics/SlaPieChart';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Building2, MapPin, Filter, RefreshCw, Clock } from 'lucide-react';
import toast from 'react-hot-toast';

export const AnalyticsPage = () => {
  const [dateRange, setDateRange] = useState('TODAY');
  const [selectedOrg, setSelectedOrg] = useState('1');
  const [selectedBranch, setSelectedBranch] = useState('1');
  const [selectedDept, setSelectedDept] = useState('ALL');
  const [lastUpdated, setLastUpdated] = useState('Just now');
  const [isRefreshing, setIsRefreshing] = useState(false);

  // TanStack Query Hooks
  const { data: summary, refetch: refetchSummary } = useAnalyticsSummaryQuery(selectedBranch);
  const { data: peakHours, refetch: refetchPeakHours } = usePeakHoursQuery(selectedBranch);
  const { data: throughput, refetch: refetchThroughput } = useDepartmentThroughputQuery(selectedBranch);
  const { data: slaCompliance, refetch: refetchSla } = useSlaComplianceQuery(selectedBranch);

  const handleManualRefresh = async () => {
    setIsRefreshing(true);
    await Promise.all([
      refetchSummary(),
      refetchPeakHours(),
      refetchThroughput(),
      refetchSla(),
    ]);
    setIsRefreshing(false);
    setLastUpdated(new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }));
    toast.success('Analytics metrics reloaded');
  };

  return (
    <div className="space-y-8 animate-in fade-in-50 duration-300">
      {/* Top Banner & Control Bar */}
      <div className="glass-card p-6 md:p-8 rounded-3xl border border-slate-800 relative overflow-hidden bg-slate-900/60 flex flex-col lg:flex-row items-start lg:items-center justify-between gap-6">
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span className="text-xs uppercase font-extrabold text-sky-400 bg-sky-500/10 px-3.5 py-1 rounded-full border border-sky-500/20">
              Enterprise Business Intelligence
            </span>
            <span className="text-[11px] font-mono text-slate-400 flex items-center gap-1">
              <Clock className="w-3 h-3 text-slate-500" />
              Last updated: {lastUpdated}
            </span>
          </div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white">Queue Throughput &amp; SLA Insights</h1>
          <p className="text-slate-400 text-xs md:text-sm max-w-xl">
            Monitor customer arrival trends, department processing throughput, wait time SLA compliance, and peak capacity metrics across organization branches.
          </p>
        </div>

        {/* Action Controls: Refresh */}
        <div className="flex items-center gap-3 w-full lg:w-auto">
          <Button
            onClick={handleManualRefresh}
            isLoading={isRefreshing}
            variant="outline"
            size="sm"
            className="text-xs font-semibold whitespace-nowrap"
          >
            <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${isRefreshing ? 'animate-spin' : ''}`} />
            Refresh Analytics
          </Button>
        </div>
      </div>

      {/* Advanced Enterprise Analytics Toolbar (Org, Branch, Dept, Date) */}
      <Card className="p-4 border-slate-800 bg-slate-900/60 flex flex-col lg:flex-row items-center justify-between gap-4">
        {/* Date Range Selector */}
        <div className="flex items-center gap-1.5 bg-slate-950 p-1.5 rounded-2xl border border-slate-800 w-full lg:w-auto">
          {['TODAY', 'WEEK', 'MONTH', 'CUSTOM'].map((range) => (
            <button
              key={range}
              onClick={() => setDateRange(range)}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                dateRange === range
                  ? 'bg-sky-600 text-white shadow-md shadow-sky-600/30'
                  : 'text-slate-400 hover:text-white hover:bg-slate-900'
              }`}
            >
              {range}
            </button>
          ))}
        </div>

        {/* Org, Branch & Dept Filter Dropdowns */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 w-full lg:w-auto">
          <div className="flex items-center gap-1.5 bg-slate-950 px-3 py-2 rounded-xl border border-slate-800">
            <Building2 className="w-3.5 h-3.5 text-sky-400 shrink-0" />
            <select
              value={selectedOrg}
              onChange={(e) => setSelectedOrg(e.target.value)}
              className="bg-transparent text-xs font-bold text-white focus:outline-none w-full"
            >
              <option value="1">City Care Hospital</option>
              <option value="2">St. Jude Healthcare</option>
            </select>
          </div>

          <div className="flex items-center gap-1.5 bg-slate-950 px-3 py-2 rounded-xl border border-slate-800">
            <MapPin className="w-3.5 h-3.5 text-purple-400 shrink-0" />
            <select
              value={selectedBranch}
              onChange={(e) => setSelectedBranch(e.target.value)}
              className="bg-transparent text-xs font-bold text-white focus:outline-none w-full"
            >
              <option value="1">Main Campus Branch</option>
              <option value="2">West Wing Specialty Branch</option>
            </select>
          </div>

          <div className="flex items-center gap-1.5 bg-slate-950 px-3 py-2 rounded-xl border border-slate-800">
            <Filter className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
            <select
              value={selectedDept}
              onChange={(e) => setSelectedDept(e.target.value)}
              className="bg-transparent text-xs font-bold text-white focus:outline-none w-full"
            >
              <option value="ALL">All Departments</option>
              <option value="EMERGENCY">Emergency Triage</option>
              <option value="CARDIOLOGY">General Cardiology</option>
              <option value="DEPOSIT">Cash &amp; Deposit Desk</option>
            </select>
          </div>
        </div>
      </Card>

      {/* 6 KPI Cards Header */}
      <AnalyticsKpiCards summary={summary} />

      {/* Main Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Peak Hours AreaChart (2 cols) */}
        <div className="lg:col-span-2">
          <PeakHoursAreaChart data={peakHours} />
        </div>

        {/* SLA Compliance PieChart (1 col) */}
        <div>
          <SlaPieChart data={slaCompliance} />
        </div>
      </div>

      {/* Full Width Department Throughput BarChart */}
      <div>
        <ThroughputBarChart data={throughput} />
      </div>
    </div>
  );
};
