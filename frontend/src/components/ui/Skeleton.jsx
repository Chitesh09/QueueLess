import React from 'react';
import { cn } from '../../utils/cn';

export const Skeleton = ({ className, ...props }) => {
  return (
    <div
      className={cn('animate-pulse rounded-lg bg-slate-800/80 dark:bg-slate-800/80', className)}
      {...props}
    />
  );
};
