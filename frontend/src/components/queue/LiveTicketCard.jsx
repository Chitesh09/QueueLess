import React, { useEffect, useState } from 'react';
import { Clock, QrCode, UserCheck, RefreshCw, Activity, Layers } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';

export const LiveTicketCard = ({ token, onOpenQR, onCheckIn, isCheckInLoading }) => {
  const [lastUpdated, setLastUpdated] = useState('Just now');

  useEffect(() => {
    if (token) {
      setLastUpdated('Just now');
      const timer = setInterval(() => {
        setLastUpdated('Updated less than 1m ago');
      }, 30000);
      return () => clearInterval(timer);
    }
  }, [token]);

  if (!token) {
    return (
      <Card className="p-8 text-center space-y-4 border-dashed border-slate-800 bg-slate-900/40">
        <div className="w-14 h-14 rounded-2xl bg-sky-500/10 border border-sky-500/20 text-sky-400 flex items-center justify-center mx-auto">
          <Layers className="w-7 h-7" />
        </div>
        <div className="space-y-1">
          <h3 className="text-sm font-bold text-white">No Active Virtual Ticket</h3>
          <p className="text-xs text-slate-400 max-w-xs mx-auto">
            Select a service and priority tier on the left to issue your digital token claim.
          </p>
        </div>
      </Card>
    );
  }

  const position = token.queuePosition || 1;
  const progressPercent = Math.max(10, Math.min(100, (1 / position) * 100));

  const getStatusBadge = (status) => {
    switch (status) {
      case 'CALLED':
        return (
          <span className="px-3 py-1 rounded-full text-xs font-mono font-bold bg-amber-500/20 text-amber-300 border border-amber-500/40 animate-pulse shadow-lg shadow-amber-500/20">
            CALLED — PROCEED TO COUNTER
          </span>
        );
      case 'CHECKED_IN':
        return (
          <span className="px-3 py-1 rounded-full text-xs font-mono font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/40">
            CHECKED IN AT COUNTER
          </span>
        );
      case 'COMPLETED':
        return (
          <span className="px-3 py-1 rounded-full text-xs font-mono font-bold bg-slate-800 text-slate-300 border border-slate-700">
            SERVICE COMPLETED
          </span>
        );
      default:
        return (
          <span className="px-3 py-1 rounded-full text-xs font-mono font-bold bg-sky-500/10 text-sky-400 border border-sky-500/20 animate-pulse">
            WAITING IN QUEUE
          </span>
        );
    }
  };

  return (
    <Card className="p-6 space-y-6 border-slate-800 relative overflow-hidden bg-slate-900/80 shadow-2xl backdrop-blur-md">
      {/* Header Info */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div className="flex items-center gap-2 text-xs font-extrabold text-slate-400">
          <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-ping" />
          Live Digital Ticket
        </div>
        {getStatusBadge(token.status)}
      </div>

      {/* Airport Boarding Pass Centerpiece */}
      <div className="bg-gradient-to-b from-slate-950 to-slate-900 p-6 rounded-2xl border border-slate-800/80 text-center space-y-2 shadow-inner relative overflow-hidden">
        <div className="absolute top-0 right-0 p-3 opacity-10">
          <Activity className="w-24 h-24 text-sky-400" />
        </div>

        <span className="text-[10px] uppercase font-mono font-bold text-slate-500 tracking-widest">
          Token Claim Ticket
        </span>
        <div className="text-5xl font-black text-transparent bg-clip-text bg-gradient-to-r from-sky-400 via-indigo-300 to-sky-200 tracking-tight font-mono py-1">
          {token.tokenNumber}
        </div>
        <p className="text-xs font-bold text-slate-300">{token.serviceName}</p>
        <span className="inline-block text-[10px] font-mono text-slate-400 bg-slate-900 px-2.5 py-0.5 rounded-full border border-slate-800">
          Priority: {token.priorityClass}
        </span>
      </div>

      {/* Queue Position Progress Bar */}
      <div className="space-y-2 bg-slate-950 p-4 rounded-xl border border-slate-800">
        <div className="flex items-center justify-between text-xs">
          <span className="font-semibold text-slate-300">
            You are <strong className="text-sky-400 font-mono">#{position}</strong> in the queue
          </span>
          <span className="text-[10px] text-slate-500 font-mono">{lastUpdated}</span>
        </div>

        {/* Progress Bar */}
        <div className="w-full h-2 bg-slate-900 rounded-full overflow-hidden border border-slate-800">
          <div
            className="h-full bg-gradient-to-r from-sky-500 to-indigo-500 transition-all duration-500 rounded-full"
            style={{ width: `${progressPercent}%` }}
          />
        </div>
      </div>

      {/* Dynamic ETA */}
      <div className="grid grid-cols-2 gap-3 text-center">
        <div className="bg-slate-950 p-3.5 rounded-xl border border-slate-800">
          <span className="text-[10px] text-slate-500 uppercase font-bold">Estimated Wait</span>
          <div className="text-xl font-bold text-sky-400 mt-0.5 flex items-center justify-center gap-1 font-mono">
            <Clock className="w-4 h-4 text-sky-400" />
            ≈ {token.estimatedWaitMinutes || 8} min
          </div>
        </div>

        <div className="bg-slate-950 p-3.5 rounded-xl border border-slate-800">
          <span className="text-[10px] text-slate-500 uppercase font-bold">Assigned Counter</span>
          <div className="text-xl font-bold text-white mt-0.5 font-mono">
            {token.counterName || 'Pending'}
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="space-y-3 pt-1">
        <Button
          onClick={onOpenQR}
          variant="secondary"
          className="w-full flex items-center justify-center gap-2 text-xs"
        >
          <QrCode className="w-4 h-4 text-sky-400" />
          View Digital Boarding Pass (HMAC QR)
        </Button>

        {token.status === 'CALLED' && (
          <Button
            onClick={onCheckIn}
            isLoading={isCheckInLoading}
            variant="primary"
            className="w-full py-3 text-sm font-bold pulse-glow bg-gradient-to-r from-emerald-500 to-teal-600 shadow-emerald-500/30"
          >
            <UserCheck className="w-5 h-5 mr-2" />
            Check-in at Counter Now
          </Button>
        )}
      </div>
    </Card>
  );
};
