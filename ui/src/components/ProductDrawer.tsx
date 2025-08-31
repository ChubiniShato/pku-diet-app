import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useProduct } from '@/lib/api/products'
import type { Product } from '@/lib/types'

interface ProductDrawerProps {
  productId: string | null
  isOpen: boolean
  onClose: () => void
}

export const ProductDrawer: React.FC<ProductDrawerProps> = ({
  productId,
  isOpen,
  onClose,
}) => {
  const { t } = useTranslation()
  const [servingSize, setServingSize] = useState<number>(100)
  const [viewMode, setViewMode] = useState<'per100g' | 'perServing'>('per100g')

  const { data: product, isLoading, error } = useProduct(productId || '', !!productId && isOpen)

  const formatNutrient = (value?: number, serving: number = 100): string => {
    if (value === null || value === undefined) return '-'
    if (viewMode === 'per100g') return value.toFixed(1)
    return ((value * serving) / 100).toFixed(1)
  }

  const getPheColor = (phe?: number, serving: number = 100): string => {
    if (!phe) return 'text-gray-500'
    const actualPhe = viewMode === 'per100g' ? phe : (phe * serving) / 100
    if (actualPhe <= 50) return 'text-green-600'
    if (actualPhe <= 100) return 'text-yellow-600'
    return 'text-red-600'
  }

  const getPheBackground = (phe?: number, serving: number = 100): string => {
    if (!phe) return 'bg-gray-100'
    const actualPhe = viewMode === 'per100g' ? phe : (phe * serving) / 100
    if (actualPhe <= 50) return 'bg-green-100'
    if (actualPhe <= 100) return 'bg-yellow-100'
    return 'bg-red-100'
  }

  if (!isOpen) return null

  return (
    <>
      {/* Backdrop */}
      <div 
        className="fixed inset-0 bg-black bg-opacity-50 z-40"
        onClick={onClose}
      />
      
      {/* Drawer */}
      <div className="fixed right-0 top-0 h-full w-full max-w-2xl bg-white shadow-xl z-50 overflow-y-auto">
        {/* Header */}
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold text-gray-900">
              Product Details
            </h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors"
            >
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        {/* Content */}
        <div className="p-6">
          {isLoading && (
            <div className="flex items-center justify-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              <span className="ml-3 text-gray-600">{t('common.loading')}</span>
            </div>
          )}

          {error && (
            <div className="bg-red-50 border border-red-200 rounded-md p-4">
              <p className="text-red-800">Failed to load product details</p>
            </div>
          )}

          {product && (
            <>
              {/* Product Info */}
              <div className="mb-6">
                <h3 className="text-2xl font-bold text-gray-900 mb-2">
                  {product.productName}
                </h3>
                <div className="flex items-center space-x-4 text-sm text-gray-600">
                  <span>{product.category}</span>
                  {product.productNumber && (
                    <span>#{product.productNumber}</span>
                  )}
                </div>
              </div>

              {/* View Mode Toggle */}
              <div className="mb-6">
                <div className="flex items-center space-x-4 mb-4">
                  <div className="flex bg-gray-100 rounded-lg p-1">
                    <button
                      onClick={() => setViewMode('per100g')}
                      className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                        viewMode === 'per100g'
                          ? 'bg-white text-blue-600 shadow-sm'
                          : 'text-gray-600 hover:text-gray-900'
                      }`}
                    >
                      Per 100g
                    </button>
                    <button
                      onClick={() => setViewMode('perServing')}
                      className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                        viewMode === 'perServing'
                          ? 'bg-white text-blue-600 shadow-sm'
                          : 'text-gray-600 hover:text-gray-900'
                      }`}
                    >
                      Per Serving
                    </button>
                  </div>
                  
                  {viewMode === 'perServing' && (
                    <div className="flex items-center space-x-2">
                      <label htmlFor="serving-size" className="text-sm text-gray-600">
                        Serving size:
                      </label>
                      <input
                        id="serving-size"
                        type="number"
                        min="1"
                        max="1000"
                        value={servingSize}
                        onChange={(e) => setServingSize(Number(e.target.value))}
                        className="w-20 px-2 py-1 border border-gray-300 rounded text-sm"
                      />
                      <span className="text-sm text-gray-600">g</span>
                    </div>
                  )}
                </div>
              </div>

              {/* PHE Highlight */}
              <div className={`${getPheBackground(product.phenylalanine, servingSize)} rounded-lg p-4 mb-6`}>
                <div className="flex items-center justify-between">
                  <span className="text-lg font-medium text-gray-700">Phenylalanine</span>
                  <div className="text-right">
                    <span className={`text-2xl font-bold ${getPheColor(product.phenylalanine, servingSize)}`}>
                      {formatNutrient(product.phenylalanine, servingSize)} mg
                    </span>
                    <p className="text-sm text-gray-500">
                      {viewMode === 'per100g' ? 'per 100g' : `per ${servingSize}g serving`}
                    </p>
                  </div>
                </div>
              </div>

              {/* Macronutrients */}
              <div className="mb-6">
                <h4 className="text-lg font-semibold text-gray-900 mb-4">Macronutrients</h4>
                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-gray-50 rounded-lg p-4">
                    <div className="flex justify-between items-center">
                      <span className="text-gray-600">Protein</span>
                      <span className="text-lg font-semibold text-gray-900">
                        {formatNutrient(product.protein, servingSize)}g
                      </span>
                    </div>
                  </div>
                  <div className="bg-gray-50 rounded-lg p-4">
                    <div className="flex justify-between items-center">
                      <span className="text-gray-600">Carbohydrates</span>
                      <span className="text-lg font-semibold text-gray-900">
                        {formatNutrient(product.carbohydrates, servingSize)}g
                      </span>
                    </div>
                  </div>
                  <div className="bg-gray-50 rounded-lg p-4">
                    <div className="flex justify-between items-center">
                      <span className="text-gray-600">Fats</span>
                      <span className="text-lg font-semibold text-gray-900">
                        {formatNutrient(product.fats, servingSize)}g
                      </span>
                    </div>
                  </div>
                  <div className="bg-gray-50 rounded-lg p-4">
                    <div className="flex justify-between items-center">
                      <span className="text-gray-600">Calories</span>
                      <span className="text-lg font-semibold text-gray-900">
                        {formatNutrient(product.kilocalories, servingSize)} kcal
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Amino Acids */}
              <div className="mb-6">
                <h4 className="text-lg font-semibold text-gray-900 mb-4">Amino Acids</h4>
                <div className="space-y-3">
                  <div className="flex justify-between items-center py-2 border-b border-gray-100">
                    <span className="text-gray-600">Phenylalanine</span>
                    <span className="font-medium text-gray-900">
                      {formatNutrient(product.phenylalanine, servingSize)} mg
                    </span>
                  </div>
                  <div className="flex justify-between items-center py-2 border-b border-gray-100">
                    <span className="text-gray-600">Leucine</span>
                    <span className="font-medium text-gray-900">
                      {formatNutrient(product.leucine, servingSize)} mg
                    </span>
                  </div>
                  <div className="flex justify-between items-center py-2 border-b border-gray-100">
                    <span className="text-gray-600">Tyrosine</span>
                    <span className="font-medium text-gray-900">
                      {formatNutrient(product.tyrosine, servingSize)} mg
                    </span>
                  </div>
                  <div className="flex justify-between items-center py-2 border-b border-gray-100">
                    <span className="text-gray-600">Methionine</span>
                    <span className="font-medium text-gray-900">
                      {formatNutrient(product.methionine, servingSize)} mg
                    </span>
                  </div>
                </div>
              </div>

              {/* Energy */}
              {product.kilojoules && (
                <div className="mb-6">
                  <h4 className="text-lg font-semibold text-gray-900 mb-4">Energy</h4>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="bg-gray-50 rounded-lg p-4">
                      <div className="flex justify-between items-center">
                        <span className="text-gray-600">Kilojoules</span>
                        <span className="text-lg font-semibold text-gray-900">
                          {formatNutrient(product.kilojoules, servingSize)} kJ
                        </span>
                      </div>
                    </div>
                    <div className="bg-gray-50 rounded-lg p-4">
                      <div className="flex justify-between items-center">
                        <span className="text-gray-600">Kilocalories</span>
                        <span className="text-lg font-semibold text-gray-900">
                          {formatNutrient(product.kilocalories, servingSize)} kcal
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </>
  )
}
