import React from 'react';
import { ShieldAlert, Sparkles, Clock, CheckCircle2 } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { cn } from '../../utils/cn';

export const ServiceCatalogCard = ({ service, isSelected, onSelect }) => {
  const isEmergency = service.name.toLowerCase().includes('emergency');
  const Icon = isEmergency ? ShieldAlert : Sparkles;

  return (
    <Card
      onClick={() => onSelect(service.id)}
      className={cn(
        'p-5 cursor-pointer transition-all duration-250 border glass-card-hover relative overflow-hidden',
        isSelected
          ? 'border-sky-500 bg-sky-500/10 shadow-lg shadow-sky-500/20 ring-2 ring-sky-500/50 border-l-4 border-l-sky-500'
          : 'border-slate-800 bg-slate-900/50 hover:border-slate-700'
      )}
    >
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-3.5">
          <div
            className={cn(
              'p-3 rounded-xl transition-all',
              isEmergency
                ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                : 'bg-sky-500/10 text-sky-400 border border-sky-500/20'
            )}
          >
            <Icon className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="font-bold text-white text-sm">{service.name}</h3>
              {isSelected && (
                <CheckCircle2 className="w-4 h-4 text-sky-400 animate-in fade-in zoom-in duration-200" />
              )}
            </div>
            <div className="flex items-center gap-3 text-xs text-slate-400 mt-1">
              <span className="flex items-center gap-1">
                <Clock className="w-3.5 h-3.5 text-slate-500" />
                ~{service.avgDurationMin || 10} min / customer
              </span>
            </div>
          </div>
        </div>

        <Badge variant={isEmergency ? 'warning' : 'info'} className="text-[10px] font-mono">
          Capacity: {service.dailyCapacity || 200}/day
        </Badge>
      </div>
    </Card>
  );
};
