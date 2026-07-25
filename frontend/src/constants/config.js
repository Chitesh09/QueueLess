export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
export const DEFAULT_TENANT_ID = import.meta.env.VITE_DEFAULT_TENANT_ID || '1';

export const STORAGE_KEYS = {
  TOKEN: 'queueless_jwt_token',
  USER: 'queueless_user_data',
  TENANT_ID: 'queueless_tenant_id',
  THEME: 'queueless_theme',
};
