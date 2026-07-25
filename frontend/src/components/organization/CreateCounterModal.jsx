import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { MonitorPlay, X } from 'lucide-react';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Label } from '../ui/Label';
import { Card } from '../ui/Card';

const counterSchema = z.object({
  name: z.string().min(2, 'Counter name is required (e.g. Counter 3 - OPD)'),
  departmentId: z.string().min(1, 'Please select a department'),
});

export const CreateCounterModal = ({ isOpen, onClose, onSubmit, isLoading, departments }) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(counterSchema),
    defaultValues: {
      name: '',
      departmentId: '1',
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
            <MonitorPlay className="w-5 h-5 text-purple-400" />
            Add New Counter Station
          </div>
          <button onClick={onClose} className="text-slate-500 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4" noValidate>
          <div className="space-y-1">
            <Label htmlFor="name">Counter Station Name</Label>
            <Input id="name" placeholder="e.g. Counter 3 - Triage Desk" error={!!errors.name} {...register('name')} />
            {errors.name && <p className="text-[11px] font-semibold text-rose-400">{errors.name.message}</p>}
          </div>

          <div className="space-y-1">
            <Label htmlFor="departmentId">Assigned Department</Label>
            <select
              id="departmentId"
              {...register('departmentId')}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs font-semibold text-white focus:outline-none focus:border-purple-500"
            >
              {departments && departments.length > 0 ? (
                departments.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name}
                  </option>
                ))
              ) : (
                <>
                  <option value="1">Emergency Triage &amp; OPD</option>
                  <option value="2">General Cardiology OPD</option>
                  <option value="3">Cash &amp; Deposit Desk</option>
                </>
              )}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3 pt-3">
            <Button type="button" onClick={onClose} variant="secondary" className="w-full text-xs">
              Cancel
            </Button>
            <Button type="submit" isLoading={isLoading} variant="primary" className="w-full text-xs font-bold">
              Create Counter
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};
