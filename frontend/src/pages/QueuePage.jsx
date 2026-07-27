import React, { useState } from 'react';
import {
  useBranchServicesQuery,
  useJoinQueueMutation,
  useTokenStatusQuery,
  useCheckInMutation,
} from '../hooks/useQueueHooks';
import { queueService } from '../services/queueService';
import { ServiceCatalogCard } from '../components/queue/ServiceCatalogCard';
import { PrioritySelector } from '../components/queue/PrioritySelector';
import { LiveTicketCard } from '../components/queue/LiveTicketCard';
import { QRCodePassModal } from '../components/queue/QRCodePassModal';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Skeleton } from '../components/ui/Skeleton';
import { Users, Clock, Server, CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react';
import { API_BASE_URL } from '../constants/config';

export const QueuePage = () => {
  const [selectedServiceId, setSelectedServiceId] = useState(1);
  const [priorityClass, setPriorityClass] = useState('STANDARD');
  const [activeTokenId, setActiveTokenId] = useState(() => queueService.getStoredTokenId());
  const [isQRModalOpen, setIsQRModalOpen] = useState(false);

  // TanStack Query Hooks
  const { data: services, isLoading: isServicesLoading, error: servicesError, refetch: refetchServices } = useBranchServicesQuery(1);
  const { data: activeToken } = useTokenStatusQuery(activeTokenId);

  const joinQueueMutation = useJoinQueueMutation((newToken) => {
    setActiveTokenId(newToken.id);
  });

  const checkInMutation = useCheckInMutation(() => {
    // Check-in success callback
  });

  const handleJoinQueue = () => {
    if (!selectedServiceId) return;
    joinQueueMutation.mutate({
      serviceId: selectedServiceId,
      priorityClass,
    });
  };

  const handleCheckIn = () => {
    if (!activeToken) return;
    checkInMutation.mutate({
      tokenId: activeToken.id,
      qrSignature: activeToken.qrSignature,
    });
  };

  const formatErrorMessage = (err) => {
    if (!err) return 'Service catalog currently unavailable.';
    if (err.response) {
      return `HTTP ${err.response.status}: ${err.response.data?.message || err.response.data?.detail || err.statusText || 'Backend response error'}`;
    }
    if (err.request) {
      return `Network Error: Unable to reach backend server at ${API_BASE_URL}`;
    }
    return err.message || 'Service loading failed';
  };

  return (
    <div className="space-y-8 animate-in fade-in-50 duration-300">
      {/* 4 Top Summary Statistics Cards Header */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <Card className="p-4 space-y-2 glass-card-hover border-slate-800/80">
          <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
            <span>Today's Visitors</span>
            <Users className="w-4 h-4 text-sky-400" />
          </div>
          <div className="text-2xl font-black text-white font-mono">142 Patients</div>
          <p className="text-[11px] text-emerald-400 font-medium">↑ 12% vs average</p>
        </Card>

        <Card className="p-4 space-y-2 glass-card-hover border-slate-800/80">
          <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
            <span>Average Wait Time</span>
            <Clock className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-black text-white font-mono">11.8 min</div>
          <p className="text-[11px] text-emerald-400 font-medium">↓ 2.4 min faster</p>
        </Card>

        <Card className="p-4 space-y-2 glass-card-hover border-slate-800/80">
          <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
            <span>Active Counters</span>
            <Server className="w-4 h-4 text-purple-400" />
          </div>
          <div className="text-2xl font-black text-white font-mono">4 Online</div>
          <p className="text-[11px] text-purple-400 font-medium">100% Shift Coverage</p>
        </Card>

        <Card className="p-4 space-y-2 glass-card-hover border-slate-800/80">
          <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
            <span>Tokens Served Today</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-black text-emerald-400 font-mono">118 Completed</div>
          <p className="text-[11px] text-slate-400">96.5% SLA Compliant</p>
        </Card>
      </div>

      {/* Top Welcome Banner */}
      <div className="glass-card p-6 md:p-8 rounded-3xl border border-slate-800 relative overflow-hidden bg-slate-900/60">
        <div className="absolute -right-10 -bottom-10 w-48 h-48 bg-sky-500/10 rounded-full blur-3xl" />
        <div className="relative z-10 space-y-2">
          <span className="text-xs uppercase font-extrabold text-sky-400 bg-sky-500/10 px-3.5 py-1 rounded-full border border-sky-500/20">
            Enterprise Virtual Queue Manager
          </span>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white">Remote Queue Claim &amp; Live Ticket</h1>
          <p className="text-slate-400 text-xs md:text-sm max-w-xl">
            Book digital token tickets remotely, track your live queue position with dynamic ETA predictions, and check in via cryptographic HMAC QR pass.
          </p>
        </div>
      </div>

      {/* Main Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Service Catalog & Visual Priority Selector (2 cols) */}
        <div className="lg:col-span-2 space-y-6">
          <Card className="p-6 md:p-8 space-y-6">
            <h2 className="text-base font-bold text-white flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-sky-400" />
              1. Select Department / Service
            </h2>

            {/* Service Catalog List */}
            {isServicesLoading ? (
              <div className="space-y-3">
                <Skeleton className="h-20 w-full rounded-2xl" />
                <Skeleton className="h-20 w-full rounded-2xl" />
              </div>
            ) : servicesError ? (
              <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs flex items-center justify-between gap-3">
                <div className="flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 shrink-0" />
                  <span>{formatErrorMessage(servicesError)}</span>
                </div>
                <button
                  type="button"
                  onClick={() => refetchServices()}
                  className="px-2.5 py-1 rounded-lg bg-rose-500/20 hover:bg-rose-500/30 font-bold text-[11px] flex items-center gap-1 transition-all shrink-0"
                >
                  <RefreshCw className="w-3 h-3" />
                  Retry
                </button>
              </div>
            ) : (
              <div className="space-y-3">
                {services?.map((service) => (
                  <ServiceCatalogCard
                    key={service.id}
                    service={service}
                    isSelected={selectedServiceId === service.id}
                    onSelect={(id) => setSelectedServiceId(id)}
                  />
                ))}
              </div>
            )}

            {/* Visual Priority Selector */}
            <div className="space-y-3 pt-2 border-t border-slate-800">
              <h2 className="text-base font-bold text-white flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-purple-400" />
                2. Choose Priority Class (Weighted Round-Robin Engine)
              </h2>

              <PrioritySelector
                value={priorityClass}
                onChange={(newPriority) => setPriorityClass(newPriority)}
              />
            </div>

            {/* Claim Ticket Button */}
            <Button
              onClick={handleJoinQueue}
              isLoading={joinQueueMutation.isPending}
              variant="primary"
              size="lg"
              className="w-full py-4 text-base font-extrabold shadow-xl shadow-sky-500/25 active:scale-[0.99] transition-all"
            >
              Get Virtual Token Ticket
            </Button>
          </Card>
        </div>

        {/* Right Column: Live Ticket Card (1 col) */}
        <div>
          <LiveTicketCard
            token={activeToken}
            onOpenQR={() => setIsQRModalOpen(true)}
            onCheckIn={handleCheckIn}
            isCheckInLoading={checkInMutation.isPending}
          />
        </div>
      </div>

      {/* HMAC QR Digital Pass Modal */}
      <QRCodePassModal
        isOpen={isQRModalOpen}
        onClose={() => setIsQRModalOpen(false)}
        token={activeToken}
      />
    </div>
  );
};
