import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { EntryRow } from './EntryRow'
import { Button } from './Button'
import { ProductPicker } from './ProductPicker'
import type { MenuSlot, MenuEntry, Product, Dish } from '@/lib/types'

interface SlotCardProps {
  slot: MenuSlot
  onAddProduct: (slotId: string, product: Product, quantity: number, unit: string) => void
  onAddDish: (slotId: string, dish: Dish, quantity: number, unit: string) => void
  onUpdateEntry: (entryId: string, updates: Partial<MenuEntry>) => void
  onRemoveEntry: (entryId: string) => void
  onToggleConsumed: (entryId: string, consumed: boolean) => void
}

export const SlotCard: React.FC<SlotCardProps> = ({
  slot,
  onAddProduct,
  onAddDish,
  onUpdateEntry,
  onRemoveEntry,
  onToggleConsumed,
}) => {
  const { t } = useTranslation()
  const [isProductPickerOpen, setIsProductPickerOpen] = useState(false)
  const [isDishPickerOpen, setIsDishPickerOpen] = useState(false)

  const handleAddProduct = (product: Product) => {
    onAddProduct(slot.id, product, 100, 'g') // Default 100g
    setIsProductPickerOpen(false)
  }

  const handleAddDish = (dish: Dish) => {
    onAddDish(slot.id, dish, 1, 'serving') // Default 1 serving
    setIsDishPickerOpen(false)
  }

  // Calculate slot totals
  const slotTotals = slot.entries.reduce(
    (acc, entry) => ({
      plannedPhenylalanine: acc.plannedPhenylalanine + (entry.plannedPhenylalanine || 0),
      plannedProtein: acc.plannedProtein + (entry.plannedProtein || 0),
      plannedCalories: acc.plannedCalories + (entry.plannedCalories || 0),
      consumedPhenylalanine: acc.consumedPhenylalanine + (entry.consumedPhenylalanine || 0),
      consumedProtein: acc.consumedProtein + (entry.consumedProtein || 0),
      consumedCalories: acc.consumedCalories + (entry.consumedCalories || 0),
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

  const formatNutrient = (value: number): string => {
    return value.toFixed(1)
  }

  const getPheColor = (phe: number): string => {
    if (phe <= 50) return 'text-green-600'
    if (phe <= 100) return 'text-yellow-600'
    return 'text-red-600'
  }

  const getSlotIcon = (slotType: string) => {
    switch (slotType.toLowerCase()) {
      case 'breakfast':
        return '🌅'
      case 'lunch':
        return '🌞'
      case 'dinner':
        return '🌆'
      case 'supper':
        return '🌙'
      default:
        return '🍽️'
    }
  }

  return (
    <>
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        {/* Header */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <span className="text-2xl">{getSlotIcon(slot.slotType)}</span>
            <div>
              <h3 className="text-lg font-semibold text-gray-900">
                {slot.slotType}
              </h3>
              <p className="text-sm text-gray-500">
                {slot.entries.length} item{slot.entries.length !== 1 ? 's' : ''}
              </p>
            </div>
          </div>
          
          <div className="flex space-x-2">
            <Button
              onClick={() => setIsProductPickerOpen(true)}
              variant="secondary"
              size="sm"
            >
              + Product
            </Button>
            <Button
              onClick={() => setIsDishPickerOpen(true)}
              variant="secondary"
              size="sm"
            >
              + Dish
            </Button>
          </div>
        </div>

        {/* Entries */}
        <div className="space-y-3 mb-4">
          {slot.entries.length === 0 ? (
            <div className="text-center py-6 text-gray-500">
              <p>No items added to this meal yet.</p>
              <p className="text-sm mt-1">Click "+ Product" or "+ Dish" to add items.</p>
            </div>
          ) : (
            slot.entries.map((entry) => (
              <EntryRow
                key={entry.id}
                entry={entry}
                onUpdate={onUpdateEntry}
                onRemove={onRemoveEntry}
                onToggleConsumed={onToggleConsumed}
              />
            ))
          )}
        </div>

        {/* Slot Totals */}
        {slot.entries.length > 0 && (
          <div className="border-t border-gray-200 pt-4">
            <h4 className="text-sm font-medium text-gray-700 mb-3">Meal Totals</h4>
            <div className="grid grid-cols-2 gap-4">
              {/* Planned */}
              <div>
                <p className="text-xs font-medium text-gray-500 uppercase tracking-wider mb-2">
                  Planned
                </p>
                <div className="space-y-1">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">PHE:</span>
                    <span className={`font-medium ${getPheColor(slotTotals.plannedPhenylalanine)}`}>
                      {formatNutrient(slotTotals.plannedPhenylalanine)} mg
                    </span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Protein:</span>
                    <span className="font-medium text-gray-900">
                      {formatNutrient(slotTotals.plannedProtein)} g
                    </span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Calories:</span>
                    <span className="font-medium text-gray-900">
                      {formatNutrient(slotTotals.plannedCalories)} kcal
                    </span>
                  </div>
                </div>
              </div>

              {/* Consumed */}
              <div>
                <p className="text-xs font-medium text-gray-500 uppercase tracking-wider mb-2">
                  Consumed
                </p>
                <div className="space-y-1">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">PHE:</span>
                    <span className={`font-medium ${getPheColor(slotTotals.consumedPhenylalanine)}`}>
                      {formatNutrient(slotTotals.consumedPhenylalanine)} mg
                    </span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Protein:</span>
                    <span className="font-medium text-gray-900">
                      {formatNutrient(slotTotals.consumedProtein)} g
                    </span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Calories:</span>
                    <span className="font-medium text-gray-900">
                      {formatNutrient(slotTotals.consumedCalories)} kcal
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Product Picker Modal */}
      <ProductPicker
        isOpen={isProductPickerOpen}
        onClose={() => setIsProductPickerOpen(false)}
        onSelect={handleAddProduct}
      />

      {/* Dish Picker Modal - We'll need to create this similar to ProductPicker */}
      {/* For now, we'll use a simple alert */}
      {isDishPickerOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
          <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
            <h3 className="text-lg font-semibold mb-4">Dish Picker</h3>
            <p className="text-gray-600 mb-4">
              Dish picker functionality will be implemented soon.
            </p>
            <Button onClick={() => setIsDishPickerOpen(false)} variant="primary">
              Close
            </Button>
          </div>
        </div>
      )}
    </>
  )
}
