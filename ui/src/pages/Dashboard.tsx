import React, { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { TestApiConnection } from '@/components/TestApiConnection'
import { useAuth } from '@/contexts/AuthContext'

export const Dashboard: React.FC = () => {
  const { t } = useTranslation()
  const { user } = useAuth()
  const navigate = useNavigate()

  // Redirect ADMIN users to AdminPanel
  useEffect(() => {
    if (user?.role === 'ADMIN') {
      navigate('/admin')
    }
  }, [user?.role, navigate])



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

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* API Connection Status */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-4">
            API Connection Status
          </h2>
          <TestApiConnection />
        </div>

        {/* Additional Dashboard Content */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-4">
            {t('pages.dashboard.welcome')}
          </h2>
          <p className="text-gray-600">
            {t('pages.dashboard.welcomeMessage')}
          </p>
        </div>
      </div>

    </div>
  )
}
