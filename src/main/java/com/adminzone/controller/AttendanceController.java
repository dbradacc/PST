package com.adminzone.controller;

import com.adminzone.dto.AttendanceRequest;
import com.adminzone.dto.AttendanceResponse;
import com.adminzone.dto.AttendanceStatsResponse;
import com.adminzone.dto.PageResponse;
import com.adminzone.service.AttendanceService;
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
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Gestiune prezențe")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "Lista prezențelor cu paginare")
    @GetMapping
    public ResponseEntity<PageResponse<AttendanceResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ResponseEntity.ok(attendanceService.findAll(pageable));
    }

    @Operation(summary = "Caută prezențe după criterii")
    @GetMapping("/search")
    public ResponseEntity<List<AttendanceResponse>> search(
            @RequestParam(required = false) String student,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) Integer semester) {
        return ResponseEntity.ok(attendanceService.search(student, course, semester));
    }

    @Operation(summary = "Statistici prezențe pe semestru")
    @GetMapping("/stats")
    public ResponseEntity<List<AttendanceStatsResponse>> getStatistics() {
        return ResponseEntity.ok(attendanceService.getStatistics());
    }

    @Operation(summary = "Obține o prezență după ID")
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.findById(id));
    }

    @Operation(summary = "Creează o prezență nouă")
    @PostMapping
    public ResponseEntity<AttendanceResponse> create(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.create(request));
    }

    @Operation(summary = "Actualizează o prezență")
    @PutMapping("/{id}")
    public ResponseEntity<AttendanceResponse> update(
            @PathVariable Long id, 
            @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.update(id, request));
    }

    @Operation(summary = "Șterge o prezență")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attendanceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
