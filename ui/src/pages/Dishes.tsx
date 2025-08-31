import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { useDishes } from '@/lib/api/dishes'
import { Button } from '@/components/Button'
import { Pagination } from '@/components/Pagination'
import type { DishFilters } from '@/lib/types'

export const Dishes: React.FC = () => {
  const { t } = useTranslation()
  const [filters, setFilters] = useState<DishFilters>({
    page: 0,
    size: 12,
  })
  const [searchQuery, setSearchQuery] = useState('')

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
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">
              {t('pages.dishes.title')}
            </h1>
            <p className="mt-2 text-gray-600">
              {t('pages.dishes.subtitle')}
            </p>
          </div>
          <Link to="/dishes/new">
            <Button variant="primary">
              {t('common.add')} Dish
            </Button>
          </Link>
        </div>
      </div>

      {/* Search */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
        <div className="flex space-x-4">
          <div className="flex-1">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="Search dishes..."
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          <Button onClick={handleSearch} variant="primary">
            {t('common.search')}
          </Button>
          <Button onClick={handleClearSearch} variant="secondary">
            Clear
          </Button>
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
                    {t('common.add')} Dish
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
