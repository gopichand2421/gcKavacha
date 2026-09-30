package com.gckavach.gckavachapp.auth.dto;

public record LoginResponse(

        String token,

        String tokenType,

        long expiresIn
) {
}