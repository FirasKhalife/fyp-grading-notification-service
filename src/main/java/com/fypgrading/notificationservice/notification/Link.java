package com.fypgrading.notificationservice.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Where a notification or feed item leads. The SPA maps it to a route for the signed-in role. */
@Embeddable
public record Link(
        @Column(name = "link_kind", nullable = false, length = 20) String kind,
        @Column(name = "link_team_id") java.util.UUID teamId,
        @Column(name = "link_assessment", length = 32) String assessment) {
}
