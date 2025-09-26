import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { useMenuWeek, useGenerateWeek, useGenerateDay } from '@/lib/api/menus'
import { useCriticalFactsForDateRange } from '@/lib/api/criticalFacts'
import { WeekCalendar } from '@/components/WeekCalendar'
import { GenerationControls, type GenerationOptions } from '@/components/GenerationControls'
import { Button } from '@/components/Button'
import { HelpButton } from '@/components/HelpButton'
import { Tooltip } from '@/components/Tooltip'
import { toast } from '@/lib/toast/toast'
import type { MenuDay } from '@/lib/types'

export const Week: React.FC = () => {
  const { t } = useTranslation()
  const [selectedDate, setSelectedDate] = useState<string | null>(null)
  const [weekStartDay, setWeekStartDay] = useState<'monday' | 'sunday'>('monday')
  
  // Calculate current week's start date (Monday by default)
  const getCurrentWeekStart = (): string => {
    const today = new Date()
    const dayOfWeek = today.getDay()
    const diff = weekStartDay === 'monday' 
      ? (dayOfWeek === 0 ? -6 : 1 - dayOfWeek) // Monday = 1, Sunday = 0
      : -dayOfWeek // Sunday = 0
    
    const weekStart = new Date(today)
    weekStart.setDate(today.getDate() + diff)
    return weekStart.toISOString().split('T')[0]
  }

  const [currentWeekStart, setCurrentWeekStart] = useState(getCurrentWeekStart())
  const patientId = 'current-patient' // TODO: Get actual patient ID

  const { data: menuWeek, isLoading, error } = useMenuWeek(currentWeekStart)
  
  // Calculate week end date for critical facts query
  const getWeekEndDate = (startDate: string): string => {
    const start = new Date(startDate)
    const end = new Date(start)
    end.setDate(start.getDate() + 6)
    return end.toISOString().split('T')[0]
  }
  
  const { data: criticalFacts } = useCriticalFactsForDateRange(
    patientId,
    currentWeekStart,
    getWeekEndDate(currentWeekStart)
  )
  const generateWeekMutation = useGenerateWeek()
  const generateDayMutation = useGenerateDay()

  const handleDayClick = (day: MenuDay) => {
    setSelectedDate(day.date)
  }

  const handleGenerateWeek = async (options: GenerationOptions) => {
    try {
      const request = {
        date: currentWeekStart,
        pheLimit: 300, // Default PKU limit
        emergencyMode: options.emergencyMode,
        respectPantry: options.respectPantry,
      }
      
      const generatedWeek = await generateWeekMutation.mutateAsync(request)
      toast.success('Week Generated', `Generated menu for week starting ${new Date(currentWeekStart).toLocaleDateString()}`)
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handleGenerateDay = async (date: string, options: GenerationOptions) => {
    try {
      const request = {
        date,
        pheLimit: 300, // Default PKU limit
        emergencyMode: options.emergencyMode,
        respectPantry: options.respectPantry,
      }
      
      const generatedDay = await generateDayMutation.mutateAsync(request)
      toast.success('Day Generated', `Generated menu for ${new Date(date).toLocaleDateString()}`)
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handlePreviousWeek = () => {
    const prevWeek = new Date(currentWeekStart)
    prevWeek.setDate(prevWeek.getDate() - 7)
    setCurrentWeekStart(prevWeek.toISOString().split('T')[0])
    setSelectedDate(null)
  }

  const handleNextWeek = () => {
    const nextWeek = new Date(currentWeekStart)
    nextWeek.setDate(nextWeek.getDate() + 7)
    setCurrentWeekStart(nextWeek.toISOString().split('T')[0])
    setSelectedDate(null)
  }

  const handleToday = () => {
    setCurrentWeekStart(getCurrentWeekStart())
    setSelectedDate(new Date().toISOString().split('T')[0])
  }

  const getWeekDateRange = (): string => {
    const start = new Date(currentWeekStart)
    const end = new Date(currentWeekStart)
    end.setDate(start.getDate() + 6)
    
    return `${start.toLocaleDateString('en-US', { 
      month: 'short', 
      day: 'numeric' 
    })} - ${end.toLocaleDateString('en-US', { 
      month: 'short', 
      day: 'numeric', 
      year: 'numeric' 
    })}`
  }

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <span className="ml-3 text-gray-600">{t('common.loading')}</span>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <p className="text-red-800">Failed to load week menu</p>
        </div>
      </div>
    )
  }

  const days = menuWeek?.days || []

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">
              {t('pages.week.title')}
            </h1>
            <p className="mt-2 text-gray-600">
              {getWeekDateRange()}
            </p>
          </div>
          
          <div className="flex items-center space-x-3">
            {criticalFacts && criticalFacts.length > 0 && (
              <Link
                to={`/critical?dateFrom=${currentWeekStart}&dateTo=${getWeekEndDate(currentWeekStart)}`}
                className="inline-flex items-center px-4 py-2 border border-red-300 text-sm font-medium rounded-md text-red-700 bg-red-50 hover:bg-red-100"
              >
                <svg className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
                {criticalFacts.length} Critical Alert{criticalFacts.length !== 1 ? 's' : ''}
              </Link>
            )}
            
            {/* Week Start Toggle */}
            <div className="flex bg-gray-100 rounded-lg p-1">
              <button
                onClick={() => setWeekStartDay('monday')}
                className={`px-3 py-1 rounded-md text-sm font-medium transition-colors ${
                  weekStartDay === 'monday'
                    ? 'bg-white text-blue-600 shadow-sm'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Mon
              </button>
              <button
                onClick={() => setWeekStartDay('sunday')}
                className={`px-3 py-1 rounded-md text-sm font-medium transition-colors ${
                  weekStartDay === 'sunday'
                    ? 'bg-white text-blue-600 shadow-sm'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Sun
              </button>
            </div>
            
            {/* Navigation */}
            <div className="flex items-center space-x-2">
              <Button onClick={handlePreviousWeek} variant="secondary" size="sm">
                ← Prev
              </Button>
              <Button onClick={handleToday} variant="secondary" size="sm">
                Today
              </Button>
              <Button onClick={handleNextWeek} variant="secondary" size="sm">
                Next →
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* Help Button */}
      <HelpButton page="week" />

      <div className="grid grid-cols-1 xl:grid-cols-4 gap-8">
        {/* Main Content - Week Calendar */}
        <div className="xl:col-span-3">
          <WeekCalendar
            days={days}
            weekStartDay={weekStartDay}
            onDayClick={handleDayClick}
          />
          
          {/* Week Statistics */}
          {days.length > 0 && (
            <div className="mt-6 bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Week Statistics</h3>
              
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-gray-900">
                    {days.filter(day => day.isGenerated).length}
                  </div>
                  <div className="text-sm text-gray-500">Days Generated</div>
                </div>
                
                <div className="text-center">
                  <div className="text-2xl font-bold text-gray-900">
                    {days.reduce((acc, day) => acc + day.meals.reduce((mealAcc, meal) => mealAcc + meal.entries.length, 0), 0)}
                  </div>
                  <div className="text-sm text-gray-500">Total Meals</div>
                </div>
                
                <div className="text-center">
                  <div className="text-2xl font-bold text-gray-900">
                    {days.filter(day => day.emergencyMode === true).length}
                  </div>
                  <div className="text-sm text-gray-500">Emergency Days</div>
                </div>
                
                <div className="text-center">
                  <div className="text-2xl font-bold text-green-600">
                    {Math.round((days.filter(day => day.isGenerated).length / days.length) * 100)}%
                  </div>
                  <div className="text-sm text-gray-500">Completion</div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Sidebar - Generation Controls */}
        <div className="xl:col-span-1">
          <GenerationControls
            onGenerateWeek={handleGenerateWeek}
            onGenerateDay={handleGenerateDay}
            isGeneratingWeek={generateWeekMutation.isPending}
            isGeneratingDay={generateDayMutation.isPending}
            selectedDate={selectedDate}
          />
        </div>
      </div>
    </div>
  )
}
