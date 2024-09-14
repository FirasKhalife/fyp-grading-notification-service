package com.fypgrading.notificationservice.service;

import com.fypgrading.notificationservice.entity.Notification;
import com.fypgrading.notificationservice.repository.NotificationRepository;
import com.fypgrading.notificationservice.service.dto.NotificationDTO;
import com.fypgrading.notificationservice.service.mapper.NotificationMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationMapper notificationMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
    }

    public void saveNotification(NotificationDTO notification) {
        notificationRepository.save(notificationMapper.toEntity(notification));
    }

    public List<NotificationDTO> getNotifications() {
        return notificationRepository.findAll()
                .stream()
                .map(notificationMapper::toDTO)
                .sorted((n1, n2) ->
                                n2.getGradeFinalizedAt() == null ? 1 :
                                        n2.getGradeFinalizedAt().compareTo(n1.getGradeFinalizedAt()))
                .toList();
    }

    public void readNotification(Long id) {
        Notification notification = getNotificationById(id);
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void readAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }
}
