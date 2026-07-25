import React from 'react';
import { Building2, ShieldAlert, Cpu, Activity, Plus, CheckCircle2 } from 'lucide-react';

export default function SuperAdminPortal() {
  const tenants = [
    { id: 1, name: 'City Care Hospital', tier: 'ENTERPRISE', branches: 4, activeQueues: 18, status: 'ACTIVE' },
    { id: 2, name: 'Metro National Bank', tier: 'PRO', branches: 12, activeQueues: 36, status: 'ACTIVE' },
    { id: 3, name: 'State Licensing Authority', tier: 'ENTERPRISE', branches: 2, activeQueues: 8, status: 'ACTIVE' },
  ];

  return (
    <div className="max-w-6xl mx-auto space-y-8 py-6">
      <div className="glass-card p-6 rounded-2xl flex flex-col md:flex-row items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase font-bold text-indigo-400 bg-indigo-500/10 px-3 py-1 rounded-full">SaaS Platform Control</span>
          <h1 className="text-2xl font-extrabold text-white mt-1">Multi-Tenant Super Admin Console</h1>
          <p className="text-xs text-slate-400">Onboard organization tenants, manage plan tiers, and observe platform health</p>
        </div>
        <button className="px-4 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 font-bold text-xs text-white shadow-lg flex items-center gap-2">
          <Plus className="w-4 h-4" />
          Onboard New Tenant Organization
        </button>
      </div>

      {/* Tenants Table */}
      <div className="glass-card rounded-2xl p-6 border border-slate-800 space-y-4">
        <h2 className="text-base font-bold text-white flex items-center gap-2">
          <Building2 className="w-5 h-5 text-sky-400" />
          Registered Tenant Organizations
        </h2>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/80 text-slate-400 uppercase font-semibold border-b border-slate-800">
              <tr>
                <th className="p-3">Organization Name</th>
                <th className="p-3">Plan Tier</th>
                <th className="p-3">Branches</th>
                <th className="p-3">Active Queues</th>
                <th className="p-3">Tenant Isolation Status</th>
                <th className="p-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 text-slate-300">
              {tenants.map((t) => (
                <tr key={t.id} className="hover:bg-slate-900/40">
                  <td className="p-3 font-bold text-white">{t.name}</td>
                  <td className="p-3">
                    <span className="px-2.5 py-0.5 rounded-full font-mono text-[10px] font-bold bg-purple-500/10 text-purple-400 border border-purple-500/20">
                      {t.tier}
                    </span>
                  </td>
                  <td className="p-3 font-mono">{t.branches}</td>
                  <td className="p-3 font-mono">{t.activeQueues}</td>
                  <td className="p-3">
                    <span className="flex items-center gap-1.5 text-emerald-400 font-semibold">
                      <CheckCircle2 className="w-4 h-4" />
                      Enforced (Header + JWT)
                    </span>
                  </td>
                  <td className="p-3">
                    <button className="text-sky-400 hover:text-sky-300 font-semibold">Manage Config</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
