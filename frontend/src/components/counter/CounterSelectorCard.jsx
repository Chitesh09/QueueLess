import React from 'react';
import { MonitorPlay, PauseCircle, PlayCircle, AlertTriangle } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';

export const CounterSelectorCard = ({
  counters,
  selectedCounterId,
  onSelectCounter,
  counterStatus,
  onTogglePause,
  isPauseLoading,
}) => {
  const isPaused = counterStatus === 'PAUSED';

  return (
    <div className="space-y-4">
      <Card className="p-6 flex flex-col md:flex-row items-center justify-between gap-4 border-slate-800 bg-slate-900/60">
        <div className="flex items-center gap-3.5">
          <div className="p-3 rounded-2xl bg-sky-500/10 text-sky-400 border border-sky-500/20">
            <MonitorPlay className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-extrabold text-white">Counter Operator Console</h1>
              <Badge variant={isPaused ? 'warning' : 'success'} className="font-mono text-[10px]">
                {counterStatus}
              </Badge>
            </div>
            <p className="text-xs text-slate-400">
              Weighted Round-Robin Fairness Dispatcher &amp; Shift Management
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3 w-full md:w-auto">
          <select
            value={selectedCounterId}
            onChange={(e) => onSelectCounter(Number(e.target.value))}
            className="w-full md:w-auto bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs font-bold text-white focus:outline-none focus:border-sky-500"
          >
            {counters?.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name} ({c.status})
              </option>
            ))}
          </select>

          <Button
            onClick={onTogglePause}
            isLoading={isPauseLoading}
            variant={isPaused ? 'primary' : 'outline'}
            size="md"
            className="whitespace-nowrap font-bold"
          >
            {isPaused ? (
              <>
                <PlayCircle className="w-4 h-4 mr-1.5" />
                Resume Shift
              </>
            ) : (
              <>
                <PauseCircle className="w-4 h-4 mr-1.5 text-amber-400" />
                Pause Counter
              </>
            )}
          </Button>
        </div>
      </Card>

      {/* Paused Shift Warning Banner */}
      {isPaused && (
        <div className="p-3.5 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-300 text-xs font-medium flex items-center gap-2 animate-in fade-in-50">
          <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0" />
          <span>
            Shift is currently <strong>PAUSED</strong>. Operator actions ("Call Next Customer") are disabled until shift is resumed.
          </span>
        </div>
      )}
    </div>
  );
};
