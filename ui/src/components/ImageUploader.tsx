import React, { useState, useCallback, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from '@/lib/toast/toast'

interface ImageFile {
  file: File
  id: string
  preview: string
  size: number
  error?: string
}

interface ImageUploaderProps {
  onImagesChange: (files: File[]) => void
  maxFiles?: number
  maxSizePerFile?: number // in MB
  acceptedTypes?: string[]
  disabled?: boolean
}

export const ImageUploader: React.FC<ImageUploaderProps> = ({
  onImagesChange,
  maxFiles = 5,
  maxSizePerFile = 10, // 10MB
  acceptedTypes = ['image/jpeg', 'image/png', 'image/webp'],
  disabled = false,
}) => {
  const { t } = useTranslation()
  const [images, setImages] = useState<ImageFile[]>([])
  const [isDragOver, setIsDragOver] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const maxSizeBytes = maxSizePerFile * 1024 * 1024

  const validateFile = (file: File): string | null => {
    if (!acceptedTypes.includes(file.type)) {
      return `Invalid file type. Accepted: ${acceptedTypes.join(', ')}`
    }
    if (file.size > maxSizeBytes) {
      return `File too large. Maximum size: ${maxSizePerFile}MB`
    }
    return null
  }

  const processFiles = useCallback((fileList: FileList) => {
    const newImages: ImageFile[] = []
    const validFiles: File[] = []

    Array.from(fileList).forEach((file) => {
      if (images.length + newImages.length >= maxFiles) {
        toast.error('Too Many Files', `Maximum ${maxFiles} files allowed`)
        return
      }

      const error = validateFile(file)
      const imageFile: ImageFile = {
        file,
        id: `${Date.now()}-${Math.random()}`,
        preview: URL.createObjectURL(file),
        size: file.size,
        error,
      }

      newImages.push(imageFile)
      if (!error) {
        validFiles.push(file)
      }
    })

    if (newImages.length > 0) {
      const updatedImages = [...images, ...newImages]
      setImages(updatedImages)
      
      // Only pass valid files to parent
      const allValidFiles = updatedImages
        .filter(img => !img.error)
        .map(img => img.file)
      
      onImagesChange(allValidFiles)
    }
  }, [images, maxFiles, onImagesChange, maxSizeBytes, acceptedTypes, maxSizePerFile])

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files
    if (files && files.length > 0) {
      processFiles(files)
    }
    // Reset input value to allow selecting the same file again
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault()
    if (!disabled) {
      setIsDragOver(true)
    }
  }

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(false)
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(false)
    
    if (disabled) return

    const files = e.dataTransfer.files
    if (files && files.length > 0) {
      processFiles(files)
    }
  }

  const removeImage = (id: string) => {
    const updatedImages = images.filter(img => {
      if (img.id === id) {
        URL.revokeObjectURL(img.preview)
        return false
      }
      return true
    })
    
    setImages(updatedImages)
    
    // Update valid files
    const validFiles = updatedImages
      .filter(img => !img.error)
      .map(img => img.file)
    
    onImagesChange(validFiles)
  }

  const clearAll = () => {
    images.forEach(img => URL.revokeObjectURL(img.preview))
    setImages([])
    onImagesChange([])
  }

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  const getFileTypeIcon = (type: string) => {
    if (type.startsWith('image/')) {
      return (
        <svg className="h-8 w-8 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
      )
    }
    return (
      <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
    )
  }

  return (
    <div className="space-y-4">
      {/* Upload Area */}
      <div
        className={`relative border-2 border-dashed rounded-lg p-6 transition-colors ${
          isDragOver
            ? 'border-blue-400 bg-blue-50'
            : disabled
            ? 'border-gray-200 bg-gray-50'
            : 'border-gray-300 hover:border-gray-400'
        }`}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
      >
        <div className="text-center">
          <div className="mx-auto h-12 w-12 text-gray-400">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
            </svg>
          </div>
          
          <div className="mt-4">
            <label htmlFor="file-upload" className={`cursor-pointer ${disabled ? 'cursor-not-allowed' : ''}`}>
              <span className="mt-2 block text-sm font-medium text-gray-900">
                {isDragOver ? 'Drop images here' : 'Upload label images'}
              </span>
              <span className="mt-1 block text-sm text-gray-600">
                Drag and drop or click to select
              </span>
              <input
                ref={fileInputRef}
                id="file-upload"
                name="file-upload"
                type="file"
                className="sr-only"
                multiple
                accept={acceptedTypes.join(',')}
                onChange={handleFileSelect}
                disabled={disabled}
              />
            </label>
          </div>
          
          <div className="mt-4 text-xs text-gray-500">
            <p>Supported: JPEG, PNG, WebP</p>
            <p>Max {maxFiles} files, {maxSizePerFile}MB each</p>
          </div>
        </div>
      </div>

      {/* File List */}
      {images.length > 0 && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-medium text-gray-900">
              Uploaded Images ({images.length}/{maxFiles})
            </h3>
            <button
              onClick={clearAll}
              className="text-sm text-red-600 hover:text-red-800"
              disabled={disabled}
            >
              Clear All
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {images.map((image) => (
              <div
                key={image.id}
                className={`relative bg-white rounded-lg border p-4 ${
                  image.error ? 'border-red-300 bg-red-50' : 'border-gray-200'
                }`}
              >
                <div className="flex items-start space-x-3">
                  {/* Thumbnail */}
                  <div className="flex-shrink-0">
                    {image.error ? (
                      <div className="h-16 w-16 bg-red-100 rounded-lg flex items-center justify-center">
                        <svg className="h-8 w-8 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                        </svg>
                      </div>
                    ) : (
                      <img
                        src={image.preview}
                        alt={image.file.name}
                        className="h-16 w-16 object-cover rounded-lg"
                      />
                    )}
                  </div>

                  {/* File Info */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between">
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-gray-900 truncate">
                          {image.file.name}
                        </p>
                        <p className="text-sm text-gray-500">
                          {formatFileSize(image.size)}
                        </p>
                        {image.error && (
                          <p className="text-sm text-red-600 mt-1">
                            {image.error}
                          </p>
                        )}
                      </div>
                      
                      <button
                        onClick={() => removeImage(image.id)}
                        className="ml-2 text-gray-400 hover:text-red-600"
                        disabled={disabled}
                      >
                        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                    </div>

                    {/* Success indicator for valid files */}
                    {!image.error && (
                      <div className="mt-2 flex items-center">
                        <svg className="h-4 w-4 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                        </svg>
                        <span className="ml-1 text-xs text-green-600">Ready for processing</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
