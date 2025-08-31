import React, { createContext, useContext, useReducer, useEffect, useCallback } from 'react'
import { AuthState, AuthContextType, LoginRequest, User } from '@/lib/types'
import { tokenUtils, userUtils, clearAuthData } from '@/lib/auth'
import { authApi } from '@/lib/api/auth'

// Auth reducer actions
type AuthAction =
  | { type: 'LOGIN_START' }
  | { type: 'LOGIN_SUCCESS'; payload: { user: User; token: string } }
  | { type: 'LOGIN_FAILURE'; payload: string }
  | { type: 'LOGOUT' }
  | { type: 'REFRESH_START' }
  | { type: 'REFRESH_SUCCESS'; payload: { user: User; token: string } }
  | { type: 'REFRESH_FAILURE' }
  | { type: 'CLEAR_ERROR' }
  | { type: 'RESTORE_SESSION'; payload: { user: User; token: string } }

// Initial auth state
const initialState: AuthState = {
  user: null,
  token: null,
  isAuthenticated: false,
  isLoading: true, // Start with loading true for session restoration
  error: null
}

// Auth reducer
const authReducer = (state: AuthState, action: AuthAction): AuthState => {
  switch (action.type) {
    case 'LOGIN_START':
    case 'REFRESH_START':
      return {
        ...state,
        isLoading: true,
        error: null
      }

    case 'LOGIN_SUCCESS':
    case 'REFRESH_SUCCESS':
      return {
        ...state,
        user: action.payload.user,
        token: action.payload.token,
        isAuthenticated: true,
        isLoading: false,
        error: null
      }

    case 'LOGIN_FAILURE':
      return {
        ...state,
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: false,
        error: action.payload
      }

    case 'REFRESH_FAILURE':
    case 'LOGOUT':
      return {
        ...state,
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: false,
        error: null
      }

    case 'CLEAR_ERROR':
      return {
        ...state,
        error: null
      }

    case 'RESTORE_SESSION':
      return {
        ...state,
        user: action.payload.user,
        token: action.payload.token,
        isAuthenticated: true,
        isLoading: false,
        error: null
      }

    default:
      return state
  }
}

// Create context
const AuthContext = createContext<AuthContextType | null>(null)

// Auth provider component
export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, initialState)

  // Restore session on app start
  useEffect(() => {
    const restoreSession = async () => {
      const token = tokenUtils.getToken()
      const user = userUtils.getUser()

      if (token && user) {
        // Check if token is expired
        if (tokenUtils.isTokenExpired(token)) {
          // Token expired, try to refresh
          try {
            await refreshToken()
          } catch {
            // Refresh failed, clear session
            clearAuthData()
            dispatch({ type: 'LOGOUT' })
          }
        } else {
          // Token valid, restore session
          dispatch({ 
            type: 'RESTORE_SESSION', 
            payload: { user, token } 
          })
        }
      } else {
        // No session to restore
        dispatch({ type: 'LOGOUT' })
      }
    }

    restoreSession()
  }, [])

  // Login function
  const login = useCallback(async (credentials: LoginRequest) => {
    dispatch({ type: 'LOGIN_START' })

    try {
      const response = await authApi.login(credentials)
      const { token, user } = response

      // Store in localStorage
      tokenUtils.setToken(token)
      userUtils.setUser(user)

      dispatch({ 
        type: 'LOGIN_SUCCESS', 
        payload: { user, token } 
      })

      // Set up automatic token refresh
      scheduleTokenRefresh(token)

    } catch (error: any) {
      const message = error?.message || 'Login failed. Please try again.'
      dispatch({ type: 'LOGIN_FAILURE', payload: message })
      throw error
    }
  }, [])

  // Logout function
  const logout = useCallback(async () => {
    try {
      // Notify server about logout (optional)
      if (state.token) {
        await authApi.logout()
      }
    } catch (error) {
      // Ignore logout errors, clear local session anyway
      console.warn('Logout request failed:', error)
    } finally {
      // Clear local session
      clearAuthData()
      dispatch({ type: 'LOGOUT' })
      
      // Clear any scheduled refresh
      if (refreshTimeoutId) {
        clearTimeout(refreshTimeoutId)
        refreshTimeoutId = null
      }
    }
  }, [state.token])

  // Refresh token function
  const refreshToken = useCallback(async () => {
    const currentToken = tokenUtils.getToken()
    
    if (!currentToken) {
      dispatch({ type: 'REFRESH_FAILURE' })
      return
    }

    dispatch({ type: 'REFRESH_START' })

    try {
      const response = await authApi.refreshToken()
      const { token, user } = response

      // Update stored data
      tokenUtils.setToken(token)
      userUtils.setUser(user)

      dispatch({ 
        type: 'REFRESH_SUCCESS', 
        payload: { user, token } 
      })

      // Schedule next refresh
      scheduleTokenRefresh(token)

    } catch (error) {
      console.error('Token refresh failed:', error)
      clearAuthData()
      dispatch({ type: 'REFRESH_FAILURE' })
      throw error
    }
  }, [])

  // Clear error function
  const clearError = useCallback(() => {
    dispatch({ type: 'CLEAR_ERROR' })
  }, [])

  // Auto-refresh token management
  let refreshTimeoutId: NodeJS.Timeout | null = null

  const scheduleTokenRefresh = (token: string) => {
    if (refreshTimeoutId) {
      clearTimeout(refreshTimeoutId)
    }

    const timeUntilExpiration = tokenUtils.getTimeUntilExpiration(token)
    // Refresh 5 minutes before expiration
    const refreshTime = Math.max(0, timeUntilExpiration - 5 * 60 * 1000)

    if (refreshTime > 0) {
      refreshTimeoutId = setTimeout(() => {
        refreshToken().catch(() => {
          // Auto-refresh failed, user will need to login again
          logout()
        })
      }, refreshTime)
    }
  }

  // Set up token refresh on mount if we have a token
  useEffect(() => {
    if (state.token && state.isAuthenticated) {
      scheduleTokenRefresh(state.token)
    }

    return () => {
      if (refreshTimeoutId) {
        clearTimeout(refreshTimeoutId)
      }
    }
  }, [state.token, state.isAuthenticated])

  const contextValue: AuthContextType = {
    ...state,
    login,
    logout,
    refreshToken,
    clearError
  }

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  )
}

// Custom hook to use auth context
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
