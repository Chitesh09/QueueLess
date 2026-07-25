import React from 'react';
import { MapPin, MonitorPlay, Layers, Plus } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';

export const BranchCard = ({ branch, onAddCounter }) => {
  return (
    <Card className="p-6 space-y-4 border-slate-800 bg-slate-900/40 glass-card-hover">
      <div className="flex items-center justify-between border-b border-slate-800 pb-3">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center">
            <MapPin className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-bold text-white text-sm">{branch.name}</h3>
            <p className="text-[11px] text-slate-400 font-mono">{branch.code || 'MAIN-01'}</p>
          </div>
        </div>

        <Badge variant="info" className="text-[10px]">
          {branch.activeCountersCount || 4} Counters Active
        </Badge>
      </div>

      <p className="text-xs text-slate-400">{branch.address || '100 Medical Center Way, Metropolitan City'}</p>

      <div className="flex items-center justify-between pt-2 border-t border-slate-800/80">
        <span className="text-[11px] text-slate-500 font-mono">Status: ACTIVE OPERATIONAL</span>
        <Button
          onClick={() => onAddCounter(branch.id)}
          variant="outline"
          size="sm"
          className="text-xs font-semibold hover:border-sky-500/50 hover:text-sky-400"
        >
          <Plus className="w-3.5 h-3.5 mr-1" />
          Add Counter Station
        </Button>
      </div>
    </Card>
  );
};
