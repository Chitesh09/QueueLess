import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Header } from './Header';
import { Sidebar } from './Sidebar';
import { MobileNav } from './MobileNav';
import { NetworkStatusBanner } from '../common/NetworkStatusBanner';
import { SkipLink } from '../common/SkipLink';

export const AppLayout = () => {
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-['Plus_Jakarta_Sans',sans-serif] selection:bg-sky-500 selection:text-white">
      <SkipLink />
      <NetworkStatusBanner />
      <Header onOpenMobileNav={() => setMobileNavOpen(true)} />

      <div className="flex-1 flex">
        <Sidebar />
        <MobileNav isOpen={mobileNavOpen} onClose={() => setMobileNavOpen(false)} />

        <main id="main-content" tabIndex={-1} className="flex-1 p-4 md:p-8 max-w-7xl mx-auto w-full overflow-x-hidden outline-none">
          <Outlet />
        </main>
      </div>

      <footer className="border-t border-slate-900 bg-slate-950/80 py-3 text-center text-xs text-slate-500" role="contentinfo">
        QueueLess Smart Virtual Queue SaaS Platform &copy; 2026 — Enterprise Production Baseline
      </footer>
    </div>
  );
};
