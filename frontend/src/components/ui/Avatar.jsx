import React from 'react';
import { cn } from '../../utils/cn';

export const Avatar = ({ name, className }) => {
  const initials = name
    ? name
        .split(' ')
        .map((n) => n[0])
        .join('')
        .substring(0, 2)
        .toUpperCase()
    : 'U';

  return (
    <div
      className={cn(
        'w-9 h-9 rounded-full bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center text-white font-bold text-xs shadow-md shadow-sky-500/20 ring-2 ring-slate-800',
        className
      )}
    >
      {initials}
    </div>
  );
};
