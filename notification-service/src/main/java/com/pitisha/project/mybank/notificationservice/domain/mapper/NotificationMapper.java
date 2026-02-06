package com.pitisha.project.mybank.notificationservice.domain.mapper;

import com.pitisha.project.mybank.notificationservice.api.dto.response.NotificationMessage;
import com.pitisha.project.mybank.notificationservice.domain.entity.NotificationEntity;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface NotificationMapper {

    NotificationMessage toNotificationMessage(NotificationEntity source);
}
