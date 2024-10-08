package com.fypgrading.notificationservice.service.event;

import com.fypgrading.notificationservice.service.enums.AssessmentEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeFinalizedEvent {

    private Long teamId;

    private AssessmentEnum assessment;

    private LocalDateTime gradeFinalizedAt;
}
