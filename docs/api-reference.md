# Smart EV Charging Platform — API Reference

Base URL: `http://localhost:8080/api/v1`

Interactive Swagger UI: `http://localhost:8080/swagger-ui.html`
OpenAPI JSON Specification: `http://localhost:8080/api-docs`

---

## 1. Authentication Endpoints

### 1.1 `POST /auth/register`
Register a new EV driver account.

**Request Body:**
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "Password123!"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 3,
      "name": "Jane Doe",
      "email": "jane@example.com",
      "role": "ROLE_USER",
      "createdAt": "2026-10-12T10:00:00Z"
    }
  },
  "timestamp": "2026-10-12T10:00:00Z"
}
```

---

### 1.2 `POST /auth/login`
Authenticate user credentials and receive a JWT Bearer token.

**Request Body:**
```json
{
  "email": "demo@smartev.com",
  "password": "Password123!"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "name": "Alex Morgan",
      "email": "demo@smartev.com",
      "role": "ROLE_USER",
      "createdAt": "2026-10-12T00:00:00Z"
    }
  },
  "timestamp": "2026-10-12T10:01:00Z"
}
```

---

### 1.3 `GET /auth/me`
Retrieve profile of currently authenticated user.
**Headers:** `Authorization: Bearer <token>`

---

## 2. Charging Stations Endpoints

### 2.1 `GET /stations`
List charging stations with optional query and status filters.

**Query Parameters:**
- `query` (optional string): Station name or address search term.
- `status` (optional string): `OPERATIONAL`, `MAINTENANCE`, `CLOSED`.
- `page` (optional int): Default `0`.
- `size` (optional int): Default `10`.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Stations retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Downtown GreenCharge Hub",
        "address": "101 Market St, Financial District, San Francisco, CA",
        "latitude": 37.7937,
        "longitude": -122.3965,
        "operationalStatus": "OPERATIONAL",
        "amenities": "WiFi, Coffee Shop, 24/7 Security, Restrooms",
        "totalChargers": 4,
        "availableChargers": 4,
        "chargers": []
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

---

### 2.2 `GET /stations/{stationId}`
Retrieve station details including all nested chargers and real-time status.

---

### 2.3 `GET /chargers/{chargerId}`
Retrieve real-time status and power specs for an individual charger.

---

## 3. Reservations Endpoints

### 3.1 `POST /reservations`
Reserve an available charger using concurrency-safe database locks.
**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "chargerId": 1,
  "durationMinutes": 30,
  "idempotencyKey": "res-9481-ac91"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Reservation created successfully",
  "data": {
    "id": 10,
    "userId": 1,
    "userName": "Alex Morgan",
    "chargerId": 1,
    "stationId": 1,
    "stationName": "Downtown GreenCharge Hub",
    "stationAddress": "101 Market St, San Francisco, CA",
    "connectorType": "CCS2",
    "maxPowerKw": 150.0,
    "pricePerKwh": 0.38,
    "startTime": "2026-10-12T10:15:00Z",
    "expiryTime": "2026-10-12T10:45:00Z",
    "status": "CONFIRMED",
    "idempotencyKey": "res-9481-ac91",
    "createdAt": "2026-10-12T10:15:00Z"
  }
}
```

---

### 3.2 `POST /reservations/{reservationId}/cancel`
Cancel an active confirmed reservation and immediately release the charger back to the grid.

---

## 4. Charging Session & Telemetry Endpoints

### 4.1 `POST /reservations/{reservationId}/start`
Consume a confirmed reservation and transition the charger into active charging mode.

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Charging session initiated successfully",
  "data": {
    "id": 5,
    "reservationId": 10,
    "userId": 1,
    "chargerId": 1,
    "stationName": "Downtown GreenCharge Hub",
    "connectorType": "CCS2",
    "maxPowerKw": 150.0,
    "pricePerKwh": 0.38,
    "startTime": "2026-10-12T10:20:00Z",
    "endTime": null,
    "status": "CHARGING",
    "energyConsumedKwh": 0.000,
    "startBatteryPct": 22.0,
    "currentBatteryPct": 22.0,
    "totalCost": 0.00,
    "elapsedSeconds": 0
  }
}
```

---

### 4.2 `GET /sessions/{sessionId}/status`
Stream/poll live simulated telemetry metrics for an ongoing charging session.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Session telemetry retrieved successfully",
  "data": {
    "sessionId": 5,
    "status": "CHARGING",
    "elapsedSeconds": 92,
    "currentBatteryPct": 29.4,
    "energyDeliveredKwh": 5.542,
    "currentCost": 2.11,
    "currentPowerKw": 142.5,
    "simulated": true
  }
}
```

---

### 4.3 `POST /sessions/{sessionId}/end`
Stop charging session, calculate final energy delivery, persist invoice amount, and release charger back to `AVAILABLE`.

---

## 5. Dashboard Endpoints

### 5.1 `GET /dashboard/metrics`
Compute aggregated metrics from persisted database records.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Dashboard metrics calculated successfully",
  "data": {
    "availableChargers": 10,
    "totalStations": 5,
    "activeReservations": 1,
    "activeChargingSessions": 1,
    "totalEnergyDeliveredKwh": 78.450,
    "userTotalCost": 29.81,
    "activeSession": null,
    "recentSessions": [],
    "recentReservations": []
  }
}
```
