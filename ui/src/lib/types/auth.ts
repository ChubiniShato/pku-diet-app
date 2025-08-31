export interface User {
  id: string
  username: string
  email: string
  role: 'USER' | 'ADMIN' | 'PATIENT' | 'HEALTHCARE_PROVIDER'
  createdAt: string
  updatedAt: string
  lastLogin?: string
  isEnabled: boolean
  isAccountNonExpired: boolean
  isAccountNonLocked: boolean
  isCredentialsNonExpired: boolean
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  user: User
  expiresIn: number
}

export interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  isLoading: boolean
  error: string | null
}

export interface AuthContextType extends AuthState {
  login: (credentials: LoginRequest) => Promise<void>
  logout: () => void
  refreshToken: () => Promise<void>
  clearError: () => void
}

export interface TokenPayload {
  sub: string
  username: string
  role: string
  iat: number
  exp: number
}
