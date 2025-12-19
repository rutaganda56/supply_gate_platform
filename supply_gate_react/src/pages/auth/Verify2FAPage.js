import React, { useState, useEffect } from "react";
import { useSearchParams, useNavigate, Link } from "react-router-dom";
import { Mail, Loader2, CheckCircle2, AlertCircle, RefreshCw, Shield } from "lucide-react";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { authApi } from "../../lib/api";
import { showSuccess, showError, showInfo, showWarning } from "../../lib/toast";

export default function Verify2FAPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const sessionId = searchParams.get("sessionId");

  const [code, setCode] = useState("");
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [validating, setValidating] = useState(true);
  const [sessionValid, setSessionValid] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState(null);
  const [attemptsRemaining, setAttemptsRemaining] = useState(null);
  const [resendCooldown, setResendCooldown] = useState(0);
  const [username, setUsername] = useState(null);

  // Validate session on mount
  useEffect(() => {
    const validateSession = async () => {
      if (!sessionId) {
        setSessionValid(false);
        setValidating(false);
        setError("Session ID is missing. Please login again.");
        return;
      }

      try {
        const result = await authApi.validate2FASession(sessionId);
        setSessionValid(result.valid);
        if (!result.valid) {
          const errorMsg = result.status || "Session is invalid or has expired. Please login again.";
          setError(errorMsg);
          showError(errorMsg);
        }
      } catch (err) {
        setSessionValid(false);
        const errorMsg = "Failed to validate session. Please login again.";
        setError(errorMsg);
        showError(errorMsg);
      } finally {
        setValidating(false);
      }
    };

    validateSession();
  }, [sessionId]);

  // Get user info from localStorage if available
  useEffect(() => {
    if (typeof window !== "undefined") {
      const storedUsername = localStorage.getItem("2fa_username");
      if (storedUsername) setUsername(storedUsername);
    }
  }, []);

  // Resend cooldown timer
  useEffect(() => {
    if (resendCooldown > 0) {
      const timer = setTimeout(() => setResendCooldown(resendCooldown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [resendCooldown]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setAttemptsRemaining(null);

    if (!code || code.length !== 6) {
      const errorMsg = "Please enter a valid 6-digit code.";
      setError(errorMsg);
      showWarning(errorMsg);
      return;
    }

    if (!sessionId) {
      const errorMsg = "Session ID is missing. Please login again.";
      setError(errorMsg);
      showError(errorMsg);
      return;
    }

    setLoading(true);

    try {
      const authResponse = await authApi.verify2FA(sessionId, code);
      
      // Store tokens
      if (typeof window !== "undefined") {
        localStorage.setItem("token", authResponse.accessToken);
        localStorage.setItem("refreshToken", authResponse.refreshToken);
        localStorage.setItem("username", authResponse.username);
        localStorage.setItem("userId", authResponse.userId);
        
        // Get userType from localStorage (stored during 2FA initiation) and ensure it's in correct format
        const storedUserType = localStorage.getItem("userType");
        if (storedUserType) {
          // Convert to uppercase format if needed
          const normalizedUserType = storedUserType === "industry" || storedUserType === "INDUSTRY_WORKER" 
            ? "INDUSTRY_WORKER" 
            : "SUPPLIER";
          localStorage.setItem("userType", normalizedUserType);
        } else {
          // Fallback: default to SUPPLIER if not found
          localStorage.setItem("userType", "SUPPLIER");
        }
        
        // Clear 2FA session data
        localStorage.removeItem("2fa_sessionId");
        localStorage.removeItem("2fa_userId");
        localStorage.removeItem("2fa_username");
      }

      setSuccess(true);
      showSuccess("Verification successful! Redirecting...");
      
      // Redirect based on user type
      setTimeout(() => {
        const userType = localStorage.getItem("userType");
        if (userType === "INDUSTRY_WORKER") {
          navigate("/industryDashBoard");
        } else {
          navigate("/dashboard");
        }
      }, 2000);
    } catch (err) {
      const errorMessage = err.response?.data?.message || 
                          err.response?.data || 
                          err.message || 
                          "Invalid verification code. Please try again.";
      
      setError(errorMessage);
      showError(errorMessage);
      
      // Extract attempts remaining from error message
      const attemptsMatch = errorMessage.match(/(\d+) attempts remaining/i);
      if (attemptsMatch) {
        setAttemptsRemaining(parseInt(attemptsMatch[1]));
      }
      
      // Clear code on error
      setCode("");
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!sessionId || resendCooldown > 0) return;

    setResending(true);
    setError(null);
    setCode("");

    try {
      const result = await authApi.resend2FACode(sessionId);
      
      // Update session ID if new one is returned
      if (result.sessionId && result.sessionId !== sessionId) {
        // Update URL without page reload
        const newUrl = new URL(window.location.href);
        newUrl.searchParams.set("sessionId", result.sessionId);
        window.history.replaceState({}, "", newUrl.toString());
      }
      
      // Set cooldown (60 seconds)
      setResendCooldown(60);
      
      // Show success message
      showSuccess("Verification code has been resent to your email. Please check your inbox.");
      setError(null);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        err.message ||
        "Failed to resend code. Please try again."
      );
    } finally {
      setResending(false);
    }
  };

  const handleCodeChange = (value) => {
    // Only allow digits and limit to 6 characters
    const digitsOnly = value.replace(/\D/g, "").slice(0, 6);
    setCode(digitsOnly);
    setError(null);
  };

  if (validating) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <Loader2 className="w-8 h-8 animate-spin text-[#1e4d5c] mx-auto mb-4" />
          <p className="text-sm text-gray-600">Validating session...</p>
        </div>
      </div>
    );
  }

  if (!sessionValid) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
        <div className="w-full max-w-md">
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <AlertCircle className="w-16 h-16 text-red-600 mx-auto mb-4" />
            <h1 className="text-2xl font-semibold text-gray-900 mb-2">
              Session Expired
            </h1>
            <p className="text-sm text-gray-600 mb-6">
              {error || "Your verification session has expired. Please login again."}
            </p>
            <Link to="/login">
              <Button className="w-full bg-[#1e4d5c] hover:bg-[#163d49] text-white">
                Back to Login
              </Button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
        <div className="w-full max-w-md">
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <CheckCircle2 className="w-16 h-16 text-green-600 mx-auto mb-4" />
            <h1 className="text-2xl font-semibold text-gray-900 mb-2">
              Verification Successful!
            </h1>
            <p className="text-sm text-gray-600 mb-6">
              Your account has been verified. Redirecting to your dashboard...
            </p>
            <Loader2 className="w-6 h-6 animate-spin text-[#1e4d5c] mx-auto" />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="w-full max-w-md">
        {/* Back to Login */}
        <Link
          to="/login"
          className="inline-flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 mb-6"
        >
          ← Back to Login
        </Link>

        {/* Card */}
        <div className="bg-white rounded-lg shadow-md p-8">
          <div className="text-center mb-6">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-[#1e4d5c] rounded-full mb-4">
              <Shield className="w-8 h-8 text-white" />
            </div>
            <h1 className="text-2xl font-semibold text-gray-900 mb-2">
              Two-Factor Authentication
            </h1>
            <p className="text-sm text-gray-600">
              {username ? `Enter the 6-digit code sent to your email for ${username}` : "Enter the 6-digit code sent to your email"}
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm">
                {error}
              </div>
            )}

            {attemptsRemaining !== null && attemptsRemaining > 0 && (
              <div className="bg-yellow-50 border border-yellow-200 text-yellow-700 px-4 py-3 rounded-md text-sm">
                {attemptsRemaining} {attemptsRemaining === 1 ? "attempt" : "attempts"} remaining
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="code" className="text-sm font-medium text-gray-700">
                Verification Code
              </Label>
              <Input
                id="code"
                type="text"
                inputMode="numeric"
                pattern="[0-9]*"
                value={code}
                onChange={(e) => handleCodeChange(e.target.value)}
                placeholder="000000"
                required
                maxLength={6}
                disabled={loading}
                className="text-center text-2xl font-mono tracking-widest border-gray-300"
                autoFocus
                autoComplete="one-time-code"
              />
              <p className="text-xs text-gray-500 text-center">
                Enter the 6-digit code from your email
              </p>
            </div>

            <Button
              type="submit"
              disabled={loading || code.length !== 6}
              className="w-full bg-[#1e4d5c] hover:bg-[#163d49] text-white"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin mr-2" />
                  Verifying...
                </>
              ) : (
                <>
                  <Shield className="w-4 h-4 mr-2" />
                  Verify Code
                </>
              )}
            </Button>
          </form>

          <div className="mt-6 space-y-3">
            <Button
              type="button"
              variant="outline"
              onClick={handleResend}
              disabled={resending || resendCooldown > 0}
              className="w-full border-gray-300"
            >
              {resending ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin mr-2" />
                  Sending...
                </>
              ) : resendCooldown > 0 ? (
                <>
                  <RefreshCw className="w-4 h-4 mr-2" />
                  Resend Code ({resendCooldown}s)
                </>
              ) : (
                <>
                  <Mail className="w-4 h-4 mr-2" />
                  Resend Code
                </>
              )}
            </Button>

            <div className="text-center">
              <p className="text-sm text-gray-600">
                Didn't receive the code?{" "}
                <button
                  type="button"
                  onClick={handleResend}
                  disabled={resendCooldown > 0}
                  className="text-[#1e4d5c] hover:underline font-medium disabled:text-gray-400"
                >
                  Resend
                </button>
              </p>
            </div>
          </div>

          {/* Security Notice */}
          <div className="mt-6 text-center">
            <p className="text-xs text-gray-500">
              This code will expire in 10 minutes. Maximum 5 attempts allowed.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
