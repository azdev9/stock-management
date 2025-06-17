package com.example.demo.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(registry -> {
                    registry.requestMatchers("/req/login", "/css/**", "/js/**", "/images/**", "/sidebar.html").permitAll();
                    registry.requestMatchers("/req/signup", "/reports/batch-receipts", "/product/withdrawal", "/product/stockentries", "/product/stockwithdrawals").hasRole("ADMIN");
                    registry.requestMatchers("/index", "/product/**", "/category/**", "/supplier/**").hasAnyRole("ADMIN", "WAREHOUSE_WORKER");
                    registry.anyRequest().authenticated();
                })
                .formLogin(form -> form
                        .loginPage("/req/login")
                        .defaultSuccessUrl("/index", true)
                        .failureUrl("/req/login?error=Invalid%20username%20or%20password")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/req/login?logout=You%20have%20been%20successfully%20logged%20out")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/req/login", "/logout")
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}