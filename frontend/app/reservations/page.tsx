"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Navbar } from "../../components/Navbar";
import { api, ApiError } from "../../lib/api";
import { Reservation } from "../../types";
import { formatCurrency, formatDateTime } from "../../lib/utils";
import {
  Calendar,
  Clock,
  Play,
  XCircle,
  AlertCircle,
  Loader2,
  CheckCircle2,
  ArrowRight,
  Zap,
} from "lucide-react";

export default function ReservationsPage() {
  const router = useRouter();
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<number | null>(null);

  const fetchReservations = async () => {
    try {
      const data = await api.getReservations();
      setReservations(data);
    } catch (err: unknown) {
      if (err instanceof ApiError && err.status === 401) {
        router.push("/auth");
      } else {
        setError("Failed to load reservations.");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReservations();
  }, [router]);

  const handleStartCharging = async (reservationId: number) => {
    setActionLoadingId(reservationId);
    setError(null);
    try {
      const session = await api.startSession(reservationId);
      router.push(`/charging/${session.id}`);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Failed to initiate charging session.");
      }
      setActionLoadingId(null);
    }
  };

  const handleCancelReservation = async (reservationId: number) => {
    if (!confirm("Are you sure you want to cancel this reservation?")) return;
    setActionLoadingId(reservationId);
    setError(null);
    try {
      await api.cancelReservation(reservationId);
      await fetchReservations();
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Failed to cancel reservation.");
      }
    } finally {
      setActionLoadingId(null);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col">
      <Navbar />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Reservations & Charger Holds
            </h1>
            <p className="text-sm text-slate-400 mt-1">
              Active booking holds, time windows, and instant charging dispatch
            </p>
          </div>
          <Link
            href="/stations"
            className="inline-flex items-center space-x-2 px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-white font-medium text-xs transition-colors border border-slate-700"
          >
            <span>Book New Charger</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
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
            <p className="text-sm text-slate-400">Loading your reservations...</p>
          </div>
        ) : reservations.length === 0 ? (
          <div className="glass-panel p-16 text-center rounded-2xl space-y-4">
            <div className="w-16 h-16 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center mx-auto text-emerald-400">
              <Calendar className="w-8 h-8" />
            </div>
            <h3 className="text-lg font-bold text-white">No reservations found</h3>
            <p className="text-sm text-slate-400 max-w-sm mx-auto">
              You haven&apos;t reserved any chargers yet. Discover stations to reserve an available charger before arrival.
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
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {reservations.map((res) => {
              const isConfirmed = res.status === "CONFIRMED";
              const isActioning = actionLoadingId === res.id;

              return (
                <div
                  key={res.id}
                  className={`glass-panel p-6 rounded-2xl border transition-all space-y-4 ${
                    isConfirmed
                      ? "border-emerald-500/40 shadow-lg shadow-emerald-500/5"
                      : "border-slate-800 opacity-80"
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="font-bold text-base text-white">
                          {res.stationName}
                        </span>
                        <span
                          className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                            res.status === "CONFIRMED"
                              ? "bg-emerald-500/20 text-emerald-300"
                              : res.status === "CONSUMED"
                              ? "bg-cyan-500/20 text-cyan-300"
                              : res.status === "CANCELLED"
                              ? "bg-rose-500/20 text-rose-300"
                              : "bg-slate-700 text-slate-300"
                          }`}
                        >
                          {res.status}
                        </span>
                      </div>
                      <p className="text-xs text-slate-400 mt-1">
                        {res.stationAddress}
                      </p>
                    </div>

                    <span className="text-xs font-mono text-slate-500">
                      #{res.id}
                    </span>
                  </div>

                  <div className="bg-slate-900/80 p-3.5 rounded-xl border border-slate-800 grid grid-cols-2 gap-3 text-xs">
                    <div>
                      <span className="text-slate-500 block">Charger Slot</span>
                      <span className="font-semibold text-white">
                        #{res.chargerId} • {res.connectorType}
                      </span>
                    </div>
                    <div>
                      <span className="text-slate-500 block">Charging Rate</span>
                      <span className="font-semibold text-emerald-400">
                        {res.maxPowerKw} kW ({formatCurrency(res.pricePerKwh)}/kWh)
                      </span>
                    </div>
                    <div>
                      <span className="text-slate-500 block">Booked At</span>
                      <span className="text-slate-300">
                        {formatDateTime(res.startTime)}
                      </span>
                    </div>
                    <div>
                      <span className="text-slate-500 block">Hold Expiration</span>
                      <span className="text-amber-300 font-medium">
                        {formatDateTime(res.expiryTime)}
                      </span>
                    </div>
                  </div>

                  {isConfirmed && (
                    <div className="flex items-center space-x-3 pt-2">
                      <button
                        onClick={() => handleCancelReservation(res.id)}
                        disabled={isActioning}
                        className="flex-1 py-2 rounded-xl border border-slate-700 hover:border-rose-500/50 hover:bg-rose-500/10 text-slate-300 hover:text-rose-300 text-xs font-semibold transition-all flex items-center justify-center space-x-1.5"
                      >
                        <XCircle className="w-3.5 h-3.5" />
                        <span>Cancel Hold</span>
                      </button>

                      <button
                        onClick={() => handleStartCharging(res.id)}
                        disabled={isActioning}
                        className="flex-1 py-2 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-500 hover:from-emerald-500 hover:to-teal-400 text-white text-xs font-semibold transition-all shadow-md shadow-emerald-500/20 flex items-center justify-center space-x-1.5"
                      >
                        {isActioning ? (
                          <Loader2 className="w-3.5 h-3.5 animate-spin" />
                        ) : (
                          <>
                            <Play className="w-3.5 h-3.5 fill-current" />
                            <span>Start Charging Now</span>
                          </>
                        )}
                      </button>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </main>
    </div>
  );
}
