import React, { memo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Bell, ShieldCheck, Sliders, CheckCircle2, Monitor } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Label } from '../ui/Label';

const preferencesSchema = z.object({
  enableSmsNotifications: z.boolean(),
  enableEmailAlerts: z.boolean(),
  autoCallNextEnabled: z.boolean(),
  queueDisplayBoardEnabled: z.boolean(),
  maxQueueCapacity: z.coerce.number().min(10, 'Capacity must be at least 10'),
  slaThresholdMinutes: z.coerce.number().min(1, 'SLA threshold must be at least 1 minute'),
});

export const TenantPreferencesCard = memo(({ preferences, onSave, isLoading }) => {
  const { register, handleSubmit } = useForm({
    resolver: zodResolver(preferencesSchema),
    defaultValues: {
      enableSmsNotifications: preferences?.enableSmsNotifications ?? true,
      enableEmailAlerts: preferences?.enableEmailAlerts ?? true,
      autoCallNextEnabled: preferences?.autoCallNextEnabled ?? false,
      queueDisplayBoardEnabled: preferences?.queueDisplayBoardEnabled ?? true,
      maxQueueCapacity: preferences?.maxQueueCapacity || 200,
      slaThresholdMinutes: preferences?.slaThresholdMinutes || 15,
    },
  });

  return (
    <Card className="p-6 md:p-8 space-y-6 border-slate-800 bg-slate-900/60">
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div className="flex items-center gap-2.5">
          <div className="p-2 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20">
            <Sliders className="w-5 h-5" />
          </div>
          <div>
            <h3 className="font-extrabold text-white text-base">Organization &amp; SLA Preferences</h3>
            <p className="text-xs text-slate-400">Configure notification alerts, auto-dispatch rules, and display board options</p>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSave)} className="space-y-6" noValidate>
        {/* Toggle Notification & Automation Checkboxes */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <label className="flex items-center gap-3 p-4 rounded-xl bg-slate-950 border border-slate-800 cursor-pointer hover:border-slate-700 transition-all">
            <input
              type="checkbox"
              className="w-4 h-4 rounded text-sky-500 focus:ring-sky-500 bg-slate-900 border-slate-800"
              {...register('enableSmsNotifications')}
            />
            <div>
              <div className="font-bold text-white text-xs">SMS Turn Alerts</div>
              <div className="text-[10px] text-slate-400">Notify customers via SMS when called</div>
            </div>
          </label>

          <label className="flex items-center gap-3 p-4 rounded-xl bg-slate-950 border border-slate-800 cursor-pointer hover:border-slate-700 transition-all">
            <input
              type="checkbox"
              className="w-4 h-4 rounded text-sky-500 focus:ring-sky-500 bg-slate-900 border-slate-800"
              {...register('enableEmailAlerts')}
            />
            <div>
              <div className="font-bold text-white text-xs">Email SLA Reports</div>
              <div className="text-[10px] text-slate-400">Send SLA breach reports to admins</div>
            </div>
          </label>

          <label className="flex items-center gap-3 p-4 rounded-xl bg-slate-950 border border-slate-800 cursor-pointer hover:border-slate-700 transition-all">
            <input
              type="checkbox"
              className="w-4 h-4 rounded text-sky-500 focus:ring-sky-500 bg-slate-900 border-slate-800"
              {...register('autoCallNextEnabled')}
            />
            <div>
              <div className="font-bold text-white text-xs">Auto Call Next Customer</div>
              <div className="text-[10px] text-slate-400">Automatically call next when service completes</div>
            </div>
          </label>

          <label className="flex items-center gap-3 p-4 rounded-xl bg-slate-950 border border-slate-800 cursor-pointer hover:border-slate-700 transition-all">
            <input
              type="checkbox"
              className="w-4 h-4 rounded text-sky-500 focus:ring-sky-500 bg-slate-900 border-slate-800"
              {...register('queueDisplayBoardEnabled')}
            />
            <div>
              <div className="font-bold text-white text-xs">Public Queue Display Board</div>
              <div className="text-[10px] text-slate-400">Enable TV display board streaming</div>
            </div>
          </label>
        </div>

        {/* Threshold Inputs */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="space-y-1">
            <Label htmlFor="maxQueueCapacity">Max Daily Queue Capacity</Label>
            <Input id="maxQueueCapacity" type="number" {...register('maxQueueCapacity')} />
          </div>

          <div className="space-y-1">
            <Label htmlFor="slaThresholdMinutes">SLA Wait Threshold (Minutes)</Label>
            <Input id="slaThresholdMinutes" type="number" {...register('slaThresholdMinutes')} />
          </div>
        </div>

        <Button type="submit" isLoading={isLoading} variant="primary" className="font-bold">
          Save Organization Preferences
        </Button>
      </form>
    </Card>
  );
});
