package com.example.demo.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MyAppUserService implements UserDetailsService {

    @Autowired
    private MyAppUserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User addUser(User user) {
        if (repository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    public User updateUser(Long id, String username, String password, String role) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        if (!user.getUsername().equals(username) && repository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        user.setUsername(username);
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setRole(role);
        return repository.save(user);
    }

    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        repository.deleteById(id);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }
}