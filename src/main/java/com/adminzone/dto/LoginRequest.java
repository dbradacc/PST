package com.adminzone.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Username-ul este obligatoriu")
    private String username;

    @NotBlank(message = "Parola este obligatorie")
    private String password;
}
