import { useState, useEffect } from 'react'
import { toast, Toast } from './toast'

export function useToast() {
  const [toasts, setToasts] = useState<Toast[]>([])

  useEffect(() => {
    const unsubscribe = toast.subscribe(setToasts)
    return unsubscribe
  }, [])

  return {
    toasts,
    show: toast.show.bind(toast),
    dismiss: toast.dismiss.bind(toast),
    dismissAll: toast.dismissAll.bind(toast),
    success: toast.success.bind(toast),
    error: toast.error.bind(toast),
    warning: toast.warning.bind(toast),
    info: toast.info.bind(toast),
  }
}
