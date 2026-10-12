"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Navbar } from "../../components/Navbar";
import { api, ApiError } from "../../lib/api";
import { ChargingSession } from "../../types";
import { formatCurrency, formatDateTime, formatDuration, formatKwh } from "../../lib/utils";
import {
  History,
  Receipt,
  Zap,
  Clock,
  ArrowRight,
  Loader2,
  Calendar,
  AlertCircle,
  FileText,
} from "lucide-react";

export default function HistoryPage() {
  const router = useRouter();
  const [sessions, setSessions] = useState<ChargingSession[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedReceipt, setSelectedReceipt] = useState<ChargingSession | null>(null);

  const fetchHistory = async () => {
    try {
      const data = await api.getSessions();
      setSessions(data);
    } catch (err: unknown) {
      if (err instanceof ApiError && err.status === 401) {
        router.push("/auth");
      } else {
        setError("Failed to load charging history.");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, [router]);

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col">
      <Navbar />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Charging Sessions & Billing History
            </h1>
            <p className="text-sm text-slate-400 mt-1">
              Verified energy deliveries, timestamps, and itemized billing statements
            </p>
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
            <p className="text-sm text-slate-400">Loading your charging records...</p>
          </div>
        ) : sessions.length === 0 ? (
          <div className="glass-panel p-16 text-center rounded-2xl space-y-4">
            <div className="w-16 h-16 rounded-2xl bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center mx-auto text-cyan-400">
              <History className="w-8 h-8" />
            </div>
            <h3 className="text-lg font-bold text-white">No charging sessions on record</h3>
            <p className="text-sm text-slate-400 max-w-sm mx-auto">
              You haven&apos;t conducted any charging sessions yet. Reserve a station charger to start powered telemetry.
            </p>
            <Link
              href="/stations"
              className="inline-flex items-center space-x-2 px-5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-sm font-semibold transition-all shadow-lg shadow-emerald-600/20"
            >
              <Zap className="w-4 h-4" />
              <span>Explore Stations & Chargers</span>
            </Link>
          </div>
        ) : (
          <div className="glass-panel rounded-2xl border-slate-800 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm text-slate-300">
                <thead className="bg-slate-900/90 text-xs uppercase tracking-wider text-slate-400 border-b border-slate-800">
                  <tr>
                    <th className="px-6 py-4">Station & Charger</th>
                    <th className="px-6 py-4">Status</th>
                    <th className="px-6 py-4">Date & Time</th>
                    <th className="px-6 py-4">Energy Delivered</th>
                    <th className="px-6 py-4">Duration</th>
                    <th className="px-6 py-4">Total Cost</th>
                    <th className="px-6 py-4 text-right">Invoice</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {sessions.map((session) => (
                    <tr
                      key={session.id}
                      className="hover:bg-slate-900/40 transition-colors"
                    >
                      <td className="px-6 py-4">
                        <div className="font-semibold text-white">
                          {session.stationName}
                        </div>
                        <div className="text-xs text-slate-400">
                          Charger #{session.chargerId} • {session.connectorType} ({session.maxPowerKw} kW)
                        </div>
                      </td>

                      <td className="px-6 py-4">
                        <span
                          className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                            session.status === "COMPLETED"
                              ? "bg-emerald-500/20 text-emerald-300"
                              : session.status === "CHARGING"
                              ? "bg-cyan-500/20 text-cyan-300 animate-pulse"
                              : "bg-slate-700 text-slate-300"
                          }`}
                        >
                          {session.status}
                        </span>
                      </td>

                      <td className="px-6 py-4 text-xs">
                        <div>{formatDateTime(session.startTime)}</div>
                        {session.endTime && (
                          <div className="text-slate-500 text-[11px]">
                            to {formatDateTime(session.endTime)}
                          </div>
                        )}
                      </td>

                      <td className="px-6 py-4 font-semibold text-white">
                        {formatKwh(session.energyConsumedKwh)}
                      </td>

                      <td className="px-6 py-4 text-xs text-slate-300">
                        {formatDuration(session.elapsedSeconds)}
                      </td>

                      <td className="px-6 py-4 font-bold text-emerald-400">
                        {formatCurrency(session.totalCost)}
                      </td>

                      <td className="px-6 py-4 text-right">
                        <button
                          onClick={() => setSelectedReceipt(session)}
                          className="inline-flex items-center space-x-1 px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-medium transition-colors border border-slate-700"
                        >
                          <Receipt className="w-3.5 h-3.5 text-emerald-400" />
                          <span>Receipt</span>
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Itemized Receipt Modal */}
        {selectedReceipt && (
          <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
            <div className="glass-panel max-w-md w-full p-6 rounded-2xl border-slate-700 space-y-5 animate-in fade-in zoom-in-95">
              <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                <div className="flex items-center space-x-2">
                  <FileText className="w-5 h-5 text-emerald-400" />
                  <h3 className="text-lg font-bold text-white">Charging Invoice</h3>
                </div>
                <button
                  onClick={() => setSelectedReceipt(null)}
                  className="text-slate-400 hover:text-white text-sm"
                >
                  ✕
                </button>
              </div>

              <div className="space-y-3 text-xs">
                <div className="flex justify-between">
                  <span className="text-slate-400">Invoice ID</span>
                  <span className="font-mono text-white">INV-{selectedReceipt.id.toString().padStart(6, "0")}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Station</span>
                  <span className="font-semibold text-white">{selectedReceipt.stationName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Charger</span>
                  <span className="text-slate-200">#{selectedReceipt.chargerId} ({selectedReceipt.connectorType})</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Start Time</span>
                  <span className="text-slate-200">{formatDateTime(selectedReceipt.startTime)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Duration</span>
                  <span className="text-slate-200">{formatDuration(selectedReceipt.elapsedSeconds)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Unit Price</span>
                  <span className="text-slate-200">{formatCurrency(selectedReceipt.pricePerKwh)} / kWh</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Energy Delivered</span>
                  <span className="font-semibold text-white">{formatKwh(selectedReceipt.energyConsumedKwh)}</span>
                </div>

                <div className="pt-3 border-t border-slate-800 flex justify-between text-sm">
                  <span className="font-bold text-white">Total Amount Paid</span>
                  <span className="font-extrabold text-emerald-400">{formatCurrency(selectedReceipt.totalCost)}</span>
                </div>
              </div>

              <div className="pt-2">
                <button
                  onClick={() => setSelectedReceipt(null)}
                  className="w-full py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-white text-xs font-semibold transition-colors border border-slate-700"
                >
                  Close Receipt
                </button>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
