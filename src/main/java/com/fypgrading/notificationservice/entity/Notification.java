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

    private Long teamId;

    @Enumerated(EnumType.STRING)
    private AssessmentEnum assessment;

    private LocalDateTime gradeFinalizedAt;

    private boolean isRead = false;

}
