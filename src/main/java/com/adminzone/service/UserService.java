package com.adminzone.service;

import com.adminzone.dto.UserRequest;
import com.adminzone.dto.UserResponse;
import com.adminzone.entity.Authority;
import com.adminzone.entity.User;
import com.adminzone.exception.DuplicateResourceException;
import com.adminzone.exception.ResourceNotFoundException;
import com.adminzone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAllWithAuthorities().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizator", username));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Un utilizator cu acest username există deja");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .build();

        // Add authorities
        for (String role : request.getRoles()) {
            String normalizedRole = normalizeRole(role);
            user.addAuthority(normalizedRole);
        }

        user = userRepository.save(user);
        auditService.log("CREATE", "User", null, 
                UserRequest.builder()
                        .username(request.getUsername())
                        .roles(request.getRoles())
                        .enabled(request.getEnabled())
                        .build());

        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse update(String username, UserRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizator", username));

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        // Update roles
        if (request.getRoles() != null) {
            user.getAuthorities().clear();
            userRepository.saveAndFlush(user);
            for (String role : request.getRoles()) {
                String normalizedRole = normalizeRole(role);
                user.addAuthority(normalizedRole);
            }
        }

        user = userRepository.save(user);
        auditService.log("UPDATE", "User", null,
                UserRequest.builder()
                        .username(username)
                        .roles(request.getRoles())
                        .enabled(request.getEnabled())
                        .build());

        return UserResponse.fromEntity(user);
    }

    @Transactional
    public void delete(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizator", username));

        UserResponse before = UserResponse.fromEntity(user);
        userRepository.delete(user);
        auditService.log("DELETE", "User", null, before);
    }

    private String normalizeRole(String role) {
        if (role.startsWith("ROLE_")) {
            return role;
        }
        return "ROLE_" + role.toUpperCase();
    }
}
