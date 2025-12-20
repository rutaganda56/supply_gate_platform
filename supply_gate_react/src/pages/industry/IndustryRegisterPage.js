import React, { useState } from "react";
import { User, Phone, Mail, Lock, Eye, EyeOff, ArrowRight, Building2, Loader2 } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "../../components/ui/button";
import { authApi } from "../../lib/api";
import { LocationSelector } from "../../components/LocationSelector";
import { showSuccess } from "../../lib/toast";

export default function IndustryRegisterPage() {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [formData, setFormData] = useState({
    username: "",
    phone: "",
    email: "",
    password: "",
    confirmPassword: "",
    organization: "",
  });
  const [locationId, setLocationId] = useState(null);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError(null); // Clear error when user types
  };

  /**
   * Handle form submission
   * Validates form data and calls registration API for industry worker
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    
    try {
      // Validation
      if (!formData.username.trim()) {
        setError("Username is required");
        setLoading(false);
        return;
      }

      if (!formData.email.trim()) {
        setError("Email is required");
        setLoading(false);
        return;
      }

      if (!/^[0-9]{10}$/.test(formData.phone)) {
        setError("Phone number must be exactly 10 digits");
        setLoading(false);
        return;
      }

      if (!locationId) {
        setError("Please select your location (District, Sector, Cell, and Village)");
        setLoading(false);
        return;
      }

      if (formData.password.length < 6) {
        setError("Password must be at least 6 characters");
        setLoading(false);
        return;
      }

      if (formData.password !== formData.confirmPassword) {
        setError("Passwords do not match");
        setLoading(false);
        return;
      }

      if (!formData.organization || !formData.organization.trim()) {
        setError("Company/organization name is required");
        setLoading(false);
        return;
      }

      // Call registration API
      // Note: Using "INDUSTRY_WORKER" as userType for industry workers
      // For industry workers, firstName and lastName are optional (companyName is used instead)
      
      // Ensure locationId is a valid UUID string
      if (!locationId) {
        setError("Please select your location (District, Sector, Cell, and Village)");
        setLoading(false);
        return;
      }
      
      // Log the data being sent for debugging
      if (process.env.NODE_ENV === 'development') {
        console.log("Registration data:", {
          username: formData.username.trim(),
          userType: "INDUSTRY_WORKER",
          email: formData.email.trim(),
          phoneNumber: formData.phone,
          locationId: locationId,
          companyName: formData.organization.trim(),
        });
      }
      
      await authApi.register({
        username: formData.username.trim(),
        password: formData.password,
        userType: "INDUSTRY_WORKER", // Use INDUSTRY_WORKER instead of CLIENT
        firstname: "", // Not required for industry workers
        lastname: "", // Not required for industry workers
        email: formData.email.trim(),
        phoneNumber: formData.phone,
        locationId: locationId, // Should be UUID string from LocationSelector
        companyName: formData.organization.trim(),
      });

      // Success - redirect to login
      showSuccess("Registration successful! Please login.");
      navigate("/login?type=industry");
    } catch (err) {
      // Better error handling to show validation errors
      let errorMessage = "Registration failed. Please try again.";
      
      if (err.response) {
        // Server responded with error
        const status = err.response.status;
        const data = err.response.data;
        
        if (status === 400) {
          // Validation error - show specific field errors
          if (typeof data === 'object' && data !== null) {
            // Check if it's a validation error object with field names
            const fieldErrors = Object.keys(data).map(field => {
              const error = Array.isArray(data[field]) ? data[field][0] : data[field];
              return `${field}: ${error}`;
            }).join(', ');
            
            if (fieldErrors) {
              errorMessage = `Validation error: ${fieldErrors}`;
            } else if (data.message) {
              errorMessage = data.message;
            } else if (typeof data === 'string') {
              errorMessage = data;
            }
          } else if (typeof data === 'string') {
            errorMessage = data;
          }
        } else if (data && data.message) {
          errorMessage = data.message;
        } else if (data && typeof data === 'string') {
          errorMessage = data;
        }
      } else if (err.message) {
        errorMessage = err.message;
      }
      
      setError(errorMessage);
      
      if (process.env.NODE_ENV === 'development') {
        console.error("Registration error:", err);
        console.error("Error response:", err.response?.data);
        console.error("Error status:", err.response?.status);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">
      {/* Left teal sidebar */}
      <div className="w-60 bg-[#1e4d5c] hidden md:flex items-center justify-center">
      </div>

      {/* Right content area */}
      <div className="flex-1 flex items-center justify-center bg-white p-8">
        <div className="w-full max-w-md">
          {/* Title */}
          <div className="text-center mb-8">
            <h1 className="text-3xl font-medium mb-2 text-gray-800">
              Industry Worker Registration
            </h1>
            <p className="text-sm text-gray-600">
            </p>
          </div>

          {/* Error Message */}
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
              {error}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Username field */}
            <div className="relative">
              <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500">
                <User size={18} />
              </div>
              <input
                type="text"
                name="username"
                placeholder="Username"
                value={formData.username}
                onChange={handleChange}
                required
                disabled={loading}
                className="w-full pl-12 pr-4 py-3 border-b border-gray-300 focus:border-gray-500 focus:outline-none text-gray-600 placeholder:text-gray-400 disabled:opacity-50"
              />
            </div>

            {/* Organization field */}
            <div className="relative">
              <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500">
                <Building2 size={18} />
              </div>
              <input
                type="text"
                name="organization"
                placeholder="Organization/Company name"
                value={formData.organization}
                onChange={handleChange}
                required
                disabled={loading}
                className="w-full pl-12 pr-4 py-3 border-b border-gray-300 focus:border-gray-500 focus:outline-none text-gray-600 placeholder:text-gray-400 disabled:opacity-50"
              />
            </div>

            {/* Phone number field */}
            <div className="relative">
              <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500">
                <Phone size={18} />
              </div>
              <input
                type="tel"
                name="phone"
                placeholder="Phone number (10 digits)"
                value={formData.phone}
                onChange={(e) => {
                  const value = e.target.value.replace(/\D/g, "").slice(0, 10);
                  setFormData({ ...formData, phone: value });
                  setError(null);
                }}
                required
                disabled={loading}
                maxLength={10}
                className="w-full pl-12 pr-4 py-3 border-b border-gray-300 focus:border-gray-500 focus:outline-none text-gray-600 placeholder:text-gray-400 disabled:opacity-50"
              />
            </div>

            {/* Email field */}
            <div className="relative">
              <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500">
                <Mail size={18} />
              </div>
              <input
                type="email"
                name="email"
                placeholder="Email"
                value={formData.email}
                onChange={handleChange}
                required
                disabled={loading}
                className="w-full pl-12 pr-4 py-3 border-b border-gray-300 focus:border-gray-500 focus:outline-none text-gray-600 placeholder:text-gray-400 disabled:opacity-50"
              />
            </div>

            {/* Location Selector */}
            <div>
              <LocationSelector
                onLocationChange={setLocationId}
                selectedLocationId={locationId}
                error={error && !locationId ? "Location is required" : undefined}
                required
              />
            </div>

            {/* Password field */}
            <div className="relative">
              <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500">
                <Lock size={18} />
              </div>
              <input
                type={showPassword ? "text" : "password"}
                name="password"
                placeholder="Password"
                value={formData.password}
                onChange={handleChange}
                required
                disabled={loading}
                minLength={6}
                className="w-full pl-12 pr-12 py-3 border-b border-gray-300 focus:border-gray-500 focus:outline-none text-gray-600 placeholder:text-gray-400 disabled:opacity-50"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                disabled={loading}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700 disabled:opacity-50"
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>

            {/* Confirm password field */}
            <div className="relative">
              <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500">
                <Lock size={18} />
              </div>
              <input
                type={showConfirmPassword ? "text" : "password"}
                name="confirmPassword"
                placeholder="Confirm password"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
                disabled={loading}
                className="w-full pl-12 pr-12 py-3 border-b border-gray-300 focus:border-gray-500 focus:outline-none text-gray-600 placeholder:text-gray-400 disabled:opacity-50"
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                disabled={loading}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700 disabled:opacity-50"
              >
                {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>

            {/* Sign up button */}
            <Button
              type="submit"
              disabled={loading}
              className="w-full bg-[#1e4d5c] text-white py-3 rounded-md flex items-center justify-center gap-2 hover:bg-[#2a4a4a] transition-colors mt-8 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Registering...
                </>
              ) : (
                <>
                  Register as Industry Worker
                  <ArrowRight size={18} />
                </>
              )}
            </Button>
          </form>

          {/* Login link */}
          <div className="mt-6 space-y-3">
            <p className="text-center text-sm text-gray-600">
              Already have an account?{" "}
              <Link to="/login?type=industry" className="text-[#1a3a3a] hover:underline font-medium">
                Login here
              </Link>
            </p>
            <div className="text-center">
              <Link to="/signUp" className="text-sm text-gray-500 hover:text-gray-700">
                Register as Supplier instead
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
