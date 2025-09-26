import React, { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useMenuDay, useValidateDay, useSnackSuggestions, useCreateMealEntry, useUpdateMealEntry, useDeleteMealEntry } from '@/lib/api/menus'
import { useCriticalFactsByDate } from '@/lib/api/criticalFacts'
import { SlotCard } from '@/components/SlotCard'
import { NutrientTotalsBar } from '@/components/NutrientTotalsBar'
import { ValidationBanner } from '@/components/ValidationBanner'
import { SnackSuggestions } from '@/components/SnackSuggestions'
import { Button } from '@/components/Button'
import { HelpButton } from '@/components/HelpButton'
import { Tooltip } from '@/components/Tooltip'
import { toast } from '@/lib/toast/toast'
import type { Product, Dish, MenuEntry, MenuValidationResult, SnackSuggestion } from '@/lib/types'

export const Day: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const { t } = useTranslation()
  const [validationResult, setValidationResult] = useState<MenuValidationResult | null>(null)
  const [showSuggestions, setShowSuggestions] = useState(false)

  // Format date for display
  const dayId = id || new Date().toISOString().split('T')[0] // Default to today
  const patientId = 'current-patient' // TODO: Get actual patient ID

  const { data: day, isLoading, error } = useMenuDay(dayId)
  const { data: criticalFacts } = useCriticalFactsByDate(patientId, dayId)
  const validateMutation = useValidateDay()
  const createEntryMutation = useCreateMealEntry()
  const updateEntryMutation = useUpdateMealEntry()
  const deleteEntryMutation = useDeleteMealEntry()
  
  const { data: snackSuggestions = [], isLoading: suggestionsLoading } = useSnackSuggestions(
    dayId, 
    'snack', 
    showSuggestions
  )

  const handleValidate = async () => {
    if (!day) return

    try {
      const result = await validateMutation.mutateAsync(day.id)
      setValidationResult(result)
      toast.success('Validation Complete', `Menu status: ${result.status}`)
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handleAddProduct = async (slotId: string, product: Product, quantity: number, unit: string) => {
    try {
      await createEntryMutation.mutateAsync({
        slotId,
        itemType: 'PRODUCT',
        itemId: product.id,
        quantity,
        unit,
      })
      toast.success('Product Added', `${product.productName} added to meal`)
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handleAddDish = async (slotId: string, dish: Dish, quantity: number, unit: string) => {
    try {
      await createEntryMutation.mutateAsync({
        slotId,
        itemType: 'DISH',
        itemId: dish.id,
        quantity,
        unit,
      })
      toast.success('Dish Added', `${dish.name} added to meal`)
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handleUpdateEntry = async (entryId: string, updates: Partial<MenuEntry>) => {
    try {
      await updateEntryMutation.mutateAsync({ entryId, data: updates })
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handleRemoveEntry = async (entryId: string) => {
    try {
      await deleteEntryMutation.mutateAsync(entryId)
      toast.success('Item Removed', 'Item removed from meal')
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handleToggleConsumed = async (entryId: string, consumed: boolean) => {
    try {
      await updateEntryMutation.mutateAsync({
        entryId,
        data: {
          consumed,
          consumedAt: consumed ? new Date().toISOString() : undefined,
        },
      })
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handleAddSuggestion = async (suggestion: SnackSuggestion) => {
    // Find or create a snack slot
    let snackSlot = day?.meals.find(meal => meal.type.toLowerCase() === 'snack')
    
    if (!snackSlot) {
      // For now, add to the first available slot or create an extra slot
      snackSlot = day?.meals.find(meal => meal.type.toLowerCase() === 'dinner') || day?.meals[0]
    }

    if (!snackSlot) return

    try {
      await createEntryMutation.mutateAsync({
        mealId: snackSlot.id,
        itemType: suggestion.itemType,
        itemId: suggestion.itemId,
        quantity: suggestion.quantity,
        unit: suggestion.unit,
      })
      toast.success('Snack Added', `${suggestion.itemName} added to your meal plan`)
    } catch (error) {
      // Error handled by global error handler
    }
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
          <p className="text-red-800">Failed to load day menu</p>
          <Link to="/week" className="text-red-600 hover:text-red-800 underline mt-2 inline-block">
            Back to Week View
          </Link>
        </div>
      </div>
    )
  }

  if (!day) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4">
          <p className="text-yellow-800">No menu found for this date</p>
          <Link to="/week" className="text-yellow-600 hover:text-yellow-800 underline mt-2 inline-block">
            Back to Week View
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center justify-between">
          <div>
            <Link to="/week" className="text-blue-600 hover:text-blue-800 text-sm mb-2 inline-block">
              ← Back to Week
            </Link>
            <h1 className="text-3xl font-bold text-gray-900">
              {new Date(day.date).toLocaleDateString('en-US', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
            </h1>
            <p className="mt-2 text-gray-600">
              Plan and track your daily meals and nutrition
            </p>
          </div>
          
          <div className="flex space-x-3">
            {criticalFacts && criticalFacts.length > 0 && (
              <Link
                to={`/critical?date=${dayId}`}
                className="inline-flex items-center px-4 py-2 border border-red-300 text-sm font-medium rounded-md text-red-700 bg-red-50 hover:bg-red-100"
              >
                <svg className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
                {criticalFacts.length} Critical Alert{criticalFacts.length !== 1 ? 's' : ''}
              </Link>
            )}
            <Tooltip content="Get suggestions for low-PHE snacks based on your remaining daily allowance">
              <Button
                onClick={() => setShowSuggestions(!showSuggestions)}
                variant="secondary"
              >
                {showSuggestions ? 'Hide' : 'Show'} Suggestions
              </Button>
            </Tooltip>
            <Tooltip content="Validate your daily menu against PKU requirements">
              <Button
                onClick={handleValidate}
                variant="primary"
                disabled={validateMutation.isPending}
              >
                {validateMutation.isPending ? 'Validating...' : 'Validate Menu'}
              </Button>
            </Tooltip>
          </div>
        </div>
      </div>

      {/* Help Button */}
      <HelpButton page="day" />

      {/* Validation Banner */}
      {validationResult && (
        <ValidationBanner
          validation={validationResult}
          onDismiss={() => setValidationResult(null)}
        />
      )}

      <div className="grid grid-cols-1 xl:grid-cols-4 gap-8">
        {/* Main Content */}
        <div className="xl:col-span-3 space-y-6">
          {/* Meal Slots */}
          <div className="space-y-6">
            {day.meals.map((meal) => (
              <SlotCard
                key={meal.id}
                slot={meal}
                onAddProduct={handleAddProduct}
                onAddDish={handleAddDish}
                onUpdateEntry={handleUpdateEntry}
                onRemoveEntry={handleRemoveEntry}
                onToggleConsumed={handleToggleConsumed}
              />
            ))}
          </div>

          {/* Snack Suggestions */}
          {showSuggestions && (
            <SnackSuggestions
              suggestions={snackSuggestions}
              onAddSuggestion={handleAddSuggestion}
              isLoading={suggestionsLoading}
            />
          )}
        </div>

        {/* Sidebar - Nutrition Totals */}
        <div className="xl:col-span-1">
          <NutrientTotalsBar day={day} />
        </div>
      </div>
    </div>
  )
}
