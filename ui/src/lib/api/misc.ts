import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from './client'
import type { 
  PatientConsent,
  ConsentRequest,
  ShareLink,
  ShareLinkCreateDto,
  LabelScanSubmission,
  LabelScanRequest,
  LabelScanResponse,
  CriticalFact,
  NotificationMessage,
  NotificationCreateDto,
  NotificationFilters,
  PageResponse 
} from '@/lib/types'

// Query Keys
export const consentKeys = {
  all: ['consents'] as const,
  list: () => [...consentKeys.all, 'list'] as const,
}

export const shareKeys = {
  all: ['shares'] as const,
  lists: () => [...shareKeys.all, 'list'] as const,
  details: () => [...shareKeys.all, 'detail'] as const,
  detail: (id: string) => [...shareKeys.details(), id] as const,
}

export const scanKeys = {
  all: ['scans'] as const,
  lists: () => [...scanKeys.all, 'list'] as const,
  details: () => [...scanKeys.all, 'detail'] as const,
  detail: (id: string) => [...scanKeys.details(), id] as const,
}

export const criticalKeys = {
  all: ['critical'] as const,
  list: () => [...criticalKeys.all, 'list'] as const,
}

export const notificationKeys = {
  all: ['notifications'] as const,
  lists: () => [...notificationKeys.all, 'list'] as const,
  list: (filters: NotificationFilters) => [...notificationKeys.lists(), filters] as const,
  unread: () => [...notificationKeys.all, 'unread'] as const,
}

// API Functions
export const consentApi = {
  async getConsents(): Promise<PatientConsent[]> {
    return apiClient.get<PatientConsent[]>('/api/v1/consents')
  },

  async updateConsent(data: ConsentRequest): Promise<PatientConsent> {
    return apiClient.post<PatientConsent>('/api/v1/consents', data)
  },
}

export const shareApi = {
  async getShareLinks(): Promise<ShareLink[]> {
    return apiClient.get<ShareLink[]>('/api/v1/shares')
  },

  async getShareLink(id: string): Promise<ShareLink> {
    return apiClient.get<ShareLink>(`/api/v1/shares/${id}`)
  },

  async createShareLink(data: ShareLinkCreateDto): Promise<ShareLink> {
    return apiClient.post<ShareLink>('/api/v1/shares', data)
  },

  async deleteShareLink(id: string): Promise<void> {
    return apiClient.delete<void>(`/api/v1/shares/${id}`)
  },

  async getSharedData(shareId: string): Promise<unknown> {
    return apiClient.get<unknown>(`/api/v1/shares/${shareId}/data`)
  },
}

export const scanApi = {
  async getScans(): Promise<PageResponse<LabelScanSubmission>> {
    return apiClient.get<PageResponse<LabelScanSubmission>>('/api/v1/scans')
  },

  async getScan(id: string): Promise<LabelScanSubmission> {
    return apiClient.get<LabelScanSubmission>(`/api/v1/scans/${id}`)
  },

  async submitScan(data: LabelScanRequest): Promise<LabelScanResponse> {
    const formData = new FormData()
    formData.append('image', data.image)
    if (data.notes) formData.append('notes', data.notes)
    
    return apiClient.post<LabelScanResponse>('/api/v1/scans', formData, {
      headers: {
        'Content-Type': undefined as any,
      },
    })
  },

  async provideFeedback(id: string, feedback: string): Promise<LabelScanSubmission> {
    return apiClient.post<LabelScanSubmission>(`/api/v1/scans/${id}/feedback`, { feedback })
  },
}

export const criticalApi = {
  async getCriticalFacts(): Promise<CriticalFact[]> {
    return apiClient.get<CriticalFact[]>('/api/v1/critical')
  },
}

export const notificationApi = {
  async getNotifications(filters: NotificationFilters = {}): Promise<PageResponse<NotificationMessage>> {
    const searchParams = new URLSearchParams()
    
    if (filters.type) searchParams.append('type', filters.type)
    if (filters.priority) searchParams.append('priority', filters.priority)
    if (filters.isRead !== undefined) searchParams.append('isRead', filters.isRead.toString())
    if (filters.startDate) searchParams.append('startDate', filters.startDate)
    if (filters.endDate) searchParams.append('endDate', filters.endDate)
    if (filters.page !== undefined) searchParams.append('page', filters.page.toString())
    if (filters.size !== undefined) searchParams.append('size', filters.size.toString())
    
    const queryString = searchParams.toString()
    const endpoint = `/api/v1/notifications${queryString ? `?${queryString}` : ''}`
    
    return apiClient.get<PageResponse<NotificationMessage>>(endpoint)
  },

  async getUnreadCount(): Promise<number> {
    return apiClient.get<number>('/api/v1/notifications/unread/count')
  },

  async markAsRead(id: string): Promise<NotificationMessage> {
    return apiClient.patch<NotificationMessage>(`/api/v1/notifications/${id}/read`)
  },

  async markAllAsRead(): Promise<void> {
    return apiClient.patch<void>('/api/v1/notifications/read-all')
  },

  async createNotification(data: NotificationCreateDto): Promise<NotificationMessage> {
    return apiClient.post<NotificationMessage>('/api/v1/notifications', data)
  },

  async deleteNotification(id: string): Promise<void> {
    return apiClient.delete<void>(`/api/v1/notifications/${id}`)
  },
}

// Consent Hooks
export function useConsents() {
  return useQuery({
    queryKey: consentKeys.list(),
    queryFn: consentApi.getConsents,
    staleTime: 10 * 60 * 1000, // 10 minutes
  })
}

export function useUpdateConsent() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: consentApi.updateConsent,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: consentKeys.all })
    },
  })
}

// Share Link Hooks
export function useShareLinks() {
  return useQuery({
    queryKey: shareKeys.lists(),
    queryFn: shareApi.getShareLinks,
    staleTime: 5 * 60 * 1000,
  })
}

export function useShareLink(id: string, enabled: boolean = true) {
  return useQuery({
    queryKey: shareKeys.detail(id),
    queryFn: () => shareApi.getShareLink(id),
    enabled: enabled && !!id,
    staleTime: 10 * 60 * 1000,
  })
}

export function useSharedData(shareId: string, enabled: boolean = true) {
  return useQuery({
    queryKey: [...shareKeys.detail(shareId), 'data'],
    queryFn: () => shareApi.getSharedData(shareId),
    enabled: enabled && !!shareId,
    staleTime: 5 * 60 * 1000,
  })
}

export function useCreateShareLink() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: shareApi.createShareLink,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: shareKeys.lists() })
    },
  })
}

export function useDeleteShareLink() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: shareApi.deleteShareLink,
    onSuccess: (_, deletedId) => {
      queryClient.removeQueries({ queryKey: shareKeys.detail(deletedId) })
      queryClient.invalidateQueries({ queryKey: shareKeys.lists() })
    },
  })
}

// Label Scan Hooks
export function useScans() {
  return useQuery({
    queryKey: scanKeys.lists(),
    queryFn: scanApi.getScans,
    staleTime: 2 * 60 * 1000,
  })
}

export function useScan(id: string, enabled: boolean = true) {
  return useQuery({
    queryKey: scanKeys.detail(id),
    queryFn: () => scanApi.getScan(id),
    enabled: enabled && !!id,
    staleTime: 5 * 60 * 1000,
  })
}

export function useSubmitScan() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: scanApi.submitScan,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: scanKeys.lists() })
    },
  })
}

export function useProvideScanFeedback() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, feedback }: { id: string; feedback: string }) =>
      scanApi.provideFeedback(id, feedback),
    onSuccess: (updatedScan) => {
      queryClient.setQueryData(scanKeys.detail(updatedScan.id), updatedScan)
      queryClient.invalidateQueries({ queryKey: scanKeys.lists() })
    },
  })
}

// Critical Facts Hooks
export function useCriticalFacts() {
  return useQuery({
    queryKey: criticalKeys.list(),
    queryFn: criticalApi.getCriticalFacts,
    staleTime: 30 * 60 * 1000, // 30 minutes
  })
}

// Notification Hooks
export function useNotifications(filters: NotificationFilters = {}) {
  return useQuery({
    queryKey: notificationKeys.list(filters),
    queryFn: () => notificationApi.getNotifications(filters),
    keepPreviousData: true,
    staleTime: 1 * 60 * 1000, // 1 minute
  })
}

export function useUnreadNotificationCount() {
  return useQuery({
    queryKey: notificationKeys.unread(),
    queryFn: notificationApi.getUnreadCount,
    staleTime: 30 * 1000, // 30 seconds
    refetchInterval: 60 * 1000, // Refetch every minute
  })
}

export function useMarkNotificationAsRead() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: notificationApi.markAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: notificationKeys.lists() })
      queryClient.invalidateQueries({ queryKey: notificationKeys.unread() })
    },
  })
}

export function useMarkAllNotificationsAsRead() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: notificationApi.markAllAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: notificationKeys.all })
    },
  })
}

export function useCreateNotification() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: notificationApi.createNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: notificationKeys.lists() })
      queryClient.invalidateQueries({ queryKey: notificationKeys.unread() })
    },
  })
}

export function useDeleteNotification() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: notificationApi.deleteNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: notificationKeys.lists() })
      queryClient.invalidateQueries({ queryKey: notificationKeys.unread() })
    },
  })
}
