// Dish-related types mirroring backend DTOs

export interface Dish {
  id: string
  name: string
  description?: string
  category?: string
  servings: number
  totalPhenylalanine: number
  totalProtein: number
  totalCalories: number
  ingredients: DishIngredient[]
  createdAt: string
  updatedAt: string
}

export interface DishIngredient {
  id: string
  productId: string
  productName: string
  quantity: number
  unit: string
  phenylalanine: number
  protein: number
  calories: number
}

export interface DishCreateDto {
  name: string
  description?: string
  category?: string
  servings: number
  ingredients: DishIngredientDto[]
}

export interface DishUpdateDto extends Partial<DishCreateDto> {}

export interface DishIngredientDto {
  productId: string
  quantity: number
  unit: string
}

export interface DishFilters {
  query?: string
  category?: string
  maxPhe?: number
  page?: number
  size?: number
}
