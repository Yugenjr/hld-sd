export type Role = "ROLE_USER" | "ROLE_ADMIN";

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

export type StationStatus = "OPERATIONAL" | "MAINTENANCE" | "CLOSED";
export type ConnectorType = "CCS2" | "NACS" | "CHADEMO" | "TYPE2";
export type ChargerStatus = "AVAILABLE" | "RESERVED" | "CHARGING" | "OFFLINE" | "OUT_OF_SERVICE";

export interface Charger {
  id: number;
  stationId: number;
  stationName: string;
  connectorType: ConnectorType;
  maxPowerKw: number;
  status: ChargerStatus;
  pricePerKwh: number;
}

export interface ChargingStation {
  id: number;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  operationalStatus: StationStatus;
  amenities: string;
  totalChargers: number;
  availableChargers: number;
  chargers: Charger[];
}

export type ReservationStatus = "CONFIRMED" | "CANCELLED" | "EXPIRED" | "CONSUMED";

export interface Reservation {
  id: number;
  userId: number;
  userName: string;
  chargerId: number;
  stationId: number;
  stationName: string;
  stationAddress: string;
  connectorType: ConnectorType;
  maxPowerKw: number;
  pricePerKwh: number;
  startTime: string;
  expiryTime: string;
  status: ReservationStatus;
  idempotencyKey?: string;
  createdAt: string;
}

export type SessionStatus = "CHARGING" | "COMPLETED" | "FAILED";

export interface ChargingSession {
  id: number;
  reservationId?: number;
  userId: number;
  userName: string;
  chargerId: number;
  stationId: number;
  stationName: string;
  stationAddress: string;
  connectorType: ConnectorType;
  maxPowerKw: number;
  pricePerKwh: number;
  startTime: string;
  endTime?: string;
  status: SessionStatus;
  energyConsumedKwh: number;
  startBatteryPct: number;
  currentBatteryPct: number;
  totalCost: number;
  elapsedSeconds: number;
}

export interface TelemetryData {
  sessionId: number;
  status: SessionStatus;
  elapsedSeconds: number;
  currentBatteryPct: number;
  energyDeliveredKwh: number;
  currentCost: number;
  currentPowerKw: number;
  simulated: boolean;
}

export interface DashboardMetrics {
  availableChargers: number;
  totalStations: number;
  activeReservations: number;
  activeChargingSessions: number;
  totalEnergyDeliveredKwh: number;
  userTotalCost: number;
  activeSession: ChargingSession | null;
  recentSessions: ChargingSession[];
  recentReservations: Reservation[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
