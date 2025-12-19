package com.adminzone.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<Authority> authorities = new HashSet<>();

    public void addAuthority(String role) {
        Authority authority = Authority.builder()
                .user(this)
                .authority(role)
                .build();
        authorities.add(authority);
    }

    public void removeAuthority(String role) {
        authorities.removeIf(a -> a.getAuthority().equals(role));
    }
}
