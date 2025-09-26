import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useProviderStatus, useCreateLabelScan } from '@/lib/api/labelScan'
import { ImageUploader } from '@/components/ImageUploader'
import { LabelScanProcessor } from '@/components/LabelScanProcessor'
import { SubmissionDialog } from '@/components/SubmissionDialog'
import { Button } from '@/components/Button'
import { HelpButton } from '@/components/HelpButton'
import { Tooltip } from '@/components/Tooltip'
import { toast } from '@/lib/toast/toast'
import type { LabelScanResponse } from '@/lib/types'

export const Scan: React.FC = () => {
  const { t } = useTranslation()
  const [images, setImages] = useState<File[]>([])
  const [region, setRegion] = useState('')
  const [barcode, setBarcode] = useState('')
  const [currentScanId, setCurrentScanId] = useState<string | null>(null)
  const [isSubmissionDialogOpen, setIsSubmissionDialogOpen] = useState(false)
  const [currentScan, setCurrentScan] = useState<LabelScanResponse | null>(null)

  const patientId = 'current-patient' // TODO: Get actual patient ID

  const { data: providerStatus } = useProviderStatus()
  const createScanMutation = useCreateLabelScan()

  const handleImagesChange = (newImages: File[]) => {
    setImages(newImages)
  }

  const handleStartScan = async () => {
    if (images.length === 0) {
      toast.error('No Images', 'Please upload at least one image to scan.')
      return
    }

    try {
      const scanResult = await createScanMutation.mutateAsync({
        images,
        region: region.trim() || undefined,
        barcode: barcode.trim() || undefined,
        patientId,
      })

      setCurrentScanId(scanResult.id)
      setCurrentScan(scanResult)
      toast.success('Scan Started', 'Your images are being processed...')
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handleProcessingComplete = (scan: LabelScanResponse) => {
    setCurrentScan(scan)
  }

  const handleSubmitToGlobal = () => {
    if (currentScan && currentScan.submissionEligible) {
      setIsSubmissionDialogOpen(true)
    } else {
      toast.error('Not Eligible', 'This scan is not eligible for submission to the global catalog.')
    }
  }

  const handleNewScan = () => {
    setImages([])
    setRegion('')
    setBarcode('')
    setCurrentScanId(null)
    setCurrentScan(null)
    setIsSubmissionDialogOpen(false)
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          {t('pages.scan.title')}
        </h1>
        <p className="mt-2 text-gray-600">
          {t('pages.scan.subtitle')}
        </p>
      </div>

      {/* Help Button */}
      <HelpButton page="scan" />

      {/* Provider Status Warning */}
      {providerStatus?.fallbackMode && (
        <div className="mb-6 bg-yellow-50 border border-yellow-200 rounded-md p-4">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-yellow-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
              </svg>
            </div>
            <div className="ml-3">
              <h3 className="text-sm font-medium text-yellow-800">
                Limited Functionality
              </h3>
              <div className="mt-2 text-sm text-yellow-700">
                <p>
                  Some scanning providers are not configured. The system is running in fallback mode with limited OCR and barcode recognition capabilities.
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-8">
        {/* Main Content */}
        <div className="xl:col-span-2 space-y-8">
          {!currentScanId ? (
            <>
              {/* Upload Section */}
              <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">
                  Upload Product Images
                </h2>
                <p className="text-sm text-gray-600 mb-6">
                  Upload clear photos of product labels, nutrition facts, and ingredient lists. Multiple angles help improve accuracy.
                </p>
                
                <ImageUploader
                  onImagesChange={handleImagesChange}
                  maxFiles={5}
                  maxSizePerFile={10}
                  disabled={createScanMutation.isPending}
                />
              </div>

              {/* Optional Information */}
              <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">
                  Additional Information (Optional)
                </h2>
                <p className="text-sm text-gray-600 mb-4">
                  Provide additional context to improve scanning accuracy.
                </p>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label htmlFor="region" className="block text-sm font-medium text-gray-700 mb-2">
                      Region/Country
                    </label>
                    <input
                      type="text"
                      id="region"
                      value={region}
                      onChange={(e) => setRegion(e.target.value)}
                      placeholder="e.g., USA, UK, Germany"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      disabled={createScanMutation.isPending}
                    />
                    <p className="text-xs text-gray-500 mt-1">
                      Helps with language detection and regional product databases
                    </p>
                  </div>
                  
                  <div>
                    <label htmlFor="barcode" className="block text-sm font-medium text-gray-700 mb-2">
                      Barcode (if visible)
                    </label>
                    <input
                      type="text"
                      id="barcode"
                      value={barcode}
                      onChange={(e) => setBarcode(e.target.value)}
                      placeholder="e.g., 1234567890123"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      disabled={createScanMutation.isPending}
                    />
                    <p className="text-xs text-gray-500 mt-1">
                      Manual entry if barcode detection fails
                    </p>
                  </div>
                </div>
              </div>

              {/* Scan Button */}
              <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-medium text-gray-900">
                      Ready to Scan?
                    </h3>
                    <p className="text-sm text-gray-600">
                      Upload at least one image to start processing.
                    </p>
                  </div>
                  
                  <Button
                    onClick={handleStartScan}
                    variant="primary"
                    disabled={images.length === 0 || createScanMutation.isPending}
                    className="px-8"
                  >
                    {createScanMutation.isPending ? 'Processing...' : 'Start Scan'}
                  </Button>
                </div>
              </div>
            </>
          ) : (
            <>
              {/* Scan Results */}
              <LabelScanProcessor 
                scanId={currentScanId}
                onProcessingComplete={handleProcessingComplete}
              />

              {/* Actions */}
              {currentScan && currentScan.status === 'COMPLETED' && (
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-lg font-medium text-gray-900">
                        Scan Complete
                      </h3>
                      <p className="text-sm text-gray-600">
                        Choose what to do with your scan results.
                      </p>
                    </div>
                    
                    <div className="flex space-x-3">
                      <Button
                        onClick={handleNewScan}
                        variant="secondary"
                      >
                        New Scan
                      </Button>
                      
                      {currentScan.submissionEligible && (
                        <Button
                          onClick={handleSubmitToGlobal}
                          variant="primary"
                        >
                          Submit to Global Catalog
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              )}
            </>
          )}
        </div>

        {/* Sidebar */}
        <div className="xl:col-span-1 space-y-6">
          {/* Scanning Tips */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Scanning Tips
            </h3>
            
            <div className="space-y-4">
              <div className="flex items-start space-x-3">
                <svg className="h-5 w-5 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div>
                  <p className="text-sm font-medium text-gray-900">Good lighting</p>
                  <p className="text-sm text-gray-600">
                    Ensure labels are well-lit and clearly visible
                  </p>
                </div>
              </div>
              
              <div className="flex items-start space-x-3">
                <svg className="h-5 w-5 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div>
                  <p className="text-sm font-medium text-gray-900">Multiple angles</p>
                  <p className="text-sm text-gray-600">
                    Capture front label, nutrition facts, and ingredients list
                  </p>
                </div>
              </div>
              
              <div className="flex items-start space-x-3">
                <svg className="h-5 w-5 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div>
                  <p className="text-sm font-medium text-gray-900">Avoid glare</p>
                  <p className="text-sm text-gray-600">
                    Position to avoid reflections and shadows
                  </p>
                </div>
              </div>
              
              <div className="flex items-start space-x-3">
                <svg className="h-5 w-5 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div>
                  <p className="text-sm font-medium text-gray-900">Focus on text</p>
                  <p className="text-sm text-gray-600">
                    Ensure nutrition facts and ingredients are in focus
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Provider Status */}
          {providerStatus && (
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">
                Service Status
              </h3>
              
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">OCR Processing:</span>
                  <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                    providerStatus.ocrEnabled 
                      ? 'bg-green-100 text-green-800' 
                      : 'bg-red-100 text-red-800'
                  }`}>
                    {providerStatus.ocrEnabled ? 'Available' : 'Unavailable'}
                  </span>
                </div>
                
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Barcode Detection:</span>
                  <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                    providerStatus.barcodeEnabled 
                      ? 'bg-green-100 text-green-800' 
                      : 'bg-red-100 text-red-800'
                  }`}>
                    {providerStatus.barcodeEnabled ? 'Available' : 'Unavailable'}
                  </span>
                </div>
                
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Safety Checking:</span>
                  <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                    providerStatus.safetyCheckEnabled 
                      ? 'bg-green-100 text-green-800' 
                      : 'bg-red-100 text-red-800'
                  }`}>
                    {providerStatus.safetyCheckEnabled ? 'Available' : 'Unavailable'}
                  </span>
                </div>
                
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Global Submission:</span>
                  <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                    providerStatus.submissionEnabled 
                      ? 'bg-green-100 text-green-800' 
                      : 'bg-red-100 text-red-800'
                  }`}>
                    {providerStatus.submissionEnabled ? 'Available' : 'Unavailable'}
                  </span>
                </div>
              </div>
              
              {providerStatus.fallbackMode && (
                <div className="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded-md">
                  <p className="text-xs text-yellow-800">
                    Running in fallback mode with limited functionality.
                  </p>
                </div>
              )}
            </div>
          )}

          {/* What Gets Scanned */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              What We Extract
            </h3>
            
            <div className="space-y-3">
              <div className="flex items-start space-x-2">
                <svg className="h-4 w-4 text-blue-500 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <div>
                  <p className="text-sm font-medium text-gray-900">Product Information</p>
                  <p className="text-xs text-gray-600">Name, brand, category</p>
                </div>
              </div>
              
              <div className="flex items-start space-x-2">
                <svg className="h-4 w-4 text-blue-500 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
                <div>
                  <p className="text-sm font-medium text-gray-900">Nutrition Facts</p>
                  <p className="text-xs text-gray-600">PHE, protein, calories, etc.</p>
                </div>
              </div>
              
              <div className="flex items-start space-x-2">
                <svg className="h-4 w-4 text-blue-500 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 10h16M4 14h16M4 18h16" />
                </svg>
                <div>
                  <p className="text-sm font-medium text-gray-900">Ingredients List</p>
                  <p className="text-xs text-gray-600">Complete ingredient breakdown</p>
                </div>
              </div>
              
              <div className="flex items-start space-x-2">
                <svg className="h-4 w-4 text-blue-500 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
                <div>
                  <p className="text-sm font-medium text-gray-900">Safety Alerts</p>
                  <p className="text-xs text-gray-600">Allergens, forbidden ingredients</p>
                </div>
              </div>
              
              <div className="flex items-start space-x-2">
                <svg className="h-4 w-4 text-blue-500 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div>
                  <p className="text-sm font-medium text-gray-900">Barcode Matching</p>
                  <p className="text-xs text-gray-600">Product database lookups</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Submission Dialog */}
      {currentScan && (
        <SubmissionDialog
          isOpen={isSubmissionDialogOpen}
          onClose={() => setIsSubmissionDialogOpen(false)}
          scan={currentScan}
          patientId={patientId}
        />
      )}
    </div>
  )
}
