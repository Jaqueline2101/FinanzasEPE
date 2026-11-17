package com.pe.finanzas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
        "/",
        "/login",
        "/admin",
        "/admin/**",
        "/cambio-sucursal"
    })
    public String forwardSpa() {
        return "forward:/index.html";
    }
}
