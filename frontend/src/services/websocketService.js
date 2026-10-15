if (typeof window !== 'undefined' && !window.global) {
  window.global = window;
}

import { Client } from '@stomp/stompjs';
import SockJSImport from 'sockjs-client';
import { API_BASE_URL, WS_URL } from '../constants/config';
import { authService } from './authService';

const SockJS = typeof SockJSImport === 'function' ? SockJSImport : (SockJSImport && SockJSImport.default) || window.SockJS;

class WebSocketService {
  constructor() {
    this.client = null;
    this.isConnected = false;
    this.statusListeners = new Set();
    this.messageListeners = new Set();
    this.topicCallbacks = new Map(); // destination -> Set of callbacks
    this.stompSubscriptions = new Map(); // destination -> StompSubscription
    this.recentEventTimestamps = new Map(); // eventKey -> timestampMs
  }

  connect(branchId = 1, onMessageCallback) {
    if (onMessageCallback) {
      this.subscribeToBranchQueue(branchId, onMessageCallback);
    }

    if (this.client && this.client.active) {
      return;
    }

    try {
      const token = authService.getStoredToken();
      const httpWsEndpoint = `${API_BASE_URL}/ws`;

      this.client = new Client({
        webSocketFactory: () => {
          if (typeof SockJS === 'function') {
            return new SockJS(httpWsEndpoint);
          }
          return new WebSocket(WS_URL);
        },
        connectHeaders: {
          Authorization: token ? `Bearer ${token}` : '',
          'X-Tenant-ID': '1',
        },
        debug: () => {},
        reconnectDelay: 3000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,

        onConnect: () => {
          this.isConnected = true;
          this._notifyStatusChange('CONNECTED');

          // Ensure default branch subscriptions exist if branchId provided
          if (branchId) {
            this._ensureSubscription(`/topic/branch/${branchId}/queue`);
            this._ensureSubscription(`/topic/branch/${branchId}/counters`);
          }

          // Resubscribe to all active destinations across reconnects
          this.topicCallbacks.forEach((callbacks, destination) => {
            if (callbacks.size > 0) {
              this._ensureSubscription(destination);
            }
          });
        },

        onDisconnect: () => {
          this.isConnected = false;
          this.stompSubscriptions.clear();
          this._notifyStatusChange('DISCONNECTED');
        },

        onStompError: () => {
          this.isConnected = false;
          this.stompSubscriptions.clear();
          this._notifyStatusChange('DISCONNECTED');
        },

        onWebSocketClose: () => {
          this.isConnected = false;
          this.stompSubscriptions.clear();
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

  subscribe(destination, callback) {
    if (!destination || typeof callback !== 'function') {
      return { unsubscribe: () => {} };
    }

    if (!this.topicCallbacks.has(destination)) {
      this.topicCallbacks.set(destination, new Set());
    }
    this.topicCallbacks.get(destination).add(callback);

    if (this.client && this.client.connected) {
      this._ensureSubscription(destination);
    } else if (!this.client || !this.client.active) {
      this.connect();
    }

    return {
      unsubscribe: () => {
        const callbacks = this.topicCallbacks.get(destination);
        if (callbacks) {
          callbacks.delete(callback);
          if (callbacks.size === 0) {
            this.topicCallbacks.delete(destination);
            const sub = this.stompSubscriptions.get(destination);
            if (sub) {
              try {
                sub.unsubscribe();
              } catch (e) {
                // ignore
              }
              this.stompSubscriptions.delete(destination);
            }
          }
        }
      },
    };
  }

  subscribeToBranchQueue(branchId, callback) {
    return this.subscribe(`/topic/branch/${branchId}/queue`, callback);
  }

  subscribeToCounters(branchId, callback) {
    return this.subscribe(`/topic/branch/${branchId}/counters`, callback);
  }

  subscribeToToken(tokenId, callback) {
    return this.subscribe(`/topic/tokens/${tokenId}`, callback);
  }

  _ensureSubscription(destination) {
    if (!this.client || !this.client.connected) return;
    if (this.stompSubscriptions.has(destination)) return;

    try {
      const sub = this.client.subscribe(destination, (message) => {
        try {
          const data = JSON.parse(message.body);
          if (this._isDuplicate(data)) {
            return;
          }

          // Dispatch to destination-specific listeners
          const callbacks = this.topicCallbacks.get(destination);
          if (callbacks) {
            callbacks.forEach((cb) => {
              try {
                cb(data, destination);
              } catch (err) {
                console.error(`Error in WS subscriber for ${destination}:`, err);
              }
            });
          }

          // Dispatch to global listeners
          this.messageListeners.forEach((listener) => {
            try {
              listener(data, destination);
            } catch (err) {
              console.error('Error in WS messageListener:', err);
            }
          });
        } catch (e) {
          console.error(`Error parsing message on ${destination}:`, e);
        }
      });

      this.stompSubscriptions.set(destination, sub);
    } catch (e) {
      console.error(`STOMP subscription failed for ${destination}:`, e);
    }
  }

  _isDuplicate(data) {
    if (!data) return false;
    const now = Date.now();

    // Clean old entries (> 5 seconds)
    for (const [key, timestamp] of this.recentEventTimestamps.entries()) {
      if (now - timestamp > 5000) {
        this.recentEventTimestamps.delete(key);
      }
    }

    const eventKey = `${data.eventType || 'EVT'}_${data.token?.id || data.id || data.counterId || 'X'}_${data.token?.status || data.status || data.counterStatus || ''}_${data.timestamp || ''}`;
    if (this.recentEventTimestamps.has(eventKey)) {
      return true; // Duplicate detected
    }

    this.recentEventTimestamps.set(eventKey, now);
    return false;
  }

  disconnect() {
    if (this.client) {
      try {
        this.client.deactivate();
      } catch (e) {
        // ignore
      }
      this.client = null;
      this.isConnected = false;
      this.stompSubscriptions.clear();
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

