// Menu-related types mirroring backend DTOs

export interface MenuDay {
  id: string
  date: string
  totalPhenylalanine: number
  totalProtein: number
  totalCalories: number
  pheLimit: number
  meals: Meal[]
  isGenerated: boolean
  createdAt: string
  updatedAt: string
}

export interface Meal {
  id: string
  type: MealType
  name: string
  plannedTime?: string
  actualTime?: string
  entries: MealEntry[]
  totalPhenylalanine: number
  totalProtein: number
  totalCalories: number
}

export interface MealEntry {
  id: string
  dishId?: string
  productId?: string
  dishName?: string
  productName?: string
  quantity: number
  unit: string
  phenylalanine: number
  protein: number
  calories: number
  consumed: boolean
  consumedAt?: string
}

export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK'

export interface MenuWeek {
  startDate: string
  endDate: string
  days: MenuDay[]
  totalPhenylalanine: number
  averagePhenylalanine: number
  pheLimit: number
}

export interface MenuGenerateRequest {
  date: string
  pheLimit: number
  preferences?: MenuPreferences
  emergencyMode?: boolean
  respectPantry?: boolean
}

export interface MenuPreferences {
  excludeCategories?: string[]
  preferredMealTimes?: { [key in MealType]?: string }
  maxMealsPerDay?: number
  minProtein?: number
  maxCalories?: number
}

export interface MealSlotCreateDto {
  mealType: MealType
  plannedTime?: string
  name?: string
}

export interface MealEntryCreateDto {
  dishId?: string
  productId?: string
  quantity: number
  unit: string
}

export interface MealEntryUpdateDto {
  quantity?: number
  unit?: string
  consumed?: boolean
  consumedAt?: string
}

export interface MenuFilters {
  startDate?: string
  endDate?: string
  isGenerated?: boolean
  page?: number
  size?: number
}

export interface MenuValidationResult {
  status: 'OK' | 'WARN' | 'BREACH'
  message?: string
  phenylalanineActual?: number
  phenylalanineTarget?: number
  proteinActual?: number
  proteinTarget?: number
  caloriesActual?: number
  caloriesTarget?: number
  warnings?: string[]
  errors?: string[]
}

export interface SnackSuggestion {
  itemId: string
  itemType: 'PRODUCT' | 'DISH'
  itemName: string
  quantity: number
  unit: string
  phenylalanine: number
  protein: number
  calories: number
  reason?: string
}
