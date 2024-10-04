package com.fypgrading.notificationservice.controller;

import com.fypgrading.notificationservice.service.NotificationService;
import com.fypgrading.notificationservice.service.dto.NotificationDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/")
    public ResponseEntity<List<NotificationDTO>> getNotifications() {
        List<NotificationDTO> notifications = notificationService.getNotifications();
        return ResponseEntity.ok().body(notifications);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateNotification(@PathVariable Long id, @RequestParam boolean read) {
        notificationService.updateNotification(id, read);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/")
    public ResponseEntity<Void> readAllNotifications() {
        notificationService.readAllNotifications();
        return ResponseEntity.ok().build();
    }

}
