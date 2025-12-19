package com.adminzone.controller;

import com.adminzone.dto.CourseRequest;
import com.adminzone.dto.CourseResponse;
import com.adminzone.dto.PageResponse;
import com.adminzone.service.CourseService;
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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Gestiune cursuri")
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "Lista cursurilor cu paginare și filtrare")
    @GetMapping
    public ResponseEntity<PageResponse<CourseResponse>> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer semester,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        
        return ResponseEntity.ok(courseService.findAll(q, semester, pageable));
    }

    @Operation(summary = "Obține un curs după ID")
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    @Operation(summary = "Creează un curs nou")
    @PostMapping
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.create(request));
    }

    @Operation(summary = "Actualizează un curs")
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> update(
            @PathVariable Long id, 
            @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.update(id, request));
    }

    @Operation(summary = "Actualizează parțial un curs")
    @PatchMapping("/{id}")
    public ResponseEntity<CourseResponse> partialUpdate(
            @PathVariable Long id, 
            @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.update(id, request));
    }

    @Operation(summary = "Șterge un curs")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
