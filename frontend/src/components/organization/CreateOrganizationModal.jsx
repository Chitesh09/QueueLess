import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Building2, X } from 'lucide-react';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Label } from '../ui/Label';
import { Card } from '../ui/Card';

const orgSchema = z.object({
  name: z.string().min(2, 'Organization name must be at least 2 characters'),
  tenantCode: z.string().min(2, 'Tenant code is required (e.g. CITY_CARE)'),
  domain: z.string().min(3, 'Domain name is required (e.g. cityhospital.queueless.com)'),
});

export const CreateOrganizationModal = ({ isOpen, onClose, onSubmit, isLoading }) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(orgSchema),
    defaultValues: {
      name: '',
      tenantCode: '',
      domain: '',
    },
  });

  const handleFormSubmit = (data) => {
    onSubmit(data);
    reset();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 bg-slate-950/85 backdrop-blur-md flex items-center justify-center p-4">
      <Card className="max-w-md w-full p-6 rounded-3xl border border-slate-800 space-y-6 bg-slate-900/90 shadow-2xl animate-in fade-in-80 scale-in-95">
        <div className="flex items-center justify-between border-b border-slate-800 pb-4">
          <div className="flex items-center gap-2 text-white font-bold text-base">
            <Building2 className="w-5 h-5 text-sky-400" />
            Add New Tenant Organization
          </div>
          <button onClick={onClose} className="text-slate-500 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4" noValidate>
          <div className="space-y-1">
            <Label htmlFor="name">Organization Name</Label>
            <Input id="name" placeholder="e.g. St. Jude Healthcare Network" error={!!errors.name} {...register('name')} />
            {errors.name && <p className="text-[11px] font-semibold text-rose-400">{errors.name.message}</p>}
          </div>

          <div className="space-y-1">
            <Label htmlFor="tenantCode">Tenant Code</Label>
            <Input id="tenantCode" placeholder="e.g. ST_JUDE" error={!!errors.tenantCode} {...register('tenantCode')} />
            {errors.tenantCode && <p className="text-[11px] font-semibold text-rose-400">{errors.tenantCode.message}</p>}
          </div>

          <div className="space-y-1">
            <Label htmlFor="domain">Domain Endpoint</Label>
            <Input id="domain" placeholder="e.g. stjude.queueless.com" error={!!errors.domain} {...register('domain')} />
            {errors.domain && <p className="text-[11px] font-semibold text-rose-400">{errors.domain.message}</p>}
          </div>

          <div className="grid grid-cols-2 gap-3 pt-3">
            <Button type="button" onClick={onClose} variant="secondary" className="w-full text-xs">
              Cancel
            </Button>
            <Button type="submit" isLoading={isLoading} variant="primary" className="w-full text-xs font-bold">
              Create Organization
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};
