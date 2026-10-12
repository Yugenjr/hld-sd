import {
  AuthResponse,
  ChargingSession,
  ChargingStation,
  DashboardMetrics,
  PageResponse,
  Reservation,
  TelemetryData,
  User,
} from "../types";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api/v1";

class ApiError extends Error {
  status: number;
  errorCode?: string;
  validationDetails?: Record<string, string>;

  constructor(
    message: string,
    status: number,
    errorCode?: string,
    validationDetails?: Record<string, string>
  ) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.errorCode = errorCode;
    this.validationDetails = validationDetails;
  }
}

function getAuthHeader(): Record<string, string> {
  if (typeof window === "undefined") return {};
  const token = localStorage.getItem("voltflow_token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  const headers = {
    "Content-Type": "application/json",
    ...getAuthHeader(),
    ...options.headers,
  };

  const response = await fetch(url, { ...options, headers });
  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = data?.message || `Request failed with status ${response.status}`;
    throw new ApiError(
      errorMsg,
      response.status,
      data?.errorCode,
      data?.validationDetails
    );
  }

  return (data?.data !== undefined ? data.data : data) as T;
}

export const api = {
  // Auth
  register: (body: { name: string; email: string; password: string }) =>
    request<AuthResponse>("/auth/register", {
      method: "POST",
      body: JSON.stringify(body),
    }),

  login: (body: { email: string; password: string }) =>
    request<AuthResponse>("/auth/login", {
      method: "POST",
      body: JSON.stringify(body),
    }),

  getMe: () => request<User>("/auth/me"),

  // Stations
  getStations: (params?: { query?: string; status?: string; page?: number; size?: number }) => {
    const searchParams = new URLSearchParams();
    if (params?.query) searchParams.set("query", params.query);
    if (params?.status) searchParams.set("status", params.status);
    if (params?.page !== undefined) searchParams.set("page", params.page.toString());
    if (params?.size !== undefined) searchParams.set("size", params.size.toString());
    const queryStr = searchParams.toString();
    return request<PageResponse<ChargingStation>>(`/stations${queryStr ? `?${queryStr}` : ""}`);
  },

  getStationById: (id: number) => request<ChargingStation>(`/stations/${id}`),

  // Reservations
  createReservation: (body: { chargerId: number; durationMinutes?: number; idempotencyKey?: string }) =>
    request<Reservation>("/reservations", {
      method: "POST",
      body: JSON.stringify(body),
    }),

  getReservations: () => request<Reservation[]>("/reservations"),

  getReservationById: (id: number) => request<Reservation>(`/reservations/${id}`),

  cancelReservation: (id: number) =>
    request<Reservation>(`/reservations/${id}/cancel`, {
      method: "POST",
    }),

  // Charging Sessions
  startSession: (reservationId: number) =>
    request<ChargingSession>(`/reservations/${reservationId}/start`, {
      method: "POST",
    }),

  getSessions: () => request<ChargingSession[]>("/sessions"),

  getSessionById: (id: number) => request<ChargingSession>(`/sessions/${id}`),

  getSessionTelemetry: (id: number) => request<TelemetryData>(`/sessions/${id}/status`),

  endSession: (id: number) =>
    request<ChargingSession>(`/sessions/${id}/end`, {
      method: "POST",
    }),

  // Dashboard
  getDashboardMetrics: () => request<DashboardMetrics>("/dashboard/metrics"),
};

export { ApiError };
