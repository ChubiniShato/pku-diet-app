import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { MenuEntry } from '@/lib/types'

interface EntryRowProps {
  entry: MenuEntry
  onUpdate: (entryId: string, updates: Partial<MenuEntry>) => void
  onRemove: (entryId: string) => void
  onToggleConsumed: (entryId: string, consumed: boolean) => void
}

export const EntryRow: React.FC<EntryRowProps> = ({
  entry,
  onUpdate,
  onRemove,
  onToggleConsumed,
}) => {
  const { t } = useTranslation()
  const [isEditing, setIsEditing] = useState(false)
  const [localQuantity, setLocalQuantity] = useState(entry.quantity.toString())
  const [localConsumedQuantity, setLocalConsumedQuantity] = useState(
    entry.consumedQuantity?.toString() || entry.quantity.toString()
  )

  const handleQuantitySubmit = () => {
    const newQuantity = parseFloat(localQuantity)
    if (!isNaN(newQuantity) && newQuantity > 0) {
      // Calculate new nutrition values based on quantity change
      const multiplier = newQuantity / entry.quantity
      
      onUpdate(entry.id, {
        quantity: newQuantity,
        plannedPhenylalanine: (entry.plannedPhenylalanine || 0) * multiplier,
        plannedProtein: (entry.plannedProtein || 0) * multiplier,
        plannedCalories: (entry.plannedCalories || 0) * multiplier,
      })
    }
    setIsEditing(false)
  }

  const handleConsumedQuantitySubmit = () => {
    const newConsumedQuantity = parseFloat(localConsumedQuantity)
    if (!isNaN(newConsumedQuantity) && newConsumedQuantity >= 0) {
      // Calculate consumed nutrition based on consumed quantity
      const baseMultiplier = entry.quantity > 0 ? 1 / entry.quantity : 0
      const consumedMultiplier = newConsumedQuantity * baseMultiplier
      
      onUpdate(entry.id, {
        consumedQuantity: newConsumedQuantity,
        consumedPhenylalanine: (entry.plannedPhenylalanine || 0) * consumedMultiplier,
        consumedProtein: (entry.plannedProtein || 0) * consumedMultiplier,
        consumedCalories: (entry.plannedCalories || 0) * consumedMultiplier,
      })
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent, onSubmit: () => void) => {
    if (e.key === 'Enter') {
      onSubmit()
    } else if (e.key === 'Escape') {
      setIsEditing(false)
      setLocalQuantity(entry.quantity.toString())
    }
  }

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

  const getItemIcon = (itemType: string) => {
    return itemType === 'PRODUCT' ? '🥕' : '🍽️'
  }

  const consumedPercentage = entry.quantity > 0 
    ? Math.round(((entry.consumedQuantity || 0) / entry.quantity) * 100)
    : 0

  return (
    <div className="border border-gray-200 rounded-lg p-4 hover:border-gray-300 transition-colors">
      {/* Main Entry Info */}
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-start space-x-3 flex-1">
          {/* Item Icon & Name */}
          <div className="flex items-center space-x-2">
            <span className="text-lg">{getItemIcon(entry.itemType)}</span>
            <div>
              <h4 className="font-medium text-gray-900 text-sm">
                {entry.itemName}
              </h4>
              <p className="text-xs text-gray-500">
                {entry.itemType === 'PRODUCT' ? 'Product' : 'Dish'}
              </p>
            </div>
          </div>

          {/* Planned Quantity */}
          <div className="flex items-center space-x-2">
            {isEditing ? (
              <input
                type="number"
                min="0"
                step="0.1"
                value={localQuantity}
                onChange={(e) => setLocalQuantity(e.target.value)}
                onBlur={handleQuantitySubmit}
                onKeyPress={(e) => handleKeyPress(e, handleQuantitySubmit)}
                className="w-20 px-2 py-1 text-sm border border-blue-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
                autoFocus
              />
            ) : (
              <button
                onClick={() => setIsEditing(true)}
                className="text-sm font-medium text-blue-600 hover:text-blue-800 hover:underline"
              >
                {formatNutrient(entry.quantity)} {entry.unit}
              </button>
            )}
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center space-x-2">
          <button
            onClick={() => onRemove(entry.id)}
            className="text-red-600 hover:text-red-800 p-1"
            title="Remove item"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>
      </div>

      {/* Nutrition & Consumption */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Planned Nutrition */}
        <div>
          <p className="text-xs font-medium text-gray-500 uppercase tracking-wider mb-2">
            Planned
          </p>
          <div className="space-y-1">
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">PHE:</span>
              <span className={`font-medium ${getPheColor(entry.plannedPhenylalanine)}`}>
                {formatNutrient(entry.plannedPhenylalanine)} mg
              </span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">Protein:</span>
              <span className="font-medium text-gray-900">
                {formatNutrient(entry.plannedProtein)} g
              </span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">Calories:</span>
              <span className="font-medium text-gray-900">
                {formatNutrient(entry.plannedCalories)} kcal
              </span>
            </div>
          </div>
        </div>

        {/* Consumed Quantity Editor */}
        <div>
          <p className="text-xs font-medium text-gray-500 uppercase tracking-wider mb-2">
            Consumed ({consumedPercentage}%)
          </p>
          <div className="flex items-center space-x-2 mb-2">
            <input
              type="number"
              min="0"
              max={entry.quantity * 2} // Allow up to 2x planned quantity
              step="0.1"
              value={localConsumedQuantity}
              onChange={(e) => setLocalConsumedQuantity(e.target.value)}
              onBlur={handleConsumedQuantitySubmit}
              onKeyPress={(e) => handleKeyPress(e, handleConsumedQuantitySubmit)}
              className="w-20 px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
            <span className="text-sm text-gray-600">{entry.unit}</span>
          </div>
          
          {/* Quick Consumption Buttons */}
          <div className="flex space-x-1">
            <button
              onClick={() => {
                setLocalConsumedQuantity('0')
                handleConsumedQuantitySubmit()
              }}
              className="text-xs px-2 py-1 bg-red-100 text-red-700 rounded hover:bg-red-200"
            >
              None
            </button>
            <button
              onClick={() => {
                setLocalConsumedQuantity((entry.quantity / 2).toString())
                handleConsumedQuantitySubmit()
              }}
              className="text-xs px-2 py-1 bg-yellow-100 text-yellow-700 rounded hover:bg-yellow-200"
            >
              Half
            </button>
            <button
              onClick={() => {
                setLocalConsumedQuantity(entry.quantity.toString())
                handleConsumedQuantitySubmit()
              }}
              className="text-xs px-2 py-1 bg-green-100 text-green-700 rounded hover:bg-green-200"
            >
              All
            </button>
          </div>
        </div>

        {/* Consumed Nutrition */}
        <div>
          <p className="text-xs font-medium text-gray-500 uppercase tracking-wider mb-2">
            Actually Consumed
          </p>
          <div className="space-y-1">
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">PHE:</span>
              <span className={`font-medium ${getPheColor(entry.consumedPhenylalanine)}`}>
                {formatNutrient(entry.consumedPhenylalanine)} mg
              </span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">Protein:</span>
              <span className="font-medium text-gray-900">
                {formatNutrient(entry.consumedProtein)} g
              </span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">Calories:</span>
              <span className="font-medium text-gray-900">
                {formatNutrient(entry.consumedCalories)} kcal
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Consumption Progress Bar */}
      <div className="mt-3">
        <div className="flex justify-between text-xs text-gray-500 mb-1">
          <span>Consumption Progress</span>
          <span>{consumedPercentage}%</span>
        </div>
        <div className="w-full bg-gray-200 rounded-full h-2">
          <div
            className={`h-2 rounded-full transition-all duration-300 ${
              consumedPercentage === 0
                ? 'bg-red-400'
                : consumedPercentage < 50
                ? 'bg-yellow-400'
                : consumedPercentage <= 100
                ? 'bg-green-400'
                : 'bg-blue-400'
            }`}
            style={{ width: `${Math.min(consumedPercentage, 100)}%` }}
          />
        </div>
      </div>
    </div>
  )
}
