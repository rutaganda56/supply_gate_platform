import React, { useState, useEffect } from "react";
import {
  LogOut,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { cn } from "../lib/utils";
import { Button } from "./ui/button";

/**
 * Unified Sidebar Component
 * 
 * Reusable sidebar that accepts navigation items and configuration
 * Supports both supplier and industry worker dashboards
 */
export function UnifiedSidebar({ 
  className,
  navItems = [],
  userRole = "SUPPLIER",
  localStorageKey = "sidebarCollapsed",
  profileStorageKey = "supplierProfile",
  defaultProfile = {
    profilePicture: "/professional-man-avatar.png",
    fullName: "User",
    username: "username",
  },
  badgeCount = 0,
}) {
  const location = useLocation();
  const navigate = useNavigate();
  const [isCollapsed, setIsCollapsed] = useState(() => {
    if (typeof window !== "undefined") {
      const saved = localStorage.getItem(localStorageKey);
      return saved === "true";
    }
    return false;
  });
  const [isMobile, setIsMobile] = useState(false);
  const [profileData, setProfileData] = useState(defaultProfile);

  // Handle responsive behavior
  useEffect(() => {
    const checkScreenSize = () => {
      const mobile = window.innerWidth < 768;
      setIsMobile(mobile);
      // Auto-collapse on mobile
      if (mobile && !isCollapsed) {
        setIsCollapsed(true);
      }
    };

    checkScreenSize();
    window.addEventListener("resize", checkScreenSize);
    return () => window.removeEventListener("resize", checkScreenSize);
  }, [isCollapsed]);

  // Load profile data
  useEffect(() => {
    const loadProfile = () => {
      const savedProfile = localStorage.getItem(profileStorageKey);
      if (savedProfile) {
        try {
          const parsed = JSON.parse(savedProfile);
          setProfileData({
            profilePicture: parsed.profilePicture || defaultProfile.profilePicture,
            fullName: parsed.fullName?.split(" ")[0] || defaultProfile.fullName,
            username: parsed.fullName?.split(" ").slice(1).join(" ") || parsed.username || defaultProfile.username,
          });
        } catch (err) {
          if (process.env.NODE_ENV === 'development') {
            console.warn(`Failed to parse ${profileStorageKey}:`, err);
          }
        }
      }
    };
    
    loadProfile();
    
    // Listen for profile updates
    window.addEventListener("profileUpdated", loadProfile);
    window.addEventListener("storage", loadProfile);
    
    return () => {
      window.removeEventListener("profileUpdated", loadProfile);
      window.removeEventListener("storage", loadProfile);
    };
  }, [profileStorageKey, defaultProfile]);

  const toggleSidebar = () => {
    const newState = !isCollapsed;
    setIsCollapsed(newState);
    localStorage.setItem(localStorageKey, String(newState));
  };

  const handleLogout = () => {
    // Clear localStorage
    if (typeof window !== 'undefined') {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('username');
      localStorage.removeItem('userId');
      localStorage.removeItem('userType');
    }
    navigate("/login");
  };

  return (
    <>
      {/* Mobile Overlay */}
      {isMobile && !isCollapsed && (
        <div
          className="fixed inset-0 bg-black/50 z-40"
          onClick={toggleSidebar}
        />
      )}
      <aside
        className={cn(
          "flex flex-col bg-[#1a3a3a] text-white transition-all duration-300 ease-in-out relative shrink-0",
          isMobile 
            ? (isCollapsed ? "w-0 overflow-hidden h-screen" : "w-64 fixed z-50 shadow-2xl h-screen")
            : (isCollapsed ? "w-20 self-stretch" : "w-auto min-w-[256px] max-w-[320px] self-stretch"),
          className
        )}
        style={!isMobile && !isCollapsed && navItems.length > 0 ? {
          width: `${Math.max(256, Math.min(320, 200 + Math.max(...navItems.map(item => item.label.length)) * 7))}px`
        } : undefined}
      >
        {/* Toggle Button */}
        <Button
          onClick={toggleSidebar}
          className="absolute -right-3 top-6 z-10 bg-[#1a3a3a] border-2 border-white/20 rounded-full p-1.5 hover:bg-[#2a4a4a] transition-colors shadow-lg h-auto"
          aria-label={isCollapsed ? "Expand sidebar" : "Collapse sidebar"}
        >
          {isCollapsed ? (
            <ChevronRight className="w-4 h-4 text-white" />
          ) : (
            <ChevronLeft className="w-4 h-4 text-white" />
          )}
        </Button>

        {/* User Profile */}
        <div className={cn(
          "flex items-center gap-3 p-4 border-b border-white/10 transition-all duration-300",
          isCollapsed && "justify-center px-2"
        )}>
          {!isCollapsed && (
            <div className="flex-1 min-w-0">
              <h3 className="font-semibold text-sm truncate">{profileData.fullName}</h3>
              <p className="text-xs text-white/80 truncate">{profileData.username}</p>
              <span className="text-xs text-white/60">
                {userRole === "INDUSTRY_WORKER" ? "Industry" : "Supplier"}
              </span>
            </div>
          )}
        </div>

        {/* Navigation */}
        <nav className="flex-1 py-4 overflow-y-auto">
          <ul className={cn("space-y-1 transition-all duration-300", isCollapsed ? "px-2" : "px-3")}>
            {navItems.map((item) => {
              const isActive = location.pathname === item.href;
              return (
                <li key={item.label}>
                  <Link
                    to={item.href}
                    className={cn(
                      "flex items-center rounded-lg transition-all duration-200 group",
                      isCollapsed ? "justify-center px-3 py-3" : "gap-3 px-4 py-3",
                      isActive 
                        ? "bg-white text-green-900 shadow-sm" 
                        : "hover:bg-white/10 text-white"
                    )}
                    title={isCollapsed ? item.label : undefined}
                  >
                    <item.icon className={cn(
                      "flex-shrink-0 transition-transform",
                      isCollapsed ? "w-5 h-5" : "w-5 h-5",
                      isActive && "scale-110"
                    )} />
                    {!isCollapsed && (
                      <>
                        <span className="flex-1 truncate">{item.label}</span>
                        {item.hasBadge && badgeCount > 0 && (
                          <span className="bg-white/20 text-xs px-2 py-0.5 rounded-full min-w-[24px] text-center">
                            {badgeCount.toString().padStart(2, "0")}
                          </span>
                        )}
                      </>
                    )}
                    {isCollapsed && item.hasBadge && badgeCount > 0 && (
                      <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full border-2 border-[#1a3a3a]" />
                    )}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        {/* Logout */}
        <div className={cn(
          "p-4 border-t border-white/10 transition-all duration-300",
          isCollapsed && "px-2"
        )}>
          <Button 
            onClick={handleLogout}
            className={cn(
              "flex items-center w-full hover:bg-white/10 rounded-lg transition-all duration-200 bg-transparent text-white hover:text-white border-none",
              isCollapsed ? "justify-center px-3 py-3" : "gap-3 px-4 py-3"
            )}
            title={isCollapsed ? "Logout" : undefined}
          >
            <LogOut className="w-5 h-5 flex-shrink-0" />
            {!isCollapsed && <span>Logout</span>}
          </Button>
        </div>
      </aside>
    </>
  );
}

