import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import LanguageDetector from 'i18next-browser-languagedetector'

import en from './locales/en.json'
import ka from './locales/ka.json'
import ru from './locales/ru.json'
import uk from './locales/uk.json'

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: en },
      ka: { translation: ka },
      ru: { translation: ru },
      uk: { translation: uk },
    },
    fallbackLng: 'en',
    debug: import.meta.env.DEV,
    interpolation: {
      escapeValue: false,
    },
    // Limit to supported languages and normalize region variants
    supportedLngs: ['en', 'ka', 'ru', 'uk'],
    nonExplicitSupportedLngs: true,
    load: 'languageOnly',
    detection: {
      order: ['localStorage', 'navigator', 'htmlTag', 'querystring'],
      caches: ['localStorage'],
      lookupQuerystring: 'lng',
      convertDetectedLanguage: (lng: string) => {
        if (!lng) return 'en'
        const lower = lng.toLowerCase()
        if (lower.startsWith('ka')) return 'ka'
        if (lower.startsWith('ru')) return 'ru'
        if (lower.startsWith('uk')) return 'uk'
        if (lower.startsWith('en')) return 'en'
        return 'en'
      },
    },
  })

export default i18n
