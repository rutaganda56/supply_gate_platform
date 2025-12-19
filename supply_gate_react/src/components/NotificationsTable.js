import React, { useState, useEffect } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "./ui/table";
import { notificationApi } from "../lib/api";
import { TableSearch } from "./TableSearch";
import { TablePagination } from "./TablePagination";
import { Loader2, ExternalLink } from "lucide-react";
import { Button } from "./ui/button";

export function NotificationsTable({ notifications: initialNotifications = [] }) {
  const [notifications, setNotifications] = useState(initialNotifications);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(false);

  // Load notifications when pagination/search changes
  useEffect(() => {
    const loadNotifications = async () => {
      setLoading(true);
      try {
        const data = await notificationApi.getAll(page, pageSize, search);
        setNotifications(data.content);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      } catch (err) {
        if (process.env.NODE_ENV === 'development') {
          console.error("Failed to load notifications:", err);
        }
        setNotifications([]);
      } finally {
        setLoading(false);
      }
    };
    loadNotifications();
  }, [page, pageSize, search]);

  const formatDate = (dateString) => {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', { month: '2-digit', day: '2-digit', year: 'numeric' });
    } catch {
      return dateString;
    }
  };

  const handleReply = async (notificationId, notificationMessage) => {
    try {
      await notificationApi.markAsRead(notificationId);
      
      let senderEmail = "";
      
      // First, try to extract email from notification message
      // Format: "New message from {senderName} ({senderEmail}): {subject}"
      if (notificationMessage) {
        // Try to extract email from format: "New message from Name (email@example.com): Subject"
        const emailMatch = notificationMessage.match(/\(([^)]+@[^)]+)\)/);
        if (emailMatch && emailMatch[1]) {
          senderEmail = emailMatch[1].trim();
        } else {
          // Fallback: try to find email pattern anywhere in the message
          const emailPattern = /([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/;
          const fallbackMatch = notificationMessage.match(emailPattern);
          if (fallbackMatch && fallbackMatch[1]) {
            senderEmail = fallbackMatch[1].trim();
          }
        }
      }
      
      // If not found in message, try to fetch from backend
      if (!senderEmail) {
        try {
          senderEmail = await notificationApi.getSenderEmail(notificationId);
        } catch (err) {
          // If backend can't find it, continue with empty email
          if (process.env.NODE_ENV === 'development') {
            console.warn("Could not fetch sender email from backend:", err);
          }
        }
      }
      
      // Open Gmail compose with the sender email
      if (senderEmail) {
        const gmailUrl = `https://mail.google.com/mail/?view=cm&fs=1&to=${encodeURIComponent(senderEmail)}`;
        window.open(gmailUrl, '_blank');
      } else {
        // Fallback: if we can't extract email, open Gmail compose without recipient
        window.open('https://mail.google.com/mail/?view=cm&fs=1', '_blank');
      }
      
      // Reload notifications to update read status
      const data = await notificationApi.getAll(page, pageSize, search);
      setNotifications(data.content);
    } catch (err) {
      if (process.env.NODE_ENV === 'development') {
        console.error("Failed to handle reply:", err);
      }
    }
  };
  
  return (
    <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
      <h3 className="font-semibold text-lg mb-6 text-gray-800">Notification Details</h3>
      
      {/* Search */}
      <div className="mb-4">
        <TableSearch
          placeholder="Search notifications by message or type..."
          onSearch={(searchTerm) => {
            setSearch(searchTerm);
            setPage(0); // Reset to first page when searching
          }}
          className="max-w-md"
        />
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-8">
          <Loader2 className="w-6 h-6 animate-spin text-[#1e4d5c]" />
        </div>
      ) : (
        <>
          <div className="overflow-x-auto mb-4">
            <Table>
              <TableHeader>
                <TableRow className="border-b border-gray-200 hover:bg-transparent">
                  <TableHead className="text-sm font-semibold text-gray-700 py-3">Notification Id</TableHead>
                  <TableHead className="text-sm font-semibold text-gray-700 py-3">user name</TableHead>
                  <TableHead className="text-sm font-semibold text-gray-700 py-3">message</TableHead>
                  <TableHead className="text-sm font-semibold text-gray-700 py-3">date</TableHead>
                  <TableHead className="text-sm font-semibold text-gray-700 py-3">action</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {notifications.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center py-8 text-gray-500">
                      {search ? "No notifications found matching your search" : "No notifications yet"}
                    </TableCell>
                  </TableRow>
                ) : (
                  notifications.map((notification) => (
                    <TableRow key={notification.notificationId} className="border-b border-gray-100 hover:bg-gray-50 transition-colors">
                      <TableCell className="text-sm text-gray-700 py-3 font-medium">{notification.notificationId.substring(0, 8)}...</TableCell>
                      <TableCell className="text-sm text-gray-700 py-3">{notification.userName}</TableCell>
                      <TableCell className="text-sm text-gray-700 py-3">{notification.message}</TableCell>
                      <TableCell className="text-sm text-gray-600 py-3">
                        {formatDate(notification.createdAt)}
                      </TableCell>
                      <TableCell className="text-sm text-gray-700 py-3">
                        {notification.type === "message" ? (
                          <Button
                            size="sm"
                            className="bg-[#1e4d5c] hover:bg-[#163a47] text-white text-xs px-3 py-1 h-7 flex items-center gap-1"
                            onClick={() => handleReply(notification.notificationId, notification.message)}
                          >
                            <ExternalLink className="h-3 w-3" />
                            Reply via Gmail
                          </Button>
                        ) : (
                          <Button
                            size="sm"
                            variant="outline"
                            className="text-xs px-3 py-1 h-7"
                            onClick={async () => {
                              await notificationApi.markAsRead(notification.notificationId);
                              const data = await notificationApi.getAll(page, pageSize, search);
                              setNotifications(data.content);
                            }}
                            disabled={notification.isRead}
                          >
                            {notification.isRead ? "Read" : "Mark Read"}
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>

          {/* Pagination */}
          <TablePagination
            currentPage={page}
            totalPages={totalPages}
            totalElements={totalElements}
            pageSize={pageSize}
            onPageChange={setPage}
            onPageSizeChange={(size) => {
              setPageSize(size);
              setPage(0);
            }}
            loading={loading}
          />
        </>
      )}
    </div>
  );
}
