package com.fypgrading.notificationservice.demo;

import com.fypgrading.notificationservice.activity.ActivityEntry;
import com.fypgrading.notificationservice.activity.ActivityRepository;
import com.fypgrading.notificationservice.common.demo.DemoSession;
import com.fypgrading.notificationservice.common.demo.SeedClock;
import com.fypgrading.notificationservice.common.demo.SeedIds;
import com.fypgrading.notificationservice.live.LiveHub;
import com.fypgrading.notificationservice.notification.Link;
import com.fypgrading.notificationservice.notification.Notification;
import com.fypgrading.notificationservice.notification.NotificationRepository;
import com.fypgrading.notificationservice.notification.NotificationType;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/** Each visitor's private copy of the notifications and the activity feed. Purged at midnight. */
@Service
public class DemoCopyService {

    private static final Logger log = LoggerFactory.getLogger(DemoCopyService.class);

    private final NotificationRepository notifications;
    private final ActivityRepository activity;
    private final LiveHub live;
    private final TransactionTemplate tx;
    private final Clock clock;
    private final Seed seed;

    public DemoCopyService(
            NotificationRepository notifications,
            ActivityRepository activity,
            LiveHub live,
            TransactionTemplate tx,
            Clock clock,
            JsonMapper json)
            throws IOException {
        this.notifications = notifications;
        this.activity = activity;
        this.live = live;
        this.tx = tx;
        this.clock = clock;
        try (InputStream in = new ClassPathResource("demo/seed.json").getInputStream()) {
            this.seed = json.readValue(in, Seed.class);
        }
    }

    /** Deletes the session's copy, if any, and writes a fresh one in one transaction (review M4). */
    public void replace(UUID session, java.time.Instant createdAt) {
        SeedClock at = SeedClock.startingAt(createdAt);
        DemoSession.runAs(
                session,
                () -> tx.executeWithoutResult(s -> {
                    notifications.deleteCopy(session.toString());
                    activity.deleteCopy(session.toString());
                    for (SeedNotification n : seed.notifications()) {
                        notifications.save(new Notification(
                                SeedIds.of(session, n.key()),
                                n.recipientId(),
                                n.key(),
                                n.type(),
                                n.text(),
                                at.resolve(n.createdAt()),
                                n.read(),
                                link(session, n.link())));
                    }
                    for (SeedActivity a : seed.activity()) {
                        activity.save(new ActivityEntry(
                                SeedIds.of(session, a.key()),
                                a.kind(),
                                a.actorId(),
                                a.actorName(),
                                a.text(),
                                at.resolve(a.at()),
                                link(session, a.link())));
                    }
                }));
        // The streams reconnect and refetch, now that the new copy exists.
        live.closeSession(session);
    }

    public void delete(UUID session) {
        tx.executeWithoutResult(s -> {
            notifications.deleteCopy(session.toString());
            activity.deleteCopy(session.toString());
        });
        live.closeSession(session);
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Paris")
    public void purgeAll() {
        Integer deleted = tx.execute(s -> notifications.deleteAllCopies() + activity.deleteAllCopies());
        live.closeAll();
        log.info("Midnight purge: deleted {} notifications and feed items across all demo copies", deleted);
    }

    private static Link link(UUID session, SeedLink link) {
        if (link == null) {
            return new Link("OVERVIEW", null, null);
        }
        return new Link(
                link.kind(), link.teamKey() == null ? null : SeedIds.of(session, link.teamKey()), link.assessment());
    }

    record Seed(List<SeedNotification> notifications, List<SeedActivity> activity) {}

    record SeedNotification(
            String key,
            UUID recipientId,
            NotificationType type,
            String text,
            String createdAt,
            boolean read,
            SeedLink link) {}

    record SeedActivity(
            String key, String kind, UUID actorId, String actorName, String text, String at, SeedLink link) {}

    record SeedLink(String kind, String teamKey, String assessment) {}
}
