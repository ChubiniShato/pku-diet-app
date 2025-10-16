import React, { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { useConsents } from '@/lib/api/consents'
import { ConsentToggle } from '@/components/ConsentToggle'
import { ShareLinkModal } from '@/components/ShareLinkModal'
import { ShareLinkManager } from '@/components/ShareLinkManager'
import { Button } from '@/components/Button'
import { ConsentType } from '@/lib/types'

export const Share: React.FC = () => {
  const { t } = useTranslation()
  const [isShareModalOpen, setIsShareModalOpen] = useState(false)
  const [canShare, setCanShare] = useState(false)
  
  const patientId = 'current-patient' // TODO: Get actual patient ID
  const { data: consentsResponse } = useConsents(patientId)

  // Check if SHARE_WITH_DOCTOR consent is granted
  useEffect(() => {
    if (consentsResponse?.consents) {
      const shareConsent = consentsResponse.consents.find(
        c => c.consentType === ConsentType.SHARE_WITH_DOCTOR
      )
      setCanShare(shareConsent?.granted || false)
    }
  }, [consentsResponse])

  const handleConsentChange = (granted: boolean) => {
    setCanShare(granted)
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          {t('pages.share.title')}
        </h1>
        <p className="mt-2 text-gray-600">
          {t('pages.share.subtitle')}
        </p>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-8">
        {/* Main Content */}
        <div className="xl:col-span-2 space-y-8">
          {/* Consents Section */}
          <div>
            <div className="mb-6">
              <h2 className="text-xl font-semibold text-gray-900">Data Sharing Consents</h2>
              <p className="mt-1 text-sm text-gray-600">
                Manage your data sharing permissions and privacy settings
              </p>
            </div>

            <div className="space-y-6">
              {/* SHARE_WITH_DOCTOR Consent */}
              <ConsentToggle
                patientId={patientId}
                consentType={ConsentType.SHARE_WITH_DOCTOR}
                title="Share with Healthcare Providers"
                description="Allow sharing of your PKU dietary data with doctors and healthcare professionals for medical consultation and treatment planning."
                onConsentChange={handleConsentChange}
              />

              {/* GLOBAL_SUBMISSION_OPTIN Consent */}
              <ConsentToggle
                patientId={patientId}
                consentType={ConsentType.GLOBAL_SUBMISSION_OPTIN}
                title="Contribute to Research"
                description="Allow your anonymized dietary data to be used in PKU research studies to help improve treatments and understanding of the condition."
              />
            </div>
          </div>

          {/* Sharing Actions Section */}
          <div>
            <div className="mb-6">
              <div className="flex items-center justify-between">
                <div>
                  <h2 className="text-xl font-semibold text-gray-900">Share Your Data</h2>
                  <p className="mt-1 text-sm text-gray-600">
                    Create secure links to share your dietary data with healthcare providers
                  </p>
                </div>
                
                <Button
                  onClick={() => setIsShareModalOpen(true)}
                  variant="primary"
                  disabled={!canShare}
                >
                  Create Share Link
                </Button>
              </div>

              {/* Consent Enforcement Notice */}
              {!canShare && (
                <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4">
                  <div className="flex">
                    <div className="flex-shrink-0">
                      <svg className="h-5 w-5 text-yellow-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                      </svg>
                    </div>
                    <div className="ml-3">
                      <h3 className="text-sm font-medium text-yellow-800">
                        Sharing Disabled
                      </h3>
                      <div className="mt-2 text-sm text-yellow-700">
                        <p>
                          To create share links, you must first grant consent to "Share with Healthcare Providers" above.
                          This ensures you have explicitly authorized sharing your medical data.
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* Share Links Manager */}
            {canShare && (
              <ShareLinkManager patientId={patientId} />
            )}
          </div>
        </div>

        {/* Sidebar */}
        <div className="xl:col-span-1 space-y-6">
          {/* Privacy & Security Info */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Privacy & Security
            </h3>
            
            <div className="space-y-4">
              <div className="flex items-start space-x-3">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.602-4.777l1.13 1.13L12 16.485 5.268 9.753l1.13-1.13L12 14.225l5.602-5.602z" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-900">End-to-end encryption</p>
                  <p className="text-sm text-gray-600">
                    All shared data is encrypted in transit and at rest
                  </p>
                </div>
              </div>
              
              <div className="flex items-start space-x-3">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-900">Access control</p>
                  <p className="text-sm text-gray-600">
                    Set expiration dates, access limits, and OTP requirements
                  </p>
                </div>
              </div>
              
              <div className="flex items-start space-x-3">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-900">Audit logging</p>
                  <p className="text-sm text-gray-600">
                    Complete audit trail of all access and sharing activities
                  </p>
                </div>
              </div>
              
              <div className="flex items-start space-x-3">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728L5.636 5.636m12.728 12.728L18.364 5.636M5.636 18.364l12.728-12.728" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-900">Instant revocation</p>
                  <p className="text-sm text-gray-600">
                    Revoke access to any share link at any time
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Sharing Best Practices */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Sharing Best Practices
            </h3>
            
            <div className="space-y-3 text-sm text-gray-600">
              <div className="flex items-start space-x-2">
                <span className="font-medium text-blue-600">1.</span>
                <p>Only share with trusted healthcare providers</p>
              </div>
              
              <div className="flex items-start space-x-2">
                <span className="font-medium text-blue-600">2.</span>
                <p>Set appropriate expiration dates for temporary access</p>
              </div>
              
              <div className="flex items-start space-x-2">
                <span className="font-medium text-blue-600">3.</span>
                <p>Use one-time links for sensitive consultations</p>
              </div>
              
              <div className="flex items-start space-x-2">
                <span className="font-medium text-blue-600">4.</span>
                <p>Enable OTP for additional security when needed</p>
              </div>
              
              <div className="flex items-start space-x-2">
                <span className="font-medium text-blue-600">5.</span>
                <p>Regularly review and revoke unused share links</p>
              </div>
              
              <div className="flex items-start space-x-2">
                <span className="font-medium text-blue-600">6.</span>
                <p>Check audit logs to monitor access to your data</p>
              </div>
            </div>
          </div>

          {/* HIPAA Compliance Notice */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <div className="flex">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div className="ml-3">
                <h4 className="text-sm font-medium text-blue-800">
                  HIPAA Compliant Sharing
                </h4>
                <div className="mt-2 text-sm text-blue-700">
                  <p>
                    This platform follows HIPAA guidelines for secure sharing of protected health information (PHI).
                    Your data is encrypted and access is logged for compliance.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Share Link Modal */}
      <ShareLinkModal
        isOpen={isShareModalOpen}
        onClose={() => setIsShareModalOpen(false)}
        patientId={patientId}
      />
    </div>
  )
}
