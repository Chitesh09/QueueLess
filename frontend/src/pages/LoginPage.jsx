import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate, useLocation } from 'react-router-dom';
import { Lock, Mail, Eye, EyeOff, Layers, Zap } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { useLoginMutation } from '../hooks/useAuthMutations';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Label } from '../components/ui/Label';
import { Card } from '../components/ui/Card';
import { ThemeToggle } from '../components/common/ThemeToggle';

const loginSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Must be a valid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

export const LoginPage = () => {
  const [showPassword, setShowPassword] = useState(false);
  const { loginUser } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/dashboard';

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: 'operator@cityhospital.com',
      password: 'password123',
    },
  });

  const loginMutation = useLoginMutation((data) => {
    loginUser(data);
    navigate(from, { replace: true });
  });

  const onSubmit = (data) => {
    loginMutation.mutate(data);
  };

  const handleDemoLogin = (email, password) => {
    setValue('email', email);
    setValue('password', password);
    loginMutation.mutate({ email, password });
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-between p-4 sm:p-6 font-['Plus_Jakarta_Sans',sans-serif] selection:bg-sky-500 selection:text-white">
      {/* Top Header */}
      <div className="max-w-7xl mx-auto w-full flex items-center justify-between py-2">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-sky-500/20">
            <Layers className="w-6 h-6 text-white" />
          </div>
          <div>
            <span className="font-extrabold text-xl tracking-tight text-white">QueueLess</span>
            <span className="text-[10px] uppercase font-bold tracking-widest px-2 py-0.5 ml-2 rounded-full bg-sky-500/10 text-sky-400 border border-sky-500/20">
              SaaS Platform
            </span>
          </div>
        </div>
        <ThemeToggle />
      </div>

      {/* Main Login Card Container */}
      <div className="max-w-md w-full mx-auto my-auto space-y-6">
        <div className="text-center space-y-2">
          <span className="text-xs uppercase tracking-widest font-extrabold text-sky-400 bg-sky-500/10 px-3.5 py-1 rounded-full border border-sky-500/20">
            Authentication Portal
          </span>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">Sign in to QueueLess</h1>
          <p className="text-xs text-slate-400">Connected to Spring Boot 3 &amp; MySQL 8 on Port 8080</p>
        </div>

        <Card className="p-8 space-y-6 border-slate-800/90 shadow-2xl">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
            {/* Email Field */}
            <div className="space-y-1">
              <Label htmlFor="email">Email Address</Label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
                <Input
                  id="email"
                  type="email"
                  placeholder="operator@cityhospital.com"
                  className="pl-10"
                  error={!!errors.email}
                  {...register('email')}
                />
              </div>
              {errors.email && (
                <p className="text-[11px] font-semibold text-rose-400">{errors.email.message}</p>
              )}
            </div>

            {/* Password Field */}
            <div className="space-y-1">
              <Label htmlFor="password">Password</Label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
                <Input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••••••"
                  className="pl-10 pr-10"
                  error={!!errors.password}
                  {...register('password')}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-3 text-slate-500 hover:text-slate-300"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              {errors.password && (
                <p className="text-[11px] font-semibold text-rose-400">{errors.password.message}</p>
              )}
            </div>

            {/* Submit Button */}
            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full mt-2"
              isLoading={loginMutation.isPending}
            >
              Sign In to Platform
            </Button>
          </form>

          {/* Quick Demo Login Shortcut Buttons */}
          <div className="pt-4 border-t border-slate-800 space-y-3">
            <div className="flex items-center justify-between text-xs text-slate-400">
              <span className="font-semibold text-slate-300 flex items-center gap-1">
                <Zap className="w-3.5 h-3.5 text-amber-400" />
                One-Click Demo Credentials:
              </span>
              <span className="text-[10px] font-mono text-slate-500">Pass: password123</span>
            </div>

            <div className="grid grid-cols-1 gap-2">
              <button
                type="button"
                onClick={() => handleDemoLogin('operator@cityhospital.com', 'password123')}
                className="p-3 rounded-xl bg-slate-900 border border-sky-500/40 hover:border-sky-400 text-left text-xs transition-all hover:bg-slate-800/80"
              >
                <div className="font-bold text-white flex items-center justify-between">
                  <span>Counter Operator / Admin</span>
                  <span className="text-[10px] text-sky-400 font-mono">DEMO READY</span>
                </div>
                <div className="text-[11px] text-slate-400">operator@cityhospital.com</div>
              </button>
            </div>
          </div>
        </Card>
      </div>

      {/* Footer */}
      <footer className="text-center text-xs text-slate-500 py-2">
        QueueLess SaaS Platform &copy; 2026 — Verified Authentication against Spring Boot 3
      </footer>
    </div>
  );
};
