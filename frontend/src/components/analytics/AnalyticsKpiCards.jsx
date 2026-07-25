import React, { memo } from 'react';
import { UserCheck, Clock, UserX, Zap, Activity, ShieldCheck } from 'lucide-react';
import { Card } from '../ui/Card';

export const AnalyticsKpiCards = memo(({ summary }) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
      <Card className="p-4 space-y-2 glass-card-hover border-slate-800/80">
        <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
          <span>Total Served</span>
          <UserCheck className="w-4 h-4 text-emerald-400" />
        </div>
        <div className="text-xl font-black text-white font-mono">{summary?.totalServed || 142}</div>
        <p className="text-[10px] text-emerald-400 font-medium">↑ 14% vs yesterday</p>
      </Card>

      <Card className="p-4 space-y-2 glass-card-hover border-slate-800/80">
        <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
          <span>Avg Wait Time</span>
          <Clock className="w-4 h-4 text-sky-400" />
        </div>
        <div className="text-xl font-black text-white font-mono">{summary?.avgWaitMinutes || 11.8}m</div>
        <p className="text-[10px] text-sky-400 font-medium">↓ 2.1m SLA Target</p>
      </Card>

      <Card className="p-4 space-y-2 glass-card-hover border-slate-800/80">
        <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
          <span>No-Show Rate</span>
          <UserX className="w-4 h-4 text-rose-400" />
        </div>
        <div className="text-xl font-black text-white font-mono">{summary?.noShowRatePercent || 3.5}%</div>
        <p className="text-[10px] text-emerald-400 font-medium">↓ 0.8% lower limit</p>
      </Card>

      <Card className="p-4 space-y-2 glass-card-hover border-slate-800/80">
        <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
          <span>Peak Throughput</span>
          <Zap className="w-4 h-4 text-amber-400" />
        </div>
        <div className="text-xl font-black text-amber-400 font-mono">
          {summary?.peakHourlyThroughput || 28}/hr
        </div>
        <p className="text-[10px] text-slate-400">Peak: 11:00 AM</p>
      </Card>

      <Card className="p-4 space-y-2 glass-card-hover border-slate-800/80">
        <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
          <span>Avg Service Time</span>
          <Activity className="w-4 h-4 text-purple-400" />
        </div>
        <div className="text-xl font-black text-purple-400 font-mono">7.4 min</div>
        <p className="text-[10px] text-purple-400 font-medium">Processing Speed</p>
      </Card>

      <Card className="p-4 space-y-2 glass-card-hover border-slate-800/80">
        <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
          <span>SLA Compliance</span>
          <ShieldCheck className="w-4 h-4 text-emerald-400" />
        </div>
        <div className="text-xl font-black text-emerald-400 font-mono">96.5%</div>
        <p className="text-[10px] text-emerald-400 font-medium">Target &gt; 95%</p>
      </Card>
    </div>
  );
});
