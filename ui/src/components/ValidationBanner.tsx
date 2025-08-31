import React from 'react'
import { useTranslation } from 'react-i18next'
import type { MenuValidationResult } from '@/lib/types'

interface ValidationBannerProps {
  validation: MenuValidationResult
  onDismiss?: () => void
}

export const ValidationBanner: React.FC<ValidationBannerProps> = ({
  validation,
  onDismiss,
}) => {
  const { t } = useTranslation()

  const getStatusConfig = (status: string) => {
    switch (status.toUpperCase()) {
      case 'OK':
        return {
          bgColor: 'bg-green-50',
          borderColor: 'border-green-200',
          textColor: 'text-green-800',
          iconColor: 'text-green-400',
          icon: (
            <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
            </svg>
          ),
          title: 'Menu Validated Successfully',
        }
      case 'WARN':
        return {
          bgColor: 'bg-yellow-50',
          borderColor: 'border-yellow-200',
          textColor: 'text-yellow-800',
          iconColor: 'text-yellow-400',
          icon: (
            <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
            </svg>
          ),
          title: 'Menu Validation Warnings',
        }
      case 'BREACH':
        return {
          bgColor: 'bg-red-50',
          borderColor: 'border-red-200',
          textColor: 'text-red-800',
          iconColor: 'text-red-400',
          icon: (
            <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
          ),
          title: 'Menu Validation Failed',
        }
      default:
        return {
          bgColor: 'bg-gray-50',
          borderColor: 'border-gray-200',
          textColor: 'text-gray-800',
          iconColor: 'text-gray-400',
          icon: (
            <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
            </svg>
          ),
          title: 'Menu Validation',
        }
    }
  }

  const config = getStatusConfig(validation.status)

  const formatNutrient = (value: number, unit: string): string => {
    return `${value.toFixed(1)} ${unit}`
  }

  const formatDelta = (value: number, unit: string): string => {
    const prefix = value > 0 ? '+' : ''
    return `${prefix}${value.toFixed(1)} ${unit}`
  }

  return (
    <div className={`${config.bgColor} ${config.borderColor} border rounded-md p-4 mb-6`}>
      <div className="flex">
        <div className="flex-shrink-0">
          <div className={config.iconColor}>
            {config.icon}
          </div>
        </div>
        
        <div className="ml-3 flex-1">
          <div className="flex items-center justify-between">
            <h3 className={`text-sm font-medium ${config.textColor}`}>
              {config.title}
            </h3>
            {onDismiss && (
              <button
                onClick={onDismiss}
                className={`${config.textColor} hover:opacity-75 transition-opacity`}
              >
                <svg className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </button>
            )}
          </div>
          
          <div className={`mt-2 text-sm ${config.textColor}`}>
            {/* Main validation message */}
            {validation.message && (
              <p className="mb-3">{validation.message}</p>
            )}
            
            {/* Nutrition deltas */}
            {(validation.phenylalanineActual !== undefined || 
              validation.proteinActual !== undefined || 
              validation.caloriesActual !== undefined) && (
              <div className="space-y-2">
                <p className="font-medium">Nutrition Analysis:</p>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {/* Phenylalanine */}
                  {validation.phenylalanineActual !== undefined && (
                    <div className="flex justify-between items-center p-2 bg-white bg-opacity-50 rounded">
                      <span className="font-medium">PHE:</span>
                      <div className="text-right">
                        <div>{formatNutrient(validation.phenylalanineActual, 'mg')}</div>
                        {validation.phenylalanineTarget !== undefined && (
                          <div className="text-xs opacity-75">
                            Target: {formatNutrient(validation.phenylalanineTarget, 'mg')}
                            {validation.phenylalanineActual !== validation.phenylalanineTarget && (
                              <span className="ml-1">
                                ({formatDelta(validation.phenylalanineActual - validation.phenylalanineTarget, 'mg')})
                              </span>
                            )}
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                  
                  {/* Protein */}
                  {validation.proteinActual !== undefined && (
                    <div className="flex justify-between items-center p-2 bg-white bg-opacity-50 rounded">
                      <span className="font-medium">Protein:</span>
                      <div className="text-right">
                        <div>{formatNutrient(validation.proteinActual, 'g')}</div>
                        {validation.proteinTarget !== undefined && (
                          <div className="text-xs opacity-75">
                            Target: {formatNutrient(validation.proteinTarget, 'g')}
                            {validation.proteinActual !== validation.proteinTarget && (
                              <span className="ml-1">
                                ({formatDelta(validation.proteinActual - validation.proteinTarget, 'g')})
                              </span>
                            )}
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                  
                  {/* Calories */}
                  {validation.caloriesActual !== undefined && (
                    <div className="flex justify-between items-center p-2 bg-white bg-opacity-50 rounded">
                      <span className="font-medium">Calories:</span>
                      <div className="text-right">
                        <div>{formatNutrient(validation.caloriesActual, 'kcal')}</div>
                        {validation.caloriesTarget !== undefined && (
                          <div className="text-xs opacity-75">
                            Target: {formatNutrient(validation.caloriesTarget, 'kcal')}
                            {validation.caloriesActual !== validation.caloriesTarget && (
                              <span className="ml-1">
                                ({formatDelta(validation.caloriesActual - validation.caloriesTarget, 'kcal')})
                              </span>
                            )}
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}
            
            {/* Additional validation messages */}
            {validation.warnings && validation.warnings.length > 0 && (
              <div className="mt-3">
                <p className="font-medium">Warnings:</p>
                <ul className="list-disc list-inside mt-1 space-y-1">
                  {validation.warnings.map((warning, index) => (
                    <li key={index} className="text-sm">{warning}</li>
                  ))}
                </ul>
              </div>
            )}
            
            {validation.errors && validation.errors.length > 0 && (
              <div className="mt-3">
                <p className="font-medium">Errors:</p>
                <ul className="list-disc list-inside mt-1 space-y-1">
                  {validation.errors.map((error, index) => (
                    <li key={index} className="text-sm">{error}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
