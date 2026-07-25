import React, { useState, useEffect } from 'react';
import { Eye } from 'lucide-react';

export const HighContrastToggle = () => {
  const [isHighContrast, setIsHighContrast] = useState(() => {
    return localStorage.getItem('queueless_high_contrast') === 'true';
  });

  useEffect(() => {
    if (isHighContrast) {
      document.documentElement.classList.add('high-contrast');
    } else {
      document.documentElement.classList.remove('high-contrast');
    }
    localStorage.setItem('queueless_high_contrast', String(isHighContrast));
  }, [isHighContrast]);

  return (
    <button
      onClick={() => setIsHighContrast(!isHighContrast)}
      className={`p-2 rounded-xl border text-xs font-mono transition-all flex items-center gap-1.5 ${
        isHighContrast
          ? 'bg-yellow-400 text-black border-yellow-300 font-bold'
          : 'bg-slate-900 border-slate-800 text-slate-400 hover:text-white'
      }`}
      title="Toggle High Contrast Accessibility Mode"
      aria-label="Toggle High Contrast Mode"
    >
      <Eye className="w-3.5 h-3.5" />
      <span className="hidden xl:inline">High Contrast</span>
    </button>
  );
};
