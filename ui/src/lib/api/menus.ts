import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from './client'
import type { 
  MenuDay, 
  MenuWeek,
  MenuGenerateRequest,
  MealSlotCreateDto,
  MealEntryCreateDto,
  MealEntryUpdateDto,
  MenuFilters,
  MenuValidationResult,
  SnackSuggestion,
  PageResponse 
} from '@/lib/types'

// Query Keys
export const menuKeys = {
  all: ['menus'] as const,
  days: () => [...menuKeys.all, 'days'] as const,
  day: (date: string) => [...menuKeys.days(), date] as const,
  weeks: () => [...menuKeys.all, 'weeks'] as const,
  week: (startDate: string) => [...menuKeys.weeks(), startDate] as const,
  lists: () => [...menuKeys.all, 'list'] as const,
  list: (filters: MenuFilters) => [...menuKeys.lists(), filters] as const,
}

// API Functions
export const menusApi = {
  async getMenuDays(filters: MenuFilters = {}): Promise<PageResponse<MenuDay>> {
    const searchParams = new URLSearchParams()
    
    if (filters.startDate) searchParams.append('startDate', filters.startDate)
    if (filters.endDate) searchParams.append('endDate', filters.endDate)
    if (filters.isGenerated !== undefined) searchParams.append('isGenerated', filters.isGenerated.toString())
    if (filters.page !== undefined) searchParams.append('page', filters.page.toString())
    if (filters.size !== undefined) searchParams.append('size', filters.size.toString())
    
    const queryString = searchParams.toString()
    const endpoint = `/api/v1/menus/days${queryString ? `?${queryString}` : ''}`
    
    return apiClient.get<PageResponse<MenuDay>>(endpoint)
  },

  async getMenuDay(date: string): Promise<MenuDay> {
    return apiClient.get<MenuDay>(`/api/v1/menus/days/${date}`)
  },

  async getMenuWeek(startDate: string): Promise<MenuWeek> {
    return apiClient.get<MenuWeek>(`/api/v1/menus/weeks/${startDate}`)
  },

  async generateMenu(request: MenuGenerateRequest): Promise<MenuDay> {
    return apiClient.post<MenuDay>('/api/v1/menus/generate', request)
  },

  async createMenuDay(date: string, pheLimit: number): Promise<MenuDay> {
    return apiClient.post<MenuDay>('/api/v1/menus/days', { date, pheLimit })
  },

  async updateMenuDay(id: string, data: { pheLimit?: number }): Promise<MenuDay> {
    return apiClient.put<MenuDay>(`/api/v1/menus/days/${id}`, data)
  },

  async deleteMenuDay(id: string): Promise<void> {
    return apiClient.delete<void>(`/api/v1/menus/days/${id}`)
  },

  // Meal slot operations
  async createMealSlot(menuDayId: string, data: MealSlotCreateDto): Promise<MenuDay> {
    return apiClient.post<MenuDay>(`/api/v1/menus/days/${menuDayId}/meals`, data)
  },

  async updateMealSlot(mealId: string, data: Partial<MealSlotCreateDto>): Promise<MenuDay> {
    return apiClient.put<MenuDay>(`/api/v1/menus/meals/${mealId}`, data)
  },

  async deleteMealSlot(mealId: string): Promise<MenuDay> {
    return apiClient.delete<MenuDay>(`/api/v1/menus/meals/${mealId}`)
  },

  // Meal entry operations
  async createMealEntry(mealId: string, data: MealEntryCreateDto): Promise<MenuDay> {
    return apiClient.post<MenuDay>(`/api/v1/menus/meals/${mealId}/entries`, data)
  },

  async updateMealEntry(entryId: string, data: MealEntryUpdateDto): Promise<MenuDay> {
    return apiClient.put<MenuDay>(`/api/v1/menus/entries/${entryId}`, data)
  },

  async deleteMealEntry(entryId: string): Promise<MenuDay> {
    return apiClient.delete<MenuDay>(`/api/v1/menus/entries/${entryId}`)
  },

  async markEntryConsumed(entryId: string, consumed: boolean = true): Promise<MenuDay> {
    return apiClient.patch<MenuDay>(`/api/v1/menus/entries/${entryId}/consumed`, { 
      consumed,
      consumedAt: consumed ? new Date().toISOString() : undefined
    })
  },

  async validateDay(dayId: string): Promise<MenuValidationResult> {
    return apiClient.post<MenuValidationResult>(`/api/v1/menus/days/${dayId}/validate`)
  },

  async getSnackSuggestions(dayId: string, type: string = 'snack'): Promise<SnackSuggestion[]> {
    return apiClient.get<SnackSuggestion[]>(`/api/v1/menus/days/${dayId}/suggestions?type=${type}`)
  },

  async generateWeek(request: MenuGenerateRequest): Promise<MenuWeek> {
    return apiClient.post<MenuWeek>('/api/v1/menus/weeks/generate', request)
  },

  async generateDay(request: MenuGenerateRequest): Promise<MenuDay> {
    return apiClient.post<MenuDay>('/api/v1/menus/days/generate', request)
  },
}

// Query Hooks
export function useMenuDays(filters: MenuFilters = {}) {
  return useQuery({
    queryKey: menuKeys.list(filters),
    queryFn: () => menusApi.getMenuDays(filters),
    keepPreviousData: true,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useMenuDay(date: string, enabled: boolean = true) {
  return useQuery({
    queryKey: menuKeys.day(date),
    queryFn: () => menusApi.getMenuDay(date),
    enabled: enabled && !!date,
    staleTime: 1 * 60 * 1000, // 1 minute
  })
}

export function useMenuWeek(startDate: string, enabled: boolean = true) {
  return useQuery({
    queryKey: menuKeys.week(startDate),
    queryFn: () => menusApi.getMenuWeek(startDate),
    enabled: enabled && !!startDate,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

// Mutation Hooks
export function useGenerateMenu() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: menusApi.generateMenu,
    onSuccess: (generatedMenu) => {
      // Update the specific day in cache
      queryClient.setQueryData(
        menuKeys.day(generatedMenu.date),
        generatedMenu
      )
      // Invalidate lists to ensure consistency
      queryClient.invalidateQueries({ queryKey: menuKeys.lists() })
      queryClient.invalidateQueries({ queryKey: menuKeys.weeks() })
    },
  })
}

export function useCreateMenuDay() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ date, pheLimit }: { date: string; pheLimit: number }) =>
      menusApi.createMenuDay(date, pheLimit),
    onSuccess: (newMenuDay) => {
      // Add to cache
      queryClient.setQueryData(
        menuKeys.day(newMenuDay.date),
        newMenuDay
      )
      // Invalidate lists
      queryClient.invalidateQueries({ queryKey: menuKeys.lists() })
    },
  })
}

export function useUpdateMenuDay() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: { pheLimit?: number } }) =>
      menusApi.updateMenuDay(id, data),
    onSuccess: (updatedMenuDay) => {
      // Update in cache
      queryClient.setQueryData(
        menuKeys.day(updatedMenuDay.date),
        updatedMenuDay
      )
      // Invalidate related queries
      queryClient.invalidateQueries({ queryKey: menuKeys.weeks() })
    },
  })
}

export function useDeleteMenuDay() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: menusApi.deleteMenuDay,
    onSuccess: (_, deletedId) => {
      // Invalidate all menu queries since we don't have the date
      queryClient.invalidateQueries({ queryKey: menuKeys.all })
    },
  })
}

// Meal slot mutations
export function useCreateMealSlot() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ menuDayId, data }: { menuDayId: string; data: MealSlotCreateDto }) =>
      menusApi.createMealSlot(menuDayId, data),
    onSuccess: (updatedMenuDay) => {
      // Update the menu day in cache
      queryClient.setQueryData(
        menuKeys.day(updatedMenuDay.date),
        updatedMenuDay
      )
      queryClient.invalidateQueries({ queryKey: menuKeys.weeks() })
    },
  })
}

export function useUpdateMealSlot() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ mealId, data }: { mealId: string; data: Partial<MealSlotCreateDto> }) =>
      menusApi.updateMealSlot(mealId, data),
    onSuccess: (updatedMenuDay) => {
      queryClient.setQueryData(
        menuKeys.day(updatedMenuDay.date),
        updatedMenuDay
      )
      queryClient.invalidateQueries({ queryKey: menuKeys.weeks() })
    },
  })
}

export function useDeleteMealSlot() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: menusApi.deleteMealSlot,
    onSuccess: (updatedMenuDay) => {
      queryClient.setQueryData(
        menuKeys.day(updatedMenuDay.date),
        updatedMenuDay
      )
      queryClient.invalidateQueries({ queryKey: menuKeys.weeks() })
    },
  })
}

// Meal entry mutations
export function useCreateMealEntry() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ mealId, data }: { mealId: string; data: MealEntryCreateDto }) =>
      menusApi.createMealEntry(mealId, data),
    onSuccess: (updatedMenuDay) => {
      queryClient.setQueryData(
        menuKeys.day(updatedMenuDay.date),
        updatedMenuDay
      )
      queryClient.invalidateQueries({ queryKey: menuKeys.weeks() })
    },
  })
}

export function useUpdateMealEntry() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ entryId, data }: { entryId: string; data: MealEntryUpdateDto }) =>
      menusApi.updateMealEntry(entryId, data),
    onSuccess: (updatedMenuDay) => {
      queryClient.setQueryData(
        menuKeys.day(updatedMenuDay.date),
        updatedMenuDay
      )
      queryClient.invalidateQueries({ queryKey: menuKeys.weeks() })
    },
  })
}

export function useDeleteMealEntry() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: menusApi.deleteMealEntry,
    onSuccess: (updatedMenuDay) => {
      queryClient.setQueryData(
        menuKeys.day(updatedMenuDay.date),
        updatedMenuDay
      )
      queryClient.invalidateQueries({ queryKey: menuKeys.weeks() })
    },
  })
}

export function useMarkEntryConsumed() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ entryId, consumed }: { entryId: string; consumed: boolean }) =>
      menusApi.markEntryConsumed(entryId, consumed),
    onSuccess: (updatedMenuDay) => {
      queryClient.setQueryData(
        menuKeys.day(updatedMenuDay.date),
        updatedMenuDay
      )
      queryClient.invalidateQueries({ queryKey: menuKeys.weeks() })
    },
  })
}

export function useValidateDay() {
  return useMutation({
    mutationFn: (dayId: string) => menusApi.validateDay(dayId),
  })
}

export function useSnackSuggestions(dayId: string, type: string = 'snack', enabled: boolean = true) {
  return useQuery({
    queryKey: [...menuKeys.day(dayId), 'suggestions', type],
    queryFn: () => menusApi.getSnackSuggestions(dayId, type),
    enabled: enabled && !!dayId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useGenerateWeek() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: MenuGenerateRequest) => menusApi.generateWeek(request),
    onSuccess: (generatedWeek) => {
      // Update the week cache
      if (generatedWeek.startDate) {
        queryClient.setQueryData(menuKeys.week(generatedWeek.startDate), generatedWeek)
      }
      // Invalidate all menu lists to ensure consistency
      queryClient.invalidateQueries({ queryKey: menuKeys.lists() })
      queryClient.invalidateQueries({ queryKey: menuKeys.weeks() })
    },
  })
}

export function useGenerateDay() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: MenuGenerateRequest) => menusApi.generateDay(request),
    onSuccess: (generatedDay) => {
      // Update the specific day cache
      queryClient.setQueryData(menuKeys.day(generatedDay.date), generatedDay)
      // Invalidate lists and weeks to ensure consistency
      queryClient.invalidateQueries({ queryKey: menuKeys.lists() })
      queryClient.invalidateQueries({ queryKey: menuKeys.weeks() })
    },
  })
}
