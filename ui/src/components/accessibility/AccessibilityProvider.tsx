import React, { createContext, useContext, useEffect } from 'react'

interface AccessibilityContextType {
  announceMessage: (message: string, priority?: 'polite' | 'assertive') => void
  focusElement: (element: HTMLElement) => void
  trapFocus: (container: HTMLElement) => () => void
}

const AccessibilityContext = createContext<AccessibilityContextType | undefined>(undefined)

export const useAccessibility = () => {
  const context = useContext(AccessibilityContext)
  if (!context) {
    throw new Error('useAccessibility must be used within AccessibilityProvider')
  }
  return context
}

export const AccessibilityProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // Live region for screen reader announcements
  const announceMessage = (message: string, priority: 'polite' | 'assertive' = 'polite') => {
    const liveRegion = document.createElement('div')
    liveRegion.setAttribute('aria-live', priority)
    liveRegion.setAttribute('aria-atomic', 'true')
    liveRegion.style.position = 'absolute'
    liveRegion.style.left = '-10000px'
    liveRegion.style.width = '1px'
    liveRegion.style.height = '1px'
    liveRegion.style.overflow = 'hidden'

    document.body.appendChild(liveRegion)
    liveRegion.textContent = message

    setTimeout(() => {
      document.body.removeChild(liveRegion)
    }, 1000)
  }

  // Focus management
  const focusElement = (element: HTMLElement) => {
    element.focus()
    if (element.scrollIntoView) {
      element.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }

  // Focus trap for modals and dialogs
  const trapFocus = (container: HTMLElement) => {
    const focusableElements = container.querySelectorAll(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    )
    const firstElement = focusableElements[0] as HTMLElement
    const lastElement = focusableElements[focusableElements.length - 1] as HTMLElement

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Tab') {
        if (e.shiftKey) {
          if (document.activeElement === firstElement) {
            lastElement.focus()
            e.preventDefault()
          }
        } else {
          if (document.activeElement === lastElement) {
            firstElement.focus()
            e.preventDefault()
          }
        }
      }
    }

    container.addEventListener('keydown', handleKeyDown)
    firstElement?.focus()

    return () => {
      container.removeEventListener('keydown', handleKeyDown)
    }
  }

  return (
    <AccessibilityContext.Provider value={{ announceMessage, focusElement, trapFocus }}>
      {children}
    </AccessibilityContext.Provider>
  )
}
