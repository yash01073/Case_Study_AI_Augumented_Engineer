# SPEC.md — Notification & Audit Service
### TaskBridge API · Multi-Tenant B2B SaaS

| Field | Value |
|---|---|
| **Version** | 1.0.0 |
| **Status** | Draft |
| **Author** | Solution Architecture |
| **Last Updated** | 2026-07-22 |
| **Service** | `taskbridge-api` |

---

## Table of Contents

1. [Purpose & Scope](#1-purpose--scope)
2. [Functional Requirements](#2-functional-requirements)
3. [Non-Functional Requirements](#3-non-functional-requirements)
4. [Data Models](#4-data-models)
5. [API Contracts](#5-api-contracts)
6. [Authorization Requirements](#6-authorization-requirements)
7. [Validation Rules](#7-validation-rules)
8. [Integration Points](#8-integration-points)
9. [Error Handling](#9-error-handling)
10. [Open Questions & Decisions](#10-open-questions--decisions)

---

## 1. Purpose & Scope

### 1.1 Purpose

The **Notification & Audit Service** is a bounded context within the TaskBridge platform responsible for two core concerns:

1. **Notifications** — generating, storing, and delivering tenant-scoped event-driven messages to users and teams in response to domain events (e.g., project status changes, assignments, deadlines).
2. **Audit Logging** — capturing an immutable, tamper-evident, chronological record of every state-changing operation performed within a tenant, for compliance, forensics, and traceability.

### 1.2 Scope

| In Scope | Out of Scope |
|---|---|
| Notification creation, persistence, delivery status | Email / SMS transport (external provider integration) |
| Audit log capture for all mutating API operations | Log storage rotation / archival policy enforcement |
| Tenant-isolated notification & audit reads | Real-time push (WebSockets / SSE) in v1 |
| Notification read/dismiss per user | Notification scheduling / delayed delivery in v1 |
| Audit record immutability enforcement | Cross-tenant audit aggregation (platform admin) |

### 1.3 Service Context Diagram

```
                        ┌────────────────────────────────┐
                        │        TaskBridge Platform       │
                        │                                  │
  ┌─────────────────┐   │  ┌──────────────────────────┐   │
  │  Project Service│──►│  │  Notification & Audit    │   │
  │  (domain events)│   │  │       Service            │   │
  └─────────────────┘   │  │                          │   │
                        │  │  ┌──────────────────┐    │   │
  ┌─────────────────┐   │  │  │ Notification DB  │    │   │
  │  Future Services│──►│  │  │  (PostgreSQL)    │    │   │
  │  (domain events)│   │  └──┼──────────────────┼────┘   │
  └─────────────────┘   │     │  Audit Log DB    │        │
                        │     │  (PostgreSQL)    │        │
  ┌─────────────────┐   │     └──────────────────┘        │
  │  API Consumers  │◄──┤                                  │
  │  (GET endpoints)│   │  JWT-authenticated, tenant-scoped│
  └─────────────────┘   └────────────────────────────────┘
```

---

## 2. Functional Requirements

### 2.1 Audit Logging

| ID | Requirement | Priority |
|---|---|---|
| AUD-01 | The system MUST create an audit record for every state-changing API operation (POST, PUT, PATCH, DELETE). | MUST |
| AUD-02 | Each audit record MUST be immutable once written; no UPDATE or DELETE operations are permitted on audit entries. | MUST |
| AUD-03 | Audit records MUST capture: `tenantId`, `actorId`, `actorEmail`, `resourceType`, `resourceId`, `action`, `oldValue` (JSON snapshot), `newValue` (JSON snapshot), `ipAddress`, `correlationId`, `occurredAt`. | MUST |
| AUD-04 | The service MUST expose a paginated, filterable read API for audit records scoped strictly to the authenticated tenant. | MUST |
| AUD-05 | Audit records MUST be queryable by `resourceType`, `resourceId`, `actorId`, and date range within a tenant. | MUST |
| AUD-06 | Audit record creation MUST NOT block the primary operation — it MUST be performed within the same transaction where possible, or asynchronously with guaranteed delivery otherwise. | MUST |
| AUD-07 | The system MUST assign a unique `correlationId` (UUID) to every inbound request and include it in the audit record and response headers (`X-Correlation-Id`). | MUST |
| AUD-08 | The system MUST record audit entries for failed operations that reach the business layer (e.g., invalid status transitions), capturing the attempted action and reason for rejection. | SHOULD |

### 2.2 Notification Generation

| ID | Requirement | Priority |
|---|---|---|
| NOT-01 | The system MUST generate a notification when a project's status changes. | MUST |
| NOT-02 | The system MUST generate a notification when a project is created or deleted. | MUST |
| NOT-03 | Notifications MUST be addressable to a specific `userId` or a `teamId` (fan-out to team members resolved at delivery time). | MUST |
| NOT-04 | Notifications MUST record: `tenantId`, `recipientUserId` or `recipientTeamId`, `type`, `title`, `body`, `resourceType`, `resourceId`, `readAt`, `createdAt`. | MUST |
| NOT-05 | Users MUST be able to mark individual notifications as read. | MUST |
| NOT-06 | Users MUST be able to list their own unread and all notifications, scoped to tenant. | MUST |
| NOT-07 | Notifications MUST NOT be visible across tenant boundaries under any circumstance. | MUST |
| NOT-08 | The system SHOULD support notification types: `PROJECT_CREATED`, `PROJECT_UPDATED`, `PROJECT_STATUS_CHANGED`, `PROJECT_DELETED`. | SHOULD |
| NOT-09 | The system SHOULD support bulk-dismiss (mark all as read) for a user within a tenant. | SHOULD |

### 2.3 Multi-Tenant Isolation

| ID | Requirement | Priority |
|---|---|---|
| MT-01 | Every database record for both `notifications` and `audit_logs` tables MUST include a `tenant_id` column. | MUST |
| MT-02 | All read queries MUST include `tenant_id` as a predicate; no unbounded cross-tenant queries are permitted. | MUST |
| MT-03 | The `tenant_id` MUST be derived exclusively from the validated JWT claim `tenant_id`; it MUST NOT be accepted from any request body or URL path parameter. | MUST |
| MT-04 | Absence of a valid `tenant_id` claim MUST result in a `401 Unauthorized` response. | MUST |
| MT-05 | Cross-tenant resource access attempts MUST return `404 Not Found` (not `403`) to prevent tenant enumeration. | MUST |

---

## 3. Non-Functional Requirements

### 3.1 Performance

| ID | Requirement | Target |
|---|---|---|
| NFR-P01 | Audit record write latency (synchronous path) | ≤ 50 ms p99 |
| NFR-P02 | Notification list API response time | ≤ 200 ms p99 |
| NFR-P03 | Audit log query (paginated, 50 records) response time | ≤ 300 ms p99 |
| NFR-P04 | Audit log writes MUST NOT degrade primary operation latency by more than 10% | — |

### 3.2 Reliability

| ID | Requirement |
|---|---|
| NFR-R01 | Audit records MUST be written durably; loss of an audit record for a committed operation is not acceptable. |
| NFR-R02 | Notification delivery failure MUST NOT cause the originating operation to fail or roll back. |
| NFR-R03 | The service MUST be stateless and horizontally scalable behind a load balancer. |

### 3.3 Security

| ID | Requirement |
|---|---|
| NFR-S01 | All endpoints require JWT authentication with a valid `tenant_id` claim. |
| NFR-S02 | Audit log data MUST NOT be mutable by any application-level operation; DB-level constraints enforce immutability (no UPDATE/DELETE privileges for the application role). |
| NFR-S03 | PII in audit `oldValue`/`newValue` snapshots MUST be masked or excluded per data classification policy. |
| NFR-S04 | All requests MUST be served over HTTPS; HTTP must be rejected or redirected. |
| NFR-S05 | `X-Correlation-Id` header MUST be returned on every response. |

### 3.4 Observability

| ID | Requirement |
|---|---|
| NFR-O01 | All log entries MUST be structured JSON, enriched with `traceId`, `tenantId`, `userId`, `correlationId`. |
| NFR-O02 | Security events (auth failures, cross-tenant attempts) MUST be logged at `WARN` level with full context. |
| NFR-O03 | The service MUST expose `/actuator/health` as a public liveness probe endpoint. |

### 3.5 Compliance

| ID | Requirement |
|---|---|
| NFR-C01 | Audit records MUST be retained for a minimum of 90 days (configurable per tenant in future). |
| NFR-C02 | The audit log MUST be exportable in JSON format for compliance reporting. |

---

## 4. Data Models

### 4.1 `audit_logs` Table

```sql
CREATE TABLE audit_logs
(
    id              UUID                        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id       UUID                        NOT NULL,
    correlation_id  UUID                        NOT NULL,
    actor_id        VARCHAR(255)                NOT NULL,
    actor_email     VARCHAR(255),
    resource_type   VARCHAR(100)                NOT NULL,
    resource_id     UUID                        NOT NULL,
    action          VARCHAR(100)                NOT NULL,
    old_value       JSONB,
    new_value       JSONB,
    outcome         VARCHAR(20)                 NOT NULL DEFAULT 'SUCCESS',
    rejection_reason TEXT,
    ip_address      VARCHAR(45),
    occurred_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT pk_audit_logs PRIMARY KEY (id),
    CONSTRAINT chk_audit_outcome CHECK (outcome IN ('SUCCESS', 'REJECTED', 'FAILED'))
);

-- All queries are tenant-scoped
CREATE INDEX idx_audit_tenant           ON audit_logs (tenant_id);
-- Lookup all events on a resource
CREATE INDEX idx_audit_tenant_resource  ON audit_logs (tenant_id, resource_type, resource_id);
-- Actor activity timeline
CREATE INDEX idx_audit_tenant_actor     ON audit_logs (tenant_id, actor_id);
-- Time-range queries
CREATE INDEX idx_audit_tenant_time      ON audit_logs (tenant_id, occurred_at DESC);
```

**Field Definitions**

| Field | Type | Nullable | Description |
|---|---|---|---|
| `id` | UUID | No | Surrogate primary key |
| `tenant_id` | UUID | No | Owning tenant — sourced from JWT |
| `correlation_id` | UUID | No | Request correlation ID from `X-Correlation-Id` header |
| `actor_id` | VARCHAR(255) | No | JWT `sub` claim — identity of the acting user |
| `actor_email` | VARCHAR(255) | Yes | Optional resolved email of actor |
| `resource_type` | VARCHAR(100) | No | e.g. `PROJECT`, `NOTIFICATION` |
| `resource_id` | UUID | No | Primary key of the affected resource |
| `action` | VARCHAR(100) | No | e.g. `PROJECT_CREATED`, `PROJECT_STATUS_CHANGED` |
| `old_value` | JSONB | Yes | Pre-change state snapshot (masked where PII) |
| `new_value` | JSONB | Yes | Post-change state snapshot |
| `outcome` | VARCHAR(20) | No | `SUCCESS`, `REJECTED`, `FAILED` |
| `rejection_reason` | TEXT | Yes | Populated when `outcome = REJECTED` |
| `ip_address` | VARCHAR(45) | Yes | Client IP (supports IPv6) |
| `occurred_at` | TIMESTAMP | No | UTC timestamp of the event |

> **Immutability contract:** The application DB role MUST have INSERT-only privileges on `audit_logs`. No UPDATE or DELETE grants are issued. This is enforced at the PostgreSQL role level, not only in application code.

---

### 4.2 `notifications` Table

```sql
CREATE TABLE notifications
(
    id                  UUID                        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id           UUID                        NOT NULL,
    recipient_user_id   VARCHAR(255),
    recipient_team_id   UUID,
    type                VARCHAR(100)                NOT NULL,
    title               VARCHAR(500)                NOT NULL,
    body                TEXT                        NOT NULL,
    resource_type       VARCHAR(100)                NOT NULL,
    resource_id         UUID                        NOT NULL,
    read_at             TIMESTAMP WITHOUT TIME ZONE,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT chk_notifications_type CHECK (
        type IN (
            'PROJECT_CREATED', 'PROJECT_UPDATED',
            'PROJECT_STATUS_CHANGED', 'PROJECT_DELETED'
        )
    ),
    CONSTRAINT chk_notifications_recipient
        CHECK (recipient_user_id IS NOT NULL OR recipient_team_id IS NOT NULL)
);

-- Inbox query — unread notifications per user per tenant
CREATE INDEX idx_notif_tenant_user_unread
    ON notifications (tenant_id, recipient_user_id, read_at)
    WHERE read_at IS NULL;

-- All notifications for a user in a tenant
CREATE INDEX idx_notif_tenant_user
    ON notifications (tenant_id, recipient_user_id);

-- Team-addressed notifications
CREATE INDEX idx_notif_tenant_team
    ON notifications (tenant_id, recipient_team_id);
```

**Field Definitions**

| Field | Type | Nullable | Description |
|---|---|---|---|
| `id` | UUID | No | Surrogate primary key |
| `tenant_id` | UUID | No | Owning tenant — sourced from JWT |
| `recipient_user_id` | VARCHAR(255) | Yes* | Target user (JWT `sub`) — mutually required with `recipient_team_id` |
| `recipient_team_id` | UUID | Yes* | Target team — fan-out to members at delivery |
| `type` | VARCHAR(100) | No | Notification category enum |
| `title` | VARCHAR(500) | No | Short human-readable title |
| `body` | TEXT | No | Full notification message |
| `resource_type` | VARCHAR(100) | No | Source resource type e.g. `PROJECT` |
| `resource_id` | UUID | No | Source resource ID |
| `read_at` | TIMESTAMP | Yes | NULL = unread; set on dismiss |
| `created_at` | TIMESTAMP | No | UTC creation timestamp |

*At least one of `recipient_user_id` or `recipient_team_id` must be non-null (enforced via check constraint).

---

### 4.3 Domain Object — `AuditLog` (Java)

```
AuditLog
├── UUID             id
├── UUID             tenantId          (immutable, from JWT)
├── UUID             correlationId
├── String           actorId           (JWT sub)
├── String           actorEmail
├── String           resourceType
├── UUID             resourceId
├── String           action
├── JsonNode         oldValue
├── JsonNode         newValue
├── AuditOutcome     outcome           (SUCCESS | REJECTED | FAILED)
├── String           rejectionReason
├── String           ipAddress
└── Instant          occurredAt        (immutable)
```

### 4.4 Domain Object — `Notification` (Java)

```
Notification
├── UUID             id
├── UUID             tenantId          (immutable, from JWT)
├── String           recipientUserId
├── UUID             recipientTeamId
├── NotificationType type
├── String           title
├── String           body
├── String           resourceType
├── UUID             resourceId
├── Instant          readAt            (null until dismissed)
└── Instant          createdAt         (immutable)
```

---

## 5. API Contracts

All endpoints require `Authorization: Bearer <jwt>` with a valid `tenant_id` claim.
All responses include `X-Correlation-Id: <uuid>` header.
Error responses conform to **RFC 7807 ProblemDetail**.

---

### 5.1 Notification Endpoints

#### `GET /api/v1/notifications`

Retrieve paginated notifications for the authenticated user within their tenant.

**Query Parameters**

| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `unreadOnly` | boolean | No | `false` | When `true`, returns only unread notifications |
| `page` | integer | No | `0` | Zero-based page index |
| `size` | integer | No | `20` | Page size (max 100) |

**Response `200 OK`**
```json
{
  "content": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "type": "PROJECT_STATUS_CHANGED",
      "title": "Project 'Alpha' is now ACTIVE",
      "body": "The project status was changed from DRAFT to ACTIVE by jane@example.com.",
      "resourceType": "PROJECT",
      "resourceId": "b1e2d3f4-...",
      "readAt": null,
      "createdAt": "2026-07-22T10:15:30Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

#### `PATCH /api/v1/notifications/{id}/read`

Mark a single notification as read.

**Path Parameters**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `id` | UUID | Yes | Notification ID |

**Response `200 OK`**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "readAt": "2026-07-22T10:20:00Z"
}
```

**Errors**

| Code | Condition |
|---|---|
| `404` | Notification not found or belongs to a different tenant |
| `409` | Notification already marked as read |

---

#### `POST /api/v1/notifications/read-all`

Mark all unread notifications as read for the authenticated user within the tenant.

**Response `204 No Content`**

---

### 5.2 Audit Log Endpoints

#### `GET /api/v1/audit-logs`

Retrieve paginated audit records for the authenticated tenant.

**Query Parameters**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `resourceType` | string | No | Filter by resource type e.g. `PROJECT` |
| `resourceId` | UUID | No | Filter by specific resource |
| `actorId` | string | No | Filter by performing user |
| `action` | string | No | Filter by action e.g. `PROJECT_CREATED` |
| `outcome` | string | No | `SUCCESS`, `REJECTED`, or `FAILED` |
| `from` | ISO-8601 datetime | No | Start of time range (inclusive) |
| `to` | ISO-8601 datetime | No | End of time range (inclusive) |
| `page` | integer | No | Zero-based page index (default `0`) |
| `size` | integer | No | Page size 1–100 (default `20`) |

**Response `200 OK`**
```json
{
  "content": [
    {
      "id": "a1b2c3d4-...",
      "correlationId": "f1e2d3c4-...",
      "actorId": "jane@example.com",
      "resourceType": "PROJECT",
      "resourceId": "b1e2d3f4-...",
      "action": "PROJECT_STATUS_CHANGED",
      "oldValue": { "status": "DRAFT" },
      "newValue": { "status": "ACTIVE" },
      "outcome": "SUCCESS",
      "occurredAt": "2026-07-22T10:15:29Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

#### `GET /api/v1/audit-logs/{id}`

Retrieve a single audit record by ID, scoped to the authenticated tenant.

**Response `200 OK`** — full `AuditLogResponse` object (same shape as above, all fields)

**Errors**

| Code | Condition |
|---|---|
| `404` | Record not found or belongs to a different tenant |

---

### 5.3 Internal / Async Endpoints (Service-to-Service)

> These endpoints are not exposed publicly. They are called internally or via an async event mechanism.

#### `POST /internal/audit-logs`

Write an audit record. Called by service interceptors or event listeners.

**Request Body**
```json
{
  "tenantId": "uuid",
  "correlationId": "uuid",
  "actorId": "string",
  "resourceType": "PROJECT",
  "resourceId": "uuid",
  "action": "PROJECT_CREATED",
  "oldValue": null,
  "newValue": { "name": "Alpha", "status": "DRAFT" },
  "outcome": "SUCCESS",
  "ipAddress": "192.168.1.1"
}
```

**Response `201 Created`** — `{ "id": "uuid" }`

---

#### `POST /internal/notifications`

Create a notification. Called by domain event listeners.

**Request Body**
```json
{
  "tenantId": "uuid",
  "recipientUserId": "jane@example.com",
  "recipientTeamId": null,
  "type": "PROJECT_STATUS_CHANGED",
  "title": "Project 'Alpha' is now ACTIVE",
  "body": "Status changed from DRAFT to ACTIVE by john@example.com.",
  "resourceType": "PROJECT",
  "resourceId": "uuid"
}
```

**Response `201 Created`** — `{ "id": "uuid" }`

---

## 6. Authorization Requirements

### 6.1 JWT Claims

Every request to protected endpoints must carry a JWT with the following claims:

| Claim | Type | Required | Description |
|---|---|---|---|
| `sub` | string | Yes | Unique user identifier (actor identity) |
| `tenant_id` | UUID string | Yes | Owning tenant — the only trusted source of tenant context |
| `iss` | string | Yes | Must equal configured `security.jwt.issuer` |
| `exp` | numeric | Yes | Must be a future Unix timestamp |

### 6.2 Endpoint Authorization Matrix

| Endpoint | Auth Required | Scope |
|---|---|---|
| `GET /api/v1/notifications` | Yes | User sees only their own notifications within their tenant |
| `PATCH /api/v1/notifications/{id}/read` | Yes | User can only dismiss their own notification |
| `POST /api/v1/notifications/read-all` | Yes | User dismisses only their own within tenant |
| `GET /api/v1/audit-logs` | Yes | Any authenticated user in the tenant may read |
| `GET /api/v1/audit-logs/{id}` | Yes | Must belong to the same tenant |
| `POST /internal/*` | Service-level (internal network) | Not exposed publicly |
| `GET /actuator/health` | No | Public liveness probe |

### 6.3 Authorization Rules

- `tenant_id` MUST be sourced exclusively from the JWT — never from request parameters, headers, or body.
- A user may only read their own notifications (`recipientUserId == JWT sub`), or notifications addressed to a team they belong to (team membership resolved at query time).
- Any resource access for a `tenant_id` that does not match the JWT claim MUST return `404` — not `403`.
- Internal endpoints (`/internal/*`) MUST be protected at the network/infrastructure layer (e.g., only reachable within the service mesh or VPC). No external JWT required.

---

## 7. Validation Rules

### 7.1 Audit Log — Write (`POST /internal/audit-logs`)

| Field | Rule |
|---|---|
| `tenantId` | Required. Valid UUID. Must match JWT `tenant_id` on authenticated paths. |
| `correlationId` | Required. Valid UUID. |
| `actorId` | Required. Non-blank. Max 255 characters. |
| `resourceType` | Required. Non-blank. Max 100 characters. Must match a known enum value. |
| `resourceId` | Required. Valid UUID. |
| `action` | Required. Non-blank. Max 100 characters. Must match a known action enum. |
| `outcome` | Required. One of: `SUCCESS`, `REJECTED`, `FAILED`. |
| `oldValue` / `newValue` | Optional. Must be valid JSON when present. Max serialized size: 64 KB each. |
| `ipAddress` | Optional. Must be valid IPv4 or IPv6 when present. |

### 7.2 Notification — Create (`POST /internal/notifications`)

| Field | Rule |
|---|---|
| `tenantId` | Required. Valid UUID. |
| `recipientUserId` | Conditionally required if `recipientTeamId` is absent. Max 255 characters. |
| `recipientTeamId` | Conditionally required if `recipientUserId` is absent. Valid UUID. |
| `type` | Required. One of the defined `NotificationType` enum values. |
| `title` | Required. Non-blank. Max 500 characters. |
| `body` | Required. Non-blank. Max 5000 characters. |
| `resourceType` | Required. Non-blank. Max 100 characters. |
| `resourceId` | Required. Valid UUID. |

### 7.3 Query Parameters — Audit Log Read

| Parameter | Rule |
|---|---|
| `page` | Integer ≥ 0. Default `0`. |
| `size` | Integer 1–100. Default `20`. Requests exceeding 100 return `400`. |
| `from` / `to` | ISO-8601 UTC datetime. `from` MUST be before `to` when both are supplied. |
| `resourceType` | Max 100 characters. Alphanumeric and underscores only. |
| `outcome` | Must be one of `SUCCESS`, `REJECTED`, `FAILED` when supplied. |

### 7.4 Notification Read

| Rule |
|---|
| `id` path parameter must be a valid UUID — return `400` for malformed UUIDs. |
| A `409 Conflict` is returned if `PATCH /{id}/read` is called on an already-read notification. |

---

## 8. Integration Points

### 8.1 Project Service → Audit & Notification (Internal)

The `ProjectService` triggers audit and notification creation on the following domain events:

| Domain Event | Audit Action | Notification Type | Recipients |
|---|---|---|---|
| Project created | `PROJECT_CREATED` | `PROJECT_CREATED` | Team members |
| Project name/desc updated | `PROJECT_UPDATED` | `PROJECT_UPDATED` | Team members |
| Project status changed | `PROJECT_STATUS_CHANGED` | `PROJECT_STATUS_CHANGED` | Team members |
| Project deleted | `PROJECT_DELETED` | `PROJECT_DELETED` | Team members |

**Integration mechanism (v1):** Synchronous in-process call via Spring `ApplicationEventPublisher` → `@TransactionalEventListener` on `AFTER_COMMIT` phase. This ensures notifications and audit logs are only written after the primary transaction commits successfully.

```
ProjectServiceImpl
    │
    ├── projectRepository.save(...)   ← primary transaction
    │
    └── applicationEventPublisher.publishEvent(ProjectCreatedEvent)
                 │
                 ▼  (AFTER_COMMIT)
        AuditEventListener.onProjectCreated(...)  → auditLogRepository.save(...)
        NotificationEventListener.onProjectCreated(...) → notificationRepository.save(...)
```

### 8.2 JWT / Identity Provider

| Concern | Detail |
|---|---|
| Token format | Signed JWT (HMAC-SHA256 or RS256) |
| Required claims | `sub`, `tenant_id`, `iss`, `exp` |
| Validation | Performed in `JwtAuthFilter` on every request |
| Secret management | Injected via `${JWT_SECRET}` environment variable; never hardcoded |

### 8.3 PostgreSQL

| Concern | Detail |
|---|---|
| Schema management | Flyway versioned migrations (`V1__`, `V2__`, ...) |
| Audit immutability | Application DB role granted INSERT-only on `audit_logs` |
| Connection pooling | HikariCP (Spring Boot default) |
| Tenant isolation | `tenant_id` column on every table; indexed composite queries |

### 8.4 Structured Logging / Observability

| Concern | Detail |
|---|---|
| Log format | JSON via Logback pattern in `application.yml` |
| Enrichment fields | `traceId`, `spanId`, `tenantId`, `userId`, `correlationId` injected via MDC in `JwtAuthFilter` |
| Log levels | `ERROR` operational failures · `WARN` security events · `INFO` business milestones · `DEBUG` diagnostics |

### 8.5 Future Integration Points (Planned, v2+)

| Integration | Description |
|---|---|
| Email / SMS delivery | Outbound notification delivery via provider (SendGrid, Twilio) triggered from `NotificationEventListener` |
| Message broker (e.g. Kafka) | Replace in-process events with durable async events for cross-service fan-out |
| Audit export API | Streaming export endpoint (`GET /api/v1/audit-logs/export`) for compliance reports |
| Tenant-configurable retention | Per-tenant `audit_retention_days` setting enforced by a scheduled archival job |

---

## 9. Error Handling

All error responses conform to **RFC 7807 ProblemDetail**:

```json
{
  "type": "https://taskbridge.io/errors/validation",
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more fields are invalid.",
  "fieldErrors": {
    "type": "must not be null"
  }
}
```

**Standard HTTP status codes used:**

| Code | When |
|---|---|
| `400 Bad Request` | Validation failures, malformed UUIDs, invalid query params |
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Valid JWT but insufficient permissions (role-level) |
| `404 Not Found` | Resource not found **or** belongs to a different tenant |
| `409 Conflict` | Invalid state transition, duplicate dismiss |
| `500 Internal Server Error` | Unexpected error — logged at `ERROR`, no detail exposed to caller |

---

## 10. Open Questions & Decisions

| # | Question | Status | Decision / Notes |
|---|---|---|---|
| OQ-01 | Should `recipient_team_id` fan-out to individual user notifications at write time, or resolve team members at read time? | Open | Read-time resolution preferred for large teams, but increases query cost. Revisit at load test. |
| OQ-02 | Should `audit_logs.old_value` / `new_value` store full entity snapshots or only changed fields (delta)? | Open | Full snapshot recommended for audit clarity; delta acceptable for storage optimisation. |
| OQ-03 | Should failed audit log writes (e.g., DB unavailable) fail the originating operation? | Decided | **No** — audit failures must not degrade primary operations. Failed writes logged at `ERROR`; alerting rule required. |
| OQ-04 | Is the 90-day retention requirement hard-delete or soft-archive? | Open | Pending legal/compliance review. Default to soft-archive (flag `archived = true`) to allow recovery. |
| OQ-05 | Should notification `body` support rich text (Markdown/HTML)? | Open | Plain text in v1. Markdown in v2 with sanitisation before storage. |
| OQ-06 | What is the SLA for notification delivery after the triggering event? | Open | Target ≤ 2 seconds end-to-end in v1 synchronous model; ≤ 30 seconds in future async model. |

# Copilot Assistance and Human Judgment

## How Copilot Helped

Copilot assisted in generating:

- Functional requirements
- Initial API contracts
- Initial domain models

## Human Judgment Applied

The initial specification did not clearly define:

- Organization-level data isolation
- Notification failure handling
- Audit record immutability enforcement
- Data retention considerations

These requirements were added manually.

## Architectural Decision

Audit records are treated as compliance data and therefore are immutable.

Notification delivery failures do not block audit persistence.