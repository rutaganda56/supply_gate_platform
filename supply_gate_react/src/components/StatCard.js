export function StatCard({ title, value, change, changeType }) {
  return (
    <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200 hover:shadow-md transition-shadow">
      <p className="text-sm font-medium text-gray-600 mb-3">
        {title}
      </p>
      <div className="flex items-baseline justify-between">
        <span className="text-3xl font-bold text-gray-900">{value}</span>
        <span
          className={`text-sm font-semibold px-2.5 py-1 rounded-md ${
            changeType === "positive"
              ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
              : "bg-red-50 text-red-700 border border-red-200"
          }`}
        >
          {change}
        </span>
      </div>
    </div>
  );
}
