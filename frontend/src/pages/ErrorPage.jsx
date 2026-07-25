import React from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';
import { Button } from '../components/ui/Button';

export const ErrorPage = ({ message, onRetry }) => {
  return (
    <div className="min-h-[400px] flex items-center justify-center p-6">
      <div className="max-w-md w-full glass-card p-8 rounded-2xl border border-slate-800 text-center space-y-6">
        <div className="w-16 h-16 rounded-2xl bg-rose-500/10 border border-rose-500/20 text-rose-400 flex items-center justify-center mx-auto">
          <AlertCircle className="w-8 h-8" />
        </div>
        <div className="space-y-2">
          <h1 className="text-xl font-bold text-white">Server Communication Error</h1>
          <p className="text-xs text-slate-400">
            {message || 'Unable to connect to Spring Boot backend server at http://localhost:8080'}
          </p>
        </div>
        {onRetry && (
          <Button onClick={onRetry} variant="primary" className="w-full">
            <RefreshCw className="w-4 h-4 mr-2" />
            Retry Connection
          </Button>
        )}
      </div>
    </div>
  );
};
