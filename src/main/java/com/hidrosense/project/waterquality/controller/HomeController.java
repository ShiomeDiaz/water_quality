package com.hidrosense.project.waterquality.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    public String home(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "<a href=\"/oauth2/authorization/google\">Iniciar sesión con Google</a>";
        }
        String email = principal.getAttribute("email");
        return "<h1>Bienvenido, " + email + "!</h1><br><a href=\"/logout\">Cerrar sesión</a>";
    }
}
