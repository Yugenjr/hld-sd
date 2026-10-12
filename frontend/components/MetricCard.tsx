import React from "react";
import { LucideIcon } from "lucide-react";

interface MetricCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: LucideIcon;
  badge?: string;
  color?: "emerald" | "blue" | "amber" | "purple";
}

export function MetricCard({
  title,
  value,
  subtitle,
  icon: Icon,
  badge,
  color = "emerald",
}: MetricCardProps) {
  const colorMap = {
    emerald: {
      bg: "bg-emerald-500/10",
      border: "border-emerald-500/20",
      text: "text-emerald-400",
      badge: "bg-emerald-500/20 text-emerald-300",
    },
    blue: {
      bg: "bg-cyan-500/10",
      border: "border-cyan-500/20",
      text: "text-cyan-400",
      badge: "bg-cyan-500/20 text-cyan-300",
    },
    amber: {
      bg: "bg-amber-500/10",
      border: "border-amber-500/20",
      text: "text-amber-400",
      badge: "bg-amber-500/20 text-amber-300",
    },
    purple: {
      bg: "bg-violet-500/10",
      border: "border-violet-500/20",
      text: "text-violet-400",
      badge: "bg-violet-500/20 text-violet-300",
    },
  };

  const scheme = colorMap[color];

  return (
    <div className="glass-panel p-5 rounded-2xl relative overflow-hidden group hover:border-slate-700 transition-all">
      <div className="flex items-center justify-between mb-3">
        <span className="text-xs font-medium text-slate-400 tracking-wide uppercase">
          {title}
        </span>
        <div
          className={`w-9 h-9 rounded-xl flex items-center justify-center ${scheme.bg} ${scheme.border} border`}
        >
          <Icon className={`w-5 h-5 ${scheme.text}`} />
        </div>
      </div>

      <div className="flex items-baseline space-x-2">
        <span className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
          {value}
        </span>
        {badge && (
          <span
            className={`text-[11px] font-semibold px-2 py-0.5 rounded-full ${scheme.badge}`}
          >
            {badge}
          </span>
        )}
      </div>

      {subtitle && (
        <p className="mt-2 text-xs text-slate-400 font-normal">{subtitle}</p>
      )}
    </div>
  );
}
