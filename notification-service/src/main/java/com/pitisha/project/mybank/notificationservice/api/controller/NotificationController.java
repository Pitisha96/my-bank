package com.pitisha.project.mybank.notificationservice.api.controller;

import com.pitisha.project.mybank.notificationservice.api.dto.response.NotificationMessage;
import com.pitisha.project.mybank.notificationservice.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.UUID.fromString;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationMessage>> findAll(@AuthenticationPrincipal final OAuth2User user,
                                                             @RequestParam(value = "delivered", required = false)
                                                             final Boolean delivered) {
        return ok(notificationService.findByRecipientAndStatus(fromString(user.getName()), delivered));
    }
}
