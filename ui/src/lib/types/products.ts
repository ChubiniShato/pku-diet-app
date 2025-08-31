// Product-related types mirroring backend DTOs

export interface Product {
  id: string
  productNumber?: number
  category: string
  productName: string
  phenylalanine?: number
  leucine?: number
  tyrosine?: number
  methionine?: number
  kilojoules?: number
  kilocalories?: number
  protein?: number
  carbohydrates?: number
  fats?: number
}

export interface ProductUpsertDto {
  productName: string
  category: string
  phenylalanine?: number
  leucine?: number
  tyrosine?: number
  methionine?: number
  kilojoules?: number
  kilocalories?: number
  protein?: number
  carbohydrates?: number
  fats?: number
}

export interface ProductFilters {
  query?: string
  category?: string
  maxPhe?: number
  page?: number
  size?: number
}

export interface ProductSearchParams extends ProductFilters {
  sort?: string
}
