package com.example.demo.Controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class LogoutController {

    @GetMapping("/logout")
    @PostMapping("/logout")
    public RedirectView logout(HttpServletRequest request) {
        // Invalidate the session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        // Clear the security context
        SecurityContextHolder.clearContext();
        // Redirect to login page
        return new RedirectView("/req/login?logout");
    }
}