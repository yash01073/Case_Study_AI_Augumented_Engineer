package com.taskbridge.audit.domain;

import jakarta.persistence.Column;
import org.hibernate.annotations.Immutable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditEntryTest {

    private static final UUID ORGANIZATION_ID = UUID.randomUUID();
    private static final UUID PROJECT_ID = UUID.randomUUID();

    @Test
    void should_createImmutableAuditEntry_when_validValuesProvided() {
        AuditEntry entry = AuditEntry.create(
            ORGANIZATION_ID,
            PROJECT_ID,
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
            ORGANIZATION_ID,
            PROJECT_ID,
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
            ORGANIZATION_ID,
            PROJECT_ID,
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
            ORGANIZATION_ID,
            PROJECT_ID,
            null,
            null,
            null,
            "actor@example.com"
        )).isInstanceOf(NullPointerException.class)
          .hasMessageContaining("eventType must not be null");
    }

    @Test
    void should_beHibernateImmutableAndContainNoPublicSetters() {
        assertThat(AuditEntry.class.isAnnotationPresent(Immutable.class)).isTrue();

        Method[] methods = AuditEntry.class.getDeclaredMethods();
        assertThat(methods)
            .noneMatch(method -> method.getName().startsWith("set")
                && java.lang.reflect.Modifier.isPublic(method.getModifiers()));
    }

    @Test
    void should_markCriticalColumnsAsNonUpdatable_forImmutability() throws Exception {
        assertColumnNonUpdatable("organizationId");
        assertColumnNonUpdatable("projectId");
        assertColumnNonUpdatable("eventType");
        assertColumnNonUpdatable("previousState");
        assertColumnNonUpdatable("newState");
        assertColumnNonUpdatable("actor");
        assertColumnNonUpdatable("occurredAt");
    }

    private static void assertColumnNonUpdatable(String fieldName) throws Exception {
        Field field = AuditEntry.class.getDeclaredField(fieldName);
        Column column = field.getAnnotation(Column.class);
        assertThat(column)
            .as("Expected @Column on field %s", fieldName)
            .isNotNull();
        assertThat(column.updatable())
            .as("Expected field %s to be non-updatable", fieldName)
            .isFalse();
    }
}

