export interface PantryItem {
  id: string
  productId: string
  productName: string
  category?: string
  quantity: number
  unit: 'g' | 'ml' | 'piece' | 'kg' | 'l' | 'cup' | 'tbsp' | 'tsp'
  expiryDate?: string
  unitPrice?: number
  currency?: string
  purchaseDate?: string
  location?: string // e.g., "Fridge", "Freezer", "Pantry"
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface PantryFilters {
  query?: string
  category?: string
  expiringWithinDays?: number
  location?: string
  page?: number
  size?: number
  sort?: string
}

export interface Budget {
  id: string
  patientId: string
  dailyCapAmount?: number
  weeklyCapAmount?: number
  monthlyCapAmount?: number
  currency: string
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export interface BudgetUpdateRequest {
  dailyCapAmount?: number
  weeklyCapAmount?: number
  monthlyCapAmount?: number
  currency?: string
  isActive?: boolean
}