import { useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { websocketService } from '../services/websocketService';

export const useWebSocket = (branchId = 1) => {
  const [connectionStatus, setConnectionStatus] = useState('DISCONNECTED');
  const queryClient = useQueryClient();

  useEffect(() => {
    const handleStatusChange = (status) => {
      setConnectionStatus(status);
      if (status === 'CONNECTED') {
        // When reconnecting, catch up on any missed updates during network drop
        queryClient.invalidateQueries({ queryKey: ['displayBoard'] });
        queryClient.invalidateQueries({ queryKey: ['counters'] });
        queryClient.invalidateQueries({ queryKey: ['services'] });
        queryClient.invalidateQueries({ queryKey: ['tokenStatus'] });
        queryClient.invalidateQueries({ queryKey: ['counterQueue'] });
      }
    };

    const handleMessage = (data) => {
      try {
        queryClient.invalidateQueries({ queryKey: ['displayBoard'] });
        queryClient.invalidateQueries({ queryKey: ['counterQueue'] });
        queryClient.invalidateQueries({ queryKey: ['counters'] });
        if (data?.token?.id) {
          queryClient.invalidateQueries({ queryKey: ['tokenStatus', data.token.id] });
        }
      } catch (e) {
        // ignore
      }
    };

    websocketService.addStatusListener(handleStatusChange);
    websocketService.connect(branchId, handleMessage);

    return () => {
      websocketService.removeStatusListener(handleStatusChange);
    };
  }, [branchId, queryClient]);

  return { connectionStatus, isConnected: connectionStatus === 'CONNECTED' };
};

