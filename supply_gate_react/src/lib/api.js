import axios from 'axios';

/**
 * API Configuration
 * 
 * This file sets up axios (HTTP client) to communicate with the Spring Boot backend.
 * It includes:
 * - Base URL configuration
 * - Automatic authentication token injection
 * - Centralized error handling
 * - Request/response logging for debugging
 */

// Base API URL - Change this if your Spring Boot runs on a different port
// You can also set REACT_APP_API_URL in .env file
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

// Create axios instance with default configuration
const api = axios.create({
  baseURL: API_BASE_URL,
  // Don't set Content-Type by default - will be set in interceptor for requests with body
  // Timeout after 10 seconds if no response
  timeout: 10000,
});

/**
 * Request Interceptor
 * 
 * This runs BEFORE every API request.
 * It automatically adds the authentication token to all requests.
 */
api.interceptors.request.use(
  (config) => {
    // Only access localStorage in browser
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    
    // Only set Content-Type for requests with a body (POST, PUT, PATCH, etc.)
    // GET requests should not have Content-Type header
    const method = config.method?.toUpperCase();
    if (method && ['POST', 'PUT', 'PATCH'].includes(method) && config.data !== undefined) {
      // Don't set Content-Type for FormData - browser will set it with boundary
      if (!(config.data instanceof FormData)) {
        // Only set if not already set
        if (!config.headers['Content-Type']) {
          config.headers['Content-Type'] = 'application/json';
        }
      } else {
        // Remove Content-Type for FormData - let browser set it with boundary
        delete config.headers['Content-Type'];
      }
    } else if (method === 'GET' || method === 'DELETE') {
      // Remove Content-Type header for GET and DELETE requests
      delete config.headers['Content-Type'];
    }
    
    // Log request for debugging (only in development)
    if (process.env.NODE_ENV === 'development') {
      // eslint-disable-next-line no-console
      console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, {
        data: config.data,
        params: config.params,
      });
    }
    
    return config;
  },
  (error) => {
    // Handle request setup errors
    return Promise.reject(error);
  }
);

/**
 * Response Interceptor
 * 
 * This runs AFTER every API response.
 * It handles errors consistently across the entire application.
 */
api.interceptors.response.use(
  (response) => {
    // Log successful responses for debugging (only in development)
    if (process.env.NODE_ENV === 'development') {
      // eslint-disable-next-line no-console
      console.log(`[API Success] ${response.config.method?.toUpperCase()} ${response.config.url}`, {
        status: response.status,
        data: response.data,
      });
    }
    return response;
  },
  (error) => {
    // Handle different types of errors
    if (error.response) {
      // Server responded with an error status (4xx, 5xx)
      const status = error.response.status;
      const errorData = error.response.data;
      
      // Log error for debugging (only in development, and only for non-network errors)
      // Don't log 404 for my-verification endpoint (expected when no verification exists)
      const isMyVerification404 = status === 404 && error.config?.url?.includes('/api/verification/my-verification');
      if (process.env.NODE_ENV === 'development' && status !== 401 && status !== 403 && !isMyVerification404) {
        // eslint-disable-next-line no-console
        console.error(`[API Error] ${error.config?.method?.toUpperCase()} ${error.config?.url}`, {
          status,
          error: errorData,
        });
      }
      
      // Handle specific error cases
      if (status === 401) {
        // Check if this is a login request
        const isLoginRequest = error.config?.url?.includes('/api/auth/login');
        
        if (isLoginRequest) {
          // For login requests, return the actual error message from backend
          const errorMessage = errorData?.message || errorData || 'Invalid username or password. Please check your credentials.';
          return Promise.reject({ 
            message: typeof errorMessage === 'string' ? errorMessage : 'Invalid username or password. Please check your credentials.',
            status: 401 
          });
        } else {
          // For other requests, it's a session expiration
          if (typeof window !== 'undefined') {
            localStorage.removeItem('token');
            localStorage.removeItem('userId');
            // Optionally redirect to login
            // window.location.href = '/login';
          }
          return Promise.reject({ 
            message: 'Your session has expired. Please login again.',
            status: 401 
          });
        }
      }
      
      if (status === 403) {
        return Promise.reject({ 
          message: 'You do not have permission to perform this action.',
          status: 403 
        });
      }
      
      if (status === 404) {
        // For my-verification endpoint, return a special error that won't be logged
        const isMyVerification = error.config?.url?.includes('/api/verification/my-verification');
        if (isMyVerification) {
          // Return error but mark it as expected (not a real error)
          return Promise.reject({ 
            message: 'The requested resource was not found.',
            status: 404,
            isExpected: true // Flag to indicate this is expected
          });
        }
        return Promise.reject({ 
          message: 'The requested resource was not found.',
          status: 404 
        });
      }
      
      // Return server error message or default message
      return Promise.reject({
        message: errorData?.message || `Server error (${status})`,
        ...errorData, // Include all error details
        status,
      });
    } else if (error.request) {
      // Request was made but no response received (network error, CORS, timeout, etc.)
      // Check if it's a timeout
      const isTimeout = error.code === 'ECONNABORTED' || error.message?.includes('timeout');
      
      // Only log network errors in development
      if (process.env.NODE_ENV === 'development') {
        // eslint-disable-next-line no-console
        console.warn('[API Network Error]', {
          url: error.config?.url,
          message: isTimeout ? 'Request timeout' : 'No response from server',
          code: error.code,
          timeout: error.config?.timeout,
        });
      }
      
      if (isTimeout) {
        return Promise.reject({ 
          message: 'Request timeout: The server took too long to respond. This may happen with large file uploads. Please try again or use smaller files.',
          isNetworkError: true,
          isTimeout: true,
          code: error.code,
          config: error.config, // Include config for debugging
        });
      }
      
      return Promise.reject({ 
        message: 'Cannot connect to server. Please check if the backend is running on ' + API_BASE_URL,
        isNetworkError: true,
        code: error.code,
        config: error.config, // Include config for debugging
      });
    } else {
      // Something else happened (request setup error)
      if (process.env.NODE_ENV === 'development') {
        // eslint-disable-next-line no-console
        console.error('[API Setup Error]', error.message);
      }
      
      return Promise.reject({ 
        message: error.message || 'An unexpected error occurred',
      });
    }
  }
);

/**
 * Store API Functions
 * 
 * These functions communicate with the Spring Boot StoreController.
 * Endpoints:
 * - GET /api/stores/stores - Get all stores
 * - POST /api/stores/createStore - Create a new store
 * - PUT /api/stores/{id} - Update a store
 * - DELETE /api/stores/deleteStore/{id} - Delete a store
 */
export const storeApi = {
  /**
   * Get all stores (paginated with search).
   * Calls: GET http://localhost:8080/api/stores/stores?page={page}&size={size}&search={search}
   * 
   * @param page - Page number (0-indexed)
   * @param size - Number of items per page
   * @param search - Optional search term to filter stores
   */
  getAllStores: async (page = 0, size = 20, search) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    if (search && search.trim()) {
      params.append('search', search.trim());
    }
    const response = await api.get(`/api/stores/stores?${params.toString()}`);
    return response.data;
  },

  /**
   * Create a new store
   * Calls: POST http://localhost:8080/api/stores/createStore
   * @param store - Store data (storeName, phoneNumber, storeEmail, userId)
   */
  createStore: async (store) => {
    const response = await api.post('/api/stores/createStore', store);
    return response.data;
  },

  /**
   * Update an existing store
   * Calls: PUT http://localhost:8080/api/stores/{id}
   * @param id - Store UUID
   * @param store - Updated store data
   */
  updateStore: async (id, store) => {
    const response = await api.put(`/api/stores/${id}`, store);
    return response.data;
  },

  /**
   * Delete a store
   * Calls: DELETE http://localhost:8080/api/stores/deleteStore/{id}
   * @param id - Store UUID
   */
  deleteStore: async (id) => {
    await api.delete(`/api/stores/deleteStore/${id}`);
  },
};

/**
 * Category API Functions
 * 
 * These functions communicate with the Spring Boot CategoryController.
 * Endpoints:
 * - GET /api/categories/categories - Get all categories
 * - POST /api/categories/createCategory - Create a new category
 * - PUT /api/categories/{id} - Update a category
 * - DELETE /api/categories/deleteCategory/{id} - Delete a category
 */
export const categoryApi = {
  /**
   * Get all categories (paginated with search).
   * Calls: GET http://localhost:8080/api/categories/categories?page={page}&size={size}&search={search}
   * 
   * @param page - Page number (0-indexed)
   * @param size - Number of items per page
   * @param search - Optional search term to filter categories
   */
  getAllCategories: async (page = 0, size = 20, search) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    if (search && search.trim()) {
      params.append('search', search.trim());
    }
    const response = await api.get(`/api/categories/categories?${params.toString()}`);
    return response.data;
  },

  /**
   * Create a new category
   * Calls: POST http://localhost:8080/api/categories/createCategory
   * @param category - Category data (categoryName)
   */
  createCategory: async (category) => {
    const response = await api.post('/api/categories/createCategory', category);
    return response.data;
  },

  /**
   * Update an existing category
   * Calls: PUT http://localhost:8080/api/categories/{id}
   * @param id - Category UUID
   * @param category - Updated category data
   */
  updateCategory: async (id, category) => {
    const response = await api.put(`/api/categories/${id}`, category);
    return response.data;
  },

  /**
   * Delete a category
   * Calls: DELETE http://localhost:8080/api/categories/deleteCategory/{id}
   * @param id - Category UUID
   */
  deleteCategory: async (id) => {
    await api.delete(`/api/categories/deleteCategory/${id}`);
  },
};

/**
 * Product API Functions
 * 
 * These functions communicate with the Spring Boot ProductController.
 * Endpoints:
 * - GET /api/products/getProducts - Get all products (paginated)
 * - POST /api/products/createAProduct - Create a new product
 * - PUT /api/products/{id} - Update a product
 * - DELETE /api/products/deleteProduct/{id} - Delete a product
 */
export const productApi = {
  /**
   * Get all products (paginated with search)
   * Calls: GET http://localhost:8080/api/products/getProducts?page={page}&size={size}&search={search}&verifiedOnly={verifiedOnly}
   * SECURITY: This is a public endpoint - no authentication required for website display.
   * @param page - Page number (0-indexed)
   * @param size - Number of items per page
   * @param search - Optional search term to filter products
   * @param verifiedOnly - Optional filter to show only products from verified suppliers (default: false - shows all products)
   */
  getAllProducts: async (page = 0, size = 10, search, verifiedOnly = false) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      verifiedOnly: verifiedOnly.toString(),
    });
    if (search && search.trim()) {
      params.append('search', search.trim());
    }
    // Use axios directly for public endpoint (no auth token required)
    const response = await axios.get(
      `${API_BASE_URL}/api/products/getProducts?${params.toString()}`,
      {
        headers: {
          'Content-Type': 'application/json',
        },
        timeout: 10000,
      }
    );
    return response.data;
  },

  /**
   * Create a new product
   * Calls: POST http://localhost:8080/api/products/createAProduct
   * @param product - Product data (categoryId, storeId, productName, productDescription, productPrice, quantity)
   */
  createProduct: async (product) => {
    const response = await api.post('/api/products/createAProduct', product);
    return response.data;
  },

  /**
   * Update an existing product
   * Calls: PUT http://localhost:8080/api/products/{id}
   * @param id - Product UUID
   * @param product - Updated product data
   */
  updateProduct: async (id, product) => {
    const response = await api.put(`/api/products/${id}`, product);
    return response.data;
  },

  /**
   * Delete a product
   * Calls: DELETE http://localhost:8080/api/products/deleteProduct/{id}
   * @param id - Product UUID
   */
  deleteProduct: async (id) => {
    await api.delete(`/api/products/deleteProduct/${id}`);
  },

  /**
   * Track a product view/impression.
   * This is called when a user views a product card.
   * Modern apps track this for analytics (like Instagram, Facebook, Amazon).
   * 
   * @param productId - Product UUID
   * @returns Success response
   */
  trackProductView: async (productId) => {
    try {
      // Use public endpoint (no auth required) for tracking views
      await axios.post(
        `${API_BASE_URL}/api/products/${productId}/track-view`,
        {},
        {
          headers: {
            'Content-Type': 'application/json',
          },
          timeout: 5000, // Short timeout for tracking calls
        }
      );
    } catch (err) {
      // Silently fail - tracking should not break the user experience
      if (process.env.NODE_ENV === 'development') {
        console.warn("Failed to track product view:", err);
      }
    }
  },

  /**
   * Track a product like/favorite.
   * This is called when a user clicks the like button on a product.
   * Modern apps track this for engagement metrics (like Instagram, Pinterest).
   * 
   * @param productId - Product UUID
   * @param isLiked - Whether the product is being liked (true) or unliked (false)
   * @returns Success response
   */
  trackProductLike: async (productId, isLiked = true) => {
    try {
      // Use public endpoint (no auth required) for tracking likes
      await axios.post(
        `${API_BASE_URL}/api/products/${productId}/track-like`,
        { isLiked },
        {
          headers: {
            'Content-Type': 'application/json',
          },
          timeout: 5000, // Short timeout for tracking calls
        }
      );
    } catch (err) {
      // Silently fail - tracking should not break the user experience
      if (process.env.NODE_ENV === 'development') {
        console.warn("Failed to track product like:", err);
      }
    }
  },
};

/**
 * Authentication API Functions
 * 
 * These functions handle user authentication (login, register).
 * Endpoints:
 * - POST /api/auth/login - Login user
 * - POST /api/auth/register - Register new user
 */
export const authApi = {
  /**
   * Login user
   * Calls: POST http://localhost:8080/api/auth/login
   * @param credentials - Username and password
   * @returns AuthResponseDto with tokens, or TwoFactorAuthResponseDto if 2FA is required
   */
  login: async (credentials) => {
    const response = await axios.post(
      `${API_BASE_URL}/api/auth/login`,
      {
        username: credentials.username,
        password: credentials.password,
      },
      {
        headers: {
          'Content-Type': 'application/json',
        },
        timeout: 30000, // Increased to 30 seconds to allow time for 2FA email sending
      }
    );
    
    // Check if 2FA is required
    const data = response.data;
    if (data.requires2FA === true) {
      // Return as TwoFactorAuthResponseDto
      return {
        sessionId: data.sessionId,
        userId: data.userId,
        username: data.username,
        message: data.message || "Please check your email for the verification code to complete login.",
      };
    }
    
    // Normal login response (AuthResponseDto)
    return data;
  },

  /**
   * Get current user information
   * This is used to get userId after login
   * Note: This requires authentication, so call it after storing the token
   * 
   * IMPORTANT: This is a workaround - ideally the backend should have a GET /api/auth/me endpoint
   */
  getCurrentUser: async () => {
    try {
      // Get all users and find the one matching the username
      // Note: This is not ideal but works until we have a "get current user" endpoint
      const response = await api.get('/api/auth/getAllUsers');
      const username = typeof window !== 'undefined' ? localStorage.getItem('username') : null;
      
      if (!username) {
        const error = { 
          message: 'Username not found in storage. Please login again.',
          status: 401,
          requiresLogin: true
        };
        throw error;
      }
      
      // Find user by username (assuming username is unique)
      // Note: In production, you should have a dedicated endpoint like GET /api/auth/me
      const user = response.data.find(u => u.username === username);
      
      if (!user) {
        const error = { 
          message: 'User not found. Please login again.',
          status: 401,
          requiresLogin: true
        };
        throw error;
      }
      
      return {
        ...user,
        userId: user.userId, // userId might be in the response
      };
    } catch (error) {
      // Ensure we always have a proper error object with message
      if (error && typeof error === 'object') {
        // If error already has message and status, use it
        if (error.message && error.status) {
          throw error;
        }
        // If error has response data, extract from there
        if (error.response) {
          const status = error.response.status || error.status;
          const errorData = error.response.data;
          const message = errorData?.message || 
                         (typeof errorData === 'string' ? errorData : 'Your session has expired. Please login again.');
          const err = new Error(message);
          err.status = status || 401;
          err.requiresLogin = true;
          err.response = error.response;
          err.originalError = error;
          throw err;
        }
        // If error is from axios interceptor (already formatted)
        if (error.status) {
          const err = new Error(error.message || 'Your session has expired. Please login again.');
          err.status = error.status;
          err.requiresLogin = error.requiresLogin !== false;
          err.originalError = error;
          throw err;
        }
      }
      
      // Fallback: create a proper error object
      const err = new Error('Failed to get user information. Please login again.');
      err.status = 401;
      err.requiresLogin = true;
      err.originalError = error;
      throw err;
    }
  },

  /**
   * Helper function to get or fetch userId
   * This ensures we always have userId available
   */
  getUserId: async () => {
    if (typeof window === 'undefined') {
      throw new Error('Cannot access localStorage on server');
    }

    // First, try to get from localStorage
    let userId = localStorage.getItem('userId');
    
    if (userId) {
      return userId;
    }

    // If not found, fetch from API
    try {
      const userInfo = await authApi.getCurrentUser();
      if (userInfo.userId) {
        localStorage.setItem('userId', userInfo.userId);
        return userInfo.userId;
      }
      
      // If userId is still not available, we need to get it from the user list
      // This is a fallback - ideally backend should return userId in login response
      throw new Error('User ID not available. Please login again.');
    } catch (error) {
      throw new Error('Could not retrieve user ID. Please login again.');
    }
  },

  /**
   * Register new user
   * Calls: POST http://localhost:8080/api/auth/register
   * @param userData - User registration data
   */
  register: async (userData) => {
    // Log the data being sent for debugging
    if (process.env.NODE_ENV === 'development') {
      console.log('[API] Register request data:', JSON.stringify(userData, null, 2));
    }
    
    try {
      const response = await axios.post(
        `${API_BASE_URL}/api/auth/register`,
        userData,
        {
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );
      return response.data;
    } catch (error) {
      // Log detailed error information
      if (process.env.NODE_ENV === 'development') {
        console.error('[API] Register error:', {
          status: error.response?.status,
          statusText: error.response?.statusText,
          data: error.response?.data,
          message: error.message,
        });
      }
      throw error;
    }
  },

  /**
   * Request password reset (forgot password).
   * SECURITY: This is a public endpoint - no authentication required.
   * 
   * @param email - User's registered email address
   * @returns Success message (doesn't reveal if email exists)
   */
  forgotPassword: async (email) => {
    // Use axios directly without the api instance to avoid token requirement
    const response = await axios.post(
      `${API_BASE_URL}/api/auth/forgot-password`,
      { email },
      {
        headers: {
          'Content-Type': 'application/json',
        },
        timeout: 10000,
      }
    );
    return response.data;
  },

  /**
   * Reset password using token.
   * SECURITY: This is a public endpoint - no authentication required.
   * 
   * @param token - Password reset token from email
   * @param newPassword - New password (minimum 6 characters)
   * @returns Success message
   */
  resetPassword: async (token, newPassword) => {
    // Use axios directly without the api instance to avoid token requirement
    const response = await axios.post(
      `${API_BASE_URL}/api/auth/reset-password`,
      { token, newPassword },
      {
        headers: {
          'Content-Type': 'application/json',
        },
        timeout: 10000,
      }
    );
    return response.data;
  },

  /**
   * Validate password reset token.
   * SECURITY: This is a public endpoint - no authentication required.
   * 
   * @param token - Password reset token to validate
   * @returns Validation result with status
   */
  validateResetToken: async (token) => {
    // Use axios directly without the api instance to avoid token requirement
    const response = await axios.get(
      `${API_BASE_URL}/api/auth/validate-reset-token?token=${encodeURIComponent(token)}`,
      {
        headers: {
          'Content-Type': 'application/json',
        },
        timeout: 10000,
      }
    );
    return response.data;
  },

  /**
   * Verify 2FA code and complete login.
   * SECURITY: This is a public endpoint - no authentication required.
   * 
   * @param sessionId - 2FA session ID from login response
   * @param code - 6-digit verification code
   * @returns AuthResponseDto with tokens and user info
   */
  verify2FA: async (sessionId, code) => {
    // Use axios directly without the api instance to avoid token requirement
    const response = await axios.post(
      `${API_BASE_URL}/api/auth/verify-2fa`,
      { sessionId, code },
      {
        headers: {
          'Content-Type': 'application/json',
        },
        timeout: 10000,
      }
    );
    return response.data;
  },

  /**
   * Resend 2FA verification code.
   * SECURITY: This is a public endpoint - no authentication required.
   * 
   * @param sessionId - 2FA session ID
   * @returns Success message with new session ID
   */
  resend2FACode: async (sessionId) => {
    // Use axios directly without the api instance to avoid token requirement
    const response = await axios.post(
      `${API_BASE_URL}/api/auth/resend-2fa-code`,
      { sessionId },
      {
        headers: {
          'Content-Type': 'application/json',
        },
        timeout: 10000,
      }
    );
    return response.data;
  },

  /**
   * Validate 2FA session.
   * SECURITY: This is a public endpoint - no authentication required.
   * 
   * @param sessionId - 2FA session ID to validate
   * @returns Validation result with status
   */
  validate2FASession: async (sessionId) => {
    // Use axios directly without the api instance to avoid token requirement
    const response = await axios.get(
      `${API_BASE_URL}/api/auth/validate-2fa-session?sessionId=${encodeURIComponent(sessionId)}`,
      {
        headers: {
          'Content-Type': 'application/json',
        },
        timeout: 10000,
      }
    );
    return response.data;
  },
};

/**
 * Location API Functions
 * 
 * These functions handle Rwanda administrative structure (District → Sector → Cell → Village).
 * Endpoints:
 * - GET /api/location/getGovernmentStructures - Get all locations
 * - GET /api/location/getGovernmentStructureByStructureCode - Get location by code
 */
export const locationApi = {
  /**
   * Get all government structures (locations)
   * Calls: GET http://localhost:8080/api/location/getGovernmentStructures
   * 
   * Note: This endpoint should be accessible without authentication for registration pages.
   * If you see 401 errors, make sure SecurityConfig allows "/api/location/**" in permitAll.
   * 
   * @returns Array of all locations
   */
  getAllLocations: async () => {
    // Use axios directly (not the api instance) to avoid auth token requirement
    // This is needed for registration pages where users aren't logged in yet
    const response = await axios.get(
      `${API_BASE_URL}/api/location/getGovernmentStructures`,
      {
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );
    return response.data;
  },

  /**
   * Get location by structure code
   * Calls: GET http://localhost:8080/api/location/getGovernmentStructureByStructureCode?structureCode={code}
   * @param structureCode - The structure code to search for
   */
  getLocationByCode: async (structureCode) => {
    const response = await api.get(
      `/api/location/getGovernmentStructureByStructureCode?structureCode=${structureCode}`
    );
    return response.data;
  },
};

/**
 * Notification API Functions
 * 
 * These functions communicate with the Spring Boot NotificationController.
 * Endpoints:
 * - GET /api/notifications - Get all notifications (paginated)
 * - GET /api/notifications/all - Get all notifications (non-paginated)
 * - GET /api/notifications/unread - Get unread notifications
 * - GET /api/notifications/unread-count - Get unread notification count
 * - PUT /api/notifications/{id}/read - Mark notification as read
 * - PUT /api/notifications/read-all - Mark all notifications as read
 */
export const notificationApi = {
  /**
   * Get all notifications for the authenticated user (paginated with search).
   * Requires authentication.
   * 
   * @param page - Page number (0-indexed)
   * @param size - Number of items per page
   * @param search - Optional search term to filter notifications
   */
  getAll: async (page = 0, size = 20, search) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    if (search && search.trim()) {
      params.append('search', search.trim());
    }
    const response = await api.get(`/api/notifications?${params.toString()}`);
    return response.data;
  },

  /**
   * Get all notifications for the authenticated user (non-paginated).
   * Requires authentication.
   */
  getAllNotifications: async () => {
    const response = await api.get('/api/notifications/all');
    return response.data;
  },

  /**
   * Get unread notifications for the authenticated user.
   * Requires authentication.
   */
  getUnread: async () => {
    const response = await api.get('/api/notifications/unread');
    return response.data;
  },

  /**
   * Get unread notification count for the authenticated user.
   * Requires authentication.
   */
  getUnreadCount: async () => {
    const response = await api.get('/api/notifications/unread-count');
    return response.data;
  },

  /**
   * Mark a notification as read.
   * Requires authentication.
   */
  markAsRead: async (notificationId) => {
    await api.put(`/api/notifications/${notificationId}/read`);
  },

  /**
   * Mark all notifications as read for the authenticated user.
   * Requires authentication.
   */
  markAllAsRead: async () => {
    await api.put('/api/notifications/read-all');
  },

  /**
   * Get sender email for a message notification.
   * Requires authentication.
   * 
   * @param notificationId Notification ID
   * @returns Sender email if found
   */
  getSenderEmail: async (notificationId) => {
    const response = await api.get(`/api/notifications/${notificationId}/sender-email`);
    return response.data;
  },
};

/**
 * Message API Functions
 * 
 * These functions communicate with the Spring Boot MessageController.
 * Endpoints:
 * - POST /api/messages/send - Send a message to a supplier (public)
 * - GET /api/messages/my-messages - Get all messages for authenticated supplier
 * - GET /api/messages/unread - Get unread messages
 * - GET /api/messages/unread-count - Get unread message count
 * - PUT /api/messages/{id}/read - Mark message as read
 */
export const messageApi = {
  /**
   * Send a message to a supplier.
   * SECURITY: This is a public endpoint - no authentication required.
   */
  sendMessage: async (message) => {
    // Use axios directly for public endpoint
    const response = await axios.post(
      `${API_BASE_URL}/api/messages/send`,
      message,
      {
        headers: {
          'Content-Type': 'application/json',
        },
        timeout: 10000,
      }
    );
    return response.data;
  },

  /**
   * Get all messages for the authenticated supplier (paginated with search).
   * Requires authentication.
   * 
   * @param page - Page number (0-indexed)
   * @param size - Number of items per page
   * @param search - Optional search term to filter messages
   */
  getMyMessages: async (page = 0, size = 20, search) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    if (search && search.trim()) {
      params.append('search', search.trim());
    }
    const response = await api.get(`/api/messages/my-messages?${params.toString()}`);
    return response.data;
  },

  /**
   * Get unread messages for the authenticated supplier.
   * Requires authentication.
   */
  getUnreadMessages: async () => {
    const response = await api.get('/api/messages/unread');
    return response.data;
  },

  /**
   * Get unread message count for the authenticated supplier.
   * Requires authentication.
   */
  getUnreadCount: async () => {
    const response = await api.get('/api/messages/unread-count');
    return response.data;
  },

  /**
   * Mark a message as read.
   * Requires authentication.
   */
  markAsRead: async (messageId) => {
    await api.put(`/api/messages/${messageId}/read`);
  },
};

/**
 * Verification API Functions
 * 
 * These functions communicate with the Spring Boot VerificationController.
 * Endpoints:
 * - GET /api/verification - Get all verifications
 * - GET /api/verification/status-counts - Get verification status counts
 * - POST /api/verification/submit - Submit verification documents
 * - GET /api/verification/my-verification - Get current user's verification
 * - POST /api/verification/{id}/review - Review verification (approve/reject)
 */
export const verificationApi = {
  /**
   * Get all verifications (paginated with search).
   * Requires authentication.
   * 
   * @param page - Page number (0-indexed)
   * @param size - Number of items per page
   * @param search - Optional search term to filter verifications
   */
  getAll: async (page = 0, size = 20, search) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    if (search && search.trim()) {
      params.append('search', search.trim());
    }
    const response = await api.get(`/api/verification?${params.toString()}`);
    return response.data;
  },

  /**
   * Get verification status counts (pending, approved, rejected, total).
   * For industry workers, only counts verifications assigned to their industry.
   * Requires authentication.
   * 
   * @returns {Promise<{pendingCount: number, approvedCount: number, rejectedCount: number, totalCount: number}>}
   */
  getStatusCounts: async () => {
    const response = await api.get('/api/verification/status-counts');
    return response.data;
  },

  /**
   * Get current user's verification.
   * Requires authentication.
   */
  getMyVerification: async () => {
    try {
      const response = await api.get('/api/verification/my-verification');
      return response.data;
    } catch (err) {
      // 404 is expected when user hasn't submitted verification yet
      if (err.response?.status === 404) {
        return null;
      }
      // Re-throw other errors
      throw err;
    }
  },

  /**
   * Submit verification documents.
   * Requires authentication (supplier role).
   */
  submit: async (data) => {
    const formData = new FormData();
    formData.append('companyName', data.companyName || '');
    if (data.assignedIndustryId) {
      formData.append('assignedIndustryId', data.assignedIndustryId);
    }
    
    // Debug logging
    if (process.env.NODE_ENV === 'development') {
      console.log('DEBUG: Submitting verification with data:', {
        companyName: data.companyName,
        assignedIndustryId: data.assignedIndustryId,
        businessLicense: data.businessLicense ? {
          name: data.businessLicense.name,
          size: data.businessLicense.size,
          type: data.businessLicense.type
        } : null,
        taxCertificate: data.taxCertificate ? {
          name: data.taxCertificate.name,
          size: data.taxCertificate.size,
          type: data.taxCertificate.type
        } : null,
        bankStatement: data.bankStatement ? {
          name: data.bankStatement.name,
          size: data.bankStatement.size,
          type: data.bankStatement.type
        } : null,
        identityProof: data.identityProof ? {
          name: data.identityProof.name,
          size: data.identityProof.size,
          type: data.identityProof.type
        } : null
      });
    }
    
    // Validate files before appending
    if (!data.businessLicense) {
      throw new Error('Business license is required');
    }
    if (!data.taxCertificate) {
      throw new Error('Tax certificate is required');
    }
    if (!data.bankStatement) {
      throw new Error('Bank statement is required');
    }
    if (!data.identityProof) {
      throw new Error('Identity proof is required');
    }
    
    formData.append('businessLicense', data.businessLicense);
    formData.append('taxCertificate', data.taxCertificate);
    formData.append('bankStatement', data.bankStatement);
    formData.append('identityProof', data.identityProof);

    try {
      // Calculate total file size for timeout estimation
      let totalSize = 0;
      if (data.businessLicense) totalSize += data.businessLicense.size;
      if (data.taxCertificate) totalSize += data.taxCertificate.size;
      if (data.bankStatement) totalSize += data.bankStatement.size;
      if (data.identityProof) totalSize += data.identityProof.size;
      
      // Set timeout based on file size: 30 seconds base + 1 second per MB
      // Maximum 120 seconds (2 minutes) for very large files
      const estimatedTimeout = Math.min(30000 + (totalSize / (1024 * 1024)) * 1000, 120000);
      
      if (process.env.NODE_ENV === 'development') {
        console.log('DEBUG: Submitting verification with timeout:', estimatedTimeout, 'ms, total size:', totalSize, 'bytes');
      }
      
      const response = await api.post('/api/verification/submit', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        timeout: estimatedTimeout, // Dynamic timeout based on file size
        // Add upload progress tracking
        onUploadProgress: (progressEvent) => {
          if (process.env.NODE_ENV === 'development' && progressEvent.total) {
            const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
            if (percentCompleted % 25 === 0) { // Log every 25%
              console.log(`DEBUG: Upload progress: ${percentCompleted}%`);
            }
          }
        },
      });
      
      if (process.env.NODE_ENV === 'development') {
        console.log('DEBUG: Verification submitted successfully:', response.data);
      }
      
      return response.data;
    } catch (err) {
      if (process.env.NODE_ENV === 'development') {
        console.error('DEBUG: Verification submission failed:', {
          message: err.message,
          response: err.response?.data,
          status: err.response?.status,
          statusText: err.response?.statusText,
          isNetworkError: err.isNetworkError,
          code: err.code
        });
      }
      
      // Provide more helpful error messages
      if (err.code === 'ECONNABORTED' || err.message?.includes('timeout')) {
        throw new Error('Upload timeout: Files are too large or connection is slow. Please try with smaller files or check your internet connection.');
      } else if (err.isNetworkError || !err.response) {
        throw new Error('Cannot connect to server. Please check if the backend is running on ' + API_BASE_URL);
      }
      
      throw err;
    }
  },

  /**
   * Review verification (approve or reject).
   * Requires authentication (industry worker role).
   */
  review: async (verificationId, reviewData) => {
    const response = await api.post(
      `/api/verification/${verificationId}/review`,
      reviewData
    );
    return response.data;
  },
};

/**
 * Dashboard API Functions
 * 
 * These functions communicate with the Spring Boot DashboardController.
 * Endpoints:
 * - GET /api/dashboard/supplier - Get supplier dashboard stats
 * - GET /api/dashboard/industry - Get industry dashboard stats
 */
export const dashboardApi = {
  /**
   * Get supplier dashboard statistics.
   * Requires authentication.
   * 
   * @returns DashboardStatsDto with real-time stats
   */
  getSupplierStats: async () => {
    const response = await api.get('/api/dashboard/supplier');
    return response.data;
  },

  /**
   * Get industry dashboard statistics.
   * Requires authentication.
   * 
   * @returns DashboardStatsDto with verification-related stats
   */
  getIndustryStats: async () => {
    const response = await api.get('/api/dashboard/industry');
    return response.data;
  },

  /**
   * Get viewers chart data (product views over time).
   * Shows daily product views for the last 7 days.
   * Requires authentication.
   * 
   * @returns Array of { name: string, value: number } for chart
   */
  getViewersChartData: async () => {
    try {
      const response = await api.get('/api/dashboard/supplier/viewers-chart');
      return response.data;
    } catch (err) {
      // Fallback to empty data if endpoint doesn't exist yet
      if (process.env.NODE_ENV === 'development') {
        console.warn("Viewers chart endpoint not available, using fallback data");
      }
      // Return last 7 days with 0 values
      const days = [];
      for (let i = 6; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        days.push({
          name: date.toLocaleDateString('en-US', { weekday: 'short' }),
          value: 0
        });
      }
      return days;
    }
  },

  /**
   * Get impressions chart data (impressions including likes over time).
   * Shows daily impressions for the last 9 days.
   * Requires authentication.
   * 
   * @returns Array of { name: string, value: number } for chart
   */
  getImpressionsChartData: async () => {
    try {
      const response = await api.get('/api/dashboard/supplier/impressions-chart');
      return response.data;
    } catch (err) {
      // Fallback to empty data if endpoint doesn't exist yet
      if (process.env.NODE_ENV === 'development') {
        console.warn("Impressions chart endpoint not available, using fallback data");
      }
      // Return last 9 days with 0 values
      const days = [];
      for (let i = 8; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        days.push({
          name: date.getDate().toString(),
          value: 0
        });
      }
      return days;
    }
  },
};

/**
 * Global Search API Functions
 * 
 * Provides unified search across multiple entities.
 * Endpoints:
 * - GET /api/search?q={query}&limit={limit} - Global search
 */
export const globalSearchApi = {
  /**
   * Perform global search across products, stores, categories, verifications, and messages.
   * 
   * @param query Search query string
   * @param limit Maximum results per category (default: 5, max: 10)
   * @returns GlobalSearchResultDto with categorized results
   */
  search: async (query, limit = 5) => {
    const safeLimit = Math.min(Math.max(limit, 1), 10);
    const params = new URLSearchParams({
      q: query,
      limit: safeLimit.toString(),
    });
    
    const response = await api.get(`/api/search?${params.toString()}`);
    return response.data;
  },
};

export default api;
