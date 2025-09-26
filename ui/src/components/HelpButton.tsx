import React from 'react'
import { useHelp } from '@/contexts/HelpContext'

interface HelpButtonProps {
  page: string
  position?: 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left'
  size?: 'small' | 'medium' | 'large'
  variant?: 'floating' | 'inline'
  className?: string
}

export const HelpButton: React.FC<HelpButtonProps> = ({
  page,
  position = 'top-right',
  size = 'medium',
  variant = 'floating',
  className = ''
}) => {
  const { showHelp } = useHelp()

  const handleClick = () => {
    showHelp(page)
  }

  const positionClasses = {
    'top-right': 'top-4 right-4',
    'top-left': 'top-4 left-4',
    'bottom-right': 'bottom-4 right-4',
    'bottom-left': 'bottom-4 left-4'
  }

  const sizeClasses = {
    small: 'w-8 h-8 text-sm',
    medium: 'w-10 h-10 text-base',
    large: 'w-12 h-12 text-lg'
  }

  const baseClasses = variant === 'floating' 
    ? `fixed z-50 ${positionClasses[position]} ${sizeClasses[size]}`
    : `inline-flex ${sizeClasses[size]}`

  return (
    <button
      onClick={handleClick}
      className={`
        ${baseClasses}
        bg-blue-600 hover:bg-blue-700 
        text-white rounded-full shadow-lg
        flex items-center justify-center
        transition-all duration-200
        hover:scale-110 active:scale-95
        focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2
        ${className}
      `}
      title="Click for help and instructions"
      aria-label="Show help for this page"
    >
      ?
    </button>
  )
}
