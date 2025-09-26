import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useLocation } from 'react-router-dom'
import { LanguageSwitcher } from './LanguageSwitcher'
import { useAuth } from '@/contexts/AuthContext'
import { Button } from './Button'

const navigation = [
  { key: 'dashboard', path: '/' },
  { key: 'products', path: '/products' },
  { key: 'dishes', path: '/dishes' },
  { key: 'pantry', path: '/pantry' },
  { key: 'scan', path: '/scan' },
]

const adminNavigation = [
  { key: 'adminPanel', path: '/admin' },
]

export const AppHeader: React.FC = () => {
  const { t, i18n } = useTranslation()
  const location = useLocation()
  const { isAuthenticated, user, logout, isLoading } = useAuth()
  const [showUserMenu, setShowUserMenu] = useState(false)
  const [showMobileMenu, setShowMobileMenu] = useState(false)

  return (
    <header className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo and Title */}
          <div className="flex items-center">
            <Link to="/" className="flex items-center space-x-3">
              <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">PKU</span>
              </div>
              <h1 className="text-xl font-semibold text-gray-900">
                {t('app.title')}
              </h1>
            </Link>
          </div>

          {/* Navigation */}
          <nav className="hidden md:flex space-x-8">
            {/* Admin Navigation - Show first for ADMIN users */}
            {user?.role === 'ADMIN' && adminNavigation.map((item) => {
              const isActive = location.pathname === item.path
              return (
                <Link
                  key={item.key}
                  to={item.path}
                  className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    isActive
                      ? 'text-red-600 bg-red-50'
                      : 'text-red-700 hover:text-red-600 hover:bg-red-50'
                  }`}
                >
                  {t(`navigation.${item.key}`)}
                </Link>
              )
            })}
            {/* Regular Navigation - Hide Dashboard for ADMIN users */}
            {navigation.filter(item => !(user?.role === 'ADMIN' && item.key === 'dashboard')).map((item) => {
              const isActive = location.pathname === item.path
              return (
                <Link
                  key={item.key}
                  to={item.path}
                  className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    isActive
                      ? 'text-blue-600 bg-blue-50'
                      : 'text-gray-700 hover:text-blue-600 hover:bg-gray-50'
                  }`}
                >
                  {t(`navigation.${item.key}`)}
                </Link>
              )
            })}
          </nav>

          {/* Mobile menu button */}
          <div className="md:hidden">
            <button
              onClick={() => setShowMobileMenu(!showMobileMenu)}
              className="p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              aria-expanded={showMobileMenu}
              aria-label={t('navigation.toggleMenu')}
            >
              <svg
                className="h-6 w-6"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                aria-hidden="true"
              >
                {showMobileMenu ? (
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                ) : (
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                )}
              </svg>
            </button>
          </div>

          {/* Right side - Authentication & Language */}
          <div className="hidden md:flex items-center space-x-4">
            {/* Current language badge */}
            <span
              className="px-2 py-1 text-xs font-semibold rounded-md bg-gray-100 text-gray-700 border border-gray-200"
              title={i18n.language}
            >
              {(i18n.language || 'en').slice(0, 2).toUpperCase()}
            </span>
            <LanguageSwitcher />
            
            {/* Authentication Section */}
            {isAuthenticated ? (
              <div className="relative">
                {/* User Menu Button */}
                <button
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  className="flex items-center space-x-3 p-2 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                >
                  <div className="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center">
                    <span className="text-white font-medium text-sm">
                      {user?.username?.charAt(0).toUpperCase()}
                    </span>
                  </div>
                  <div className="hidden md:block text-left">
                    <p className="text-sm font-medium text-gray-700">{user?.username}</p>
                    <p className="text-xs text-gray-500">{t(`roles.${user?.role?.toLowerCase()}`)}</p>
                  </div>
                  <svg className="h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                </button>

                {/* Dropdown Menu */}
                {showUserMenu && (
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none z-50">
                    <div className="py-1">
                      <Link
                        to="/profile"
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                        onClick={() => setShowUserMenu(false)}
                      >
                        {t('navigation.profile')}
                      </Link>
                      <Link
                        to="/settings"
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                        onClick={() => setShowUserMenu(false)}
                      >
                        {t('navigation.settings')}
                      </Link>
                      <div className="border-t border-gray-100"></div>
                      <button
                        onClick={() => {
                          setShowUserMenu(false)
                          logout()
                        }}
                        className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                        disabled={isLoading}
                      >
                        {isLoading ? t('auth.logout.signingOut') : t('auth.logout.title')}
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <Link to="/login">
                <Button size="sm">
                  {t('auth.login.signIn')}
                </Button>
              </Link>
            )}
          </div>
        </div>

        {/* Mobile menu */}
        {showMobileMenu && (
          <div className="md:hidden">
            <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3 border-t border-gray-200 bg-white">
              {/* Admin Mobile Navigation - Show first for ADMIN users */}
              {user?.role === 'ADMIN' && adminNavigation.map((item) => {
                const isActive = location.pathname === item.path
                return (
                  <Link
                    key={item.key}
                    to={item.path}
                    className={`block px-3 py-2 rounded-md text-base font-medium transition-colors ${
                      isActive
                        ? 'text-red-600 bg-red-50'
                        : 'text-red-700 hover:text-red-600 hover:bg-red-50'
                    }`}
                    onClick={() => setShowMobileMenu(false)}
                  >
                    {t(`navigation.${item.key}`)}
                  </Link>
                )
              })}
              {/* Mobile Navigation Links - Hide Dashboard for ADMIN users */}
              {navigation.filter(item => !(user?.role === 'ADMIN' && item.key === 'dashboard')).map((item) => {
                const isActive = location.pathname === item.path
                return (
                  <Link
                    key={item.key}
                    to={item.path}
                    className={`block px-3 py-2 rounded-md text-base font-medium transition-colors ${
                      isActive
                        ? 'text-blue-600 bg-blue-50'
                        : 'text-gray-700 hover:text-blue-600 hover:bg-gray-50'
                    }`}
                    onClick={() => setShowMobileMenu(false)}
                  >
                    {t(`navigation.${item.key}`)}
                  </Link>
                )
              })}
              
              {/* Mobile Authentication Section */}
              <div className="pt-4 pb-3 border-t border-gray-200">
                <div className="flex items-center px-3 mb-3">
                  <LanguageSwitcher />
                </div>
                
                {isAuthenticated ? (
                  <div className="space-y-1">
                    {/* User Info */}
                    <div className="flex items-center px-3 py-2">
                      <div className="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center mr-3">
                        <span className="text-white font-medium text-sm">
                          {user?.username?.charAt(0).toUpperCase()}
                        </span>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-gray-700">{user?.username}</p>
                        <p className="text-xs text-gray-500">{t(`roles.${user?.role?.toLowerCase()}`)}</p>
                      </div>
                    </div>
                    
                    {/* User Menu Links */}
                    <Link
                      to="/profile"
                      className="block px-3 py-2 text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50 rounded-md"
                      onClick={() => setShowMobileMenu(false)}
                    >
                      {t('navigation.profile')}
                    </Link>
                    <Link
                      to="/settings"
                      className="block px-3 py-2 text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50 rounded-md"
                      onClick={() => setShowMobileMenu(false)}
                    >
                      {t('navigation.settings')}
                    </Link>
                    <button
                      onClick={() => {
                        setShowMobileMenu(false)
                        logout()
                      }}
                      className="block w-full text-left px-3 py-2 text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50 rounded-md"
                      disabled={isLoading}
                    >
                      {isLoading ? t('auth.logout.signingOut') : t('auth.logout.title')}
                    </button>
                  </div>
                ) : (
                  <div className="px-3">
                    <Link
                      to="/login"
                      onClick={() => setShowMobileMenu(false)}
                    >
                      <Button size="sm" className="w-full">
                        {t('auth.login.signIn')}
                      </Button>
                    </Link>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </header>
  )
}
