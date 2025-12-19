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
import { Folder, X, Save, Edit } from "lucide-react";

export function CategoryDialog({
  open,
  onClose,
  onSubmit,
  categoryId,
  initialData,
}) {
  const isEditMode = !!categoryId && !!initialData;
  const [categoryName, setCategoryName] = useState("");
  const [error, setError] = useState("");

  // Load initial data when in edit mode
  useEffect(() => {
    if (isEditMode && initialData) {
      setCategoryName(initialData.categoryName || "");
    } else {
      setCategoryName("");
    }
    setError("");
  }, [isEditMode, initialData, open]);

  const validate = () => {
    if (!categoryName.trim()) {
      setError("Category name is required");
      return false;
    }
    setError("");
    return true;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validate()) {
      onSubmit({ categoryName: categoryName.trim() });
      setCategoryName("");
      setError("");
      onClose();
    }
  };

  const handleClose = () => {
    setCategoryName("");
    setError("");
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
                Update Category
              </>
            ) : (
              <>
                <Folder className="w-5 h-5 text-[#1a3a3a]" />
                Create New Category
              </>
            )}
          </DialogTitle>
          <DialogDescription className="text-sm text-gray-600">
            {isEditMode
              ? "Update the category name"
              : "Enter the category name to create a new category"}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          <div>
            <Label htmlFor="categoryName" className="text-gray-700">
              Category Name <span className="text-red-500">*</span>
            </Label>
            <Input
              id="categoryName"
              value={categoryName}
              onChange={(e) => {
                setCategoryName(e.target.value);
                setError("");
              }}
              className={`mt-2 ${error ? "border-red-500" : ""}`}
              placeholder="Enter category name"
            />
            {error && <p className="text-xs text-red-500 mt-1">{error}</p>}
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
              {isEditMode ? "Update Category" : "Create Category"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
