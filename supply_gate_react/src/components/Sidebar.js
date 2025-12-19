import React from "react";
import {
  LayoutDashboard,
  Store,
  Bell,
  CheckSquare,
  Settings,
} from "lucide-react";
import { UnifiedSidebar } from "./UnifiedSidebar";
import { useNotifications } from "../contexts/notifications-context";

const navItems = [
  { icon: LayoutDashboard, label: "Dashboard", href: "/dashboard" },
  { icon: Store, label: "Store", href: "/dashboard/store" },
  {
    icon: Bell,
    label: "Notifications",
    href: "/dashboard/notifications",
    hasBadge: true,
  },
  { icon: CheckSquare, label: "Verification", href: "/dashboard/verification" },
  { icon: Settings, label: "Settings", href: "/dashboard/settings" },
];

export function Sidebar({ className }) {
  const { unreadCount } = useNotifications();

  return (
    <UnifiedSidebar
      className={className}
      navItems={navItems}
      userRole="SUPPLIER"
      localStorageKey="sidebarCollapsed"
      profileStorageKey="supplierProfile"
      defaultProfile={{
        profilePicture: "/professional-man-avatar.png",
        fullName: "Rutaganda",
        username: "jean valentin",
      }}
      badgeCount={unreadCount}
    />
  );
}
