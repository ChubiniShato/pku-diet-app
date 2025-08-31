import React from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { MenuDay } from '@/lib/types'

interface WeekCalendarProps {
  days: MenuDay[]
  weekStartDay?: 'monday' | 'sunday'
  onDayClick?: (day: MenuDay) => void
}

export const WeekCalendar: React.FC<WeekCalendarProps> = ({
  days,
  weekStartDay = 'monday',
  onDayClick,
}) => {
  const { t } = useTranslation()

  // Calculate nutrition totals for a day
  const calculateDayTotals = (day: MenuDay) => {
    return day.slots.reduce(
      (dayAcc, slot) => {
        const slotTotals = slot.entries.reduce(
          (slotAcc, entry) => ({
            plannedPhenylalanine: slotAcc.plannedPhenylalanine + (entry.plannedPhenylalanine || 0),
            plannedProtein: slotAcc.plannedProtein + (entry.plannedProtein || 0),
            plannedCalories: slotAcc.plannedCalories + (entry.plannedCalories || 0),
            plannedFats: slotAcc.plannedFats + (entry.plannedFats || 0),
            consumedPhenylalanine: slotAcc.consumedPhenylalanine + (entry.consumedPhenylalanine || 0),
            consumedProtein: slotAcc.consumedProtein + (entry.consumedProtein || 0),
            consumedCalories: slotAcc.consumedCalories + (entry.consumedCalories || 0),
            consumedFats: slotAcc.consumedFats + (entry.consumedFats || 0),
          }),
          {
            plannedPhenylalanine: 0,
            plannedProtein: 0,
            plannedCalories: 0,
            plannedFats: 0,
            consumedPhenylalanine: 0,
            consumedProtein: 0,
            consumedCalories: 0,
            consumedFats: 0,
          }
        )
        
        return {
          plannedPhenylalanine: dayAcc.plannedPhenylalanine + slotTotals.plannedPhenylalanine,
          plannedProtein: dayAcc.plannedProtein + slotTotals.plannedProtein,
          plannedCalories: dayAcc.plannedCalories + slotTotals.plannedCalories,
          plannedFats: dayAcc.plannedFats + slotTotals.plannedFats,
          consumedPhenylalanine: dayAcc.consumedPhenylalanine + slotTotals.consumedPhenylalanine,
          consumedProtein: dayAcc.consumedProtein + slotTotals.consumedProtein,
          consumedCalories: dayAcc.consumedCalories + slotTotals.consumedCalories,
          consumedFats: dayAcc.consumedFats + slotTotals.consumedFats,
        }
      },
      {
        plannedPhenylalanine: 0,
        plannedProtein: 0,
        plannedCalories: 0,
        plannedFats: 0,
        consumedPhenylalanine: 0,
        consumedProtein: 0,
        consumedCalories: 0,
        consumedFats: 0,
      }
    )
  }

  // Get status badge color based on nutrition values
  const getStatusColor = (consumed: number, planned: number, target: number, isPhenylalanine = false): string => {
    const value = consumed || planned // Use consumed if available, otherwise planned
    const percentage = (value / target) * 100

    if (isPhenylalanine) {
      // For PHE, red means over limit
      if (percentage > 100) return 'bg-red-500'
      if (percentage > 80) return 'bg-yellow-500'
      return 'bg-green-500'
    } else {
      // For protein/calories/fats, green means meeting target
      if (percentage < 50) return 'bg-red-500'
      if (percentage < 80) return 'bg-yellow-500'
      return 'bg-green-500'
    }
  }

  const formatNutrient = (value: number): string => {
    return value.toFixed(0)
  }

  const getDayOfWeekName = (date: string): string => {
    return new Date(date).toLocaleDateString('en-US', { weekday: 'short' })
  }

  const getDayOfMonth = (date: string): string => {
    return new Date(date).getDate().toString()
  }

  const isToday = (date: string): boolean => {
    const today = new Date().toISOString().split('T')[0]
    return date === today
  }

  const isPast = (date: string): boolean => {
    const today = new Date().toISOString().split('T')[0]
    return date < today
  }

  // Sort days based on week start preference
  const sortedDays = [...days].sort((a, b) => {
    const dateA = new Date(a.date)
    const dateB = new Date(b.date)
    
    if (weekStartDay === 'sunday') {
      // Sunday = 0, Monday = 1, etc.
      const dayA = dateA.getDay()
      const dayB = dateB.getDay()
      return dayA - dayB
    } else {
      // Monday = 0, Tuesday = 1, ..., Sunday = 6
      const dayA = (dateA.getDay() + 6) % 7
      const dayB = (dateB.getDay() + 6) % 7
      return dayA - dayB
    }
  })

  const NutritionBadge = ({ 
    label, 
    consumed, 
    planned, 
    target, 
    unit, 
    isPhenylalanine = false 
  }: {
    label: string
    consumed: number
    planned: number
    target: number
    unit: string
    isPhenylalanine?: boolean
  }) => {
    const value = consumed || planned
    const statusColor = getStatusColor(consumed, planned, target, isPhenylalanine)
    
    return (
      <div className="flex items-center space-x-1 text-xs">
        <div className={`w-2 h-2 rounded-full ${statusColor}`} />
        <span className="text-gray-600 font-medium">{label}:</span>
        <span className="text-gray-900 font-semibold">
          {formatNutrient(value)}{unit}
        </span>
      </div>
    )
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
      <div className="grid grid-cols-7 gap-0">
        {sortedDays.map((day) => {
          const totals = calculateDayTotals(day)
          const dayOfWeek = getDayOfWeekName(day.date)
          const dayOfMonth = getDayOfMonth(day.date)
          const today = isToday(day.date)
          const past = isPast(day.date)
          
          return (
            <div
              key={day.date}
              className={`border-r border-b border-gray-200 last:border-r-0 ${
                today ? 'bg-blue-50 border-blue-200' : ''
              } ${past ? 'bg-gray-50' : ''}`}
            >
              <Link
                to={`/day/${day.date}`}
                className="block p-4 hover:bg-gray-50 transition-colors h-full"
                onClick={() => onDayClick?.(day)}
              >
                {/* Day Header */}
                <div className="flex items-center justify-between mb-3">
                  <div>
                    <div className={`text-xs font-medium ${
                      today ? 'text-blue-600' : 'text-gray-500'
                    }`}>
                      {dayOfWeek}
                    </div>
                    <div className={`text-lg font-bold ${
                      today ? 'text-blue-900' : past ? 'text-gray-600' : 'text-gray-900'
                    }`}>
                      {dayOfMonth}
                    </div>
                  </div>
                  
                  {/* Today indicator */}
                  {today && (
                    <div className="w-2 h-2 bg-blue-500 rounded-full" />
                  )}
                </div>

                {/* Meal slots indicator */}
                <div className="flex items-center space-x-1 mb-3">
                  {day.slots.map((slot, index) => {
                    const hasEntries = slot.entries.length > 0
                    const isEmpty = slot.entries.length === 0
                    
                    return (
                      <div
                        key={slot.id}
                        className={`w-3 h-3 rounded-full text-xs flex items-center justify-center ${
                          hasEntries
                            ? 'bg-green-500 text-white'
                            : 'bg-gray-200 text-gray-400'
                        }`}
                        title={`${slot.slotType}: ${slot.entries.length} items`}
                      >
                        {hasEntries ? '✓' : '○'}
                      </div>
                    )
                  })}
                </div>

                {/* Nutrition badges */}
                <div className="space-y-2">
                  <NutritionBadge
                    label="PHE"
                    consumed={totals.consumedPhenylalanine}
                    planned={totals.plannedPhenylalanine}
                    target={300}
                    unit="mg"
                    isPhenylalanine={true}
                  />
                  
                  <NutritionBadge
                    label="Pro"
                    consumed={totals.consumedProtein}
                    planned={totals.plannedProtein}
                    target={50}
                    unit="g"
                  />
                  
                  <NutritionBadge
                    label="Cal"
                    consumed={totals.consumedCalories}
                    planned={totals.plannedCalories}
                    target={2000}
                    unit=""
                  />
                  
                  <NutritionBadge
                    label="Fat"
                    consumed={totals.consumedFats}
                    planned={totals.plannedFats}
                    target={65}
                    unit="g"
                  />
                </div>

                {/* Generation status */}
                {day.isGenerated && (
                  <div className="mt-2 pt-2 border-t border-gray-100">
                    <div className="flex items-center space-x-1">
                      <div className="w-1.5 h-1.5 bg-blue-400 rounded-full" />
                      <span className="text-xs text-blue-600 font-medium">Generated</span>
                    </div>
                  </div>
                )}
                
                {/* Emergency mode indicator */}
                {day.emergencyMode && (
                  <div className="mt-1">
                    <div className="flex items-center space-x-1">
                      <div className="w-1.5 h-1.5 bg-orange-400 rounded-full" />
                      <span className="text-xs text-orange-600 font-medium">Emergency</span>
                    </div>
                  </div>
                )}
              </Link>
            </div>
          )
        })}
      </div>

      {/* Week summary footer */}
      <div className="bg-gray-50 px-4 py-3 border-t border-gray-200">
        <div className="flex items-center justify-between text-sm">
          <div className="flex items-center space-x-4">
            <span className="text-gray-600">Week Summary:</span>
            <div className="flex items-center space-x-1">
              <div className="w-2 h-2 bg-green-500 rounded-full" />
              <span className="text-gray-700">Planned</span>
            </div>
            <div className="flex items-center space-x-1">
              <div className="w-2 h-2 bg-blue-500 rounded-full" />
              <span className="text-gray-700">Generated</span>
            </div>
            <div className="flex items-center space-x-1">
              <div className="w-2 h-2 bg-orange-500 rounded-full" />
              <span className="text-gray-700">Emergency</span>
            </div>
          </div>
          
          <div className="text-gray-500">
            {days.filter(day => day.isGenerated).length} of {days.length} days generated
          </div>
        </div>
      </div>
    </div>
  )
}
