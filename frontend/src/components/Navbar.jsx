import React from 'react';
import { NavLink } from 'react-router-dom';
import { Users, MonitorPlay, BarChart3, ShieldCheck, Layers, RefreshCw } from 'lucide-react';

export default function Navbar({ activeRole, setActiveRole }) {
  return (
    <nav className="border-b border-slate-800 bg-slate-900/80 backdrop-blur-md sticky top-0 z-50 px-4 lg:px-8 py-3">
      <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
        {/* Brand */}
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-sky-500/20">
            <Layers className="w-6 h-6 text-white" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-extrabold text-xl tracking-tight text-white">QueueLess</span>
              <span className="text-[10px] uppercase font-bold tracking-widest px-2 py-0.5 rounded-full bg-sky-500/10 text-sky-400 border border-sky-500/20">SaaS Platform</span>
            </div>
            <p className="text-xs text-slate-400">Smart Virtual Queue Engine</p>
          </div>
        </div>

        {/* Portals Navigation */}
        <div className="flex items-center gap-1 bg-slate-950 p-1 rounded-xl border border-slate-800/80">
          <NavLink
            to="/"
            className={({ isActive }) =>
              `flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                isActive ? 'bg-sky-600 text-white shadow-md shadow-sky-600/30' : 'text-slate-400 hover:text-white hover:bg-slate-900'
              }`
            }
          >
            <Users className="w-4 h-4" />
            Customer View
          </NavLink>

          <NavLink
            to="/operator"
            className={({ isActive }) =>
              `flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                isActive ? 'bg-sky-600 text-white shadow-md shadow-sky-600/30' : 'text-slate-400 hover:text-white hover:bg-slate-900'
              }`
            }
          >
            <MonitorPlay className="w-4 h-4" />
            Operator Portal
          </NavLink>

          <NavLink
            to="/admin"
            className={({ isActive }) =>
              `flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                isActive ? 'bg-sky-600 text-white shadow-md shadow-sky-600/30' : 'text-slate-400 hover:text-white hover:bg-slate-900'
              }`
            }
          >
            <BarChart3 className="w-4 h-4" />
            Branch Analytics
          </NavLink>

          <NavLink
            to="/superadmin"
            className={({ isActive }) =>
              `flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                isActive ? 'bg-sky-600 text-white shadow-md shadow-sky-600/30' : 'text-slate-400 hover:text-white hover:bg-slate-900'
              }`
            }
          >
            <ShieldCheck className="w-4 h-4" />
            Super Admin
          </NavLink>
        </div>

        {/* Tenant Indicator & Connection Badge */}
        <div className="flex items-center gap-3 text-xs">
          <div className="flex items-center gap-2 bg-slate-950 px-3 py-1.5 rounded-lg border border-slate-800">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
            <span className="text-slate-300 font-medium">Tenant: <strong className="text-white">City Care Hospital</strong></span>
          </div>
        </div>
      </div>
    </nav>
  );
}
