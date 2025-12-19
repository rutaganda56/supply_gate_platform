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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "./ui/select";
import { Textarea } from "./ui/textarea";
import { Package, X, Save, Upload, Trash2, Edit } from "lucide-react";
import { showWarning } from "../lib/toast";

const API_BASE_URL = process.env.REACT_APP_API_URL || "http://localhost:8080";

export function ProductDialog({
  open,
  onClose,
  onSubmit,
  categories = [],
  stores = [],
  productId,
  initialData,
}) {
  const isEditMode = !!productId && !!initialData;
  
  const [formData, setFormData] = useState({
    categoryId: "",
    storeId: "",
    productName: "",
    productDescription: "",
    productPrice: "",
    quantity: "",
  });
  const [errors, setErrors] = useState({});
  const [selectedImages, setSelectedImages] = useState([]);
  const [imagePreviews, setImagePreviews] = useState([]);
  const [existingImageUrls, setExistingImageUrls] = useState([]);

  // Load initial data when in edit mode
  useEffect(() => {
    if (isEditMode && initialData) {
      setFormData({
        categoryId: initialData.categoryId || "",
        storeId: initialData.storeId || "",
        productName: initialData.productName || "",
        productDescription: initialData.productDescription || "",
        productPrice: initialData.productPrice?.toString() || "",
        quantity: initialData.quantity || "",
      });
      if (initialData.imageUrls && initialData.imageUrls.length > 0) {
        setExistingImageUrls(initialData.imageUrls);
      }
    } else {
      setFormData({
        categoryId: "",
        storeId: "",
        productName: "",
        productDescription: "",
        productPrice: "",
        quantity: "",
      });
      setExistingImageUrls([]);
    }
    setSelectedImages([]);
    setImagePreviews([]);
    setErrors({});
  }, [isEditMode, initialData, open]);

  const validate = () => {
    const newErrors = {};

    if (!formData.categoryId) {
      newErrors.categoryId = "Category is required";
    }

    if (!formData.storeId) {
      newErrors.storeId = "Store is required";
    }

    if (!formData.productName.trim()) {
      newErrors.productName = "Product name is required";
    }

    if (!formData.productDescription.trim()) {
      newErrors.productDescription = "Product description is required";
    }

    if (!formData.productPrice.trim()) {
      newErrors.productPrice = "Product price is required";
    } else {
      const price = parseFloat(formData.productPrice);
      if (isNaN(price) || price <= 0) {
        newErrors.productPrice = "Price must be a positive number";
      }
    }

    if (!formData.quantity.trim()) {
      newErrors.quantity = "Quantity is required";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files || []);
    
    // Validate file types and sizes
    const validFiles = [];
    const maxSize = 5 * 1024 * 1024; // 5MB
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif'];
    
    files.forEach((file) => {
      if (!allowedTypes.includes(file.type)) {
        showWarning(`${file.name} is not a valid image type. Allowed: JPG, PNG, WEBP, GIF`);
        return;
      }
      if (file.size > maxSize) {
        showWarning(`${file.name} is too large. Maximum size is 5MB`);
        return;
      }
      validFiles.push(file);
    });
    
    // Limit to 5 images
    const totalImages = selectedImages.length + validFiles.length;
    if (totalImages > 5) {
      showWarning('Maximum 5 images allowed');
      return;
    }
    
    setSelectedImages([...selectedImages, ...validFiles]);
    
    // Create previews
    validFiles.forEach((file) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreviews((prev) => [...prev, reader.result]);
      };
      reader.readAsDataURL(file);
    });
  };
  
  const removeImage = (index) => {
    setSelectedImages(selectedImages.filter((_, i) => i !== index));
    setImagePreviews(imagePreviews.filter((_, i) => i !== index));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validate()) {
      onSubmit({
        categoryId: formData.categoryId,
        storeId: formData.storeId,
        productName: formData.productName.trim(),
        productDescription: formData.productDescription.trim(),
        productPrice: parseFloat(formData.productPrice),
        quantity: formData.quantity.trim(),
        images: selectedImages.length > 0 ? selectedImages : undefined,
      });
      setFormData({
        categoryId: "",
        storeId: "",
        productName: "",
        productDescription: "",
        productPrice: "",
        quantity: "",
      });
      setSelectedImages([]);
      setImagePreviews([]);
      setErrors({});
      onClose();
    }
  };

  const handleClose = () => {
    setFormData({
      categoryId: "",
      storeId: "",
      productName: "",
      productDescription: "",
      productPrice: "",
      quantity: "",
    });
    setSelectedImages([]);
    setImagePreviews([]);
    setErrors({});
    onClose();
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-xl font-semibold text-gray-800 flex items-center gap-2">
            {isEditMode ? (
              <>
                <Edit className="w-5 h-5 text-[#1a3a3a]" />
                Update Product
              </>
            ) : (
              <>
                <Package className="w-5 h-5 text-[#1a3a3a]" />
                Create New Product
              </>
            )}
          </DialogTitle>
          <DialogDescription className="text-sm text-gray-600">
            {isEditMode
              ? "Update the product information"
              : "Enter the product information to create a new product"}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <Label htmlFor="categoryId" className="text-gray-700">
                Category <span className="text-red-500">*</span>
              </Label>
              <Select
                value={formData.categoryId}
                onValueChange={(value) => {
                  setFormData({ ...formData, categoryId: value });
                  setErrors({ ...errors, categoryId: "" });
                }}
              >
                <SelectTrigger
                  className={`mt-2 ${
                    errors.categoryId ? "border-red-500" : ""
                  }`}
                >
                  <SelectValue placeholder="Select category" />
                </SelectTrigger>
                <SelectContent>
                  {categories.map((category) => (
                    <SelectItem key={category.id} value={category.id}>
                      {category.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.categoryId && (
                <p className="text-xs text-red-500 mt-1">{errors.categoryId}</p>
              )}
            </div>

            <div>
              <Label htmlFor="storeId" className="text-gray-700">
                Store <span className="text-red-500">*</span>
              </Label>
              <Select
                value={formData.storeId}
                onValueChange={(value) => {
                  setFormData({ ...formData, storeId: value });
                  setErrors({ ...errors, storeId: "" });
                }}
              >
                <SelectTrigger
                  className={`mt-2 ${errors.storeId ? "border-red-500" : ""}`}
                >
                  <SelectValue placeholder="Select store" />
                </SelectTrigger>
                <SelectContent>
                  {stores.map((store) => (
                    <SelectItem key={store.id} value={store.id}>
                      {store.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.storeId && (
                <p className="text-xs text-red-500 mt-1">{errors.storeId}</p>
              )}
            </div>
          </div>

          <div>
            <Label htmlFor="productName" className="text-gray-700">
              Product Name <span className="text-red-500">*</span>
            </Label>
            <Input
              id="productName"
              value={formData.productName}
              onChange={(e) => {
                setFormData({ ...formData, productName: e.target.value });
                setErrors({ ...errors, productName: "" });
              }}
              className={`mt-2 ${errors.productName ? "border-red-500" : ""}`}
              placeholder="Enter product name"
            />
            {errors.productName && (
              <p className="text-xs text-red-500 mt-1">{errors.productName}</p>
            )}
          </div>

          <div>
            <Label htmlFor="productDescription" className="text-gray-700">
              Product Description <span className="text-red-500">*</span>
            </Label>
            <Textarea
              id="productDescription"
              value={formData.productDescription}
              onChange={(e) => {
                setFormData({
                  ...formData,
                  productDescription: e.target.value,
                });
                setErrors({ ...errors, productDescription: "" });
              }}
              className={`mt-2 ${
                errors.productDescription ? "border-red-500" : ""
              }`}
              placeholder="Enter product description"
              rows={4}
            />
            {errors.productDescription && (
              <p className="text-xs text-red-500 mt-1">
                {errors.productDescription}
              </p>
            )}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <Label htmlFor="productPrice" className="text-gray-700">
                Product Price <span className="text-red-500">*</span>
              </Label>
              <Input
                id="productPrice"
                type="number"
                step="0.01"
                min="0"
                value={formData.productPrice}
                onChange={(e) => {
                  setFormData({ ...formData, productPrice: e.target.value });
                  setErrors({ ...errors, productPrice: "" });
                }}
                className={`mt-2 ${errors.productPrice ? "border-red-500" : ""}`}
                placeholder="0.00"
              />
              {errors.productPrice && (
                <p className="text-xs text-red-500 mt-1">
                  {errors.productPrice}
                </p>
              )}
            </div>

            <div>
              <Label htmlFor="quantity" className="text-gray-700">
                Quantity <span className="text-red-500">*</span>
              </Label>
              <Input
                id="quantity"
                value={formData.quantity}
                onChange={(e) => {
                  setFormData({ ...formData, quantity: e.target.value });
                  setErrors({ ...errors, quantity: "" });
                }}
                className={`mt-2 ${errors.quantity ? "border-red-500" : ""}`}
                placeholder="Enter quantity"
              />
              {errors.quantity && (
                <p className="text-xs text-red-500 mt-1">{errors.quantity}</p>
              )}
            </div>
          </div>

          {/* Image Upload Section */}
          <div>
            <Label htmlFor="images" className="text-gray-700">
              Product Images (Optional)
            </Label>
            <div className="mt-2">
              <input
                type="file"
                id="images"
                accept="image/jpeg,image/jpg,image/png,image/webp,image/gif"
                multiple
                onChange={handleImageChange}
                className="hidden"
              />
              <label
                htmlFor="images"
                className="flex items-center justify-center w-full px-4 py-3 border-2 border-dashed border-gray-300 rounded-lg cursor-pointer hover:border-[#1a3a3a] transition-colors"
              >
                <Upload className="w-5 h-5 mr-2 text-gray-500" />
                <span className="text-sm text-gray-600">
                  Click to upload images (JPG, PNG, WEBP, GIF - Max 5MB each, up to 5 images)
                </span>
              </label>
            </div>
            
            {/* Existing Images (Edit Mode) */}
            {isEditMode && existingImageUrls.length > 0 && (
              <div className="mt-4">
                <Label className="text-gray-700 mb-2 block">Existing Images</Label>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  {existingImageUrls.map((imageUrl, index) => {
                    const fullImageUrl = imageUrl.startsWith('http') 
                      ? imageUrl 
                      : `${API_BASE_URL}/api/images?path=${encodeURIComponent(imageUrl)}`;
                    return (
                      <div key={index} className="relative group">
                        <img
                          src={fullImageUrl}
                          alt={`Existing ${index + 1}`}
                          className="w-full h-32 object-cover rounded-lg border border-gray-200"
                        />
                        <div className="absolute bottom-2 left-2 text-xs text-white bg-black bg-opacity-50 px-2 py-1 rounded">
                          Existing
                        </div>
                      </div>
                    );
                  })}
                </div>
                <p className="text-xs text-gray-500 mt-2">
                  Upload new images below to add to existing ones
                </p>
              </div>
            )}
            
            {/* New Image Previews */}
            {imagePreviews.length > 0 && (
              <div className="mt-4">
                <Label className="text-gray-700 mb-2 block">New Images</Label>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  {imagePreviews.map((preview, index) => (
                    <div key={index} className="relative group">
                      <img
                        src={preview}
                        alt={`Preview ${index + 1}`}
                        className="w-full h-32 object-cover rounded-lg border border-gray-200"
                      />
                      <button
                        type="button"
                        onClick={() => removeImage(index)}
                        className="absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                      <div className="absolute bottom-2 left-2 text-xs text-white bg-black bg-opacity-50 px-2 py-1 rounded">
                        {selectedImages[index]?.name || `Image ${index + 1}`}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
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
              {isEditMode ? "Update Product" : "Create Product"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
