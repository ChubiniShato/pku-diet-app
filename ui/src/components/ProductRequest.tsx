import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from './Button'
import { Modal } from './Modal'

interface ProductRequestProps {
  isOpen: boolean
  onClose: () => void
  onSubmit: (request: ProductRequestData) => void
}

export interface ProductRequestData {
  productName: string
  productType: 'raw' | 'processed'
  category: string
  description: string
  pheContent?: number
  proteinContent?: number
  sourceLink?: string
  productImages?: File[]
  notes: string
}

export const ProductRequest: React.FC<ProductRequestProps> = ({
  isOpen,
  onClose,
  onSubmit
}) => {
  const { t } = useTranslation()
  const [formData, setFormData] = useState<ProductRequestData>({
    productName: '',
    productType: 'raw',
    category: '',
    description: '',
    pheContent: undefined,
    proteinContent: undefined,
    sourceLink: '',
    productImages: [],
    notes: ''
  })
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDragOver, setIsDragOver] = useState(false)

  const categories = [
    'Fruits',
    'Vegetables',
    'Grains',
    'Dairy',
    'Meat',
    'Fish',
    'Nuts',
    'Beverages',
    'Snacks',
    'Other'
  ]

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)

    // Validate required fields
    if (!formData.productName.trim()) {
      alert(t('components.productRequest.validation.productNameRequired'))
      setIsSubmitting(false)
      return
    }
    
    if (!formData.category) {
      alert(t('components.productRequest.validation.categoryRequired'))
      setIsSubmitting(false)
      return
    }
    
    if (!formData.description.trim()) {
      alert(t('components.productRequest.validation.descriptionRequired'))
      setIsSubmitting(false)
      return
    }
    
    if (formData.productType === 'processed' && (!formData.productImages || formData.productImages.length === 0)) {
      alert(t('components.productRequest.validation.imagesRequired'))
      setIsSubmitting(false)
      return
    }

    try {
      await onSubmit(formData)
      // Reset form
      setFormData({
        productName: '',
        productType: 'raw',
        category: '',
        description: '',
        pheContent: undefined,
        proteinContent: undefined,
        sourceLink: '',
        productImages: [],
        notes: ''
      })
      onClose()
    } catch (error) {
      console.error('Error submitting product request:', error)
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleChange = (field: keyof ProductRequestData, value: string | number) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }))
  }

  const handleFileSelect = (files: FileList | null) => {
    if (!files) return
    
    const imageFiles = Array.from(files).filter(file => 
      file.type.startsWith('image/') && file.size <= 5 * 1024 * 1024 // 5MB limit
    )
    
    setFormData(prev => ({
      ...prev,
      productImages: [...(prev.productImages || []), ...imageFiles].slice(0, 5) // Max 5 images
    }))
  }

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(true)
  }

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(false)
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(false)
    handleFileSelect(e.dataTransfer.files)
  }

  const removeImage = (index: number) => {
    setFormData(prev => ({
      ...prev,
      productImages: prev.productImages?.filter((_, i) => i !== index) || []
    }))
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={t('components.productRequest.title')}
      size="lg"
    >
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Product Name */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t('components.productRequest.productName')} *
          </label>
          <input
            type="text"
            required
            value={formData.productName}
            onChange={(e) => handleChange('productName', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder={t('components.productRequest.productNamePlaceholder')}
          />
        </div>

        {/* Product Type */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t('components.productRequest.productType')} *
          </label>
          <div className="space-y-2">
            <label className="flex items-center">
              <input
                type="radio"
                name="productType"
                value="raw"
                checked={formData.productType === 'raw'}
                onChange={(e) => handleChange('productType', e.target.value)}
                className="mr-2"
              />
              <span className="text-sm text-gray-700">{t('components.productRequest.rawProduct')}</span>
            </label>
            <label className="flex items-center">
              <input
                type="radio"
                name="productType"
                value="processed"
                checked={formData.productType === 'processed'}
                onChange={(e) => handleChange('productType', e.target.value)}
                className="mr-2"
              />
              <span className="text-sm text-gray-700">{t('components.productRequest.processedProduct')}</span>
            </label>
          </div>
        </div>

        {/* Category */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t('components.productRequest.category')} *
          </label>
          <select
            required
            value={formData.category}
            onChange={(e) => handleChange('category', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">{t('components.productRequest.selectCategory')}</option>
            {categories.map(category => (
              <option key={category} value={category}>
                {category}
              </option>
            ))}
          </select>
        </div>

        {/* Description */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t('components.productRequest.description')} *
          </label>
          <textarea
            required
            rows={3}
            value={formData.description}
            onChange={(e) => handleChange('description', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder={t('components.productRequest.descriptionPlaceholder')}
          />
        </div>

        {/* PHE Content */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t('components.productRequest.pheContent')} (mg/100g)
          </label>
          <input
            type="number"
            step="0.1"
            min="0"
            value={formData.pheContent || ''}
            onChange={(e) => handleChange('pheContent', parseFloat(e.target.value) || 0)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder={t('components.productRequest.pheContentPlaceholder')}
          />
        </div>

        {/* Protein Content */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t('components.productRequest.proteinContent')} (g/100g)
          </label>
          <input
            type="number"
            step="0.1"
            min="0"
            value={formData.proteinContent || ''}
            onChange={(e) => handleChange('proteinContent', parseFloat(e.target.value) || 0)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder={t('components.productRequest.proteinContentPlaceholder')}
          />
        </div>

        {/* Source Link */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t('components.productRequest.sourceLink')}
          </label>
          <input
            type="url"
            value={formData.sourceLink || ''}
            onChange={(e) => handleChange('sourceLink', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder={t('components.productRequest.sourceLinkPlaceholder')}
          />
          <p className="text-sm text-gray-500 mt-1">
            {t('components.productRequest.sourceLinkHelp')}
          </p>
        </div>

        {/* Product Images - Only for processed products */}
        {formData.productType === 'processed' && (
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              {t('components.productRequest.productImages')} *
            </label>
            
            {/* Drag and Drop Area */}
            <div
              className={`border-2 border-dashed rounded-lg p-6 text-center transition-colors ${
                isDragOver 
                  ? 'border-blue-500 bg-blue-50' 
                  : (!formData.productImages || formData.productImages.length === 0)
                    ? 'border-red-300 bg-red-50'
                    : 'border-gray-300 hover:border-gray-400'
              }`}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
            >
              <div className="space-y-2">
                <svg className={`mx-auto h-12 w-12 ${(!formData.productImages || formData.productImages.length === 0) ? 'text-red-400' : 'text-gray-400'}`} stroke="currentColor" fill="none" viewBox="0 0 48 48">
                  <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                <div className="text-sm text-gray-600">
                  <p>{t('components.productRequest.dragDropImages')}</p>
                  {(!formData.productImages || formData.productImages.length === 0) && (
                    <p className="text-xs text-red-600 font-medium mt-1">
                      * {t('components.productRequest.validation.imagesRequired')}
                    </p>
                  )}
                  <p className="text-xs text-gray-500 mt-1">
                    {t('components.productRequest.imageRequirements')}
                  </p>
                </div>
                <input
                  type="file"
                  multiple
                  accept="image/*"
                  onChange={(e) => handleFileSelect(e.target.files)}
                  className="hidden"
                  id="image-upload"
                />
                <label
                  htmlFor="image-upload"
                  className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 cursor-pointer"
                >
                  {t('components.productRequest.selectImages')}
                </label>
              </div>
            </div>

            {/* Image Preview */}
            {formData.productImages && formData.productImages.length > 0 && (
              <div className="mt-4">
                <p className="text-sm font-medium text-gray-700 mb-2">
                  {t('components.productRequest.selectedImages')} ({formData.productImages.length}/5)
                </p>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                  {formData.productImages.map((file, index) => (
                    <div key={index} className="relative group">
                      <img
                        src={URL.createObjectURL(file)}
                        alt={`Preview ${index + 1}`}
                        className="w-full h-24 object-cover rounded-lg border border-gray-200"
                      />
                      <button
                        type="button"
                        onClick={() => removeImage(index)}
                        className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs hover:bg-red-600"
                      >
                        ×
                      </button>
                      <p className="text-xs text-gray-500 mt-1 truncate">
                        {file.name}
                      </p>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {/* Notes */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t('components.productRequest.notes')}
          </label>
          <textarea
            rows={2}
            value={formData.notes}
            onChange={(e) => handleChange('notes', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder={t('components.productRequest.notesPlaceholder')}
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
            {isSubmitting ? t('components.productRequest.submitting') : t('components.productRequest.submit')}
          </Button>
        </div>
      </form>
    </Modal>
  )
}
