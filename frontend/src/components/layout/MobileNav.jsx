import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Users, MonitorPlay, Building2, BarChart3, Settings, Tv, Layers } from 'lucide-react';
import { Sheet } from '../ui/Sheet';

export const MobileNav = ({ isOpen, onClose }) => {
  return (
    <Sheet isOpen={isOpen} onClose={onClose} title="QueueLess SaaS">
      <div className="space-y-4">
        <div className="flex items-center gap-3 p-2 bg-slate-950 rounded-xl border border-slate-800">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center">
            <Layers className="w-4 h-4 text-white" />
          </div>
          <div>
            <div className="font-bold text-sm text-white">QueueLess SaaS</div>
            <p className="text-[10px] text-slate-400">Phase 7 WebSocket Engine</p>
          </div>
        </div>

        <div className="space-y-1">
          <NavLink
            to="/dashboard"
            onClick={onClose}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                isActive ? 'bg-sky-600 text-white' : 'text-slate-400 hover:text-white hover:bg-slate-800'
              }`
            }
          >
            <LayoutDashboard className="w-4 h-4" />
            Dashboard Overview
          </NavLink>

          <NavLink
            to="/queue"
            onClick={onClose}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                isActive ? 'bg-sky-600 text-white' : 'text-slate-400 hover:text-white hover:bg-slate-800'
              }`
            }
          >
            <Users className="w-4 h-4" />
            Queue Management
          </NavLink>

          <NavLink
            to="/counter"
            onClick={onClose}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                isActive ? 'bg-sky-600 text-white' : 'text-slate-400 hover:text-white hover:bg-slate-800'
              }`
            }
          >
            <MonitorPlay className="w-4 h-4" />
            Operator Console
          </NavLink>

          <NavLink
            to="/organization"
            onClick={onClose}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                isActive ? 'bg-sky-600 text-white' : 'text-slate-400 hover:text-white hover:bg-slate-800'
              }`
            }
          >
            <Building2 className="w-4 h-4" />
            Organization &amp; Branches
          </NavLink>

          <NavLink
            to="/analytics"
            onClick={onClose}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                isActive ? 'bg-sky-600 text-white' : 'text-slate-400 hover:text-white hover:bg-slate-800'
              }`
            }
          >
            <BarChart3 className="w-4 h-4" />
            Analytics &amp; Insights
          </NavLink>

          <NavLink
            to="/display-board"
            onClick={onClose}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                isActive ? 'bg-sky-600 text-white' : 'text-slate-400 hover:text-white hover:bg-slate-800'
              }`
            }
          >
            <Tv className="w-4 h-4" />
            TV Display Board
          </NavLink>

          <NavLink
            to="/settings"
            onClick={onClose}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                isActive ? 'bg-sky-600 text-white' : 'text-slate-400 hover:text-white hover:bg-slate-800'
              }`
            }
          >
            <Settings className="w-4 h-4" />
            Settings &amp; Profile
          </NavLink>
        </div>
      </div>
    </Sheet>
  );
};
