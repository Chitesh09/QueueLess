import React from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, LineChart, Line, CartesianGrid } from 'recharts';
import { TrendingUp, Users, Clock, AlertTriangle, ShieldCheck } from 'lucide-react';

export default function AdminDashboard() {
  const waitTimeTrendData = [
    { hour: '08:00 AM', avgWaitMin: 4 },
    { hour: '09:00 AM', avgWaitMin: 12 },
    { hour: '10:00 AM', avgWaitMin: 22 },
    { hour: '11:00 AM', avgWaitMin: 18 },
    { hour: '12:00 PM', avgWaitMin: 9 },
    { hour: '01:00 PM', avgWaitMin: 14 },
    { hour: '02:00 PM', avgWaitMin: 16 },
    { hour: '03:00 PM', avgWaitMin: 8 },
  ];

  const serviceThroughputData = [
    { service: 'Emergency Triage', completed: 86, noShow: 4 },
    { service: 'Cardiology OPD', completed: 42, noShow: 2 },
    { service: 'Cashier Desk', completed: 130, noShow: 6 },
    { service: 'Loan Desk', completed: 25, noShow: 1 },
  ];

  return (
    <div className="max-w-7xl mx-auto space-y-8 py-6">
      {/* Top Banner */}
      <div className="glass-card p-6 rounded-2xl flex flex-col md:flex-row items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase font-bold text-sky-400 bg-sky-500/10 px-3 py-1 rounded-full">Branch Administration</span>
          <h1 className="text-2xl font-extrabold text-white mt-1">Queue Analytics & Operational SLA</h1>
          <p className="text-xs text-slate-400">Real-time throughput, wait-time heatmaps, and capacity insights</p>
        </div>
        <div className="flex items-center gap-3">
          <button className="px-4 py-2 rounded-xl bg-slate-800 border border-slate-700 text-xs font-semibold text-white hover:bg-slate-700">
            Export Operational PDF Report
          </button>
        </div>
      </div>

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="glass-card p-5 rounded-2xl space-y-2 border border-slate-800">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold">
            <span>Total Tokens Served Today</span>
            <Users className="w-4 h-4 text-sky-400" />
          </div>
          <div className="text-3xl font-black text-white font-mono">283</div>
          <p className="text-[11px] text-emerald-400 font-medium">↑ 14% vs yesterday</p>
        </div>

        <div className="glass-card p-5 rounded-2xl space-y-2 border border-slate-800">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold">
            <span>Avg Wait Time</span>
            <Clock className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-3xl font-black text-white font-mono">11.8 min</div>
          <p className="text-[11px] text-emerald-400 font-medium">↓ 2.4 min faster</p>
        </div>

        <div className="glass-card p-5 rounded-2xl space-y-2 border border-slate-800">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold">
            <span>No-Show Rate</span>
            <AlertTriangle className="w-4 h-4 text-rose-400" />
          </div>
          <div className="text-3xl font-black text-white font-mono">4.2%</div>
          <p className="text-[11px] text-slate-400">13 unfulfilled tokens</p>
        </div>

        <div className="glass-card p-5 rounded-2xl space-y-2 border border-slate-800">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold">
            <span>SLA Compliance (&lt;15m)</span>
            <ShieldCheck className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-3xl font-black text-emerald-400 font-mono">96.8%</div>
          <p className="text-[11px] text-emerald-400 font-medium">Target: &gt;95.0%</p>
        </div>
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Wait Time Trend Line Chart */}
        <div className="glass-card p-6 rounded-2xl space-y-4 border border-slate-800">
          <h2 className="text-base font-bold text-white flex items-center justify-between">
            <span>Average Wait Time Trend (Hourly)</span>
            <span className="text-xs text-slate-400 font-normal">Peak: 10:00 AM</span>
          </h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={waitTimeTrendData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="hour" stroke="#64748b" fontSize={11} />
                <YAxis stroke="#64748b" fontSize={11} />
                <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '8px', color: '#fff' }} />
                <Line type="monotone" dataKey="avgWaitMin" stroke="#38bdf8" strokeWidth={3} dot={{ r: 4, fill: '#38bdf8' }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Throughput Bar Chart */}
        <div className="glass-card p-6 rounded-2xl space-y-4 border border-slate-800">
          <h2 className="text-base font-bold text-white flex items-center justify-between">
            <span>Service Throughput vs No-Shows</span>
            <span className="text-xs text-slate-400 font-normal">By Department</span>
          </h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={serviceThroughputData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="service" stroke="#64748b" fontSize={11} />
                <YAxis stroke="#64748b" fontSize={11} />
                <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '8px', color: '#fff' }} />
                <Bar dataKey="completed" fill="#0284c7" radius={[4, 4, 0, 0]} />
                <Bar dataKey="noShow" fill="#f43f5e" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
