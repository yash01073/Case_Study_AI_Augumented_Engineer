package com.taskbridge.notifications.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationTest {

    @Test
    void should_createUnreadNotification_when_validValuesProvided() {
        Notification notification = Notification.create(
            UUID.randomUUID(),
            " recipient@example.com ",
            UUID.randomUUID(),
            " Project status changed "
        );

        assertThat(notification.getRecipient()).isEqualTo("recipient@example.com");
        assertThat(notification.getMessage()).isEqualTo("Project status changed");
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getReadAt()).isNull();
    }

    @Test
    void should_markNotificationAsRead_when_unread() {
        Notification notification = Notification.create(
            UUID.randomUUID(),
            "recipient@example.com",
            UUID.randomUUID(),
            "Project created"
        );

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getReadAt()).isNotNull();
    }

    @Test
    void should_throwException_when_notificationAlreadyRead() {
        Notification notification = Notification.create(
            UUID.randomUUID(),
            "recipient@example.com",
            UUID.randomUUID(),
            "Project updated"
        );
        notification.markAsRead();

        assertThatThrownBy(notification::markAsRead)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already marked as read");
    }

    @Test
    void should_throwException_when_messageIsBlank() {
        assertThatThrownBy(() -> Notification.create(
            UUID.randomUUID(),
            "recipient@example.com",
            UUID.randomUUID(),
            "   "
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("message must not be blank");
    }
}

