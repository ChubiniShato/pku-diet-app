import React, { useState, useRef, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from './Button'

interface CsvUploadProps {
  onFileSelect: (file: File) => void
  onUpload: (file: File) => void
  isUploading?: boolean
  uploadProgress?: number
  acceptedTypes?: string[]
  maxFileSize?: number // in MB
  className?: string
}

export const CsvUpload: React.FC<CsvUploadProps> = ({
  onFileSelect,
  onUpload,
  isUploading = false,
  uploadProgress = 0,
  acceptedTypes = ['.csv', 'text/csv'],
  maxFileSize = 10, // 10MB default
  className = ''
}) => {
  const { t } = useTranslation()
  const [isDragOver, setIsDragOver] = useState(false)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [error, setError] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const validateFile = useCallback((file: File): string | null => {
    // Check file type
    const isValidType = acceptedTypes.some(type => 
      file.type === type || file.name.toLowerCase().endsWith(type)
    )
    
    if (!isValidType) {
      return t('components.csvUpload.errors.invalidType')
    }

    // Check file size
    const fileSizeMB = file.size / (1024 * 1024)
    if (fileSizeMB > maxFileSize) {
      return t('components.csvUpload.errors.fileTooLarge', { maxSize: maxFileSize })
    }

    return null
  }, [acceptedTypes, maxFileSize, t])

  const handleFileSelect = useCallback((file: File) => {
    const validationError = validateFile(file)
    
    if (validationError) {
      setError(validationError)
      return
    }

    setError(null)
    setSelectedFile(file)
    onFileSelect(file)
  }, [validateFile, onFileSelect])

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(true)
  }, [])

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(false)
  }, [])

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(false)
    
    const files = Array.from(e.dataTransfer.files)
    if (files.length > 0) {
      handleFileSelect(files[0])
    }
  }, [handleFileSelect])

  const handleFileInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files
    if (files && files.length > 0) {
      handleFileSelect(files[0])
    }
  }, [handleFileSelect])

  const handleBrowseClick = useCallback(() => {
    fileInputRef.current?.click()
  }, [])

  const handleUpload = useCallback(() => {
    if (selectedFile) {
      onUpload(selectedFile)
    }
  }, [selectedFile, onUpload])

  const handleClear = useCallback(() => {
    setSelectedFile(null)
    setError(null)
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }, [])

  return (
    <div className={`space-y-4 ${className}`}>
      {/* Drag & Drop Zone */}
      <div
        className={`
          relative border-2 border-dashed rounded-lg p-8 text-center transition-colors
          ${isDragOver 
            ? 'border-blue-400 bg-blue-50' 
            : 'border-gray-300 hover:border-gray-400'
          }
          ${error ? 'border-red-300 bg-red-50' : ''}
        `}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
      >
        <div className="space-y-4">
          {/* Icon */}
          <div className="mx-auto w-12 h-12 text-gray-400">
            <svg
              className="w-full h-full"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
              />
            </svg>
          </div>

          {/* Text */}
          <div>
            <p className="text-lg font-medium text-gray-900">
              {t('components.csvUpload.dragDrop.title')}
            </p>
            <p className="text-sm text-gray-500">
              {t('components.csvUpload.dragDrop.subtitle')}
            </p>
          </div>

          {/* Browse Button */}
          <Button
            variant="secondary"
            onClick={handleBrowseClick}
            disabled={isUploading}
          >
            {t('components.csvUpload.browseFiles')}
          </Button>

          {/* File Input (Hidden) */}
          <input
            ref={fileInputRef}
            type="file"
            accept={acceptedTypes.join(',')}
            onChange={handleFileInputChange}
            className="hidden"
          />
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-md p-3">
          <p className="text-sm text-red-600">{error}</p>
        </div>
      )}

      {/* Selected File Info */}
      {selectedFile && (
        <div className="bg-green-50 border border-green-200 rounded-md p-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <svg className="w-5 h-5 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
              </svg>
              <span className="text-sm text-green-700">
                {selectedFile.name} ({(selectedFile.size / 1024 / 1024).toFixed(2)} MB)
              </span>
            </div>
            <Button
              variant="danger"
              size="sm"
              onClick={handleClear}
              disabled={isUploading}
            >
              {t('common.clear')}
            </Button>
          </div>
        </div>
      )}

      {/* Upload Button */}
      {selectedFile && (
        <Button
          variant="primary"
          onClick={handleUpload}
          disabled={isUploading}
          className="w-full"
        >
          {isUploading 
            ? t('components.csvUpload.uploading') 
            : t('components.csvUpload.upload')
          }
        </Button>
      )}

      {/* Progress Bar */}
      {isUploading && (
        <div className="space-y-2">
          <div className="flex justify-between text-sm text-gray-600">
            <span>{t('components.csvUpload.uploading')}</span>
            <span>{uploadProgress}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div
              className="bg-blue-600 h-2 rounded-full transition-all duration-300"
              style={{ width: `${uploadProgress}%` }}
            />
          </div>
        </div>
      )}
    </div>
  )
}
