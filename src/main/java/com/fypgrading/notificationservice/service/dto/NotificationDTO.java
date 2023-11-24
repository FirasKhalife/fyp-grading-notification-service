package com.fypgrading.notificationservice.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fypgrading.notificationservice.service.enums.AssessmentEnum;
import com.fypgrading.notificationservice.service.event.GradeFinalizedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Integer id;

    private Integer teamId;

    private AssessmentEnum assessment;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private LocalDateTime gradeFinalizedAt;

    private boolean isRead = false;

    public NotificationDTO(GradeFinalizedEvent event) {
        this.teamId = event.getTeam().getId();
        this.assessment = event.getAssessment();
        this.gradeFinalizedAt = event.getGradeFinalizedAt();
    }

}
