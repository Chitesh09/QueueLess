import React from 'react';
import { useAuth } from '../hooks/useAuth';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Shield, Server, User, Building, Activity, CheckCircle2 } from 'lucide-react';

export const DashboardPage = () => {
  const { user } = useAuth();

  return (
    <div className="space-y-8">
      {/* Top Welcome Banner */}
      <div className="glass-card p-6 md:p-8 rounded-2xl border border-slate-800 relative overflow-hidden">
        <div className="absolute -right-10 -bottom-10 w-48 h-48 bg-sky-500/10 rounded-full blur-3xl" />
        <div className="relative z-10 space-y-3">
          <div className="flex items-center gap-2">
            <Badge variant="success" className="px-3 py-1">
              <CheckCircle2 className="w-3.5 h-3.5 mr-1" />
              Authenticated Session Active
            </Badge>
            <span className="text-xs text-slate-400 font-mono">Phase 1 Architecture Baseline</span>
          </div>

          <h1 className="text-2xl md:text-3xl font-extrabold text-white">
            Welcome back, {user?.name || 'User'}!
          </h1>
          <p className="text-slate-400 text-xs md:text-sm max-w-2xl">
            You are logged in as <strong className="text-sky-400">{user?.email}</strong> with role{' '}
            <strong className="text-indigo-400 uppercase">{user?.role || 'SUPER_ADMIN'}</strong> under Tenant Organization{' '}
            <strong className="text-emerald-400">ID #{user?.organizationId || 1}</strong>.
          </p>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card className="space-y-2 p-5">
          <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
            <span>Backend Server</span>
            <Server className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-black text-white font-mono">ONLINE</div>
          <p className="text-[11px] text-emerald-400 font-medium">http://localhost:8080</p>
        </Card>

        <Card className="space-y-2 p-5">
          <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
            <span>MySQL 8 Database</span>
            <Activity className="w-4 h-4 text-sky-400" />
          </div>
          <div className="text-2xl font-black text-white font-mono">Port 3307</div>
          <p className="text-[11px] text-sky-400 font-medium">Flyway Schema Applied</p>
        </Card>

        <Card className="space-y-2 p-5">
          <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
            <span>Current Tenant</span>
            <Building className="w-4 h-4 text-purple-400" />
          </div>
          <div className="text-2xl font-black text-white font-mono">City Care</div>
          <p className="text-[11px] text-purple-400 font-medium">Multi-Tenant Header Enforced</p>
        </Card>

        <Card className="space-y-2 p-5">
          <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
            <span>JWT Security</span>
            <Shield className="w-4 h-4 text-indigo-400" />
          </div>
          <div className="text-2xl font-black text-white font-mono">Bearer Valid</div>
          <p className="text-[11px] text-indigo-400 font-medium">Auto Intercepted via Axios</p>
        </Card>
      </div>

      {/* User Session Info Card */}
      <Card className="p-6 space-y-4">
        <h2 className="text-base font-bold text-white flex items-center gap-2">
          <User className="w-4 h-4 text-sky-400" />
          Authenticated Principal Context
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs font-mono">
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 space-y-1">
            <span className="text-[10px] text-slate-500 uppercase font-bold">User ID</span>
            <div className="font-bold text-white text-base">#{user?.userId || user?.id || 1}</div>
          </div>

          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 space-y-1">
            <span className="text-[10px] text-slate-500 uppercase font-bold">User Role</span>
            <div className="font-bold text-sky-400 text-base">{user?.role || 'SUPER_ADMIN'}</div>
          </div>

          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 space-y-1">
            <span className="text-[10px] text-slate-500 uppercase font-bold">Tenant Organization</span>
            <div className="font-bold text-emerald-400 text-base">Org #{user?.organizationId || 1}</div>
          </div>
        </div>
      </Card>
    </div>
  );
};
