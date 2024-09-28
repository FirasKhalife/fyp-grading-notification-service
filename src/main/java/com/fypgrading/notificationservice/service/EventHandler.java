package com.fypgrading.notificationservice.service;

import com.fypgrading.notificationservice.service.event.GradeFinalizedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@RequiredArgsConstructor
@Service
public class EventHandler {

    private final Logger logger = LoggerFactory.getLogger(EventHandler.class);
    private final NotificationService notificationService;
    @Bean
    public Consumer<GradeFinalizedEvent> handleNotification() {
        return message -> {
            logger.info("Received GradeFinalizedEvent: {}", message);
            notificationService.saveGradeFinalizedNotification(message);
        };
    }
}

