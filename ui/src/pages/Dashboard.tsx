import React from 'react'
import { useTranslation } from 'react-i18next'
import { TestApiConnection } from '@/components/TestApiConnection'

export const Dashboard: React.FC = () => {
  const { t } = useTranslation()

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          {t('pages.dashboard.title')}
        </h1>
        <p className="mt-2 text-gray-600">
          {t('pages.dashboard.subtitle')}
        </p>
      </div>

      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-lg font-medium text-gray-900 mb-4">
          API Connection Status
        </h2>
        <TestApiConnection />
      </div>
    </div>
  )
}
