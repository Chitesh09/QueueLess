import React, { useState } from 'react';
import {
  useBranchCountersQuery,
  useCallNextMutation,
  useCompleteServiceMutation,
  useSkipTokenMutation,
  usePauseCounterMutation,
  useResumeCounterMutation,
} from '../hooks/useCounterHooks';
import { useCounterKeyboardShortcuts } from '../hooks/useCounterKeyboardShortcuts';
import { counterService } from '../services/counterService';
import { CounterSelectorCard } from '../components/counter/CounterSelectorCard';
import { CurrentCustomerCard } from '../components/counter/CurrentCustomerCard';
import { NextUpQueueList } from '../components/counter/NextUpQueueList';
import { ConfirmActionModal } from '../components/counter/ConfirmActionModal';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { PhoneCall, UserCheck, Clock, Users, Activity, Keyboard } from 'lucide-react';

export const CounterPage = () => {
  const [selectedCounterId, setSelectedCounterId] = useState(() => counterService.getStoredCounterId());
  const [counterStatus, setCounterStatus] = useState('ONLINE');
  const [currentCustomer, setCurrentCustomer] = useState(null);
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, actionType: null });

  // TanStack Query Hooks
  const { data: counters } = useBranchCountersQuery(1);

  const callNextMutation = useCallNextMutation((nextToken) => {
    setCurrentCustomer(nextToken);
  });

  const completeServiceMutation = useCompleteServiceMutation(() => {
    setCurrentCustomer(null);
    setConfirmModal({ isOpen: false, actionType: null });
  });

  const skipTokenMutation = useSkipTokenMutation(() => {
    setCurrentCustomer(null);
    setConfirmModal({ isOpen: false, actionType: null });
  });

  const pauseCounterMutation = usePauseCounterMutation(() => {
    setCounterStatus('PAUSED');
  });

  const resumeCounterMutation = useResumeCounterMutation(() => {
    setCounterStatus('ONLINE');
  });

  const handleSelectCounter = (id) => {
    setSelectedCounterId(id);
    counterService.setStoredCounterId(id);
    setCurrentCustomer(null);
  };

  const handleCallNext = () => {
    if (counterStatus === 'PAUSED') return;
    callNextMutation.mutate(selectedCounterId);
  };

  const handleConfirmAction = () => {
    if (!currentCustomer) return;
    if (confirmModal.actionType === 'COMPLETE') {
      completeServiceMutation.mutate({
        counterId: selectedCounterId,
        tokenId: currentCustomer.id,
      });
    } else if (confirmModal.actionType === 'SKIP') {
      skipTokenMutation.mutate({
        counterId: selectedCounterId,
        tokenId: currentCustomer.id,
      });
    }
  };

  const handleTogglePause = () => {
    if (counterStatus === 'ONLINE') {
      pauseCounterMutation.mutate(selectedCounterId);
    } else {
      resumeCounterMutation.mutate(selectedCounterId);
    }
  };

  const isPaused = counterStatus === 'PAUSED';

  // Bind Operator Keyboard Hotkeys (N = Call Next, C = Complete, S = Skip, P = Pause)
  useCounterKeyboardShortcuts({
    onCallNext: handleCallNext,
    onOpenCompleteModal: () => setConfirmModal({ isOpen: true, actionType: 'COMPLETE' }),
    onOpenSkipModal: () => setConfirmModal({ isOpen: true, actionType: 'SKIP' }),
    onTogglePause: handleTogglePause,
    isModalOpen: confirmModal.isOpen,
    isPaused,
    hasCurrentCustomer: !!currentCustomer,
  });

  return (
    <div className="space-y-6 animate-in fade-in-50 duration-300">
      {/* Top Keyboard Shortcut Legend Bar */}
      <div className="hidden md:flex items-center justify-between bg-slate-900/80 border border-slate-800 px-4 py-2 rounded-2xl text-xs text-slate-400">
        <div className="flex items-center gap-2 font-semibold text-slate-300">
          <Keyboard className="w-4 h-4 text-sky-400" />
          <span>Operator Hotkeys Enabled:</span>
        </div>
        <div className="flex items-center gap-4 font-mono text-[11px]">
          <span>
            <kbd className="px-2 py-0.5 rounded bg-slate-800 border border-slate-700 text-sky-400 font-bold">N</kbd> Call Next
          </span>
          <span>
            <kbd className="px-2 py-0.5 rounded bg-slate-800 border border-slate-700 text-emerald-400 font-bold">C</kbd> Complete
          </span>
          <span>
            <kbd className="px-2 py-0.5 rounded bg-slate-800 border border-slate-700 text-amber-400 font-bold">S</kbd> Skip
          </span>
          <span>
            <kbd className="px-2 py-0.5 rounded bg-slate-800 border border-slate-700 text-purple-400 font-bold">P</kbd> Pause/Resume
          </span>
        </div>
      </div>

      {/* 4 Operator Summary Stat Cards Header */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <Card className="p-4 space-y-2 glass-card-hover border-slate-800/80">
          <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
            <span>Customers Served Today</span>
            <UserCheck className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-black text-white font-mono">28 Served</div>
          <p className="text-[11px] text-emerald-400 font-medium">↑ 100% SLA Compliant</p>
        </Card>

        <Card className="p-4 space-y-2 glass-card-hover border-slate-800/80">
          <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
            <span>Avg Service Time</span>
            <Clock className="w-4 h-4 text-sky-400" />
          </div>
          <div className="text-2xl font-black text-white font-mono">7.4 min</div>
          <p className="text-[11px] text-sky-400 font-medium">Optimal Processing Speed</p>
        </Card>

        <Card className="p-4 space-y-2 glass-card-hover border-slate-800/80">
          <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
            <span>Current Queue Length</span>
            <Users className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-black text-white font-mono">3 Waiting</div>
          <p className="text-[11px] text-slate-400">Next-up in Dispatch</p>
        </Card>

        <Card className="p-4 space-y-2 glass-card-hover border-slate-800/80">
          <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
            <span>Shift Duration</span>
            <Activity className="w-4 h-4 text-purple-400" />
          </div>
          <div className="text-2xl font-black text-white font-mono">3h 45m</div>
          <p className="text-[11px] text-purple-400 font-medium">Shift Status: {counterStatus}</p>
        </Card>
      </div>

      {/* Shift Control & Counter Selector Header */}
      <CounterSelectorCard
        counters={counters}
        selectedCounterId={selectedCounterId}
        onSelectCounter={handleSelectCounter}
        counterStatus={counterStatus}
        onTogglePause={handleTogglePause}
        isPauseLoading={pauseCounterMutation.isPending || resumeCounterMutation.isPending}
      />

      {/* Main Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Call Next Action Bar & Currently Serving Hero Card (2 cols) */}
        <div className="lg:col-span-2 space-y-6">
          {/* Call Next Button Bar */}
          <Button
            onClick={handleCallNext}
            isLoading={callNextMutation.isPending}
            disabled={isPaused}
            variant="primary"
            className="w-full py-5 rounded-2xl bg-gradient-to-r from-sky-500 via-indigo-600 to-purple-600 font-extrabold text-white text-base md:text-lg shadow-xl shadow-sky-500/20 hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center gap-3 disabled:opacity-50"
          >
            <PhoneCall className="w-6 h-6 animate-bounce" />
            {callNextMutation.isPending
              ? 'Calling Next Customer...'
              : 'Call Next Customer [Key: N]'}
          </Button>

          {/* Currently Serving Hero Card */}
          <CurrentCustomerCard
            currentCustomer={currentCustomer}
            onOpenCompleteModal={() => setConfirmModal({ isOpen: true, actionType: 'COMPLETE' })}
            onOpenSkipModal={() => setConfirmModal({ isOpen: true, actionType: 'SKIP' })}
            isPaused={isPaused}
          />
        </div>

        {/* Right Column: Live Next-Up Waiting Queue (1 col) */}
        <div>
          <NextUpQueueList waitingList={null} />
        </div>
      </div>

      {/* Confirmation Modal */}
      <ConfirmActionModal
        isOpen={confirmModal.isOpen}
        onClose={() => setConfirmModal({ isOpen: false, actionType: null })}
        onConfirm={handleConfirmAction}
        actionType={confirmModal.actionType}
        tokenNumber={currentCustomer?.tokenNumber}
        isLoading={completeServiceMutation.isPending || skipTokenMutation.isPending}
      />
    </div>
  );
};
