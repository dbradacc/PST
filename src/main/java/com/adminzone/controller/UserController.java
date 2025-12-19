package com.adminzone.controller;

import com.adminzone.dto.UserRequest;
import com.adminzone.dto.UserResponse;
import com.adminzone.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gestiune utilizatori (doar ADMIN)")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Lista tuturor utilizatorilor")
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @Operation(summary = "Obține un utilizator după username")
    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> findByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @Operation(summary = "Creează un utilizator nou")
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @Operation(summary = "Actualizează un utilizator")
    @PutMapping("/{username}")
    public ResponseEntity<UserResponse> update(
            @PathVariable String username, 
            @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.update(username, request));
    }

    @Operation(summary = "Șterge un utilizator")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> delete(@PathVariable String username) {
        userService.delete(username);
        return ResponseEntity.noContent().build();
    }
}
