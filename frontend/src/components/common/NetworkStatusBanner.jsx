import React, { useState, useEffect } from 'react';
import { WifiOff, Wifi } from 'lucide-react';
import toast from 'react-hot-toast';

export const NetworkStatusBanner = () => {
  const [isOffline, setIsOffline] = useState(!navigator.onLine);

  useEffect(() => {
    const handleOnline = () => {
      setIsOffline(false);
      toast.success('Internet connection restored. Live WebSocket reconnecting...', { id: 'online-toast' });
    };

    const handleOffline = () => {
      setIsOffline(true);
      toast.error('Offline — Real-time updates paused.', { id: 'offline-toast' });
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  if (!isOffline) return null;

  return (
    <div
      className="bg-amber-600 text-white text-xs font-bold py-2.5 px-4 text-center flex items-center justify-center gap-2 animate-in slide-in-from-top duration-300 shadow-lg"
      role="alert"
      aria-live="assertive"
    >
      <WifiOff className="w-4 h-4 animate-pulse shrink-0 text-amber-200" />
      <span>Offline — Some real-time updates may be unavailable. Reconnect to resume synchronization.</span>
    </div>
  );
};
