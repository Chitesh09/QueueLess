import React from 'react';
import { Globe } from 'lucide-react';
import { useTranslation } from '../../i18n/I18nContext';

export const LanguageSelector = () => {
  const { language, setLanguage } = useTranslation();

  const languages = [
    { code: 'en', label: 'English', flag: '🇺🇸' },
    { code: 'es', label: 'Español', flag: '🇪🇸' },
    { code: 'fr', label: 'Français', flag: '🇫🇷' },
    { code: 'hi', label: 'हिंदी', flag: '🇮🇳' },
  ];

  return (
    <div className="flex items-center gap-1.5 bg-slate-900 border border-slate-800 px-2.5 py-1 rounded-xl text-xs">
      <Globe className="w-3.5 h-3.5 text-sky-400 shrink-0" />
      <select
        value={language}
        onChange={(e) => setLanguage(e.target.value)}
        className="bg-transparent text-xs font-bold text-white focus:outline-none cursor-pointer"
        aria-label="Select Interface Language"
      >
        {languages.map((lang) => (
          <option key={lang.code} value={lang.code} className="bg-slate-950 text-white">
            {lang.flag} {lang.label}
          </option>
        ))}
      </select>
    </div>
  );
};
