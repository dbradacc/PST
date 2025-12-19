package com.adminzone.controller;

import com.adminzone.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Tag(name = "Export", description = "Export CSV")
public class ExportController {

    private final ExportService exportService;

    @Operation(summary = "Export CSV pentru tipul specificat")
    @GetMapping("/csv/{type}")
    public void exportCsv(
            @PathVariable String type,
            HttpServletResponse response) throws IOException {
        
        switch (type.toLowerCase()) {
            case "students":
                exportService.exportStudentsCsv(response);
                break;
            case "courses":
                exportService.exportCoursesCsv(response);
                break;
            case "attendance":
                exportService.exportAttendanceCsv(response);
                break;
            case "enrollments":
                exportService.exportEnrollmentsCsv(response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                        "Tip export necunoscut: " + type);
        }
    }
}
