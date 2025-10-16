import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useShareLinks, useRevokeShareLink, useShareLinkAudit } from '@/lib/api/consents'
import { Button } from './Button'
import { toast } from '@/lib/toast/toast'
import type { ShareLink, ShareFilters } from '@/lib/types'

interface ShareLinkManagerProps {
  patientId: string
}

export const ShareLinkManager: React.FC<ShareLinkManagerProps> = ({
  patientId,
}) => {
  const { t } = useTranslation()
  const [filters, setFilters] = useState<ShareFilters>({
    isActive: true,
    page: 0,
    size: 10,
  })
  const [selectedLinkId, setSelectedLinkId] = useState<string | null>(null)

  const { data: shareLinksResponse, isLoading } = useShareLinks(patientId, filters)
  const { data: auditLogs } = useShareLinkAudit(selectedLinkId || '', !!selectedLinkId)
  const revokeMutation = useRevokeShareLink()

  const shareLinks = shareLinksResponse?.content || []

  const handleRevoke = async (linkId: string) => {
    if (window.confirm('Are you sure you want to revoke this share link? This action cannot be undone.')) {
      try {
        await revokeMutation.mutateAsync(linkId)
        toast.success('Link Revoked', 'Share link has been revoked and is no longer accessible')
      } catch (error) {
        // Error handled by global error handler
      }
    }
  }

  const handleCopyLink = async (shareUrl: string) => {
    try {
      await navigator.clipboard.writeText(shareUrl)
      toast.success('Link Copied', 'Share link copied to clipboard')
    } catch (error) {
      toast.error('Copy Failed', 'Could not copy link to clipboard')
    }
  }

  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleString()
  }

  const getStatusBadge = (link: ShareLink) => {
    if (!link.isActive) {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
          Revoked
        </span>
      )
    }
    
    if (link.expiresAt && new Date(link.expiresAt) < new Date()) {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
          Expired
        </span>
      )
    }

    if (link.oneTimeUse && link.accessCount > 0) {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
          Used
        </span>
      )
    }

    if (link.maxAccess && link.accessCount >= link.maxAccess) {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
          Limit Reached
        </span>
      )
    }

    return (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
        Active
      </span>
    )
  }

  const getScopeIcon = (scope: string) => {
    switch (scope) {
      case 'CRITICAL_FACTS':
        return (
          <svg className="h-4 w-4 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
        )
      case 'DAY':
        return (
          <svg className="h-4 w-4 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
        )
      case 'WEEK':
        return (
          <svg className="h-4 w-4 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
        )
      case 'RANGE':
        return (
          <svg className="h-4 w-4 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
        )
      default:
        return (
          <svg className="h-4 w-4 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.367 2.684 3 3 0 00-5.367-2.684z" />
          </svg>
        )
    }
  }

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex items-center justify-center py-8">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
          <span className="ml-3 text-gray-600">{t('common.loading')}</span>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Filters */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">
          Share Links ({shareLinks.length})
        </h3>
        
        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-2">
            <label className="text-sm font-medium text-gray-700">Show:</label>
            <select
              value={filters.isActive === undefined ? 'all' : filters.isActive ? 'active' : 'inactive'}
              onChange={(e) => {
                const value = e.target.value
                setFilters({
                  ...filters,
                  isActive: value === 'all' ? undefined : value === 'active',
                  page: 0,
                })
              }}
              className="px-3 py-1 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="all">All Links</option>
              <option value="active">Active Only</option>
              <option value="inactive">Inactive Only</option>
            </select>
          </div>
          
          <div className="flex items-center space-x-2">
            <label className="text-sm font-medium text-gray-700">Scope:</label>
            <select
              value={filters.scope || ''}
              onChange={(e) => setFilters({
                ...filters,
                scope: e.target.value || undefined,
                page: 0,
              })}
              className="px-3 py-1 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">All Scopes</option>
              <option value="CRITICAL_FACTS">Critical Facts</option>
              <option value="DAY">Day</option>
              <option value="WEEK">Week</option>
              <option value="RANGE">Range</option>
            </select>
          </div>
        </div>
      </div>

      {/* Share Links List */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        {shareLinks.length === 0 ? (
          <div className="text-center py-12">
            <div className="mx-auto h-12 w-12 text-gray-400">
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.367 2.684 3 3 0 00-5.367-2.684z" />
              </svg>
            </div>
            <h3 className="mt-2 text-sm font-medium text-gray-900">No share links</h3>
            <p className="mt-1 text-sm text-gray-500">
              Create your first share link to start sharing data.
            </p>
          </div>
        ) : (
          <div className="divide-y divide-gray-200">
            {shareLinks.map((link) => (
              <div key={link.id} className="p-6">
                <div className="flex items-start justify-between">
                  <div className="flex items-start space-x-3">
                    <div className="flex-shrink-0 mt-1">
                      {getScopeIcon(link.scope)}
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center space-x-2 mb-2">
                        <h4 className="text-sm font-medium text-gray-900">
                          {link.scope.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())}
                        </h4>
                        {getStatusBadge(link)}
                        {link.oneTimeUse && (
                          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800">
                            One-time
                          </span>
                        )}
                        {link.otpRequired && (
                          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-purple-100 text-purple-800">
                            OTP
                          </span>
                        )}
                      </div>

                      <div className="space-y-1 text-sm text-gray-600">
                        <div className="flex items-center space-x-4">
                          <span>
                            <strong>Created:</strong> {formatDate(link.createdAt)}
                          </span>
                          <span>
                            <strong>Accessed:</strong> {link.accessCount} time{link.accessCount !== 1 ? 's' : ''}
                            {link.maxAccess && ` of ${link.maxAccess}`}
                          </span>
                        </div>
                        
                        {link.expiresAt && (
                          <div>
                            <strong>Expires:</strong> {formatDate(link.expiresAt)}
                          </div>
                        )}
                        
                        {link.lastAccessedAt && (
                          <div>
                            <strong>Last accessed:</strong> {formatDate(link.lastAccessedAt)}
                          </div>
                        )}

                        {link.emailInvite && (
                          <div>
                            <strong>Email invite sent to:</strong> {link.emailInvite}
                          </div>
                        )}
                      </div>

                      {/* Truncated URL */}
                      <div className="mt-2">
                        <div className="flex items-center space-x-2">
                          <code className="text-xs bg-gray-100 px-2 py-1 rounded font-mono">
                            {link.shareUrl.length > 60 ? `${link.shareUrl.substring(0, 60)}...` : link.shareUrl}
                          </code>
                          <button
                            onClick={() => handleCopyLink(link.shareUrl)}
                            className="text-blue-600 hover:text-blue-800 text-sm"
                          >
                            Copy
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center space-x-2">
                    <button
                      onClick={() => setSelectedLinkId(selectedLinkId === link.id ? null : link.id)}
                      className="text-gray-600 hover:text-gray-800 text-sm"
                    >
                      {selectedLinkId === link.id ? 'Hide Audit' : 'View Audit'}
                    </button>
                    
                    {link.isActive && (
                      <Button
                        onClick={() => handleRevoke(link.id)}
                        variant="danger"
                        size="sm"
                        disabled={revokeMutation.isPending}
                      >
                        Revoke
                      </Button>
                    )}
                  </div>
                </div>

                {/* Audit Log */}
                {selectedLinkId === link.id && auditLogs && (
                  <div className="mt-4 pt-4 border-t border-gray-200">
                    <h5 className="text-sm font-medium text-gray-900 mb-3">
                      Audit Log ({auditLogs.length} events)
                    </h5>
                    
                    {auditLogs.length === 0 ? (
                      <p className="text-sm text-gray-500">No audit events recorded.</p>
                    ) : (
                      <div className="space-y-2">
                        {auditLogs.map((log) => (
                          <div key={log.id} className="flex items-start space-x-3 text-sm">
                            <div className="flex-shrink-0 w-20 text-gray-500">
                              {formatDate(log.createdAt).split(' ')[1]}
                            </div>
                            <div className="flex-1">
                              <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${
                                log.action === 'ACCESSED' ? 'bg-green-100 text-green-800' :
                                log.action === 'CREATED' ? 'bg-blue-100 text-blue-800' :
                                log.action === 'REVOKED' ? 'bg-red-100 text-red-800' :
                                log.action === 'EXPIRED' ? 'bg-yellow-100 text-yellow-800' :
                                'bg-gray-100 text-gray-800'
                              }`}>
                                {log.action.toLowerCase().replace('_', ' ')}
                              </span>
                              {log.ipAddress && (
                                <span className="ml-2 text-gray-600">
                                  from {log.ipAddress}
                                </span>
                              )}
                              {log.details && (
                                <span className="ml-2 text-gray-600">
                                  - {log.details}
                                </span>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
