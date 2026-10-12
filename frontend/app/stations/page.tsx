"use client";

import React, { useEffect, useState, useTransition } from "react";
import { useRouter } from "next/navigation";
import { Navbar } from "../../components/Navbar";
import { api, ApiError } from "../../lib/api";
import { ChargingStation, Charger, StationStatus } from "../../types";
import { formatCurrency } from "../../lib/utils";
import {
  Search,
  MapPin,
  Zap,
  CheckCircle2,
  AlertTriangle,
  XCircle,
  Filter,
  Loader2,
  Calendar,
  Sparkles,
  Coffee,
  Wifi,
  Shield,
  Layers,
} from "lucide-react";

export default function StationsPage() {
  const router = useRouter();
  const [stations, setStations] = useState<ChargingStation[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<StationStatus | "ALL">("ALL");

  // Reservation Modal State
  const [selectedCharger, setSelectedCharger] = useState<Charger | null>(null);
  const [selectedStation, setSelectedStation] = useState<ChargingStation | null>(null);
  const [durationMinutes, setDurationMinutes] = useState(30);
  const [reserving, setReserving] = useState(false);
  const [reserveError, setReserveError] = useState<string | null>(null);
  const [reserveSuccess, setReserveSuccess] = useState<string | null>(null);

  const fetchStations = async () => {
    setLoading(true);
    try {
      const data = await api.getStations({
        query: searchQuery || undefined,
        status: statusFilter === "ALL" ? undefined : statusFilter,
      });
      // Fetch full details (with chargers) for each station
      const detailed = await Promise.all(
        data.content.map((s) => api.getStationById(s.id).catch(() => s))
      );
      setStations(detailed);
    } catch (err: unknown) {
      if (err instanceof ApiError && err.status === 401) {
        router.push("/auth");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStations();
  }, [statusFilter]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchStations();
  };

  const handleReserve = async () => {
    if (!selectedCharger) return;
    setReserving(true);
    setReserveError(null);
    setReserveSuccess(null);

    const idempotencyKey = `res-${Date.now()}-${Math.random().toString(36).substring(7)}`;

    try {
      await api.createReservation({
        chargerId: selectedCharger.id,
        durationMinutes,
        idempotencyKey,
      });
      setReserveSuccess("Charger successfully reserved! Redirecting to reservations...");
      setTimeout(() => {
        setSelectedCharger(null);
        router.push("/reservations");
      }, 1200);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setReserveError(err.message);
      } else {
        setReserveError("Failed to book reservation. Please try again.");
      }
    } finally {
      setReserving(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col">
      <Navbar />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            Station Discovery & Charging Grid
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Real-time availability, power capabilities, and verified connector pricing
          </p>
        </div>

        {/* Search and Filters Bar */}
        <div className="glass-panel p-4 rounded-2xl flex flex-col md:flex-row gap-3 items-center justify-between">
          <form onSubmit={handleSearchSubmit} className="w-full md:w-96 relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3.5" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search by station name or city..."
              className="w-full pl-9 pr-20 py-2.5 bg-slate-900 border border-slate-700/80 rounded-xl text-sm text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            />
            <button
              type="submit"
              className="absolute right-1.5 top-1.5 px-3 py-1 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg text-xs font-semibold transition-colors"
            >
              Search
            </button>
          </form>

          {/* Status Filter */}
          <div className="flex items-center space-x-2 self-start md:self-auto">
            <Filter className="w-4 h-4 text-slate-400" />
            <span className="text-xs text-slate-400 font-medium">Status:</span>
            <div className="flex bg-slate-900 p-1 rounded-xl border border-slate-800">
              {(["ALL", "OPERATIONAL", "MAINTENANCE"] as const).map((status) => (
                <button
                  key={status}
                  onClick={() => setStatusFilter(status)}
                  className={`px-3 py-1 rounded-lg text-xs font-medium transition-all ${
                    statusFilter === status
                      ? "bg-emerald-600 text-white shadow-sm"
                      : "text-slate-400 hover:text-white"
                  }`}
                >
                  {status === "ALL" ? "All Stations" : status}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Stations Grid */}
        {loading ? (
          <div className="py-24 flex flex-col items-center justify-center space-y-3">
            <Loader2 className="w-8 h-8 text-emerald-400 animate-spin" />
            <p className="text-sm text-slate-400">Discovering stations...</p>
          </div>
        ) : stations.length === 0 ? (
          <div className="glass-panel p-12 text-center rounded-2xl space-y-3">
            <MapPin className="w-10 h-10 text-slate-600 mx-auto" />
            <h3 className="text-lg font-bold text-white">No stations found</h3>
            <p className="text-sm text-slate-400">
              Try adjusting your search criteria or clearing filters.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {stations.map((station) => (
              <div
                key={station.id}
                className="glass-panel p-6 rounded-2xl border-slate-800 hover:border-slate-700 transition-all space-y-5"
              >
                {/* Station Header */}
                <div className="flex items-start justify-between">
                  <div>
                    <div className="flex items-center space-x-2">
                      <h2 className="text-lg font-bold text-white">{station.name}</h2>
                      <span
                        className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                          station.operationalStatus === "OPERATIONAL"
                            ? "bg-emerald-500/20 text-emerald-300"
                            : "bg-amber-500/20 text-amber-300"
                        }`}
                      >
                        {station.operationalStatus}
                      </span>
                    </div>
                    <div className="flex items-center space-x-1.5 text-xs text-slate-400 mt-1">
                      <MapPin className="w-3.5 h-3.5 text-slate-500 flex-shrink-0" />
                      <span>{station.address}</span>
                    </div>
                  </div>

                  <div className="text-right">
                    <span className="text-xs font-semibold px-2.5 py-1 rounded-xl bg-slate-800 text-slate-300 border border-slate-700">
                      <strong className="text-emerald-400">{station.availableChargers}</strong> / {station.totalChargers} Available
                    </span>
                  </div>
                </div>

                {/* Amenities */}
                {station.amenities && (
                  <div className="flex flex-wrap gap-1.5 text-[11px] text-slate-300">
                    {station.amenities.split(",").map((amenity, i) => (
                      <span
                        key={i}
                        className="px-2 py-0.5 rounded-md bg-slate-900 border border-slate-800 text-slate-400"
                      >
                        {amenity.trim()}
                      </span>
                    ))}
                  </div>
                )}

                {/* Charger Slots */}
                <div className="space-y-2 pt-2 border-t border-slate-800/80">
                  <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                    Equipped Chargers
                  </div>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                    {station.chargers?.map((charger) => {
                      const isAvailable = charger.status === "AVAILABLE";
                      return (
                        <div
                          key={charger.id}
                          className={`p-3 rounded-xl border transition-all ${
                            isAvailable
                              ? "bg-slate-900/90 border-slate-700/80 hover:border-emerald-500/50"
                              : "bg-slate-900/40 border-slate-800 opacity-60"
                          }`}
                        >
                          <div className="flex items-center justify-between">
                            <span className="text-xs font-bold text-white">
                              {charger.connectorType}
                            </span>
                            <span className="text-[11px] font-semibold text-emerald-400">
                              {charger.maxPowerKw} kW
                            </span>
                          </div>

                          <div className="flex items-center justify-between mt-2 pt-2 border-t border-slate-800 text-xs">
                            <span className="text-slate-400">
                              {formatCurrency(charger.pricePerKwh)}/kWh
                            </span>

                            {isAvailable ? (
                              <button
                                onClick={() => {
                                  setSelectedCharger(charger);
                                  setSelectedStation(station);
                                  setReserveError(null);
                                  setReserveSuccess(null);
                                }}
                                className="px-2.5 py-1 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-[11px] transition-colors shadow-sm"
                              >
                                Reserve
                              </button>
                            ) : (
                              <span className="text-[11px] text-slate-500 font-medium">
                                {charger.status}
                              </span>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Reservation Modal */}
        {selectedCharger && selectedStation && (
          <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
            <div className="glass-panel max-w-md w-full p-6 rounded-2xl border-slate-700 space-y-5 animate-in fade-in zoom-in-95">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <Calendar className="w-5 h-5 text-emerald-400" />
                  <h3 className="text-lg font-bold text-white">Confirm Charger Hold</h3>
                </div>
                <button
                  onClick={() => setSelectedCharger(null)}
                  className="text-slate-400 hover:text-white text-sm"
                >
                  ✕
                </button>
              </div>

              <div className="bg-slate-900 p-4 rounded-xl border border-slate-800 space-y-2 text-sm">
                <div>
                  <div className="text-xs text-slate-400">Station</div>
                  <div className="font-semibold text-white">{selectedStation.name}</div>
                  <div className="text-xs text-slate-400">{selectedStation.address}</div>
                </div>
                <div className="pt-2 border-t border-slate-800 flex justify-between">
                  <div>
                    <div className="text-xs text-slate-400">Connector</div>
                    <div className="font-semibold text-emerald-400">
                      {selectedCharger.connectorType} • {selectedCharger.maxPowerKw} kW
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="text-xs text-slate-400">Rate</div>
                    <div className="font-semibold text-white">
                      {formatCurrency(selectedCharger.pricePerKwh)} / kWh
                    </div>
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Hold Duration (Minutes)
                </label>
                <select
                  value={durationMinutes}
                  onChange={(e) => setDurationMinutes(Number(e.target.value))}
                  className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                >
                  <option value={15}>15 Minutes</option>
                  <option value={30}>30 Minutes (Recommended)</option>
                  <option value={45}>45 Minutes</option>
                  <option value={60}>60 Minutes</option>
                </select>
                <p className="text-[11px] text-slate-400 mt-1">
                  Charger will remain locked for you. Auto-releases if session is not started.
                </p>
              </div>

              {reserveError && (
                <div className="p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl text-rose-400 text-xs">
                  {reserveError}
                </div>
              )}

              {reserveSuccess && (
                <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 rounded-xl text-emerald-400 text-xs">
                  {reserveSuccess}
                </div>
              )}

              <div className="flex space-x-3 pt-2">
                <button
                  type="button"
                  onClick={() => setSelectedCharger(null)}
                  className="flex-1 py-2.5 rounded-xl border border-slate-700 text-slate-300 hover:bg-slate-800 text-sm font-semibold transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  disabled={reserving}
                  onClick={handleReserve}
                  className="flex-1 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-sm font-semibold transition-colors shadow-lg shadow-emerald-600/30 flex items-center justify-center space-x-2 disabled:opacity-50"
                >
                  {reserving ? (
                    <Loader2 className="w-4 h-4 animate-spin" />
                  ) : (
                    <span>Confirm Hold</span>
                  )}
                </button>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
