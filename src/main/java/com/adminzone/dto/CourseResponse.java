package com.adminzone.dto;

import com.adminzone.entity.Course;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {

    private Long id;
    private String denumire;
    private String profesorTitular;
    private Integer nrCredite;
    private Integer semester;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseResponse fromEntity(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .denumire(course.getDenumire())
                .profesorTitular(course.getProfesorTitular())
                .nrCredite(course.getNrCredite())
                .semester(course.getSemester())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
