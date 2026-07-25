import React, { createContext, useContext, useState } from 'react';
import { translations } from './translations';

const I18nContext = createContext(null);

export const I18nProvider = ({ children }) => {
  const [language, setLanguageState] = useState(() => {
    return localStorage.getItem('queueless_language') || 'en';
  });

  const setLanguage = (lang) => {
    setLanguageState(lang);
    localStorage.setItem('queueless_language', lang);
  };

  // Supports both nested keys (e.g. 'navigation.dashboard') and flat keys ('dashboard')
  const t = (key) => {
    if (!key) return '';

    const langObj = translations[language] || translations['en'];
    const fallbackObj = translations['en'];

    // Resolve nested object path if dot notation exists
    if (key.includes('.')) {
      const keys = key.split('.');
      let current = langObj;
      for (const k of keys) {
        current = current?.[k];
      }
      if (current !== undefined) return current;

      // Fallback to English nested path
      let fbCurrent = fallbackObj;
      for (const k of keys) {
        fbCurrent = fbCurrent?.[k];
      }
      if (fbCurrent !== undefined) return fbCurrent;
    }

    // Direct key lookup or fallback
    return langObj?.[key] || fallbackObj?.[key] || key;
  };

  return (
    <I18nContext.Provider value={{ language, setLanguage, t }}>
      {children}
    </I18nContext.Provider>
  );
};

export const useTranslation = () => {
  const context = useContext(I18nContext);
  if (!context) {
    throw new Error('useTranslation must be used within an I18nProvider');
  }
  return context;
};
