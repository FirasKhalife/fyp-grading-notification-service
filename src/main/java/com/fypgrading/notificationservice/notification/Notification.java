package com.fypgrading.notificationservice.notification;

import com.fypgrading.notificationservice.common.jpa.AssignedIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

/** A personal notification: one row per recipient, with its own read state (brief 4.5). */
@Entity
@Table(name = "notification")
public class Notification extends AssignedIdEntity {

    @TenantId
    @Column(name = "demo_session", nullable = false, updatable = false)
    private String demoSession;

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    /** Deduplicates redelivered events: unique per (session, event key, recipient). */
    @Column(name = "event_key", nullable = false, length = 200)
    private String eventKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Embedded
    private Link link;

    protected Notification() {
    }

    public Notification(UUID id, UUID recipientId, String eventKey, NotificationType type, String text,
            Instant createdAt, boolean read, Link link) {
        super(id);
        this.recipientId = recipientId;
        this.eventKey = eventKey;
        this.type = type;
        this.text = text;
        this.createdAt = createdAt;
        this.read = read;
        this.link = link;
    }

    public void markRead(boolean read) {
        this.read = read;
    }

    public UUID getRecipientId() {
        return recipientId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public Link getLink() {
        return link;
    }
}
