CREATE TABLE projects
(
    id          UUID                        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id   UUID                        NOT NULL,
    team_id     UUID                        NOT NULL,
    name        VARCHAR(255)                NOT NULL,
    description TEXT,
    status      VARCHAR(50)                 NOT NULL DEFAULT 'DRAFT',
    created_by  VARCHAR(255)                NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT pk_projects PRIMARY KEY (id),
    CONSTRAINT chk_projects_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED', 'CANCELLED'))
);

-- Tenant-scoped lookups
CREATE INDEX idx_projects_tenant_id ON projects (tenant_id);

-- Get-by-team filtered to tenant (most frequent query)
CREATE INDEX idx_projects_tenant_team ON projects (tenant_id, team_id);

-- Status filtering within tenant
CREATE INDEX idx_projects_tenant_status ON projects (tenant_id, status);

