import React, { useState, useEffect } from "react";
import { IndustrySidebar } from "../../components/IndustrySidebar";
import { Calendar, CheckCircle, XCircle, Eye, Download, AlertCircle, Loader2 } from "lucide-react";
import { Button } from "../../components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../../components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../../components/ui/table";
import { Badge } from "../../components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "../../components/ui/dialog";
import { Textarea } from "../../components/ui/textarea";
import { Label } from "../../components/ui/label";
import { verificationApi } from "../../lib/api";
import { TableSearch } from "../../components/TableSearch";
import { TablePagination } from "../../components/TablePagination";
import { showSuccess, showError, showWarning, showInfo } from "../../lib/toast";

const API_BASE_URL = process.env.REACT_APP_API_URL || "http://localhost:8080";

export default function IndustryVerificationPage() {
  const [requests, setRequests] = useState([]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [rejectionReason, setRejectionReason] = useState("");
  const [requestToReject, setRequestToReject] = useState(null);

  // Fetch verifications from backend
  useEffect(() => {
    const fetchVerifications = async () => {
      setLoading(true);
      try {
        const data = await verificationApi.getAll(page, pageSize, search);
        
        // Debug logging
        if (process.env.NODE_ENV === 'development') {
          console.log("DEBUG: Fetched verifications:", {
            totalElements: data.totalElements,
            totalPages: data.totalPages,
            contentCount: data.content?.length || 0,
            content: data.content?.map(v => ({
              id: v.verificationId,
              supplierName: v.supplierName,
              assignedIndustryId: v.assignedIndustryId,
              assignedIndustryName: v.assignedIndustryName,
              status: v.status
            }))
          });
        }
        
        // Filter out NOT_SUBMITTED and transform to VerificationRequest format
        // Keep all other statuses (PENDING, APPROVED, REJECTED) visible in the table
        const submittedVerifications = data.content
          .filter(v => v.status !== 'NOT_SUBMITTED')
          .map((v) => ({
            id: v.verificationId || '',
            supplierName: v.supplierName,
            email: v.email,
            businessType: v.businessType || 'N/A',
            submittedDate: v.submittedDate ? new Date(v.submittedDate).toLocaleDateString() : 'N/A',
            status: v.status === 'PENDING' ? 'pending' :
                    v.status === 'APPROVED' ? 'approved' : 'rejected',
            rejectionReason: v.rejectionReason,
            assignedIndustryId: v.assignedIndustryId,
            assignedIndustryName: v.assignedIndustryName,
            documents: {
              businessLicense: v.businessLicenseUrl || '',
              taxCertificate: v.taxCertificateUrl || '',
              bankStatement: v.bankStatementUrl || '',
              identityProof: v.identityProofUrl || '',
            },
          }));
        
        if (process.env.NODE_ENV === 'development') {
          console.log("DEBUG: Filtered verifications:", {
            count: submittedVerifications.length,
            verifications: submittedVerifications.map(v => ({
              id: v.id,
              supplierName: v.supplierName,
              assignedIndustryId: v.assignedIndustryId,
              status: v.status
            }))
          });
        }
        
        setRequests(submittedVerifications);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      } catch (err) {
        console.error("Failed to load verifications:", err);
        if (process.env.NODE_ENV === 'development') {
          console.error("Error details:", {
            message: err.message,
            response: err.response?.data,
            status: err.response?.status
          });
        }
        setRequests([]);
      } finally {
        setLoading(false);
      }
    };

    fetchVerifications();
  }, [page, pageSize, search]);

  const handleApprove = async (id) => {
    try {
      await verificationApi.review(id, {
        status: 'APPROVED',
      });
      
      // Immediately reload verifications to reflect the updated status
      const data = await verificationApi.getAll(page, pageSize, search);
      const submittedVerifications = data.content
        .filter(v => v.status !== 'NOT_SUBMITTED')
        .map((v) => ({
          id: v.verificationId || '',
          supplierName: v.supplierName,
          email: v.email,
          businessType: v.businessType || 'N/A',
          submittedDate: v.submittedDate ? new Date(v.submittedDate).toLocaleDateString() : 'N/A',
          status: v.status === 'PENDING' ? 'pending' :
                  v.status === 'APPROVED' ? 'approved' : 'rejected',
          rejectionReason: v.rejectionReason,
          assignedIndustryId: v.assignedIndustryId,
          assignedIndustryName: v.assignedIndustryName,
          documents: {
            businessLicense: v.businessLicenseUrl || '',
            taxCertificate: v.taxCertificateUrl || '',
            bankStatement: v.bankStatementUrl || '',
            identityProof: v.identityProofUrl || '',
          },
        }));
      setRequests(submittedVerifications);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
      
      if (selectedRequest?.id === id) {
        setSelectedRequest(null);
      }
      
      // Status is now APPROVED - supplier's products will now be visible on public pages
      showSuccess("Verification approved successfully! Supplier status is now APPROVED and their products are now visible.");
    } catch (err) {
      if (process.env.NODE_ENV === 'development') {
        console.error("Failed to approve verification:", err);
      }
      const errorMsg = err.response?.data?.message || err.message || "Failed to approve verification";
      if (err.response?.status === 403) {
        showError("Access denied: You don't have permission to approve verifications.");
      } else {
        showError(`Error: ${errorMsg}`);
      }
    }
  };

  const handleRejectClick = (id) => {
    setRequestToReject(id);
    setRejectionReason("");
    setRejectDialogOpen(true);
  };

  const handleReject = async () => {
    if (!requestToReject || !rejectionReason.trim()) {
      showWarning("Please provide a reason for rejection");
      return;
    }

    try {
      await verificationApi.review(requestToReject, {
        status: 'REJECTED',
        rejectionReason: rejectionReason.trim(),
      });
      
      // Immediately reload verifications to reflect the updated status
      const data = await verificationApi.getAll(page, pageSize, search);
      const submittedVerifications = data.content
        .filter(v => v.status !== 'NOT_SUBMITTED')
        .map((v) => ({
          id: v.verificationId || '',
          supplierName: v.supplierName,
          email: v.email,
          businessType: v.businessType || 'N/A',
          submittedDate: v.submittedDate ? new Date(v.submittedDate).toLocaleDateString() : 'N/A',
          status: v.status === 'PENDING' ? 'pending' :
                  v.status === 'APPROVED' ? 'approved' : 'rejected',
          rejectionReason: v.rejectionReason,
          assignedIndustryId: v.assignedIndustryId,
          assignedIndustryName: v.assignedIndustryName,
          documents: {
            businessLicense: v.businessLicenseUrl || '',
            taxCertificate: v.taxCertificateUrl || '',
            bankStatement: v.bankStatementUrl || '',
            identityProof: v.identityProofUrl || '',
          },
        }));
      setRequests(submittedVerifications);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
      
      if (selectedRequest?.id === requestToReject) {
        setSelectedRequest(null);
      }
      
      setRejectDialogOpen(false);
      setRequestToReject(null);
      setRejectionReason("");
      
      // Status is now REJECTED - supplier's products will be hidden from public pages
      showSuccess("Verification rejected! Supplier has been notified and their products are no longer visible on public pages.");
    } catch (err) {
      if (process.env.NODE_ENV === 'development') {
        console.error("Failed to reject verification:", err);
      }
      const errorMsg = err.response?.data?.message || err.message || "Failed to reject verification";
      if (err.response?.status === 403) {
        showError("Access denied: You don't have permission to reject verifications.");
      } else {
        showError(`Error: ${errorMsg}`);
      }
    }
  };

  /**
   * Constructs the full URL for a document path.
   * Documents are served via ImageController at /api/images?path=...
   */
  const getDocumentUrl = (relativePath) => {
    if (!relativePath || relativePath.trim() === '') {
      return null;
    }
    
    // If already a full URL (starts with http), return as-is
    if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
      return relativePath;
    }
    
    // If it's a relative path (e.g., "verifications/uuid-filename.pdf"), use ImageController
    // The ImageController serves documents at /api/images?path=...
    const encodedPath = encodeURIComponent(relativePath);
    return `${API_BASE_URL}/api/images?path=${encodedPath}`;
  };

  /**
   * Opens a document in a new tab for viewing.
   */
  const handleViewDocument = (relativePath, documentName) => {
    const url = getDocumentUrl(relativePath);
    if (url) {
      window.open(url, '_blank', 'noopener,noreferrer');
    } else {
      showWarning(`Document ${documentName} is not available`);
    }
  };

  /**
   * Downloads a document file.
   */
  const handleDownload = (relativePath, documentName) => {
    const url = getDocumentUrl(relativePath);
    if (!url) {
      showWarning(`Document ${documentName} is not available`);
      return;
    }

    // Create a temporary anchor element to trigger download
    const link = document.createElement('a');
    link.href = url;
    link.download = documentName || 'document';
    link.target = '_blank';
    link.rel = 'noopener noreferrer';
    
    // Append to body, click, and remove
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    showInfo(`Downloading ${documentName || 'document'}...`);
  };

  return (
    <div className="flex min-h-screen bg-gray-50 h-full">
      <IndustrySidebar />

      <main className="flex-1 p-8 min-h-full">
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-3xl font-bold text-gray-800">Verification</h1>
          <div className="flex items-center gap-2 text-sm text-gray-600 bg-white px-4 py-2 rounded-lg border border-gray-200 shadow-sm">
            <Calendar className="w-4 h-4 text-gray-500" />
            <span className="font-medium">12, July 2025</span>
          </div>
        </div>

        {/* Verification Requests Table */}
        <Card className="bg-white rounded-lg shadow-sm border border-gray-200">
          <CardHeader className="pb-4">
            <CardTitle className="text-lg font-semibold text-gray-800">Verification Requests</CardTitle>
            <CardDescription className="text-sm text-gray-600">Review and approve supplier verification documents</CardDescription>
          </CardHeader>
          <CardContent>
            {/* Search */}
            <div className="mb-4">
              <TableSearch
                placeholder="Search verifications by supplier name, email, company, or status..."
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
                        <TableHead className="text-sm font-semibold text-gray-700 py-3">ID</TableHead>
                        <TableHead className="text-sm font-semibold text-gray-700 py-3">Supplier Name</TableHead>
                        <TableHead className="text-sm font-semibold text-gray-700 py-3">Email</TableHead>
                        <TableHead className="text-sm font-semibold text-gray-700 py-3">Company</TableHead>
                        <TableHead className="text-sm font-semibold text-gray-700 py-3">Submitted Date</TableHead>
                        <TableHead className="text-sm font-semibold text-gray-700 py-3">Status</TableHead>
                        <TableHead className="text-sm font-semibold text-gray-700 py-3">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {requests.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={7} className="text-center py-8 text-gray-500">
                            {search ? "No verifications found matching your search" : "No verification requests yet"}
                          </TableCell>
                        </TableRow>
                      ) : (
                        requests.map((request) => (
                          <TableRow key={request.id} className="border-b border-gray-100 hover:bg-gray-50 transition-colors">
                            <TableCell className="font-medium text-gray-700 py-3">{request.id.substring(0, 8)}...</TableCell>
                            <TableCell className="text-gray-700 py-3">{request.supplierName}</TableCell>
                            <TableCell className="text-gray-700 py-3">{request.email}</TableCell>
                            <TableCell className="text-gray-700 py-3">{request.businessType}</TableCell>
                            <TableCell className="text-gray-700 py-3">{request.submittedDate}</TableCell>
                            <TableCell className="py-3">
                              {request.status === "pending" && (
                                <Badge variant="secondary" className="bg-yellow-100 text-yellow-800 border-yellow-200 font-medium">
                                  Pending
                                </Badge>
                              )}
                              {request.status === "approved" && (
                                <Badge variant="secondary" className="bg-green-100 text-green-800 border-green-200 font-medium">
                                  Approved
                                </Badge>
                              )}
                              {request.status === "rejected" && (
                                <Badge variant="secondary" className="bg-red-100 text-red-800 border-red-200 font-medium">
                                  Rejected
                                </Badge>
                              )}
                            </TableCell>
                            <TableCell className="py-3">
                              <div className="flex items-center gap-2">
                                <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={() => setSelectedRequest(request)}
                                  className="text-teal-700 border-teal-700 hover:bg-teal-50"
                                >
                                  <Eye className="w-4 h-4 mr-1" />
                                  View
                                </Button>
                                {request.status === "pending" && (
                                  <>
                                    <Button
                                      size="sm"
                                      onClick={() => handleApprove(request.id)}
                                      className="bg-green-600 hover:bg-green-700 text-white"
                                    >
                                      <CheckCircle className="w-4 h-4 mr-1" />
                                      Approve
                                    </Button>
                                    <Button
                                      size="sm"
                                      onClick={() => handleRejectClick(request.id)}
                                      className="bg-red-600 hover:bg-red-700 text-white"
                                    >
                                      <XCircle className="w-4 h-4 mr-1" />
                                      Reject
                                    </Button>
                                  </>
                                )}
                              </div>
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
          </CardContent>
        </Card>

        {/* Document Viewer Modal */}
        {selectedRequest && (
          <div
            className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
            onClick={() => setSelectedRequest(null)}
          >
            <Card className="w-full max-w-2xl max-h-[80vh] overflow-y-auto bg-white" onClick={(e) => e.stopPropagation()}>
              <CardHeader>
                <CardTitle className="text-lg font-semibold text-gray-800">Verification Documents - {selectedRequest.supplierName}</CardTitle>
                <CardDescription className="text-sm text-gray-600">{selectedRequest.email}</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {Object.entries(selectedRequest.documents).map(([key, value]) => {
                  const documentName = key.replace(/([A-Z])/g, " $1").trim();
                  const documentUrl = getDocumentUrl(value);
                  const hasDocument = value && value.trim() !== '';
                  
                  return (
                    <div key={key} className="flex items-center justify-between p-4 border rounded-lg">
                      <div className="flex items-center gap-3">
                        <Download className="w-5 h-5 text-teal-700" />
                        <div>
                          <p className="font-medium capitalize">{documentName}</p>
                          {hasDocument ? (
                            <p className="text-sm text-gray-500 truncate max-w-md">{value}</p>
                          ) : (
                            <p className="text-sm text-gray-400 italic">No document uploaded</p>
                          )}
                        </div>
                      </div>
                      {hasDocument && documentUrl ? (
                        <div className="flex items-center gap-2">
                          <Button
                            size="sm"
                            variant="outline"
                            className="text-teal-700 border-teal-700 bg-transparent hover:bg-teal-50"
                            onClick={() => handleViewDocument(value, documentName)}
                          >
                            <Eye className="w-4 h-4 mr-1" />
                            View
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            className="text-teal-700 border-teal-700 bg-transparent hover:bg-teal-50"
                            onClick={() => handleDownload(value, documentName)}
                          >
                            <Download className="w-4 h-4 mr-1" />
                            Download
                          </Button>
                        </div>
                      ) : (
                        <span className="text-sm text-gray-400">N/A</span>
                      )}
                    </div>
                  );
                })}
                {selectedRequest.status === "pending" && (
                  <div className="flex gap-3 pt-4">
                    <Button
                      onClick={async () => {
                        await handleApprove(selectedRequest.id);
                        setSelectedRequest(null);
                      }}
                      className="flex-1 bg-green-600 hover:bg-green-700 text-white"
                    >
                      <CheckCircle className="w-4 h-4 mr-2" />
                      Approve
                    </Button>
                    <Button
                      onClick={() => {
                        handleRejectClick(selectedRequest.id);
                      }}
                      className="flex-1 bg-red-600 hover:bg-red-700 text-white"
                    >
                      <XCircle className="w-4 h-4 mr-2" />
                      Reject
                    </Button>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        )}

        {/* Rejection Reason Dialog */}
        <Dialog open={rejectDialogOpen} onOpenChange={setRejectDialogOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <AlertCircle className="w-5 h-5 text-red-600" />
                Reject Verification Request
              </DialogTitle>
              <DialogDescription>
                Please provide a detailed reason for rejection. This message will be sent to the supplier.
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="rejectionReason">Rejection Reason *</Label>
                <Textarea
                  id="rejectionReason"
                  placeholder="Example: Business license is expired. Please upload a valid license issued within the last year. Tax certificate is missing. Bank statement must be from the last 3 months..."
                  value={rejectionReason}
                  onChange={(e) => setRejectionReason(e.target.value)}
                  rows={6}
                  className="resize-none"
                />
                <p className="text-xs text-gray-500">
                  Be specific about what documents are missing or need to be corrected.
                </p>
              </div>
            </div>
            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => {
                  setRejectDialogOpen(false);
                  setRejectionReason("");
                  setRequestToReject(null);
                }}
              >
                Cancel
              </Button>
              <Button
                onClick={handleReject}
                className="bg-red-600 hover:bg-red-700 text-white"
                disabled={!rejectionReason.trim()}
              >
                <XCircle className="w-4 h-4 mr-2" />
                Reject & Notify Supplier
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </main>
    </div>
  );
}
