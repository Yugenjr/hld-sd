"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { Navbar } from "../../../components/Navbar";
import { api, ApiError } from "../../../lib/api";
import { ChargingSession, TelemetryData } from "../../../types";
import { formatCurrency, formatDuration, formatKwh } from "../../../lib/utils";
import {
  BatteryCharging,
  Zap,
  Clock,
  DollarSign,
  Square,
  Loader2,
  AlertCircle,
  CheckCircle2,
  Cpu,
  ChevronLeft,
} from "lucide-react";

export default function LiveChargingMonitorPage() {
  const params = useParams();
  const router = useRouter();
  const sessionId = Number(params?.sessionId);

  const [session, setSession] = useState<ChargingSession | null>(null);
  const [telemetry, setTelemetry] = useState<TelemetryData | null>(null);
  const [loading, setLoading] = useState(true);
  const [ending, setEnding] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [completedSummary, setCompletedSummary] = useState<ChargingSession | null>(null);

  const loadData = async () => {
    try {
      const [sessionData, telemetryData] = await Promise.all([
        api.getSessionById(sessionId),
        api.getSessionTelemetry(sessionId),
      ]);
      setSession(sessionData);
      setTelemetry(telemetryData);

      if (sessionData.status === "COMPLETED") {
        setCompletedSummary(sessionData);
      }
    } catch (err: unknown) {
      if (err instanceof ApiError && err.status === 401) {
        router.push("/auth");
      } else {
        setError("Could not stream charger telemetry. Session may not exist.");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    const interval = setInterval(() => {
      if (!completedSummary) {
        api.getSessionTelemetry(sessionId)
          .then((tel) => {
            setTelemetry(tel);
            if (tel.status === "COMPLETED") {
              api.getSessionById(sessionId).then(setCompletedSummary);
            }
          })
          .catch(() => {});
      }
    }, 2000); // 2 second live telemetry poll

    return () => clearInterval(interval);
  }, [sessionId, completedSummary]);

  const handleEndSession = async () => {
    if (!confirm("Are you sure you want to stop charging?")) return;
    setEnding(true);
    setError(null);
    try {
      const result = await api.endSession(sessionId);
      setCompletedSummary(result);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Failed to end session.");
      }
    } finally {
      setEnding(false);
    }
  };

  const batteryPct = telemetry?.currentBatteryPct ?? session?.currentBatteryPct ?? 20;

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col">
      <Navbar />

      <main className="flex-1 max-w-5xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* Back Link */}
        <Link
          href="/dashboard"
          className="inline-flex items-center space-x-1 text-xs text-slate-400 hover:text-slate-200 transition-colors"
        >
          <ChevronLeft className="w-4 h-4" />
          <span>Back to Dashboard</span>
        </Link>

        {error && (
          <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-400 text-sm flex items-center space-x-3">
            <AlertCircle className="w-5 h-5 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {loading ? (
          <div className="py-24 flex flex-col items-center justify-center space-y-3">
            <Loader2 className="w-8 h-8 text-emerald-400 animate-spin" />
            <p className="text-sm text-slate-400">Connecting to charger hardware bus...</p>
          </div>
        ) : session ? (
          <div className="space-y-6">
            {/* Simulation Notice Banner */}
            <div className="flex items-center justify-between p-3.5 bg-slate-900/80 border border-emerald-500/30 rounded-xl text-xs text-slate-300">
              <div className="flex items-center space-x-2">
                <Cpu className="w-4 h-4 text-emerald-400" />
                <span>
                  <strong>High-Fidelity Telemetry Simulation:</strong> Deterministic energy calculus & SoC progression active.
                </span>
              </div>
              <span className="hidden sm:inline-block px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 font-mono text-[10px]">
                15x Time-Warp
              </span>
            </div>

            {/* Core Telemetry Gauge Card */}
            <div className="glass-panel p-8 rounded-3xl border-slate-800 text-center relative overflow-hidden space-y-6">
              <div className="flex items-center justify-between">
                <div className="text-left">
                  <div className="flex items-center space-x-2">
                    <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-emerald-500/20 text-emerald-400">
                      {session.status}
                    </span>
                    <span className="text-xs text-slate-400">
                      Session #{session.id}
                    </span>
                  </div>
                  <h2 className="text-xl font-bold text-white mt-1">
                    {session.stationName}
                  </h2>
                  <p className="text-xs text-slate-400">
                    Charger #{session.chargerId} • {session.connectorType} ({session.maxPowerKw} kW)
                  </p>
                </div>

                {session.status === "CHARGING" && (
                  <button
                    onClick={handleEndSession}
                    disabled={ending}
                    className="inline-flex items-center space-x-2 px-4 py-2 rounded-xl bg-rose-600 hover:bg-rose-500 text-white text-xs font-semibold transition-all shadow-lg shadow-rose-600/25 disabled:opacity-50"
                  >
                    {ending ? (
                      <Loader2 className="w-3.5 h-3.5 animate-spin" />
                    ) : (
                      <>
                        <Square className="w-3.5 h-3.5 fill-current" />
                        <span>Stop Charging</span>
                      </>
                    )}
                  </button>
                )}
              </div>

              {/* Circular Gauge / Center Ring */}
              <div className="relative w-64 h-64 mx-auto flex items-center justify-center my-4">
                {/* SVG Progress Ring */}
                <svg className="w-full h-full -rotate-90 transform" viewBox="0 0 100 100">
                  <circle
                    cx="50"
                    cy="50"
                    r="42"
                    className="stroke-slate-800"
                    strokeWidth="8"
                    fill="transparent"
                  />
                  <circle
                    cx="50"
                    cy="50"
                    r="42"
                    className="stroke-emerald-500 transition-all duration-1000 ease-out"
                    strokeWidth="8"
                    strokeDasharray={264}
                    strokeDashoffset={264 - (264 * Math.min(100, batteryPct)) / 100}
                    strokeLinecap="round"
                    fill="transparent"
                  />
                </svg>

                {/* Center Content */}
                <div className="absolute inset-0 flex flex-col items-center justify-center">
                  <div className="w-10 h-10 rounded-full bg-emerald-500/20 flex items-center justify-center mb-1">
                    <Zap className="w-5 h-5 text-emerald-400" />
                  </div>
                  <span className="text-4xl font-extrabold text-white tracking-tight">
                    {batteryPct.toFixed(1)}%
                  </span>
                  <span className="text-xs font-medium text-slate-400 mt-1 uppercase tracking-wider">
                    Battery State of Charge
                  </span>
                </div>
              </div>

              {/* 4 Telemetry Metrics Grid */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-left pt-4 border-t border-slate-800">
                <div className="bg-slate-900/90 p-4 rounded-2xl border border-slate-800">
                  <div className="flex items-center space-x-2 text-slate-400 text-xs mb-1">
                    <Zap className="w-3.5 h-3.5 text-emerald-400" />
                    <span>Delivered Energy</span>
                  </div>
                  <div className="text-lg font-bold text-white">
                    {formatKwh(telemetry?.energyDeliveredKwh ?? session.energyConsumedKwh)}
                  </div>
                </div>

                <div className="bg-slate-900/90 p-4 rounded-2xl border border-slate-800">
                  <div className="flex items-center space-x-2 text-slate-400 text-xs mb-1">
                    <BatteryCharging className="w-3.5 h-3.5 text-cyan-400" />
                    <span>Active Power Rate</span>
                  </div>
                  <div className="text-lg font-bold text-white">
                    {telemetry?.currentPowerKw ?? session.maxPowerKw} kW
                  </div>
                </div>

                <div className="bg-slate-900/90 p-4 rounded-2xl border border-slate-800">
                  <div className="flex items-center space-x-2 text-slate-400 text-xs mb-1">
                    <Clock className="w-3.5 h-3.5 text-amber-400" />
                    <span>Elapsed Duration</span>
                  </div>
                  <div className="text-lg font-bold text-white">
                    {formatDuration(telemetry?.elapsedSeconds ?? session.elapsedSeconds)}
                  </div>
                </div>

                <div className="bg-slate-900/90 p-4 rounded-2xl border border-slate-800">
                  <div className="flex items-center space-x-2 text-slate-400 text-xs mb-1">
                    <DollarSign className="w-3.5 h-3.5 text-purple-400" />
                    <span>Current Session Cost</span>
                  </div>
                  <div className="text-lg font-bold text-emerald-400">
                    {formatCurrency(Number(telemetry?.currentCost ?? session.totalCost))}
                  </div>
                </div>
              </div>
            </div>

            {/* Session Summary Modal / Card upon completion */}
            {completedSummary && (
              <div className="glass-panel p-6 rounded-2xl border-emerald-500/40 bg-emerald-950/20 space-y-4">
                <div className="flex items-center space-x-3 text-emerald-400">
                  <CheckCircle2 className="w-6 h-6" />
                  <h3 className="text-lg font-bold text-white">
                    Charging Session Completed
                  </h3>
                </div>
                <p className="text-xs text-slate-300">
                  Your charger slot has been released back to the grid. Here is your final session breakdown:
                </p>

                <div className="bg-slate-900 p-4 rounded-xl border border-slate-800 grid grid-cols-3 gap-4 text-center">
                  <div>
                    <span className="text-xs text-slate-500 block">Total Energy</span>
                    <span className="text-base font-bold text-white">
                      {formatKwh(completedSummary.energyConsumedKwh)}
                    </span>
                  </div>
                  <div>
                    <span className="text-xs text-slate-500 block">Duration</span>
                    <span className="text-base font-bold text-white">
                      {formatDuration(completedSummary.elapsedSeconds)}
                    </span>
                  </div>
                  <div>
                    <span className="text-xs text-slate-500 block">Final Cost</span>
                    <span className="text-base font-bold text-emerald-400">
                      {formatCurrency(completedSummary.totalCost)}
                    </span>
                  </div>
                </div>

                <div className="flex justify-end space-x-3 pt-2">
                  <Link
                    href="/history"
                    className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold transition-colors"
                  >
                    View All Invoices in History &rarr;
                  </Link>
                </div>
              </div>
            )}
          </div>
        ) : null}
      </main>
    </div>
  );
}
