export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://queueless-8u4a.onrender.com';
export const DEFAULT_TENANT_ID = import.meta.env.VITE_DEFAULT_TENANT_ID || '1';

export const WS_URL = import.meta.env.VITE_WS_URL || 
  (API_BASE_URL.replace(/^http/, 'ws') + '/ws');

export const STORAGE_KEYS = {
  TOKEN: 'queueless_jwt_token',
  USER: 'queueless_user_data',
  TENANT_ID: 'queueless_tenant_id',
  THEME: 'queueless_theme',
};
