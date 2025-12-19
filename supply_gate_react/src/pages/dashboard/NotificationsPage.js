import React, { useState, useEffect } from "react";
import { Sidebar } from "../../components/Sidebar";
import { Calendar, ExternalLink, FileText, Loader2, RefreshCw, Mail, Eye } from "lucide-react";
import { Button } from "../../components/ui/button";
import { notificationApi, messageApi } from "../../lib/api";
import { TablePagination } from "../../components/TablePagination";
import { TableSearch } from "../../components/TableSearch";
import { Link } from "react-router-dom";
import { useNotifications } from "../../contexts/notifications-context";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "../../components/ui/dialog";
import { showWarning } from "../../lib/toast";

export default function NotificationsPage() {
  const { refreshNotifications } = useNotifications();
  
  // Notifications state
  const [notifications, setNotifications] = useState([]);
  const [notificationsPage, setNotificationsPage] = useState(0);
  const [notificationsPageSize, setNotificationsPageSize] = useState(20);
  const [notificationsTotalPages, setNotificationsTotalPages] = useState(0);
  const [notificationsTotalElements, setNotificationsTotalElements] = useState(0);
  const [notificationsSearch, setNotificationsSearch] = useState("");
  const [loadingNotifications, setLoadingNotifications] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [selectedMessage, setSelectedMessage] = useState(null);
  const [messageDialogOpen, setMessageDialogOpen] = useState(false);

  // Load notifications
  const loadNotifications = async (showRefreshing = false) => {
    if (showRefreshing) {
      setRefreshing(true);
    } else {
      setLoadingNotifications(true);
    }
    try {
      const data = await notificationApi.getAll(notificationsPage, notificationsPageSize, notificationsSearch);
      setNotifications(data.content);
      setNotificationsTotalPages(data.totalPages);
      setNotificationsTotalElements(data.totalElements);
      // Also refresh the global notifications context (but don't await to avoid blocking)
      refreshNotifications().catch(() => {
        // Silently fail - context refresh is not critical
      });
    } catch (err) {
      if (process.env.NODE_ENV === 'development') {
        console.error("Failed to load notifications:", err);
      }
      // Don't clear notifications on error - keep existing data
      // Only clear if it's an auth error
      if (err.response?.status === 401 || err.response?.status === 403) {
        setNotifications([]);
      }
    } finally {
      setLoadingNotifications(false);
      setRefreshing(false);
    }
  };

  // Manual refresh function
  const handleRefresh = async () => {
    await loadNotifications(true);
  };

  // Load data when component mounts or when pagination/search changes
  useEffect(() => {
    loadNotifications();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [notificationsPage, notificationsPageSize, notificationsSearch]);

  // Set up real-time polling for notifications
  useEffect(() => {
    // Poll every 10 seconds for real-time updates
    const notificationsInterval = setInterval(() => {
      loadNotifications(false);
    }, 10000);

    return () => {
      clearInterval(notificationsInterval);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Empty dependency array - intervals are stable

  const markAsRead = async (notificationId) => {
    try {
      await notificationApi.markAsRead(notificationId);
      await loadNotifications();
    } catch (err) {
      if (process.env.NODE_ENV === 'development') {
        console.error("Failed to mark notification as read:", err);
      }
    }
  };

  // Load message details for a notification
  const loadMessageDetails = async (notificationMessage) => {
    try {
      // Extract sender email from notification message
      // Format: "New message from {senderName} ({senderEmail}): {subject}"
      let senderEmail = "";
      let subject = "";
      
      if (notificationMessage) {
        const emailMatch = notificationMessage.match(/\(([^)]+@[^)]+)\)/);
        if (emailMatch && emailMatch[1]) {
          senderEmail = emailMatch[1].trim();
        }
        
        // Extract subject (after the colon)
        const subjectMatch = notificationMessage.match(/:\s*(.+)$/);
        if (subjectMatch && subjectMatch[1]) {
          subject = subjectMatch[1].trim();
        }
      }
      
      if (!senderEmail) {
        return null;
      }
      
      // Try to find the unread message from this sender with this subject
      try {
        const unreadMessages = await messageApi.getUnreadMessages();
        const matchingMessage = unreadMessages.find(
          (msg) => msg.senderEmail === senderEmail && msg.subject === subject
        );
        return matchingMessage || null;
      } catch (err) {
        if (process.env.NODE_ENV === 'development') {
          console.warn("Could not fetch message details:", err);
        }
        return null;
      }
    } catch (err) {
      if (process.env.NODE_ENV === 'development') {
        console.error("Failed to load message details:", err);
      }
      return null;
    }
  };

  const handleViewMessage = async (notificationId, notificationMessage) => {
    try {
      // Mark notification as read
      await markAsRead(notificationId);
      
      // Load message details
      const message = await loadMessageDetails(notificationMessage);
      if (message) {
        setSelectedMessage(message);
        setMessageDialogOpen(true);
        
        // Also mark the message as read when viewing
        try {
          await messageApi.markAsRead(message.messageId);
          // Refresh notifications context to update counter
          refreshNotifications().catch(() => {});
        } catch (err) {
          if (process.env.NODE_ENV === 'development') {
            console.warn("Failed to mark message as read:", err);
          }
        }
      } else {
        // If we can't find the message, just show the notification info
        showWarning("Message details not available. Please reply via email.");
      }
    } catch (err) {
      if (process.env.NODE_ENV === 'development') {
        console.error("Failed to handle view message:", err);
      }
    }
  };

  const handleReply = async (notificationId, notificationMessage) => {
    try {
      // Mark notification as read
      await markAsRead(notificationId);
      
      let senderEmail = "";
      
      // First, try to extract email from notification message
      if (notificationMessage) {
        const emailMatch = notificationMessage.match(/\(([^)]+@[^)]+)\)/);
        if (emailMatch && emailMatch[1]) {
          senderEmail = emailMatch[1].trim();
        }
      }
      
      // If not found in message, try to fetch from backend
      if (!senderEmail) {
        try {
          senderEmail = await notificationApi.getSenderEmail(notificationId);
        } catch (err) {
          if (process.env.NODE_ENV === 'development') {
            console.warn("Could not fetch sender email from backend:", err);
          }
        }
      }
      
      // Try to find and mark the corresponding message as read
      if (senderEmail) {
        try {
          const unreadMessages = await messageApi.getUnreadMessages();
          // Find messages from this sender and mark them as read
          const messagesToMark = unreadMessages.filter(
            (msg) => msg.senderEmail === senderEmail
          );
          
          // Mark all unread messages from this sender as read
          for (const msg of messagesToMark) {
            try {
              await messageApi.markAsRead(msg.messageId);
            } catch (err) {
              // Silently continue if one fails
            }
          }
          
          // Refresh notifications context to update counter
          refreshNotifications().catch(() => {});
        } catch (err) {
          // Silently fail - marking message as read is not critical
          if (process.env.NODE_ENV === 'development') {
            console.warn("Failed to mark message as read:", err);
          }
        }
      }
      
      // Open Gmail compose with the sender email
      if (senderEmail) {
        const gmailUrl = `https://mail.google.com/mail/?view=cm&fs=1&to=${encodeURIComponent(senderEmail)}`;
        window.open(gmailUrl, '_blank');
      } else {
        window.open('https://mail.google.com/mail/?view=cm&fs=1', '_blank');
      }
    } catch (err) {
      if (process.env.NODE_ENV === 'development') {
        console.error("Failed to handle reply:", err);
      }
    }
  };

  const handleViewVerification = async (notificationId) => {
    await markAsRead(notificationId);
    // Navigation will be handled by Link
  };

  const formatDate = (dateString) => {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return dateString;
    }
  };

  return (
    <div className="flex min-h-screen bg-gray-100 h-full">
      <Sidebar />

      <main className="flex-1 p-8 min-h-full">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-3xl font-bold text-gray-800">
            Notifications
          </h1>
          <div className="flex items-center gap-3">
            <Button
              variant="outline"
              size="sm"
              onClick={handleRefresh}
              disabled={refreshing}
              className="flex items-center gap-2"
            >
              <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
              Refresh
            </Button>
            <div className="flex items-center gap-2 text-sm text-gray-600 bg-white px-4 py-2 rounded-lg border border-gray-200 shadow-sm">
              <Calendar className="h-4 w-4 text-gray-500" />
              <span className="font-medium">{new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span>
            </div>
          </div>
        </div>

        {/* Notification Details Table */}
        <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-semibold text-gray-800">
              All Notifications
            </h2>
          </div>

          {/* Search */}
          <div className="mb-4">
            <TableSearch
              placeholder="Search notifications by message or type..."
              onSearch={(search) => {
                setNotificationsSearch(search);
                setNotificationsPage(0);
              }}
              className="max-w-md"
            />
          </div>

          {loadingNotifications ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="w-6 h-6 animate-spin text-[#1e4d5c]" />
            </div>
          ) : notifications.length === 0 ? (
            <p className="text-gray-500 text-center py-8">
              {notificationsSearch ? "No notifications found matching your search" : "No notifications yet"}
            </p>
          ) : (
            <>
              <div className="overflow-x-auto mb-4">
                <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-200">
                    <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">
                      Notification Id
                    </th>
                    <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">
                      User Name
                    </th>
                    <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">
                      Message
                    </th>
                    <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">
                      Date
                    </th>
                    <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">
                      Action
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {notifications.map((notification) => (
                    <tr
                      key={notification.notificationId}
                      className={`border-b border-gray-100 hover:bg-gray-50 transition-colors ${
                        notification.isRead ? "opacity-60" : ""
                      }`}
                    >
                      <td className="py-3 px-4 text-sm text-gray-700 font-medium">
                        {notification.notificationId.substring(0, 8)}...
                      </td>
                      <td className="py-3 px-4 text-sm text-gray-700">
                        {notification.userName}
                      </td>
                      <td className="py-3 px-4 text-sm text-gray-700">
                        {notification.message}
                      </td>
                      <td className="py-3 px-4 text-sm text-gray-600">
                        {formatDate(notification.createdAt)}
                      </td>
                      <td className="py-3 px-4">
                        {notification.type === "verification_rejection" ? (
                          <Link to="/dashboard/verification">
                            <Button
                              size="sm"
                              className="bg-[#1a3a3a] hover:bg-[#2a4a4a] text-white text-xs px-3 py-1 h-7"
                              onClick={() => handleViewVerification(notification.notificationId)}
                            >
                              <FileText className="h-3 w-3 mr-1" />
                              View Details
                            </Button>
                          </Link>
                        ) : notification.type === "message" ? (
                          <div className="flex gap-2">
                            <Button
                              size="sm"
                              variant="outline"
                              className="text-xs px-3 py-1 h-7 flex items-center gap-1"
                              onClick={() => handleViewMessage(notification.notificationId, notification.message)}
                            >
                              <Eye className="h-3 w-3" />
                              View
                            </Button>
                            <Button
                              size="sm"
                              className="bg-[#1e4d5c] hover:bg-[#163a47] text-white text-xs px-3 py-1 h-7 flex items-center gap-1"
                              onClick={() => handleReply(notification.notificationId, notification.message)}
                            >
                              <ExternalLink className="h-3 w-3" />
                              Reply via Email
                            </Button>
                          </div>
                        ) : (
                          <Button
                            size="sm"
                            variant="outline"
                            className="text-xs px-3 py-1 h-7"
                            onClick={() => markAsRead(notification.notificationId)}
                            disabled={notification.isRead}
                          >
                            {notification.isRead ? "Read" : "Mark Read"}
                          </Button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              </div>

              {/* Pagination */}
              <TablePagination
                currentPage={notificationsPage}
                totalPages={notificationsTotalPages}
                totalElements={notificationsTotalElements}
                pageSize={notificationsPageSize}
                onPageChange={setNotificationsPage}
                onPageSizeChange={(size) => {
                  setNotificationsPageSize(size);
                  setNotificationsPage(0);
                }}
                loading={loadingNotifications}
              />
            </>
          )}
        </div>

        {/* Message Details Dialog */}
        <Dialog open={messageDialogOpen} onOpenChange={setMessageDialogOpen}>
          <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle className="text-xl font-semibold text-gray-800 flex items-center gap-2">
                <Mail className="w-5 h-5 text-[#1e4d5c]" />
                Message Details
              </DialogTitle>
              <DialogDescription className="text-sm text-gray-600">
                View the full message content before replying
              </DialogDescription>
            </DialogHeader>

            {selectedMessage && (
              <div className="space-y-4 mt-4">
                <div>
                  <label className="text-sm font-medium text-gray-700">From:</label>
                  <p className="text-sm text-gray-900 mt-1">
                    {selectedMessage.senderName} ({selectedMessage.senderEmail})
                  </p>
                </div>

                {selectedMessage.senderPhone && (
                  <div>
                    <label className="text-sm font-medium text-gray-700">Phone:</label>
                    <p className="text-sm text-gray-900 mt-1">{selectedMessage.senderPhone}</p>
                  </div>
                )}

                <div>
                  <label className="text-sm font-medium text-gray-700">Subject:</label>
                  <p className="text-sm text-gray-900 mt-1">{selectedMessage.subject}</p>
                </div>

                {selectedMessage.productName && (
                  <div>
                    <label className="text-sm font-medium text-gray-700">Regarding Product:</label>
                    <p className="text-sm text-gray-900 mt-1">{selectedMessage.productName}</p>
                  </div>
                )}

                <div>
                  <label className="text-sm font-medium text-gray-700">Message:</label>
                  <div className="mt-2 p-4 bg-gray-50 rounded-lg border border-gray-200">
                    <p className="text-sm text-gray-900 whitespace-pre-wrap">
                      {selectedMessage.messageContent}
                    </p>
                  </div>
                </div>

                <div>
                  <label className="text-sm font-medium text-gray-700">Received:</label>
                  <p className="text-sm text-gray-600 mt-1">
                    {formatDate(selectedMessage.createdAt)}
                  </p>
                </div>
              </div>
            )}

            <DialogFooter className="mt-6">
              <Button
                variant="outline"
                onClick={() => setMessageDialogOpen(false)}
                className="border-gray-300"
              >
                Close
              </Button>
              {selectedMessage && (
                <Button
                  className="bg-[#1e4d5c] hover:bg-[#163a47] text-white flex items-center gap-2"
                  onClick={() => {
                    const gmailUrl = `https://mail.google.com/mail/?view=cm&fs=1&to=${encodeURIComponent(selectedMessage.senderEmail)}`;
                    window.open(gmailUrl, '_blank');
                    setMessageDialogOpen(false);
                  }}
                >
                  <ExternalLink className="w-4 h-4" />
                  Reply via Email
                </Button>
              )}
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </main>
    </div>
  );
}
