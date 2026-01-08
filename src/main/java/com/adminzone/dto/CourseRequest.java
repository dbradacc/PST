package com.adminzone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequest {

    @NotBlank(message = "Denumirea este obligatorie")
    @Size(max = 255, message = "Denumirea nu poate depăși 255 caractere")
    private String denumire;

    @NotBlank(message = "Profesorul titular este obligatoriu")
    @Size(max = 255, message = "Numele profesorului nu poate depăși 255 caractere")
    private String profesorTitular;

    @NotNull(message = "Numărul de credite este obligatoriu")
    @Min(value = 1, message = "Numărul de credite trebuie să fie minim 1")
    private Integer nrCredite;

    @NotNull(message = "Semestrul este obligatoriu")
    @Min(1)
    @Max(2)
    @JsonProperty("semester")
    private Integer semestru;
}
