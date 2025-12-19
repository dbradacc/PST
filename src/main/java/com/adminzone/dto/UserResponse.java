package com.adminzone.dto;

import com.adminzone.entity.User;
import lombok.*;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private String username;
    private Boolean enabled;
    private List<String> roles;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .enabled(user.getEnabled())
                .roles(user.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .collect(Collectors.toList()))
                .build();
    }
}
