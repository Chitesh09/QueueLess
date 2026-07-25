import React from 'react';
import { cn } from '../../utils/cn';

export const Input = React.forwardRef(({ className, error, ...props }, ref) => {
  return (
    <input
      ref={ref}
      className={cn(
        'w-full bg-slate-900/90 dark:bg-slate-900 border border-slate-700/80 rounded-xl px-4 py-2.5 text-sm font-medium text-slate-100 placeholder-slate-500 focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all disabled:opacity-50',
        error && 'border-rose-500 focus:border-rose-500 focus:ring-rose-500',
        className
      )}
      {...props}
    />
  );
});
Input.displayName = 'Input';
