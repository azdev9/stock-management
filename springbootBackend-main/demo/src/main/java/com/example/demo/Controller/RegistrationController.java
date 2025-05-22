package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Model.User;
import com.example.demo.Model.MyAppUserRepository;

@Controller
public class RegistrationController {

    @Autowired
    private MyAppUserRepository myAppUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/req/add-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(@RequestParam String username, @RequestParam String password, Model model) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            model.addAttribute("errorMessage", "Username and password are required");
            return "add-user";
        }

        if (myAppUserRepository.findByUsername(username).isPresent()) {
            model.addAttribute("errorMessage", "Username already exists");
            return "add-user";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("WAREHOUSE_WORKER");

        try {
            myAppUserRepository.save(user);
            return "redirect:/index";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating user: " + e.getMessage());
            return "add-user";
        }
    }

    @GetMapping("/create-admin")
    public String createOrUpdateAdmin() {
        String username = "admin";
        String password = "admin123";
        String role = "ADMIN";

        User user = myAppUserRepository.findByUsername(username).orElse(new User());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        myAppUserRepository.save(user);

        return "redirect:/req/login";
    }
}