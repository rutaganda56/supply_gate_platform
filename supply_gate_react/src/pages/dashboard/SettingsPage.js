import React, { useState, useEffect } from "react";
import { Sidebar } from "../../components/Sidebar";
import { Calendar, User, Phone, Mail, CheckCircle, Globe, Loader2 } from "lucide-react";
import { Button } from "../../components/ui/button";
import { Label } from "../../components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../../components/ui/select";
import { ProfileEditDialog } from "../../components/ProfileEditDialog";
import api, { authApi } from "../../lib/api";
import { useSession } from "../../lib/auth-utils";
import { showError, showSuccess } from "../../lib/toast";

export default function SettingsPage() {
  // eslint-disable-next-line no-unused-vars
  const [profilePicture, setProfilePicture] = useState("/professional-man-avatar.png");
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  // eslint-disable-next-line no-unused-vars
  const [locationId] = useState(null); // Used in handleProfileSave

  // Standardized session management
  const { userId, hasRole, loading: authLoading } = useSession('SUPPLIER');

  const [profileData, setProfileData] = useState({
    fullName: "",
    phone: "",
    email: "",
    username: "",
    organization: "",
    profilePicture: "/professional-man-avatar.png",
  });

  const [settingsData, setSettingsData] = useState({
    language: "en",
  });

  useEffect(() => {
    // Don't fetch data if auth is still loading or user doesn't have the right role
    if (authLoading || !hasRole || !userId) {
      if (!authLoading) {
        setLoading(false);
      }
      return;
    }

    const fetchUserData = async () => {
      setLoading(true);
      setError(null);
      try {
        // Get current authenticated user
        const user = await authApi.getCurrentUser();
        
        // Split full name into first and last name
        const nameParts = (user.firstname + " " + user.lastname).trim().split(" ");
        const firstName = nameParts[0] || "";
        const lastName = nameParts.slice(1).join(" ") || nameParts[0] || "";
        
        setProfileData({
          fullName: `${firstName} ${lastName}`.trim(),
          phone: user.phoneNumber || "",
          email: user.email || "",
          username: user.username,
          organization: "",
          profilePicture: "/professional-man-avatar.png",
        });

        // Load saved settings from localStorage
        try {
          const savedSettings = localStorage.getItem("supplierSettings");
          if (savedSettings) {
            const parsed = JSON.parse(savedSettings);
            const { theme, ...settingsWithoutTheme } = parsed;
            setSettingsData(settingsWithoutTheme);
          }
        } catch (parseError) {
          if (process.env.NODE_ENV === 'development') {
            console.warn('Failed to parse supplierSettings from localStorage:', parseError);
          }
          localStorage.removeItem("supplierSettings");
        }

        // Load saved profile picture from localStorage
        try {
          const savedProfile = localStorage.getItem("supplierProfile");
          if (savedProfile) {
            const parsed = JSON.parse(savedProfile);
            if (parsed.profilePicture) {
              setProfilePicture(parsed.profilePicture);
            }
          }
        } catch (parseError) {
          if (process.env.NODE_ENV === 'development') {
            console.warn('Failed to parse supplierProfile from localStorage:', parseError);
          }
          localStorage.removeItem("supplierProfile");
        }
      } catch (err) {
        let errorMessage = 'Failed to load user data';
        
        if (err.message) {
          errorMessage = err.message;
        } else if (typeof err === 'string') {
          errorMessage = err;
        } else if (err.response?.data?.message) {
          errorMessage = err.response.data.message;
        } else if (err.response?.data) {
          errorMessage = typeof err.response.data === 'string' 
            ? err.response.data 
            : JSON.stringify(err.response.data);
        } else if (err.status === 401) {
          errorMessage = 'Your session has expired. Please login again.';
        } else if (err.status === 403) {
          errorMessage = 'Access denied. You do not have permission to view these settings.';
        }
        
        setError(errorMessage);
        
        if (process.env.NODE_ENV === 'development') {
          console.error('User data fetch error:', {
            error: err,
            message: errorMessage,
            status: err.status,
            response: err.response?.data,
          });
        }
        
        // If it's an authentication error, redirect to login after a short delay
        if (err.status === 401 || errorMessage.includes('session has expired') || errorMessage.includes('login again')) {
          setTimeout(() => {
            if (typeof window !== 'undefined') {
              window.location.href = '/login';
            }
          }, 2000);
        }
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, [userId, hasRole, authLoading]);

  const handleSettingsChange = (field, value) => {
    const updated = { ...settingsData, [field]: value };
    setSettingsData(updated);
    
    // Auto-save preferences immediately
    localStorage.setItem("supplierSettings", JSON.stringify(updated));
  };

  // eslint-disable-next-line no-unused-vars
  const handleProfileSave = async (data, picture) => {
    if (!userId) {
      showError("User ID not found. Please login again.");
      return;
    }

    setError(null);
    try {
      // Split full name into first and last name
      const nameParts = data.fullName.trim().split(" ");
      const firstname = nameParts[0] || "";
      const lastname = nameParts.slice(1).join(" ") || nameParts[0] || "";

      // Get current user to preserve existing data
      const currentUser = await authApi.getCurrentUser();

      // Update user via API
      await api.put(`/api/auth/updateUser/${userId}`, {
        username: data.username || currentUser.username,
        password: "",
        userType: "SUPPLIER",
        firstname: firstname,
        lastname: lastname,
        email: data.email || currentUser.email || "",
        phoneNumber: data.phone || "",
        locationId: locationId || undefined,
      });

      // Update local state
      const updatedProfile = { ...data, profilePicture: picture };
      setProfileData(updatedProfile);
      setProfilePicture(picture);
      localStorage.setItem("supplierProfile", JSON.stringify(updatedProfile));
      window.dispatchEvent(new Event("profileUpdated"));
      
      showSuccess("Profile updated successfully!");
      setIsDialogOpen(false);
    } catch (err) {
      const errorMessage = err.message || "Failed to update profile. Please try again.";
      setError(errorMessage);
      showError(errorMessage);
      if (process.env.NODE_ENV === 'development') {
        console.error('Profile update error:', err);
      }
    }
  };

  return (
    <div className="flex min-h-screen bg-gray-100 h-full">
      <Sidebar />
      <main className="flex-1 p-8 min-h-full">
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-3xl font-bold text-gray-800">Settings</h1>
          <div className="flex items-center gap-2 text-sm text-gray-600 bg-white px-4 py-2 rounded-lg border border-gray-200 shadow-sm">
            <Calendar className="h-4 w-4 text-gray-500" />
            <span className="font-medium">12, July 2025</span>
          </div>
        </div>

        {/* Loading State */}
        {loading && (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-[#1a3a3a]" />
            <span className="ml-2 text-gray-600">Loading user data...</span>
          </div>
        )}

        {/* Error State */}
        {error && !loading && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
            {error}
          </div>
        )}

        <div className="space-y-8">
          {/* Profile Section */}
          {!loading && (
          <div className="bg-white rounded-lg p-8 shadow-sm border border-gray-200">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-semibold text-gray-800">Profile Information</h2>
              <Button
                onClick={() => setIsDialogOpen(true)}
                className="bg-[#1a3a3a] hover:bg-[#2a4a4a] text-white"
              >
                Edit Profile
              </Button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Full name */}
              <div>
                <Label className="text-gray-700">Full name</Label>
                <div className="flex items-center gap-3 bg-gray-100 rounded-lg px-4 py-3 mt-2">
                  <User className="h-5 w-5 text-gray-500" />
                  <span className="text-gray-700">{profileData.fullName}</span>
                </div>
              </div>

              {/* Phone number */}
              <div>
                <Label className="text-gray-700">Phone number</Label>
                <div className="flex items-center gap-3 bg-gray-100 rounded-lg px-4 py-3 mt-2">
                  <Phone className="h-5 w-5 text-gray-500" />
                  <span className="text-gray-700">{profileData.phone}</span>
                </div>
              </div>

              {/* Email */}
              <div>
                <Label className="text-gray-700">Email</Label>
                <div className="flex items-center gap-3 bg-gray-100 rounded-lg px-4 py-3 mt-2">
                  <Mail className="h-5 w-5 text-gray-500" />
                  <span className="text-gray-700">{profileData.email}</span>
                </div>
              </div>

              {/* Username */}
              <div>
                <Label className="text-gray-700">Username</Label>
                <div className="flex items-center gap-3 bg-gray-100 rounded-lg px-4 py-3 mt-2">
                  <User className="h-5 w-5 text-gray-500" />
                  <span className="text-gray-700">{profileData.username}</span>
                </div>
              </div>

              {/* Organization */}
              <div>
                <Label className="text-gray-700">Organization/Store Name</Label>
                <div className="flex items-center gap-3 bg-gray-100 rounded-lg px-4 py-3 mt-2">
                  <User className="h-5 w-5 text-gray-500" />
                  <span className="text-gray-700">{profileData.organization || "N/A"}</span>
                </div>
              </div>

              {/* Status */}
              <div>
                <Label className="text-gray-700">Status</Label>
                <div className="flex items-center gap-2 mt-2">
                  <CheckCircle className="h-5 w-5 text-teal-600" />
                  <span className="text-gray-700">Approved</span>
                </div>
              </div>
            </div>
          </div>
          )}

          {/* Profile Edit Dialog */}
          <ProfileEditDialog
            open={isDialogOpen}
            onClose={() => setIsDialogOpen(false)}
            onSave={handleProfileSave}
            initialData={profileData}
            initialProfilePicture={profilePicture}
          />

          {/* Preferences Section */}
          {!loading && (
          <div className="bg-white rounded-lg p-8 shadow-sm border border-gray-200">
            <h2 className="text-xl font-semibold text-gray-800 mb-6">Preferences</h2>

            <div className="space-y-6">
              {/* Language */}
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Globe className="h-5 w-5 text-gray-500" />
                  <div>
                    <Label className="text-gray-900 font-medium">Language</Label>
                    <p className="text-sm text-gray-500">Choose your preferred language</p>
                  </div>
                </div>
                <Select
                  value={settingsData.language}
                  onValueChange={(value) => handleSettingsChange("language", value)}
                >
                  <SelectTrigger className="w-[180px]">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="en">English</SelectItem>
                    <SelectItem value="fr">Français</SelectItem>
                    <SelectItem value="es">Español</SelectItem>
                    <SelectItem value="de">Deutsch</SelectItem>
                    <SelectItem value="zh">中文</SelectItem>
                    <SelectItem value="ar">العربية</SelectItem>
                    <SelectItem value="sw">Kiswahili</SelectItem>
                    <SelectItem value="rw">Kinyarwanda</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>
          )}

        </div>
      </main>
    </div>
  );
}
