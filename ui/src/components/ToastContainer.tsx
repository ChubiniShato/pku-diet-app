import React from 'react'
import { useToast } from '@/lib/toast/useToast'
import type { ToastType } from '@/lib/toast/toast'

const toastIcons: Record<ToastType, string> = {
  success: '✓',
  error: '✕',
  warning: '⚠',
  info: 'ℹ',
}

const toastStyles: Record<ToastType, string> = {
  success: 'bg-green-50 border-green-200 text-green-800',
  error: 'bg-red-50 border-red-200 text-red-800',
  warning: 'bg-yellow-50 border-yellow-200 text-yellow-800',
  info: 'bg-blue-50 border-blue-200 text-blue-800',
}

const iconStyles: Record<ToastType, string> = {
  success: 'text-green-400',
  error: 'text-red-400',
  warning: 'text-yellow-400',
  info: 'text-blue-400',
}

export const ToastContainer: React.FC = () => {
  const { toasts, dismiss } = useToast()

  if (toasts.length === 0) return null

  return (
    <div className="fixed top-4 right-4 z-50 space-y-2 max-w-sm w-full">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`
            ${toastStyles[toast.type]}
            border rounded-lg p-4 shadow-lg
            transform transition-all duration-300 ease-in-out
            animate-in slide-in-from-right-full
          `}
        >
          <div className="flex items-start">
            <div className={`flex-shrink-0 ${iconStyles[toast.type]}`}>
              <span className="text-lg font-bold">
                {toastIcons[toast.type]}
              </span>
            </div>
            
            <div className="ml-3 flex-1">
              <p className="text-sm font-medium">
                {toast.title}
              </p>
              {toast.message && (
                <p className="mt-1 text-sm opacity-90">
                  {toast.message}
                </p>
              )}
            </div>
            
            <button
              onClick={() => dismiss(toast.id)}
              className="flex-shrink-0 ml-3 text-gray-400 hover:text-gray-600 transition-colors"
            >
              <span className="sr-only">Close</span>
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>
      ))}
    </div>
  )
}
