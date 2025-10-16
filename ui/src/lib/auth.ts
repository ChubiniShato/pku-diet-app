import { TokenPayload } from '@/lib/types'

const TOKEN_KEY = 'pku-diet-token'
const USER_KEY = 'pku-diet-user'

/**
 * Token management utilities
 */
export const tokenUtils = {
  /**
   * Get token from localStorage
   */
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY)
  },

  /**
   * Set token in localStorage
   */
  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token)
  },

  /**
   * Remove token from localStorage
   */
  removeToken(): void {
    localStorage.removeItem(TOKEN_KEY)
  },

  /**
   * Decode JWT token payload (without verification)
   * Note: This is for client-side display only, server always validates
   */
  decodeToken(token: string): TokenPayload | null {
    try {
      const parts = token.split('.')
      if (parts.length !== 3) return null
      
      const payload = parts[1]
      const decoded = JSON.parse(atob(payload))
      return decoded as TokenPayload
    } catch (error) {
      console.error('Error decoding token:', error)
      return null
    }
  },

  /**
   * Check if token is expired (client-side only)
   */
  isTokenExpired(token: string): boolean {
    const payload = this.decodeToken(token)
    if (!payload) return true
    
    const currentTime = Math.floor(Date.now() / 1000)
    return payload.exp < currentTime
  },

  /**
   * Get time until token expiration in milliseconds
   */
  getTimeUntilExpiration(token: string): number {
    const payload = this.decodeToken(token)
    if (!payload) return 0
    
    const currentTime = Math.floor(Date.now() / 1000)
    return Math.max(0, (payload.exp - currentTime) * 1000)
  }
}

/**
 * User data management utilities
 */
export const userUtils = {
  /**
   * Get user from localStorage
   */
  getUser(): any | null {
    const userData = localStorage.getItem(USER_KEY)
    return userData ? JSON.parse(userData) : null
  },

  /**
   * Set user in localStorage
   */
  setUser(user: any): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  },

  /**
   * Remove user from localStorage
   */
  removeUser(): void {
    localStorage.removeItem(USER_KEY)
  }
}

/**
 * Clear all authentication data
 */
export const clearAuthData = (): void => {
  tokenUtils.removeToken()
  userUtils.removeUser()
}

/**
 * Check if user has required role
 */
export const hasRole = (userRole: string, requiredRole: string | string[]): boolean => {
  if (Array.isArray(requiredRole)) {
    return requiredRole.includes(userRole)
  }
  return userRole === requiredRole
}

/**
 * Role hierarchy for authorization
 */
const ROLE_HIERARCHY = {
  'ADMIN': 4,
  'HEALTHCARE_PROVIDER': 3,
  'PATIENT': 2,
  'USER': 1
}

/**
 * Check if user has minimum role level
 */
export const hasMinimumRole = (userRole: string, minimumRole: string): boolean => {
  const userLevel = ROLE_HIERARCHY[userRole as keyof typeof ROLE_HIERARCHY] || 0
  const minimumLevel = ROLE_HIERARCHY[minimumRole as keyof typeof ROLE_HIERARCHY] || 0
  return userLevel >= minimumLevel
}
