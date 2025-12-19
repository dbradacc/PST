package com.adminzone.controller;

import com.adminzone.dto.PageResponse;
import com.adminzone.dto.StudentRequest;
import com.adminzone.dto.StudentResponse;
import com.adminzone.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Gestiune studenți")
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "Lista studenților cu paginare și filtrare")
    @GetMapping
    public ResponseEntity<PageResponse<StudentResponse>> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer anStudiu,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        
        return ResponseEntity.ok(studentService.findAll(q, anStudiu, pageable));
    }

    @Operation(summary = "Obține un student după ID")
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.findById(id));
    }

    @Operation(summary = "Creează un student nou")
    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request));
    }

    @Operation(summary = "Actualizează un student")
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> update(
            @PathVariable Long id, 
            @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.update(id, request));
    }

    @Operation(summary = "Actualizează parțial un student")
    @PatchMapping("/{id}")
    public ResponseEntity<StudentResponse> partialUpdate(
            @PathVariable Long id, 
            @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.update(id, request));
    }

    @Operation(summary = "Șterge un student")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
