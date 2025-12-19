import React, { useState, useEffect } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "./ui/dialog";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { FileText, Upload, CheckCircle2, X, Building2, Loader2 } from "lucide-react";
import axios from "axios";
import { showError, showWarning } from "../lib/toast";

const API_BASE_URL = process.env.REACT_APP_API_URL || "http://localhost:8080";

export function VerificationDialog({
  open,
  onClose,
  onSubmit,
  isResubmission = false,
}) {
  const [files, setFiles] = useState({});
  const [industries, setIndustries] = useState([]);
  const [selectedIndustryId, setSelectedIndustryId] = useState("");
  const [loadingIndustries, setLoadingIndustries] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");

  // Load industries when dialog opens
  useEffect(() => {
    if (open) {
      loadIndustries();
    }
  }, [open]);

  const loadIndustries = async () => {
    setLoadingIndustries(true);
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(
        `${API_BASE_URL}/api/auth/industries`,
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : undefined,
          },
        }
      );
      setIndustries(response.data || []);
    } catch (error) {
      if (process.env.NODE_ENV === 'development') {
        console.error("Failed to load industries:", error);
      }
      showError("Failed to load industries. Please try again.");
    } finally {
      setLoadingIndustries(false);
    }
  };

  // Filter industries based on search term
  const filteredIndustries = industries.filter((industry) =>
    (industry.companyName || "").toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleFileChange = (type, file) => {
    setFiles((prev) => ({ ...prev, [type]: file }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    // Check if industry is selected
    if (!selectedIndustryId || !selectedIndustryId.trim()) {
      showWarning("Please select an industry");
      return;
    }

    // Check if all required files are uploaded
    const requiredFiles = [
      "businessLicense",
      "taxCertificate",
      "bankStatement",
      "identityProof",
    ];
    const missingFiles = requiredFiles.filter(
      (file) => !files[file]
    );

    if (missingFiles.length > 0) {
      showWarning(
        `Please upload all required documents. Missing: ${missingFiles.join(", ")}`
      );
      return;
    }

    const selectedIndustry = industries.find(ind => ind.userId === selectedIndustryId);
    onSubmit({
      companyName: selectedIndustry?.companyName || "",
      assignedIndustryId: selectedIndustryId,
      ...files,
    });
    setFiles({});
    setSelectedIndustryId("");
    setSearchTerm("");
    onClose();
  };

  const handleClose = () => {
    setFiles({});
    setSelectedIndustryId("");
    setSearchTerm("");
    onClose();
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-xl font-semibold text-gray-800">
            {isResubmission
              ? "Resubmit Verification Documents"
              : "Submit Verification Documents"}
          </DialogTitle>
          <DialogDescription className="text-sm text-gray-600">
            {isResubmission
              ? "Please review the feedback and upload corrected documents"
              : "Upload your business documents to become a verified supplier"}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6 mt-4">
          {/* Industry Selection */}
          <div className="space-y-2">
            <Label
              htmlFor="industryId"
              className="flex items-center gap-2 text-sm font-medium text-gray-700"
            >
              <Building2 className="w-4 h-4 text-[#1a3a3a]" />
              Select Industry <span className="text-red-500">*</span>
            </Label>
            <div className="space-y-2">
              <Input
                type="text"
                placeholder="Search industries..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="border-gray-300"
              />
              {loadingIndustries ? (
                <div className="flex items-center gap-2 text-sm text-gray-500 py-2">
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Loading industries...
                </div>
              ) : (
                <select
                  id="industryId"
                  value={selectedIndustryId}
                  onChange={(e) => setSelectedIndustryId(e.target.value)}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#1a3a3a] text-gray-700"
                >
                  <option value="">-- Select an industry --</option>
                  {filteredIndustries.map((industry) => (
                    <option key={industry.userId} value={industry.userId}>
                      {industry.companyName || industry.email}
                    </option>
                  ))}
                </select>
              )}
              {filteredIndustries.length === 0 && !loadingIndustries && searchTerm && (
                <p className="text-xs text-gray-500">No industries found matching "{searchTerm}"</p>
              )}
            </div>
            <p className="text-xs text-gray-500">
              Select the industry that will review your verification documents. Only that industry will see your submission.
            </p>
          </div>

          {/* Business License */}
          <div className="space-y-2">
            <Label
              htmlFor="businessLicense"
              className="flex items-center gap-2 text-sm font-medium text-gray-700"
            >
              <FileText className="w-4 h-4 text-[#1a3a3a]" />
              Business License <span className="text-red-500">*</span>
            </Label>
            <div className="flex items-center gap-3">
              <Input
                id="businessLicense"
                type="file"
                accept=".pdf,.jpg,.jpeg,.png"
                onChange={(e) =>
                  handleFileChange(
                    "businessLicense",
                    e.target.files?.[0]
                  )
                }
                className="flex-1 border-gray-300"
              />
              {files.businessLicense && (
                <CheckCircle2 className="w-5 h-5 text-green-600 flex-shrink-0" />
              )}
            </div>
            <p className="text-xs text-gray-500">
              Upload your valid business license (PDF, JPG, PNG)
            </p>
          </div>

          {/* Tax Certificate */}
          <div className="space-y-2">
            <Label
              htmlFor="taxCertificate"
              className="flex items-center gap-2 text-sm font-medium text-gray-700"
            >
              <FileText className="w-4 h-4 text-[#1a3a3a]" />
              Tax Certificate <span className="text-red-500">*</span>
            </Label>
            <div className="flex items-center gap-3">
              <Input
                id="taxCertificate"
                type="file"
                accept=".pdf,.jpg,.jpeg,.png"
                onChange={(e) =>
                  handleFileChange(
                    "taxCertificate",
                    e.target.files?.[0]
                  )
                }
                className="flex-1 border-gray-300"
              />
              {files.taxCertificate && (
                <CheckCircle2 className="w-5 h-5 text-green-600 flex-shrink-0" />
              )}
            </div>
            <p className="text-xs text-gray-500">
              Upload your tax registration certificate
            </p>
          </div>

          {/* Bank Statement */}
          <div className="space-y-2">
            <Label
              htmlFor="bankStatement"
              className="flex items-center gap-2 text-sm font-medium text-gray-700"
            >
              <FileText className="w-4 h-4 text-[#1a3a3a]" />
              Bank Statement <span className="text-red-500">*</span>
            </Label>
            <div className="flex items-center gap-3">
              <Input
                id="bankStatement"
                type="file"
                accept=".pdf,.jpg,.jpeg,.png"
                onChange={(e) =>
                  handleFileChange("bankStatement", e.target.files?.[0])
                }
                className="flex-1 border-gray-300"
              />
              {files.bankStatement && (
                <CheckCircle2 className="w-5 h-5 text-green-600 flex-shrink-0" />
              )}
            </div>
            <p className="text-xs text-gray-500">
              Recent bank statement (last 3 months)
            </p>
          </div>

          {/* Identity Proof */}
          <div className="space-y-2">
            <Label
              htmlFor="identityProof"
              className="flex items-center gap-2 text-sm font-medium text-gray-700"
            >
              <FileText className="w-4 h-4 text-[#1a3a3a]" />
              Identity Proof <span className="text-red-500">*</span>
            </Label>
            <div className="flex items-center gap-3">
              <Input
                id="identityProof"
                type="file"
                accept=".pdf,.jpg,.jpeg,.png"
                onChange={(e) =>
                  handleFileChange("identityProof", e.target.files?.[0])
                }
                className="flex-1 border-gray-300"
              />
              {files.identityProof && (
                <CheckCircle2 className="w-5 h-5 text-green-600 flex-shrink-0" />
              )}
            </div>
            <p className="text-xs text-gray-500">
              Government-issued ID or passport
            </p>
          </div>

          <DialogFooter className="mt-6">
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              className="border-gray-300"
            >
              <X className="w-4 h-4 mr-2" />
              Cancel
            </Button>
            <Button
              type="submit"
              className="bg-[#1a3a3a] hover:bg-[#2a4a4a] text-white"
              disabled={
                !selectedIndustryId ||
                !files.businessLicense ||
                !files.taxCertificate ||
                !files.bankStatement ||
                !files.identityProof
              }
            >
              <Upload className="w-4 h-4 mr-2" />
              {isResubmission ? "Resubmit Documents" : "Submit for Verification"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
