package com.hidrosense.project.waterquality.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    @GetMapping("/login")
    @ResponseBody
    public String login() {
        return "<a href=\"/oauth2/authorization/google\">Iniciar sesión con Google</a>";
    }
}
