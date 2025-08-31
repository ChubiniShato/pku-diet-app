import React from 'react'
import { useTranslation } from 'react-i18next'
import type { Product } from '@/lib/types'

interface ProductCardProps {
  product: Product
  onClick?: () => void
}

export const ProductCard: React.FC<ProductCardProps> = ({ product, onClick }) => {
  const { t } = useTranslation()

  const formatNutrient = (value?: number): string => {
    if (value === null || value === undefined) return '-'
    return value.toFixed(1)
  }

  const getPheColor = (phe?: number): string => {
    if (!phe) return 'text-gray-500'
    if (phe <= 50) return 'text-green-600'
    if (phe <= 100) return 'text-yellow-600'
    return 'text-red-600'
  }

  const getPheBackground = (phe?: number): string => {
    if (!phe) return 'bg-gray-100'
    if (phe <= 50) return 'bg-green-100'
    if (phe <= 100) return 'bg-yellow-100'
    return 'bg-red-100'
  }

  return (
    <div 
      className={`bg-white rounded-lg shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow ${
        onClick ? 'cursor-pointer hover:border-blue-300' : ''
      }`}
      onClick={onClick}
    >
      {/* Header */}
      <div className="mb-4">
        <div className="flex items-start justify-between">
          <div>
            <h3 className="text-lg font-semibold text-gray-900 mb-1">
              {product.productName}
            </h3>
            <p className="text-sm text-gray-600">{product.category}</p>
          </div>
          {product.productNumber && (
            <span className="text-xs text-gray-400">#{product.productNumber}</span>
          )}
        </div>
      </div>

      {/* PHE Highlight */}
      <div className={`${getPheBackground(product.phenylalanine)} rounded-lg p-3 mb-4`}>
        <div className="flex items-center justify-between">
          <span className="text-sm font-medium text-gray-700">Phenylalanine</span>
          <span className={`text-lg font-bold ${getPheColor(product.phenylalanine)}`}>
            {formatNutrient(product.phenylalanine)} mg
          </span>
        </div>
        <p className="text-xs text-gray-500 mt-1">per 100g</p>
      </div>

      {/* Nutrition Grid */}
      <div className="grid grid-cols-2 gap-3 text-sm">
        <div className="flex justify-between">
          <span className="text-gray-600">Protein:</span>
          <span className="font-medium">{formatNutrient(product.protein)}g</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-600">Carbs:</span>
          <span className="font-medium">{formatNutrient(product.carbohydrates)}g</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-600">Fats:</span>
          <span className="font-medium">{formatNutrient(product.fats)}g</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-600">Calories:</span>
          <span className="font-medium">{formatNutrient(product.kilocalories)} kcal</span>
        </div>
      </div>

      {/* Amino Acids (Collapsible) */}
      <details className="mt-4">
        <summary className="text-sm text-blue-600 cursor-pointer hover:text-blue-800">
          Show amino acids
        </summary>
        <div className="mt-2 pt-2 border-t border-gray-100 grid grid-cols-1 gap-2 text-sm">
          <div className="flex justify-between">
            <span className="text-gray-600">Leucine:</span>
            <span className="font-medium">{formatNutrient(product.leucine)} mg</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">Tyrosine:</span>
            <span className="font-medium">{formatNutrient(product.tyrosine)} mg</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">Methionine:</span>
            <span className="font-medium">{formatNutrient(product.methionine)} mg</span>
          </div>
        </div>
      </details>
    </div>
  )
}
