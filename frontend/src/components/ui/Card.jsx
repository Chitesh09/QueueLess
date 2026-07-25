import React from 'react';
import { cn } from '../../utils/cn';

export const Card = ({ className, children, ...props }) => {
  return (
    <div
      className={cn(
        'glass-card rounded-2xl p-6 border border-slate-800/80 bg-slate-900/60 dark:bg-slate-900/60 text-slate-100 shadow-xl backdrop-blur-md',
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
};
