import React from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from './Button'
import type { SnackSuggestion } from '@/lib/types'

interface SnackSuggestionsProps {
  suggestions: SnackSuggestion[]
  onAddSuggestion: (suggestion: SnackSuggestion) => void
  isLoading?: boolean
}

export const SnackSuggestions: React.FC<SnackSuggestionsProps> = ({
  suggestions,
  onAddSuggestion,
  isLoading = false,
}) => {
  const { t } = useTranslation()

  const formatNutrient = (value?: number): string => {
    if (value === null || value === undefined) return '-'
    return value.toFixed(1)
  }

  const getPheColor = (phe?: number): string => {
    if (!phe) return 'text-gray-500'
    if (phe <= 25) return 'text-green-600'
    if (phe <= 50) return 'text-yellow-600'
    return 'text-red-600'
  }

  const getItemIcon = (itemType: string) => {
    return itemType === 'PRODUCT' ? '🥕' : '🍽️'
  }

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">
          Snack Suggestions
        </h3>
        <div className="flex items-center justify-center py-8">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
          <span className="ml-3 text-gray-600">Loading suggestions...</span>
        </div>
      </div>
    )
  }

  if (suggestions.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">
          Snack Suggestions
        </h3>
        <div className="text-center py-8 text-gray-500">
          <div className="mx-auto h-12 w-12 text-gray-400 mb-4">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6-4h6m2 5.291A7.962 7.962 0 0112 20a7.962 7.962 0 01-5-1.709M15 3H9a2 2 0 00-2 2v1.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 000 1.414l6.414 6.414a1 1 0 00.707.293H9a2 2 0 002-2V9a2 2 0 012-2h2a2 2 0 012 2v10a2 2 0 01-2 2z" />
            </svg>
          </div>
          <p>No snack suggestions available</p>
          <p className="text-sm mt-1">Try adding some meals first to get personalized suggestions.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-900">
          Snack Suggestions
        </h3>
        <div className="text-sm text-gray-500">
          {suggestions.length} suggestion{suggestions.length !== 1 ? 's' : ''}
        </div>
      </div>

      <p className="text-sm text-gray-600 mb-4">
        Based on your current daily intake, here are some low-PHE snack options:
      </p>

      <div className="space-y-3">
        {suggestions.map((suggestion, index) => (
          <div
            key={`${suggestion.itemType}-${suggestion.itemId}-${index}`}
            className="border border-gray-200 rounded-lg p-4 hover:border-gray-300 transition-colors"
          >
            <div className="flex items-start justify-between">
              <div className="flex items-start space-x-3 flex-1">
                {/* Item Icon & Info */}
                <div className="flex items-center space-x-2">
                  <span className="text-lg">{getItemIcon(suggestion.itemType)}</span>
                  <div>
                    <h4 className="font-medium text-gray-900 text-sm">
                      {suggestion.itemName}
                    </h4>
                    <p className="text-xs text-gray-500">
                      {suggestion.itemType === 'PRODUCT' ? 'Product' : 'Dish'} • {formatNutrient(suggestion.quantity)} {suggestion.unit}
                    </p>
                  </div>
                </div>

                {/* Nutrition Info */}
                <div className="flex-1 min-w-0">
                  <div className="grid grid-cols-3 gap-3 text-sm">
                    <div className="text-center">
                      <div className={`font-medium ${getPheColor(suggestion.phenylalanine)}`}>
                        {formatNutrient(suggestion.phenylalanine)} mg
                      </div>
                      <div className="text-xs text-gray-500">PHE</div>
                    </div>
                    <div className="text-center">
                      <div className="font-medium text-gray-900">
                        {formatNutrient(suggestion.protein)} g
                      </div>
                      <div className="text-xs text-gray-500">Protein</div>
                    </div>
                    <div className="text-center">
                      <div className="font-medium text-gray-900">
                        {formatNutrient(suggestion.calories)} kcal
                      </div>
                      <div className="text-xs text-gray-500">Calories</div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Add Button */}
              <div className="ml-3">
                <Button
                  onClick={() => onAddSuggestion(suggestion)}
                  variant="primary"
                  size="sm"
                >
                  Add
                </Button>
              </div>
            </div>

            {/* Suggestion Reason */}
            {suggestion.reason && (
              <div className="mt-3 pt-3 border-t border-gray-100">
                <div className="flex items-start space-x-2">
                  <div className="flex-shrink-0 mt-0.5">
                    <svg className="h-4 w-4 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <p className="text-xs text-blue-700 bg-blue-50 rounded px-2 py-1">
                    {suggestion.reason}
                  </p>
                </div>
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Footer Note */}
      <div className="mt-4 pt-4 border-t border-gray-200">
        <p className="text-xs text-gray-500">
          💡 These suggestions are based on your remaining daily PHE allowance and nutritional needs. 
          Always consult with your healthcare provider for personalized dietary advice.
        </p>
      </div>
    </div>
  )
}
