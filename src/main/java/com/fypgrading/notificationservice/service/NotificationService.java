package com.fypgrading.notificationservice.service;

import com.fypgrading.notificationservice.repository.NotificationRepository;
import com.fypgrading.notificationservice.service.dto.NotificationDTO;
import com.fypgrading.notificationservice.service.mapper.NotificationMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
}
