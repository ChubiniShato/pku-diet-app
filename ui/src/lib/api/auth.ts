import { apiClient } from './client'
import { LoginRequest, LoginResponse, User } from '@/lib/types'

/**
 * Authentication API endpoints
 */
export const authApi = {
  /**
   * Login with username and password
   */
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    return apiClient.post<LoginResponse>('/api/v1/auth/login', credentials)
  },

  /**
   * Logout (invalidate current session)
   */
  async logout(): Promise<void> {
    return apiClient.post<void>('/api/v1/auth/logout')
  },

  /**
   * Refresh access token
   */
  async refreshToken(): Promise<LoginResponse> {
    return apiClient.post<LoginResponse>('/api/v1/auth/refresh')
  },

  /**
   * Get current user profile
   */
  async getProfile(): Promise<User> {
    return apiClient.get<User>('/api/v1/auth/profile')
  },

  /**
   * Validate current token
   */
  async validateToken(): Promise<{ valid: boolean; user?: User }> {
    return apiClient.get<{ valid: boolean; user?: User }>('/api/v1/auth/validate')
  },

  /**
   * Change password (requires current password)
   */
  async changePassword(data: {
    currentPassword: string
    newPassword: string
  }): Promise<void> {
    return apiClient.post<void>('/api/v1/auth/change-password', data)
  }
}
