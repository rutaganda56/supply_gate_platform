import React, { useState, useEffect } from "react";
import { LineChart, Line, XAxis, YAxis, ResponsiveContainer, Tooltip } from "recharts";
import { Loader2 } from "lucide-react";
import { dashboardApi } from "../lib/api";

export function ImpressionsChart() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadChartData = async () => {
      try {
        setLoading(true);
        const chartData = await dashboardApi.getImpressionsChartData();
        setData(chartData);
      } catch (err) {
        if (process.env.NODE_ENV === 'development') {
          console.error("Failed to load impressions chart data:", err);
        }
        // Fallback to empty data
        setData([]);
      } finally {
        setLoading(false);
      }
    };

    loadChartData();
    
    // Refresh chart data every 30 seconds to show real-time updates
    const interval = setInterval(() => {
      loadChartData();
    }, 30000);

    return () => clearInterval(interval);
  }, []);

  return (
    <div className="bg-gradient-to-br from-[#1a3a3a] to-[#2a4a4a] rounded-lg p-6 shadow-lg">
      <div className="mb-4">
        <h3 className="font-semibold text-lg text-white mb-1">Impressions</h3>
        <p className="text-xs text-white/70">Impressions Progress (includes product views & likes)</p>
      </div>
      {loading ? (
        <div className="h-48 flex items-center justify-center">
          <Loader2 className="w-6 h-6 animate-spin text-white" />
        </div>
      ) : data.length === 0 ? (
        <div className="h-48 flex items-center justify-center text-white/70 text-sm">
          No data available
        </div>
      ) : (
        <div className="h-48">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data} margin={{ top: 5, right: 10, left: 0, bottom: 5 }}>
              <XAxis
                dataKey="name"
                axisLine={false}
                tickLine={false}
                tick={{ fill: "#e5e7eb", fontSize: 11 }}
              />
              <YAxis
                axisLine={false}
                tickLine={false}
                tick={{ fill: "#e5e7eb", fontSize: 11 }}
                label={{ value: "Total Impressions", angle: -90, position: "insideLeft", fill: "#e5e7eb", fontSize: 11 }}
              />
              <Tooltip 
                contentStyle={{ 
                  backgroundColor: '#1a3a3a', 
                  border: '1px solid rgba(255,255,255,0.1)',
                  borderRadius: '6px',
                  fontSize: '12px',
                  color: '#fff'
                }}
                labelStyle={{ color: '#e5e7eb' }}
              />
              <Line
                type="monotone"
                dataKey="value"
                stroke="#4ade80"
                strokeWidth={3}
                dot={{ fill: "#4ade80", strokeWidth: 2, stroke: "#fff", r: 4 }}
                activeDot={{ r: 6 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}
      <div className="flex items-center gap-2 mt-4 pt-4 border-t border-white/10">
        <div className="w-3 h-3 bg-green-400 rounded-full shadow-sm" />
        <span className="text-xs text-white/90 font-medium">Impressions per period (views + likes)</span>
      </div>
    </div>
  );
}
