import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useProducts, useProductCategories } from '@/lib/api/products'
import { ProductCard } from '@/components/ProductCard'
import { ProductSearch } from '@/components/ProductSearch'
import { ProductDrawer } from '@/components/ProductDrawer'
import { CsvUpload } from '@/components/CsvUpload'
import { Pagination } from '@/components/Pagination'
import type { ProductSearchParams } from '@/lib/types'

export const Products: React.FC = () => {
  const { t } = useTranslation()
  const [filters, setFilters] = useState<ProductSearchParams>({
    page: 0,
    size: 12,
  })
  const [selectedProductId, setSelectedProductId] = useState<string | null>(null)
  const [isDrawerOpen, setIsDrawerOpen] = useState(false)

  // Check if admin mode is enabled via URL parameter
  const isAdminMode = new URLSearchParams(window.location.search).get('admin') === '1'

  const {
    data: productsResponse,
    isLoading,
    error,
    isFetching,
  } = useProducts(filters)

  const handleFiltersChange = (newFilters: ProductSearchParams) => {
    setFilters(newFilters)
  }

  const handlePageChange = (page: number) => {
    setFilters(prev => ({ ...prev, page }))
  }

  const handleProductClick = (productId: string) => {
    setSelectedProductId(productId)
    setIsDrawerOpen(true)
  }

  const handleCloseDrawer = () => {
    setIsDrawerOpen(false)
    setSelectedProductId(null)
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          {t('pages.products.title')}
        </h1>
        <p className="mt-2 text-gray-600">
          {t('pages.products.subtitle')}
        </p>
      </div>

      {/* CSV Upload (Admin Only) */}
      <CsvUpload isVisible={isAdminMode} />

      {/* Search and Filters */}
      <ProductSearch
        filters={filters}
        onFiltersChange={handleFiltersChange}
      />

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
                <p>
                  Failed to load products. Please make sure your backend is running on port 8080.
                </p>
                {error instanceof Error && (
                  <p className="mt-1 font-mono text-xs">{error.message}</p>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Products Grid */}
      {productsResponse && (
        <>
          {/* Results Summary */}
          <div className="mb-6">
            <div className="flex items-center justify-between">
              <p className="text-sm text-gray-700">
                Showing {productsResponse.numberOfElements} of {productsResponse.totalElements} products
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

          {/* Products Grid */}
          {productsResponse.content.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-8">
              {productsResponse.content.map((product) => (
                <ProductCard 
                  key={product.id} 
                  product={product} 
                  onClick={() => handleProductClick(product.id)}
                />
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <div className="mx-auto h-12 w-12 text-gray-400">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
              <h3 className="mt-2 text-sm font-medium text-gray-900">No products found</h3>
              <p className="mt-1 text-sm text-gray-500">
                Try adjusting your search filters or clear all filters to see all products.
              </p>
            </div>
          )}

          {/* Pagination */}
          <Pagination
            currentPage={productsResponse.number}
            totalPages={productsResponse.totalPages}
            totalElements={productsResponse.totalElements}
            pageSize={productsResponse.size}
            onPageChange={handlePageChange}
          />
        </>
      )}

      {/* Product Drawer */}
      <ProductDrawer
        productId={selectedProductId}
        isOpen={isDrawerOpen}
        onClose={handleCloseDrawer}
      />
    </div>
  )
}
