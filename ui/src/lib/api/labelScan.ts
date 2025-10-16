import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from './client'
import type { 
  LabelScanRequest,
  LabelScanResponse,
  LabelScanSubmission,
  LabelScanFilters,
  ProviderStatus,
  SubmissionStatus,
  PageResponse
} from '@/lib/types'

// Query Keys
export const labelScanKeys = {
  all: ['labelScan'] as const,
  lists: () => [...labelScanKeys.all, 'list'] as const,
  list: (filters: LabelScanFilters) => [...labelScanKeys.lists(), filters] as const,
  details: () => [...labelScanKeys.all, 'detail'] as const,
  detail: (id: string) => [...labelScanKeys.details(), id] as const,
  status: () => [...labelScanKeys.all, 'status'] as const,
  submissions: () => [...labelScanKeys.all, 'submissions'] as const,
  submission: (id: string) => [...labelScanKeys.submissions(), id] as const,
}

// API Functions
export const labelScanApi = {
  async getProviderStatus(): Promise<ProviderStatus> {
    return apiClient.get<ProviderStatus>('/api/v1/label-scan/status')
  },

  async createLabelScan(request: Omit<LabelScanRequest, 'images'> & { images: File[] }): Promise<LabelScanResponse> {
    const formData = new FormData()
    
    // Add images
    request.images.forEach((file, index) => {
      formData.append(`images`, file)
    })
    
    // Add other fields
    formData.append('patientId', request.patientId)
    if (request.region) formData.append('region', request.region)
    if (request.barcode) formData.append('barcode', request.barcode)
    
    return apiClient.post<LabelScanResponse>('/api/v1/label-scan', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  async getLabelScan(id: string): Promise<LabelScanResponse> {
    return apiClient.get<LabelScanResponse>(`/api/v1/label-scan/${id}`)
  },

  async getLabelScans(patientId: string, filters: LabelScanFilters = {}): Promise<PageResponse<LabelScanResponse>> {
    const searchParams = new URLSearchParams()
    searchParams.append('patientId', patientId)
    
    if (filters.status) searchParams.append('status', filters.status)
    if (filters.dateFrom) searchParams.append('dateFrom', filters.dateFrom)
    if (filters.dateTo) searchParams.append('dateTo', filters.dateTo)
    if (filters.hasBarcode !== undefined) searchParams.append('hasBarcode', filters.hasBarcode.toString())
    if (filters.hasSafetyFlags !== undefined) searchParams.append('hasSafetyFlags', filters.hasSafetyFlags.toString())
    if (filters.submissionEligible !== undefined) searchParams.append('submissionEligible', filters.submissionEligible.toString())
    if (filters.page !== undefined) searchParams.append('page', filters.page.toString())
    if (filters.size !== undefined) searchParams.append('size', filters.size.toString())
    if (filters.sort) searchParams.append('sort', filters.sort)
    
    const queryString = searchParams.toString()
    const endpoint = `/api/v1/label-scan${queryString ? `?${queryString}` : ''}`
    
    return apiClient.get<PageResponse<LabelScanResponse>>(endpoint)
  },

  async submitToGlobalCatalog(scanId: string, patientId: string): Promise<LabelScanSubmission> {
    return apiClient.post<LabelScanSubmission>('/api/v1/submissions', {
      scanId,
      patientId,
    })
  },

  async getSubmission(id: string): Promise<LabelScanSubmission> {
    return apiClient.get<LabelScanSubmission>(`/api/v1/submissions/${id}`)
  },

  async getSubmissions(patientId: string, filters: { status?: SubmissionStatus; page?: number; size?: number } = {}): Promise<PageResponse<LabelScanSubmission>> {
    const searchParams = new URLSearchParams()
    searchParams.append('patientId', patientId)
    
    if (filters.status) searchParams.append('status', filters.status)
    if (filters.page !== undefined) searchParams.append('page', filters.page.toString())
    if (filters.size !== undefined) searchParams.append('size', filters.size.toString())
    
    const queryString = searchParams.toString()
    const endpoint = `/api/v1/submissions${queryString ? `?${queryString}` : ''}`
    
    return apiClient.get<PageResponse<LabelScanSubmission>>(endpoint)
  },
}

// Query Hooks
export function useProviderStatus(enabled: boolean = true) {
  return useQuery({
    queryKey: labelScanKeys.status(),
    queryFn: () => labelScanApi.getProviderStatus(),
    enabled,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useLabelScan(id: string, enabled: boolean = true) {
  return useQuery({
    queryKey: labelScanKeys.detail(id),
    queryFn: () => labelScanApi.getLabelScan(id),
    enabled: enabled && !!id,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useLabelScans(patientId: string, filters: LabelScanFilters = {}, enabled: boolean = true) {
  return useQuery({
    queryKey: labelScanKeys.list({ ...filters, patientId } as any),
    queryFn: () => labelScanApi.getLabelScans(patientId, filters),
    enabled: enabled && !!patientId,
    keepPreviousData: true,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useCreateLabelScan() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: Omit<LabelScanRequest, 'images'> & { images: File[] }) =>
      labelScanApi.createLabelScan(request),
    onSuccess: () => {
      // Invalidate label scan lists
      queryClient.invalidateQueries({ queryKey: labelScanKeys.lists() })
    },
  })
}

export function useSubmitToGlobalCatalog() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ scanId, patientId }: { scanId: string; patientId: string }) =>
      labelScanApi.submitToGlobalCatalog(scanId, patientId),
    onSuccess: () => {
      // Invalidate submissions lists
      queryClient.invalidateQueries({ queryKey: labelScanKeys.submissions() })
    },
  })
}

export function useSubmission(id: string, enabled: boolean = true) {
  return useQuery({
    queryKey: labelScanKeys.submission(id),
    queryFn: () => labelScanApi.getSubmission(id),
    enabled: enabled && !!id,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useSubmissions(patientId: string, filters: { status?: SubmissionStatus; page?: number; size?: number } = {}, enabled: boolean = true) {
  return useQuery({
    queryKey: [...labelScanKeys.submissions(), patientId, filters],
    queryFn: () => labelScanApi.getSubmissions(patientId, filters),
    enabled: enabled && !!patientId,
    keepPreviousData: true,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}
