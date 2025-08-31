export interface CriticalFact {
  id: string
  patientId: string
  date: string // ISO date string
  type: CriticalFactType
  delta: CriticalFactDelta
  context: CriticalFactContext
  severity: CriticalFactSeverity
  resolved: boolean
  resolvedAt?: string
  resolvedBy?: string
  notes?: string
  createdAt: string
  updatedAt: string
}

export enum CriticalFactType {
  PHE_BREACH = 'PHE_BREACH',
  PROTEIN_DEFICIENCY = 'PROTEIN_DEFICIENCY',
  CALORIE_EXCESS = 'CALORIE_EXCESS',
  CALORIE_DEFICIENCY = 'CALORIE_DEFICIENCY',
  MICRONUTRIENT_DEFICIENCY = 'MICRONUTRIENT_DEFICIENCY',
  MEDICATION_INTERACTION = 'MEDICATION_INTERACTION',
  ALLERGEN_EXPOSURE = 'ALLERGEN_EXPOSURE',
  FORBIDDEN_INGREDIENT = 'FORBIDDEN_INGREDIENT',
  SAFETY_WARNING = 'SAFETY_WARNING',
  DIETARY_VIOLATION = 'DIETARY_VIOLATION'
}

export interface CriticalFactDelta {
  nutrient: string
  expectedValue: number
  actualValue: number
  difference: number
  percentageChange: number
  unit: string
  threshold?: number
  thresholdType?: 'MAX' | 'MIN' | 'RANGE'
}

export interface CriticalFactContext {
  source: CriticalFactSource
  mealType?: string
  productName?: string
  dishName?: string
  quantity?: number
  unit?: string
  additionalInfo?: Record<string, any>
}

export enum CriticalFactSource {
  DAILY_VALIDATION = 'DAILY_VALIDATION',
  MEAL_ENTRY = 'MEAL_ENTRY',
  PRODUCT_SCAN = 'PRODUCT_SCAN',
  MANUAL_ENTRY = 'MANUAL_ENTRY',
  AUTOMATED_CHECK = 'AUTOMATED_CHECK',
  HEALTHCARE_PROVIDER = 'HEALTHCARE_PROVIDER',
  SYSTEM_ALERT = 'SYSTEM_ALERT'
}

export enum CriticalFactSeverity {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export interface CriticalFactFilters {
  patientId?: string
  type?: CriticalFactType[]
  severity?: CriticalFactSeverity[]
  source?: CriticalFactSource[]
  dateFrom?: string
  dateTo?: string
  resolved?: boolean
  page?: number
  size?: number
  sort?: string
  sortDirection?: 'ASC' | 'DESC'
}

export interface CriticalFactSummary {
  totalFacts: number
  unresolvedFacts: number
  factsByType: Record<CriticalFactType, number>
  factsBySeverity: Record<CriticalFactSeverity, number>
  recentFacts: CriticalFact[]
  trendData: CriticalFactTrend[]
}

export interface CriticalFactTrend {
  date: string
  count: number
  severity: CriticalFactSeverity
  type: CriticalFactType
}

export interface CriticalFactExportRequest {
  filters: CriticalFactFilters
  format: 'CSV' | 'PDF' | 'JSON'
  includeResolved?: boolean
  dateRange?: {
    start: string
    end: string
  }
}

export interface CriticalFactExportResponse {
  downloadUrl: string
  filename: string
  size: number
  recordCount: number
  expiresAt: string
}
