import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { QRCodeSVG } from 'qrcode.react';
import { Clock, QrCode, ShieldAlert, Sparkles, CheckCircle2, UserCheck, AlertCircle, RefreshCw } from 'lucide-react';

export default function CustomerPortal() {
  const [selectedService, setSelectedService] = useState(1); // 1: Emergency, 2: Cardiology
  const [priorityClass, setPriorityClass] = useState('STANDARD');
  const [activeToken, setActiveToken] = useState(null);
  const [loading, setLoading] = useState(false);
  const [showQRModal, setShowQRModal] = useState(false);

  const services = [
    { id: 1, name: 'Emergency Triage & OPD', avgMin: 8, waiting: 14, icon: ShieldAlert, color: 'text-amber-400 bg-amber-400/10' },
    { id: 2, name: 'General Cardiology OPD', avgMin: 15, waiting: 6, icon: Sparkles, color: 'text-sky-400 bg-sky-400/10' },
  ];

  const joinQueue = async () => {
    setLoading(true);
    try {
      const res = await axios.post(`/api/v1/services/${selectedService}/tokens`, {
        serviceId: selectedService,
        priorityClass: priorityClass
      });
      if (res.data && res.data.data) {
        setActiveToken(res.data.data);
      }
    } catch (err) {
      console.error("Failed to join queue", err);
    } finally {
      setLoading(false);
    }
  };

  const checkIn = async () => {
    if (!activeToken) return;
    try {
      const res = await axios.post(`/api/v1/tokens/${activeToken.id}/check-in`, {
        qrSignature: activeToken.qrSignature
      });
      if (res.data && res.data.data) {
        setActiveToken(res.data.data);
        setShowQRModal(false);
      }
    } catch (err) {
      alert("Check-in failed: " + (err.response?.data?.detail || err.message));
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8 py-6">
      {/* Banner */}
      <div className="glass-card p-6 rounded-2xl relative overflow-hidden border border-slate-800">
        <div className="absolute -right-10 -bottom-10 w-48 h-48 bg-sky-500/10 rounded-full blur-3xl"></div>
        <div className="relative z-10 space-y-2">
          <span className="text-xs uppercase tracking-widest font-bold text-sky-400 bg-sky-500/10 px-3 py-1 rounded-full">Remote Virtual Queue</span>
          <h1 className="text-2xl font-extrabold text-white">Join Queue & Claim Ticket</h1>
          <p className="text-slate-400 text-sm max-w-xl">
            Get dynamic ETA predictions, track your position in real-time without standing in line, and check in via secure QR code when called.
          </p>
        </div>
      </div>

      {/* Main Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Service Selector & Join Form */}
        <div className="glass-card p-6 rounded-2xl space-y-6">
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-sky-400"></span>
            1. Select Service & Priority Tier
          </h2>

          <div className="space-y-3">
            {services.map((svc) => {
              const Icon = svc.icon;
              return (
                <div
                  key={svc.id}
                  onClick={() => setSelectedService(svc.id)}
                  className={`p-4 rounded-xl border cursor-pointer transition-all ${
                    selectedService === svc.id
                      ? 'border-sky-500 bg-sky-500/10 shadow-lg shadow-sky-500/10'
                      : 'border-slate-800 bg-slate-900/50 hover:border-slate-700'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className={`p-2.5 rounded-lg ${svc.color}`}>
                        <Icon className="w-5 h-5" />
                      </div>
                      <div>
                        <h3 className="font-semibold text-white text-sm">{svc.name}</h3>
                        <p className="text-xs text-slate-400">Avg {svc.avgMin} mins / customer</p>
                      </div>
                    </div>
                    <span className="text-xs font-semibold text-slate-300 bg-slate-800 px-2.5 py-1 rounded-md">
                      {svc.waiting} waiting
                    </span>
                  </div>
                </div>
              );
            })}
          </div>

          <div className="space-y-2">
            <label className="text-xs font-semibold text-slate-400">Priority Tier (Weighted Fairness Scheduler)</label>
            <select
              value={priorityClass}
              onChange={(e) => setPriorityClass(e.target.value)}
              className="w-full bg-slate-900 border border-slate-800 rounded-xl px-4 py-2.5 text-sm font-medium text-white focus:outline-none focus:border-sky-500"
            >
              <option value="STANDARD">Standard Patient / Customer</option>
              <option value="SENIOR">Senior Citizen / PwD (Priority Weight: 40)</option>
              <option value="APPOINTMENT">Pre-Booked Appointment (Priority Weight: 20)</option>
              <option value="EMERGENCY">Emergency Triage (Priority Weight: 100)</option>
            </select>
          </div>

          <button
            onClick={joinQueue}
            disabled={loading}
            className="w-full py-3 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 font-bold text-white shadow-lg shadow-sky-500/25 hover:from-sky-400 hover:to-indigo-500 transition-all disabled:opacity-50"
          >
            {loading ? 'Issuing Ticket...' : 'Get Virtual Token Ticket'}
          </button>
        </div>

        {/* Live Token Status Card */}
        <div className="glass-card p-6 rounded-2xl space-y-6 relative overflow-hidden">
          <h2 className="text-lg font-bold text-white flex items-center justify-between">
            <span className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
              Your Active Token Status
            </span>
            {activeToken && (
              <span className="text-xs font-mono font-bold px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                STATUS: {activeToken.status}
              </span>
            )}
          </h2>

          {activeToken ? (
            <div className="space-y-6">
              {/* Giant Token Display */}
              <div className="bg-slate-950 p-6 rounded-xl border border-slate-800 text-center space-y-2">
                <span className="text-xs uppercase font-bold text-slate-400 tracking-wider">Token Claim Number</span>
                <div className="text-4xl font-black text-transparent bg-clip-text bg-gradient-to-r from-sky-400 to-indigo-400 tracking-tight font-mono">
                  {activeToken.tokenNumber}
                </div>
                <p className="text-xs text-slate-400">{activeToken.serviceName}</p>
              </div>

              {/* Position & ETA Counters */}
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 text-center">
                  <span className="text-xs text-slate-400 font-medium">Queue Position</span>
                  <div className="text-2xl font-bold text-white mt-1">#{activeToken.queuePosition}</div>
                </div>
                <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 text-center">
                  <span className="text-xs text-slate-400 font-medium">Estimated Wait</span>
                  <div className="text-2xl font-bold text-sky-400 mt-1 flex items-center justify-center gap-1">
                    <Clock className="w-5 h-5 text-sky-400" />
                    ~{activeToken.estimatedWaitMinutes} min
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="space-y-3">
                <button
                  onClick={() => setShowQRModal(true)}
                  className="w-full py-3 rounded-xl bg-slate-800 border border-slate-700 hover:bg-slate-700 text-white font-semibold text-sm flex items-center justify-center gap-2 transition-all"
                >
                  <QrCode className="w-4 h-4 text-sky-400" />
                  View Signed HMAC QR Pass
                </button>

                {activeToken.status === 'CALLED' && (
                  <button
                    onClick={checkIn}
                    className="w-full py-3 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-sm flex items-center justify-center gap-2 shadow-lg shadow-emerald-600/30 transition-all pulse-glow"
                  >
                    <UserCheck className="w-5 h-5" />
                    Check-in at Counter Now
                  </button>
                )}
              </div>
            </div>
          ) : (
            <div className="h-64 flex flex-col items-center justify-center text-center p-6 border-2 border-dashed border-slate-800 rounded-xl space-y-3">
              <Clock className="w-10 h-10 text-slate-600" />
              <p className="text-slate-400 text-sm font-medium">No active virtual ticket in queue.</p>
              <p className="text-xs text-slate-500">Select a service on the left to join instantly.</p>
            </div>
          )}
        </div>
      </div>

      {/* Signed QR Modal */}
      {showQRModal && activeToken && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-md flex items-center justify-center z-50 p-4">
          <div className="glass-card max-w-sm w-full p-6 rounded-2xl space-y-6 text-center border border-slate-800 relative">
            <div className="space-y-1">
              <h3 className="text-lg font-bold text-white">Signed Digital Pass</h3>
              <p className="text-xs text-slate-400">HMAC-SHA256 Signed Payload</p>
            </div>

            <div className="bg-white p-4 rounded-2xl inline-block shadow-xl">
              <QRCodeSVG value={activeToken.qrSignature || activeToken.tokenNumber} size={180} />
            </div>

            <div className="bg-slate-950 p-3 rounded-xl text-xs font-mono text-slate-400 break-all border border-slate-800">
              {activeToken.qrSignature}
            </div>

            <button
              onClick={() => setShowQRModal(false)}
              className="w-full py-2.5 rounded-xl bg-slate-800 text-white text-xs font-semibold hover:bg-slate-700"
            >
              Close Ticket
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
