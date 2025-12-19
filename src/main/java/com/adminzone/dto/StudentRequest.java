package com.adminzone.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequest {

    @NotBlank(message = "Numele este obligatoriu")
    @Size(max = 100, message = "Numele nu poate depăși 100 caractere")
    private String nume;

    @NotBlank(message = "Prenumele este obligatoriu")
    @Size(max = 100, message = "Prenumele nu poate depăși 100 caractere")
    private String prenume;

    @NotBlank(message = "Email-ul este obligatoriu")
    @Email(message = "Email-ul trebuie să fie valid")
    private String email;

    @Size(max = 20, message = "Telefonul nu poate depăși 20 caractere")
    private String telefon;

    @NotNull(message = "Anul de studiu este obligatoriu")
    @Min(value = 1, message = "Anul de studiu trebuie să fie între 1 și 6")
    @Max(value = 6, message = "Anul de studiu trebuie să fie între 1 și 6")
    private Integer anStudiu;
}
