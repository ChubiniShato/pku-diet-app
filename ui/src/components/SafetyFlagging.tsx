import React from 'react'
import { useTranslation } from 'react-i18next'
import type { SafetyFlag, SafetyFlagType, SafetySeverity } from '@/lib/types'

interface SafetyFlaggingProps {
  safetyFlags: SafetyFlag[]
}

export const SafetyFlagging: React.FC<SafetyFlaggingProps> = ({ safetyFlags }) => {
  const { t } = useTranslation()

  const getSeverityColor = (severity: SafetySeverity): string => {
    switch (severity) {
      case 'CRITICAL':
        return 'bg-red-50 border-red-200 text-red-800'
      case 'HIGH':
        return 'bg-orange-50 border-orange-200 text-orange-800'
      case 'MEDIUM':
        return 'bg-yellow-50 border-yellow-200 text-yellow-800'
      case 'LOW':
        return 'bg-blue-50 border-blue-200 text-blue-800'
      default:
        return 'bg-gray-50 border-gray-200 text-gray-800'
    }
  }

  const getSeverityIcon = (severity: SafetySeverity) => {
    switch (severity) {
      case 'CRITICAL':
        return (
          <svg className="h-5 w-5 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
        )
      case 'HIGH':
        return (
          <svg className="h-5 w-5 text-orange-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        )
      case 'MEDIUM':
        return (
          <svg className="h-5 w-5 text-yellow-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        )
      case 'LOW':
        return (
          <svg className="h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        )
      default:
        return (
          <svg className="h-5 w-5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        )
    }
  }

  const getFlagTypeIcon = (type: SafetyFlagType) => {
    switch (type) {
      case 'HIGH_PHE_CONTENT':
        return (
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
        )
      case 'FORBIDDEN_INGREDIENT':
        return (
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728L5.636 5.636m12.728 12.728L18.364 5.636M5.636 18.364l12.728-12.728" />
          </svg>
        )
      case 'ALLERGEN_WARNING':
        return (
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
        )
      case 'ARTIFICIAL_SWEETENER':
        return (
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 8.172V5L8 4z" />
          </svg>
        )
      case 'PROCESSING_WARNING':
        return (
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
        )
      case 'INCOMPLETE_INFO':
        return (
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        )
      default:
        return (
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        )
    }
  }

  const getFlagTypeLabel = (type: SafetyFlagType): string => {
    switch (type) {
      case 'HIGH_PHE_CONTENT':
        return 'High PHE Content'
      case 'FORBIDDEN_INGREDIENT':
        return 'Forbidden Ingredient'
      case 'ALLERGEN_WARNING':
        return 'Allergen Warning'
      case 'ARTIFICIAL_SWEETENER':
        return 'Artificial Sweetener'
      case 'PROCESSING_WARNING':
        return 'Processing Warning'
      case 'INCOMPLETE_INFO':
        return 'Incomplete Information'
      default:
        return type.replace('_', ' ').toLowerCase()
    }
  }

  const getSeverityLabel = (severity: SafetySeverity): string => {
    return severity.charAt(0) + severity.slice(1).toLowerCase()
  }

  // Group flags by severity for better organization
  const flagsBySeverity = safetyFlags.reduce((acc, flag) => {
    if (!acc[flag.severity]) {
      acc[flag.severity] = []
    }
    acc[flag.severity].push(flag)
    return acc
  }, {} as Record<SafetySeverity, SafetyFlag[]>)

  // Order severities by importance
  const severityOrder: SafetySeverity[] = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']
  const orderedSeverities = severityOrder.filter(severity => flagsBySeverity[severity])

  if (safetyFlags.length === 0) {
    return null
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center space-x-2">
        <svg className="h-5 w-5 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
        </svg>
        <h3 className="text-lg font-medium text-gray-900">
          Safety Alerts ({safetyFlags.length})
        </h3>
      </div>

      <div className="space-y-3">
        {orderedSeverities.map(severity => (
          <div key={severity} className="space-y-2">
            {flagsBySeverity[severity].map((flag, index) => (
              <div
                key={`${severity}-${index}`}
                className={`rounded-lg border p-4 ${getSeverityColor(flag.severity)}`}
              >
                <div className="flex items-start space-x-3">
                  <div className="flex-shrink-0 flex items-center space-x-2">
                    {getSeverityIcon(flag.severity)}
                    <div className="flex items-center space-x-1">
                      {getFlagTypeIcon(flag.type)}
                    </div>
                  </div>
                  
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center space-x-2 mb-2">
                      <h4 className="text-sm font-medium">
                        {getFlagTypeLabel(flag.type)}
                      </h4>
                      <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${
                        flag.severity === 'CRITICAL' ? 'bg-red-100 text-red-800' :
                        flag.severity === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                        flag.severity === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-blue-100 text-blue-800'
                      }`}>
                        {getSeverityLabel(flag.severity)}
                      </span>
                    </div>
                    
                    <p className="text-sm mb-2">
                      {flag.message}
                    </p>
                    
                    {flag.details && (
                      <p className="text-sm opacity-90 mb-2">
                        {flag.details}
                      </p>
                    )}

                    {flag.affectedIngredients.length > 0 && (
                      <div className="mb-2">
                        <span className="text-xs font-medium">Affected ingredients:</span>
                        <div className="flex flex-wrap gap-1 mt-1">
                          {flag.affectedIngredients.map((ingredient, ingredientIndex) => (
                            <span
                              key={ingredientIndex}
                              className="inline-flex items-center px-2 py-0.5 rounded-full text-xs bg-white bg-opacity-50"
                            >
                              {ingredient}
                            </span>
                          ))}
                        </div>
                      </div>
                    )}

                    {flag.recommendation && (
                      <div className={`mt-3 p-2 rounded ${
                        flag.severity === 'CRITICAL' ? 'bg-red-100 bg-opacity-50' :
                        flag.severity === 'HIGH' ? 'bg-orange-100 bg-opacity-50' :
                        flag.severity === 'MEDIUM' ? 'bg-yellow-100 bg-opacity-50' :
                        'bg-blue-100 bg-opacity-50'
                      }`}>
                        <div className="flex items-start space-x-2">
                          <svg className="h-4 w-4 mt-0.5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                          </svg>
                          <div>
                            <p className="text-xs font-medium">Recommendation:</p>
                            <p className="text-xs">{flag.recommendation}</p>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        ))}
      </div>

      {/* Summary */}
      <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
        <div className="flex items-center space-x-2 mb-2">
          <svg className="h-4 w-4 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          <h4 className="text-sm font-medium text-gray-900">Safety Summary</h4>
        </div>
        
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
          {severityOrder.map(severity => {
            const count = flagsBySeverity[severity]?.length || 0
            if (count === 0) return null
            
            return (
              <div key={severity} className="text-center">
                <div className={`text-lg font-bold ${
                  severity === 'CRITICAL' ? 'text-red-600' :
                  severity === 'HIGH' ? 'text-orange-600' :
                  severity === 'MEDIUM' ? 'text-yellow-600' :
                  'text-blue-600'
                }`}>
                  {count}
                </div>
                <div className="text-xs text-gray-600">
                  {getSeverityLabel(severity)}
                </div>
              </div>
            )
          })}
        </div>
        
        <p className="text-xs text-gray-600 mt-3">
          Please review all safety alerts before consuming this product. Consult your healthcare provider if you have questions about any flagged ingredients.
        </p>
      </div>
    </div>
  )
}
