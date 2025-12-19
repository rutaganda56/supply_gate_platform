import { toast } from 'react-toastify';

/**
 * Modern, subtle toast notifications
 * Uses a less intrusive style similar to modern applications
 */

// Configure toast defaults for subtle, modern appearance
const toastConfig = {
  position: 'bottom-right',
  autoClose: 3000,
  hideProgressBar: true,
  closeOnClick: true,
  pauseOnHover: true,
  draggable: true,
  closeButton: false,
  style: {
    borderRadius: '8px',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
    fontSize: '14px',
    padding: '12px 16px',
  },
};

/**
 * Success toast - Green with checkmark
 */
export const showSuccess = (message) => {
  toast.success(message, {
    ...toastConfig,
    icon: '✅',
    style: {
      ...toastConfig.style,
      background: '#10b981',
      color: '#ffffff',
    },
  });
};

/**
 * Error toast - Red with X icon
 */
export const showError = (message) => {
  toast.error(message, {
    ...toastConfig,
    icon: '❌',
    style: {
      ...toastConfig.style,
      background: '#ef4444',
      color: '#ffffff',
    },
  });
};

/**
 * Info toast - Blue with info icon
 */
export const showInfo = (message) => {
  toast.info(message, {
    ...toastConfig,
    icon: 'ℹ️',
    style: {
      ...toastConfig.style,
      background: '#3b82f6',
      color: '#ffffff',
    },
  });
};

/**
 * Warning toast - Yellow/Orange with warning icon
 */
export const showWarning = (message) => {
  toast.warning(message, {
    ...toastConfig,
    icon: '⚠️',
    style: {
      ...toastConfig.style,
      background: '#f59e0b',
      color: '#ffffff',
    },
  });
};

/**
 * Default toast - Neutral gray
 */
export const showToast = (message) => {
  toast(message, {
    ...toastConfig,
    style: {
      ...toastConfig.style,
      background: '#374151',
      color: '#ffffff',
    },
  });
};

