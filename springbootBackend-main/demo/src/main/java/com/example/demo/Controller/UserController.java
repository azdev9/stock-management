package com.example.demo.Controller;

import com.example.demo.Model.MyAppUserService;
import com.example.demo.Model.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private MyAppUserService userService;

    // Display user list
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String getAllUsers(Model model, @RequestParam(value = "error", required = false) String error) {
        logger.info("Fetching all users");
        List<User> workers = userService.findAll().stream()
                .filter(user -> "WAREHOUSE_WORKER".equals(user.getRole()))
                .collect(Collectors.toList());
        model.addAttribute("workers", workers != null ? workers : Collections.emptyList());
        model.addAttribute("user", new User());
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        return "users";
    }

    // Show add user form
    @GetMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAddUserForm(Model model) {
        logger.info("Showing add user form");
        model.addAttribute("user", new User());
        return "add-user";
    }

    // Add a new user
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addUser(@Valid @ModelAttribute User user, BindingResult result, Model model) {
        logger.info("Adding new user: {}", user.getUsername());
        if (result.hasErrors()) {
            logger.warn("Validation errors while adding user: {}", result.getAllErrors());
            model.addAttribute("errorMessage", "Please correct the errors in the form");
            return "add-user";
        }
        try {
            user.setRole("WAREHOUSE_WORKER"); // Default role for new users
            userService.addUser(user);
            logger.info("User added successfully: {}", user.getUsername());
            return "redirect:/users?success=User added successfully";
        } catch (Exception e) {
            logger.error("Error adding user: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Failed to add user: " + e.getMessage());
            return "add-user";
        }
    }

    // Fetch a user for editing
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public User getUserById(@PathVariable("id") Long id) {
        logger.info("Fetching user with ID: {}", id);
        return userService.findById(id).orElse(null);
    }

    // Update a user
    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(@Valid @ModelAttribute User user, BindingResult result, Model model) {
        logger.info("Updating user with ID: {}", user.getId());
        if (result.hasErrors()) {
            logger.warn("Validation errors while updating user: {}", result.getAllErrors());
            model.addAttribute("workers", userService.findAll().stream()
                    .filter(u -> "WAREHOUSE_WORKER".equals(u.getRole()))
                    .collect(Collectors.toList()));
            model.addAttribute("errorMessage", "Please correct the errors in the form");
            return "users";
        }
        try {
            userService.updateUser(user.getId(), user.getUsername(), user.getPassword(), user.getRole());
            logger.info("User updated successfully: ID {}", user.getId());
            return "redirect:/users?success=User updated successfully";
        } catch (Exception e) {
            logger.error("Error updating user with ID {}: {}", user.getId(), e.getMessage(), e);
            String errorMessage = URLEncoder.encode("Failed to update user: " + e.getMessage(), StandardCharsets.UTF_8);
            model.addAttribute("workers", userService.findAll().stream()
                    .filter(u -> "WAREHOUSE_WORKER".equals(u.getRole()))
                    .collect(Collectors.toList()));
            model.addAttribute("errorMessage", "Failed to update user: " + e.getMessage());
            return "users";
        }
    }

    // Delete a user
    @PostMapping("/delete/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable("id") Long id) {
        logger.info("Attempting to delete user with ID: {}", id);
        try {
            userService.deleteUser(id);
            logger.info("User deleted successfully: ID {}", id);
            return "redirect:/users?success=User deleted successfully";
        } catch (Exception e) {
            logger.error("Error deleting user with ID {}: {}", id, e.getMessage(), e);
            String errorMessage = URLEncoder.encode("Failed to delete user: " + e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/users?error=" + errorMessage;
        }
    }
}