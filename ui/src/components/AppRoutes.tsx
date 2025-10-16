import React from 'react'
import { Routes, Route } from 'react-router-dom'
import { Dashboard } from '@/pages/Dashboard'
import { Day } from '@/pages/Day'
import { Week } from '@/pages/Week'
import { Products } from '@/pages/Products'
import { CategoryProducts } from '@/pages/CategoryProducts'
import { AllProducts } from '@/pages/AllProducts'
import { Dishes } from '@/pages/Dishes'
import { DishDetail } from '@/pages/DishDetail'
import { Pantry } from '@/pages/Pantry'
import { Scan } from '@/pages/Scan'
import { Share } from '@/pages/Share'
import { Critical } from '@/pages/Critical'
import { Profile } from '@/pages/Profile'
import { Settings } from '@/pages/Settings'
import { AdminPanel } from '@/pages/AdminPanel'
import { ProductNew } from '@/pages/ProductNew'
import { Login } from '@/pages/Login'
import { ProtectedRoute, AdminRoute } from '@/components/ProtectedRoute'

export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/login" element={<Login />} />

      {/* Protected Routes - Require Authentication */}
      <Route path="/" element={
        <ProtectedRoute>
          <Dashboard />
        </ProtectedRoute>
      } />
      
      <Route path="/day/:id" element={
        <ProtectedRoute>
          <Day />
        </ProtectedRoute>
      } />
      
      <Route path="/week" element={
        <ProtectedRoute>
          <Week />
        </ProtectedRoute>
      } />
      
      <Route path="/dishes" element={
        <ProtectedRoute>
          <Dishes />
        </ProtectedRoute>
      } />
      
      <Route path="/dishes/:id" element={
        <ProtectedRoute>
          <DishDetail />
        </ProtectedRoute>
      } />
      
      <Route path="/pantry" element={
        <ProtectedRoute>
          <Pantry />
        </ProtectedRoute>
      } />
      
      <Route path="/scan" element={
        <ProtectedRoute>
          <Scan />
        </ProtectedRoute>
      } />
      
      <Route path="/share" element={
        <ProtectedRoute>
          <Share />
        </ProtectedRoute>
      } />
      
      <Route path="/critical" element={
        <ProtectedRoute>
          <Critical />
        </ProtectedRoute>
      } />
      
      <Route path="/profile" element={
        <ProtectedRoute>
          <Profile />
        </ProtectedRoute>
      } />
      
      <Route path="/settings" element={
        <ProtectedRoute>
          <Settings />
        </ProtectedRoute>
      } />

      {/* Admin Routes - Admin only */}
      <Route path="/admin" element={
        <AdminRoute>
          <AdminPanel />
        </AdminRoute>
      } />

      {/* Products Routes - All authenticated users */}
      <Route path="/products" element={
        <ProtectedRoute>
          <Products />
        </ProtectedRoute>
      } />
      
      <Route path="/products/all" element={
        <ProtectedRoute>
          <AllProducts />
        </ProtectedRoute>
      } />
      
      <Route path="/products/category/:categoryName" element={
        <ProtectedRoute>
          <CategoryProducts />
        </ProtectedRoute>
      } />
      
      <Route path="/products/new" element={
        <AdminRoute>
          <ProductNew />
        </AdminRoute>
      } />
    </Routes>
  )
}
