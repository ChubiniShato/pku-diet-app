import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useCreateShareLink } from '@/lib/api/consents'
import { Button } from './Button'
import { toast } from '@/lib/toast/toast'
import type { ShareLinkCreateRequest, ShareLinkResponse } from '@/lib/types'
import { ShareScope } from '@/lib/types'

interface ShareLinkModalProps {
  isOpen: boolean
  onClose: () => void
  patientId: string
}

export const ShareLinkModal: React.FC<ShareLinkModalProps> = ({
  isOpen,
  onClose,
  patientId,
}) => {
  const { t } = useTranslation()
  const [step, setStep] = useState<'config' | 'result'>('config')
  const [shareResult, setShareResult] = useState<ShareLinkResponse | null>(null)
  
  const [formData, setFormData] = useState<ShareLinkCreateRequest>({
    scope: ShareScope.CRITICAL_FACTS,
    ttlHours: 24,
    oneTimeUse: false,
    otpRequired: false,
    qrCodeEnabled: true,
    maxAccess: undefined,
    emailInvite: '',
  })

  const createMutation = useCreateShareLink()

  const handleClose = () => {
    setStep('config')
    setShareResult(null)
    setFormData({
      scope: ShareScope.CRITICAL_FACTS,
      ttlHours: 24,
      oneTimeUse: false,
      otpRequired: false,
      qrCodeEnabled: true,
      maxAccess: undefined,
      emailInvite: '',
    })
    onClose()
  }

  const handleCreate = async () => {
    try {
      const result = await createMutation.mutateAsync({
        patientId,
        request: formData,
      })
      
      setShareResult(result)
      setStep('result')
      toast.success('Share Link Created', 'Your secure share link has been generated')
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handleCopyLink = async () => {
    if (shareResult?.shareLink.shareUrl) {
      try {
        await navigator.clipboard.writeText(shareResult.shareLink.shareUrl)
        toast.success('Link Copied', 'Share link copied to clipboard')
      } catch (error) {
        toast.error('Copy Failed', 'Could not copy link to clipboard')
      }
    }
  }

  const handleCopyOTP = async () => {
    if (shareResult?.otp) {
      try {
        await navigator.clipboard.writeText(shareResult.otp)
        toast.success('OTP Copied', 'One-time password copied to clipboard')
      } catch (error) {
        toast.error('Copy Failed', 'Could not copy OTP to clipboard')
      }
    }
  }

  const getScopeDescription = (scope: ShareScope): string => {
    switch (scope) {
      case ShareScope.CRITICAL_FACTS:
        return 'Share critical dietary information and PHE limits'
      case ShareScope.DAY:
        return 'Share a specific day\'s meal plan and nutrition data'
      case ShareScope.WEEK:
        return 'Share a week\'s meal plans and nutrition summary'
      case ShareScope.RANGE:
        return 'Share meal plans and nutrition data for a custom date range'
      default:
        return 'Share selected data'
    }
  }

  const getTTLDescription = (hours?: number): string => {
    if (!hours) return 'No expiration'
    if (hours < 24) return `${hours} hour${hours > 1 ? 's' : ''}`
    const days = Math.floor(hours / 24)
    return `${days} day${days > 1 ? 's' : ''}`
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onClick={handleClose}></div>

        <div className="inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-2xl sm:w-full sm:p-6">
          {step === 'config' && (
            <>
              <div className="mb-6">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg leading-6 font-medium text-gray-900">
                    Create Share Link
                  </h3>
                  <button
                    onClick={handleClose}
                    className="text-gray-400 hover:text-gray-600"
                  >
                    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
                <p className="mt-1 text-sm text-gray-500">
                  Generate a secure link to share your data with healthcare providers
                </p>
              </div>

              <div className="space-y-6">
                {/* Scope Selection */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-3">
                    What to Share
                  </label>
                  <div className="space-y-3">
                    {Object.values(ShareScope).map((scope) => (
                      <label key={scope} className="flex items-start cursor-pointer">
                        <input
                          type="radio"
                          name="scope"
                          value={scope}
                          checked={formData.scope === scope}
                          onChange={(e) => setFormData({ ...formData, scope: e.target.value as ShareScope })}
                          className="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                        />
                        <div className="ml-3">
                          <div className="text-sm font-medium text-gray-900">
                            {scope.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())}
                          </div>
                          <div className="text-sm text-gray-500">
                            {getScopeDescription(scope)}
                          </div>
                        </div>
                      </label>
                    ))}
                  </div>
                </div>

                {/* Date Selection for DAY/WEEK/RANGE */}
                {(formData.scope === ShareScope.DAY || formData.scope === ShareScope.WEEK || formData.scope === ShareScope.RANGE) && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Date Selection
                    </label>
                    {formData.scope === ShareScope.DAY && (
                      <input
                        type="date"
                        value={formData.scopeData?.date || ''}
                        onChange={(e) => setFormData({
                          ...formData,
                          scopeData: { ...formData.scopeData, date: e.target.value }
                        })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    )}
                    {formData.scope === ShareScope.WEEK && (
                      <input
                        type="week"
                        value={formData.scopeData?.date || ''}
                        onChange={(e) => setFormData({
                          ...formData,
                          scopeData: { ...formData.scopeData, date: e.target.value }
                        })}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    )}
                    {formData.scope === ShareScope.RANGE && (
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block text-xs text-gray-500 mb-1">Start Date</label>
                          <input
                            type="date"
                            value={formData.scopeData?.startDate || ''}
                            onChange={(e) => setFormData({
                              ...formData,
                              scopeData: { ...formData.scopeData, startDate: e.target.value }
                            })}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                          />
                        </div>
                        <div>
                          <label className="block text-xs text-gray-500 mb-1">End Date</label>
                          <input
                            type="date"
                            value={formData.scopeData?.endDate || ''}
                            onChange={(e) => setFormData({
                              ...formData,
                              scopeData: { ...formData.scopeData, endDate: e.target.value }
                            })}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                          />
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* Expiration */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Link Expiration
                  </label>
                  <select
                    value={formData.ttlHours || ''}
                    onChange={(e) => setFormData({ 
                      ...formData, 
                      ttlHours: e.target.value ? parseInt(e.target.value) : undefined 
                    })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">No expiration</option>
                    <option value="1">1 hour</option>
                    <option value="6">6 hours</option>
                    <option value="24">1 day</option>
                    <option value="72">3 days</option>
                    <option value="168">1 week</option>
                    <option value="720">1 month</option>
                  </select>
                </div>

                {/* Access Options */}
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <div className="text-sm font-medium text-gray-900">One-time use</div>
                      <div className="text-sm text-gray-500">Link becomes invalid after first access</div>
                    </div>
                    <label className="relative inline-flex items-center cursor-pointer">
                      <input
                        type="checkbox"
                        checked={formData.oneTimeUse}
                        onChange={(e) => setFormData({ ...formData, oneTimeUse: e.target.checked })}
                        className="sr-only peer"
                      />
                      <div className="relative w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                    </label>
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <div className="text-sm font-medium text-gray-900">Require OTP</div>
                      <div className="text-sm text-gray-500">Recipient needs one-time password</div>
                    </div>
                    <label className="relative inline-flex items-center cursor-pointer">
                      <input
                        type="checkbox"
                        checked={formData.otpRequired}
                        onChange={(e) => setFormData({ ...formData, otpRequired: e.target.checked })}
                        className="sr-only peer"
                      />
                      <div className="relative w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                    </label>
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <div className="text-sm font-medium text-gray-900">Generate QR Code</div>
                      <div className="text-sm text-gray-500">Easy mobile access via QR code</div>
                    </div>
                    <label className="relative inline-flex items-center cursor-pointer">
                      <input
                        type="checkbox"
                        checked={formData.qrCodeEnabled}
                        onChange={(e) => setFormData({ ...formData, qrCodeEnabled: e.target.checked })}
                        className="sr-only peer"
                      />
                      <div className="relative w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                    </label>
                  </div>
                </div>

                {/* Email Invite */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Email Invite (Optional)
                  </label>
                  <input
                    type="email"
                    value={formData.emailInvite}
                    onChange={(e) => setFormData({ ...formData, emailInvite: e.target.value })}
                    placeholder="doctor@example.com"
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                  <p className="mt-1 text-sm text-gray-500">
                    Send the share link directly to this email address
                  </p>
                </div>

                {/* Max Access Limit */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Access Limit (Optional)
                  </label>
                  <input
                    type="number"
                    min="1"
                    max="100"
                    value={formData.maxAccess || ''}
                    onChange={(e) => setFormData({ 
                      ...formData, 
                      maxAccess: e.target.value ? parseInt(e.target.value) : undefined 
                    })}
                    placeholder="Unlimited"
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                  <p className="mt-1 text-sm text-gray-500">
                    Maximum number of times this link can be accessed
                  </p>
                </div>
              </div>

              <div className="mt-8 flex justify-end space-x-3">
                <Button onClick={handleClose} variant="secondary">
                  Cancel
                </Button>
                <Button 
                  onClick={handleCreate} 
                  variant="primary"
                  disabled={createMutation.isPending}
                >
                  {createMutation.isPending ? 'Creating...' : 'Create Share Link'}
                </Button>
              </div>
            </>
          )}

          {step === 'result' && shareResult && (
            <>
              <div className="mb-6">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg leading-6 font-medium text-gray-900">
                    Share Link Created
                  </h3>
                  <button
                    onClick={handleClose}
                    className="text-gray-400 hover:text-gray-600"
                  >
                    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
                <p className="mt-1 text-sm text-gray-500">
                  Your secure share link is ready to use
                </p>
              </div>

              <div className="space-y-6">
                {/* Share Link */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Share Link
                  </label>
                  <div className="flex">
                    <input
                      type="text"
                      value={shareResult.shareLink.shareUrl}
                      readOnly
                      className="flex-1 px-3 py-2 border border-gray-300 rounded-l-md bg-gray-50 text-sm"
                    />
                    <button
                      onClick={handleCopyLink}
                      className="px-4 py-2 bg-blue-600 text-white rounded-r-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                      Copy
                    </button>
                  </div>
                </div>

                {/* OTP */}
                {shareResult.otp && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      One-Time Password
                    </label>
                    <div className="flex">
                      <input
                        type="text"
                        value={shareResult.otp}
                        readOnly
                        className="flex-1 px-3 py-2 border border-gray-300 rounded-l-md bg-gray-50 text-lg font-mono text-center"
                      />
                      <button
                        onClick={handleCopyOTP}
                        className="px-4 py-2 bg-green-600 text-white rounded-r-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500"
                      >
                        Copy
                      </button>
                    </div>
                    <p className="mt-1 text-sm text-gray-500">
                      Share this password separately with the recipient
                    </p>
                  </div>
                )}

                {/* QR Code */}
                {shareResult.qrCodeDataUrl && (
                  <div className="text-center">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      QR Code
                    </label>
                    <img 
                      src={shareResult.qrCodeDataUrl} 
                      alt="QR Code for share link"
                      className="mx-auto border border-gray-300 rounded-lg"
                    />
                    <p className="mt-2 text-sm text-gray-500">
                      Scan with mobile device for easy access
                    </p>
                  </div>
                )}

                {/* Link Details */}
                <div className="bg-gray-50 rounded-lg p-4">
                  <h4 className="text-sm font-medium text-gray-900 mb-3">Link Details</h4>
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span className="text-gray-600">Scope:</span>
                      <span className="font-medium">
                        {shareResult.shareLink.scope.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Expires:</span>
                      <span className="font-medium">
                        {shareResult.shareLink.expiresAt 
                          ? new Date(shareResult.shareLink.expiresAt).toLocaleString()
                          : 'Never'
                        }
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Access Limit:</span>
                      <span className="font-medium">
                        {shareResult.shareLink.maxAccess || 'Unlimited'}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">One-time use:</span>
                      <span className="font-medium">
                        {shareResult.shareLink.oneTimeUse ? 'Yes' : 'No'}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="mt-8 flex justify-end">
                <Button onClick={handleClose} variant="primary">
                  Done
                </Button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
