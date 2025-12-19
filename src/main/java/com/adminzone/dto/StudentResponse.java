package com.adminzone.dto;

import com.adminzone.entity.Student;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {

    private Long id;
    private String nume;
    private String prenume;
    private String email;
    private String telefon;
    private Integer anStudiu;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StudentResponse fromEntity(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .nume(student.getNume())
                .prenume(student.getPrenume())
                .email(student.getEmail())
                .telefon(student.getTelefon())
                .anStudiu(student.getAnStudiu())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}
