// Simple toast notification system

export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface Toast {
  id: string
  type: ToastType
  title: string
  message?: string
  duration?: number
  persistent?: boolean
}

type ToastListener = (toasts: Toast[]) => void

class ToastManager {
  private toasts: Toast[] = []
  private listeners: Set<ToastListener> = new Set()
  private idCounter = 0

  subscribe(listener: ToastListener) {
    this.listeners.add(listener)
    return () => this.listeners.delete(listener)
  }

  private notify() {
    this.listeners.forEach(listener => listener([...this.toasts]))
  }

  private generateId(): string {
    return `toast-${++this.idCounter}-${Date.now()}`
  }

  show(toast: Omit<Toast, 'id'>): string {
    const id = this.generateId()
    const newToast: Toast = {
      id,
      duration: 5000, // 5 seconds default
      ...toast,
    }

    this.toasts.push(newToast)
    this.notify()

    // Auto-remove after duration (unless persistent)
    if (!newToast.persistent && newToast.duration && newToast.duration > 0) {
      setTimeout(() => {
        this.dismiss(id)
      }, newToast.duration)
    }

    return id
  }

  dismiss(id: string) {
    const index = this.toasts.findIndex(toast => toast.id === id)
    if (index > -1) {
      this.toasts.splice(index, 1)
      this.notify()
    }
  }

  dismissAll() {
    this.toasts = []
    this.notify()
  }

  // Convenience methods
  success(title: string, message?: string, options?: Partial<Toast>): string {
    return this.show({ type: 'success', title, message, ...options })
  }

  error(title: string, message?: string, options?: Partial<Toast>): string {
    return this.show({ 
      type: 'error', 
      title, 
      message, 
      duration: 8000, // Errors stay longer
      ...options 
    })
  }

  warning(title: string, message?: string, options?: Partial<Toast>): string {
    return this.show({ type: 'warning', title, message, ...options })
  }

  info(title: string, message?: string, options?: Partial<Toast>): string {
    return this.show({ type: 'info', title, message, ...options })
  }
}

export const toast = new ToastManager()
