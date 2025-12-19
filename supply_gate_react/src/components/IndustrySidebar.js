import React from "react";
import {
  LayoutDashboard,
  CheckSquare,
  Settings,
} from "lucide-react";
import { UnifiedSidebar } from "./UnifiedSidebar";

const navItems = [
  { icon: LayoutDashboard, label: "Dashboard", href: "/industryDashBoard" },
  { icon: CheckSquare, label: "Verification", href: "/industryDashBoard/verification" },
  { icon: Settings, label: "Settings", href: "/industryDashBoard/settings" },
];

export function IndustrySidebar({ className }) {
  return (
    <UnifiedSidebar
      className={className}
      navItems={navItems}
      userRole="INDUSTRY_WORKER"
      localStorageKey="industrySidebarCollapsed"
      profileStorageKey="industryProfile"
      defaultProfile={{
        profilePicture: "/professional-man-avatar.png",
        fullName: "Industry Worker",
        username: "Worker User",
      }}
      badgeCount={0}
    />
  );
}
