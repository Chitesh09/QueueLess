import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import {
  useUserProfileQuery,
  useTenantPreferencesQuery,
  useBackendHealthQuery,
  useUpdateProfileMutation,
  useChangePasswordMutation,
  useUpdateTenantPreferencesMutation,
} from '../hooks/useSettingsHooks';
import { UserProfileCard } from '../components/settings/UserProfileCard';
import { SecurityCard } from '../components/settings/SecurityCard';
import { TenantPreferencesCard } from '../components/settings/TenantPreferencesCard';
import { SystemHealthCard } from '../components/settings/SystemHealthCard';
import { User, Lock, Sliders, Activity, ChevronRight, Clock } from 'lucide-react';
import { Link } from 'react-router-dom';

export const SettingsPage = () => {
  const { user, loginUser } = useAuth();
  const [activeTab, setActiveTab] = useState('PROFILE');
  const [lastSync, setLastSync] = useState('Just now');

  // TanStack Query Hooks
  const { data: userProfile } = useUserProfileQuery();
  const { data: preferences } = useTenantPreferencesQuery();
  const { data: health, refetch: refetchHealth, isFetching: isHealthFetching } = useBackendHealthQuery();

  const updateProfileMutation = useUpdateProfileMutation((updatedUser) => {
    loginUser({ token: localStorage.getItem('queueless_jwt_token'), user: updatedUser });
  });

  const changePasswordMutation = useChangePasswordMutation();
  const updatePreferencesMutation = useUpdateTenantPreferencesMutation();

  const handleSaveProfile = (formData) => {
    updateProfileMutation.mutate(formData);
    setLastSync(new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }));
  };

  const handleChangePassword = (formData) => {
    changePasswordMutation.mutate(formData);
    setLastSync(new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }));
  };

  const handleSavePreferences = (formData) => {
    updatePreferencesMutation.mutate(formData);
    setLastSync(new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }));
  };

  const tabs = [
    { id: 'PROFILE', label: 'User Profile', icon: User },
    { id: 'SECURITY', label: 'Security & Password', icon: Lock },
    { id: 'PREFERENCES', label: 'Organization Preferences', icon: Sliders },
    { id: 'HEALTH', label: 'System Diagnostics', icon: Activity },
  ];

  return (
    <div className="space-y-8 animate-in fade-in-50 duration-300">
      {/* Breadcrumb Navigation */}
      <nav className="flex items-center gap-2 text-xs text-slate-400 font-mono" aria-label="Breadcrumb">
        <Link to="/dashboard" className="hover:text-white transition-colors">
          Dashboard
        </Link>
        <ChevronRight className="w-3.5 h-3.5 text-slate-600" />
        <span className="text-sky-400 font-bold">Settings &amp; Profile</span>
      </nav>

      {/* Top Banner */}
      <div className="glass-card p-6 md:p-8 rounded-3xl border border-slate-800 relative overflow-hidden bg-slate-900/60 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div className="relative z-10 space-y-2">
          <div className="flex items-center gap-2">
            <span className="text-xs uppercase font-extrabold text-sky-400 bg-sky-500/10 px-3.5 py-1 rounded-full border border-sky-500/20">
              Platform Configuration
            </span>
            <span className="text-[11px] font-mono text-slate-400 flex items-center gap-1">
              <Clock className="w-3 h-3 text-slate-500" />
              Last sync: {lastSync}
            </span>
          </div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white">Settings &amp; User Profile</h1>
          <p className="text-slate-400 text-xs md:text-sm max-w-xl">
            Manage your account credentials, security preferences, tenant notifications, and backend diagnostic monitors.
          </p>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="flex items-center gap-2 border-b border-slate-800 pb-2 overflow-x-auto">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-bold transition-all whitespace-nowrap ${
                isActive
                  ? 'bg-sky-600 text-white shadow-md shadow-sky-600/30'
                  : 'text-slate-400 hover:text-white hover:bg-slate-900'
              }`}
            >
              <Icon className="w-4 h-4" />
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* Active Tab Content */}
      {activeTab === 'PROFILE' && (
        <UserProfileCard
          user={userProfile || user}
          onSave={handleSaveProfile}
          isLoading={updateProfileMutation.isPending}
        />
      )}

      {activeTab === 'SECURITY' && (
        <SecurityCard
          onChangePassword={handleChangePassword}
          isLoading={changePasswordMutation.isPending}
        />
      )}

      {activeTab === 'PREFERENCES' && (
        <TenantPreferencesCard
          preferences={preferences}
          onSave={handleSavePreferences}
          isLoading={updatePreferencesMutation.isPending}
        />
      )}

      {activeTab === 'HEALTH' && (
        <SystemHealthCard
          health={health}
          onRefetch={() => refetchHealth()}
          isLoading={isHealthFetching}
        />
      )}
    </div>
  );
};
