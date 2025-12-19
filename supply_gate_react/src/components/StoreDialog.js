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
import { Store, X, Save, Edit } from "lucide-react";

/**
 * Dialog component for creating or updating a store.
 * 
 * SECURITY: User ID is NOT collected or sent from this component.
 * Backend extracts authenticated user from JWT token automatically.
 */
export function StoreDialog({ open, onClose, onSubmit, storeId, initialData }) {
  const isEditMode = !!storeId && !!initialData;
  
  const [formData, setFormData] = useState({
    storeName: "",
    phoneNumber: "",
    storeEmail: "",
  });
  const [errors, setErrors] = useState({});

  // Load initial data when in edit mode
  useEffect(() => {
    if (isEditMode && initialData) {
      setFormData({
        storeName: initialData.storeName || "",
        phoneNumber: initialData.phoneNumber || "",
        storeEmail: initialData.storeEmail || "",
      });
    } else {
      setFormData({
        storeName: "",
        phoneNumber: "",
        storeEmail: "",
      });
    }
    setErrors({});
  }, [isEditMode, initialData, open]);

  const validate = () => {
    const newErrors = {};

    if (!formData.storeName.trim()) {
      newErrors.storeName = "Store name is required";
    }

    if (!formData.phoneNumber.trim()) {
      newErrors.phoneNumber = "Phone number is required";
    } else if (!/^[0-9]{10}$/.test(formData.phoneNumber)) {
      newErrors.phoneNumber = "Phone number must be exactly 10 digits";
    }

    if (!formData.storeEmail.trim()) {
      newErrors.storeEmail = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.storeEmail)) {
      newErrors.storeEmail = "Please enter a valid email address";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validate()) {
      // SECURITY: Do NOT include userId - backend extracts it from JWT token
      onSubmit({ ...formData });
      setFormData({ storeName: "", phoneNumber: "", storeEmail: "" });
      setErrors({});
      onClose();
    }
  };

  const handleClose = () => {
    setFormData({ storeName: "", phoneNumber: "", storeEmail: "" });
    setErrors({});
    onClose();
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="text-xl font-semibold text-gray-800 flex items-center gap-2">
            {isEditMode ? (
              <>
                <Edit className="w-5 h-5 text-[#1a3a3a]" />
                Update Store
              </>
            ) : (
              <>
                <Store className="w-5 h-5 text-[#1a3a3a]" />
                Create New Store
              </>
            )}
          </DialogTitle>
          <DialogDescription className="text-sm text-gray-600">
            {isEditMode
              ? "Update the store information"
              : "Enter the store information to create a new store"}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          <div>
            <Label htmlFor="storeName" className="text-gray-700">
              Store Name <span className="text-red-500">*</span>
            </Label>
            <Input
              id="storeName"
              value={formData.storeName}
              onChange={(e) =>
                setFormData({ ...formData, storeName: e.target.value })
              }
              className={`mt-2 ${errors.storeName ? "border-red-500" : ""}`}
              placeholder="Enter store name"
            />
            {errors.storeName && (
              <p className="text-xs text-red-500 mt-1">{errors.storeName}</p>
            )}
          </div>

          <div>
            <Label htmlFor="phoneNumber" className="text-gray-700">
              Phone Number <span className="text-red-500">*</span>
            </Label>
            <Input
              id="phoneNumber"
              type="tel"
              value={formData.phoneNumber}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  phoneNumber: e.target.value.replace(/\D/g, ""),
                })
              }
              className={`mt-2 ${errors.phoneNumber ? "border-red-500" : ""}`}
              placeholder="Enter 10-digit phone number"
              maxLength={10}
            />
            {errors.phoneNumber && (
              <p className="text-xs text-red-500 mt-1">{errors.phoneNumber}</p>
            )}
            <p className="text-xs text-gray-500 mt-1">
              Must be exactly 10 digits (numbers only)
            </p>
          </div>

          <div>
            <Label htmlFor="storeEmail" className="text-gray-700">
              Store Email <span className="text-red-500">*</span>
            </Label>
            <Input
              id="storeEmail"
              type="email"
              value={formData.storeEmail}
              onChange={(e) =>
                setFormData({ ...formData, storeEmail: e.target.value })
              }
              className={`mt-2 ${errors.storeEmail ? "border-red-500" : ""}`}
              placeholder="Enter store email"
            />
            {errors.storeEmail && (
              <p className="text-xs text-red-500 mt-1">{errors.storeEmail}</p>
            )}
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
            >
              <Save className="w-4 h-4 mr-2" />
              {isEditMode ? "Update Store" : "Create Store"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
