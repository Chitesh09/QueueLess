import React, { useState, memo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Lock, Eye, EyeOff, ShieldCheck } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Label } from '../ui/Label';

const securitySchema = z
  .object({
    currentPassword: z.string().min(6, 'Current password must be at least 6 characters'),
    newPassword: z.string().min(6, 'New password must be at least 6 characters'),
    confirmPassword: z.string().min(6, 'Confirm password must be at least 6 characters'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: 'New passwords do not match',
    path: ['confirmPassword'],
  });

export const SecurityCard = memo(({ onChangePassword, isLoading }) => {
  const [showPasswords, setShowPasswords] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(securitySchema),
    defaultValues: {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  const newPasswordValue = watch('newPassword', '');

  const calculatePasswordStrength = (pass) => {
    if (!pass) return { score: 0, text: 'Empty', color: 'bg-slate-800' };
    if (pass.length < 6) return { score: 1, text: 'Weak', color: 'bg-rose-500' };
    if (pass.length >= 8 && /[A-Z]/.test(pass) && /[0-9]/.test(pass)) {
      return { score: 3, text: 'Strong', color: 'bg-emerald-500' };
    }
    return { score: 2, text: 'Medium', color: 'bg-amber-500' };
  };

  const strength = calculatePasswordStrength(newPasswordValue);

  const handleFormSubmit = (data) => {
    onChangePassword(data);
    reset();
  };

  return (
    <Card className="p-6 md:p-8 space-y-6 border-slate-800 bg-slate-900/60">
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div className="flex items-center gap-2.5">
          <div className="p-2 rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/20">
            <Lock className="w-5 h-5" />
          </div>
          <div>
            <h3 className="font-extrabold text-white text-base">Security &amp; Password Update</h3>
            <p className="text-xs text-slate-400">Update your account credentials and BCrypt hash</p>
          </div>
        </div>

        <button
          type="button"
          onClick={() => setShowPasswords(!showPasswords)}
          className="text-xs text-slate-400 hover:text-white flex items-center gap-1 font-mono"
        >
          {showPasswords ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
          {showPasswords ? 'Hide Passwords' : 'Show Passwords'}
        </button>
      </div>

      <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4" noValidate>
        <div className="space-y-1">
          <Label htmlFor="currentPassword">Current Password</Label>
          <div className="relative">
            <Lock className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
            <Input
              id="currentPassword"
              type={showPasswords ? 'text' : 'password'}
              className="pl-10"
              error={!!errors.currentPassword}
              {...register('currentPassword')}
            />
          </div>
          {errors.currentPassword && (
            <p className="text-[11px] font-semibold text-rose-400">{errors.currentPassword.message}</p>
          )}
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="space-y-1">
            <Label htmlFor="newPassword">New Password</Label>
            <div className="relative">
              <Lock className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
              <Input
                id="newPassword"
                type={showPasswords ? 'text' : 'password'}
                className="pl-10"
                error={!!errors.newPassword}
                {...register('newPassword')}
              />
            </div>
            {/* Dynamic Password Strength Indicator */}
            {newPasswordValue && (
              <div className="space-y-1 pt-1">
                <div className="flex justify-between items-center text-[10px] font-mono text-slate-400">
                  <span>Password Strength:</span>
                  <span className="font-bold text-white">{strength.text}</span>
                </div>
                <div className="w-full h-1.5 bg-slate-950 rounded-full overflow-hidden border border-slate-800">
                  <div
                    className={`h-full ${strength.color} transition-all duration-300 rounded-full`}
                    style={{ width: `${(strength.score / 3) * 100}%` }}
                  />
                </div>
              </div>
            )}
            {errors.newPassword && (
              <p className="text-[11px] font-semibold text-rose-400">{errors.newPassword.message}</p>
            )}
          </div>

          <div className="space-y-1">
            <Label htmlFor="confirmPassword">Confirm New Password</Label>
            <div className="relative">
              <Lock className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
              <Input
                id="confirmPassword"
                type={showPasswords ? 'text' : 'password'}
                className="pl-10"
                error={!!errors.confirmPassword}
                {...register('confirmPassword')}
              />
            </div>
            {errors.confirmPassword && (
              <p className="text-[11px] font-semibold text-rose-400">{errors.confirmPassword.message}</p>
            )}
          </div>
        </div>

        <Button type="submit" isLoading={isLoading} variant="primary" className="font-bold">
          Update Security Password
        </Button>
      </form>
    </Card>
  );
});
