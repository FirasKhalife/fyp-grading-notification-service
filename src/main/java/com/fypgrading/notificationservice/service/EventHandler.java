package com.fypgrading.notificationservice.service;

import com.fypgrading.notificationservice.service.event.GradeFinalizedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventHandler {

    private final NotificationService notificationService;

    @Bean
    public Consumer<GradeFinalizedEvent> handleNotification() {
        return message -> {
            log.info("Received GradeFinalizedEvent: {}", message);
            notificationService.saveGradeFinalizedNotification(message);
        };
    }
}

