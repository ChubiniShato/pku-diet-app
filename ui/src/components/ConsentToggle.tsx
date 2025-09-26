import React from 'react'
import { useTranslation } from 'react-i18next'
import { useConsents, useUpdateConsent } from '@/lib/api/consents'
import { toast } from '@/lib/toast/toast'
import type { ConsentType } from '@/lib/types'

interface ConsentToggleProps {
  patientId: string
  consentType: ConsentType
  title: string
  description: string
  onConsentChange?: (granted: boolean) => void
}

export const ConsentToggle: React.FC<ConsentToggleProps> = ({
  patientId,
  consentType,
  title,
  description,
  onConsentChange,
}) => {
  const { t } = useTranslation()
  const { data: consentsResponse, isLoading } = useConsents(patientId)
  const updateMutation = useUpdateConsent()

  const consent = consentsResponse?.consents.find(c => c.consentType === consentType)
  const isGranted = consent?.granted || false

  const handleToggle = async () => {
    const newGranted = !isGranted

    try {
      await updateMutation.mutateAsync({
        patientId,
        request: {
          consentType,
          granted: newGranted,
        },
      })

      toast.success(
        newGranted ? 'Consent Granted' : 'Consent Revoked',
        `${title} has been ${newGranted ? 'enabled' : 'disabled'}`
      )

      onConsentChange?.(newGranted)
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const getConsentIcon = (granted: boolean) => {
    if (granted) {
      return (
        <svg className="h-5 w-5 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      )
    }
    return (
      <svg className="h-5 w-5 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    )
  }

  const getStatusBadge = (granted: boolean) => {
    if (granted) {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
          Granted
        </span>
      )
    }
    return (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
        Not Granted
      </span>
    )
  }

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="animate-pulse">
          <div className="flex items-center space-x-3">
            <div className="h-5 w-5 bg-gray-300 rounded-full"></div>
            <div className="h-4 bg-gray-300 rounded w-1/3"></div>
          </div>
          <div className="mt-2 h-3 bg-gray-300 rounded w-2/3"></div>
        </div>
      </div>
    )
  }

  return (
    <div className={`bg-white rounded-lg shadow-sm border p-6 transition-all ${
      isGranted ? 'border-green-200 bg-green-50' : 'border-gray-200'
    }`}>
      <div className="flex items-start justify-between">
        <div className="flex items-start space-x-3">
          <div className="flex-shrink-0 mt-1">
            {getConsentIcon(isGranted)}
          </div>
          <div className="flex-1">
            <div className="flex items-center space-x-2 mb-1">
              <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
              {getStatusBadge(isGranted)}
            </div>
            <p className="text-sm text-gray-600 mb-4">{description}</p>
            
            {/* Consent Details */}
            {consent && (
              <div className="text-xs text-gray-500 space-y-1">
                {isGranted && consent.grantedAt && (
                  <p>
                    <span className="font-medium">Granted:</span>{' '}
                    {new Date(consent.grantedAt).toLocaleString()}
                  </p>
                )}
                {!isGranted && consent.revokedAt && (
                  <p>
                    <span className="font-medium">Revoked:</span>{' '}
                    {new Date(consent.revokedAt).toLocaleString()}
                  </p>
                )}
                <p>
                  <span className="font-medium">Last updated:</span>{' '}
                  {new Date(consent.updatedAt).toLocaleString()}
                </p>
              </div>
            )}
          </div>
        </div>

        <div className="flex-shrink-0">
          <label className="relative inline-flex items-center cursor-pointer">
            <input
              type="checkbox"
              checked={isGranted}
              onChange={handleToggle}
              disabled={updateMutation.isPending}
              className="sr-only peer"
            />
            <div className={`relative w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 ${
              isGranted ? 'peer-checked:bg-green-600' : ''
            } ${updateMutation.isPending ? 'opacity-50 cursor-not-allowed' : ''}`}></div>
          </label>
        </div>
      </div>

      {/* Additional Information */}
      {consentType === 'SHARE_WITH_DOCTOR' && (
        <div className={`mt-4 p-3 rounded-md ${
          isGranted ? 'bg-green-100 border border-green-200' : 'bg-yellow-100 border border-yellow-200'
        }`}>
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className={`h-4 w-4 ${isGranted ? 'text-green-400' : 'text-yellow-400'}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div className="ml-3">
              <p className={`text-sm ${isGranted ? 'text-green-800' : 'text-yellow-800'}`}>
                {isGranted 
                  ? 'You can now create and share data with healthcare providers.'
                  : 'Sharing features are disabled. Enable this consent to share your data with doctors.'
                }
              </p>
            </div>
          </div>
        </div>
      )}

      {consentType === 'GLOBAL_SUBMISSION_OPTIN' && (
        <div className={`mt-4 p-3 rounded-md ${
          isGranted ? 'bg-blue-100 border border-blue-200' : 'bg-gray-100 border border-gray-200'
        }`}>
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className={`h-4 w-4 ${isGranted ? 'text-blue-400' : 'text-gray-400'}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <div className="ml-3">
              <p className={`text-sm ${isGranted ? 'text-blue-800' : 'text-gray-800'}`}>
                {isGranted 
                  ? 'Your anonymized data may be used for research to improve PKU treatments.'
                  : 'Your data will not be included in research studies.'
                }
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
