package com.hidrosense.project.waterquality.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/api/me")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> userData = new HashMap<>();
        if (principal != null) {
            userData.put("email", principal.getAttribute("email"));
            userData.put("name", principal.getAttribute("name"));
            // Si guardas el rol en la sesión, puedes agregarlo aquí.
        }
        return userData;
    }
}
