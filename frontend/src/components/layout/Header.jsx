import React from 'react';
import { LogOut, Menu, Building } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { ThemeToggle } from '../common/ThemeToggle';
import { HealthStatusBadge } from '../common/HealthStatusBadge';
import { WebSocketStatusIndicator } from '../common/WebSocketStatusIndicator';
import { LanguageSelector } from '../common/LanguageSelector';
import { HighContrastToggle } from '../common/HighContrastToggle';
import { Avatar } from '../ui/Avatar';
import { DropdownMenu, DropdownMenuItem } from '../ui/DropdownMenu';
import { Badge } from '../ui/Badge';

export const Header = ({ onOpenMobileNav }) => {
  const { user, logoutUser } = useAuth();

  return (
    <header className="sticky top-0 z-40 border-b border-slate-800 bg-slate-950/80 backdrop-blur-md px-4 lg:px-8 py-3">
      <div className="max-w-7xl mx-auto flex items-center justify-between gap-4">
        {/* Left: Mobile Nav Toggle & Tenant Badge */}
        <div className="flex items-center gap-3">
          <button
            onClick={onOpenMobileNav}
            className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-400 hover:text-white md:hidden"
            aria-label="Open mobile menu"
          >
            <Menu className="w-5 h-5" />
          </button>

          <div className="flex items-center gap-2 bg-slate-900 border border-slate-800 px-3 py-1.5 rounded-xl text-xs">
            <Building className="w-4 h-4 text-sky-400" />
            <span className="text-slate-400 hidden sm:inline">Tenant:</span>
            <strong className="text-white font-semibold">City Care Hospital</strong>
          </div>
        </div>

        {/* Right: Language, High Contrast, WebSocket, Health, Theme & User Dropdown */}
        <div className="flex items-center gap-2 sm:gap-3">
          <LanguageSelector />
          <HighContrastToggle />
          <WebSocketStatusIndicator />
          <HealthStatusBadge />
          <ThemeToggle />

          {/* User Profile Dropdown */}
          <DropdownMenu
            trigger={
              <div className="flex items-center gap-2.5 bg-slate-900 border border-slate-800 hover:border-slate-700 px-3 py-1.5 rounded-xl transition-all">
                <Avatar name={user?.name || user?.email || 'Admin User'} />
                <div className="hidden md:block text-left">
                  <div className="text-xs font-bold text-white truncate max-w-[120px]">
                    {user?.name || user?.email || 'User'}
                  </div>
                  <div className="text-[10px] text-sky-400 font-mono uppercase">
                    {user?.role || 'SUPER_ADMIN'}
                  </div>
                </div>
              </div>
            }
          >
            <div className="p-3 border-b border-slate-800">
              <p className="text-xs font-bold text-white">{user?.name || 'User'}</p>
              <p className="text-[11px] text-slate-400 truncate">{user?.email || 'admin@queueless.com'}</p>
              <Badge variant="info" className="mt-2 text-[10px]">
                {user?.role || 'SUPER_ADMIN'}
              </Badge>
            </div>

            <DropdownMenuItem onClick={logoutUser} danger className="mt-1">
              <LogOut className="w-4 h-4" />
              Log Out Session
            </DropdownMenuItem>
          </DropdownMenu>
        </div>
      </div>
    </header>
  );
};
