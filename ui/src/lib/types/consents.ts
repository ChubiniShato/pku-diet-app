export interface Consent {
  id: string
  patientId: string
  consentType: ConsentType
  granted: boolean
  grantedAt?: string
  revokedAt?: string
  createdAt: string
  updatedAt: string
}

export enum ConsentType {
  GLOBAL_SUBMISSION_OPTIN = 'GLOBAL_SUBMISSION_OPTIN',
  SHARE_WITH_DOCTOR = 'SHARE_WITH_DOCTOR',
}

export interface ConsentRequest {
  consentType: ConsentType
  granted: boolean
}

export interface ConsentResponse {
  consents: Consent[]
}

export interface ShareLink {
  id: string
  patientId: string
  token: string
  shareUrl: string
  scope: ShareScope
  ttlHours?: number
  oneTimeUse: boolean
  emailInvite?: string
  otpRequired: boolean
  qrCodeEnabled: boolean
  accessCount: number
  maxAccess?: number
  expiresAt?: string
  isActive: boolean
  createdAt: string
  updatedAt: string
  lastAccessedAt?: string
}

export enum ShareScope {
  CRITICAL_FACTS = 'CRITICAL_FACTS',
  DAY = 'DAY',
  WEEK = 'WEEK',
  RANGE = 'RANGE',
}

export interface ShareLinkCreateRequest {
  scope: ShareScope
  scopeData?: {
    date?: string
    startDate?: string
    endDate?: string
  }
  ttlHours?: number
  oneTimeUse?: boolean
  emailInvite?: string
  otpRequired?: boolean
  qrCodeEnabled?: boolean
  maxAccess?: number
}

export interface ShareLinkResponse {
  shareLink: ShareLink
  qrCodeDataUrl?: string
  otp?: string
}

export interface ShareAuditLog {
  id: string
  shareLinkId: string
  action: ShareAuditAction
  ipAddress?: string
  userAgent?: string
  accessedBy?: string
  details?: string
  createdAt: string
}

export enum ShareAuditAction {
  CREATED = 'CREATED',
  ACCESSED = 'ACCESSED',
  REVOKED = 'REVOKED',
  EXPIRED = 'EXPIRED',
  OTP_VERIFIED = 'OTP_VERIFIED',
  OTP_FAILED = 'OTP_FAILED',
}

export interface ShareFilters {
  isActive?: boolean
  scope?: ShareScope
  page?: number
  size?: number
  sort?: string
}
