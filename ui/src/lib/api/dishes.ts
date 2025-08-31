import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from './client'
import type { 
  Dish, 
  DishCreateDto, 
  DishUpdateDto, 
  DishFilters, 
  PageResponse 
} from '@/lib/types'

// Query Keys
export const dishKeys = {
  all: ['dishes'] as const,
  lists: () => [...dishKeys.all, 'list'] as const,
  list: (filters: DishFilters) => [...dishKeys.lists(), filters] as const,
  details: () => [...dishKeys.all, 'detail'] as const,
  detail: (id: string) => [...dishKeys.details(), id] as const,
  categories: () => [...dishKeys.all, 'categories'] as const,
}

// API Functions
export const dishesApi = {
  async getDishes(filters: DishFilters = {}): Promise<PageResponse<Dish>> {
    const searchParams = new URLSearchParams()
    
    if (filters.query) searchParams.append('query', filters.query)
    if (filters.category) searchParams.append('category', filters.category)
    if (filters.maxPhe !== undefined) searchParams.append('maxPhe', filters.maxPhe.toString())
    if (filters.page !== undefined) searchParams.append('page', filters.page.toString())
    if (filters.size !== undefined) searchParams.append('size', filters.size.toString())
    
    const queryString = searchParams.toString()
    const endpoint = `/api/v1/dishes${queryString ? `?${queryString}` : ''}`
    
    return apiClient.get<PageResponse<Dish>>(endpoint)
  },

  async getDish(id: string): Promise<Dish> {
    return apiClient.get<Dish>(`/api/v1/dishes/${id}`)
  },

  async getDishCategories(): Promise<string[]> {
    return apiClient.get<string[]>('/api/v1/dishes/categories')
  },

  async createDish(data: DishCreateDto): Promise<Dish> {
    return apiClient.post<Dish>('/api/v1/dishes', data)
  },

  async updateDish(id: string, data: DishUpdateDto): Promise<Dish> {
    return apiClient.put<Dish>(`/api/v1/dishes/${id}`, data)
  },

  async deleteDish(id: string): Promise<void> {
    return apiClient.delete<void>(`/api/v1/dishes/${id}`)
  },

  async duplicateDish(id: string, name?: string): Promise<Dish> {
    return apiClient.post<Dish>(`/api/v1/dishes/${id}/duplicate`, { name })
  },

  async scaleDish(id: string, scaleFactor: number): Promise<Dish> {
    return apiClient.post<Dish>(`/api/v1/dishes/${id}/scale`, { scaleFactor })
  },

  async solveMass(id: string, targetPhenylalanine: number): Promise<Dish> {
    return apiClient.post<Dish>(`/api/v1/dishes/${id}/solve-mass`, { targetPhenylalanine })
  },
}

// Query Hooks
export function useDishes(filters: DishFilters = {}) {
  return useQuery({
    queryKey: dishKeys.list(filters),
    queryFn: () => dishesApi.getDishes(filters),
    keepPreviousData: true,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useDish(id: string, enabled: boolean = true) {
  return useQuery({
    queryKey: dishKeys.detail(id),
    queryFn: () => dishesApi.getDish(id),
    enabled: enabled && !!id,
    staleTime: 10 * 60 * 1000, // 10 minutes
  })
}

export function useDishCategories() {
  return useQuery({
    queryKey: dishKeys.categories(),
    queryFn: () => dishesApi.getDishCategories(),
    staleTime: 30 * 60 * 1000, // 30 minutes
  })
}

// Mutation Hooks
export function useCreateDish() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: dishesApi.createDish,
    onSuccess: () => {
      // Invalidate and refetch dishes list
      queryClient.invalidateQueries({ queryKey: dishKeys.lists() })
      queryClient.invalidateQueries({ queryKey: dishKeys.categories() })
    },
  })
}

export function useUpdateDish() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: DishUpdateDto }) =>
      dishesApi.updateDish(id, data),
    onSuccess: (updatedDish) => {
      // Update the specific dish in cache
      queryClient.setQueryData(
        dishKeys.detail(updatedDish.id),
        updatedDish
      )
      // Invalidate lists to ensure consistency
      queryClient.invalidateQueries({ queryKey: dishKeys.lists() })
    },
  })
}

export function useDeleteDish() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: dishesApi.deleteDish,
    onSuccess: (_, deletedId) => {
      // Remove from cache
      queryClient.removeQueries({ queryKey: dishKeys.detail(deletedId) })
      // Invalidate lists
      queryClient.invalidateQueries({ queryKey: dishKeys.lists() })
    },
  })
}

export function useDuplicateDish() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, name }: { id: string; name?: string }) =>
      dishesApi.duplicateDish(id, name),
    onSuccess: () => {
      // Invalidate dishes list to show the new duplicate
      queryClient.invalidateQueries({ queryKey: dishKeys.lists() })
    },
  })
}

export function useScaleDish() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, scaleFactor }: { id: string; scaleFactor: number }) =>
      dishesApi.scaleDish(id, scaleFactor),
    onSuccess: (updatedDish) => {
      // Update the specific dish in cache
      queryClient.setQueryData(dishKeys.detail(updatedDish.id), updatedDish)
      // Invalidate lists to ensure consistency
      queryClient.invalidateQueries({ queryKey: dishKeys.lists() })
    },
  })
}

export function useSolveMass() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, targetPhenylalanine }: { id: string; targetPhenylalanine: number }) =>
      dishesApi.solveMass(id, targetPhenylalanine),
    onSuccess: (updatedDish) => {
      // Update the specific dish in cache
      queryClient.setQueryData(dishKeys.detail(updatedDish.id), updatedDish)
      // Invalidate lists to ensure consistency
      queryClient.invalidateQueries({ queryKey: dishKeys.lists() })
    },
  })
}
