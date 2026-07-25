import React from 'react';
import { cn } from '../../utils/cn';

export const Separator = ({ className, orientation = 'horizontal' }) => {
  return (
    <div
      className={cn(
        'bg-slate-800 shrink-0',
        orientation === 'horizontal' ? 'h-[1px] w-full my-4' : 'h-full w-[1px] mx-4',
        className
      )}
    />
  );
};
