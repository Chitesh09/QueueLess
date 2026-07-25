import React from 'react';
import { Clock, Users } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';

export const NextUpQueueList = ({ waitingList }) => {
  const defaultList = [
    { id: 101, tokenNumber: 'EME-001', serviceName: 'Emergency Triage & OPD', priorityClass: 'EMERGENCY', waitMinutes: 14 },
    { id: 102, tokenNumber: 'EME-002', serviceName: 'Emergency Triage & OPD', priorityClass: 'SENIOR', waitMinutes: 9 },
    { id: 103, tokenNumber: 'EME-003', serviceName: 'Emergency Triage & OPD', priorityClass: 'STANDARD', waitMinutes: 4 },
  ];

  const items = waitingList && waitingList.length > 0 ? waitingList : defaultList;

  return (
    <Card className="p-6 space-y-4 border-slate-800 bg-slate-900/60">
      <div className="flex items-center justify-between border-b border-slate-800 pb-3">
        <h2 className="text-sm font-bold text-white flex items-center gap-2">
          <span>Next Up in Queue</span>
        </h2>
        <span className="text-[10px] font-mono font-bold bg-slate-800 text-slate-300 px-2 py-0.5 rounded-full">
          {items.length} Waiting
        </span>
      </div>

      {items.length === 0 ? (
        <div className="p-6 text-center space-y-2 border border-dashed border-slate-800 rounded-xl">
          <Users className="w-8 h-8 text-slate-600 mx-auto" />
          <p className="text-xs font-semibold text-slate-400">No Customers Waiting</p>
          <p className="text-[11px] text-slate-500">Queue is currently clear.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {items.map((item) => (
            <div
              key={item.id}
              className="p-3.5 rounded-xl bg-slate-950/80 border border-slate-800 flex items-center justify-between hover:border-slate-700 transition-all"
            >
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-mono font-bold text-white text-xs">{item.tokenNumber}</span>
                  <Badge
                    variant={
                      item.priorityClass === 'EMERGENCY'
                        ? 'danger'
                        : item.priorityClass === 'SENIOR'
                        ? 'warning'
                        : 'info'
                    }
                    className="text-[9px] px-1.5 py-0"
                  >
                    {item.priorityClass}
                  </Badge>
                </div>
                <p className="text-[11px] text-slate-400 mt-0.5">{item.serviceName}</p>
              </div>

              <div className="text-right text-xs text-slate-400 flex items-center gap-1 font-mono">
                <Clock className="w-3 h-3 text-slate-500" />
                {item.waitMinutes || 5}m
              </div>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
};
