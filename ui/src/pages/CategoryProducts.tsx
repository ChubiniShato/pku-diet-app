import React, { useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate, useParams } from 'react-router-dom'
import { useProducts } from '@/lib/api/products'
import { ProductCard } from '@/components/ProductCard'
import { ProductDrawer } from '@/components/ProductDrawer'
import { Pagination } from '@/components/Pagination'
import type { ProductSearchParams } from '@/lib/types'

function getAlphabetForLanguage(lang: string): string[] {
  const l = lang?.toLowerCase() || 'en'
  if (l.startsWith('ka')) {
    return 'აბგდევზთიკლმნოპჟრსტუფქღყშჩცძწჭხჯჰ'.split('')
  }
  if (l.startsWith('uk')) {
    return ['А','Б','В','Г','Ґ','Д','Е','Є','Ж','З','И','І','Ї','Й','К','Л','М','Н','О','П','Р','С','Т','У','Ф','Х','Ц','Ч','Ш','Щ','Ь','Ю','Я']
  }
  if (l.startsWith('ru')) {
    return ['А','Б','В','Г','Д','Е','Ё','Ж','З','И','Й','К','Л','М','Н','О','П','Р','С','Т','У','Ф','Х','Ц','Ч','Ш','Щ','Ъ','Ы','Ь','Э','Ю','Я']
  }
  return Array.from({ length: 26 }, (_, i) => String.fromCharCode(65 + i))
}

export const CategoryProducts: React.FC = () => {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const { categoryName } = useParams<{ categoryName: string }>()
  
  const [filters, setFilters] = useState<ProductSearchParams>({
    page: 0,
    size: 1000,
    category: categoryName ? decodeURIComponent(categoryName) : undefined,
  })
  const [selectedProductId, setSelectedProductId] = useState<string | null>(null)
  const [isDrawerOpen, setIsDrawerOpen] = useState(false)
  const [activeLetter, setActiveLetter] = useState<string | null>(null)

  const {
    data: productsResponse,
    isLoading,
    error,
    isFetching,
  } = useProducts(filters, i18n.language)

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

  const handleBackToProducts = () => {
    navigate('/products')
  }

  if (!categoryName) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center py-12">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">
            {t('common.error')}
          </h1>
          <p className="text-gray-600 mb-6">
            Category not found
          </p>
          <button
            onClick={handleBackToProducts}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
          >
            {t('common.back')} to Products
          </button>
        </div>
      </div>
    )
  }

  const displayCategoryName = decodeURIComponent(categoryName)

  const alphabet = useMemo(() => getAlphabetForLanguage(i18n.language), [i18n.language])

  const filteredAndSorted = useMemo(() => {
    const list = productsResponse?.content ?? []
    const normalizedLetter = activeLetter?.toLocaleUpperCase(i18n.language) || null
    const filtered = normalizedLetter
      ? list.filter(p => {
          const first = (p.name || '').trim().charAt(0)
          if (!first) return false
          return first.toLocaleUpperCase(i18n.language) === normalizedLetter
        })
      : list
    return [...filtered].sort((a, b) => (a.name || '').localeCompare(b.name || '', i18n.language, { sensitivity: 'base' }))
  }, [productsResponse, activeLetter, i18n.language])

  const clearLetter = () => setActiveLetter(null)

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header with Back Button */}
      <div className="mb-8">
        <div className="flex items-center mb-4">
          <button
            onClick={handleBackToProducts}
            className="flex items-center text-gray-600 hover:text-gray-900 mr-4"
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
            {t('common.back')}
          </button>
        </div>
        <h1 className="text-3xl font-bold text-gray-900">
          {displayCategoryName}
        </h1>
        <p className="mt-2 text-gray-600">
          {t('pages.products.subtitle')}
        </p>
      </div>

      {/* Alphabet Filter Box */}
      <div className="mb-6 border border-gray-200 rounded-lg p-3 bg-white">
        <div className="flex flex-wrap gap-2 items-center">
          {alphabet.map(letter => (
            <button
              key={letter}
              onClick={() => { setActiveLetter(letter); setFilters(prev => ({ ...prev, page: 0 })) }}
              className={`px-3 py-1 rounded-md text-sm font-medium border ${
                activeLetter === letter
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'bg-gray-50 text-gray-700 border-gray-200 hover:bg-gray-100'
              }`}
            >
              {letter}
            </button>
          ))}
          <button
            onClick={clearLetter}
            className={`ml-auto px-3 py-1 rounded-md text-sm font-medium border ${
              activeLetter == null ? 'bg-gray-200 text-gray-700 border-gray-200' : 'bg-gray-50 text-gray-700 border-gray-200 hover:bg-gray-100'
            }`}
          >
            {t('common.clear') || 'Clear'}
          </button>
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
                Showing {filteredAndSorted.length} of {productsResponse.totalElements} products
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
          {filteredAndSorted.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-8">
              {filteredAndSorted.map((product) => (
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
                No products found in this category.
              </p>
            </div>
          )}

          {/* Pagination */}
          {filteredAndSorted.length > 0 && (
            <Pagination
              currentPage={productsResponse.number}
              totalPages={productsResponse.totalPages}
              totalElements={productsResponse.totalElements}
              pageSize={productsResponse.size}
              onPageChange={handlePageChange}
            />
          )}
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
