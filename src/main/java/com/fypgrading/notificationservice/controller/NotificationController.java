package com.fypgrading.notificationservice.controller;

import com.fypgrading.notificationservice.service.NotificationService;
import com.fypgrading.notificationservice.service.dto.NotificationDTO;
import com.fypgrading.notificationservice.service.dto.NotificationListDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RefreshScope
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final String buildVersion;

    public NotificationController(
        NotificationService notificationService,
        @Value("${build.version}") String buildVersion
    ) {
        this.notificationService = notificationService;
        this.buildVersion = buildVersion;
    }

    @GetMapping("/build-version")
    public ResponseEntity<String> getBuildVersion() {
        return ResponseEntity.ok().body(buildVersion);
    }

    @GetMapping("/")
    public ResponseEntity<NotificationListDTO> getNotifications() {
        List<NotificationDTO> notifications = notificationService.getNotifications();
        return ResponseEntity.ok().body(new NotificationListDTO(notifications));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> readNotification(@PathVariable Long id) {
        notificationService.readNotification(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/")
    public ResponseEntity<Void> readAllNotifications() {
        notificationService.readAllNotifications();
        return ResponseEntity.ok().build();
    }

}
