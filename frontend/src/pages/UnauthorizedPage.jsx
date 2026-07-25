import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldAlert, ArrowLeft } from 'lucide-react';
import { Button } from '../components/ui/Button';

export const UnauthorizedPage = () => {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex items-center justify-center p-6">
      <div className="max-w-md w-full glass-card p-8 rounded-2xl border border-slate-800 text-center space-y-6">
        <div className="w-16 h-16 rounded-2xl bg-amber-500/10 border border-amber-500/20 text-amber-400 flex items-center justify-center mx-auto">
          <ShieldAlert className="w-8 h-8" />
        </div>
        <div className="space-y-2">
          <h1 className="text-3xl font-black text-white">403 Forbidden</h1>
          <h2 className="text-base font-bold text-slate-200">Access Denied</h2>
          <p className="text-xs text-slate-400">
            You do not have the required permissions or role to view this page.
          </p>
        </div>
        <Link to="/login" className="block">
          <Button variant="secondary" className="w-full">
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to Login
          </Button>
        </Link>
      </div>
    </div>
  );
};
