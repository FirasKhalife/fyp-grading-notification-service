package com.fypgrading.notificationservice.service.mapper;

import com.fypgrading.notificationservice.service.dto.NotificationDTO;
import com.fypgrading.notificationservice.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    Notification toEntity(NotificationDTO notification);

    NotificationDTO toDTO(Notification notification);

}
