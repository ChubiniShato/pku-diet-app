import React, { useState, useCallback, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useDish, useCreateDish, useUpdateDish, useDeleteDish, useScaleDish, useSolveMass } from '@/lib/api/dishes'
import { ProductPicker } from '@/components/ProductPicker'
import { Button } from '@/components/Button'
import { toast } from '@/lib/toast/toast'
import type { Product, DishIngredientDto } from '@/lib/types'

interface LocalIngredient extends DishIngredientDto {
  id: string
  productName: string
  phenylalanine: number
  protein: number
  calories: number
}

export const DishDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const [isPickerOpen, setIsPickerOpen] = useState(false)
  const [ingredients, setIngredients] = useState<LocalIngredient[]>([])
  const [dishName, setDishName] = useState('')
  const [dishDescription, setDishDescription] = useState('')
  const [dishCategory, setDishCategory] = useState('')
  const [servings, setServings] = useState(1)
  const [viewMode, setViewMode] = useState<'per100g' | 'perServing'>('perServing')

  const { data: dish, isLoading, error } = useDish(id || '', !!id && id !== 'new')
  const createMutation = useCreateDish()
  const updateMutation = useUpdateDish()
  const deleteMutation = useDeleteDish()
  const scaleMutation = useScaleDish()
  const solveMassMutation = useSolveMass()

  const isNewDish = id === 'new'

  // Initialize form data when dish loads
  useEffect(() => {
    if (dish && !isNewDish) {
      setDishName(dish.name)
      setDishDescription(dish.description || '')
      setDishCategory(dish.category || '')
      setServings(dish.servings)
      setIngredients(dish.ingredients.map(ing => ({
        id: ing.id,
        productId: ing.productId,
        productName: ing.productName,
        quantity: ing.quantity,
        unit: ing.unit,
        phenylalanine: ing.phenylalanine,
        protein: ing.protein,
        calories: ing.calories,
      })))
    }
  }, [dish, isNewDish])

  // Calculate totals
  const calculateTotals = useCallback(() => {
    const totals = ingredients.reduce(
      (acc, ingredient) => ({
        phenylalanine: acc.phenylalanine + ingredient.phenylalanine,
        protein: acc.protein + ingredient.protein,
        calories: acc.calories + ingredient.calories,
      }),
      { phenylalanine: 0, protein: 0, calories: 0 }
    )

    return {
      total: totals,
      perServing: {
        phenylalanine: totals.phenylalanine / servings,
        protein: totals.protein / servings,
        calories: totals.calories / servings,
      },
    }
  }, [ingredients, servings])

  const totals = calculateTotals()

  const formatNutrient = (value: number): string => {
    return value.toFixed(1)
  }

  const getPheColor = (phe: number): string => {
    if (phe <= 50) return 'text-green-600'
    if (phe <= 100) return 'text-yellow-600'
    return 'text-red-600'
  }

  const handleAddProduct = (product: Product) => {
    const newIngredient: LocalIngredient = {
      id: `temp-${Date.now()}`,
      productId: product.id,
      productName: product.productName,
      quantity: 100,
      unit: 'g',
      phenylalanine: (product.phenylalanine || 0) * 1, // per 100g initially
      protein: (product.protein || 0) * 1,
      calories: (product.kilocalories || 0) * 1,
    }
    setIngredients(prev => [...prev, newIngredient])
  }

  const handleQuantityChange = (ingredientId: string, newQuantity: number) => {
    setIngredients(prev => prev.map(ing => {
      if (ing.id === ingredientId) {
        // For new dishes, we need to calculate from the original product values
        if (isNewDish) {
          // Find the base nutrition values (assuming we have them from when we added the product)
          const basePhePer100g = ing.phenylalanine / (ing.quantity / 100)
          const baseProteinPer100g = ing.protein / (ing.quantity / 100)
          const baseCaloriesPer100g = ing.calories / (ing.quantity / 100)
          
          return {
            ...ing,
            quantity: newQuantity,
            phenylalanine: basePhePer100g * (newQuantity / 100),
            protein: baseProteinPer100g * (newQuantity / 100),
            calories: baseCaloriesPer100g * (newQuantity / 100),
          }
        } else {
          // For existing dishes, use the dish ingredient data
          const originalIngredient = dish?.ingredients.find(di => di.productId === ing.productId)
          if (originalIngredient) {
            const multiplier = newQuantity / originalIngredient.quantity
            return {
              ...ing,
              quantity: newQuantity,
              phenylalanine: originalIngredient.phenylalanine * multiplier,
              protein: originalIngredient.protein * multiplier,
              calories: originalIngredient.calories * multiplier,
            }
          }
        }
      }
      return ing
    }))
  }

  const handleRemoveIngredient = (ingredientId: string) => {
    setIngredients(prev => prev.filter(ing => ing.id !== ingredientId))
  }

  const handleSave = async () => {
    if (!dishName.trim()) {
      toast.error('Validation Error', 'Dish name is required')
      return
    }

    if (ingredients.length === 0) {
      toast.error('Validation Error', 'At least one ingredient is required')
      return
    }

    try {
      const dishData = {
        name: dishName.trim(),
        description: dishDescription.trim() || undefined,
        category: dishCategory.trim() || undefined,
        servings,
        ingredients: ingredients.map(ing => ({
          productId: ing.productId,
          quantity: ing.quantity,
          unit: ing.unit,
        })),
      }

      if (isNewDish) {
        const newDish = await createMutation.mutateAsync(dishData)
        toast.success('Dish Created', 'New dish has been created successfully')
        navigate(`/dishes/${newDish.id}`)
      } else {
        await updateMutation.mutateAsync({ id: id!, data: dishData })
        toast.success('Dish Updated', 'Dish has been updated successfully')
      }
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handleDelete = async () => {
    if (!id || isNewDish) return

    if (window.confirm('Are you sure you want to delete this dish?')) {
      try {
        await deleteMutation.mutateAsync(id)
        toast.success('Dish Deleted', 'Dish has been deleted successfully')
        navigate('/dishes')
      } catch (error) {
        // Error handled by global error handler
      }
    }
  }

  const handleScale = async () => {
    if (!id || isNewDish) return

    const scaleFactor = prompt('Enter scale factor (e.g., 2 for double, 0.5 for half):')
    if (!scaleFactor) return

    const factor = parseFloat(scaleFactor)
    if (isNaN(factor) || factor <= 0) {
      toast.error('Invalid Input', 'Please enter a valid positive number')
      return
    }

    try {
      const updatedDish = await scaleMutation.mutateAsync({ id, scaleFactor: factor })
      toast.success('Dish Scaled', `Dish has been scaled by ${factor}x`)
      
      // Update local state with scaled dish
      setIngredients(updatedDish.ingredients.map(ing => ({
        id: ing.id,
        productId: ing.productId,
        productName: ing.productName,
        quantity: ing.quantity,
        unit: ing.unit,
        phenylalanine: ing.phenylalanine,
        protein: ing.protein,
        calories: ing.calories,
      })))
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handleSolveMass = async () => {
    if (!id || isNewDish) return

    const targetPhe = prompt('Enter target phenylalanine amount (mg):')
    if (!targetPhe) return

    const target = parseFloat(targetPhe)
    if (isNaN(target) || target <= 0) {
      toast.error('Invalid Input', 'Please enter a valid positive number')
      return
    }

    try {
      const updatedDish = await solveMassMutation.mutateAsync({ id, targetPhenylalanine: target })
      toast.success('Mass Solved', `Dish quantities adjusted to target ${target}mg PHE`)
      
      // Update local state with solved dish
      setIngredients(updatedDish.ingredients.map(ing => ({
        id: ing.id,
        productId: ing.productId,
        productName: ing.productName,
        quantity: ing.quantity,
        unit: ing.unit,
        phenylalanine: ing.phenylalanine,
        protein: ing.protein,
        calories: ing.calories,
      })))
    } catch (error) {
      // Error handled by global error handler
    }
  }

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <span className="ml-3 text-gray-600">{t('common.loading')}</span>
        </div>
      </div>
    )
  }

  if (error && !isNewDish) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <p className="text-red-800">Failed to load dish details</p>
          <Link to="/dishes" className="text-red-600 hover:text-red-800 underline mt-2 inline-block">
            Back to Dishes
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center justify-between">
          <div>
            <Link to="/dishes" className="text-blue-600 hover:text-blue-800 text-sm mb-2 inline-block">
              ← Back to Dishes
            </Link>
            <h1 className="text-3xl font-bold text-gray-900">
              {isNewDish ? 'Create New Dish' : dishName || 'Edit Dish'}
            </h1>
          </div>
          <div className="flex space-x-3">
            <Button 
              onClick={handleScale} 
              variant="secondary" 
              size="sm"
              disabled={scaleMutation.isLoading || isNewDish}
            >
              {scaleMutation.isLoading ? 'Scaling...' : 'Scale'}
            </Button>
            <Button 
              onClick={handleSolveMass} 
              variant="secondary" 
              size="sm"
              disabled={solveMassMutation.isLoading || isNewDish}
            >
              {solveMassMutation.isLoading ? 'Solving...' : 'Solve Mass'}
            </Button>
            {!isNewDish && (
              <Button 
                onClick={handleDelete} 
                variant="danger" 
                size="sm"
                disabled={deleteMutation.isLoading}
              >
                {deleteMutation.isLoading ? 'Deleting...' : 'Delete'}
              </Button>
            )}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Basic Info */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Basic Information</h2>
            <div className="space-y-4">
              <div>
                <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">
                  Dish Name *
                </label>
                <input
                  id="name"
                  type="text"
                  value={dishName}
                  onChange={(e) => setDishName(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Enter dish name"
                />
              </div>
              <div>
                <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
                  Description
                </label>
                <textarea
                  id="description"
                  value={dishDescription}
                  onChange={(e) => setDishDescription(e.target.value)}
                  rows={3}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Enter dish description"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-1">
                    Category
                  </label>
                  <input
                    id="category"
                    type="text"
                    value={dishCategory}
                    onChange={(e) => setDishCategory(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="e.g. Main Course"
                  />
                </div>
                <div>
                  <label htmlFor="servings" className="block text-sm font-medium text-gray-700 mb-1">
                    Servings *
                  </label>
                  <input
                    id="servings"
                    type="number"
                    min="1"
                    max="20"
                    value={servings}
                    onChange={(e) => setServings(Number(e.target.value))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Ingredients */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-900">Ingredients</h2>
              <Button onClick={() => setIsPickerOpen(true)} variant="primary" size="sm">
                Add Ingredient
              </Button>
            </div>

            {ingredients.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                <p>No ingredients added yet.</p>
                <p className="text-sm mt-1">Click "Add Ingredient" to get started.</p>
              </div>
            ) : (
              <div className="space-y-3">
                {ingredients.map((ingredient) => (
                  <div key={ingredient.id} className="flex items-center space-x-4 p-4 border border-gray-200 rounded-lg">
                    <div className="flex-1">
                      <h4 className="font-medium text-gray-900">{ingredient.productName}</h4>
                      <div className="flex items-center space-x-4 mt-1 text-sm text-gray-600">
                        <span>{formatNutrient(ingredient.phenylalanine)} mg PHE</span>
                        <span>{formatNutrient(ingredient.protein)} g protein</span>
                        <span>{formatNutrient(ingredient.calories)} kcal</span>
                      </div>
                    </div>
                    <div className="flex items-center space-x-2">
                      <input
                        type="number"
                        min="1"
                        max="2000"
                        value={ingredient.quantity}
                        onChange={(e) => handleQuantityChange(ingredient.id, Number(e.target.value))}
                        className="w-20 px-2 py-1 border border-gray-300 rounded text-sm"
                      />
                      <span className="text-sm text-gray-600">{ingredient.unit}</span>
                    </div>
                    <button
                      onClick={() => handleRemoveIngredient(ingredient.id)}
                      className="text-red-600 hover:text-red-800 p-1"
                    >
                      <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Sidebar - Nutrition Totals */}
        <div className="space-y-6">
          {/* View Mode Toggle */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div className="flex bg-gray-100 rounded-lg p-1">
              <button
                onClick={() => setViewMode('perServing')}
                className={`flex-1 px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                  viewMode === 'perServing'
                    ? 'bg-white text-blue-600 shadow-sm'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Per Serving
              </button>
              <button
                onClick={() => setViewMode('per100g')}
                className={`flex-1 px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                  viewMode === 'per100g'
                    ? 'bg-white text-blue-600 shadow-sm'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Total
              </button>
            </div>
          </div>

          {/* Nutrition Summary */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Nutrition {viewMode === 'perServing' ? 'Per Serving' : 'Total'}
            </h3>

            {/* PHE Highlight */}
            <div className="bg-gray-50 rounded-lg p-4 mb-4">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-gray-700">Phenylalanine</span>
                <div className="text-right">
                  <span className={`text-xl font-bold ${getPheColor(viewMode === 'perServing' ? totals.perServing.phenylalanine : totals.total.phenylalanine)}`}>
                    {formatNutrient(viewMode === 'perServing' ? totals.perServing.phenylalanine : totals.total.phenylalanine)} mg
                  </span>
                </div>
              </div>
            </div>

            {/* Other Nutrients */}
            <div className="space-y-3">
              <div className="flex justify-between items-center py-2 border-b border-gray-100">
                <span className="text-gray-600">Protein</span>
                <span className="font-medium text-gray-900">
                  {formatNutrient(viewMode === 'perServing' ? totals.perServing.protein : totals.total.protein)} g
                </span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-gray-100">
                <span className="text-gray-600">Calories</span>
                <span className="font-medium text-gray-900">
                  {formatNutrient(viewMode === 'perServing' ? totals.perServing.calories : totals.total.calories)} kcal
                </span>
              </div>
              <div className="flex justify-between items-center py-2">
                <span className="text-gray-600">Servings</span>
                <span className="font-medium text-gray-900">{servings}</span>
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <Button
              onClick={handleSave}
              variant="primary"
              className="w-full"
              disabled={updateMutation.isLoading || createMutation.isLoading}
            >
              {(updateMutation.isLoading || createMutation.isLoading) 
                ? 'Saving...' 
                : isNewDish ? 'Create Dish' : 'Save Changes'}
            </Button>
          </div>
        </div>
      </div>

      {/* Product Picker Modal */}
      <ProductPicker
        isOpen={isPickerOpen}
        onClose={() => setIsPickerOpen(false)}
        onSelect={handleAddProduct}
        excludeIds={ingredients.map(ing => ing.productId)}
      />
    </div>
  )
}
