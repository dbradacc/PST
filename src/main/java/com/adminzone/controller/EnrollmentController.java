package com.adminzone.controller;

import com.adminzone.dto.EnrollmentRequest;
import com.adminzone.dto.EnrollmentResponse;
import com.adminzone.dto.PageResponse;
import com.adminzone.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Gestiune înscrieri")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Operation(summary = "Lista înscrierilor cu paginare")
    @GetMapping
    public ResponseEntity<PageResponse<EnrollmentResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ResponseEntity.ok(enrollmentService.findAll(pageable));
    }

    @Operation(summary = "Filtrează înscrieri după student sau curs")
    @GetMapping("/filter")
    public ResponseEntity<List<EnrollmentResponse>> findByFilters(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId) {
        return ResponseEntity.ok(enrollmentService.findByFilters(studentId, courseId));
    }

    @Operation(summary = "Obține o înscriere după student și curs")
    @GetMapping("/{studentId}/{courseId}")
    public ResponseEntity<EnrollmentResponse> findById(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.findById(studentId, courseId));
    }

    @Operation(summary = "Creează o înscriere nouă")
    @PostMapping
    public ResponseEntity<EnrollmentResponse> create(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.create(request));
    }

    @Operation(summary = "Actualizează nota unei înscrieri")
    @PutMapping("/{studentId}/{courseId}")
    public ResponseEntity<EnrollmentResponse> update(
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.update(studentId, courseId, request));
    }

    @Operation(summary = "Șterge o înscriere")
    @DeleteMapping("/{studentId}/{courseId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        enrollmentService.delete(studentId, courseId);
        return ResponseEntity.noContent().build();
    }
}
