import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/Button'
import { toast } from '@/lib/toast'

interface ProductFormData {
  productName: string
  category: string
  phenylalanine?: number
  leucine?: number
  tyrosine?: number
  methionine?: number
  kilojoules?: number
  kilocalories?: number
  protein?: number
  carbohydrates?: number
  fats?: number
  description?: string
}

export const ProductNew: React.FC = () => {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [formData, setFormData] = useState<ProductFormData>({
    productName: '',
    category: '',
    phenylalanine: undefined,
    leucine: undefined,
    tyrosine: undefined,
    methionine: undefined,
    kilojoules: undefined,
    kilocalories: undefined,
    protein: undefined,
    carbohydrates: undefined,
    fats: undefined,
    description: ''
  })

  const categories = [
    'Fruits',
    'Vegetables',
    'Grains',
    'Dairy',
    'Meat',
    'Fish',
    'Beverages',
    'Snacks',
    'Other'
  ]

  const handleChange = (field: keyof ProductFormData, value: string | number) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)

    try {
      // TODO: Implement API call to create product
      console.log('Product data:', formData)
      
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      toast.success(t('pages.productNew.success'))
      navigate('/products')
    } catch (error) {
      console.error('Error creating product:', error)
      toast.error(t('pages.productNew.error'))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleCancel = () => {
    navigate('/products')
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          {t('pages.productNew.title')}
        </h1>
        <p className="mt-2 text-gray-600">
          {t('pages.productNew.subtitle')}
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-8">
        {/* Basic Information */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-6">
            {t('pages.productNew.basicInfo')}
          </h2>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('pages.productNew.productName')} *
              </label>
              <input
                type="text"
                required
                value={formData.productName}
                onChange={(e) => handleChange('productName', e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder={t('pages.productNew.productNamePlaceholder')}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('pages.productNew.category')} *
              </label>
              <select
                required
                value={formData.category}
                onChange={(e) => handleChange('category', e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">{t('pages.productNew.selectCategory')}</option>
                {categories.map((category) => (
                  <option key={category} value={category}>
                    {category}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="mt-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              {t('pages.productNew.description')}
            </label>
            <textarea
              value={formData.description}
              onChange={(e) => handleChange('description', e.target.value)}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder={t('pages.productNew.descriptionPlaceholder')}
            />
          </div>
        </div>

        {/* Amino Acids */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-6">
            {t('pages.productNew.aminoAcids')}
          </h2>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('pages.productNew.phenylalanine')} (mg/100g)
              </label>
              <input
                type="number"
                min="0"
                step="0.1"
                value={formData.phenylalanine || ''}
                onChange={(e) => handleChange('phenylalanine', Number(e.target.value))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="0.0"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('pages.productNew.leucine')} (mg/100g)
              </label>
              <input
                type="number"
                min="0"
                step="0.1"
                value={formData.leucine || ''}
                onChange={(e) => handleChange('leucine', Number(e.target.value))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="0.0"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('pages.productNew.tyrosine')} (mg/100g)
              </label>
              <input
                type="number"
                min="0"
                step="0.1"
                value={formData.tyrosine || ''}
                onChange={(e) => handleChange('tyrosine', Number(e.target.value))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="0.0"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('pages.productNew.methionine')} (mg/100g)
              </label>
              <input
                type="number"
                min="0"
                step="0.1"
                value={formData.methionine || ''}
                onChange={(e) => handleChange('methionine', Number(e.target.value))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="0.0"
              />
            </div>
          </div>
        </div>

        {/* Macronutrients */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-6">
            {t('pages.productNew.macronutrients')}
          </h2>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('pages.productNew.protein')} (g/100g)
              </label>
              <input
                type="number"
                min="0"
                step="0.1"
                value={formData.protein || ''}
                onChange={(e) => handleChange('protein', Number(e.target.value))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="0.0"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('pages.productNew.carbohydrates')} (g/100g)
              </label>
              <input
                type="number"
                min="0"
                step="0.1"
                value={formData.carbohydrates || ''}
                onChange={(e) => handleChange('carbohydrates', Number(e.target.value))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="0.0"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('pages.productNew.fats')} (g/100g)
              </label>
              <input
                type="number"
                min="0"
                step="0.1"
                value={formData.fats || ''}
                onChange={(e) => handleChange('fats', Number(e.target.value))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="0.0"
              />
            </div>
          </div>
        </div>

        {/* Energy */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-6">
            {t('pages.productNew.energy')}
          </h2>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('pages.productNew.kilocalories')} (kcal/100g)
              </label>
              <input
                type="number"
                min="0"
                step="0.1"
                value={formData.kilocalories || ''}
                onChange={(e) => handleChange('kilocalories', Number(e.target.value))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="0.0"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('pages.productNew.kilojoules')} (kJ/100g)
              </label>
              <input
                type="number"
                min="0"
                step="0.1"
                value={formData.kilojoules || ''}
                onChange={(e) => handleChange('kilojoules', Number(e.target.value))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="0.0"
              />
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex justify-end space-x-4">
          <Button
            type="button"
            variant="secondary"
            onClick={handleCancel}
            disabled={isSubmitting}
          >
            {t('common.cancel')}
          </Button>
          <Button
            type="submit"
            variant="primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? t('pages.productNew.creating') : t('pages.productNew.create')}
          </Button>
        </div>
      </form>
    </div>
  )
}
