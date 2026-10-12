# Smart EV Charging Platform — System Architecture & Product Scope

## 1. Executive Summary & Product Scope

The **Smart EV Charging Platform** is a full-stack, enterprise-grade electric vehicle charging station reservation, telemetry monitoring, and session management platform. It addresses the real-world operational challenges of EV drivers and charging point operators (CPOs):
- Real-time station discovery with multi-faceted filtering (power rating, connector type, operational status, amenities).
- Concurrency-safe charger reservation to eliminate arrival contention and charger "range anxiety".
- End-to-end charging session management with live telemetry calculation (energy in kWh, state of charge %, elapsed duration, running cost).
- Centralized user dashboard aggregating usage analytics, real-time status indicators, and historical charging invoices.
- Strict state-machine enforcement preventing race conditions, double reservations, or invalid session transitions.

---

## 2. High-Level System Architecture Diagram

```mermaid
graph TB
    subgraph ClientLayer ["Client Layer (Presentation)"]
        Browser["Modern Web Browser / Mobile Web"]
        NextJS["Next.js 14+ App Router (TypeScript + Tailwind CSS)"]
        Browser --> NextJS
    end

    subgraph APILayer ["API & Gateway Layer"]
        ReverseProxy["Reverse Proxy / API Ingress (Port 8080)"]
        NextJS -->|REST API over HTTPS| ReverseProxy
    end

    subgraph BackendCore ["Backend Modular Monolith (Spring Boot 3.3.x / Java 21)"]
        SecurityFilter["Spring Security Filter Chain (Stateless JWT / BCrypt)"]
        ReverseProxy --> SecurityFilter

        subgraph Modules ["Business Logic Modules"]
            AuthModule["Auth & User Module<br/>(/api/v1/auth)"]
            StationModule["Station & Charger Discovery<br/>(/api/v1/stations, /chargers)"]
            ReservationModule["Reservation Engine<br/>(/api/v1/reservations)"]
            ChargingModule["Charging Session & Telemetry<br/>(/api/v1/sessions)"]
            DashboardModule["Analytics & Dashboard<br/>(/api/v1/dashboard)"]
        end

        SecurityFilter --> AuthModule
        SecurityFilter --> StationModule
        SecurityFilter --> ReservationModule
        SecurityFilter --> ChargingModule
        SecurityFilter --> DashboardModule

        subgraph CrossCutting ["Cross-Cutting Concerns"]
            GlobalExceptionHandler["@RestControllerAdvice Exception Handler"]
            OpenAPI["Springdoc OpenAPI 3.0 / Swagger UI"]
            Actuator["Spring Boot Actuator (Health, Metrics)"]
            AuditLog["Audit & Concurrency Controls"]
        end
    end

    subgraph PersistenceLayer ["Data & Storage Layer"]
        Postgres[(PostgreSQL 16 Engine)]
        Flyway["Flyway Migration Engine"]
        Redis[("Optional Redis Cache Layer<br/>(Read Cache / Hot Charger State)")]
        
        StationModule -.->|Read Cache| Redis
        StationModule --> Postgres
        ReservationModule -->|Pessimistic/Optimistic Locking| Postgres
        ChargingModule --> Postgres
        AuthModule --> Postgres
        DashboardModule --> Postgres
        Flyway --> Postgres
    end

    subgraph FutureScale ["Future Enterprise Extensions"]
        Kafka["Apache Kafka Event Bus<br/>(Charger Heartbeats & OCPP 2.0.1 Ingestion)"]
        OCPPGateway["OCPP IoT Gateway<br/>(Hardware WebSocket Server)"]
        OCPPGateway -.-> Kafka
        Kafka -.-> ChargingModule
    end

    classDef primary fill:#059669,stroke:#047857,stroke-width:2px,color:#ffffff;
    classDef secondary fill:#0284c7,stroke:#0369a1,stroke-width:2px,color:#ffffff;
    classDef storage fill:#334155,stroke:#1e293b,stroke-width:2px,color:#ffffff;
    classDef future fill:#64748b,stroke:#475569,stroke-dasharray: 5 5,color:#ffffff;

    class NextJS,AuthModule,StationModule,ReservationModule,ChargingModule,DashboardModule primary;
    class SecurityFilter,ReverseProxy secondary;
    class Postgres,Redis,Flyway storage;
    class Kafka,OCPPGateway future;
```

---

## 3. Component Architecture & Layered Responsibilities

| Layer | Technology | Responsibilities |
|---|---|---|
| **Frontend UI** | Next.js 14, React 18, Tailwind CSS, Lucide Icons | Responsive UI, state management, form validation (Zod / Hook Form), client-side telemetry animation, session timers. |
| **Security & Auth** | Spring Security 6, JJWT, BCrypt | Token issuance, request authentication, role-based authorization, resource ownership enforcement. |
| **REST Controllers** | Spring Web MVC | Input validation (`@Valid`), HTTP status mapping, standardized error envelope formatting. |
| **Service Domain** | Spring `@Service` Beans | Business invariants, transactional boundaries (`@Transactional`), deterministic charging simulation, state machines. |
| **Data Access** | Spring Data JPA, Hibernate, Flyway | Versioned database schema migrations, query optimization, composite indexes, concurrency locking (`@Version`). |
| **Primary Database** | PostgreSQL 16 (or H2 in Postgres mode for zero-dependency local runs) | ACID transactional consistency, unique constraints, foreign keys, deterministic timestamping. |

---

## 4. State Machines & Lifecycle Invariants

### 4.1 Charger State Machine
```mermaid
stateDiagram-v2
    [*] --> AVAILABLE
    AVAILABLE --> RESERVED: Valid Reservation Created
    RESERVED --> CHARGING: Session Started
    RESERVED --> AVAILABLE: Reservation Cancelled or Expired
    CHARGING --> AVAILABLE: Session Ended & Cost Persisted
    AVAILABLE --> OFFLINE: Station Maintenance
    OFFLINE --> AVAILABLE: Station Restored
    AVAILABLE --> OUT_OF_SERVICE: Hardware Fault
    OUT_OF_SERVICE --> AVAILABLE: Hardware Repaired
```

### 4.2 Reservation State Machine
```mermaid
stateDiagram-v2
    [*] --> CONFIRMED: Reservation Created (Eligible Charger)
    CONFIRMED --> CONSUMED: Session Started (Consumed exactly once)
    CONFIRMED --> CANCELLED: Explicit User Cancellation
    CONFIRMED --> EXPIRED: Expiration Time Elapsed without Session Start
    CONSUMED --> [*]
    CANCELLED --> [*]
    EXPIRED --> [*]
```

### 4.3 Charging Session State Machine
```mermaid
stateDiagram-v2
    [*] --> CHARGING: Started from CONFIRMED Reservation
    CHARGING --> COMPLETED: Explicit User Stop / 100% SoC Reached
    CHARGING --> FAILED: Telemetry / Hardware Failure
    COMPLETED --> [*]
    FAILED --> [*]
```

---

## 5. Security Architecture

1. **Authentication:** Stateless Bearer Token (JWT) issued on `POST /api/v1/auth/login` or `POST /api/v1/auth/register`.
2. **Password Security:** Salted BCrypt (work factor 12) prevents brute-force rainbow table attacks.
3. **Authorization & Ownership:** Every user-specific endpoint (`/reservations/{id}/cancel`, `/reservations/{id}/start`, `/sessions/{id}/end`) verifies that the requesting `userId` matches the record owner or possesses `ROLE_ADMIN`.
4. **Idempotency:** Unique `idempotencyKey` provided in reservation creation headers/payload prevents double billing or duplicate booking on network retries.
5. **No Credential Leakage:** Entity models are never returned directly; specialized Data Transfer Objects (DTOs) sanitize sensitive attributes.
