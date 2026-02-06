package com.pitisha.project.mybank.notificationservice.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    private static final String USERNAME = "username";
    private static final String PREFERRED_USERNAME = "preferred_username";
    private static final String INDEX = "index";
    private static final String LOGGED_OUT = "logged-out";

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/")
    public String welcomePage(final Model model, @AuthenticationPrincipal final OAuth2User user) {
        model.addAttribute(USERNAME, user.getAttribute(PREFERRED_USERNAME));
        return INDEX;
    }

    @GetMapping("/logged-out")
    public String logout() {
        return LOGGED_OUT;
    }
}
