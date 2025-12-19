package com.adminzone.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "Username-ul este obligatoriu")
    @Size(min = 3, max = 50, message = "Username-ul trebuie să aibă între 3 și 50 caractere")
    private String username;

    @Size(min = 6, message = "Parola trebuie să aibă minim 6 caractere")
    private String password;

    private Boolean enabled;

    @NotEmpty(message = "Cel puțin un rol este obligatoriu")
    private List<String> roles;
}
