import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { useDishes } from '@/lib/api/dishes'
import { Button } from '@/components/Button'
import { HelpButton } from '@/components/HelpButton'
import { Tooltip } from '@/components/Tooltip'
import { Pagination } from '@/components/Pagination'
import { DishCategoryFilter, type DishCategory } from '@/components/DishCategoryFilter'
import { useAuth } from '@/contexts/AuthContext'
import { toast } from '@/lib/toast'
import type { DishFilters } from '@/lib/types'

export const Dishes: React.FC = () => {
  const { t } = useTranslation()
  const { user } = useAuth()
  const [filters, setFilters] = useState<DishFilters>({
    page: 0,
    size: 12,
  })
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedCategory, setSelectedCategory] = useState<string>()
  const [selectedSubcategory, setSelectedSubcategory] = useState<string>()
  const [activeTab, setActiveTab] = useState<'suggested' | 'my-dishes'>('suggested')

  // Define dish categories
  const dishCategories: DishCategory[] = [
    {
      id: 'suggested',
      name: t('pages.dishes.categories.suggested'),
      subcategories: [
        { id: 'georgian-cuisine', name: t('pages.dishes.categories.georgianCuisine'), count: 0 },
        { id: 'ukrainian-cuisine', name: t('pages.dishes.categories.ukrainianCuisine'), count: 0 },
        { id: 'caucasian-cuisine', name: t('pages.dishes.categories.caucasianCuisine'), count: 0 },
        { id: 'russian-cuisine', name: t('pages.dishes.categories.russianCuisine'), count: 0 },
        { id: 'european-cuisine', name: t('pages.dishes.categories.europeanCuisine'), count: 0 },
        { id: 'asian-cuisine', name: t('pages.dishes.categories.asianCuisine'), count: 0 },
        { id: 'mediterranean-cuisine', name: t('pages.dishes.categories.mediterraneanCuisine'), count: 0 },
        { id: 'american-cuisine', name: t('pages.dishes.categories.americanCuisine'), count: 0 },
      ]
    },
    {
      id: 'my-dishes',
      name: t('pages.dishes.categories.myDishes'),
      subcategories: [
        { id: 'breakfast', name: t('pages.dishes.categories.breakfast'), count: 0 },
        { id: 'lunch', name: t('pages.dishes.categories.lunch'), count: 0 },
        { id: 'dinner', name: t('pages.dishes.categories.dinner'), count: 0 },
        { id: 'snacks', name: t('pages.dishes.categories.snacks'), count: 0 },
      ]
    }
  ]

  const {
    data: dishesResponse,
    isLoading,
    error,
    isFetching,
  } = useDishes(filters)

  const handleSearch = () => {
    setFilters(prev => ({
      ...prev,
      query: searchQuery.trim() || undefined,
      page: 0,
    }))
  }

  const handleClearSearch = () => {
    setSearchQuery('')
    setFilters(prev => ({
      ...prev,
      query: undefined,
      page: 0,
    }))
  }

  const handlePageChange = (page: number) => {
    setFilters(prev => ({ ...prev, page }))
  }

  const handleCategoryChange = (categoryId?: string, subcategoryId?: string) => {
    setSelectedCategory(categoryId)
    setSelectedSubcategory(subcategoryId)
    
    // Map cuisine subcategories to appropriate filter values
    let filterCategory = categoryId
    let filterSubcategory = subcategoryId
    
    if (categoryId === 'suggested' && subcategoryId) {
      // Map cuisine subcategories to backend-compatible values
      const cuisineMapping: { [key: string]: string } = {
        'georgian-cuisine': 'georgian',
        'ukrainian-cuisine': 'ukrainian',
        'caucasian-cuisine': 'caucasian',
        'russian-cuisine': 'russian',
        'european-cuisine': 'european',
        'asian-cuisine': 'asian',
        'mediterranean-cuisine': 'mediterranean',
        'american-cuisine': 'american',
      }
      filterSubcategory = cuisineMapping[subcategoryId] || subcategoryId
    }
    
    setFilters(prev => ({
      ...prev,
      category: filterCategory,
      subcategory: filterSubcategory,
      page: 0,
    }))
  }

  const handleTabChange = (tab: 'suggested' | 'my-dishes') => {
    setActiveTab(tab)
    setSelectedCategory(tab)
    setSelectedSubcategory(undefined)
    setFilters(prev => ({
      ...prev,
      category: tab,
      subcategory: undefined,
      page: 0,
    }))
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch()
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

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-gray-900 mb-4">
            {t('pages.dishes.title')}
          </h1>
          <div className="flex flex-col sm:flex-row gap-3 justify-center">
            {/* Admin can still see the original Add Dish button */}
            {user?.role === 'ADMIN' && (
              <Tooltip content="Create new dish in the common database">
                <Link to="/dishes/new">
                  <Button variant="primary">
                    {t('common.addDish')}
                  </Button>
                </Link>
              </Tooltip>
            )}
            {(user?.role === 'USER' || user?.role === 'PATIENT') && (
              <>
                <Tooltip content="Submit dish to be added to the shared database">
                  <Link to="/dishes/new?request=true">
                    <Button 
                      variant="success"
                    >
                      {t('pages.dishes.requestToAddToCommon')}
                    </Button>
                  </Link>
                </Tooltip>
                <Tooltip content="Create personal dish for your own use">
                  <Link to="/dishes/new">
                    <Button 
                      variant="secondary"
                    >
                      {t('pages.dishes.addToMyDishes')}
                    </Button>
                  </Link>
                </Tooltip>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Help Button */}
      <HelpButton page="dishes" />

      {/* Search and Filters */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
        <div className="flex flex-col space-y-4">
          {/* Search Row */}
          <div className="flex space-x-4">
            <div className="flex-1">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder={t('common.searchProducts')}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            <Button onClick={handleSearch} variant="primary">
              {t('common.search')}
            </Button>
            <Button onClick={handleClearSearch} variant="secondary">
              {t('common.clear')}
            </Button>
          </div>
        </div>
      </div>

      {/* Category Buttons */}
      <div className="flex justify-center mb-8">
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-1">
          <div className="flex space-x-1">
            <button
              onClick={() => handleTabChange('suggested')}
              className={`px-6 py-3 text-sm font-medium rounded-md transition-colors ${
                activeTab === 'suggested'
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-gray-700 hover:text-gray-900 hover:bg-gray-50'
              }`}
            >
              {t('pages.dishes.categories.suggested')}
            </button>
            <button
              onClick={() => handleTabChange('my-dishes')}
              className={`px-6 py-3 text-sm font-medium rounded-md transition-colors ${
                activeTab === 'my-dishes'
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-gray-700 hover:text-gray-900 hover:bg-gray-50'
              }`}
            >
              {t('pages.dishes.categories.myDishes')}
            </button>
          </div>
        </div>
      </div>

      {/* Loading State */}
      {isLoading && (
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <span className="ml-3 text-gray-600">{t('common.loading')}</span>
        </div>
      )}

      {/* Error State */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <h3 className="text-sm font-medium text-red-800">
                {t('common.error')}
              </h3>
              <div className="mt-2 text-sm text-red-700">
                <p>Failed to load dishes. Please make sure your backend is running on port 8080.</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Subcategory Buttons - Always visible */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-8">
        <div className="flex flex-wrap gap-3">
          <span className="text-sm font-medium text-gray-700 mr-4">
            {t('pages.dishes.categories.filterBy')}
          </span>
          {dishCategories
            .find(cat => cat.id === activeTab)
            ?.subcategories.map((subcategory) => (
              <button
                key={subcategory.id}
                onClick={() => handleCategoryChange(activeTab, subcategory.id)}
                className={`px-4 py-2 text-sm font-medium rounded-lg border transition-colors ${
                  selectedCategory === activeTab && selectedSubcategory === subcategory.id
                    ? 'bg-blue-600 text-white border-blue-600'
                    : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50 hover:border-gray-400'
                }`}
              >
                {subcategory.name}
                {subcategory.count !== undefined && (
                  <span className="ml-2 text-xs opacity-75">
                    ({subcategory.count})
                  </span>
                )}
              </button>
            ))}
          <button
            onClick={() => handleCategoryChange(activeTab, undefined)}
            className={`px-4 py-2 text-sm font-medium rounded-lg border transition-colors ${
              selectedCategory === activeTab && !selectedSubcategory
                ? 'bg-blue-600 text-white border-blue-600'
                : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50 hover:border-gray-400'
            }`}
          >
            {t('pages.dishes.categories.all')}
          </button>
        </div>
      </div>

      {/* Dishes Grid */}
      {dishesResponse && (
        <>
          {/* Results Summary */}
          <div className="mb-6">
            <div className="flex items-center justify-between">
              <p className="text-sm text-gray-700">
                Showing {dishesResponse.numberOfElements} of {dishesResponse.totalElements} dishes
                {isFetching && (
                  <span className="ml-2 text-blue-600">
                    <svg className="inline h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                  </span>
                )}
              </p>
            </div>
          </div>

          {/* Dishes List */}
          {dishesResponse.content.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
              {dishesResponse.content.map((dish) => (
                <Link
                  key={dish.id}
                  to={`/dishes/${dish.id}`}
                  className="block bg-white rounded-lg shadow-sm border border-gray-200 p-6 hover:shadow-md hover:border-blue-300 transition-all"
                >
                  <div className="mb-4">
                    <h3 className="text-lg font-semibold text-gray-900 mb-1">
                      {dish.name}
                    </h3>
                    {dish.description && (
                      <p className="text-sm text-gray-600 line-clamp-2">
                        {dish.description}
                      </p>
                    )}
                    <div className="flex items-center space-x-4 mt-2 text-xs text-gray-500">
                      {dish.category && <span>{dish.category}</span>}
                    <span>{dish.servings} serving{dish.servings !== 1 ? 's' : ''}</span>
                    <span>{dish.ingredients.length} ingredient{dish.ingredients.length !== 1 ? 's' : ''}</span>
                    </div>
                  </div>

                  {/* PHE Highlight */}
                  <div className="bg-gray-50 rounded-lg p-3 mb-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium text-gray-700">PHE per serving</span>
                      <span className={`text-lg font-bold ${getPheColor(dish.totalPhenylalanine / dish.servings)}`}>
                        {formatNutrient(dish.totalPhenylalanine / dish.servings)} mg
                      </span>
                    </div>
                  </div>

                  {/* Nutrition Summary */}
                  <div className="grid grid-cols-3 gap-3 text-sm">
                    <div className="text-center">
                      <div className="font-medium text-gray-900">
                        {formatNutrient(dish.totalProtein / dish.servings)}g
                      </div>
                    <div className="text-gray-600">Protein</div>
                  </div>
                  <div className="text-center">
                    <div className="font-medium text-gray-900">
                      {formatNutrient(dish.totalCalories / dish.servings)}
                    </div>
                    <div className="text-gray-600">kcal</div>
                  </div>
                  <div className="text-center">
                    <div className="font-medium text-gray-900">
                      {formatNutrient(dish.totalPhenylalanine)}mg
                    </div>
                    <div className="text-gray-600">Total PHE</div>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <div className="mx-auto h-12 w-12 text-gray-400">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                </svg>
              </div>
              <h3 className="mt-2 text-sm font-medium text-gray-900">No dishes found</h3>
              <p className="mt-1 text-sm text-gray-500">
                Get started by creating your first dish.
              </p>
              <div className="mt-6">
                <Link to="/dishes/new">
                  <Button variant="primary">
                    {t('common.addDish')}
                  </Button>
                </Link>
              </div>
            </div>
          )}

          {/* Pagination */}
          {dishesResponse.totalPages > 1 && (
            <Pagination
              currentPage={dishesResponse.number}
              totalPages={dishesResponse.totalPages}
              totalElements={dishesResponse.totalElements}
              pageSize={dishesResponse.size}
              onPageChange={handlePageChange}
            />
          )}
        </>
      )}

    </div>
  )
}
