import React, { useState } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Volume2, VolumeX, Monitor, Clock, Building2, Bell } from 'lucide-react';

export const DisplayBoardPage = () => {
  const { isConnected } = useWebSocket(1);
  const [audioMuted, setAudioMuted] = useState(false);

  // Live TV Display Board Token Calls
  const calledTokens = [
    { tokenNumber: 'EME-001', serviceName: 'Emergency Triage & OPD', counterName: 'Counter 1', priorityClass: 'EMERGENCY' },
    { tokenNumber: 'CAR-004', serviceName: 'General Cardiology OPD', counterName: 'Counter 2', priorityClass: 'SENIOR' },
    { tokenNumber: 'DEP-012', serviceName: 'Cash & Deposit Desk', counterName: 'Counter 3', priorityClass: 'STANDARD' },
  ];

  const waitingTokens = [
    { tokenNumber: 'EME-002', serviceName: 'Emergency Triage & OPD', priorityClass: 'EMERGENCY', waitMinutes: 4 },
    { tokenNumber: 'EME-003', serviceName: 'Emergency Triage & OPD', priorityClass: 'STANDARD', waitMinutes: 7 },
    { tokenNumber: 'CAR-005', serviceName: 'General Cardiology OPD', priorityClass: 'APPOINTMENT', waitMinutes: 12 },
  ];

  const handleSimulateChime = () => {
    if (!audioMuted) {
      // Simulate audio announcement chime
      const audio = new Audio('https://assets.mixkit.co/active_storage/sfx/2869/2869-preview.mp3');
      audio.play().catch(() => {});
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-white p-6 space-y-8 font-['Plus_Jakarta_Sans',sans-serif]">
      {/* TV Display Board Header */}
      <div className="glass-card p-6 rounded-3xl border border-slate-800 flex flex-col md:flex-row items-center justify-between gap-4 bg-slate-900/80 shadow-2xl">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center text-white shadow-lg shadow-sky-500/20">
            <Building2 className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-2xl font-black tracking-tight text-white">City Care Hospital</h1>
            <p className="text-xs text-slate-400 font-mono">Public Waiting Room TV Display Board</p>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <button
            onClick={() => setAudioMuted(!audioMuted)}
            className="p-2.5 rounded-xl bg-slate-950 border border-slate-800 text-slate-400 hover:text-white flex items-center gap-2 text-xs font-mono"
          >
            {audioMuted ? <VolumeX className="w-4 h-4 text-rose-400" /> : <Volume2 className="w-4 h-4 text-emerald-400" />}
            {audioMuted ? 'Muted' : 'Audio Chime On'}
          </button>

          <div
            className={`flex items-center gap-2 px-3 py-1.5 rounded-xl text-xs font-mono font-bold border ${
              isConnected
                ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                : 'bg-amber-500/10 text-amber-400 border-amber-500/20'
            }`}
          >
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
            STOMP WebSocket: {isConnected ? 'LIVE' : 'CONNECTING'}
          </div>
        </div>
      </div>

      {/* Currently Called Hero Banner */}
      <div className="space-y-4">
        <h2 className="text-sm font-extrabold text-sky-400 uppercase tracking-widest flex items-center gap-2">
          <Bell className="w-4 h-4 text-sky-400 animate-bounce" />
          Now Serving — Proceed to Counter Immediately
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {calledTokens.map((t, idx) => (
            <Card
              key={idx}
              className="p-6 space-y-3 border-sky-500/40 bg-slate-900/90 shadow-2xl relative overflow-hidden ring-2 ring-sky-500/30 animate-in fade-in-80"
            >
              <div className="flex items-center justify-between border-b border-slate-800 pb-2">
                <span className="text-[10px] uppercase font-mono font-bold text-slate-400">{t.serviceName}</span>
                <Badge variant="warning" className="text-[9px]">
                  {t.priorityClass}
                </Badge>
              </div>

              <div className="text-center py-2">
                <span className="text-[10px] uppercase font-mono text-slate-500 block">Token Number</span>
                <div className="text-5xl font-black text-transparent bg-clip-text bg-gradient-to-r from-sky-400 to-indigo-300 font-mono tracking-tight">
                  {t.tokenNumber}
                </div>
              </div>

              <div className="bg-slate-950 p-3 rounded-xl border border-slate-800 text-center">
                <span className="text-[10px] uppercase font-mono text-slate-500 block">Assigned Counter</span>
                <div className="text-lg font-black text-emerald-400 font-mono">{t.counterName}</div>
              </div>
            </Card>
          ))}
        </div>
      </div>

      {/* Live Next-Up Waiting Queue */}
      <Card className="p-6 space-y-4 border-slate-800 bg-slate-900/60">
        <h2 className="text-sm font-extrabold text-slate-300 uppercase tracking-widest flex items-center gap-2">
          <Clock className="w-4 h-4 text-slate-400" />
          Next Up in Waiting Queue
        </h2>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 font-mono text-xs">
          {waitingTokens.map((w, idx) => (
            <div key={idx} className="p-4 bg-slate-950 rounded-2xl border border-slate-800 space-y-1">
              <div className="flex items-center justify-between">
                <span className="font-bold text-white text-base">{w.tokenNumber}</span>
                <Badge variant="info" className="text-[9px]">
                  {w.priorityClass}
                </Badge>
              </div>
              <p className="text-[11px] text-slate-400 font-sans">{w.serviceName}</p>
              <p className="text-[10px] text-sky-400 pt-1">Estimated Wait: ~{w.waitMinutes}m</p>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
};
