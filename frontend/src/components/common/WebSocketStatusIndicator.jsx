import React from 'react';
import { Wifi, WifiOff } from 'lucide-react';
import { useWebSocket } from '../../hooks/useWebSocket';

export const WebSocketStatusIndicator = () => {
  const { connectionStatus, isConnected } = useWebSocket(1);

  return (
    <div
      className={`flex items-center gap-1.5 px-2.5 py-1 rounded-xl text-[10px] font-mono font-bold border transition-all ${
        isConnected
          ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
          : 'bg-amber-500/10 text-amber-400 border-amber-500/20'
      }`}
      title={`Real-Time STOMP Status: ${connectionStatus}`}
    >
      {isConnected ? (
        <>
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
          <Wifi className="w-3.5 h-3.5 text-emerald-400" />
          <span>LIVE WS</span>
        </>
      ) : (
        <>
          <WifiOff className="w-3.5 h-3.5 text-amber-400" />
          <span>CONNECTING</span>
        </>
      )}
    </div>
  );
};
