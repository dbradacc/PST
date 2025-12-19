package com.adminzone.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String username;
    private List<String> roles;
    private String message;
}
