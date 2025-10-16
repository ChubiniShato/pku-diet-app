const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// Token utilities for API client
const getAuthToken = (): string | null => {
  return localStorage.getItem('pku-diet-token')
}

export interface ApiError {
  message: string
  status: number
  timestamp?: string
  path?: string
  details?: unknown
}

export class ApiClientError extends Error {
  constructor(
    public status: number,
    message: string,
    public details?: unknown
  ) {
    super(message)
    this.name = 'ApiClientError'
  }
}

export interface RequestConfig extends RequestInit {
  timeout?: number
}

class ApiClient {
  private baseURL: string

  constructor(baseURL: string) {
    this.baseURL = baseURL
  }

  private async request<T>(
    endpoint: string,
    config: RequestConfig = {}
  ): Promise<T> {
    const { timeout = 10000, ...requestInit } = config
    const url = `${this.baseURL}${endpoint}`

    // Create abort controller for timeout and manual cancellation
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), timeout)

    // Merge abort signal with any existing signal
    const signal = requestInit.signal
      ? this.combineSignals(controller.signal, requestInit.signal)
      : controller.signal

    // Add authorization header if token exists
    const token = getAuthToken()
    const authHeaders = token ? { Authorization: `Bearer ${token}` } : {}

    // Detect FormData to avoid forcing JSON content type
    const isFormDataBody = typeof FormData !== 'undefined' && requestInit.body instanceof FormData

    const mergedHeaders: Record<string, any> = {
      ...(isFormDataBody ? {} : { 'Content-Type': 'application/json' }),
      ...authHeaders,
      ...(requestInit.headers as Record<string, any> | undefined),
    }
    // If header explicitly set to undefined or null, remove it
    if (mergedHeaders['Content-Type'] == null) {
      delete mergedHeaders['Content-Type']
    }

    const requestConfig: RequestInit = {
      headers: mergedHeaders as HeadersInit,
      signal,
      ...requestInit,
    }

    try {
      const response = await fetch(url, requestConfig)
      clearTimeout(timeoutId)

      // Handle non-JSON responses (like 204 No Content)
      const contentType = response.headers.get('content-type')
      const hasJsonContent = contentType?.includes('application/json')

      if (!response.ok) {
        let errorData: ApiError
        
        if (hasJsonContent) {
          try {
            errorData = await response.json()
          } catch {
            errorData = {
              message: `HTTP ${response.status}: ${response.statusText}`,
              status: response.status,
            }
          }
        } else {
          errorData = {
            message: `HTTP ${response.status}: ${response.statusText}`,
            status: response.status,
          }
        }

        // Handle authentication errors
        if (response.status === 401) {
          // Token expired or invalid, clear local storage
          localStorage.removeItem('pku-diet-token')
          localStorage.removeItem('pku-diet-user')
          
          // Dispatch custom event for auth context to handle
          window.dispatchEvent(new CustomEvent('auth:token-expired'))
        }

        throw new ApiClientError(
          response.status,
          errorData.message,
          errorData
        )
      }

      // Return empty object for successful responses without content
      if (!hasJsonContent || response.status === 204) {
        return {} as T
      }

      return await response.json()
    } catch (error) {
      clearTimeout(timeoutId)

      if (error instanceof ApiClientError) {
        throw error
      }

      if (error instanceof DOMException && error.name === 'AbortError') {
        throw new ApiClientError(0, 'Request was cancelled or timed out')
      }

      throw new ApiClientError(
        0,
        error instanceof Error ? error.message : 'Network error occurred'
      )
    }
  }

  private combineSignals(signal1: AbortSignal, signal2: AbortSignal): AbortSignal {
    const controller = new AbortController()

    const abort = () => controller.abort()
    
    if (signal1.aborted || signal2.aborted) {
      abort()
    } else {
      signal1.addEventListener('abort', abort, { once: true })
      signal2.addEventListener('abort', abort, { once: true })
    }

    return controller.signal
  }

  async get<T>(endpoint: string, config?: RequestConfig): Promise<T> {
    return this.request<T>(endpoint, { ...config, method: 'GET' })
  }

  async post<T>(endpoint: string, data?: unknown, config?: RequestConfig): Promise<T> {
    const bodyIsForm = typeof FormData !== 'undefined' && data instanceof FormData
    return this.request<T>(endpoint, {
      ...config,
      method: 'POST',
      body: bodyIsForm ? (data as BodyInit) : data ? JSON.stringify(data) : undefined,
    })
  }

  async put<T>(endpoint: string, data?: unknown, config?: RequestConfig): Promise<T> {
    const bodyIsForm = typeof FormData !== 'undefined' && data instanceof FormData
    return this.request<T>(endpoint, {
      ...config,
      method: 'PUT',
      body: bodyIsForm ? (data as BodyInit) : data ? JSON.stringify(data) : undefined,
    })
  }

  async patch<T>(endpoint: string, data?: unknown, config?: RequestConfig): Promise<T> {
    const bodyIsForm = typeof FormData !== 'undefined' && data instanceof FormData
    return this.request<T>(endpoint, {
      ...config,
      method: 'PATCH',
      body: bodyIsForm ? (data as BodyInit) : data ? JSON.stringify(data) : undefined,
    })
  }

  async delete<T>(endpoint: string, config?: RequestConfig): Promise<T> {
    return this.request<T>(endpoint, { ...config, method: 'DELETE' })
  }

  // Health check methods
  async checkHealth(): Promise<{ status: string }> {
    try {
      // Try actuator/health first
      return await this.get<{ status: string }>('/actuator/health')
    } catch {
      // Fallback to ping endpoint
      return await this.get<{ status: string }>('/api/v1/ping')
    }
  }
}

export const apiClient = new ApiClient(BASE_URL)
export { BASE_URL }
