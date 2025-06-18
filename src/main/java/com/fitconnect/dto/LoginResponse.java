package com.fitconnect.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String email;
    private String role; // e.g. "CLIENT", "PROFESSIONAL"
    private Long userId;
    private String profileStatus;


    public LoginResponse(String token, Long userId, String email, String role, String profileStatus) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.profileStatus = profileStatus;
    }
}
