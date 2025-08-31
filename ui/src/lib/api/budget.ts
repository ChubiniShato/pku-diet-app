import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from './client'
import type { Budget, BudgetUpdateRequest } from '@/lib/types'

// Query Keys
export const budgetKeys = {
  all: ['budget'] as const,
  details: () => [...budgetKeys.all, 'detail'] as const,
  detail: (patientId: string) => [...budgetKeys.details(), patientId] as const,
}

// API Functions
export const budgetApi = {
  async getBudget(patientId: string): Promise<Budget> {
    return apiClient.get<Budget>(`/api/v1/patients/${patientId}/budget`)
  },

  async updateBudget(patientId: string, data: BudgetUpdateRequest): Promise<Budget> {
    return apiClient.put<Budget>(`/api/v1/patients/${patientId}/budget`, data)
  },

  async createBudget(patientId: string, data: BudgetUpdateRequest): Promise<Budget> {
    return apiClient.post<Budget>(`/api/v1/patients/${patientId}/budget`, data)
  },
}

// Query Hooks
export function useBudget(patientId: string, enabled: boolean = true) {
  return useQuery({
    queryKey: budgetKeys.detail(patientId),
    queryFn: () => budgetApi.getBudget(patientId),
    enabled: enabled && !!patientId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useUpdateBudget() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ patientId, data }: { patientId: string; data: BudgetUpdateRequest }) =>
      budgetApi.updateBudget(patientId, data),
    onSuccess: (updatedBudget) => {
      // Update the budget cache
      queryClient.setQueryData(budgetKeys.detail(updatedBudget.patientId), updatedBudget)
    },
  })
}

export function useCreateBudget() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ patientId, data }: { patientId: string; data: BudgetUpdateRequest }) =>
      budgetApi.createBudget(patientId, data),
    onSuccess: (newBudget) => {
      // Set the new budget in cache
      queryClient.setQueryData(budgetKeys.detail(newBudget.patientId), newBudget)
    },
  })
}
