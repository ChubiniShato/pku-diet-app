import React from 'react'

interface SkeletonLoaderProps {
  variant?: 'text' | 'rectangular' | 'circular'
  width?: string | number
  height?: string | number
  className?: string
  animation?: 'pulse' | 'wave' | 'none'
}

export const SkeletonLoader: React.FC<SkeletonLoaderProps> = ({
  variant = 'text',
  width,
  height,
  className = '',
  animation = 'pulse',
}) => {
  const baseClasses = 'bg-gray-200 dark:bg-gray-700'

  const animationClasses = {
    pulse: 'animate-pulse',
    wave: 'animate-pulse',
    none: '',
  }

  const variantClasses = {
    text: 'h-4 rounded',
    rectangular: 'rounded',
    circular: 'rounded-full',
  }

  const style: React.CSSProperties = {}
  if (width) style.width = typeof width === 'number' ? `${width}px` : width
  if (height) style.height = typeof height === 'number' ? `${height}px` : height

  return (
    <div
      className={`
        ${baseClasses}
        ${variantClasses[variant]}
        ${animationClasses[animation]}
        ${className}
      `}
      style={style}
      role="presentation"
      aria-hidden="true"
    />
  )
}

interface TableSkeletonProps {
  rows?: number
  columns?: number
  className?: string
}

export const TableSkeleton: React.FC<TableSkeletonProps> = ({
  rows = 5,
  columns = 4,
  className = '',
}) => {
  return (
    <div className={`space-y-4 ${className}`} role="presentation" aria-hidden="true">
      {/* Table Header */}
      <div className="flex space-x-4">
        {Array.from({ length: columns }).map((_, i) => (
          <SkeletonLoader
            key={`header-${i}`}
            variant="rectangular"
            height={40}
            className="flex-1"
          />
        ))}
      </div>

      {/* Table Rows */}
      {Array.from({ length: rows }).map((_, rowIndex) => (
        <div key={`row-${rowIndex}`} className="flex space-x-4">
          {Array.from({ length: columns }).map((_, colIndex) => (
            <SkeletonLoader
              key={`cell-${rowIndex}-${colIndex}`}
              variant="rectangular"
              height={32}
              className="flex-1"
            />
          ))}
        </div>
      ))}
    </div>
  )
}

interface CardSkeletonProps {
  cards?: number
  className?: string
}

export const CardSkeleton: React.FC<CardSkeletonProps> = ({
  cards = 3,
  className = '',
}) => {
  return (
    <div className={`grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 ${className}`}>
      {Array.from({ length: cards }).map((_, i) => (
        <div key={i} className="bg-white rounded-lg shadow p-6 space-y-4">
          <SkeletonLoader variant="rectangular" height={24} width="60%" />
          <SkeletonLoader variant="rectangular" height={16} width="80%" />
          <SkeletonLoader variant="rectangular" height={16} width="40%" />
        </div>
      ))}
    </div>
  )
}

interface FormSkeletonProps {
  fields?: number
  className?: string
}

export const FormSkeleton: React.FC<FormSkeletonProps> = ({
  fields = 4,
  className = '',
}) => {
  return (
    <div className={`space-y-6 ${className}`} role="presentation" aria-hidden="true">
      {Array.from({ length: fields }).map((_, i) => (
        <div key={i} className="space-y-2">
          <SkeletonLoader variant="rectangular" height={16} width="25%" />
          <SkeletonLoader variant="rectangular" height={40} width="100%" />
        </div>
      ))}
    </div>
  )
}
