import React, { useEffect, useState } from 'react';
import { UserCheck, CheckCircle, SkipForward, Clock, ShieldAlert, Activity } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';

export const CurrentCustomerCard = ({
  currentCustomer,
  onOpenCompleteModal,
  onOpenSkipModal,
  isPaused,
}) => {
  const [elapsedSeconds, setElapsedSeconds] = useState(0);

  useEffect(() => {
    if (currentCustomer) {
      setElapsedSeconds(0);
      const interval = setInterval(() => {
        setElapsedSeconds((prev) => prev + 1);
      }, 1000);
      return () => clearInterval(interval);
    }
  }, [currentCustomer]);

  const formatElapsed = (sec) => {
    const mins = Math.floor(sec / 60);
    const remainingSec = sec % 60;
    return `${String(mins).padStart(2, '0')}:${String(remainingSec).padStart(2, '0')}`;
  };

  if (!currentCustomer) {
    return (
      <Card className="p-8 text-center space-y-3 border-dashed border-slate-800 bg-slate-900/40">
        <div className="w-14 h-14 rounded-2xl bg-sky-500/10 border border-sky-500/20 text-sky-400 flex items-center justify-center mx-auto">
          <UserCheck className="w-7 h-7" />
        </div>
        <h3 className="text-sm font-bold text-slate-300">Counter is Currently Idle</h3>
        <p className="text-xs text-slate-500 max-w-xs mx-auto">
          Click "Call Next Customer" above to dispatch the next ticket based on Weighted Round-Robin fairness.
        </p>
      </Card>
    );
  }

  return (
    <Card className="p-6 space-y-6 border-slate-800 bg-slate-900/80 shadow-2xl relative overflow-hidden">
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div className="flex items-center gap-2 text-xs font-extrabold text-white">
          <span className="w-2.5 h-2.5 rounded-full bg-sky-400 animate-ping" />
          Currently Serving Customer
        </div>
        <Badge variant="warning" className="font-mono text-[10px]">
          {currentCustomer.status}
        </Badge>
      </div>

      {/* Serving Customer Hero Banner */}
      <div className="flex flex-col sm:flex-row items-center justify-between bg-slate-950 p-6 rounded-2xl border border-slate-800 gap-4 shadow-inner relative overflow-hidden">
        <div className="space-y-1 text-center sm:text-left">
          <span className="text-[10px] uppercase font-mono font-bold text-slate-500">Token Ticket</span>
          <div className="text-4xl font-black text-white font-mono">{currentCustomer.tokenNumber}</div>
          <p className="text-xs text-slate-400 font-semibold">{currentCustomer.serviceName}</p>
        </div>

        {/* Live Serving Elapsed Timer */}
        <div className="text-center sm:text-right space-y-1.5">
          <Badge variant="warning" className="text-xs px-3 py-1 font-semibold">
            Priority: {currentCustomer.priorityClass}
          </Badge>
          <div className="flex items-center justify-center sm:justify-end gap-1.5 text-xs text-sky-400 font-mono font-bold pt-1">
            <Clock className="w-3.5 h-3.5" />
            Service Elapsed: {formatElapsed(elapsedSeconds)}
          </div>
          <p className="text-xs text-slate-300 font-medium">
            Customer: <strong className="text-white">{currentCustomer.userName || 'John Doe'}</strong>
          </p>
        </div>
      </div>

      {/* Operator Action Buttons */}
      <div className="grid grid-cols-2 gap-4">
        <Button
          onClick={onOpenSkipModal}
          disabled={isPaused}
          variant="outline"
          className="py-3 text-xs font-semibold hover:border-amber-500/50 hover:text-amber-400 disabled:opacity-50"
        >
          <SkipForward className="w-4 h-4 mr-1.5 text-amber-400" />
          Skip (Absent)
        </Button>

        <Button
          onClick={onOpenCompleteModal}
          disabled={isPaused}
          variant="primary"
          className="py-3 text-xs font-bold bg-gradient-to-r from-emerald-500 to-teal-600 shadow-emerald-500/20 disabled:opacity-50"
        >
          <CheckCircle className="w-4 h-4 mr-1.5" />
          Complete Service
        </Button>
      </div>
    </Card>
  );
};
