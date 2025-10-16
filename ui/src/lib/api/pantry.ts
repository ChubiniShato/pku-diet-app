import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from './client'
import type { PantryItem, PantryFilters, PageResponse } from '@/lib/types'

// Query Keys
export const pantryKeys = {
  all: ['pantry'] as const,
  lists: () => [...pantryKeys.all, 'list'] as const,
  list: (filters: PantryFilters) => [...pantryKeys.lists(), filters] as const,
  details: () => [...pantryKeys.all, 'detail'] as const,
  detail: (id: string) => [...pantryKeys.details(), id] as const,
}

// API Functions
export const pantryApi = {
  async getPantryItems(filters: PantryFilters = {}): Promise<PageResponse<PantryItem>> {
    const searchParams = new URLSearchParams()
    
    if (filters.query) searchParams.append('query', filters.query)
    if (filters.category) searchParams.append('category', filters.category)
    if (filters.expiringWithinDays !== undefined) {
      searchParams.append('expiringWithinDays', filters.expiringWithinDays.toString())
    }
    if (filters.page !== undefined) searchParams.append('page', filters.page.toString())
    if (filters.size !== undefined) searchParams.append('size', filters.size.toString())
    if (filters.sort) searchParams.append('sort', filters.sort)
    
    const queryString = searchParams.toString()
    const endpoint = `/api/v1/pantry${queryString ? `?${queryString}` : ''}`
    
    return apiClient.get<PageResponse<PantryItem>>(endpoint)
  },

  async getPantryItem(id: string): Promise<PantryItem> {
    return apiClient.get<PantryItem>(`/api/v1/pantry/${id}`)
  },

  async createPantryItem(data: Omit<PantryItem, 'id' | 'createdAt' | 'updatedAt'>): Promise<PantryItem> {
    return apiClient.post<PantryItem>('/api/v1/pantry', data)
  },

  async updatePantryItem(id: string, data: Partial<PantryItem>): Promise<PantryItem> {
    return apiClient.put<PantryItem>(`/api/v1/pantry/${id}`, data)
  },

  async deletePantryItem(id: string): Promise<void> {
    return apiClient.delete<void>(`/api/v1/pantry/${id}`)
  },

  async bulkUpdatePantry(items: Partial<PantryItem>[]): Promise<PantryItem[]> {
    return apiClient.post<PantryItem[]>('/api/v1/pantry/bulk', { items })
  },
}

// Query Hooks
export function usePantryItems(filters: PantryFilters = {}) {
  return useQuery({
    queryKey: pantryKeys.list(filters),
    queryFn: () => pantryApi.getPantryItems(filters),
    keepPreviousData: true,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function usePantryItem(id: string, enabled: boolean = true) {
  return useQuery({
    queryKey: pantryKeys.detail(id),
    queryFn: () => pantryApi.getPantryItem(id),
    enabled: enabled && !!id,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useCreatePantryItem() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: Omit<PantryItem, 'id' | 'createdAt' | 'updatedAt'>) =>
      pantryApi.createPantryItem(data),
    onSuccess: () => {
      // Invalidate and refetch pantry lists
      queryClient.invalidateQueries({ queryKey: pantryKeys.lists() })
    },
  })
}

export function useUpdatePantryItem() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<PantryItem> }) =>
      pantryApi.updatePantryItem(id, data),
    onSuccess: (updatedItem) => {
      // Update the specific item in cache
      queryClient.setQueryData(pantryKeys.detail(updatedItem.id), updatedItem)
      // Invalidate lists to ensure consistency
      queryClient.invalidateQueries({ queryKey: pantryKeys.lists() })
    },
  })
}

export function useDeletePantryItem() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => pantryApi.deletePantryItem(id),
    onSuccess: () => {
      // Invalidate lists to remove deleted item
      queryClient.invalidateQueries({ queryKey: pantryKeys.lists() })
    },
  })
}

export function useBulkUpdatePantry() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (items: Partial<PantryItem>[]) => pantryApi.bulkUpdatePantry(items),
    onSuccess: () => {
      // Invalidate all pantry data
      queryClient.invalidateQueries({ queryKey: pantryKeys.all })
    },
  })
}