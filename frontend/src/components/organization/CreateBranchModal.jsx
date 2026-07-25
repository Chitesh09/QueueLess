import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { MapPin, X } from 'lucide-react';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Label } from '../ui/Label';
import { Card } from '../ui/Card';

const branchSchema = z.object({
  name: z.string().min(2, 'Branch name must be at least 2 characters'),
  code: z.string().min(2, 'Branch code is required (e.g. WEST-01)'),
  address: z.string().min(5, 'Valid address is required'),
});

export const CreateBranchModal = ({ isOpen, onClose, onSubmit, isLoading }) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(branchSchema),
    defaultValues: {
      name: '',
      code: '',
      address: '',
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
            <MapPin className="w-5 h-5 text-sky-400" />
            Add New Branch Location
          </div>
          <button onClick={onClose} className="text-slate-500 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4" noValidate>
          <div className="space-y-1">
            <Label htmlFor="name">Branch Name</Label>
            <Input id="name" placeholder="e.g. West Wing Campus" error={!!errors.name} {...register('name')} />
            {errors.name && <p className="text-[11px] font-semibold text-rose-400">{errors.name.message}</p>}
          </div>

          <div className="space-y-1">
            <Label htmlFor="code">Branch Code</Label>
            <Input id="code" placeholder="e.g. WEST-01" error={!!errors.code} {...register('code')} />
            {errors.code && <p className="text-[11px] font-semibold text-rose-400">{errors.code.message}</p>}
          </div>

          <div className="space-y-1">
            <Label htmlFor="address">Address</Label>
            <Input id="address" placeholder="e.g. 450 Medical Boulevard, North Wing" error={!!errors.address} {...register('address')} />
            {errors.address && <p className="text-[11px] font-semibold text-rose-400">{errors.address.message}</p>}
          </div>

          <div className="grid grid-cols-2 gap-3 pt-3">
            <Button type="button" onClick={onClose} variant="secondary" className="w-full text-xs">
              Cancel
            </Button>
            <Button type="submit" isLoading={isLoading} variant="primary" className="w-full text-xs font-bold">
              Create Branch
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};
