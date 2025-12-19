package com.adminzone.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRequest {

    @NotNull(message = "ID-ul studentului este obligatoriu")
    private Long studentId;

    @NotNull(message = "ID-ul cursului este obligatoriu")
    private Long courseId;

    @NotNull(message = "Data este obligatorie")
    private LocalDate data;

    @NotNull(message = "Semestrul este obligatoriu")
    @Min(value = 1, message = "Semestrul trebuie să fie 1 sau 2")
    @Max(value = 2, message = "Semestrul trebuie să fie 1 sau 2")
    private Integer semester;

    @NotBlank(message = "Statusul este obligatoriu")
    @Pattern(regexp = "prezent|absent|motivat", message = "Statusul trebuie să fie: prezent, absent sau motivat")
    private String status;
}
