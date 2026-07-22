package com.taskbridge.audit.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditEntryTest {

    @Test
    void should_createImmutableAuditEntry_when_validValuesProvided() {
        AuditEntry entry = AuditEntry.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            AuditEventType.PROJECT_CREATED,
            "{\"status\":\"DRAFT\"}",
            "{\"status\":\"ACTIVE\"}",
            " user@example.com "
        );

        assertThat(entry.getEventType()).isEqualTo(AuditEventType.PROJECT_CREATED);
        assertThat(entry.getActor()).isEqualTo("user@example.com");
        assertThat(entry.getPreviousState()).isEqualTo("{\"status\":\"DRAFT\"}");
        assertThat(entry.getNewState()).isEqualTo("{\"status\":\"ACTIVE\"}");
    }

    @Test
    void should_storeNullStates_when_optionalStatesAreBlank() {
        AuditEntry entry = AuditEntry.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            AuditEventType.PROJECT_UPDATED,
            "   ",
            null,
            "actor@example.com"
        );

        assertThat(entry.getPreviousState()).isNull();
        assertThat(entry.getNewState()).isNull();
    }

    @Test
    void should_throwException_when_actorIsBlank() {
        assertThatThrownBy(() -> AuditEntry.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            AuditEventType.PROJECT_DELETED,
            null,
            null,
            "   "
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("actor must not be blank");
    }

    @Test
    void should_throwException_when_eventTypeIsNull() {
        assertThatThrownBy(() -> AuditEntry.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            null,
            "actor@example.com"
        )).isInstanceOf(NullPointerException.class)
          .hasMessageContaining("eventType must not be null");
    }
}

