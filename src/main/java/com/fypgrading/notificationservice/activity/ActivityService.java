package com.fypgrading.notificationservice.activity;

import com.fypgrading.notificationservice.common.demo.DemoSession;
import com.fypgrading.notificationservice.live.AfterCommit;
import com.fypgrading.notificationservice.live.LiveHub;
import com.fypgrading.notificationservice.notification.Link;
import com.fypgrading.notificationservice.notification.PageView;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The coordinator's feed: everything that happened in this demo copy, newest first, pushed live. */
@Service
public class ActivityService {

    private final ActivityRepository activity;
    private final LiveHub live;

    public ActivityService(ActivityRepository activity, LiveHub live) {
        this.activity = activity;
        this.live = live;
    }

    @Transactional(readOnly = true)
    public PageView<ActivityView> list(int page, int size) {
        return PageView.of(
                activity.findAllByOrderByAtDesc(PageRequest.of(page, size)).map(ActivityView::of));
    }

    /** Idempotent on the event id, so a redelivered message is a no-op. */
    @Transactional
    public void record(UUID eventId, String kind, UUID actorId, String actorName, String text, Instant at, Link link) {
        if (activity.existsById(eventId)) {
            return;
        }
        ActivityEntry entry = activity.save(new ActivityEntry(eventId, kind, actorId, actorName, text, at, link));
        UUID session = DemoSession.require();
        ActivityView view = ActivityView.of(entry);
        AfterCommit.run(() -> live.toAdmins(session, "activity", eventId.toString(), view));
    }

    public record Actor(UUID id, String displayName) {}

    public record ActivityView(UUID id, String kind, Actor actor, String text, Instant at, Link link) {

        static ActivityView of(ActivityEntry e) {
            return new ActivityView(
                    e.getId(),
                    e.getKind(),
                    new Actor(e.getActorId(), e.getActorName()),
                    e.getText(),
                    e.getAt(),
                    e.getLink());
        }
    }
}
