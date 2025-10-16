import { apiClient } from './client'

export interface CsvUploadResponse {
  message: string
  success: boolean
  count?: number
  errors?: string[]
}

export const csvUploadApi = {
  // Upload products CSV
  async uploadProductsCsv(file: File): Promise<CsvUploadResponse> {
    const formData = new FormData()
    formData.append('file', file)

    const response = await apiClient.post('/products/upload-csv', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    return {
      message: response.data,
      success: true,
    }
  },

  // Upload dishes CSV
  async uploadDishesCsv(file: File): Promise<CsvUploadResponse> {
    const formData = new FormData()
    formData.append('file', file)

    const response = await apiClient.post('/dishes/upload-csv', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    return {
      message: response.data,
      success: true,
    }
  },

  // Upload multi-language dishes CSV
  async uploadMultiLanguageDishesCsv(file: File): Promise<CsvUploadResponse> {
    const formData = new FormData()
    formData.append('file', file)

    const response = await apiClient.post('/dishes/upload-multilang-csv', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    return {
      message: response.data,
      success: true,
    }
  },

  // Download CSV templates
  async downloadProductsTemplate(): Promise<Blob> {
    const response = await apiClient.get('/products/csv-template', {
      responseType: 'blob',
    })
    return response.data
  },

  async downloadDishesTemplate(): Promise<Blob> {
    const response = await apiClient.get('/dishes/csv-template', {
      responseType: 'blob',
    })
    return response.data
  },

  async downloadMultiLanguageDishesTemplate(): Promise<Blob> {
    const response = await apiClient.get('/dishes/multilang-csv-template', {
      responseType: 'blob',
    })
    return response.data
  },
}
