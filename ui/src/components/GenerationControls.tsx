import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { usePantryItems } from '@/lib/api/pantry'
import { useBudget } from '@/lib/api/budget'
import { Button } from './Button'

interface GenerationControlsProps {
  onGenerateWeek: (options: GenerationOptions) => void
  onGenerateDay: (date: string, options: GenerationOptions) => void
  isGeneratingWeek?: boolean
  isGeneratingDay?: boolean
  selectedDate?: string
}

export interface GenerationOptions {
  emergencyMode: boolean
  respectPantry: boolean
}

export const GenerationControls: React.FC<GenerationControlsProps> = ({
  onGenerateWeek,
  onGenerateDay,
  isGeneratingWeek = false,
  isGeneratingDay = false,
  selectedDate,
}) => {
  const { t } = useTranslation()
  const [emergencyMode, setEmergencyMode] = useState(false)
  const [respectPantry, setRespectPantry] = useState(true)

  // Load pantry and budget data for display
  const { data: pantryResponse } = usePantryItems({ size: 100 }) // Get all pantry items
  const { data: budget } = useBudget('current-patient') // TODO: Get actual patient ID

  const pantryItems = pantryResponse?.content || []
  const pantryItemCount = pantryItems.length
  const expiringItemsCount = pantryItems.filter(item => {
    if (!item.expiryDate) return false
    const today = new Date()
    const expiry = new Date(item.expiryDate)
    const daysUntilExpiry = Math.ceil((expiry.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
    return daysUntilExpiry <= 7 && daysUntilExpiry >= 0
  }).length

  const generationOptions: GenerationOptions = {
    emergencyMode,
    respectPantry,
  }

  const handleGenerateWeek = () => {
    onGenerateWeek(generationOptions)
  }

  const handleGenerateDay = () => {
    if (selectedDate) {
      onGenerateDay(selectedDate, generationOptions)
    }
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-lg font-semibold text-gray-900">
          Menu Generation
        </h2>
        <div className="text-sm text-gray-500">
          AI-powered meal planning
        </div>
      </div>

      {/* Generation Options */}
      <div className="space-y-4 mb-6">
        <div>
          <h3 className="text-sm font-medium text-gray-700 mb-3">Generation Options</h3>
          
          <div className="space-y-3">
            {/* Emergency Mode Toggle */}
            <div className="flex items-center justify-between">
              <div className="flex-1">
                <label className="flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={emergencyMode}
                    onChange={(e) => setEmergencyMode(e.target.checked)}
                    className="h-4 w-4 text-orange-600 focus:ring-orange-500 border-gray-300 rounded"
                  />
                  <div className="ml-3">
                    <span className="text-sm font-medium text-gray-700">Emergency Mode</span>
                    <p className="text-xs text-gray-500">
                      Allows dish repeats within 2 days for urgent planning
                    </p>
                  </div>
                </label>
              </div>
              {emergencyMode && (
                <div className="ml-3">
                  <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
                    <svg className="w-3 h-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                    </svg>
                    Active
                  </span>
                </div>
              )}
            </div>

            {/* Respect Pantry Toggle */}
            <div className="flex items-center justify-between">
              <div className="flex-1">
                <label className="flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={respectPantry}
                    onChange={(e) => setRespectPantry(e.target.checked)}
                    className="h-4 w-4 text-green-600 focus:ring-green-500 border-gray-300 rounded"
                  />
                  <div className="ml-3">
                    <span className="text-sm font-medium text-gray-700">Respect Pantry</span>
                    <p className="text-xs text-gray-500">
                      Prioritize ingredients you already have in your pantry
                    </p>
                  </div>
                </label>
              </div>
              {respectPantry && (
                <div className="ml-3">
                  <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                    <svg className="w-3 h-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    Enabled
                  </span>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Pantry & Budget Status */}
        <div className="space-y-3">
          {/* Pantry Status */}
          <div className="bg-gray-50 border border-gray-200 rounded-md p-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <svg className="h-4 w-4 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                </svg>
                <span className="text-sm font-medium text-gray-700">Pantry Status</span>
              </div>
              <div className="text-right">
                <div className="text-sm font-semibold text-gray-900">
                  {pantryItemCount} items
                </div>
                {expiringItemsCount > 0 && (
                  <div className="text-xs text-yellow-600">
                    {expiringItemsCount} expiring soon
                  </div>
                )}
              </div>
            </div>
            {respectPantry && pantryItemCount > 0 && (
              <div className="mt-2 text-xs text-gray-600">
                ✓ Will prioritize using existing pantry items
              </div>
            )}
          </div>

          {/* Budget Status */}
          {budget && budget.isActive && (
            <div className="bg-green-50 border border-green-200 rounded-md p-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <svg className="h-4 w-4 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                  </svg>
                  <span className="text-sm font-medium text-green-700">Budget Active</span>
                </div>
                <div className="text-right">
                  {budget.dailyCapAmount && (
                    <div className="text-sm font-semibold text-green-900">
                      {new Intl.NumberFormat('en-US', {
                        style: 'currency',
                        currency: budget.currency,
                      }).format(budget.dailyCapAmount)}/day
                    </div>
                  )}
                </div>
              </div>
              <div className="mt-2 text-xs text-green-700">
                ✓ Will respect budget constraints during generation
              </div>
            </div>
          )}
        </div>

        {/* Generation Rules Info */}
        <div className="bg-blue-50 border border-blue-200 rounded-md p-3">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-4 w-4 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div className="ml-3">
              <h4 className="text-sm font-medium text-blue-800">Generation Rules</h4>
              <div className="mt-1 text-sm text-blue-700">
                <ul className="list-disc list-inside space-y-1">
                  <li>
                    <strong>Variety:</strong> {emergencyMode ? 'Dishes can repeat within 2 days' : 'No dish repeats within 2 days'}
                  </li>
                  <li>
                    <strong>Pantry:</strong> {respectPantry ? 'Uses available ingredients first' : 'Ignores current pantry stock'}
                  </li>
                  <li><strong>Nutrition:</strong> Targets daily PHE limits and nutritional balance</li>
                  <li><strong>Budget:</strong> {budget?.isActive ? 'Respects budget constraints' : 'No budget limits'}</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Generation Buttons */}
      <div className="space-y-3">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {/* Generate Week */}
          <Button
            onClick={handleGenerateWeek}
            disabled={isGeneratingWeek}
            variant="primary"
            className="w-full"
          >
            <div className="flex items-center justify-center space-x-2">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span>{isGeneratingWeek ? 'Generating Week...' : 'Generate Week'}</span>
            </div>
          </Button>

          {/* Generate Day */}
          <Button
            onClick={handleGenerateDay}
            disabled={isGeneratingDay || !selectedDate}
            variant="secondary"
            className="w-full"
          >
            <div className="flex items-center justify-center space-x-2">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
              <span>
                {isGeneratingDay 
                  ? 'Generating Day...' 
                  : selectedDate 
                    ? `Generate ${new Date(selectedDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}`
                    : 'Select Day First'
                }
              </span>
            </div>
          </Button>
        </div>

        {/* Selected Date Info */}
        {selectedDate && (
          <div className="text-center">
            <p className="text-sm text-gray-600">
              Selected: <span className="font-medium text-gray-900">
                {new Date(selectedDate).toLocaleDateString('en-US', {
                  weekday: 'long',
                  month: 'long',
                  day: 'numeric',
                  year: 'numeric'
                })}
              </span>
            </p>
          </div>
        )}
      </div>

      {/* Advanced Options Hint */}
      <div className="mt-6 pt-6 border-t border-gray-200">
        <div className="flex items-center justify-between text-sm">
          <span className="text-gray-500">
            💡 Advanced dietary preferences can be configured in your profile
          </span>
          <button className="text-blue-600 hover:text-blue-800 font-medium">
            Profile Settings →
          </button>
        </div>
      </div>
    </div>
  )
}
