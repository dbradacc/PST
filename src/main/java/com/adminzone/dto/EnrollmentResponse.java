package com.adminzone.dto;

import com.adminzone.entity.Enrollment;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {

    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private BigDecimal notaFinala;

    public static EnrollmentResponse fromEntity(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .studentId(enrollment.getStudent().getId())
                .studentName(enrollment.getStudent().getFullName())
                .courseId(enrollment.getCourse().getId())
                .courseName(enrollment.getCourse().getDenumire())
                .notaFinala(enrollment.getNotaFinala())
                .build();
    }
}
