import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from './client'
import type { 
  UserProfile, 
  ProfileUpdateDto, 
  NutritionalNorms 
} from '@/lib/types'

// Query Keys
export const profileKeys = {
  all: ['profile'] as const,
  profile: () => [...profileKeys.all, 'current'] as const,
  norms: () => [...profileKeys.all, 'norms'] as const,
}

// API Functions
export const profileApi = {
  async getProfile(): Promise<UserProfile> {
    return apiClient.get<UserProfile>('/api/v1/profile')
  },

  async updateProfile(data: ProfileUpdateDto): Promise<UserProfile> {
    return apiClient.put<UserProfile>('/api/v1/profile', data)
  },

  async getNutritionalNorms(): Promise<NutritionalNorms> {
    return apiClient.get<NutritionalNorms>('/api/v1/profile/norms')
  },

  async recalculateNorms(): Promise<NutritionalNorms> {
    return apiClient.post<NutritionalNorms>('/api/v1/profile/norms/recalculate')
  },
}

// Query Hooks
export function useProfile() {
  return useQuery({
    queryKey: profileKeys.profile(),
    queryFn: profileApi.getProfile,
    staleTime: 10 * 60 * 1000, // 10 minutes
  })
}

export function useNorms() {
  return useQuery({
    queryKey: profileKeys.norms(),
    queryFn: profileApi.getNutritionalNorms,
    staleTime: 15 * 60 * 1000, // 15 minutes
  })
}

// Mutation Hooks
export function useUpdateProfile() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: profileApi.updateProfile,
    onSuccess: (updatedProfile) => {
      // Update profile in cache
      queryClient.setQueryData(profileKeys.profile(), updatedProfile)
      // Invalidate norms as they might have changed
      queryClient.invalidateQueries({ queryKey: profileKeys.norms() })
    },
  })
}

export function useRecalculateNorms() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: profileApi.recalculateNorms,
    onSuccess: (newNorms) => {
      // Update norms in cache
      queryClient.setQueryData(profileKeys.norms(), newNorms)
    },
  })
}
