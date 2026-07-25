import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Users, MonitorPlay, Building2, BarChart3, Settings, Tv, Layers } from 'lucide-react';

export const Sidebar = () => {
  const navItems = [
    { label: 'Dashboard Overview', icon: LayoutDashboard, path: '/dashboard' },
    { label: 'Queue Management', icon: Users, path: '/queue' },
    { label: 'Operator Console', icon: MonitorPlay, path: '/counter' },
    { label: 'Organization & Branches', icon: Building2, path: '/organization' },
    { label: 'Analytics & Insights', icon: BarChart3, path: '/analytics' },
    { label: 'TV Display Board', icon: Tv, path: '/display-board' },
    { label: 'Settings & Profile', icon: Settings, path: '/settings' },
  ];

  return (
    <aside className="w-64 border-r border-slate-800 bg-slate-950/60 backdrop-blur-md p-4 hidden md:flex flex-col justify-between shrink-0 min-h-[calc(100vh-61px)]">
      <div className="space-y-6">
        {/* Brand Logo */}
        <div className="flex items-center gap-3 px-2 py-1">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-sky-500/20">
            <Layers className="w-5 h-5 text-white" />
          </div>
          <div>
            <div className="font-extrabold text-base tracking-tight text-white">QueueLess</div>
            <p className="text-[10px] text-slate-400">Enterprise Virtual Queue</p>
          </div>
        </div>

        {/* Navigation Section */}
        <div className="space-y-1">
          <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider px-3">Main Navigation</span>
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                    isActive
                      ? 'bg-sky-600 text-white shadow-md shadow-sky-600/30'
                      : 'text-slate-400 hover:text-white hover:bg-slate-900'
                  }`
                }
              >
                <Icon className="w-4 h-4" />
                {item.label}
              </NavLink>
            );
          })}
        </div>
      </div>

      {/* Footer Info */}
      <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800 text-[11px] text-slate-400 space-y-1">
        <p className="font-bold text-slate-200">Phase 7 Enabled</p>
        <p className="text-[10px] text-slate-500">Real-Time STOMP WebSocket</p>
      </div>
    </aside>
  );
};
