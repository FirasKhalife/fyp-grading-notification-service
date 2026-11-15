package com.fypgrading.notificationservice.notification;

import com.fypgrading.notificationservice.activity.ActivityService;
import com.fypgrading.notificationservice.activity.ActivityService.ActivityView;
import com.fypgrading.notificationservice.common.demo.DemoSession;
import com.fypgrading.notificationservice.common.security.CurrentUser;
import com.fypgrading.notificationservice.live.LiveHub;
import com.fypgrading.notificationservice.notification.NotificationService.NotificationPage;
import com.fypgrading.notificationservice.notification.NotificationService.NotificationView;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** P10 notifications (every role, only their own) and the P5 activity feed (coordinators). */
@RestController
@Validated
public class NotificationController {

    private final NotificationService notifications;
    private final ActivityService activity;
    private final LiveHub live;

    public NotificationController(NotificationService notifications, ActivityService activity, LiveHub live) {
        this.notifications = notifications;
        this.activity = activity;
        this.live = live;
    }

    @GetMapping("/api/notifications")
    public NotificationPage list(@RequestParam(defaultValue = "false") boolean unread,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return notifications.list(CurrentUser.require().id(), unread, page, size);
    }

    @PatchMapping("/api/notifications/{id}")
    public NotificationView update(@PathVariable UUID id, @RequestBody ReadState body) {
        return notifications.markRead(CurrentUser.require().id(), id, body.read());
    }

    @PostMapping("/api/notifications/read-all")
    public ResponseEntity<Void> readAll() {
        notifications.markAllRead(CurrentUser.require().id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/api/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        CurrentUser me = CurrentUser.require();
        return live.open(DemoSession.require(), me.id(), me.isAdmin());
    }

    @GetMapping("/api/activity")
    @PreAuthorize("hasRole('ADMIN')")
    public PageView<ActivityView> activity(@RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return activity.list(page, size);
    }

    public record ReadState(@NotNull Boolean read) {
    }
}
