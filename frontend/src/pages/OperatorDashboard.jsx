import React, { useState } from 'react';
import axios from 'axios';
import { PhoneCall, CheckCircle, SkipForward, PauseCircle, PlayCircle, UserCheck, AlertTriangle, Clock } from 'lucide-react';

export default function OperatorDashboard() {
  const [selectedCounter, setSelectedCounter] = useState(1); // 1: Counter 1 (Emergency)
  const [counterStatus, setCounterStatus] = useState('ONLINE');
  const [currentCustomer, setCurrentCustomer] = useState(null);
  const [loading, setLoading] = useState(false);

  const mockWaitingQueue = [
    { id: 101, tokenNumber: 'EME-001', serviceName: 'Emergency Triage & OPD', priorityClass: 'EMERGENCY', waitMinutes: 14 },
    { id: 102, tokenNumber: 'EME-002', serviceName: 'Emergency Triage & OPD', priorityClass: 'SENIOR', waitMinutes: 9 },
    { id: 103, tokenNumber: 'EME-003', serviceName: 'Emergency Triage & OPD', priorityClass: 'STANDARD', waitMinutes: 4 },
  ];

  const callNext = async () => {
    setLoading(true);
    try {
      const res = await axios.post(`/api/v1/counters/${selectedCounter}/call-next`);
      if (res.data && res.data.data) {
        setCurrentCustomer(res.data.data);
      } else {
        alert("No waiting tokens in queue!");
      }
    } catch (err) {
      alert("Call next failed: " + (err.response?.data?.detail || err.message));
    } finally {
      setLoading(false);
    }
  };

  const completeService = async () => {
    if (!currentCustomer) return;
    try {
      await axios.post(`/api/v1/counters/${selectedCounter}/complete/${currentCustomer.id}`);
      setCurrentCustomer(null);
    } catch (err) {
      alert("Complete service failed: " + err.message);
    }
  };

  const skipToken = async () => {
    if (!currentCustomer) return;
    try {
      await axios.post(`/api/v1/counters/${selectedCounter}/skip/${currentCustomer.id}`);
      setCurrentCustomer(null);
    } catch (err) {
      alert("Skip token failed: " + err.message);
    }
  };

  const togglePause = async () => {
    const action = counterStatus === 'ONLINE' ? 'pause' : 'resume';
    try {
      await axios.post(`/api/v1/counters/${selectedCounter}/${action}`);
      setCounterStatus(counterStatus === 'ONLINE' ? 'PAUSED' : 'ONLINE');
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="max-w-6xl mx-auto space-y-8 py-6">
      {/* Header Controls */}
      <div className="glass-card p-6 rounded-2xl flex flex-col md:flex-row items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase font-bold text-sky-400 bg-sky-500/10 px-3 py-1 rounded-full">Front-line Staff Portal</span>
          <h1 className="text-2xl font-extrabold text-white mt-1">Counter Operator Console</h1>
          <p className="text-xs text-slate-400">Weighted Round-Robin Fairness Queue Dispatcher</p>
        </div>

        <div className="flex items-center gap-3">
          <select
            value={selectedCounter}
            onChange={(e) => setSelectedCounter(Number(e.target.value))}
            className="bg-slate-900 border border-slate-800 rounded-xl px-4 py-2.5 text-sm font-semibold text-white focus:outline-none focus:border-sky-500"
          >
            <option value={1}>Counter 1 (Emergency Triage)</option>
            <option value={2}>Counter 2 (General OPD)</option>
          </select>

          <button
            onClick={togglePause}
            className={`px-4 py-2.5 rounded-xl font-bold text-xs flex items-center gap-2 border transition-all ${
              counterStatus === 'ONLINE'
                ? 'bg-amber-500/10 border-amber-500/30 text-amber-400 hover:bg-amber-500/20'
                : 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400 hover:bg-emerald-500/20'
            }`}
          >
            {counterStatus === 'ONLINE' ? <PauseCircle className="w-4 h-4" /> : <PlayCircle className="w-4 h-4" />}
            {counterStatus === 'ONLINE' ? 'Pause Counter' : 'Resume Shift'}
          </button>
        </div>
      </div>

      {/* Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Call Next & Current Serving Hero (2 cols) */}
        <div className="lg:col-span-2 space-y-6">
          {/* Main Action Bar */}
          <button
            onClick={callNext}
            disabled={loading || counterStatus === 'PAUSED'}
            className="w-full py-5 rounded-2xl bg-gradient-to-r from-sky-500 via-indigo-600 to-purple-600 font-extrabold text-white text-lg shadow-xl shadow-sky-500/20 hover:scale-[1.01] transition-all flex items-center justify-center gap-3 disabled:opacity-50"
          >
            <PhoneCall className="w-6 h-6 animate-bounce" />
            {loading ? 'Calling Next Customer...' : 'Call Next Customer (WRR Fairness Engine)'}
          </button>

          {/* Current Serving Customer Hero Card */}
          <div className="glass-card p-6 rounded-2xl space-y-6 border border-slate-800">
            <div className="flex items-center justify-between border-b border-slate-800/80 pb-4">
              <h2 className="text-base font-bold text-white flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-sky-400 animate-ping"></span>
                Currently Serving Customer
              </h2>
              {currentCustomer && (
                <span className="text-xs font-mono font-bold px-3 py-1 rounded-full bg-sky-500/10 text-sky-400 border border-sky-500/20">
                  {currentCustomer.status}
                </span>
              )}
            </div>

            {currentCustomer ? (
              <div className="space-y-6">
                <div className="flex flex-col md:flex-row items-center justify-between bg-slate-950 p-6 rounded-xl border border-slate-800 gap-4">
                  <div className="space-y-1 text-center md:text-left">
                    <span className="text-xs uppercase font-bold text-slate-400 tracking-wider">Serving Token</span>
                    <div className="text-4xl font-black text-white font-mono">{currentCustomer.tokenNumber}</div>
                    <p className="text-xs text-slate-400">{currentCustomer.serviceName}</p>
                  </div>

                  <div className="text-right space-y-1">
                    <span className="text-xs font-semibold px-2.5 py-1 rounded-md bg-amber-500/10 text-amber-400 border border-amber-500/20">
                      Priority: {currentCustomer.priorityClass}
                    </span>
                    <p className="text-xs text-slate-400 mt-2">Customer: {currentCustomer.userName || 'John Doe'}</p>
                  </div>
                </div>

                {/* Operator Actions */}
                <div className="grid grid-cols-2 gap-4">
                  <button
                    onClick={skipToken}
                    className="py-3 rounded-xl bg-slate-900 hover:bg-slate-800 border border-slate-800 text-slate-300 font-semibold text-sm flex items-center justify-center gap-2 transition-all"
                  >
                    <SkipForward className="w-4 h-4 text-amber-400" />
                    Skip (Absent)
                  </button>

                  <button
                    onClick={completeService}
                    className="py-3 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-sm flex items-center justify-center gap-2 shadow-lg shadow-emerald-600/20 transition-all"
                  >
                    <CheckCircle className="w-4 h-4" />
                    Complete Service
                  </button>
                </div>
              </div>
            ) : (
              <div className="h-48 flex flex-col items-center justify-center text-center p-6 border-2 border-dashed border-slate-800 rounded-xl space-y-2">
                <UserCheck className="w-8 h-8 text-slate-600" />
                <p className="text-slate-400 text-sm font-medium">Counter is idle.</p>
                <p className="text-xs text-slate-500">Click "Call Next Customer" above to dispatch the next ticket.</p>
              </div>
            )}
          </div>
        </div>

        {/* Live Waiting Queue Sidebar (1 col) */}
        <div className="glass-card p-6 rounded-2xl space-y-4">
          <h2 className="text-base font-bold text-white flex items-center justify-between">
            <span>Next Up in Queue</span>
            <span className="text-xs bg-slate-800 text-slate-300 px-2 py-0.5 rounded-full font-mono font-semibold">
              3 Tokens
            </span>
          </h2>

          <div className="space-y-3">
            {mockWaitingQueue.map((item) => (
              <div key={item.id} className="p-3.5 rounded-xl bg-slate-900/60 border border-slate-800 flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-mono font-bold text-white text-sm">{item.tokenNumber}</span>
                    <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-sky-500/10 text-sky-400">
                      {item.priorityClass}
                    </span>
                  </div>
                  <p className="text-xs text-slate-400 mt-0.5">{item.serviceName}</p>
                </div>
                <div className="text-right text-xs text-slate-400 flex items-center gap-1">
                  <Clock className="w-3.5 h-3.5 text-slate-500" />
                  {item.waitMinutes}m
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
