"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Navbar } from "../../components/Navbar";
import { MetricCard } from "../../components/MetricCard";
import { api, ApiError } from "../../lib/api";
import { DashboardMetrics } from "../../types";
import { formatCurrency, formatDateTime, formatDuration, formatKwh } from "../../lib/utils";
import {
  Zap,
  BatteryCharging,
  MapPin,
  Calendar,
  DollarSign,
  ArrowRight,
  Loader2,
  AlertCircle,
  PlayCircle,
  Clock,
} from "lucide-react";

export default function DashboardPage() {
  const router = useRouter();
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchMetrics = async () => {
    try {
      const data = await api.getDashboardMetrics();
      setMetrics(data);
    } catch (err: unknown) {
      if (err instanceof ApiError && err.status === 401) {
        router.push("/auth");
      } else {
        setError("Failed to load dashboard metrics. Ensure the backend server is reachable.");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const token = localStorage.getItem("voltflow_token");
    if (!token) {
      router.push("/auth");
      return;
    }
    fetchMetrics();
    const interval = setInterval(fetchMetrics, 10000); // 10s auto refresh
    return () => clearInterval(interval);
  }, [router]);

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col">
      <Navbar />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        {/* Welcome & Quick Action Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Command Dashboard
            </h1>
            <p className="text-sm text-slate-400 mt-1">
              Live charging telemetry, reservations, and grid availability
            </p>
          </div>
          <div className="flex items-center space-x-3">
            <Link
              href="/stations"
              className="inline-flex items-center space-x-2 px-4 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-medium text-sm transition-all shadow-lg shadow-emerald-600/25"
            >
              <MapPin className="w-4 h-4" />
              <span>Find Stations</span>
            </Link>
          </div>
        </div>

        {error && (
          <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-400 text-sm flex items-center space-x-3">
            <AlertCircle className="w-5 h-5 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {loading ? (
          <div className="py-24 flex flex-col items-center justify-center space-y-3">
            <Loader2 className="w-8 h-8 text-emerald-400 animate-spin" />
            <p className="text-sm text-slate-400">Loading network telemetry...</p>
          </div>
        ) : metrics ? (
          <>
            {/* Top Metric Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              <MetricCard
                title="Available Chargers"
                value={metrics.availableChargers}
                subtitle="Ready for immediate reservation"
                icon={Zap}
                color="emerald"
                badge="Online"
              />
              <MetricCard
                title="Active Charging"
                value={metrics.activeChargingSessions}
                subtitle="Currently drawing grid power"
                icon={BatteryCharging}
                color="blue"
              />
              <MetricCard
                title="Active Reservations"
                value={metrics.activeReservations}
                subtitle="Chargers held across network"
                icon={Calendar}
                color="amber"
              />
              <MetricCard
                title="Your Energy Delivered"
                value={formatKwh(metrics.totalEnergyDeliveredKwh)}
                subtitle={`Total spent: ${formatCurrency(metrics.userTotalCost)}`}
                icon={DollarSign}
                color="purple"
              />
            </div>

            {/* Active Charging Session Banner (if any) */}
            {metrics.activeSession ? (
              <div className="glass-panel p-6 rounded-2xl border-emerald-500/30 relative overflow-hidden">
                <div className="absolute -right-10 -bottom-10 w-48 h-48 bg-emerald-500/10 rounded-full blur-2xl pointer-events-none" />

                <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 relative z-10">
                  <div className="flex items-center space-x-4">
                    <div className="w-14 h-14 rounded-2xl bg-emerald-500/20 border border-emerald-500/40 flex items-center justify-center relative">
                      <div className="w-3 h-3 rounded-full bg-emerald-400 absolute -top-1 -right-1 charging-pulse" />
                      <BatteryCharging className="w-7 h-7 text-emerald-400" />
                    </div>
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300">
                          Active Session #{metrics.activeSession.id}
                        </span>
                        <span className="text-xs text-slate-400">
                          {metrics.activeSession.stationName}
                        </span>
                      </div>
                      <h3 className="text-lg font-bold text-white mt-1">
                        Charger #{metrics.activeSession.chargerId} ({metrics.activeSession.connectorType} • {metrics.activeSession.maxPowerKw} kW)
                      </h3>
                      <div className="flex items-center space-x-4 text-xs text-slate-300 mt-2">
                        <span>Elapsed: {formatDuration(metrics.activeSession.elapsedSeconds)}</span>
                        <span>•</span>
                        <span>Delivered: {formatKwh(metrics.activeSession.energyConsumedKwh)}</span>
                        <span>•</span>
                        <span>Cost: {formatCurrency(metrics.activeSession.totalCost)}</span>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center space-x-3">
                    <Link
                      href={`/charging/${metrics.activeSession.id}`}
                      className="inline-flex items-center space-x-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-500 hover:from-emerald-500 hover:to-teal-400 text-white font-medium text-sm transition-all shadow-lg shadow-emerald-500/20"
                    >
                      <PlayCircle className="w-4 h-4" />
                      <span>Open Live Monitor</span>
                    </Link>
                  </div>
                </div>
              </div>
            ) : null}

            {/* Split Grid: Recent Reservations & Recent Sessions */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Recent Reservations */}
              <div className="glass-panel p-6 rounded-2xl space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <Calendar className="w-5 h-5 text-amber-400" />
                    <h2 className="text-base font-bold text-white">Your Reservations</h2>
                  </div>
                  <Link
                    href="/reservations"
                    className="text-xs text-emerald-400 hover:text-emerald-300 flex items-center space-x-1"
                  >
                    <span>View all</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Link>
                </div>

                {metrics.recentReservations.length === 0 ? (
                  <div className="py-10 text-center space-y-2">
                    <Calendar className="w-8 h-8 text-slate-600 mx-auto" />
                    <p className="text-sm text-slate-400">No active or previous reservations</p>
                    <Link
                      href="/stations"
                      className="inline-block text-xs text-emerald-400 hover:underline"
                    >
                      Browse stations to book a charger &rarr;
                    </Link>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {metrics.recentReservations.map((res) => (
                      <div
                        key={res.id}
                        className="glass-card p-4 rounded-xl flex items-center justify-between hover:border-slate-600 transition-all"
                      >
                        <div>
                          <div className="flex items-center space-x-2">
                            <span className="font-semibold text-sm text-white">
                              {res.stationName}
                            </span>
                            <span
                              className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                                res.status === "CONFIRMED"
                                  ? "bg-emerald-500/20 text-emerald-300"
                                  : res.status === "CONSUMED"
                                  ? "bg-cyan-500/20 text-cyan-300"
                                  : "bg-slate-700 text-slate-300"
                              }`}
                            >
                              {res.status}
                            </span>
                          </div>
                          <p className="text-xs text-slate-400 mt-1">
                            Charger #{res.chargerId} • {res.connectorType} ({res.maxPowerKw} kW)
                          </p>
                          <div className="flex items-center space-x-2 text-[11px] text-slate-400 mt-1">
                            <Clock className="w-3 h-3" />
                            <span>{formatDateTime(res.startTime)}</span>
                          </div>
                        </div>

                        {res.status === "CONFIRMED" && (
                          <Link
                            href="/reservations"
                            className="text-xs font-semibold px-3 py-1.5 rounded-lg bg-emerald-600/20 hover:bg-emerald-600/30 text-emerald-300 border border-emerald-500/30"
                          >
                            Manage
                          </Link>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Recent Charging Sessions */}
              <div className="glass-panel p-6 rounded-2xl space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <BatteryCharging className="w-5 h-5 text-cyan-400" />
                    <h2 className="text-base font-bold text-white">Recent Charging Sessions</h2>
                  </div>
                  <Link
                    href="/history"
                    className="text-xs text-emerald-400 hover:text-emerald-300 flex items-center space-x-1"
                  >
                    <span>Full history</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Link>
                </div>

                {metrics.recentSessions.length === 0 ? (
                  <div className="py-10 text-center space-y-2">
                    <BatteryCharging className="w-8 h-8 text-slate-600 mx-auto" />
                    <p className="text-sm text-slate-400">No charging sessions yet</p>
                    <p className="text-xs text-slate-400">
                      Reserve an eligible charger to initiate a session.
                    </p>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {metrics.recentSessions.map((session) => (
                      <div
                        key={session.id}
                        className="glass-card p-4 rounded-xl flex items-center justify-between hover:border-slate-600 transition-all"
                      >
                        <div>
                          <div className="flex items-center space-x-2">
                            <span className="font-semibold text-sm text-white">
                              {session.stationName}
                            </span>
                            <span
                              className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                                session.status === "CHARGING"
                                  ? "bg-emerald-500/20 text-emerald-300 animate-pulse"
                                  : "bg-slate-700 text-slate-300"
                              }`}
                            >
                              {session.status}
                            </span>
                          </div>
                          <p className="text-xs text-slate-400 mt-1">
                            {formatKwh(session.energyConsumedKwh)} delivered • {formatDuration(session.elapsedSeconds)}
                          </p>
                          <p className="text-[11px] text-slate-400 mt-0.5">
                            {formatDateTime(session.startTime)}
                          </p>
                        </div>

                        <div className="text-right">
                          <span className="text-sm font-bold text-white">
                            {formatCurrency(session.totalCost)}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </>
        ) : null}
      </main>
    </div>
  );
}
