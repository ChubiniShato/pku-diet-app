import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from './client'
import type { 
  Product, 
  ProductUpsertDto, 
  ProductSearchParams, 
  PageResponse 
} from '@/lib/types'

// Query Keys
export const productKeys = {
  all: ['products'] as const,
  lists: () => [...productKeys.all, 'list'] as const,
  list: (params: ProductSearchParams) => [...productKeys.lists(), params] as const,
  details: () => [...productKeys.all, 'detail'] as const,
  detail: (id: string) => [...productKeys.details(), id] as const,
  categories: (lang?: string) => [...productKeys.all, 'categories', { lang }] as const,
  lowPhe: (maxPhe: number, page?: number, size?: number) => 
    [...productKeys.all, 'low-phe', { maxPhe, page, size }] as const,
}

// API Functions
export const productsApi = {
  async getProducts(params: ProductSearchParams = {}): Promise<PageResponse<Product>> {
    // If maxPhe is set, use the low-phe endpoint
    if (params.maxPhe !== undefined) {
      return this.getLowPheProducts(params.maxPhe, params.page, params.size)
    }

    const searchParams = new URLSearchParams()
    
    if (params.query) searchParams.append('query', params.query)
    if (params.category) searchParams.append('category', params.category)
    if (params.page !== undefined) searchParams.append('page', params.page.toString())
    if (params.size !== undefined) searchParams.append('size', params.size.toString())
    if (params.sort) searchParams.append('sort', params.sort)
    
    const queryString = searchParams.toString()
    const endpoint = `/api/v1/products${queryString ? `?${queryString}` : ''}`
    
    return apiClient.get<PageResponse<Product>>(endpoint)
  },

  async getProduct(id: string): Promise<Product> {
    return apiClient.get<Product>(`/api/v1/products/${id}`)
  },

  async getCategoriesLocalized(lang?: string): Promise<string[]> {
    // Fallback to non-localized endpoint if localized fails
    try {
      const param = lang ? `?lang=${encodeURIComponent(lang)}` : ''
      return await apiClient.get<string[]>(`/api/v1/products/categories-localized${param}`)
    } catch (error) {
      console.warn('Localized categories failed, falling back to basic categories:', error)
      return await apiClient.get<string[]>('/api/v1/products/categories')
    }
  },

  async getProductsByCategory(
    category: string, 
    page: number = 0, 
    size: number = 20
  ): Promise<PageResponse<Product>> {
    return apiClient.get<PageResponse<Product>>(
      `/api/v1/products/category/${encodeURIComponent(category)}?page=${page}&size=${size}`
    )
  },

  async getLowPheProducts(
    maxPhe: number, 
    page: number = 0, 
    size: number = 20
  ): Promise<PageResponse<Product>> {
    return apiClient.get<PageResponse<Product>>(
      `/api/v1/products/low-phe?maxPhe=${maxPhe}&page=${page}&size=${size}`
    )
  },

  async createProduct(data: ProductUpsertDto): Promise<Product> {
    return apiClient.post<Product>('/api/v1/products', data)
  },

  async updateProduct(id: string, data: ProductUpsertDto): Promise<Product> {
    return apiClient.put<Product>(`/api/v1/products/${id}`, data)
  },

  async deleteProduct(id: string): Promise<void> {
    return apiClient.delete<void>(`/api/v1/products/${id}`)
  },

  async uploadCsv(file: File): Promise<string> {
    const formData = new FormData()
    formData.append('file', file)

    // Let ApiClient detect FormData and send proper multipart boundaries
    return apiClient.post<string>('/api/v1/products/upload-csv', formData)
  },
}

// Query Hooks
export function useProducts(params: ProductSearchParams = {}, lang?: string) {
  return useQuery({
    queryKey: productKeys.list({ ...params, lang }),
    queryFn: () => {
      // Add lang parameter to API call
      const searchParams = new URLSearchParams()
      if (params.query) searchParams.append('query', params.query)
      if (params.category) searchParams.append('category', params.category)
      if (params.page !== undefined) searchParams.append('page', params.page.toString())
      if (params.size !== undefined) searchParams.append('size', params.size.toString())
      if (params.sort) searchParams.append('sort', params.sort)
      if (lang) searchParams.append('lang', lang)
      
      const queryString = searchParams.toString()
      const endpoint = `/api/v1/products${queryString ? `?${queryString}` : ''}`
      
      return apiClient.get<PageResponse<Product>>(endpoint)
    },
    keepPreviousData: true,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useProduct(id: string, enabled: boolean = true, lang?: string) {
  return useQuery({
    queryKey: productKeys.detail(id + (lang ? `_${lang}` : '')),
    queryFn: () => {
      const param = lang ? `?lang=${encodeURIComponent(lang)}` : ''
      return apiClient.get<Product>(`/api/v1/products/${id}${param}`)
    },
    enabled: enabled && !!id,
    staleTime: 10 * 60 * 1000, // 10 minutes
  })
}

export function useProductCategories(lang?: string) {
  return useQuery({
    queryKey: productKeys.categories(lang),
    queryFn: () => productsApi.getCategoriesLocalized(lang),
    staleTime: 30 * 60 * 1000,
  })
}

export function useProductsByCategory(category: string, page?: number, size?: number) {
  return useQuery({
    queryKey: [...productKeys.all, 'category', category, { page, size }],
    queryFn: () => productsApi.getProductsByCategory(category, page, size),
    enabled: !!category,
    keepPreviousData: true,
    staleTime: 5 * 60 * 1000,
  })
}

export function useLowPheProducts(maxPhe: number, page?: number, size?: number) {
  return useQuery({
    queryKey: productKeys.lowPhe(maxPhe, page, size),
    queryFn: () => productsApi.getLowPheProducts(maxPhe, page, size),
    enabled: maxPhe > 0,
    keepPreviousData: true,
    staleTime: 5 * 60 * 1000,
  })
}

// Mutation Hooks
export function useCreateProduct() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: productsApi.createProduct,
    onSuccess: () => {
      // Invalidate and refetch products list
      queryClient.invalidateQueries({ queryKey: productKeys.lists() })
      queryClient.invalidateQueries({ queryKey: productKeys.categories() })
    },
  })
}

export function useUpdateProduct() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: ProductUpsertDto }) =>
      productsApi.updateProduct(id, data),
    onSuccess: (updatedProduct) => {
      // Update the specific product in cache
      queryClient.setQueryData(
        productKeys.detail(updatedProduct.id),
        updatedProduct
      )
      // Invalidate lists to ensure consistency
      queryClient.invalidateQueries({ queryKey: productKeys.lists() })
    },
  })
}

export function useDeleteProduct() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: productsApi.deleteProduct,
    onSuccess: (_, deletedId) => {
      // Remove from cache
      queryClient.removeQueries({ queryKey: productKeys.detail(deletedId) })
      // Invalidate lists
      queryClient.invalidateQueries({ queryKey: productKeys.lists() })
    },
  })
}

export function useUploadProductsCsv() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: productsApi.uploadCsv,
    onSuccess: () => {
      // Invalidate all product-related queries after CSV upload
      queryClient.invalidateQueries({ queryKey: productKeys.all })
    },
  })
}
