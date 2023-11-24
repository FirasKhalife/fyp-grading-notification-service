package com.fypgrading.notificationservice.controller;

import com.fypgrading.notificationservice.service.NotificationService;
import com.fypgrading.notificationservice.service.dto.NotificationDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/")
    public ResponseEntity<List<NotificationDTO>> getNotifications() {
        List<NotificationDTO> notifications = notificationService.getNotifications();
        return ResponseEntity.ok().body(notifications);
    }

}