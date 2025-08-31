import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from './client'
import type { 
  CriticalFact,
  CriticalFactFilters,
  CriticalFactSummary,
  CriticalFactExportRequest,
  CriticalFactExportResponse,
  PageResponse
} from '@/lib/types'

// Query Keys
export const criticalFactsKeys = {
  all: ['criticalFacts'] as const,
  lists: () => [...criticalFactsKeys.all, 'list'] as const,
  list: (filters: CriticalFactFilters) => [...criticalFactsKeys.lists(), filters] as const,
  details: () => [...criticalFactsKeys.all, 'detail'] as const,
  detail: (id: string) => [...criticalFactsKeys.details(), id] as const,
  summary: (patientId: string) => [...criticalFactsKeys.all, 'summary', patientId] as const,
  exports: () => [...criticalFactsKeys.all, 'exports'] as const,
}

// API Functions
export const criticalFactsApi = {
  async getCriticalFacts(filters: CriticalFactFilters = {}): Promise<PageResponse<CriticalFact>> {
    const searchParams = new URLSearchParams()
    
    if (filters.patientId) searchParams.append('patientId', filters.patientId)
    if (filters.type?.length) {
      filters.type.forEach(type => searchParams.append('type', type))
    }
    if (filters.severity?.length) {
      filters.severity.forEach(severity => searchParams.append('severity', severity))
    }
    if (filters.source?.length) {
      filters.source.forEach(source => searchParams.append('source', source))
    }
    if (filters.dateFrom) searchParams.append('dateFrom', filters.dateFrom)
    if (filters.dateTo) searchParams.append('dateTo', filters.dateTo)
    if (filters.resolved !== undefined) searchParams.append('resolved', filters.resolved.toString())
    if (filters.page !== undefined) searchParams.append('page', filters.page.toString())
    if (filters.size !== undefined) searchParams.append('size', filters.size.toString())
    if (filters.sort) searchParams.append('sort', filters.sort)
    if (filters.sortDirection) searchParams.append('sortDirection', filters.sortDirection)
    
    const queryString = searchParams.toString()
    const endpoint = `/api/v1/critical-facts${queryString ? `?${queryString}` : ''}`
    
    return apiClient.get<PageResponse<CriticalFact>>(endpoint)
  },

  async getCriticalFact(id: string): Promise<CriticalFact> {
    return apiClient.get<CriticalFact>(`/api/v1/critical-facts/${id}`)
  },

  async getCriticalFactsByDate(patientId: string, date: string): Promise<CriticalFact[]> {
    const searchParams = new URLSearchParams()
    searchParams.append('patientId', patientId)
    searchParams.append('date', date)
    
    return apiClient.get<CriticalFact[]>(`/api/v1/critical-facts/by-date?${searchParams.toString()}`)
  },

  async getCriticalFactsForDateRange(patientId: string, startDate: string, endDate: string): Promise<CriticalFact[]> {
    const searchParams = new URLSearchParams()
    searchParams.append('patientId', patientId)
    searchParams.append('startDate', startDate)
    searchParams.append('endDate', endDate)
    
    return apiClient.get<CriticalFact[]>(`/api/v1/critical-facts/date-range?${searchParams.toString()}`)
  },

  async getCriticalFactsSummary(patientId: string): Promise<CriticalFactSummary> {
    return apiClient.get<CriticalFactSummary>(`/api/v1/critical-facts/summary?patientId=${patientId}`)
  },

  async resolveCriticalFact(id: string, notes?: string): Promise<CriticalFact> {
    return apiClient.post<CriticalFact>(`/api/v1/critical-facts/${id}/resolve`, {
      notes,
    })
  },

  async unresolveeCriticalFact(id: string): Promise<CriticalFact> {
    return apiClient.post<CriticalFact>(`/api/v1/critical-facts/${id}/unresolve`)
  },

  async exportCriticalFacts(request: CriticalFactExportRequest): Promise<CriticalFactExportResponse> {
    return apiClient.post<CriticalFactExportResponse>('/api/v1/critical-facts/export', request)
  },
}

// Query Hooks
export function useCriticalFacts(filters: CriticalFactFilters = {}, enabled: boolean = true) {
  return useQuery({
    queryKey: criticalFactsKeys.list(filters),
    queryFn: () => criticalFactsApi.getCriticalFacts(filters),
    enabled,
    keepPreviousData: true,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useCriticalFact(id: string, enabled: boolean = true) {
  return useQuery({
    queryKey: criticalFactsKeys.detail(id),
    queryFn: () => criticalFactsApi.getCriticalFact(id),
    enabled: enabled && !!id,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useCriticalFactsByDate(patientId: string, date: string, enabled: boolean = true) {
  return useQuery({
    queryKey: [...criticalFactsKeys.all, 'by-date', patientId, date],
    queryFn: () => criticalFactsApi.getCriticalFactsByDate(patientId, date),
    enabled: enabled && !!patientId && !!date,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useCriticalFactsForDateRange(patientId: string, startDate: string, endDate: string, enabled: boolean = true) {
  return useQuery({
    queryKey: [...criticalFactsKeys.all, 'date-range', patientId, startDate, endDate],
    queryFn: () => criticalFactsApi.getCriticalFactsForDateRange(patientId, startDate, endDate),
    enabled: enabled && !!patientId && !!startDate && !!endDate,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useCriticalFactsSummary(patientId: string, enabled: boolean = true) {
  return useQuery({
    queryKey: criticalFactsKeys.summary(patientId),
    queryFn: () => criticalFactsApi.getCriticalFactsSummary(patientId),
    enabled: enabled && !!patientId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useResolveCriticalFact() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, notes }: { id: string; notes?: string }) =>
      criticalFactsApi.resolveCriticalFact(id, notes),
    onSuccess: (updatedFact) => {
      // Update the specific fact in cache
      queryClient.setQueryData(criticalFactsKeys.detail(updatedFact.id), updatedFact)
      
      // Invalidate lists to refresh counts and filters
      queryClient.invalidateQueries({ queryKey: criticalFactsKeys.lists() })
      queryClient.invalidateQueries({ queryKey: criticalFactsKeys.summary(updatedFact.patientId) })
    },
  })
}

export function useUnresolveCriticalFact() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => criticalFactsApi.unresolveeCriticalFact(id),
    onSuccess: (updatedFact) => {
      // Update the specific fact in cache
      queryClient.setQueryData(criticalFactsKeys.detail(updatedFact.id), updatedFact)
      
      // Invalidate lists to refresh counts and filters
      queryClient.invalidateQueries({ queryKey: criticalFactsKeys.lists() })
      queryClient.invalidateQueries({ queryKey: criticalFactsKeys.summary(updatedFact.patientId) })
    },
  })
}

export function useExportCriticalFacts() {
  return useMutation({
    mutationFn: (request: CriticalFactExportRequest) =>
      criticalFactsApi.exportCriticalFacts(request),
  })
}
