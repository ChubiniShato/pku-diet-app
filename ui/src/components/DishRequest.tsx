import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from './Button'
import { Modal } from './Modal'

interface DishRequestProps {
  isOpen: boolean
  onClose: () => void
  onSubmit: (request: DishRequestData) => void
}

export interface DishRequestData {
  dishName: string
  category: string
  description: string
  recipeProject: string
  cuisine: string
  notes: string
}

export const DishRequest: React.FC<DishRequestProps> = ({
  isOpen,
  onClose,
  onSubmit
}) => {
  const { t } = useTranslation()
  const [formData, setFormData] = useState<DishRequestData>({
    dishName: '',
    category: '',
    description: '',
    recipeProject: '',
    cuisine: '',
    notes: ''
  })
  const [isSubmitting, setIsSubmitting] = useState(false)

  const categories = [
    'Breakfast',
    'Lunch',
    'Dinner',
    'Snacks',
    'Desserts',
    'Beverages',
    'Soups',
    'Salads',
    'Main Courses',
    'Side Dishes',
    'Other'
  ]

  const cuisines = [
    'Georgian',
    'Ukrainian',
    'Caucasian',
    'Russian',
    'European',
    'Asian',
    'Mediterranean',
    'American',
    'Other'
  ]

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)

    try {
      await onSubmit(formData)
      // Reset form
      setFormData({
        dishName: '',
        category: '',
        description: '',
        recipeProject: '',
        cuisine: '',
        notes: ''
      })
      onClose()
    } catch (error) {
      console.error('Error submitting dish request:', error)
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleChange = (field: keyof DishRequestData, value: string) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }))
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={t('components.dishRequest.title')}
      size="lg"
    >
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Dish Name */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t('components.dishRequest.dishName')} *
          </label>
          <input
            type="text"
            required
            value={formData.dishName}
            onChange={(e) => handleChange('dishName', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder={t('components.dishRequest.dishNamePlaceholder')}
          />
        </div>

        {/* Category */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t('components.dishRequest.category')} *
          </label>
          <select
            required
            value={formData.category}
            onChange={(e) => handleChange('category', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">{t('components.dishRequest.selectCategory')}</option>
            {categories.map(category => (
              <option key={category} value={category}>
                {category}
              </option>
            ))}
          </select>
        </div>

        {/* Cuisine */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t('components.dishRequest.cuisine')} *
          </label>
          <select
            required
            value={formData.cuisine}
            onChange={(e) => handleChange('cuisine', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">{t('components.dishRequest.selectCuisine')}</option>
            {cuisines.map(cuisine => (
              <option key={cuisine} value={cuisine}>
                {cuisine}
              </option>
            ))}
          </select>
        </div>

        {/* Description */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t('components.dishRequest.description')} *
          </label>
          <textarea
            required
            rows={3}
            value={formData.description}
            onChange={(e) => handleChange('description', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder={t('components.dishRequest.descriptionPlaceholder')}
          />
        </div>

        {/* Recipe Project */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t('components.dishRequest.recipeProject')} *
          </label>
          <textarea
            required
            rows={4}
            value={formData.recipeProject}
            onChange={(e) => handleChange('recipeProject', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder={t('components.dishRequest.recipeProjectPlaceholder')}
          />
          <p className="text-sm text-gray-500 mt-1">
            {t('components.dishRequest.recipeProjectHelp')}
          </p>
        </div>

        {/* Notes */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t('components.dishRequest.notes')}
          </label>
          <textarea
            rows={2}
            value={formData.notes}
            onChange={(e) => handleChange('notes', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder={t('components.dishRequest.notesPlaceholder')}
          />
        </div>

        {/* Submit Buttons */}
        <div className="flex justify-end space-x-3">
          <Button
            type="button"
            variant="secondary"
            onClick={onClose}
            disabled={isSubmitting}
          >
            {t('common.cancel')}
          </Button>
          <Button
            type="submit"
            variant="primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? t('components.dishRequest.submitting') : t('components.dishRequest.submit')}
          </Button>
        </div>
      </form>
    </Modal>
  )
}
