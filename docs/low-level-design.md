# Smart EV Charging Platform — Low-Level Design (LLD)

## 1. Domain Modeling and Class Hierarchy

The platform implements a clean Domain-Driven Modular Monolith pattern within `backend/src/main/java/com/smartev/platform`:

```
com.smartev.platform/
├── config/             # OpenAPI, WebMvc CORS, Jackson, Caching configuration
├── security/           # JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig
├── common/             # ApiResponse envelope, GlobalExceptionHandler, ErrorCode, BaseEntity
├── user/               # User entity, UserRepository, UserService, AuthController, UserDTOs
├── station/            # ChargingStation entity, Repository, Service, StationController
├── charger/            # Charger entity, ChargerStatus, ConnectorType, ChargerRepository
├── reservation/        # Reservation entity, ReservationStatus, Service, ReservationController
├── charging/           # ChargingSession entity, SessionStatus, TelemetrySimulator, SessionController
└── dashboard/          # DashboardService, DashboardMetricsDTO, DashboardController
```

---

## 2. Concurrency and Locking Strategy

### 2.1 Charger Race Condition Prevention
When multiple users attempt to reserve the same charger concurrently, data consistency is strictly preserved using **Optimistic Locking with JPA `@Version`** and **Pessimistic Write Locks (`LockModeType.PESSIMISTIC_WRITE`)** during state transitions:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Charger c WHERE c.id = :id")
Optional<Charger> findByIdForUpdate(@Param("id") Long id);
```

### 2.2 Idempotency Key Handling
1. The client supplies an optional or generated `idempotencyKey` UUID upon calling `POST /api/v1/reservations`.
2. The `Reservation` table enforces a database-level unique constraint on `(user_id, idempotency_key)`:
   ```sql
   CREATE UNIQUE INDEX idx_reservation_user_idempotency 
   ON reservations (user_id, idempotency_key) 
   WHERE idempotency_key IS NOT NULL;
   ```
3. If a duplicate request is received within the retry window, the existing reservation is returned with HTTP 200 rather than re-reserving or erroring out.

---

## 3. Telemetry Simulation Engine

In industrial EV charging networks, chargers stream telemetry over OCPP (Open Charge Point Protocol) via WebSockets. To provide a high-fidelity local experience without external hardware, the platform employs a **Deterministic Mathematical Telemetry Simulator**:

$$\Delta t = \text{currentTime} - \text{startTime}$$
$$\text{energyDelivered} = \min\left(\text{batteryCapacity} \times (1 - \text{startSoC}), \text{maxPowerKw} \times \frac{\Delta t}{3600}\right)$$
$$\text{currentBatteryPct} = \min\left(100.0, \text{startBatteryPct} + \frac{\text{energyDelivered}}{\text{batteryCapacity}} \times 100\right)$$
$$\text{totalCost} = \text{energyDelivered} \times \text{pricePerKwh}$$

- **Non-drift:** Telemetry state is calculated on-demand from deterministic timestamps and stored parameters, ensuring consistency across page refreshes and server reboots.
- **Hardware Agnostic:** A future OCPP 2.0.1 WebSocket gateway can replace the simulation engine without changing REST endpoints or frontend contracts.

---

## 4. Sequence Diagrams

### 4.1 Reservation Creation Sequence
```mermaid
sequenceDiagram
    autonumber
    actor Driver as EV Driver (Client)
    participant Ctrl as ReservationController
    participant Svc as ReservationService
    participant Repo as ChargerRepository
    participant DB as Database (Postgres)

    Driver->>Ctrl: POST /api/v1/reservations (chargerId, durationMinutes, idempotencyKey)
    Ctrl->>Svc: createReservation(userId, request)
    Svc->>DB: Check idempotencyKey exists for user
    alt Key already exists
        DB-->>Svc: Existing Reservation record
        Svc-->>Ctrl: Return existing reservation (HTTP 200)
        Ctrl-->>Driver: Reservation Details
    else New request
        Svc->>Repo: findByIdForUpdate(chargerId) [Pessimistic Lock]
        Repo->>DB: SELECT * FROM chargers WHERE id = ? FOR UPDATE
        DB-->>Repo: Charger Entity
        alt Charger NOT AVAILABLE
            Svc-->>Ctrl: throw ConflictException("Charger is not available")
            Ctrl-->>Driver: HTTP 409 Conflict
        else Charger AVAILABLE
            Svc->>Svc: Verify active reservations for charger
            Svc->>DB: INSERT INTO reservations (..., status='CONFIRMED')
            Svc->>DB: UPDATE chargers SET status='RESERVED', version=version+1
            DB-->>Svc: Success
            Svc-->>Ctrl: ReservationResponseDTO
            Ctrl-->>Driver: HTTP 201 Created
        end
    end
```

### 4.2 Charging Session Lifecycle Sequence
```mermaid
sequenceDiagram
    autonumber
    actor Driver as EV Driver (Client)
    participant Ctrl as ChargingSessionController
    participant Svc as ChargingSessionService
    participant Sim as TelemetrySimulator
    participant DB as Database (Postgres)

    Driver->>Ctrl: POST /api/v1/reservations/{id}/start
    Ctrl->>Svc: startSession(userId, reservationId)
    Svc->>DB: Fetch reservation with lock
    Svc->>Svc: Validate reservation status == CONFIRMED && owner == userId
    Svc->>DB: Update reservation status -> CONSUMED
    Svc->>DB: Update charger status -> CHARGING
    Svc->>DB: Insert charging_sessions (status='CHARGING', startBattery=20%, startTime=NOW)
    DB-->>Svc: Saved Session Entity
    Svc-->>Ctrl: SessionResponseDTO
    Ctrl-->>Driver: HTTP 201 Created (Session Active)

    loop Telemetry Poll / Status Inquiries
        Driver->>Ctrl: GET /api/v1/sessions/{id}/status
        Ctrl->>Svc: getSessionTelemetry(userId, sessionId)
        Svc->>Sim: computeCurrentTelemetry(session, charger)
        Sim-->>Svc: TelemetryData (SoC %, kWh, duration, running cost)
        Svc-->>Ctrl: TelemetryResponseDTO
        Ctrl-->>Driver: HTTP 200 OK (Live Metrics)
    end

    Driver->>Ctrl: POST /api/v1/sessions/{id}/end
    Ctrl->>Svc: endSession(userId, sessionId)
    Svc->>Sim: computeFinalMetrics(session, charger)
    Svc->>DB: Update charging_sessions (status='COMPLETED', endTime=NOW, finalCost, totalKwh)
    Svc->>DB: Update charger status -> AVAILABLE
    DB-->>Svc: Persisted
    Svc-->>Ctrl: SessionCompletedDTO
    Ctrl-->>Driver: HTTP 200 OK (Invoice & Summary)
```
