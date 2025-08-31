import React, { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { useConsents } from '@/lib/api/consents'
import { useSubmitToGlobalCatalog } from '@/lib/api/labelScan'
import { Button } from './Button'
import { toast } from '@/lib/toast/toast'
import { ConsentType, type LabelScanResponse, type ExtractedProduct } from '@/lib/types'

interface SubmissionDialogProps {
  isOpen: boolean
  onClose: () => void
  scan: LabelScanResponse
  patientId: string
}

export const SubmissionDialog: React.FC<SubmissionDialogProps> = ({
  isOpen,
  onClose,
  scan,
  patientId,
}) => {
  const { t } = useTranslation()
  const [canSubmit, setCanSubmit] = useState(false)
  const [step, setStep] = useState<'consent' | 'review' | 'submit'>('consent')
  
  const { data: consentsResponse } = useConsents(patientId)
  const submitMutation = useSubmitToGlobalCatalog()

  // Check if GLOBAL_SUBMISSION_OPTIN consent is granted
  useEffect(() => {
    if (consentsResponse?.consents) {
      const submissionConsent = consentsResponse.consents.find(
        c => c.consentType === ConsentType.GLOBAL_SUBMISSION_OPTIN
      )
      const hasConsent = submissionConsent?.granted || false
      setCanSubmit(hasConsent)
      setStep(hasConsent ? 'review' : 'consent')
    }
  }, [consentsResponse])

  const handleClose = () => {
    setStep('consent')
    onClose()
  }

  const handleSubmit = async () => {
    if (!canSubmit) {
      toast.error('Consent Required', 'You must grant consent to contribute to research before submitting.')
      return
    }

    try {
      const submission = await submitMutation.mutateAsync({
        scanId: scan.id,
        patientId,
      })
      
      toast.success('Submitted Successfully', 'Your product data has been submitted to the global catalog for review.')
      setStep('submit')
      
      // Close dialog after a short delay
      setTimeout(() => {
        handleClose()
      }, 2000)
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const renderProductSummary = (product: ExtractedProduct) => {
    return (
      <div className="bg-gray-50 rounded-lg p-4">
        <h4 className="text-sm font-medium text-gray-900 mb-3">Product Information</h4>
        
        <div className="space-y-2">
          <div className="flex justify-between">
            <span className="text-sm text-gray-600">Product Name:</span>
            <span className="text-sm font-medium text-gray-900">{product.productName}</span>
          </div>
          
          {product.brand && (
            <div className="flex justify-between">
              <span className="text-sm text-gray-600">Brand:</span>
              <span className="text-sm font-medium text-gray-900">{product.brand}</span>
            </div>
          )}
          
          {product.category && (
            <div className="flex justify-between">
              <span className="text-sm text-gray-600">Category:</span>
              <span className="text-sm font-medium text-gray-900">{product.category}</span>
            </div>
          )}
          
          <div className="flex justify-between">
            <span className="text-sm text-gray-600">Ingredients:</span>
            <span className="text-sm font-medium text-gray-900">{product.ingredients.length} detected</span>
          </div>
          
          {product.nutritionFacts.per100g.phenylalanine && (
            <div className="flex justify-between">
              <span className="text-sm text-gray-600">Phenylalanine:</span>
              <span className="text-sm font-bold text-red-600">
                {product.nutritionFacts.per100g.phenylalanine}mg per 100g
              </span>
            </div>
          )}
          
          <div className="flex justify-between">
            <span className="text-sm text-gray-600">Confidence:</span>
            <span className="text-sm font-medium text-gray-900">
              {Math.round(product.confidence * 100)}%
            </span>
          </div>
        </div>
        
        {product.allergens.length > 0 && (
          <div className="mt-3">
            <span className="text-sm text-gray-600">Allergens:</span>
            <div className="flex flex-wrap gap-1 mt-1">
              {product.allergens.map((allergen, index) => (
                <span
                  key={index}
                  className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800"
                >
                  {allergen}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>
    )
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onClick={handleClose}></div>

        <div className="inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-2xl sm:w-full sm:p-6">
          {step === 'consent' && (
            <>
              <div className="mb-6">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg leading-6 font-medium text-gray-900">
                    Consent Required
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
                  To submit product data to the global catalog, you must first grant consent to contribute to research.
                </p>
              </div>

              <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4 mb-6">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <svg className="h-5 w-5 text-yellow-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                    </svg>
                  </div>
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-yellow-800">
                      Research Contribution Consent Required
                    </h3>
                    <div className="mt-2 text-sm text-yellow-700">
                      <p>
                        Submitting product data to the global catalog requires consent to contribute your anonymized data to PKU research studies.
                        This helps improve treatments and understanding of the condition.
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="space-y-4">
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <h4 className="text-sm font-medium text-blue-800 mb-2">What happens when you submit:</h4>
                  <ul className="text-sm text-blue-700 space-y-1 list-disc list-inside">
                    <li>Your product data is anonymized and added to a research database</li>
                    <li>Researchers can use this data to study PKU dietary patterns</li>
                    <li>Your personal information is never shared or linked to the data</li>
                    <li>The data helps improve PKU treatment recommendations</li>
                    <li>You can revoke consent at any time in your sharing settings</li>
                  </ul>
                </div>

                <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                  <h4 className="text-sm font-medium text-green-800 mb-2">Benefits of contributing:</h4>
                  <ul className="text-sm text-green-700 space-y-1 list-disc list-inside">
                    <li>Help expand the global PKU food database</li>
                    <li>Improve accuracy of PHE content information</li>
                    <li>Support development of better dietary management tools</li>
                    <li>Contribute to PKU research and treatment advances</li>
                  </ul>
                </div>
              </div>

              <div className="mt-8 flex justify-end space-x-3">
                <Button onClick={handleClose} variant="secondary">
                  Cancel
                </Button>
                <Button 
                  onClick={() => window.open('/share', '_blank')}
                  variant="primary"
                >
                  Grant Consent in Settings
                </Button>
              </div>
            </>
          )}

          {step === 'review' && (
            <>
              <div className="mb-6">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg leading-6 font-medium text-gray-900">
                    Submit to Global Catalog
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
                  Review the extracted product information before submitting to the global catalog.
                </p>
              </div>

              {scan.extractedProduct && renderProductSummary(scan.extractedProduct)}

              <div className="mt-6 bg-blue-50 border border-blue-200 rounded-md p-4">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <svg className="h-5 w-5 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-blue-800">
                      Submission Process
                    </h3>
                    <div className="mt-2 text-sm text-blue-700">
                      <p>
                        Your submission will be reviewed by our moderation team to ensure accuracy before being added to the global catalog.
                        This process typically takes 1-3 business days.
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="mt-8 flex justify-end space-x-3">
                <Button onClick={handleClose} variant="secondary">
                  Cancel
                </Button>
                <Button 
                  onClick={handleSubmit}
                  variant="primary"
                  disabled={submitMutation.isLoading}
                >
                  {submitMutation.isLoading ? 'Submitting...' : 'Submit to Catalog'}
                </Button>
              </div>
            </>
          )}

          {step === 'submit' && (
            <>
              <div className="mb-6">
                <div className="flex items-center justify-center">
                  <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100">
                    <svg className="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                </div>
                <div className="mt-3 text-center">
                  <h3 className="text-lg leading-6 font-medium text-gray-900">
                    Submission Successful!
                  </h3>
                  <div className="mt-2">
                    <p className="text-sm text-gray-500">
                      Your product data has been submitted to the global catalog and will be reviewed by our moderation team.
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-green-50 border border-green-200 rounded-md p-4">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <svg className="h-5 w-5 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-green-800">
                      Thank you for contributing!
                    </h3>
                    <div className="mt-2 text-sm text-green-700">
                      <p>
                        Your contribution helps improve the PKU community's access to accurate dietary information.
                        You'll receive a notification once your submission has been reviewed.
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="mt-8 flex justify-center">
                <Button onClick={handleClose} variant="primary">
                  Close
                </Button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
