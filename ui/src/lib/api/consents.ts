import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from './client'
import type { 
  Consent, 
  ConsentRequest, 
  ConsentResponse, 
  ConsentType,
  ShareLink,
  ShareLinkCreateRequest,
  ShareLinkResponse,
  ShareAuditLog,
  ShareFilters,
  PageResponse
} from '@/lib/types'

// Query Keys
export const consentsKeys = {
  all: ['consents'] as const,
  lists: () => [...consentsKeys.all, 'list'] as const,
  list: (patientId: string) => [...consentsKeys.lists(), patientId] as const,
}

export const shareLinksKeys = {
  all: ['shareLinks'] as const,
  lists: () => [...shareLinksKeys.all, 'list'] as const,
  list: (filters: ShareFilters) => [...shareLinksKeys.lists(), filters] as const,
  details: () => [...shareLinksKeys.all, 'detail'] as const,
  detail: (id: string) => [...shareLinksKeys.details(), id] as const,
  audit: (id: string) => [...shareLinksKeys.detail(id), 'audit'] as const,
}

// Consents API Functions
export const consentsApi = {
  async getConsents(patientId: string): Promise<ConsentResponse> {
    return apiClient.get<ConsentResponse>(`/api/v1/consents?patientId=${patientId}`)
  },

  async updateConsent(patientId: string, request: ConsentRequest): Promise<Consent> {
    return apiClient.post<Consent>('/api/v1/consents', { ...request, patientId })
  },

  async revokeConsent(patientId: string, consentType: ConsentType): Promise<void> {
    return apiClient.delete<void>(`/api/v1/consents/${consentType}?patientId=${patientId}`)
  },
}

// Share Links API Functions
export const shareLinksApi = {
  async getShareLinks(patientId: string, filters: ShareFilters = {}): Promise<PageResponse<ShareLink>> {
    const searchParams = new URLSearchParams()
    searchParams.append('patientId', patientId)
    
    if (filters.isActive !== undefined) searchParams.append('isActive', filters.isActive.toString())
    if (filters.scope) searchParams.append('scope', filters.scope)
    if (filters.page !== undefined) searchParams.append('page', filters.page.toString())
    if (filters.size !== undefined) searchParams.append('size', filters.size.toString())
    if (filters.sort) searchParams.append('sort', filters.sort)
    
    const queryString = searchParams.toString()
    const endpoint = `/api/v1/share-links${queryString ? `?${queryString}` : ''}`
    
    return apiClient.get<PageResponse<ShareLink>>(endpoint)
  },

  async createShareLink(patientId: string, request: ShareLinkCreateRequest): Promise<ShareLinkResponse> {
    return apiClient.post<ShareLinkResponse>('/api/v1/share-links', { ...request, patientId })
  },

  async getShareLink(id: string): Promise<ShareLink> {
    return apiClient.get<ShareLink>(`/api/v1/share-links/${id}`)
  },

  async revokeShareLink(id: string): Promise<void> {
    return apiClient.delete<void>(`/api/v1/share-links/${id}`)
  },

  async getShareLinkAudit(shareLinkId: string): Promise<ShareAuditLog[]> {
    return apiClient.get<ShareAuditLog[]>(`/api/v1/share-links/${shareLinkId}/audit`)
  },
}

// Consents Query Hooks
export function useConsents(patientId: string, enabled: boolean = true) {
  return useQuery({
    queryKey: consentsKeys.list(patientId),
    queryFn: () => consentsApi.getConsents(patientId),
    enabled: enabled && !!patientId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useUpdateConsent() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ patientId, request }: { patientId: string; request: ConsentRequest }) =>
      consentsApi.updateConsent(patientId, request),
    onSuccess: (updatedConsent) => {
      // Update consents cache
      queryClient.invalidateQueries({ queryKey: consentsKeys.list(updatedConsent.patientId) })
    },
  })
}

export function useRevokeConsent() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ patientId, consentType }: { patientId: string; consentType: ConsentType }) =>
      consentsApi.revokeConsent(patientId, consentType),
    onSuccess: (_, variables) => {
      // Update consents cache
      queryClient.invalidateQueries({ queryKey: consentsKeys.list(variables.patientId) })
    },
  })
}

// Share Links Query Hooks
export function useShareLinks(patientId: string, filters: ShareFilters = {}, enabled: boolean = true) {
  return useQuery({
    queryKey: shareLinksKeys.list({ ...filters, patientId } as any),
    queryFn: () => shareLinksApi.getShareLinks(patientId, filters),
    enabled: enabled && !!patientId,
    keepPreviousData: true,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useShareLink(id: string, enabled: boolean = true) {
  return useQuery({
    queryKey: shareLinksKeys.detail(id),
    queryFn: () => shareLinksApi.getShareLink(id),
    enabled: enabled && !!id,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useCreateShareLink() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ patientId, request }: { patientId: string; request: ShareLinkCreateRequest }) =>
      shareLinksApi.createShareLink(patientId, request),
    onSuccess: () => {
      // Invalidate share links lists
      queryClient.invalidateQueries({ queryKey: shareLinksKeys.lists() })
    },
  })
}

export function useRevokeShareLink() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => shareLinksApi.revokeShareLink(id),
    onSuccess: () => {
      // Invalidate share links lists and details
      queryClient.invalidateQueries({ queryKey: shareLinksKeys.lists() })
      queryClient.invalidateQueries({ queryKey: shareLinksKeys.details() })
    },
  })
}

export function useShareLinkAudit(shareLinkId: string, enabled: boolean = true) {
  return useQuery({
    queryKey: shareLinksKeys.audit(shareLinkId),
    queryFn: () => shareLinksApi.getShareLinkAudit(shareLinkId),
    enabled: enabled && !!shareLinkId,
    staleTime: 30 * 1000, // 30 seconds
  })
}
