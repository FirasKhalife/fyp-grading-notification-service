package com.fypgrading.notificationservice.contracts;

import static org.assertj.core.api.Assertions.assertThat;

import com.fypgrading.notificationservice.events.EventConsumers;
import com.fypgrading.notificationservice.notification.NotificationType;
import org.junit.jupiter.api.Test;

/** The events notification-service reads from admin-service and evaluation-service. */
class PayloadContractsTest {

    @Test
    void readsTheActivityEvent() {
        EventConsumers.ActivityEvent e = Contracts.read("activity-event", EventConsumers.ActivityEvent.class);
        assertThat(e.eventId()).isNotBlank();
        assertThat(e.demoSession()).isNotBlank();
        assertThat(e.at()).isNotNull();
        assertThat(e.link().assessment()).isEqualTo("FINAL_REPORT");
    }

    @Test
    void readsTheNotificationEvent() {
        EventConsumers.NotificationEvent e =
                Contracts.read("notification-event", EventConsumers.NotificationEvent.class);
        assertThat(e.type()).isEqualTo(NotificationType.GRADE_FINAL);
        assertThat(e.recipients()).hasSize(2);
        assertThat(e.link().teamId()).isNotBlank();
    }
}
