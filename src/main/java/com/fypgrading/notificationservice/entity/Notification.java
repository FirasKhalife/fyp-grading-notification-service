package com.fypgrading.notificationservice.entity;

import com.fypgrading.notificationservice.service.enums.AssessmentEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Notification extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long teamId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentEnum assessment;

    private LocalDateTime gradeFinalizedAt;

    private boolean isRead = false;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Notification notification)) return false;
        return id.equals(notification.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
