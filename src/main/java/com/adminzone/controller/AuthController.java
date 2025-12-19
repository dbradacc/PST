package com.adminzone.controller;

import com.adminzone.dto.LoginRequest;
import com.adminzone.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Autentificare și sesiuni")
public class AuthController {

    @Operation(summary = "Informații despre utilizatorul curent")
    @GetMapping("/me")
    public ResponseEntity<LoginResponse> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(
                    LoginResponse.builder()
                            .message("Nu sunteți autentificat")
                            .build());
        }

        return ResponseEntity.ok(LoginResponse.builder()
                .username(auth.getName())
                .roles(auth.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .collect(Collectors.toList()))
                .message("Autentificat")
                .build());
    }

    @Operation(summary = "Login - Autentificare")
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        // This is handled by Spring Security's form login
        // This endpoint exists for documentation purposes
        return ResponseEntity.ok("Login handled by Spring Security");
    }

    @Operation(summary = "Logout - Deconectare")
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // This is handled by Spring Security's logout
        return ResponseEntity.ok("Logout handled by Spring Security");
    }
}
