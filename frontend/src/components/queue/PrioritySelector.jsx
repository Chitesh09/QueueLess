import React from 'react';
import { User, HeartHandshake, CalendarCheck, ShieldAlert, CheckCircle2 } from 'lucide-react';
import { cn } from '../../utils/cn';

export const PrioritySelector = ({ value, onChange }) => {
  const options = [
    {
      id: 'STANDARD',
      title: 'Standard Patient',
      weight: 'Weight: 10',
      color: 'emerald',
      badgeColor: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
      activeBorder: 'border-emerald-500 ring-2 ring-emerald-500/40 bg-emerald-500/10 border-l-4 border-l-emerald-500',
      icon: User,
    },
    {
      id: 'SENIOR',
      title: 'Senior Citizen / PwD',
      weight: 'Weight: 40',
      color: 'amber',
      badgeColor: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
      activeBorder: 'border-amber-500 ring-2 ring-amber-500/40 bg-amber-500/10 border-l-4 border-l-amber-500',
      icon: HeartHandshake,
    },
    {
      id: 'APPOINTMENT',
      title: 'Pre-Booked Appointment',
      weight: 'Weight: 20',
      color: 'purple',
      badgeColor: 'bg-purple-500/10 text-purple-400 border-purple-500/20',
      activeBorder: 'border-purple-500 ring-2 ring-purple-500/40 bg-purple-500/10 border-l-4 border-l-purple-500',
      icon: CalendarCheck,
    },
    {
      id: 'EMERGENCY',
      title: 'Emergency Triage',
      weight: 'Weight: 100',
      color: 'rose',
      badgeColor: 'bg-rose-500/10 text-rose-400 border-rose-500/20',
      activeBorder: 'border-rose-500 ring-2 ring-rose-500/40 bg-rose-500/10 border-l-4 border-l-rose-500',
      icon: ShieldAlert,
    },
  ];

  return (
    <div className="space-y-2">
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {options.map((opt) => {
          const Icon = opt.icon;
          const isSelected = value === opt.id;
          return (
            <div
              key={opt.id}
              onClick={() => onChange(opt.id)}
              className={cn(
                'p-3.5 rounded-xl border cursor-pointer transition-all duration-200 flex items-center justify-between',
                isSelected
                  ? opt.activeBorder
                  : 'border-slate-800 bg-slate-950/60 hover:border-slate-700'
              )}
            >
              <div className="flex items-center gap-3">
                <div className={cn('p-2 rounded-lg border', opt.badgeColor)}>
                  <Icon className="w-4 h-4" />
                </div>
                <div>
                  <div className="flex items-center gap-1.5">
                    <span className="text-xs font-bold text-white">{opt.title}</span>
                    {isSelected && (
                      <CheckCircle2 className="w-3.5 h-3.5 text-sky-400 animate-in fade-in zoom-in" />
                    )}
                  </div>
                  <span className="text-[10px] font-mono text-slate-400">{opt.weight}</span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
