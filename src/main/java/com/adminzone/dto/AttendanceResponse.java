package com.adminzone.dto;

import com.adminzone.entity.Attendance;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private LocalDate data;
    private Integer semester;
    private String status;

    public static AttendanceResponse fromEntity(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getFullName())
                .courseId(attendance.getCourse().getId())
                .courseName(attendance.getCourse().getDenumire())
                .data(attendance.getData())
                .semester(attendance.getSemester())
                .status(attendance.getStatus())
                .build();
    }
}
