import React, { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { useLabelScan } from '@/lib/api/labelScan'
import { SafetyFlagging } from './SafetyFlagging'
import type { LabelScanResponse, OCRResult, BarcodeMatch, ExtractedProduct } from '@/lib/types'

interface LabelScanProcessorProps {
  scanId: string
  onProcessingComplete?: (scan: LabelScanResponse) => void
}

export const LabelScanProcessor: React.FC<LabelScanProcessorProps> = ({
  scanId,
  onProcessingComplete,
}) => {
  const { t } = useTranslation()
  const [expandedImage, setExpandedImage] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState<'ocr' | 'barcode' | 'product'>('ocr')

  const { data: scan, isLoading, error, refetch } = useLabelScan(scanId, !!scanId)

  // Poll for updates while processing
  useEffect(() => {
    if (scan && (scan.status === 'PROCESSING' || scan.status === 'PENDING')) {
      const interval = setInterval(() => {
        refetch()
      }, 2000) // Poll every 2 seconds

      return () => clearInterval(interval)
    }
  }, [scan?.status, refetch])

  // Notify parent when processing is complete
  useEffect(() => {
    if (scan && scan.status === 'COMPLETED' && onProcessingComplete) {
      onProcessingComplete(scan)
    }
  }, [scan, onProcessingComplete])

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PENDING':
        return (
          <svg className="animate-spin h-5 w-5 text-yellow-500" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
        )
      case 'PROCESSING':
        return (
          <svg className="animate-spin h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
        )
      case 'COMPLETED':
        return (
          <svg className="h-5 w-5 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        )
      case 'FAILED':
        return (
          <svg className="h-5 w-5 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        )
      case 'PARTIAL':
        return (
          <svg className="h-5 w-5 text-yellow-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
        )
      default:
        return null
    }
  }

  const getStatusMessage = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'Scan queued for processing...'
      case 'PROCESSING':
        return 'Analyzing images and extracting data...'
      case 'COMPLETED':
        return 'Processing completed successfully'
      case 'FAILED':
        return 'Processing failed. Please try again.'
      case 'PARTIAL':
        return 'Processing completed with some issues'
      default:
        return 'Unknown status'
    }
  }

  const formatConfidence = (confidence: number): string => {
    return `${Math.round(confidence * 100)}%`
  }

  const renderOCRResults = (ocrResults: OCRResult[]) => {
    if (ocrResults.length === 0) {
      return (
        <div className="text-center py-8">
          <div className="mx-auto h-12 w-12 text-gray-400">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <h3 className="mt-2 text-sm font-medium text-gray-900">No text detected</h3>
          <p className="mt-1 text-sm text-gray-500">
            Unable to extract text from the uploaded images.
          </p>
        </div>
      )
    }

    return (
      <div className="space-y-4">
        {ocrResults.map((result, index) => (
          <div key={index} className="bg-gray-50 rounded-lg p-4">
            <div className="flex items-center justify-between mb-3">
              <h4 className="text-sm font-medium text-gray-900">
                Image {index + 1}
              </h4>
              <div className="flex items-center space-x-2">
                <span className="text-xs text-gray-500">
                  Confidence: {formatConfidence(result.confidence)}
                </span>
                {result.language && (
                  <span className="text-xs text-gray-500">
                    Language: {result.language}
                  </span>
                )}
              </div>
            </div>
            
            <div className="bg-white rounded border p-3">
              <pre className="text-sm text-gray-900 whitespace-pre-wrap font-mono">
                {result.text}
              </pre>
            </div>

            {result.regions.length > 0 && (
              <div className="mt-3">
                <h5 className="text-xs font-medium text-gray-700 mb-2">
                  Detected Regions ({result.regions.length})
                </h5>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                  {result.regions.map((region, regionIndex) => (
                    <div key={regionIndex} className="text-xs">
                      <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                        region.type === 'PRODUCT_NAME' ? 'bg-blue-100 text-blue-800' :
                        region.type === 'INGREDIENTS' ? 'bg-green-100 text-green-800' :
                        region.type === 'NUTRITION_TABLE' ? 'bg-purple-100 text-purple-800' :
                        region.type === 'ALLERGENS' ? 'bg-red-100 text-red-800' :
                        region.type === 'BARCODE' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {region.type.replace('_', ' ').toLowerCase()}
                      </span>
                      <span className="ml-2 text-gray-600">
                        {formatConfidence(region.confidence)}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    )
  }

  const renderBarcodeMatches = (barcodeMatches: BarcodeMatch[]) => {
    if (barcodeMatches.length === 0) {
      return (
        <div className="text-center py-8">
          <div className="mx-auto h-12 w-12 text-gray-400">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h3 className="mt-2 text-sm font-medium text-gray-900">No barcodes detected</h3>
          <p className="mt-1 text-sm text-gray-500">
            No barcodes were found in the uploaded images.
          </p>
        </div>
      )
    }

    return (
      <div className="space-y-4">
        {barcodeMatches.map((match, index) => (
          <div key={index} className="bg-white border rounded-lg p-4">
            <div className="flex items-start justify-between mb-3">
              <div>
                <h4 className="text-sm font-medium text-gray-900">
                  Barcode: {match.barcode}
                </h4>
                <div className="flex items-center space-x-2 mt-1">
                  <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                    match.source === 'DETECTED' ? 'bg-blue-100 text-blue-800' :
                    match.source === 'PROVIDED' ? 'bg-green-100 text-green-800' :
                    match.source === 'OPENFOODFACTS' ? 'bg-purple-100 text-purple-800' :
                    'bg-gray-100 text-gray-800'
                  }`}>
                    {match.source.toLowerCase().replace('_', ' ')}
                  </span>
                  <span className="text-xs text-gray-500">
                    Confidence: {formatConfidence(match.confidence)}
                  </span>
                  {match.verified && (
                    <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                      Verified
                    </span>
                  )}
                </div>
              </div>
            </div>

            {match.product && (
              <div className="bg-gray-50 rounded-lg p-3">
                <h5 className="text-sm font-medium text-gray-900 mb-2">
                  Matched Product
                </h5>
                <div className="space-y-2">
                  <div>
                    <span className="text-sm font-medium text-gray-700">Name:</span>
                    <span className="ml-2 text-sm text-gray-900">{match.product.productName}</span>
                  </div>
                  {match.product.brand && (
                    <div>
                      <span className="text-sm font-medium text-gray-700">Brand:</span>
                      <span className="ml-2 text-sm text-gray-900">{match.product.brand}</span>
                    </div>
                  )}
                  {match.product.category && (
                    <div>
                      <span className="text-sm font-medium text-gray-700">Category:</span>
                      <span className="ml-2 text-sm text-gray-900">{match.product.category}</span>
                    </div>
                  )}
                  {match.product.phenylalaninePer100g && (
                    <div>
                      <span className="text-sm font-medium text-gray-700">PHE per 100g:</span>
                      <span className="ml-2 text-sm text-gray-900">{match.product.phenylalaninePer100g}mg</span>
                    </div>
                  )}
                  <div className="flex items-center space-x-2 mt-2">
                    <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                      match.product.verified ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
                    }`}>
                      {match.product.verified ? 'Verified' : 'Unverified'}
                    </span>
                    <span className="text-xs text-gray-500">
                      Source: {match.product.source}
                    </span>
                  </div>
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    )
  }

  const renderExtractedProduct = (product: ExtractedProduct) => {
    return (
      <div className="bg-white border rounded-lg p-6">
        <div className="flex items-center justify-between mb-4">
          <h4 className="text-lg font-medium text-gray-900">
            Extracted Product Information
          </h4>
          <span className="text-sm text-gray-500">
            Confidence: {formatConfidence(product.confidence)}
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Basic Information */}
          <div className="space-y-4">
            <div>
              <h5 className="text-sm font-medium text-gray-700 mb-2">Basic Information</h5>
              <div className="space-y-2">
                <div>
                  <span className="text-sm font-medium text-gray-600">Product Name:</span>
                  <p className="text-sm text-gray-900">{product.productName}</p>
                </div>
                {product.brand && (
                  <div>
                    <span className="text-sm font-medium text-gray-600">Brand:</span>
                    <p className="text-sm text-gray-900">{product.brand}</p>
                  </div>
                )}
                {product.category && (
                  <div>
                    <span className="text-sm font-medium text-gray-600">Category:</span>
                    <p className="text-sm text-gray-900">{product.category}</p>
                  </div>
                )}
              </div>
            </div>

            {/* Serving Size */}
            {product.servingSize && (
              <div>
                <h5 className="text-sm font-medium text-gray-700 mb-2">Serving Size</h5>
                <p className="text-sm text-gray-900">
                  {product.servingSize.amount} {product.servingSize.unit}
                  {product.servingSize.description && ` (${product.servingSize.description})`}
                </p>
              </div>
            )}
          </div>

          {/* Nutrition Facts */}
          <div className="space-y-4">
            <div>
              <h5 className="text-sm font-medium text-gray-700 mb-2">Nutrition Facts (per 100g)</h5>
              <div className="bg-gray-50 rounded-lg p-3">
                <div className="grid grid-cols-2 gap-2 text-sm">
                  {product.nutritionFacts.per100g.energy && (
                    <div className="flex justify-between">
                      <span className="text-gray-600">Energy:</span>
                      <span className="font-medium">{product.nutritionFacts.per100g.energy} kcal</span>
                    </div>
                  )}
                  {product.nutritionFacts.per100g.protein && (
                    <div className="flex justify-between">
                      <span className="text-gray-600">Protein:</span>
                      <span className="font-medium">{product.nutritionFacts.per100g.protein}g</span>
                    </div>
                  )}
                  {product.nutritionFacts.per100g.carbohydrates && (
                    <div className="flex justify-between">
                      <span className="text-gray-600">Carbs:</span>
                      <span className="font-medium">{product.nutritionFacts.per100g.carbohydrates}g</span>
                    </div>
                  )}
                  {product.nutritionFacts.per100g.fat && (
                    <div className="flex justify-between">
                      <span className="text-gray-600">Fat:</span>
                      <span className="font-medium">{product.nutritionFacts.per100g.fat}g</span>
                    </div>
                  )}
                  {product.nutritionFacts.per100g.phenylalanine && (
                    <div className="flex justify-between col-span-2">
                      <span className="text-gray-600 font-medium">Phenylalanine:</span>
                      <span className="font-bold text-red-600">{product.nutritionFacts.per100g.phenylalanine}mg</span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Ingredients */}
        {product.ingredients.length > 0 && (
          <div className="mt-6">
            <h5 className="text-sm font-medium text-gray-700 mb-2">
              Ingredients ({product.ingredients.length})
            </h5>
            <div className="bg-gray-50 rounded-lg p-3">
              <p className="text-sm text-gray-900">
                {product.ingredients.join(', ')}
              </p>
            </div>
          </div>
        )}

        {/* Allergens */}
        {product.allergens.length > 0 && (
          <div className="mt-4">
            <h5 className="text-sm font-medium text-gray-700 mb-2">
              Allergens ({product.allergens.length})
            </h5>
            <div className="flex flex-wrap gap-2">
              {product.allergens.map((allergen, index) => (
                <span
                  key={index}
                  className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800"
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

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <span className="ml-3 text-gray-600">{t('common.loading')}</span>
      </div>
    )
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3">
            <h3 className="text-sm font-medium text-red-800">
              Processing Error
            </h3>
            <div className="mt-2 text-sm text-red-700">
              <p>Failed to load scan results. Please try again.</p>
            </div>
          </div>
        </div>
      </div>
    )
  }

  if (!scan) {
    return null
  }

  return (
    <div className="space-y-6">
      {/* Status Header */}
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            {getStatusIcon(scan.status)}
            <div>
              <h3 className="text-lg font-medium text-gray-900">
                Label Scan Results
              </h3>
              <p className="text-sm text-gray-600">
                {getStatusMessage(scan.status)}
              </p>
            </div>
          </div>
          
          {scan.processingTimeMs && (
            <div className="text-right">
              <p className="text-sm text-gray-500">
                Processed in {Math.round(scan.processingTimeMs)}ms
              </p>
              <p className="text-xs text-gray-400">
                {new Date(scan.createdAt).toLocaleString()}
              </p>
            </div>
          )}
        </div>

        {/* Processing Progress */}
        {(scan.status === 'PROCESSING' || scan.status === 'PENDING') && (
          <div className="mt-4">
            <div className="bg-gray-200 rounded-full h-2">
              <div className={`bg-blue-600 h-2 rounded-full transition-all duration-300 ${
                scan.status === 'PENDING' ? 'w-1/4' : 'w-3/4'
              }`}></div>
            </div>
            <p className="text-xs text-gray-500 mt-1">
              {scan.status === 'PENDING' ? 'Queued for processing...' : 'Analyzing images...'}
            </p>
          </div>
        )}
      </div>

      {/* Safety Flags */}
      {scan.safetyFlags.length > 0 && (
        <SafetyFlagging safetyFlags={scan.safetyFlags} />
      )}

      {/* Results Tabs */}
      {scan.status === 'COMPLETED' && (
        <div className="bg-white rounded-lg border border-gray-200">
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8 px-6" aria-label="Tabs">
              <button
                onClick={() => setActiveTab('ocr')}
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'ocr'
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                OCR Results ({scan.ocrResults.length})
              </button>
              <button
                onClick={() => setActiveTab('barcode')}
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'barcode'
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                Barcodes ({scan.barcodeMatches.length})
              </button>
              {scan.extractedProduct && (
                <button
                  onClick={() => setActiveTab('product')}
                  className={`py-4 px-1 border-b-2 font-medium text-sm ${
                    activeTab === 'product'
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  Product Data
                </button>
              )}
            </nav>
          </div>

          <div className="p-6">
            {activeTab === 'ocr' && renderOCRResults(scan.ocrResults)}
            {activeTab === 'barcode' && renderBarcodeMatches(scan.barcodeMatches)}
            {activeTab === 'product' && scan.extractedProduct && renderExtractedProduct(scan.extractedProduct)}
          </div>
        </div>
      )}
    </div>
  )
}
