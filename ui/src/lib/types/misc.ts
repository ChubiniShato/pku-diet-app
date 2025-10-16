// Miscellaneous types for additional features

// Consents
export interface PatientConsent {
  id: string
  consentType: ConsentType
  granted: boolean
  grantedAt?: string
  revokedAt?: string
  version: string
  ipAddress?: string
  userAgent?: string
}

export type ConsentType = 'DATA_PROCESSING' | 'MARKETING' | 'ANALYTICS' | 'SHARING'

export interface ConsentRequest {
  consentType: ConsentType
  granted: boolean
}

// Share Links
export interface ShareLink {
  id: string
  title: string
  description?: string
  expiresAt?: string
  isActive: boolean
  accessCount: number
  maxAccesses?: number
  sharedData: SharedData
  createdAt: string
  updatedAt: string
}

export interface SharedData {
  type: ShareDataType
  menuDayId?: string
  dishId?: string
  recipeData?: unknown
}

export type ShareDataType = 'MENU_DAY' | 'DISH' | 'RECIPE'

export interface ShareLinkCreateDto {
  title: string
  description?: string
  expiresAt?: string
  maxAccesses?: number
  sharedData: SharedData
}

// Label Scan
export interface LabelScanSubmission {
  id: string
  status: ScanStatus
  imageUrl?: string
  extractedText?: string
  productMatch?: Product
  userFeedback?: string
  processedAt?: string
  createdAt: string
  updatedAt: string
}

export type ScanStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'FLAGGED'

export interface LabelScanRequest {
  image: File
  notes?: string
}

export interface LabelScanResponse {
  id: string
  status: ScanStatus
  message: string
}

// Critical Facts
export interface CriticalFact {
  id: string
  title: string
  content: string
  category: CriticalCategory
  priority: Priority
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export type CriticalCategory = 'EMERGENCY' | 'MEDICATION' | 'SYMPTOMS' | 'CONTACTS' | 'DIETARY'
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

// Notifications
export interface NotificationMessage {
  id: string
  title: string
  message: string
  type: NotificationType
  priority: Priority
  isRead: boolean
  data?: unknown
  scheduledFor?: string
  sentAt?: string
  createdAt: string
}

export type NotificationType = 'REMINDER' | 'ALERT' | 'INFO' | 'WARNING' | 'ERROR'

export interface NotificationCreateDto {
  title: string
  message: string
  type: NotificationType
  priority: Priority
  scheduledFor?: string
  data?: unknown
}

export interface NotificationFilters {
  type?: NotificationType
  priority?: Priority
  isRead?: boolean
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}
