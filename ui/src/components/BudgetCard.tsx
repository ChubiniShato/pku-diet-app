import React, { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { useBudget, useUpdateBudget, useCreateBudget } from '@/lib/api/budget'
import { Button } from './Button'
import { toast } from '@/lib/toast/toast'
import type { BudgetUpdateRequest } from '@/lib/types'

interface BudgetCardProps {
  patientId: string
}

export const BudgetCard: React.FC<BudgetCardProps> = ({ patientId }) => {
  const { t } = useTranslation()
  const [isEditing, setIsEditing] = useState(false)
  const [formData, setFormData] = useState<BudgetUpdateRequest>({
    dailyCapAmount: undefined,
    weeklyCapAmount: undefined,
    monthlyCapAmount: undefined,
    currency: 'USD',
    isActive: true,
  })

  const { data: budget, isLoading } = useBudget(patientId)
  const updateMutation = useUpdateBudget()
  const createMutation = useCreateBudget()

  // Initialize form data when budget loads
  useEffect(() => {
    if (budget) {
      setFormData({
        dailyCapAmount: budget.dailyCapAmount,
        weeklyCapAmount: budget.weeklyCapAmount,
        monthlyCapAmount: budget.monthlyCapAmount,
        currency: budget.currency,
        isActive: budget.isActive,
      })
    }
  }, [budget])

  const handleSave = async () => {
    try {
      if (budget) {
        await updateMutation.mutateAsync({ patientId, data: formData })
        toast.success('Budget Updated', 'Budget settings saved successfully')
      } else {
        await createMutation.mutateAsync({ patientId, data: formData })
        toast.success('Budget Created', 'Budget settings created successfully')
      }
      setIsEditing(false)
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handleCancel = () => {
    if (budget) {
      setFormData({
        dailyCapAmount: budget.dailyCapAmount,
        weeklyCapAmount: budget.weeklyCapAmount,
        monthlyCapAmount: budget.monthlyCapAmount,
        currency: budget.currency,
        isActive: budget.isActive,
      })
    }
    setIsEditing(false)
  }

  const formatCurrency = (amount?: number, currency?: string): string => {
    if (!amount) return 'Not set'
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
    }).format(amount)
  }

  const calculateWeeklyFromDaily = (daily?: number): number | undefined => {
    return daily ? daily * 7 : undefined
  }

  const calculateMonthlyFromWeekly = (weekly?: number): number | undefined => {
    return weekly ? weekly * 4.33 : undefined
  }

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex items-center justify-center py-8">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
          <span className="ml-3 text-gray-600">{t('common.loading')}</span>
        </div>
      </div>
    )
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-lg font-semibold text-gray-900">Budget Settings</h2>
          <p className="text-sm text-gray-600">Set spending limits for meal planning</p>
        </div>
        
        <div className="flex items-center space-x-2">
          {budget && (
            <div className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
              budget.isActive 
                ? 'bg-green-100 text-green-800' 
                : 'bg-gray-100 text-gray-800'
            }`}>
              {budget.isActive ? 'Active' : 'Inactive'}
            </div>
          )}
          
          {!isEditing && (
            <Button
              onClick={() => setIsEditing(true)}
              variant="secondary"
              size="sm"
            >
              {budget ? 'Edit' : 'Set Budget'}
            </Button>
          )}
        </div>
      </div>

      {isEditing ? (
        <div className="space-y-6">
          {/* Budget Active Toggle */}
          <div className="flex items-center justify-between">
            <div>
              <label className="text-sm font-medium text-gray-700">
                Enable Budget Constraints
              </label>
              <p className="text-xs text-gray-500">
                When enabled, menu generation will respect budget limits
              </p>
            </div>
            <label className="flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={formData.isActive}
                onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
            </label>
          </div>

          {/* Currency Selection */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Currency
            </label>
            <select
              value={formData.currency}
              onChange={(e) => setFormData({ ...formData, currency: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="USD">USD ($)</option>
              <option value="EUR">EUR (€)</option>
              <option value="GBP">GBP (£)</option>
              <option value="GEL">GEL (₾)</option>
            </select>
          </div>

          {/* Budget Amounts */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Daily Budget
              </label>
              <input
                type="number"
                min="0"
                step="0.01"
                value={formData.dailyCapAmount || ''}
                onChange={(e) => setFormData({ 
                  ...formData, 
                  dailyCapAmount: e.target.value ? Number(e.target.value) : undefined 
                })}
                placeholder="0.00"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <p className="text-xs text-gray-500 mt-1">
                Maximum daily spending
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Weekly Budget
              </label>
              <input
                type="number"
                min="0"
                step="0.01"
                value={formData.weeklyCapAmount || ''}
                onChange={(e) => setFormData({ 
                  ...formData, 
                  weeklyCapAmount: e.target.value ? Number(e.target.value) : undefined 
                })}
                placeholder="0.00"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <p className="text-xs text-gray-500 mt-1">
                Maximum weekly spending
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Monthly Budget
              </label>
              <input
                type="number"
                min="0"
                step="0.01"
                value={formData.monthlyCapAmount || ''}
                onChange={(e) => setFormData({ 
                  ...formData, 
                  monthlyCapAmount: e.target.value ? Number(e.target.value) : undefined 
                })}
                placeholder="0.00"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <p className="text-xs text-gray-500 mt-1">
                Maximum monthly spending
              </p>
            </div>
          </div>

          {/* Budget Suggestions */}
          {formData.dailyCapAmount && (
            <div className="bg-blue-50 border border-blue-200 rounded-md p-3">
              <div className="flex">
                <div className="flex-shrink-0">
                  <svg className="h-4 w-4 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div className="ml-3">
                  <h4 className="text-sm font-medium text-blue-800">Budget Suggestions</h4>
                  <div className="mt-1 text-sm text-blue-700">
                    <p>
                      Based on your daily budget of {formatCurrency(formData.dailyCapAmount, formData.currency)}, 
                      consider setting:
                    </p>
                    <ul className="list-disc list-inside mt-1 space-y-1">
                      <li>
                        Weekly: {formatCurrency(calculateWeeklyFromDaily(formData.dailyCapAmount), formData.currency)}
                      </li>
                      <li>
                        Monthly: {formatCurrency(calculateMonthlyFromWeekly(calculateWeeklyFromDaily(formData.dailyCapAmount)), formData.currency)}
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex justify-end space-x-3">
            <Button
              onClick={handleCancel}
              variant="secondary"
            >
              Cancel
            </Button>
            <Button
              onClick={handleSave}
              variant="primary"
              disabled={updateMutation.isPending || createMutation.isPending}
            >
              {(updateMutation.isPending || createMutation.isPending) 
                ? 'Saving...' 
                : budget ? 'Update Budget' : 'Create Budget'}
            </Button>
          </div>
        </div>
      ) : (
        <div className="space-y-4">
          {budget ? (
            <>
              {/* Current Budget Display */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="text-center p-4 bg-gray-50 rounded-lg">
                  <div className="text-2xl font-bold text-gray-900">
                    {formatCurrency(budget.dailyCapAmount, budget.currency)}
                  </div>
                  <div className="text-sm text-gray-500">Daily Budget</div>
                </div>
                
                <div className="text-center p-4 bg-gray-50 rounded-lg">
                  <div className="text-2xl font-bold text-gray-900">
                    {formatCurrency(budget.weeklyCapAmount, budget.currency)}
                  </div>
                  <div className="text-sm text-gray-500">Weekly Budget</div>
                </div>
                
                <div className="text-center p-4 bg-gray-50 rounded-lg">
                  <div className="text-2xl font-bold text-gray-900">
                    {formatCurrency(budget.monthlyCapAmount, budget.currency)}
                  </div>
                  <div className="text-sm text-gray-500">Monthly Budget</div>
                </div>
              </div>

              {/* Budget Impact */}
              <div className="bg-green-50 border border-green-200 rounded-md p-3">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <svg className="h-4 w-4 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <div className="ml-3">
                    <h4 className="text-sm font-medium text-green-800">Budget Active</h4>
                    <div className="mt-1 text-sm text-green-700">
                      <p>
                        Menu generation will respect your budget constraints and prioritize cost-effective ingredients.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </>
          ) : (
            <div className="text-center py-8">
              <div className="mx-auto h-12 w-12 text-gray-400">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                </svg>
              </div>
              <h3 className="mt-2 text-sm font-medium text-gray-900">No budget set</h3>
              <p className="mt-1 text-sm text-gray-500">
                Set budget limits to help control meal planning costs.
              </p>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
