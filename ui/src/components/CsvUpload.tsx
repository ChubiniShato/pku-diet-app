import React, { useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useUploadProductsCsv } from '@/lib/api/products'
import { Button } from './Button'
import { toast } from '@/lib/toast/toast'

interface CsvUploadProps {
  isVisible: boolean
}

export const CsvUpload: React.FC<CsvUploadProps> = ({ isVisible }) => {
  const { t } = useTranslation()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  
  const uploadMutation = useUploadProductsCsv()

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (file) {
      // Validate file type
      if (!file.name.toLowerCase().endsWith('.csv')) {
        toast.error('Invalid File Type', 'Please select a CSV file')
        return
      }
      
      // Validate file size (max 10MB)
      if (file.size > 10 * 1024 * 1024) {
        toast.error('File Too Large', 'Please select a file smaller than 10MB')
        return
      }
      
      setSelectedFile(file)
    }
  }

  const handleUpload = async () => {
    if (!selectedFile) {
      toast.warning('No File Selected', 'Please select a CSV file first')
      return
    }

    try {
      const result = await uploadMutation.mutateAsync(selectedFile)
      toast.success('Upload Successful', result)
      setSelectedFile(null)
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    } catch (error) {
      // Error is handled by the global error handler
      console.error('Upload failed:', error)
    }
  }

  const handleCancel = () => {
    setSelectedFile(null)
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  if (!isVisible) return null

  return (
    <div className="bg-orange-50 border border-orange-200 rounded-lg p-4 mb-6">
      <div className="flex items-start">
        <div className="flex-shrink-0">
          <svg className="h-5 w-5 text-orange-400" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
          </svg>
        </div>
        
        <div className="ml-3 flex-1">
          <h3 className="text-sm font-medium text-orange-800">
            Admin Mode - CSV Upload
          </h3>
          <div className="mt-2 text-sm text-orange-700">
            <p className="mb-3">
              Upload a CSV file to bulk import products. The CSV should have columns for: 
              category, productName, phenylalanine, leucine, tyrosine, methionine, kilojoules, kilocalories, protein, carbohydrates, fats.
            </p>
            
            <div className="flex items-center space-x-3">
              <input
                ref={fileInputRef}
                type="file"
                accept=".csv"
                onChange={handleFileSelect}
                className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-orange-50 file:text-orange-700 hover:file:bg-orange-100"
              />
            </div>
            
            {selectedFile && (
              <div className="mt-3 p-3 bg-white rounded-md border border-orange-200">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {selectedFile.name}
                    </p>
                    <p className="text-xs text-gray-500">
                      {(selectedFile.size / 1024).toFixed(1)} KB
                    </p>
                  </div>
                  
                  <div className="flex space-x-2">
                    <Button
                      onClick={handleUpload}
                      disabled={uploadMutation.isLoading}
                      variant="primary"
                      size="sm"
                    >
                      {uploadMutation.isLoading ? 'Uploading...' : 'Upload'}
                    </Button>
                    <Button
                      onClick={handleCancel}
                      disabled={uploadMutation.isLoading}
                      variant="secondary"
                      size="sm"
                    >
                      Cancel
                    </Button>
                  </div>
                </div>
                
                {uploadMutation.isLoading && (
                  <div className="mt-2">
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div className="bg-orange-600 h-2 rounded-full animate-pulse" style={{ width: '45%' }}></div>
                    </div>
                    <p className="text-xs text-gray-500 mt-1">Processing CSV file...</p>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
