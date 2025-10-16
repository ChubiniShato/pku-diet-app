import React from 'react'
import { useTranslation } from 'react-i18next'
import type { MenuDay } from '@/lib/types'

interface NutrientTotalsBarProps {
  day: MenuDay
  targetPhenylalanine?: number
  targetProtein?: number
  targetCalories?: number
}

export const NutrientTotalsBar: React.FC<NutrientTotalsBarProps> = ({
  day,
  targetPhenylalanine = 300, // Default PKU daily limit
  targetProtein = 50,
  targetCalories = 2000,
}) => {
  const { t } = useTranslation()

  // Calculate totals across all slots
  const totals = day.slots.reduce(
    (acc, slot) => {
      const slotTotals = slot.entries.reduce(
        (slotAcc, entry) => ({
          plannedPhenylalanine: slotAcc.plannedPhenylalanine + (entry.plannedPhenylalanine || 0),
          plannedProtein: slotAcc.plannedProtein + (entry.plannedProtein || 0),
          plannedCalories: slotAcc.plannedCalories + (entry.plannedCalories || 0),
          consumedPhenylalanine: slotAcc.consumedPhenylalanine + (entry.consumedPhenylalanine || 0),
          consumedProtein: slotAcc.consumedProtein + (entry.consumedProtein || 0),
          consumedCalories: slotAcc.consumedCalories + (entry.consumedCalories || 0),
        }),
        {
          plannedPhenylalanine: 0,
          plannedProtein: 0,
          plannedCalories: 0,
          consumedPhenylalanine: 0,
          consumedProtein: 0,
          consumedCalories: 0,
        }
      )
      
      return {
        plannedPhenylalanine: acc.plannedPhenylalanine + slotTotals.plannedPhenylalanine,
        plannedProtein: acc.plannedProtein + slotTotals.plannedProtein,
        plannedCalories: acc.plannedCalories + slotTotals.plannedCalories,
        consumedPhenylalanine: acc.consumedPhenylalanine + slotTotals.consumedPhenylalanine,
        consumedProtein: acc.consumedProtein + slotTotals.consumedProtein,
        consumedCalories: acc.consumedCalories + slotTotals.consumedCalories,
      }
    },
    {
      plannedPhenylalanine: 0,
      plannedProtein: 0,
      plannedCalories: 0,
      consumedPhenylalanine: 0,
      consumedProtein: 0,
      consumedCalories: 0,
    }
  )

  const formatNutrient = (value: number): string => {
    return value.toFixed(1)
  }

  const getProgressColor = (current: number, target: number, isPhenylalanine = false): string => {
    const percentage = (current / target) * 100
    
    if (isPhenylalanine) {
      // For PHE, red means over limit
      if (percentage > 100) return 'bg-red-500'
      if (percentage > 80) return 'bg-yellow-500'
      return 'bg-green-500'
    } else {
      // For protein/calories, green means meeting target
      if (percentage < 50) return 'bg-red-500'
      if (percentage < 80) return 'bg-yellow-500'
      return 'bg-green-500'
    }
  }

  const getStatusColor = (current: number, target: number, isPhenylalanine = false): string => {
    const percentage = (current / target) * 100
    
    if (isPhenylalanine) {
      if (percentage > 100) return 'text-red-600'
      if (percentage > 80) return 'text-yellow-600'
      return 'text-green-600'
    } else {
      if (percentage < 50) return 'text-red-600'
      if (percentage < 80) return 'text-yellow-600'
      return 'text-green-600'
    }
  }

  const NutrientBar = ({ 
    label, 
    planned, 
    consumed, 
    target, 
    unit, 
    isPhenylalanine = false 
  }: {
    label: string
    planned: number
    consumed: number
    target: number
    unit: string
    isPhenylalanine?: boolean
  }) => {
    const plannedPercentage = Math.min((planned / target) * 100, 100)
    const consumedPercentage = Math.min((consumed / target) * 100, 100)
    
    return (
      <div className="space-y-2">
        <div className="flex justify-between items-center">
          <span className="text-sm font-medium text-gray-700">{label}</span>
          <div className="text-right">
            <div className="text-sm">
              <span className={`font-semibold ${getStatusColor(consumed, target, isPhenylalanine)}`}>
                {formatNutrient(consumed)}
              </span>
              <span className="text-gray-500 mx-1">/</span>
              <span className="text-gray-600">
                {formatNutrient(planned)}
              </span>
              <span className="text-gray-500 mx-1">/</span>
              <span className="text-gray-600">
                {formatNutrient(target)} {unit}
              </span>
            </div>
            <div className="text-xs text-gray-500">
              Consumed / Planned / Target
            </div>
          </div>
        </div>
        
        {/* Progress Bar */}
        <div className="relative w-full bg-gray-200 rounded-full h-3">
          {/* Planned amount (background) */}
          <div
            className="absolute top-0 left-0 h-3 bg-gray-300 rounded-full transition-all duration-300"
            style={{ width: `${plannedPercentage}%` }}
          />
          {/* Consumed amount (foreground) */}
          <div
            className={`absolute top-0 left-0 h-3 rounded-full transition-all duration-300 ${getProgressColor(consumed, target, isPhenylalanine)}`}
            style={{ width: `${consumedPercentage}%` }}
          />
          {/* Target line */}
          <div className="absolute top-0 right-0 w-0.5 h-3 bg-gray-800 rounded-full" />
        </div>
        
        <div className="flex justify-between text-xs text-gray-500">
          <span>
            {formatNutrient((consumed / target) * 100)}% consumed
          </span>
          <span>
            {formatNutrient((planned / target) * 100)}% planned
          </span>
        </div>
      </div>
    )
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 sticky top-4">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-lg font-semibold text-gray-900">Daily Nutrition</h2>
        <div className="text-sm text-gray-500">
          {day.date}
        </div>
      </div>

      <div className="space-y-6">
        {/* Phenylalanine - Most Important */}
        <NutrientBar
          label="Phenylalanine"
          planned={totals.plannedPhenylalanine}
          consumed={totals.consumedPhenylalanine}
          target={targetPhenylalanine}
          unit="mg"
          isPhenylalanine={true}
        />

        {/* Protein */}
        <NutrientBar
          label="Protein"
          planned={totals.plannedProtein}
          consumed={totals.consumedProtein}
          target={targetProtein}
          unit="g"
        />

        {/* Calories */}
        <NutrientBar
          label="Calories"
          planned={totals.plannedCalories}
          consumed={totals.consumedCalories}
          target={targetCalories}
          unit="kcal"
        />
      </div>

      {/* Summary Stats */}
      <div className="mt-6 pt-6 border-t border-gray-200">
        <div className="grid grid-cols-2 gap-4 text-center">
          <div>
            <div className="text-lg font-semibold text-gray-900">
              {day.slots.reduce((acc, slot) => acc + slot.entries.length, 0)}
            </div>
            <div className="text-sm text-gray-500">Total Items</div>
          </div>
          <div>
            <div className="text-lg font-semibold text-gray-900">
              {day.slots.length}
            </div>
            <div className="text-sm text-gray-500">Meal Slots</div>
          </div>
        </div>
      </div>

      {/* PHE Status Alert */}
      {totals.consumedPhenylalanine > targetPhenylalanine && (
        <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-md">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <h3 className="text-sm font-medium text-red-800">
                PHE Limit Exceeded
              </h3>
              <div className="mt-2 text-sm text-red-700">
                <p>
                  You've consumed {formatNutrient(totals.consumedPhenylalanine - targetPhenylalanine)} mg 
                  over your daily PHE limit.
                </p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
