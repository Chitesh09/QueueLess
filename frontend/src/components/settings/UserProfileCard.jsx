import React, { memo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { User, Mail, Shield, Building, CheckCircle2 } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Label } from '../ui/Label';
import { Badge } from '../ui/Badge';
import { Avatar } from '../ui/Avatar';

const profileSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters'),
  email: z.string().min(1, 'Email is required').email('Must be a valid email address'),
});

export const UserProfileCard = memo(({ user, onSave, isLoading }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      name: user?.name || 'Super Admin User',
      email: user?.email || 'superadmin@queueless.com',
    },
  });

  return (
    <Card className="p-6 md:p-8 space-y-6 border-slate-800 bg-slate-900/60">
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div className="flex items-center gap-3">
          <Avatar name={user?.name || user?.email || 'Admin'} className="w-12 h-12 text-base" />
          <div>
            <h3 className="font-extrabold text-white text-base">{user?.name || 'User Profile'}</h3>
            <p className="text-xs text-slate-400 font-mono">{user?.email}</p>
          </div>
        </div>
        <Badge variant="info" className="font-mono text-[10px] uppercase">
          {user?.role || 'SUPER_ADMIN'}
        </Badge>
      </div>

      <form onSubmit={handleSubmit(onSave)} className="space-y-4" noValidate>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="space-y-1">
            <Label htmlFor="name">Full Name</Label>
            <div className="relative">
              <User className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
              <Input id="name" className="pl-10" error={!!errors.name} {...register('name')} />
            </div>
            {errors.name && <p className="text-[11px] font-semibold text-rose-400">{errors.name.message}</p>}
          </div>

          <div className="space-y-1">
            <Label htmlFor="email">Email Address</Label>
            <div className="relative">
              <Mail className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
              <Input id="email" type="email" className="pl-10" error={!!errors.email} {...register('email')} />
            </div>
            {errors.email && <p className="text-[11px] font-semibold text-rose-400">{errors.email.message}</p>}
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs font-mono pt-2">
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 space-y-1">
            <span className="text-[10px] text-slate-500 uppercase font-bold">Assigned Role</span>
            <div className="font-bold text-sky-400">{user?.role || 'SUPER_ADMIN'}</div>
          </div>

          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 space-y-1">
            <span className="text-[10px] text-slate-500 uppercase font-bold">Tenant Organization</span>
            <div className="font-bold text-emerald-400">ID #{user?.organizationId || 1}</div>
          </div>
        </div>

        <Button type="submit" isLoading={isLoading} variant="primary" className="font-bold">
          Save Profile Changes
        </Button>
      </form>
    </Card>
  );
});
