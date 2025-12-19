package com.adminzone.controller;

import com.adminzone.dto.AuditLogResponse;
import com.adminzone.dto.PageResponse;
import com.adminzone.entity.AuditLog;
import com.adminzone.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Jurnal de audit (doar ADMIN)")
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "Lista înregistrărilor din audit")
    @GetMapping
    public ResponseEntity<PageResponse<AuditLogResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, Math.min(size, 200));
        Page<AuditLog> auditPage = auditService.findAll(pageable);
        
        PageResponse<AuditLogResponse> response = PageResponse.<AuditLogResponse>builder()
                .data(auditPage.getContent().stream()
                        .map(AuditLogResponse::fromEntity)
                        .collect(Collectors.toList()))
                .page(auditPage.getNumber())
                .size(auditPage.getSize())
                .totalElements(auditPage.getTotalElements())
                .totalPages(auditPage.getTotalPages())
                .first(auditPage.isFirst())
                .last(auditPage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Filtrează audit după utilizator")
    @GetMapping("/user/{username}")
    public ResponseEntity<PageResponse<AuditLogResponse>> findByUsername(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, Math.min(size, 200));
        Page<AuditLog> auditPage = auditService.findByUsername(username, pageable);
        
        PageResponse<AuditLogResponse> response = PageResponse.<AuditLogResponse>builder()
                .data(auditPage.getContent().stream()
                        .map(AuditLogResponse::fromEntity)
                        .collect(Collectors.toList()))
                .page(auditPage.getNumber())
                .size(auditPage.getSize())
                .totalElements(auditPage.getTotalElements())
                .totalPages(auditPage.getTotalPages())
                .first(auditPage.isFirst())
                .last(auditPage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }
}
