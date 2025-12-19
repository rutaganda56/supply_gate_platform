import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * Authentication and Authorization Utilities
 * 
 * Provides functions to check user roles and protect routes
 */

/**
 * Hook for standardized session management across supplier and industry pages
 * 
 * This hook ensures consistent authentication checks, role verification,
 * and proper handling of 403 errors (authorization failures) vs 401 errors (authentication failures)
 * 
 * @param requiredRole - The required user role for the page
 * @returns Object with auth state: { isAuthenticated, hasRole, userId, userType, loading }
 */
export function useSession(requiredRole) {
  const navigate = useNavigate();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [hasRole, setHasRole] = useState(false);
  const [userId, setUserId] = useState(null);
  const [userType, setUserType] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (typeof window === 'undefined') {
      setLoading(false);
      return;
    }

    const token = localStorage.getItem('token');
    const storedUserTypeRaw = localStorage.getItem('userType');
    const storedUserId = localStorage.getItem('userId');

    // Check if token exists
    if (!token) {
      setIsAuthenticated(false);
      setHasRole(false);
      setLoading(false);
      navigate('/login');
      return;
    }

    // Normalize userType: convert lowercase to uppercase format
    let storedUserType = null;
    if (storedUserTypeRaw) {
      const normalized = storedUserTypeRaw.toLowerCase();
      if (normalized === 'supplier' || normalized === 'supplier') {
        storedUserType = 'SUPPLIER';
        // Update localStorage to ensure consistency
        localStorage.setItem('userType', 'SUPPLIER');
      } else if (normalized === 'industry' || normalized === 'industry_worker') {
        storedUserType = 'INDUSTRY_WORKER';
        // Update localStorage to ensure consistency
        localStorage.setItem('userType', 'INDUSTRY_WORKER');
      } else if (storedUserTypeRaw === 'SUPPLIER' || storedUserTypeRaw === 'INDUSTRY_WORKER') {
        storedUserType = storedUserTypeRaw;
      }
    }

    setIsAuthenticated(true);
    setUserId(storedUserId);
    setUserType(storedUserType);

    // Check if user has the correct role
    if (storedUserType === requiredRole) {
      setHasRole(true);
    } else {
      setHasRole(false);
      // User has wrong role - redirect to appropriate dashboard
      if (storedUserType === 'SUPPLIER') {
        navigate('/dashboard');
      } else if (storedUserType === 'INDUSTRY_WORKER') {
        navigate('/industryDashBoard');
      } else {
        // Unknown or missing role - redirect to login
        navigate('/login');
      }
    }

    setLoading(false);
  }, [requiredRole, navigate]);

  return {
    isAuthenticated,
    hasRole,
    userId,
    userType,
    loading,
  };
}

/**
 * Get current user type from localStorage
 */
export function getCurrentUserType() {
  if (typeof window === 'undefined') return null;
  
  // Try to get from token or user data
  // For now, we'll infer from the route or store it during login
  const userType = localStorage.getItem('userType');
  if (userType === 'SUPPLIER' || userType === 'INDUSTRY_WORKER') {
    return userType;
  }
  
  return null;
}

/**
 * Check if user is authenticated
 */
export function isAuthenticated() {
  if (typeof window === 'undefined') return false;
  const token = localStorage.getItem('token');
  return !!token;
}

/**
 * Check if user has required role
 */
export function hasRole(requiredRole) {
  const userType = getCurrentUserType();
  return userType === requiredRole;
}

/**
 * Redirect to login if not authenticated
 */
export function requireAuth(navigate) {
  if (!isAuthenticated()) {
    navigate('/login');
    return false;
  }
  return true;
}

/**
 * Check if user has required role (non-blocking check)
 * Returns true if user is authenticated and has the required role
 * Returns false otherwise (but doesn't redirect - let the component handle it)
 */
export function requireRole(requiredRole, navigate) {
  if (typeof window === 'undefined') return false;
  
  if (!isAuthenticated()) {
    // Only redirect if we're sure there's no token at all
    // Use setTimeout to avoid redirect loops
    setTimeout(() => {
      if (!localStorage.getItem('token')) {
        navigate('/login');
      }
    }, 100);
    return false;
  }
  
  const userType = getCurrentUserType();
  if (!userType || userType !== requiredRole) {
    // Don't redirect immediately - might be a timing issue
    // The component should handle showing an error or redirecting
    return false;
  }
  
  return true;
}
