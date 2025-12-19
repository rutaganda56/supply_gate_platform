import React, { useState, useEffect } from "react";
import { IndustrySidebar } from "../../components/IndustrySidebar";
import { GlobalSearch } from "../../components/GlobalSearch";
import { Calendar, CheckCircle, XCircle, Eye, Loader2 } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../../components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../../components/ui/table";
import { Badge } from "../../components/ui/badge";
import { verificationApi } from "../../lib/api";
import { TableSearch } from "../../components/TableSearch";
import { TablePagination } from "../../components/TablePagination";

export default function IndustryDashboard() {
  const [requests, setRequests] = useState([]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [statusCounts, setStatusCounts] = useState({
    pendingCount: 0,
    approvedCount: 0,
    rejectedCount: 0,
    totalCount: 0
  });

  // Fetch status counts from backend
  useEffect(() => {
    const fetchStatusCounts = async () => {
      try {
        const counts = await verificationApi.getStatusCounts();
        setStatusCounts(counts);
      } catch (err) {
        if (process.env.NODE_ENV === 'development') {
          console.error("Failed to load status counts:", err);
        }
        // Keep default zero counts on error
      }
    };

    fetchStatusCounts();
  }, []); // Fetch counts once on mount

  // Fetch verifications from backend
  useEffect(() => {
    const fetchVerifications = async () => {
      setLoading(true);
      try {
        const data = await verificationApi.getAll(page, pageSize, search);
        
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
        
        // Refresh status counts after fetching verifications to ensure they're up to date
        const counts = await verificationApi.getStatusCounts();
        setStatusCounts(counts);
      } catch (err) {
        if (process.env.NODE_ENV === 'development') {
          console.error("Failed to load verifications:", err);
        }
        setRequests([]);
      } finally {
        setLoading(false);
      }
    };

    fetchVerifications();
  }, [page, pageSize, search]);

  // Use backend counts instead of filtering current page
  const { pendingCount, approvedCount, rejectedCount } = statusCounts;

  return (
    <div className="flex min-h-screen bg-gray-50 h-full">
      <IndustrySidebar />

      <main className="flex-1 p-8 min-h-full">
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-3xl font-bold text-gray-800">Industry Dashboard</h1>
          <div className="flex items-center gap-4">
            {/* Global Search */}
            <div className="hidden md:block w-80">
              <GlobalSearch />
            </div>
            <div className="flex items-center gap-2 text-sm text-gray-600 bg-white px-4 py-2 rounded-lg border border-gray-200 shadow-sm">
              <Calendar className="w-4 h-4 text-gray-500" />
              <span className="font-medium">12, July 2025</span>
            </div>
          </div>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <Card className="bg-white rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow">
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600 mb-3">Pending Requests</p>
                  <p className="text-3xl font-bold text-gray-900">{pendingCount}</p>
                </div>
                <div className="w-14 h-14 bg-yellow-100 rounded-xl flex items-center justify-center shadow-sm">
                  <Eye className="w-7 h-7 text-yellow-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-white rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow">
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600 mb-3">Approved</p>
                  <p className="text-3xl font-bold text-gray-900">{approvedCount}</p>
                </div>
                <div className="w-14 h-14 bg-green-100 rounded-xl flex items-center justify-center shadow-sm">
                  <CheckCircle className="w-7 h-7 text-green-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-white rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow">
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600 mb-3">Rejected</p>
                  <p className="text-3xl font-bold text-gray-900">{rejectedCount}</p>
                </div>
                <div className="w-14 h-14 bg-red-100 rounded-xl flex items-center justify-center shadow-sm">
                  <XCircle className="w-7 h-7 text-red-600" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Verification Requests Table */}
        <Card className="bg-white rounded-lg shadow-sm border border-gray-200">
          <CardHeader className="pb-4">
            <CardTitle className="text-lg font-semibold text-gray-800">Verification Requests</CardTitle>
            <CardDescription className="text-sm text-gray-600">View verification request status</CardDescription>
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
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {requests.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={6} className="text-center py-8 text-gray-500">
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
      </main>
    </div>
  );
}
