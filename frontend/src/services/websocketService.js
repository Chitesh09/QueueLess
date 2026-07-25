if (typeof window !== 'undefined' && !window.global) {
  window.global = window;
}

import { Client } from '@stomp/stompjs';
import SockJSImport from 'sockjs-client';
import { API_BASE_URL } from '../constants/config';
import { authService } from './authService';

const SockJS = typeof SockJSImport === 'function' ? SockJSImport : (SockJSImport && SockJSImport.default) || window.SockJS;

class WebSocketService {
  constructor() {
    this.client = null;
    this.isConnected = false;
    this.statusListeners = new Set();
    this.messageListeners = new Set();
  }

  connect(branchId = 1, onMessageCallback) {
    if (this.client && this.client.active) {
      return;
    }

    try {
      const token = authService.getToken();
      const wsUrl = `${API_BASE_URL}/ws`;

      this.client = new Client({
        webSocketFactory: () => {
          if (typeof SockJS === 'function') {
            return new SockJS(wsUrl);
          }
          // Fallback to native browser WebSocket if SockJS is unavailable
          return new WebSocket(wsUrl.replace(/^http/, 'ws'));
        },
        connectHeaders: {
          Authorization: `Bearer ${token}`,
          'X-Tenant-ID': '1',
        },
        debug: () => {},
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,

        onConnect: () => {
          this.isConnected = true;
          this._notifyStatusChange('CONNECTED');

          try {
            this.client.subscribe(`/topic/branch/${branchId}/queue`, (message) => {
              try {
                const data = JSON.parse(message.body);
                if (onMessageCallback) onMessageCallback(data);
                this.messageListeners.forEach((listener) => listener(data));
              } catch (e) {
                console.error('Error parsing WS queue message:', e);
              }
            });

            this.client.subscribe(`/topic/branch/${branchId}/counters`, (message) => {
              try {
                const data = JSON.parse(message.body);
                this.messageListeners.forEach((listener) => listener(data));
              } catch (e) {
                console.error('Error parsing WS counter message:', e);
              }
            });
          } catch (e) {
            console.error('STOMP subscription error:', e);
          }
        },

        onDisconnect: () => {
          this.isConnected = false;
          this._notifyStatusChange('DISCONNECTED');
        },

        onStompError: () => {
          this.isConnected = false;
          this._notifyStatusChange('DISCONNECTED');
        },

        onWebSocketClose: () => {
          this.isConnected = false;
          this._notifyStatusChange('DISCONNECTED');
        },
      });

      this.client.activate();
    } catch (e) {
      console.warn('WebSocket STOMP connection error:', e);
      this.isConnected = false;
      this._notifyStatusChange('DISCONNECTED');
    }
  }

  disconnect() {
    if (this.client) {
      try {
        this.client.deactivate();
      } catch (e) {
        // ignore
      }
      this.isConnected = false;
      this._notifyStatusChange('DISCONNECTED');
    }
  }

  addStatusListener(listener) {
    this.statusListeners.add(listener);
    try {
      listener(this.isConnected ? 'CONNECTED' : 'DISCONNECTED');
    } catch (e) {
      // ignore
    }
  }

  removeStatusListener(listener) {
    this.statusListeners.delete(listener);
  }

  addMessageListener(listener) {
    this.messageListeners.add(listener);
  }

  removeMessageListener(listener) {
    this.messageListeners.delete(listener);
  }

  _notifyStatusChange(status) {
    this.statusListeners.forEach((listener) => {
      try {
        listener(status);
      } catch (e) {
        // ignore
      }
    });
  }
}

export const websocketService = new WebSocketService();
