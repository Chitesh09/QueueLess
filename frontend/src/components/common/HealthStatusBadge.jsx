import React from 'react';
import { API_BASE_URL } from '../../constants/config';

export const HealthStatusBadge = () => {
  const displayHost = API_BASE_URL.replace(/^https?:\/\//, '');

  return (
    <div className="hidden sm:flex items-center gap-2 bg-slate-900/90 border border-slate-800 px-3 py-1.5 rounded-xl text-xs">
      <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
      <span className="text-slate-400">
        Backend: <strong className="text-slate-200 font-mono">{displayHost}</strong>
      </span>
    </div>
  );
};
