package com.fypgrading.notificationservice.activity;

import com.fypgrading.notificationservice.common.jpa.AssignedIdEntity;
import com.fypgrading.notificationservice.notification.Link;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

/** One line of the coordinator's live feed ("Lea Mansour submitted the Final presentation for Team Epsilon"). */
@Entity
@Table(name = "activity")
public class ActivityEntry extends AssignedIdEntity {

    @TenantId
    @Column(name = "demo_session", nullable = false, updatable = false)
    private String demoSession;

    @Column(nullable = false, length = 32)
    private String kind;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "actor_name", nullable = false, length = 120)
    private String actorName;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(name = "occurred_at", nullable = false)
    private Instant at;

    @Embedded
    private Link link;

    protected ActivityEntry() {
    }

    public ActivityEntry(UUID id, String kind, UUID actorId, String actorName, String text, Instant at, Link link) {
        super(id);
        this.kind = kind;
        this.actorId = actorId;
        this.actorName = actorName;
        this.text = text;
        this.at = at;
        this.link = link;
    }

    public String getKind() {
        return kind;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public String getText() {
        return text;
    }

    public Instant getAt() {
        return at;
    }

    public Link getLink() {
        return link;
    }
}
