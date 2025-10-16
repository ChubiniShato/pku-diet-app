import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'

export interface DishCategory {
  id: string
  name: string
  subcategories: DishSubcategory[]
}

export interface DishSubcategory {
  id: string
  name: string
  count?: number
}

interface DishCategoryFilterProps {
  categories: DishCategory[]
  selectedCategory?: string
  selectedSubcategory?: string
  onCategoryChange: (categoryId?: string, subcategoryId?: string) => void
}

export const DishCategoryFilter: React.FC<DishCategoryFilterProps> = ({
  categories,
  selectedCategory,
  selectedSubcategory,
  onCategoryChange,
}) => {
  const { t } = useTranslation()
  const [isOpen, setIsOpen] = useState(false)

  const selectedCategoryData = categories.find(cat => cat.id === selectedCategory)
  const selectedSubcategoryData = selectedCategoryData?.subcategories.find(sub => sub.id === selectedSubcategory)

  const handleCategorySelect = (categoryId: string) => {
    if (selectedCategory === categoryId) {
      // If clicking the same category, close it
      onCategoryChange(undefined, undefined)
      setIsOpen(false)
    } else {
      // Select new category and first subcategory
      const category = categories.find(cat => cat.id === categoryId)
      const firstSubcategory = category?.subcategories[0]
      onCategoryChange(categoryId, firstSubcategory?.id)
    }
  }

  const handleSubcategorySelect = (subcategoryId: string) => {
    onCategoryChange(selectedCategory, subcategoryId)
    setIsOpen(false)
  }

  const handleClear = () => {
    onCategoryChange(undefined, undefined)
    setIsOpen(false)
  }

  return (
    <div className="relative">
      {/* Main Filter Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center space-x-2 px-4 py-2 bg-white border border-gray-300 rounded-lg shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
      >
        <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
        </svg>
        <span className="text-sm font-medium text-gray-700">
          {selectedCategoryData ? selectedCategoryData.name : t('dishes.categories.all')}
        </span>
        {selectedSubcategoryData && (
          <span className="text-xs text-gray-500">
            / {selectedSubcategoryData.name}
          </span>
        )}
        <svg 
          className={`w-4 h-4 text-gray-400 transition-transform ${isOpen ? 'rotate-180' : ''}`} 
          fill="none" 
          stroke="currentColor" 
          viewBox="0 0 24 24"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
        </svg>
      </button>

      {/* Dropdown Menu */}
      {isOpen && (
        <div className="absolute top-full left-0 mt-1 w-80 bg-white border border-gray-200 rounded-lg shadow-lg z-50">
          <div className="p-4">
            {/* Clear Filter */}
            <button
              onClick={handleClear}
              className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-md mb-2"
            >
              {t('dishes.categories.all')}
            </button>

            {/* Categories */}
            {categories.map((category) => (
              <div key={category.id} className="mb-2">
                <button
                  onClick={() => handleCategorySelect(category.id)}
                  className={`w-full text-left px-3 py-2 text-sm font-medium rounded-md mb-1 ${
                    selectedCategory === category.id
                      ? 'bg-blue-100 text-blue-700'
                      : 'text-gray-700 hover:bg-gray-100'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span>{category.name}</span>
                    <svg 
                      className={`w-4 h-4 transition-transform ${
                        selectedCategory === category.id ? 'rotate-90' : ''
                      }`} 
                      fill="none" 
                      stroke="currentColor" 
                      viewBox="0 0 24 24"
                    >
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                    </svg>
                  </div>
                </button>

                {/* Subcategories */}
                {selectedCategory === category.id && (
                  <div className="ml-4 space-y-1">
                    {category.subcategories.map((subcategory) => (
                      <button
                        key={subcategory.id}
                        onClick={() => handleSubcategorySelect(subcategory.id)}
                        className={`w-full text-left px-3 py-2 text-sm rounded-md flex items-center justify-between ${
                          selectedSubcategory === subcategory.id
                            ? 'bg-blue-50 text-blue-600'
                            : 'text-gray-600 hover:bg-gray-50'
                        }`}
                      >
                        <span>{subcategory.name}</span>
                        {subcategory.count !== undefined && (
                          <span className="text-xs text-gray-400">
                            {subcategory.count}
                          </span>
                        )}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Backdrop */}
      {isOpen && (
        <div 
          className="fixed inset-0 z-40" 
          onClick={() => setIsOpen(false)}
        />
      )}
    </div>
  )
}

