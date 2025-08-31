import React from 'react'
import { useTranslation } from 'react-i18next'

export const Profile: React.FC = () => {
  const { t } = useTranslation()

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          {t('pages.profile.title')}
        </h1>
        <p className="mt-2 text-gray-600">
          {t('pages.profile.subtitle')}
        </p>
      </div>

      <div className="bg-white rounded-lg shadow p-6">
        <p className="text-gray-500">Profile management coming soon...</p>
      </div>
    </div>
  )
}
