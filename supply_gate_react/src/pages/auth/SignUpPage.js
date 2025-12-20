import React, { useState } from "react";
import { User, Phone, Mail, Lock, Eye, EyeOff, ArrowRight, Loader2 } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { authApi } from "../../lib/api";
import { LocationSelector } from "../../components/LocationSelector";
import { showSuccess } from "../../lib/toast";
export default function SignUpPage() {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [formData, setFormData] = useState({
    username: "",
    firstname: "",
    lastname: "",
    email: "",
    phoneNumber: "",
    password: "",
    confirmPassword: "",
  });
  const [locationId, setLocationId] = useState(null);
  const navigate = useNavigate();

  /**
   * Handle form submission
   * Validates form data and calls registration API
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      // Validation
      if (!formData.username.trim()) {
        setError("Username or email is required");
        setLoading(false);
        return;
      }
      
      // If username looks like an email, validate email format
      const usernameInput = formData.username.trim();
      if (usernameInput.includes("@")) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(usernameInput)) {
          setError("Please enter a valid email address");
          setLoading(false);
          return;
        }
      }
      if (!formData.firstname.trim() || !formData.lastname.trim()) {
        setError("First name and last name are required");
        setLoading(false);
        return;
      }
      if (!formData.email.trim()) {
        setError("Email is required");
        setLoading(false);
        return;
      }
      if (!/^[0-9]{10}$/.test(formData.phoneNumber)) {
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

      // Call registration API
      await authApi.register({
        username: formData.username.trim(),
        password: formData.password,
        userType: "SUPPLIER", // Matches UserEnum.SUPPLIER
        firstname: formData.firstname.trim(),
        lastname: formData.lastname.trim(),
        email: formData.email.trim(),
        phoneNumber: formData.phoneNumber,
        locationId: locationId,
      });

      // Success - redirect to login
      showSuccess("Registration successful! Please login.");
      navigate("/login");
    } catch (err) {
      const errorMessage = err.message || err.username || err.email || "Registration failed. Please try again.";
      setError(errorMessage);
      
      if (process.env.NODE_ENV === 'development') {
        console.error("Registration error:", err);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">
      {/* Left teal sidebar */}
      <div className="w-[180px] bg-[#1e4d5c] hidden md:block" />

      {/* Right content area */}
      <div className="flex-1 flex items-center justify-center bg-white p-8">
        <div className="w-full max-w-md">
          {/* Title */}
          <h1 className="text-3xl font-medium text-center mb-10 text-gray-800">Supplier Registration</h1>
          {/* Error Message */}
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
              {error}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Username field - can be username or email */}
            <div className="relative">
              <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500">
                <User size={18} />
              </div>
              <input
                type="text"
                placeholder="Username or Email"
                value={formData.username}
                onChange={(e) => {
                  setFormData({ ...formData, username: e.target.value });
                  setError(null);
                }}
                required
                disabled={loading}
                className="w-full pl-12 pr-4 py-3 border-b border-gray-300 focus:border-gray-500 focus:outline-none text-gray-600 placeholder:text-gray-400 disabled:opacity-50"
              />
            </div>
            <p className="text-xs text-gray-500 -mt-3 mb-2">
              You can use your email address as your username
            </p>

            {/* First name field */}
            <div className="relative">
              <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500">
                <User size={18} />
              </div>
              <input
                type="text"
                placeholder="first name"
                value={formData.firstname}
                onChange={(e) => {
                  setFormData({ ...formData, firstname: e.target.value });
                  setError(null);
                }}
                required
                disabled={loading}
                className="w-full pl-12 pr-4 py-3 border-b border-gray-300 focus:border-gray-500 focus:outline-none text-gray-600 placeholder:text-gray-400 disabled:opacity-50"
              />
            </div>

            {/* Last name field */}
            <div className="relative">
              <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500">
                <User size={18} />
              </div>
              <input
                type="text"
                placeholder="last name"
                value={formData.lastname}
                onChange={(e) => {
                  setFormData({ ...formData, lastname: e.target.value });
                  setError(null);
                }}
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
                placeholder="phone number (10 digits)"
                value={formData.phoneNumber}
                onChange={(e) => {
                  const value = e.target.value.replace(/\D/g, "").slice(0, 10);
                  setFormData({ ...formData, phoneNumber: value });
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
                placeholder="email"
                value={formData.email}
                onChange={(e) => {
                  setFormData({ ...formData, email: e.target.value });
                  setError(null);
                }}
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
                placeholder="password"
                value={formData.password}
                onChange={(e) => {
                  setFormData({ ...formData, password: e.target.value });
                  setError(null);
                }}
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
                placeholder="confirm password"
                value={formData.confirmPassword}
                onChange={(e) => {
                  setFormData({ ...formData, confirmPassword: e.target.value });
                  setError(null);
                }}
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
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-[#1e4d5c] text-white py-3 rounded-md flex items-center justify-center gap-2 hover:bg-[#163d49] transition-colors mt-8 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Registering...
                </>
              ) : (
                <>
                  sign up
                  <ArrowRight size={18} />
                </>
              )}
            </button>
          </form>
          {/* Login link */}
                    <div className="mt-6 space-y-3">
                      <p className="text-center text-sm text-gray-600">
                        Already have an account?{" "}
                        <Link to="/login?type=supplier" className="text-[#1a3a3a] hover:underline font-medium">
                          Login here
                        </Link>
                      </p>
                      <div className="text-center">
                        <Link to="/industryDashBoard/register" className="text-sm text-gray-500 hover:text-gray-700">
                          Register as an industry worker instead
                        </Link>
                      </div>
                    </div>
        </div>
      </div>
    </div>
  );
}
