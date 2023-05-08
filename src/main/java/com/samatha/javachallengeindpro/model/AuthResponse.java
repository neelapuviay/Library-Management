package com.samatha.javachallengeindpro.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private Long userId;
    private String username;
    private String role;
    private String authToken;

    public AuthResponse(Long userId, String username, String role, String authToken) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.authToken = authToken;
    }

}
