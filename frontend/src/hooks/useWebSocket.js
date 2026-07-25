import { useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { websocketService } from '../services/websocketService';

export const useWebSocket = (branchId = 1) => {
  const [connectionStatus, setConnectionStatus] = useState('DISCONNECTED');
  const queryClient = useQueryClient();

  useEffect(() => {
    const handleStatusChange = (status) => {
      setConnectionStatus(status);
    };

    const handleMessage = () => {
      try {
        queryClient.invalidateQueries({ queryKey: ['tokenStatus'] });
        queryClient.invalidateQueries({ queryKey: ['counters'] });
        queryClient.invalidateQueries({ queryKey: ['services'] });
      } catch (e) {
        // ignore
      }
    };

    try {
      websocketService.addStatusListener(handleStatusChange);
      websocketService.addMessageListener(handleMessage);
      websocketService.connect(branchId, handleMessage);
    } catch (e) {
      console.warn('useWebSocket connection error:', e);
    }

    return () => {
      try {
        websocketService.removeStatusListener(handleStatusChange);
        websocketService.removeMessageListener(handleMessage);
      } catch (e) {
        // ignore
      }
    };
  }, [branchId, queryClient]);

  return { connectionStatus, isConnected: connectionStatus === 'CONNECTED' };
};
