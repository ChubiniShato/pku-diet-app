export interface LabelScanRequest {
  images: File[]
  region?: string
  barcode?: string
  patientId: string
}

export interface LabelScanResponse {
  id: string
  status: LabelScanStatus
  images: LabelScanImage[]
  ocrResults: OCRResult[]
  barcodeMatches: BarcodeMatch[]
  safetyFlags: SafetyFlag[]
  extractedProduct?: ExtractedProduct
  submissionEligible: boolean
  processingTimeMs: number
  createdAt: string
  updatedAt: string
}

export enum LabelScanStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  PARTIAL = 'PARTIAL'
}

export interface LabelScanImage {
  id: string
  filename: string
  originalSize: number
  processedSize: number
  mimeType: string
  width: number
  height: number
  thumbnailUrl: string
  fullUrl: string
  status: ImageProcessingStatus
  error?: string
}

export enum ImageProcessingStatus {
  UPLOADED = 'UPLOADED',
  PROCESSING = 'PROCESSING',
  PROCESSED = 'PROCESSED',
  FAILED = 'FAILED'
}

export interface OCRResult {
  imageId: string
  confidence: number
  text: string
  regions: OCRRegion[]
  language?: string
  processingTimeMs: number
}

export interface OCRRegion {
  boundingBox: BoundingBox
  text: string
  confidence: number
  type: OCRRegionType
}

export enum OCRRegionType {
  PRODUCT_NAME = 'PRODUCT_NAME',
  INGREDIENTS = 'INGREDIENTS',
  NUTRITION_TABLE = 'NUTRITION_TABLE',
  ALLERGENS = 'ALLERGENS',
  BARCODE = 'BARCODE',
  OTHER = 'OTHER'
}

export interface BoundingBox {
  x: number
  y: number
  width: number
  height: number
}

export interface BarcodeMatch {
  barcode: string
  confidence: number
  source: BarcodeSource
  product?: ExistingProduct
  verified: boolean
}

export enum BarcodeSource {
  DETECTED = 'DETECTED',
  PROVIDED = 'PROVIDED',
  OPENFOODFACTS = 'OPENFOODFACTS',
  INTERNAL = 'INTERNAL'
}

export interface ExistingProduct {
  id: string
  productName: string
  brand?: string
  category?: string
  phenylalaninePer100g?: number
  proteinPer100g?: number
  kilocaloriesPer100g?: number
  verified: boolean
  source: string
}

export interface SafetyFlag {
  type: SafetyFlagType
  severity: SafetySeverity
  message: string
  details: string
  affectedIngredients: string[]
  recommendation: string
}

export enum SafetyFlagType {
  HIGH_PHE_CONTENT = 'HIGH_PHE_CONTENT',
  FORBIDDEN_INGREDIENT = 'FORBIDDEN_INGREDIENT',
  ALLERGEN_WARNING = 'ALLERGEN_WARNING',
  ARTIFICIAL_SWEETENER = 'ARTIFICIAL_SWEETENER',
  PROCESSING_WARNING = 'PROCESSING_WARNING',
  INCOMPLETE_INFO = 'INCOMPLETE_INFO'
}

export enum SafetySeverity {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export interface ExtractedProduct {
  productName: string
  brand?: string
  category?: string
  ingredients: string[]
  nutritionFacts: NutritionFacts
  allergens: string[]
  servingSize?: ServingSize
  confidence: number
}

export interface NutritionFacts {
  per100g: NutritionValues
  perServing?: NutritionValues
}

export interface NutritionValues {
  energy?: number
  protein?: number
  carbohydrates?: number
  fat?: number
  fiber?: number
  sodium?: number
  sugar?: number
  phenylalanine?: number
}

export interface ServingSize {
  amount: number
  unit: string
  description?: string
}

export interface LabelScanSubmission {
  id: string
  scanId: string
  patientId: string
  submittedProduct: ExtractedProduct
  status: SubmissionStatus
  moderationNotes?: string
  submittedAt: string
  processedAt?: string
}

export enum SubmissionStatus {
  PENDING = 'PENDING',
  UNDER_REVIEW = 'UNDER_REVIEW',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  NEEDS_REVISION = 'NEEDS_REVISION'
}

export interface LabelScanFilters {
  status?: LabelScanStatus
  dateFrom?: string
  dateTo?: string
  hasBarcode?: boolean
  hasSafetyFlags?: boolean
  submissionEligible?: boolean
  page?: number
  size?: number
  sort?: string
}

export interface ProviderStatus {
  ocrEnabled: boolean
  barcodeEnabled: boolean
  safetyCheckEnabled: boolean
  submissionEnabled: boolean
  providers: {
    ocr: string[]
    barcode: string[]
    safety: string[]
  }
  fallbackMode: boolean
}
