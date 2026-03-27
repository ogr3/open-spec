package com.openspec.usernameservice.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerUiController {

    @GetMapping("/swagger-ui")
    public String redirectUi() {
        return "redirect:/swagger-ui/index.html";
    }
}
