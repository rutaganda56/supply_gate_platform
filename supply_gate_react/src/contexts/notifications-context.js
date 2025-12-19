import { createContext, useContext, useState, useEffect, useCallback, useRef } from "react";
import { notificationApi, messageApi } from "../lib/api";

const NotificationsContext = createContext(undefined);

export function NotificationsProvider({ children }) {
  const [notifications, setNotifications] = useState([]);
  const [unreadMessageCount, setUnreadMessageCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const isMountedRef = useRef(true);

  // Memoize loadNotifications to prevent infinite loops
  const loadNotifications = useCallback(async () => {
    // Check if user is authenticated
    if (typeof window === "undefined") return;
    
    const token = localStorage.getItem("token");
    const userType = localStorage.getItem("userType");
    
    if (!token) {
      if (isMountedRef.current) {
        setNotifications([]);
        setUnreadMessageCount(0);
        setLoading(false);
      }
      return;
    }

    if (isMountedRef.current) {
      setLoading(true);
      setError(null);
    }
    
    try {
      // Load first page of notifications for the sidebar count
      // Individual pages will load their own paginated data
      const data = await notificationApi.getAll(0, 50); // Get first 50 for context
      
      if (!isMountedRef.current) return; // Component unmounted, don't update state
      
      // Ensure data has the expected structure
      if (data && Array.isArray(data.content)) {
        setNotifications(data.content);
      } else if (Array.isArray(data)) {
        // Fallback: if API returns array directly instead of paginated response
        setNotifications(data);
      } else {
        if (process.env.NODE_ENV === 'development') {
          console.warn("Unexpected notification data structure:", data);
        }
        setNotifications([]);
      }

      // Also load unread message count for suppliers
      if (userType === "SUPPLIER") {
        try {
          const messageCount = await messageApi.getUnreadCount();
          if (!isMountedRef.current) return;
          setUnreadMessageCount(messageCount || 0);
        } catch (err) {
          // Silently fail - message count is not critical
          if (process.env.NODE_ENV === 'development') {
            console.warn("Failed to load unread message count:", err);
          }
          if (isMountedRef.current) {
            setUnreadMessageCount(0);
          }
        }
      } else {
        setUnreadMessageCount(0);
      }
    } catch (err) {
      if (!isMountedRef.current) return; // Component unmounted, don't update state
      
      // If unauthorized, user is not logged in - set empty array (this is expected)
      if (err.response?.status === 401 || err.response?.status === 403) {
        setNotifications([]);
        setUnreadMessageCount(0);
        setError(null); // Don't show error for auth issues
        // Don't log this error - it's expected when user is not logged in
      } else if (err.code === 'ERR_NETWORK' || err.message?.includes('Network Error')) {
        // Network error - don't show error, just keep existing notifications
        // Don't clear notifications on network error - keep what we have
        setError(null);
        // Only log network errors in development
        if (process.env.NODE_ENV === 'development') {
          console.warn("Network error loading notifications:", err.message);
        }
      } else {
        // Only log unexpected errors in development
        if (process.env.NODE_ENV === 'development') {
          console.error("Failed to load notifications:", err);
        }
        setError(null); // Don't show error to user
        // Keep existing notifications on error
      }
    } finally {
      if (isMountedRef.current) {
        setLoading(false);
      }
    }
  }, []); // Empty dependency array - function is stable

  // Load notifications on mount
  useEffect(() => {
    isMountedRef.current = true;
    loadNotifications();
    
    // Set up polling to refresh notifications every 10 seconds for real-time updates
    const interval = setInterval(() => {
      if (isMountedRef.current) {
        loadNotifications();
      }
    }, 10000); // Poll every 10 seconds for better real-time responsiveness

    return () => {
      isMountedRef.current = false;
      clearInterval(interval);
    };
  }, [loadNotifications]);

  // Listen for storage changes (when user logs in/out)
  useEffect(() => {
    if (typeof window === "undefined") return;

    const handleStorageChange = (e) => {
      if (e.key === "token" && isMountedRef.current) {
        loadNotifications();
      }
    };

    window.addEventListener("storage", handleStorageChange);
    return () => window.removeEventListener("storage", handleStorageChange);
  }, [loadNotifications]);

  // Calculate unread count from notifications and messages
  // Includes both regular notifications and unread messages
  const unreadCount = notifications.filter((n) => !n.isRead).length + unreadMessageCount;

  const markAsRead = async (notificationId) => {
    try {
      await notificationApi.markAsRead(notificationId);
      // Update local state
      setNotifications((prev) =>
        prev.map((n) =>
          n.notificationId === notificationId ? { ...n, isRead: true } : n
        )
      );
      // Refresh message count if this was a message notification
      const notification = notifications.find((n) => n.notificationId === notificationId);
      if (notification && notification.type === "message") {
        // Reload message count to update counter
        try {
          const userType = localStorage.getItem("userType");
          if (userType === "SUPPLIER") {
            const messageCount = await messageApi.getUnreadCount();
            setUnreadMessageCount(messageCount || 0);
          }
        } catch (err) {
          // Silently fail
          if (process.env.NODE_ENV === 'development') {
            console.warn("Failed to refresh message count:", err);
          }
        }
      }
    } catch (err) {
      console.error("Failed to mark notification as read:", err);
      throw err;
    }
  };

  const markAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      // Update local state
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    } catch (err) {
      console.error("Failed to mark all notifications as read:", err);
      throw err;
    }
  };

  const refreshNotifications = async () => {
    await loadNotifications();
  };

  return (
    <NotificationsContext.Provider
      value={{
        notifications,
        unreadCount,
        loading,
        error,
        markAsRead,
        markAllAsRead,
        refreshNotifications,
      }}
    >
      {children}
    </NotificationsContext.Provider>
  );
}

export function useNotifications() {
  const context = useContext(NotificationsContext);
  if (!context) {
    throw new Error(
      "useNotifications must be used within a NotificationsProvider"
    );
  }
  return context;
}
