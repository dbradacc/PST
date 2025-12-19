package com.adminzone.config;

import com.adminzone.entity.Authority;
import com.adminzone.entity.User;
import com.adminzone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Create default users if they don't exist
            createUserIfNotExists("admin", "password123", "ROLE_ADMIN");
            createUserIfNotExists("secretar", "password123", "ROLE_SECRETARY");
            createUserIfNotExists("profesor", "password123", "ROLE_PROFESSOR");
        };
    }

    private void createUserIfNotExists(String username, String password, String role) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .enabled(true)
                    .build();
            
            Authority authority = Authority.builder()
                    .user(user)
                    .authority(role)
                    .build();
            user.getAuthorities().add(authority);
            
            userRepository.save(user);
            log.info("Created user: {} with role: {}", username, role);
        } else {
            // Update password if user exists (for development/testing)
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                log.info("Updated password for user: {}", username);
            }
        }
    }
}
