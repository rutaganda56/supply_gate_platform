import React, { useState, useEffect } from "react";
import { Sidebar } from "../../components/Sidebar";
import { StatCard } from "../../components/StatCard";
import { ViewersChart } from "../../components/ViewersChart";
import { ImpressionsChart } from "../../components/ImpressionsChart";
import { NotificationsTable } from "../../components/NotificationsTable";
import { GlobalSearch } from "../../components/GlobalSearch";
import { Calendar } from "lucide-react";
import { dashboardApi } from "../../lib/api";

export default function SupplierDashboard() {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalFollowers: 0,
    totalCustomers: 0,
    totalImpressions: 0,
    totalNotifications: 0,
    followersChange: 0,
    customersChange: 0,
    impressionsChange: 0,
    notificationsChange: 0,
  });

  // Load dashboard stats
  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        setLoading(true);
        const statsData = await dashboardApi.getSupplierStats();
        setStats(statsData);
      } catch (err) {
        if (process.env.NODE_ENV === 'development') {
          console.error("Failed to load dashboard data:", err);
        }
      } finally {
        setLoading(false);
      }
    };

    loadDashboardData();
  }, []);

  return (
    <div className="flex min-h-screen bg-gray-50 h-full">
      <Sidebar />

      <main className="flex-1 p-8 min-h-full">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-3xl font-bold text-gray-800">Dashboard</h1>
          <div className="flex items-center gap-4">
            {/* Global Search */}
            <div className="hidden md:block w-80">
              <GlobalSearch />
            </div>
            <div className="flex items-center gap-2 text-sm text-gray-600 bg-white px-4 py-2 rounded-lg border border-gray-200 shadow-sm">
              <Calendar className="w-4 h-4 text-gray-500" />
              <span className="font-medium">{new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span>
            </div>
          </div>
        </div>

        {/* Stats Grid */}
        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="bg-white rounded-lg p-6 shadow-sm border border-gray-200 animate-pulse">
                <div className="h-4 bg-gray-200 rounded w-1/2 mb-4"></div>
                <div className="h-8 bg-gray-200 rounded w-1/3 mb-2"></div>
                <div className="h-3 bg-gray-200 rounded w-1/4"></div>
              </div>
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <StatCard
              title="Total Followers"
              value={stats.totalFollowers.toString()}
              change={stats.followersChange !== 0 ? `${Math.abs(stats.followersChange).toFixed(1)}%` : "0%"}
              changeType={stats.followersChange >= 0 ? "positive" : "negative"}
            />
            <StatCard
              title="Total Customers"
              value={stats.totalCustomers.toString()}
              change={stats.customersChange !== 0 ? `${Math.abs(stats.customersChange).toFixed(1)}%` : "0%"}
              changeType={stats.customersChange >= 0 ? "positive" : "negative"}
            />
            <StatCard
              title="Total Impressions"
              value={stats.totalImpressions.toString()}
              change={stats.impressionsChange !== 0 ? `${Math.abs(stats.impressionsChange).toFixed(1)}%` : "0%"}
              changeType={stats.impressionsChange >= 0 ? "positive" : "negative"}
            />
            <StatCard
              title="Total Notifications"
              value={stats.totalNotifications.toString()}
              change={stats.notificationsChange !== 0 ? `${Math.abs(stats.notificationsChange).toFixed(1)}%` : "0%"}
              changeType={stats.notificationsChange >= 0 ? "positive" : "negative"}
            />
          </div>
        )}

        {/* Charts Row */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          <div className="lg:col-span-2">
            <ViewersChart />
          </div>
          <div>
            <ImpressionsChart />
          </div>
        </div>

        {/* Notifications Table */}
        <NotificationsTable />
      </main>
    </div>
  );
}
