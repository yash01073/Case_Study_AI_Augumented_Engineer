CREATE TABLE audit_entries
(
    id              UUID                        NOT NULL DEFAULT gen_random_uuid(),
    organization_id UUID                        NOT NULL,
    project_id      UUID                        NOT NULL,
    event_type      VARCHAR(100)                NOT NULL,
    previous_state  TEXT,
    new_state       TEXT,
    actor           VARCHAR(255)                NOT NULL,
    occurred_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT pk_audit_entries PRIMARY KEY (id),
    CONSTRAINT chk_audit_entries_actor_not_blank CHECK (btrim(actor) <> ''),
    CONSTRAINT chk_audit_entries_previous_state_length CHECK (previous_state IS NULL OR char_length(previous_state) <= 20000),
    CONSTRAINT chk_audit_entries_new_state_length CHECK (new_state IS NULL OR char_length(new_state) <= 20000),
    CONSTRAINT chk_audit_entries_event_type CHECK (
        event_type IN (
            'PROJECT_CREATED',
            'PROJECT_UPDATED',
            'PROJECT_STATUS_CHANGED',
            'PROJECT_DELETED',
            'NOTIFICATION_CREATED',
            'NOTIFICATION_READ'
        )
    )
);

CREATE INDEX idx_audit_entries_org_time ON audit_entries (organization_id, occurred_at DESC);
CREATE INDEX idx_audit_entries_org_actor ON audit_entries (organization_id, actor);
CREATE INDEX idx_audit_entries_org_event ON audit_entries (organization_id, event_type);
CREATE INDEX idx_audit_entries_org_project_time ON audit_entries (organization_id, project_id, occurred_at DESC);

CREATE TABLE notifications
(
    id              UUID                        NOT NULL DEFAULT gen_random_uuid(),
    organization_id UUID                        NOT NULL,
    recipient       VARCHAR(255)                NOT NULL,
    project_id      UUID                        NOT NULL,
    message         VARCHAR(2000)               NOT NULL,
    read_at         TIMESTAMP WITHOUT TIME ZONE,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT chk_notifications_recipient_not_blank CHECK (btrim(recipient) <> ''),
    CONSTRAINT chk_notifications_message_not_blank CHECK (btrim(message) <> ''),
    CONSTRAINT chk_notifications_message_length CHECK (char_length(message) <= 2000)
);

CREATE INDEX idx_notifications_org_recipient_read ON notifications (organization_id, recipient, read_at);
CREATE INDEX idx_notifications_org_project_time ON notifications (organization_id, project_id, created_at DESC);
CREATE INDEX idx_notifications_org_time ON notifications (organization_id, created_at DESC);

