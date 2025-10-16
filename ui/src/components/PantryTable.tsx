import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { usePantryItems, useCreatePantryItem, useUpdatePantryItem, useDeletePantryItem } from '@/lib/api/pantry'
import { useProducts } from '@/lib/api/products'
import { Button } from './Button'
import { ProductPicker } from './ProductPicker'
import { toast } from '@/lib/toast/toast'
import type { PantryItem, PantryFilters, Product } from '@/lib/types'

interface PantryTableProps {
  filters?: PantryFilters
  onFiltersChange?: (filters: PantryFilters) => void
}

export const PantryTable: React.FC<PantryTableProps> = ({
  filters = {},
  onFiltersChange,
}) => {
  const { t } = useTranslation()
  const [isProductPickerOpen, setIsProductPickerOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<PantryItem | null>(null)
  const [newItem, setNewItem] = useState<Partial<PantryItem>>({})

  const { data: pantryResponse, isLoading, error } = usePantryItems(filters)
  const createMutation = useCreatePantryItem()
  const updateMutation = useUpdatePantryItem()
  const deleteMutation = useDeletePantryItem()

  const handleAddProduct = (product: Product) => {
    setNewItem({
      productId: product.id,
      productName: product.productName,
      category: product.category,
      quantity: 1,
      unit: 'piece',
      currency: 'USD',
    })
    setIsProductPickerOpen(false)
  }

  const handleCreateItem = async () => {
    if (!newItem.productId || !newItem.productName || !newItem.quantity) {
      toast.error('Validation Error', 'Product, quantity are required')
      return
    }

    try {
      await createMutation.mutateAsync({
        productId: newItem.productId,
        productName: newItem.productName,
        category: newItem.category,
        quantity: newItem.quantity,
        unit: newItem.unit || 'piece',
        expiryDate: newItem.expiryDate,
        unitPrice: newItem.unitPrice,
        currency: newItem.currency || 'USD',
        location: newItem.location,
        notes: newItem.notes,
      })
      
      toast.success('Item Added', 'Pantry item added successfully')
      setNewItem({})
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handleUpdateItem = async (item: PantryItem) => {
    try {
      await updateMutation.mutateAsync({ id: item.id, data: item })
      toast.success('Item Updated', 'Pantry item updated successfully')
      setEditingItem(null)
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const handleDeleteItem = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this pantry item?')) {
      try {
        await deleteMutation.mutateAsync(id)
        toast.success('Item Deleted', 'Pantry item removed from pantry')
      } catch (error) {
        // Error handled by global error handler
      }
    }
  }

  const formatDate = (dateString?: string): string => {
    if (!dateString) return ''
    return new Date(dateString).toLocaleDateString()
  }

  const formatCurrency = (amount?: number, currency?: string): string => {
    if (!amount) return ''
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
    }).format(amount)
  }

  const isExpiringSoon = (expiryDate?: string): boolean => {
    if (!expiryDate) return false
    const today = new Date()
    const expiry = new Date(expiryDate)
    const daysUntilExpiry = Math.ceil((expiry.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
    return daysUntilExpiry <= 7 && daysUntilExpiry >= 0
  }

  const isExpired = (expiryDate?: string): boolean => {
    if (!expiryDate) return false
    const today = new Date()
    const expiry = new Date(expiryDate)
    return expiry < today
  }

  const getExpiryRowClass = (expiryDate?: string): string => {
    if (isExpired(expiryDate)) return 'bg-red-50 border-red-200'
    if (isExpiringSoon(expiryDate)) return 'bg-yellow-50 border-yellow-200'
    return 'bg-white border-gray-200'
  }

  const EditableCell = ({ 
    value, 
    type = 'text', 
    onChange, 
    options 
  }: { 
    value: any
    type?: 'text' | 'number' | 'date' | 'select'
    onChange: (value: any) => void
    options?: string[]
  }) => {
    if (type === 'select' && options) {
      return (
        <select
          value={value || ''}
          onChange={(e) => onChange(e.target.value)}
          className="w-full px-2 py-1 border border-gray-300 rounded text-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
        >
          <option value="">Select...</option>
          {options.map(option => (
            <option key={option} value={option}>{option}</option>
          ))}
        </select>
      )
    }

    return (
      <input
        type={type}
        value={value || ''}
        onChange={(e) => onChange(type === 'number' ? Number(e.target.value) : e.target.value)}
        className="w-full px-2 py-1 border border-gray-300 rounded text-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
      />
    )
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <span className="ml-3 text-gray-600">{t('common.loading')}</span>
      </div>
    )
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
        <p className="text-red-800">Failed to load pantry items</p>
      </div>
    )
  }

  const pantryItems = pantryResponse?.content || []

  return (
    <div className="space-y-6">
      {/* Add New Item Section */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-gray-900">Add New Item</h3>
          <Button
            onClick={() => setIsProductPickerOpen(true)}
            variant="primary"
            size="sm"
          >
            Select Product
          </Button>
        </div>

        {newItem.productName && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Product
                </label>
                <input
                  type="text"
                  value={newItem.productName}
                  disabled
                  className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-600"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Quantity *
                </label>
                <input
                  type="number"
                  min="0"
                  step="0.1"
                  value={newItem.quantity || ''}
                  onChange={(e) => setNewItem({ ...newItem, quantity: Number(e.target.value) })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Unit
                </label>
                <select
                  value={newItem.unit || 'piece'}
                  onChange={(e) => setNewItem({ ...newItem, unit: e.target.value as any })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="piece">piece</option>
                  <option value="g">g</option>
                  <option value="kg">kg</option>
                  <option value="ml">ml</option>
                  <option value="l">l</option>
                  <option value="cup">cup</option>
                  <option value="tbsp">tbsp</option>
                  <option value="tsp">tsp</option>
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Expiry Date
                </label>
                <input
                  type="date"
                  value={newItem.expiryDate || ''}
                  onChange={(e) => setNewItem({ ...newItem, expiryDate: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Unit Price
                </label>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  value={newItem.unitPrice || ''}
                  onChange={(e) => setNewItem({ ...newItem, unitPrice: Number(e.target.value) })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Currency
                </label>
                <select
                  value={newItem.currency || 'USD'}
                  onChange={(e) => setNewItem({ ...newItem, currency: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="USD">USD</option>
                  <option value="EUR">EUR</option>
                  <option value="GBP">GBP</option>
                  <option value="GEL">GEL</option>
                </select>
              </div>
            </div>
            
            <div className="flex justify-end space-x-2">
              <Button
                onClick={() => setNewItem({})}
                variant="secondary"
                size="sm"
              >
                Cancel
              </Button>
              <Button
                onClick={handleCreateItem}
                variant="primary"
                size="sm"
                disabled={createMutation.isPending}
              >
                {createMutation.isPending ? 'Adding...' : 'Add Item'}
              </Button>
            </div>
          </div>
        )}
      </div>

      {/* Pantry Items Table */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-semibold text-gray-900">
            Pantry Items ({pantryItems.length})
          </h3>
        </div>

        {pantryItems.length === 0 ? (
          <div className="text-center py-12">
            <div className="mx-auto h-12 w-12 text-gray-400">
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
            </div>
            <h3 className="mt-2 text-sm font-medium text-gray-900">No pantry items</h3>
            <p className="mt-1 text-sm text-gray-500">
              Get started by adding your first pantry item.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Product
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Quantity
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Unit
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Expiry
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Price
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {pantryItems.map((item) => (
                  <tr
                    key={item.id}
                    className={`${getExpiryRowClass(item.expiryDate)} border-l-4`}
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900">
                          {item.productName}
                        </div>
                        {item.category && (
                          <div className="text-sm text-gray-500">{item.category}</div>
                        )}
                      </div>
                    </td>
                    
                    <td className="px-6 py-4 whitespace-nowrap">
                      {editingItem?.id === item.id ? (
                        <EditableCell
                          type="number"
                          value={editingItem.quantity}
                          onChange={(value) => setEditingItem({ ...editingItem, quantity: value })}
                        />
                      ) : (
                        <div className="text-sm text-gray-900">{item.quantity}</div>
                      )}
                    </td>
                    
                    <td className="px-6 py-4 whitespace-nowrap">
                      {editingItem?.id === item.id ? (
                        <EditableCell
                          type="select"
                          value={editingItem.unit}
                          onChange={(value) => setEditingItem({ ...editingItem, unit: value })}
                          options={['piece', 'g', 'kg', 'ml', 'l', 'cup', 'tbsp', 'tsp']}
                        />
                      ) : (
                        <div className="text-sm text-gray-900">{item.unit}</div>
                      )}
                    </td>
                    
                    <td className="px-6 py-4 whitespace-nowrap">
                      {editingItem?.id === item.id ? (
                        <EditableCell
                          type="date"
                          value={editingItem.expiryDate}
                          onChange={(value) => setEditingItem({ ...editingItem, expiryDate: value })}
                        />
                      ) : (
                        <div className="text-sm">
                          {item.expiryDate ? (
                            <span className={`${
                              isExpired(item.expiryDate) 
                                ? 'text-red-600 font-medium' 
                                : isExpiringSoon(item.expiryDate)
                                  ? 'text-yellow-600 font-medium'
                                  : 'text-gray-900'
                            }`}>
                              {formatDate(item.expiryDate)}
                            </span>
                          ) : (
                            <span className="text-gray-400">No expiry</span>
                          )}
                        </div>
                      )}
                    </td>
                    
                    <td className="px-6 py-4 whitespace-nowrap">
                      {editingItem?.id === item.id ? (
                        <div className="flex space-x-2">
                          <EditableCell
                            type="number"
                            value={editingItem.unitPrice}
                            onChange={(value) => setEditingItem({ ...editingItem, unitPrice: value })}
                          />
                          <EditableCell
                            type="select"
                            value={editingItem.currency}
                            onChange={(value) => setEditingItem({ ...editingItem, currency: value })}
                            options={['USD', 'EUR', 'GBP', 'GEL']}
                          />
                        </div>
                      ) : (
                        <div className="text-sm text-gray-900">
                          {formatCurrency(item.unitPrice, item.currency)}
                        </div>
                      )}
                    </td>
                    
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      {editingItem?.id === item.id ? (
                        <div className="flex space-x-2">
                          <button
                            onClick={() => handleUpdateItem(editingItem)}
                            className="text-green-600 hover:text-green-900"
                            disabled={updateMutation.isPending}
                          >
                            Save
                          </button>
                          <button
                            onClick={() => setEditingItem(null)}
                            className="text-gray-600 hover:text-gray-900"
                          >
                            Cancel
                          </button>
                        </div>
                      ) : (
                        <div className="flex space-x-2">
                          <button
                            onClick={() => setEditingItem(item)}
                            className="text-blue-600 hover:text-blue-900"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleDeleteItem(item.id)}
                            className="text-red-600 hover:text-red-900"
                            disabled={deleteMutation.isPending}
                          >
                            Delete
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Product Picker Modal */}
      <ProductPicker
        isOpen={isProductPickerOpen}
        onClose={() => setIsProductPickerOpen(false)}
        onSelect={handleAddProduct}
      />
    </div>
  )
}
