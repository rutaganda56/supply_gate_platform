import { useState, useEffect } from "react";
import { User, Lock, Eye, EyeOff, ArrowRight, Building2, Loader2 } from "lucide-react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { Button } from "../../components/ui/button";
import { authApi } from "../../lib/api";

export default function LoginPage() {
  const [showPassword, setShowPassword] = useState(false);
  const [userType, setUserType] = useState("supplier");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  useEffect(() => {
    // Check if user is already logged in
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      const storedUserType = localStorage.getItem('userType');
      
      if (token) {
        // User is already logged in, redirect to appropriate dashboard
        if (storedUserType === 'INDUSTRY_WORKER') {
          navigate('/industryDashBoard', { replace: true });
        } else {
          navigate('/dashboard', { replace: true });
        }
        return;
      }
    }
    
    const type = searchParams.get("type");
    if (type === "industry") {
      setUserType("industry");
    }
    
    // Load remembered username if available
    if (typeof window !== 'undefined') {
      const rememberedUsername = localStorage.getItem('rememberedUsername');
      if (rememberedUsername) {
        setUsername(rememberedUsername);
        setRememberMe(true);
      }
    }
  }, [searchParams, navigate]);

  /**
   * Handle login form submission
   * This function:
   * 1. Calls the Spring Boot login API
   * 2. Stores the JWT token in localStorage
   * 3. Fetches user info to get userId
   * 4. Redirects to appropriate dashboard
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      // Validate inputs
      if (!username.trim()) {
        setError("Username is required");
        setLoading(false);
        return;
      }
      if (!password.trim()) {
        setError("Password is required");
        setLoading(false);
        return;
      }

      // Call login API
      const loginResponse = await authApi.login({
        username: username.trim(),
        password: password,
      });

      // Log response for debugging (only in development)
      if (process.env.NODE_ENV === 'development') {
        // eslint-disable-next-line no-console
        console.log('Login response:', loginResponse);
      }

      // Check if 2FA is required
      if ('sessionId' in loginResponse && 'message' in loginResponse && !('accessToken' in loginResponse)) {
        // 2FA is required - redirect to verification page
        const twoFactorResponse = loginResponse;
        if (typeof window !== 'undefined') {
          // Store 2FA session data
          localStorage.setItem('2fa_sessionId', twoFactorResponse.sessionId);
          localStorage.setItem('2fa_userId', twoFactorResponse.userId);
          localStorage.setItem('2fa_username', twoFactorResponse.username);
          // Store userType in uppercase format expected by useSession hook
          localStorage.setItem('userType', userType === "industry" ? "INDUSTRY_WORKER" : "SUPPLIER");
        }
        // Redirect to 2FA verification page
        navigate(`/verify-2fa?sessionId=${twoFactorResponse.sessionId}`);
        return;
      }

      // Normal login (no 2FA) - store tokens
      const authResponse = loginResponse;
      if (typeof window !== 'undefined') {
        localStorage.setItem('token', authResponse.accessToken || authResponse.token);
        localStorage.setItem('refreshToken', authResponse.refreshToken || '');
        localStorage.setItem('username', authResponse.username);
        localStorage.setItem('userId', authResponse.userId || authResponse.userId?.toString());
        
        // Use userType from backend response if available, otherwise use toggle selection
        const backendUserType = authResponse.userType;
        let finalUserType;
        if (backendUserType) {
          // Normalize backend userType to uppercase format
          const normalized = backendUserType.toUpperCase();
          if (normalized === 'SUPPLIER' || normalized === 'INDUSTRY_WORKER') {
            finalUserType = normalized;
          } else if (normalized.includes('INDUSTRY')) {
            finalUserType = 'INDUSTRY_WORKER';
          } else {
            finalUserType = 'SUPPLIER';
          }
        } else {
          // Fallback to toggle selection
          finalUserType = userType === "industry" ? "INDUSTRY_WORKER" : "SUPPLIER";
        }
        
        localStorage.setItem('userType', finalUserType);
        
        // If remember me is checked, also store username
        if (rememberMe) {
          localStorage.setItem('rememberedUsername', authResponse.username);
        } else {
          localStorage.removeItem('rememberedUsername');
        }
        
        // Redirect based on actual user type from backend
        if (finalUserType === "INDUSTRY_WORKER") {
          navigate("/industryDashBoard");
        } else {
          navigate("/dashboard");
        }
      }
    } catch (err) {
      // Handle login errors
      let errorMessage = "Login failed. Please check your credentials.";
      
      if (err.response) {
        // Server responded with error
        const errorData = err.response.data;
        if (typeof errorData === 'string') {
          errorMessage = errorData;
        } else if (errorData?.message) {
          errorMessage = errorData.message;
        } else if (errorData?.error) {
          errorMessage = errorData.error;
        } else {
          errorMessage = err.message || errorMessage;
        }
      } else if (err.message) {
        errorMessage = err.message;
      } else if (err.isNetworkError) {
        errorMessage = "Cannot connect to server. Please check if the backend is running.";
      }
      
      setError(errorMessage);
      
      if (process.env.NODE_ENV === 'development') {
        // eslint-disable-next-line no-console
        console.error("Login error:", err);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">
      {/* Left teal sidebar */}
      <div className={`w-60 hidden md:block ${userType === "industry" ? "bg-[#1a3a3a]" : "bg-[#1e4d5c]"}`} />

      {/* Right content area */}
      <div className="flex-1 flex items-center justify-center bg-white p-8">
        <div className="w-full max-w-md">
          {/* Title */}
          <h1 className="text-3xl font-medium text-center mb-10 text-gray-800">
            Supply Gate
          </h1>

          {/* User Type Toggle */}
          <div className="flex items-center justify-center gap-4 mb-6 p-1 bg-gray-100 rounded-lg">
            <button
              type="button"
              onClick={() => setUserType("supplier")}
              className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
                userType === "supplier"
                  ? "bg-white text-[#1e4d5c] shadow-sm"
                  : "text-gray-600 hover:text-gray-800"
              }`}
            >
              Supplier
            </button>
            <button
              type="button"
              onClick={() => setUserType("industry")}
              className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors flex items-center justify-center gap-1 ${
                userType === "industry"
                  ? "bg-white text-[#1a3a3a] shadow-sm"
                  : "text-gray-600 hover:text-gray-800"
              }`}
            >
              <Building2 className="w-4 h-4" />
              Industry Worker
            </button>
          </div>

          {/* Error Message */}
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
              {error}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Username/Email field */}
            <div className="relative">
              <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500">
                <User size={18} />
              </div>
              <input
                type="text"
                placeholder="username/email"
                value={username}
                onChange={(e) => {
                  setUsername(e.target.value);
                  setError(null); // Clear error when user types
                }}
                disabled={loading}
                className="w-full pl-12 pr-4 py-3 border-b border-gray-300 focus:border-gray-500 focus:outline-none text-gray-600 placeholder:text-gray-400 disabled:opacity-50 disabled:cursor-not-allowed"
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
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  setError(null); // Clear error when user types
                }}
                disabled={loading}
                className="w-full pl-12 pr-12 py-3 border-b border-gray-300 focus:border-gray-500 focus:outline-none text-gray-600 placeholder:text-gray-400 disabled:opacity-50 disabled:cursor-not-allowed"
                required
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

            {/* Remember me & Forgot password */}
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="remember"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  disabled={loading}
                  className={`w-4 h-4 rounded border-gray-400 cursor-pointer disabled:opacity-50 ${
                    userType === "industry"
                      ? "checked:bg-[#1a3a3a] checked:border-[#1a3a3a]"
                      : "checked:bg-[#1e4d5c] checked:border-[#1e4d5c]"
                  }`}
                />
                <label
                  htmlFor="remember"
                  className="text-sm text-gray-600 cursor-pointer"
                >
                  remember me
                </label>
              </div>
              <Link 
                to="/forgot-password" 
                className={`text-sm hover:underline ${
                  userType === "industry" ? "text-[#1a3a3a]" : "text-[#1e4d5c]"
                }`}
              >
                forgot password?
              </Link>
            </div>

            {/* Login button */}
            <Button
              type="submit"
              disabled={loading}
              className={`w-full text-white py-3 rounded-md flex items-center justify-center gap-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed ${
                userType === "industry"
                  ? "bg-[#1a3a3a] hover:bg-[#2a4a4a]"
                  : "bg-[#1e4d5c] hover:bg-[#163d49]"
              }`}
            >
              {loading ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Logging in...
                </>
              ) : (
                <>
                  Login
                  <ArrowRight className="w-5 h-5" />
                </>
              )}
            </Button>
          </form>

          {/* Sign up links */}
          <div className="mt-6 space-y-3">
            {userType === "supplier" ? (
              <p className="text-center text-sm text-gray-600">
                Don't have an account?{" "}
                <Link to="/signUp" className="text-[#1e4d5c] hover:underline font-medium">
                  Sign up as Supplier
                </Link>
              </p>
            ) : (
              <p className="text-center text-sm text-gray-600">
                Don't have an account?{" "}
                <Link 
                  to="/industryDashBoard/register" 
                  className="text-[#1a3a3a] hover:underline font-medium"
                >
                  Register as Industry Worker
                </Link>
              </p>
            )}
            <div className="relative my-4">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300"></div>
              </div>
              <div className="relative flex justify-center text-xs">
                <span className="bg-white px-2 text-gray-500">OR</span>
              </div>
            </div>
            <p className="text-center text-sm text-gray-600">
              {userType === "supplier" ? (
                <>
                  Are you an industry worker?{" "}
                  <Link 
                    to="/industryDashBoard/register" 
                    className="text-[#1a3a3a] hover:underline font-medium"
                  >
                    Register as Industry Worker
                  </Link>
                </>
              ) : (
                <>
                  Are you a supplier?{" "}
                  <Link to="/signUp" className="text-[#1e4d5c] hover:underline font-medium">
                    Sign up as Supplier
                  </Link>
                </>
              )}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
