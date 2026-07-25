import React, { lazy, Suspense } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AppLayout } from '../components/layout/AppLayout';
import { ProtectedRoute } from '../components/layout/ProtectedRoute';
import { Skeleton } from '../components/ui/Skeleton';

// Code-split Lazy-loaded Pages
const LoginPage = lazy(() => import('../pages/LoginPage').then((m) => ({ default: m.LoginPage })));
const DashboardPage = lazy(() => import('../pages/DashboardPage').then((m) => ({ default: m.DashboardPage })));
const QueuePage = lazy(() => import('../pages/QueuePage').then((m) => ({ default: m.QueuePage })));
const CounterPage = lazy(() => import('../pages/CounterPage').then((m) => ({ default: m.CounterPage })));
const OrganizationPage = lazy(() => import('../pages/OrganizationPage').then((m) => ({ default: m.OrganizationPage })));
const AnalyticsPage = lazy(() => import('../pages/AnalyticsPage').then((m) => ({ default: m.AnalyticsPage })));
const DisplayBoardPage = lazy(() => import('../pages/DisplayBoardPage').then((m) => ({ default: m.DisplayBoardPage })));
const SettingsPage = lazy(() => import('../pages/SettingsPage').then((m) => ({ default: m.SettingsPage })));
const NotFoundPage = lazy(() => import('../pages/NotFoundPage').then((m) => ({ default: m.NotFoundPage })));
const UnauthorizedPage = lazy(() => import('../pages/UnauthorizedPage').then((m) => ({ default: m.UnauthorizedPage })));

const SuspenseFallback = () => (
  <div className="min-h-screen bg-slate-950 flex items-center justify-center p-6">
    <div className="w-full max-w-md space-y-4 text-center">
      <Skeleton className="h-12 w-12 rounded-full mx-auto animate-pulse" />
      <Skeleton className="h-4 w-48 mx-auto" />
      <Skeleton className="h-10 w-full rounded-xl" />
    </div>
  </div>
);

export const AppRoutes = () => {
  return (
    <Suspense fallback={<SuspenseFallback />}>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/unauthorized" element={<UnauthorizedPage />} />

        {/* Protected App Routes */}
        <Route
          element={
            <ProtectedRoute>
              <AppLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/queue" element={<QueuePage />} />
          <Route path="/counter" element={<CounterPage />} />
          <Route path="/organization" element={<OrganizationPage />} />
          <Route path="/analytics" element={<AnalyticsPage />} />
          <Route path="/display-board" element={<DisplayBoardPage />} />
          <Route path="/settings" element={<SettingsPage />} />
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
        </Route>

        {/* Catch-all 404 Route */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Suspense>
  );
};
