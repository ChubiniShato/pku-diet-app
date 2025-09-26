import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { CsvUpload } from '@/components/CsvUpload'
import { Modal } from '@/components/Modal'
import { Button } from '@/components/Button'
import { HelpButton } from '@/components/HelpButton'
import { Tooltip } from '@/components/Tooltip'
import { csvUploadApi } from '@/lib/api/csvUpload'
import { toast } from '@/lib/toast'

export const AdminPanel: React.FC = () => {
  const { t } = useTranslation()
  const [productsUploading, setProductsUploading] = useState(false)
  const [dishesUploading, setDishesUploading] = useState(false)
  const [productsProgress, setProductsProgress] = useState(0)
  const [dishesProgress, setDishesProgress] = useState(0)
  const [showProductsModal, setShowProductsModal] = useState(false)
  const [showDishesModal, setShowDishesModal] = useState(false)

  const handleProductsFileSelect = (file: File) => {
    console.log('Products file selected:', file.name)
  }

  const handleDishesFileSelect = (file: File) => {
    console.log('Dishes file selected:', file.name)
  }

  const handleDownloadProductsTemplate = async () => {
    try {
      const blob = await csvUploadApi.downloadProductsTemplate()
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'products_template.csv'
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
      toast.success('Products template downloaded!')
    } catch (error) {
      console.error('Template download error:', error)
      toast.error('Failed to download template')
    }
  }

  const handleDownloadDishesTemplate = async () => {
    try {
      const blob = await csvUploadApi.downloadDishesTemplate()
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'dishes_template.csv'
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
      toast.success('Dishes template downloaded!')
    } catch (error) {
      console.error('Template download error:', error)
      toast.error('Failed to download template')
    }
  }

  const handleDishesUpload = async (file: File) => {
    setDishesUploading(true)
    setDishesProgress(0)
    
    try {
      // Simulate progress
      for (let i = 0; i <= 90; i += 10) {
        setDishesProgress(i)
        await new Promise(resolve => setTimeout(resolve, 100))
      }
      
      // Real API call
      const response = await csvUploadApi.uploadDishesCsv(file)
      
      setDishesProgress(100)
      
      // Show success message
      toast.success(response.message || 'Dishes uploaded successfully!')
      
      // Close modal after successful upload
      setTimeout(() => {
        setShowDishesModal(false)
        setDishesProgress(0)
      }, 1000)
      
    } catch (error: any) {
      console.error('Dishes upload error:', error)
      toast.error(error.response?.data || 'Failed to upload dishes CSV')
    } finally {
      setDishesUploading(false)
    }
  }

  const handleProductsUpload = async (file: File) => {
    setProductsUploading(true)
    setProductsProgress(0)
    
    try {
      // Simulate progress
      for (let i = 0; i <= 90; i += 10) {
        setProductsProgress(i)
        await new Promise(resolve => setTimeout(resolve, 100))
      }
      
      // Real API call
      const response = await csvUploadApi.uploadProductsCsv(file)
      
      setProductsProgress(100)
      
      // Show success message
      toast.success(response.message || 'Products uploaded successfully!')
      
      // Close modal after successful upload
      setTimeout(() => {
        setShowProductsModal(false)
        setProductsProgress(0)
      }, 1000)
      
    } catch (error: any) {
      console.error('Products upload error:', error)
      toast.error(error.response?.data || 'Failed to upload products CSV')
    } finally {
      setProductsUploading(false)
    }
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          {t('pages.adminPanel.title')}
        </h1>
        <p className="mt-2 text-gray-600">
          {t('pages.adminPanel.subtitle')}
        </p>
      </div>

      {/* Help Button */}
      <HelpButton page="adminPanel" />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Pending Requests */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-4">
            {t('pages.adminPanel.pendingRequests')}
          </h2>
          <div className="space-y-4">
            <div className="flex items-center justify-between p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
              <div>
                <h3 className="text-sm font-medium text-yellow-800">
                  {t('pages.adminPanel.productRequests')}
                </h3>
                <p className="text-sm text-yellow-600">
                  {t('pages.adminPanel.noRequestsYet')}
                </p>
              </div>
              <Button
                variant="secondary"
                size="sm"
                disabled
              >
                {t('pages.adminPanel.viewAll')}
              </Button>
            </div>
            <div className="flex items-center justify-between p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
              <div>
                <h3 className="text-sm font-medium text-yellow-800">
                  {t('pages.adminPanel.dishRequests')}
                </h3>
                <p className="text-sm text-yellow-600">
                  {t('pages.adminPanel.noRequestsYet')}
                </p>
              </div>
              <Button
                variant="secondary"
                size="sm"
                disabled
              >
                {t('pages.adminPanel.viewAll')}
              </Button>
            </div>
          </div>
        </div>

        {/* CSV Upload Section */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-6">
            {t('pages.adminPanel.csvUpload')}
          </h2>
          <div className="space-y-4">
            <Tooltip content="Upload multiple products at once using CSV format">
              <Button
                variant="success"
                size="md"
                className="w-full"
                onClick={() => setShowProductsModal(true)}
              >
                {t('pages.adminPanel.uploadProductsCsv')}
              </Button>
            </Tooltip>
            <Tooltip content="Upload multiple dishes at once using CSV format">
              <Button
                variant="secondary"
                size="md"
                className="w-full"
                onClick={() => setShowDishesModal(true)}
              >
                {t('pages.adminPanel.uploadDishesCsv')}
              </Button>
            </Tooltip>
          </div>
        </div>
      </div>

      {/* Products CSV Upload Modal */}
      <Modal
        isOpen={showProductsModal}
        onClose={() => setShowProductsModal(false)}
        title={t('pages.adminPanel.uploadProductsCsv')}
        size="lg"
      >
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <p className="text-sm text-gray-600">
              Download a template to see the required CSV format
            </p>
            <Button
              variant="secondary"
              size="sm"
              onClick={handleDownloadProductsTemplate}
            >
              Download Template
            </Button>
          </div>
          <CsvUpload
            onFileSelect={handleProductsFileSelect}
            onUpload={handleProductsUpload}
            isUploading={productsUploading}
            uploadProgress={productsProgress}
            maxFileSize={50} // 50MB for products
          />
        </div>
      </Modal>

      {/* Dishes CSV Upload Modal */}
      <Modal
        isOpen={showDishesModal}
        onClose={() => setShowDishesModal(false)}
        title={t('pages.adminPanel.uploadDishesCsv')}
        size="lg"
      >
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <p className="text-sm text-gray-600">
              Download a template to see the required CSV format
            </p>
            <Button
              variant="secondary"
              size="sm"
              onClick={handleDownloadDishesTemplate}
            >
              Download Template
            </Button>
          </div>
          <CsvUpload
            onFileSelect={handleDishesFileSelect}
            onUpload={handleDishesUpload}
            isUploading={dishesUploading}
            uploadProgress={dishesProgress}
            maxFileSize={20} // 20MB for dishes
          />
        </div>
      </Modal>
    </div>
  )
}
