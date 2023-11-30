package com.fypgrading.notificationservice.controller;

import com.fypgrading.notificationservice.service.NotificationService;
import com.fypgrading.notificationservice.service.dto.NotificationDTO;
import com.fypgrading.notificationservice.service.dto.NotificationListDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/")
    public ResponseEntity<NotificationListDTO> getNotifications() {
        List<NotificationDTO> notifications = notificationService.getNotifications();
        return ResponseEntity.ok().body(new NotificationListDTO(notifications));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> readNotification(@PathVariable Integer id) {
        notificationService.readNotification(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/")
    public ResponseEntity<Void> readAllNotifications() {
        notificationService.readAllNotifications();
        return ResponseEntity.ok().build();
    }

}
