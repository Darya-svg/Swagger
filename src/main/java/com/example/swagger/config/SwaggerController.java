package com.example.swagger.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SwaggerController {

    @RequestMapping(path = {"/swagger-ui/", "/"})
    public String uiHome() {
        return "redirect:/swagger-ui.html";
    }
}
