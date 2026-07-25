import React from 'react';
import { Building2, ShieldCheck, Globe, MapPin, Layers, MonitorPlay } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';

export const OrganizationHeaderCard = ({ organization, branchCount = 2, departmentCount = 4, counterCount = 6 }) => {
  return (
    <Card className="p-6 md:p-8 space-y-6 border-slate-800 bg-slate-900/60 relative overflow-hidden">
      <div className="absolute -right-10 -bottom-10 w-48 h-48 bg-sky-500/10 rounded-full blur-3xl" />
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 relative z-10">
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center text-white shadow-lg shadow-sky-500/20">
            <Building2 className="w-7 h-7" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-black text-white tracking-tight">
                {organization?.name || 'City Care Hospital'}
              </h1>
              <Badge variant="success" className="text-[10px] uppercase font-mono">
                {organization?.tenantCode || 'CITY_CARE'}
              </Badge>
            </div>
            <p className="text-xs text-slate-400 mt-1 flex items-center gap-2">
              <Globe className="w-3.5 h-3.5 text-sky-400" />
              Domain: <span className="text-slate-200 font-mono">{organization?.domain || 'cityhospital.queueless.com'}</span>
            </p>
          </div>
        </div>

        {/* Enterprise KPI Metrics */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 w-full md:w-auto font-mono text-xs">
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 text-center">
            <span className="text-[9px] text-slate-500 uppercase font-bold block">Branches</span>
            <div className="text-base font-black text-white">{branchCount}</div>
          </div>

          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 text-center">
            <span className="text-[9px] text-slate-500 uppercase font-bold block">Departments</span>
            <div className="text-base font-black text-purple-400">{departmentCount}</div>
          </div>

          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 text-center">
            <span className="text-[9px] text-slate-500 uppercase font-bold block">Counters</span>
            <div className="text-base font-black text-emerald-400">{counterCount}</div>
          </div>

          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 text-center">
            <span className="text-[9px] text-slate-500 uppercase font-bold block">Subscription</span>
            <div className="text-[11px] font-black text-sky-400">ENTERPRISE</div>
          </div>
        </div>
      </div>
    </Card>
  );
};
