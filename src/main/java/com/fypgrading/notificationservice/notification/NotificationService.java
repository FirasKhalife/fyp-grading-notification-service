package com.fypgrading.notificationservice.notification;

import com.fypgrading.notificationservice.common.demo.DemoSession;
import com.fypgrading.notificationservice.common.demo.SeedIds;
import com.fypgrading.notificationservice.common.web.ApiException;
import com.fypgrading.notificationservice.live.AfterCommit;
import com.fypgrading.notificationservice.live.LiveHub;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Personal notifications: stored once per recipient, pushed live, read state per person. */
@Service
public class NotificationService {

    private final NotificationRepository notifications;
    private final LiveHub live;

    public NotificationService(NotificationRepository notifications, LiveHub live) {
        this.notifications = notifications;
        this.live = live;
    }

    @Transactional(readOnly = true)
    public NotificationPage list(UUID me, boolean unreadOnly, int page, int size) {
        PageRequest request = PageRequest.of(page, size);
        Page<Notification> found = unreadOnly
                ? notifications.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(me, request)
                : notifications.findByRecipientIdOrderByCreatedAtDesc(me, request);
        PageView<NotificationView> view = PageView.of(found.map(NotificationView::of));
        return new NotificationPage(view.content(), view.page(), notifications.countByRecipientIdAndReadFalse(me));
    }

    @Transactional
    public NotificationView markRead(UUID me, UUID id, boolean read) {
        Notification n = notifications.findByIdAndRecipientId(id, me)
                .orElseThrow(() -> ApiException.notFound("The notification"));
        n.markRead(read);
        pushUnreadCount(me);
        return NotificationView.of(n);
    }

    @Transactional
    public void markAllRead(UUID me) {
        notifications.markAllRead(me);
        pushUnreadCount(me);
    }

    /**
     * Stores one row per recipient and pushes it live. Redelivered events are ignored: the event key is unique
     * per recipient.
     */
    @Transactional
    public void deliver(String eventKey, NotificationType type, String text, Instant at, Link link,
            List<UUID> recipients) {
        UUID session = DemoSession.require();
        for (UUID recipient : recipients.stream().distinct().toList()) {
            if (notifications.existsByEventKeyAndRecipientId(eventKey, recipient)) {
                continue;
            }
            Notification n = notifications.save(new Notification(
                    SeedIds.of(session, eventKey + "/" + recipient), recipient, eventKey, type, text, at, false, link));
            NotificationView view = NotificationView.of(n);
            long unread = notifications.countByRecipientIdAndReadFalse(recipient);
            AfterCommit.run(() -> {
                live.toUser(session, recipient, "notification", view.id().toString(), view);
                live.toUser(session, recipient, "unread-count", view.id() + "-count", Map.of("unreadCount", unread));
            });
        }
    }

    private void pushUnreadCount(UUID me) {
        UUID session = DemoSession.require();
        long unread = notifications.countByRecipientIdAndReadFalse(me);
        AfterCommit.run(() -> live.toUser(session, me, "unread-count", UUID.randomUUID().toString(),
                Map.of("unreadCount", unread)));
    }

    public record NotificationView(UUID id, NotificationType type, String text, Instant createdAt, boolean read,
                                   Link link) {

        static NotificationView of(Notification n) {
            return new NotificationView(n.getId(), n.getType(), n.getText(), n.getCreatedAt(), n.isRead(), n.getLink());
        }
    }

    public record NotificationPage(List<NotificationView> content, PageView.PageMeta page, long unreadCount) {
    }
}
