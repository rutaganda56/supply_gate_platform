import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Providers } from './components/Providers';

// Import pages
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/auth/LoginPage';
import SignUpPage from './pages/auth/SignUpPage';
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage';
import ResetPasswordPage from './pages/auth/ResetPasswordPage';
import Verify2FAPage from './pages/auth/Verify2FAPage';
import SupplierDashboard from './pages/dashboard/SupplierDashboard';
import StorePage from './pages/dashboard/StorePage';
import NotificationsPage from './pages/dashboard/NotificationsPage';
import VerificationPage from './pages/dashboard/VerificationPage';
import SettingsPage from './pages/dashboard/SettingsPage';
import IndustryDashboard from './pages/industry/IndustryDashboard';
import IndustryVerificationPage from './pages/industry/IndustryVerificationPage';
import IndustrySettingsPage from './pages/industry/IndustrySettingsPage';
import IndustryRegisterPage from './pages/industry/IndustryRegisterPage';
import ProductsPage from './pages/website/ProductsPage';
import PricingPage from './pages/website/PricingPage';
import SupportPage from './pages/website/SupportPage';

// Protected Route Component
function ProtectedRoute({ children, requiredRole }) {
  const token = localStorage.getItem('token');
  const userType = localStorage.getItem('userType');
  
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  
  if (requiredRole && userType !== requiredRole) {
    // Redirect to appropriate dashboard
    if (userType === 'SUPPLIER') {
      return <Navigate to="/dashboard" replace />;
    } else if (userType === 'INDUSTRY_WORKER') {
      return <Navigate to="/industryDashBoard" replace />;
    }
    return <Navigate to="/login" replace />;
  }
  
  return children;
}

function App() {
  return (
    <Providers>
      <Router>
        <Routes>
          {/* Public routes */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signUp" element={<SignUpPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
          <Route path="/verify-2fa" element={<Verify2FAPage />} />
          
          {/* Website pages */}
          <Route path="/website/products" element={<ProductsPage />} />
          <Route path="/website/pricing" element={<PricingPage />} />
          <Route path="/website/support" element={<SupportPage />} />
          
          {/* Public routes - Registration */}
          <Route path="/industryDashBoard/register" element={<IndustryRegisterPage />} />
          
          {/* Protected routes - Supplier Dashboard */}
          <Route 
            path="/dashboard" 
            element={
              <ProtectedRoute requiredRole="SUPPLIER">
                <SupplierDashboard />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/dashboard/store" 
            element={
              <ProtectedRoute requiredRole="SUPPLIER">
                <StorePage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/dashboard/notifications" 
            element={
              <ProtectedRoute requiredRole="SUPPLIER">
                <NotificationsPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/dashboard/verification" 
            element={
              <ProtectedRoute requiredRole="SUPPLIER">
                <VerificationPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/dashboard/settings" 
            element={
              <ProtectedRoute requiredRole="SUPPLIER">
                <SettingsPage />
              </ProtectedRoute>
            } 
          />
          
          {/* Protected routes - Industry Dashboard */}
          <Route 
            path="/industryDashBoard" 
            element={
              <ProtectedRoute requiredRole="INDUSTRY_WORKER">
                <IndustryDashboard />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/industryDashBoard/verification" 
            element={
              <ProtectedRoute requiredRole="INDUSTRY_WORKER">
                <IndustryVerificationPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/industryDashBoard/settings" 
            element={
              <ProtectedRoute requiredRole="INDUSTRY_WORKER">
                <IndustrySettingsPage />
              </ProtectedRoute>
            } 
          />
          
          {/* Catch all - redirect to home */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </Providers>
  );
}

export default App;
