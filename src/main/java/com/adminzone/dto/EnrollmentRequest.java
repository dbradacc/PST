package com.adminzone.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentRequest {

    @NotNull(message = "ID-ul studentului este obligatoriu")
    private Long studentId;

    @NotNull(message = "ID-ul cursului este obligatoriu")
    private Long courseId;

    @DecimalMin(value = "1.0", message = "Nota trebuie să fie între 1 și 10")
    @DecimalMax(value = "10.0", message = "Nota trebuie să fie între 1 și 10")
    private BigDecimal notaFinala;
}
