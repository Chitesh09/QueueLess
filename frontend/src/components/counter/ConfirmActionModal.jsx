import React, { useEffect } from 'react';
import { AlertTriangle, CheckCircle, SkipForward } from 'lucide-react';
import { Button } from '../ui/Button';
import { Card } from '../ui/Card';

export const ConfirmActionModal = ({ isOpen, onClose, onConfirm, actionType, tokenNumber, isLoading }) => {
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (!isOpen) return;
      if (e.key === 'Enter') {
        e.preventDefault();
        onConfirm();
      } else if (e.key === 'Escape') {
        e.preventDefault();
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onConfirm, onClose]);

  if (!isOpen) return null;

  const isComplete = actionType === 'COMPLETE';

  return (
    <div
      className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-modal-title"
    >
      <Card className="max-w-sm w-full p-6 rounded-3xl border border-slate-800 text-center space-y-5 bg-slate-900/90 shadow-2xl animate-in fade-in-80 scale-in-95">
        <div
          className={`w-14 h-14 rounded-2xl flex items-center justify-center mx-auto border ${
            isComplete
              ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400'
              : 'bg-amber-500/10 border-amber-500/20 text-amber-400'
          }`}
        >
          {isComplete ? <CheckCircle className="w-7 h-7" /> : <SkipForward className="w-7 h-7" />}
        </div>

        <div className="space-y-1.5">
          <h3 id="confirm-modal-title" className="text-lg font-bold text-white">
            {isComplete ? 'Complete Service?' : 'Skip Absent Customer?'}
          </h3>
          <p className="text-xs text-slate-400 leading-relaxed">
            {isComplete
              ? `Are you sure you want to mark service as complete for Token #${tokenNumber}?`
              : `Are you sure you want to skip Token #${tokenNumber}? The customer will be marked absent.`}
          </p>
        </div>

        <div className="grid grid-cols-2 gap-3 pt-2">
          <Button onClick={onClose} variant="secondary" className="w-full text-xs">
            Cancel <span className="text-[10px] font-mono text-slate-500 ml-1">(Esc)</span>
          </Button>

          <Button
            onClick={onConfirm}
            isLoading={isLoading}
            variant={isComplete ? 'primary' : 'danger'}
            className="w-full text-xs font-bold"
          >
            {isComplete ? 'Yes, Complete' : 'Yes, Skip'}{' '}
            <span className="text-[10px] font-mono opacity-80 ml-1">(Enter)</span>
          </Button>
        </div>
      </Card>
    </div>
  );
};
