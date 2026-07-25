import React from 'react';
import { cn } from '../../utils/cn';

export const Label = ({ className, children, ...props }) => {
  return (
    <label
      className={cn('block text-xs font-semibold text-slate-300 dark:text-slate-300 mb-1.5', className)}
      {...props}
    >
      {children}
    </label>
  );
};
