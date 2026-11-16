package com.fypgrading.notificationservice.events;

import com.fypgrading.notificationservice.activity.ActivityService;
import com.fypgrading.notificationservice.common.demo.DemoSession;
import com.fypgrading.notificationservice.common.demo.SeedIds;
import com.fypgrading.notificationservice.notification.Link;
import com.fypgrading.notificationservice.notification.NotificationService;
import com.fypgrading.notificationservice.notification.NotificationType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ consumers. Both are idempotent (event ids and keys are unique per demo copy), so a redelivery after a
 * crash can't create duplicates. Failures retry, then go to the queue's own dead-letter queue.
 */
@Configuration
public class EventConsumers {

    @Bean
    Consumer<ActivityEvent> activity(ActivityService activity) {
        return e -> {
            DemoSession.runAs(
                    UUID.fromString(e.demoSession()),
                    () -> activity.record(
                            idOf(e.demoSession(), e.eventId()),
                            e.kind(),
                            e.actorId() == null ? null : UUID.fromString(e.actorId()),
                            e.actorName(),
                            e.text(),
                            e.at(),
                            link(e.link())));
        };
    }

    @Bean
    Consumer<NotificationEvent> notifications(NotificationService notifications) {
        return e -> {
            DemoSession.runAs(
                    UUID.fromString(e.demoSession()),
                    () -> notifications.deliver(
                            e.eventKey(),
                            e.type(),
                            e.text(),
                            e.at(),
                            link(e.link()),
                            e.recipients().stream().map(UUID::fromString).toList()));
        };
    }

    private static UUID idOf(String session, String eventId) {
        try {
            return UUID.fromString(eventId);
        } catch (IllegalArgumentException notAUuid) {
            return SeedIds.of(UUID.fromString(session), eventId);
        }
    }

    private static Link link(EventLink link) {
        if (link == null) {
            return new Link("OVERVIEW", null, null);
        }
        return new Link(link.kind(), link.teamId() == null ? null : UUID.fromString(link.teamId()), link.assessment());
    }

    /** Published by evaluation-service and admin-service on {@code assay.activity}. */
    public record ActivityEvent(
            String eventId,
            String demoSession,
            String kind,
            String actorId,
            String actorName,
            String text,
            Instant at,
            EventLink link) {}

    /** Published by admin-service on {@code assay.notifications}, with the recipients already resolved. */
    public record NotificationEvent(
            String eventKey,
            String demoSession,
            NotificationType type,
            String text,
            Instant at,
            EventLink link,
            List<String> recipients) {}

    public record EventLink(String kind, String teamId, String assessment) {}
}
