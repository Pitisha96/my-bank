package com.pitisha.project.mybank.notificationservice.web.controller;

import com.pitisha.project.mybank.notificationservice.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;

import static java.util.UUID.fromString;

@Controller
@RequiredArgsConstructor
public class WsNotificationController {

    private final NotificationService notificationService;

    @MessageMapping("/notifications/{id}/read")
    public void readNotification(@DestinationVariable("id") final Long id, @AuthenticationPrincipal final OAuth2User user) {
        notificationService.read(id, fromString(user.getName()));
    }
}
