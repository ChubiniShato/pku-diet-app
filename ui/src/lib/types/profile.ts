// Profile and norms-related types

export interface UserProfile {
  id: string
  email: string
  firstName: string
  lastName: string
  dateOfBirth?: string
  gender?: Gender
  height?: number
  weight?: number
  activityLevel?: ActivityLevel
  pheLimit: number
  proteinTarget?: number
  calorieTarget?: number
  preferences: ProfilePreferences
  createdAt: string
  updatedAt: string
}

export type Gender = 'MALE' | 'FEMALE' | 'OTHER'
export type ActivityLevel = 'SEDENTARY' | 'LIGHT' | 'MODERATE' | 'ACTIVE' | 'VERY_ACTIVE'

export interface ProfilePreferences {
  language: string
  timezone: string
  notifications: NotificationSettings
  dietary: DietaryPreferences
}

export interface NotificationSettings {
  email: boolean
  push: boolean
  sms: boolean
  reminderTimes: string[]
}

export interface DietaryPreferences {
  excludeCategories: string[]
  favoriteProducts: string[]
  allergies: string[]
  intolerances: string[]
}

export interface ProfileUpdateDto {
  firstName?: string
  lastName?: string
  dateOfBirth?: string
  gender?: Gender
  height?: number
  weight?: number
  activityLevel?: ActivityLevel
  pheLimit?: number
  proteinTarget?: number
  calorieTarget?: number
  preferences?: Partial<ProfilePreferences>
}

export interface NutritionalNorms {
  pheLimit: number
  proteinTarget: number
  calorieTarget: number
  calculatedAt: string
  basedOnProfile: {
    age: number
    gender: Gender
    height: number
    weight: number
    activityLevel: ActivityLevel
  }
}
