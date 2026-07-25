import React, { memo } from 'react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts';
import { Card } from '../ui/Card';

export const PeakHoursAreaChart = memo(({ data }) => {
  return (
    <Card
      className="p-6 space-y-4 border-slate-800 bg-slate-900/60"
      role="region"
      aria-label="Peak Hours Hourly Customer Arrivals and Dispatch Area Chart"
    >
      <div className="flex items-center justify-between border-b border-slate-800 pb-3">
        <div>
          <h2 className="text-sm font-bold text-white">Peak Hours Customer Arrivals &amp; Dispatch</h2>
          <p className="text-[11px] text-slate-400">Hourly patient arrival spikes vs served throughput</p>
        </div>
        <span className="text-[10px] font-mono text-sky-400 bg-sky-500/10 px-2 py-0.5 rounded-full border border-sky-500/20">
          HOURLY TREND ANALYSIS
        </span>
      </div>

      <div className="h-64 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
            <defs>
              <linearGradient id="colorArrivals" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#38bdf8" stopOpacity={0.4} />
                <stop offset="95%" stopColor="#38bdf8" stopOpacity={0} />
              </linearGradient>
              <linearGradient id="colorServed" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#818cf8" stopOpacity={0.4} />
                <stop offset="95%" stopColor="#818cf8" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
            <XAxis dataKey="hour" stroke="#64748b" fontSize={11} />
            <YAxis stroke="#64748b" fontSize={11} />
            <Tooltip
              contentStyle={{
                backgroundColor: '#090d16',
                borderColor: '#1e293b',
                borderRadius: '0.75rem',
                color: '#fff',
                fontSize: '12px',
              }}
            />
            <Area type="monotone" dataKey="arrivals" stroke="#38bdf8" fillOpacity={1} fill="url(#colorArrivals)" name="Arrivals" />
            <Area type="monotone" dataKey="served" stroke="#818cf8" fillOpacity={1} fill="url(#colorServed)" name="Served Customers" />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </Card>
  );
});
