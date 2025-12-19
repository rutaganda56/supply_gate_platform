import React, { useState, useRef, useEffect } from "react";
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
import { Camera, X, Save } from "lucide-react";
import { showWarning } from "../lib/toast";

export function ProfileEditDialog({
  open,
  onClose,
  onSave,
  initialData,
  initialProfilePicture,
}) {
  const fileInputRef = useRef(null);
  const [profilePicture, setProfilePicture] = useState(initialProfilePicture);
  const [profileData, setProfileData] = useState(initialData);

  // Reset state when dialog opens/closes or initial data changes
  useEffect(() => {
    if (open) {
      setProfileData(initialData);
      setProfilePicture(initialProfilePicture);
    }
  }, [open, initialData, initialProfilePicture]);

  const handleProfileChange = (field, value) => {
    setProfileData((prev) => ({ ...prev, [field]: value }));
  };

  const handleProfilePictureChange = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        showWarning("File size must be less than 5MB");
        return;
      }
      const reader = new FileReader();
      reader.onloadend = () => {
        setProfilePicture(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSave = async () => {
    await onSave(profileData, profilePicture);
    onClose();
  };

  const handleClose = () => {
    setProfileData(initialData);
    setProfilePicture(initialProfilePicture);
    onClose();
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-xl font-semibold text-gray-800">
            Edit Profile Information
          </DialogTitle>
          <DialogDescription className="text-sm text-gray-600">
            Update your personal information and profile picture
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6 mt-4">
          {/* Profile Picture */}
          <div className="flex items-center gap-6 pb-6 border-b border-gray-200">
            <div className="relative">
              <img
                src={profilePicture}
                alt="Profile"
                className="w-24 h-24 rounded-full object-cover border-4 border-gray-200"
              />
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="absolute bottom-0 right-0 bg-[#1a3a3a] text-white rounded-full p-2 hover:bg-[#2a4a4a] transition-colors"
              >
                <Camera className="w-4 h-4" />
              </button>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleProfilePictureChange}
                className="hidden"
              />
            </div>
            <div>
              <p className="text-sm font-medium text-gray-700">Profile Picture</p>
              <p className="text-xs text-gray-500">JPG, PNG or GIF. Max size 5MB</p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Full name */}
            <div>
              <Label htmlFor="fullName" className="text-gray-700">
                Full name
              </Label>
              <Input
                id="fullName"
                value={profileData.fullName}
                onChange={(e) => handleProfileChange("fullName", e.target.value)}
                className="mt-2"
              />
            </div>

            {/* Phone number */}
            <div>
              <Label htmlFor="phone" className="text-gray-700">
                Phone number
              </Label>
              <Input
                id="phone"
                type="tel"
                value={profileData.phone}
                onChange={(e) => handleProfileChange("phone", e.target.value)}
                className="mt-2"
              />
            </div>

            {/* Email */}
            <div>
              <Label htmlFor="email" className="text-gray-700">
                Email
              </Label>
              <Input
                id="email"
                type="email"
                value={profileData.email}
                onChange={(e) => handleProfileChange("email", e.target.value)}
                className="mt-2"
              />
            </div>

            {/* Username */}
            <div>
              <Label htmlFor="username" className="text-gray-700">
                Username
              </Label>
              <Input
                id="username"
                value={profileData.username}
                onChange={(e) => handleProfileChange("username", e.target.value)}
                className="mt-2"
              />
            </div>

            {/* Organization */}
            {profileData.organization !== undefined && (
              <div>
                <Label htmlFor="organization" className="text-gray-700">
                  Organization/Store Name
                </Label>
                <Input
                  id="organization"
                  value={profileData.organization || ""}
                  onChange={(e) =>
                    handleProfileChange("organization", e.target.value)
                  }
                  className="mt-2"
                />
              </div>
            )}
          </div>
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
            type="button"
            onClick={handleSave}
            className="bg-[#1a3a3a] hover:bg-[#2a4a4a] text-white"
          >
            <Save className="w-4 h-4 mr-2" />
            Save Changes
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
