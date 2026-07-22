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
    version     BIGINT                      NOT NULL DEFAULT 0,

    CONSTRAINT pk_projects PRIMARY KEY (id),
    CONSTRAINT chk_projects_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED', 'CANCELLED')),
    CONSTRAINT chk_projects_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT chk_projects_created_by_not_blank CHECK (btrim(created_by) <> ''),
    CONSTRAINT chk_projects_description_length CHECK (char_length(description) <= 2000)
);

-- Tenant-scoped lookups
CREATE INDEX idx_projects_tenant_id ON projects (tenant_id);

-- Get-by-team filtered to tenant (most frequent query)
CREATE INDEX idx_projects_tenant_team ON projects (tenant_id, team_id);

-- Status filtering within tenant
CREATE INDEX idx_projects_tenant_status ON projects (tenant_id, status);

