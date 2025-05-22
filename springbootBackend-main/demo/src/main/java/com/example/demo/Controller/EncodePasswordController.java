package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EncodePasswordController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/encode")
    public String encodePassword(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }

    @GetMapping("/verify")
    public String verifyPassword(@RequestParam String rawPassword, @RequestParam String encodedPassword) {
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        return "Password '" + rawPassword + "' matches encoded password: " + matches;
    }
}