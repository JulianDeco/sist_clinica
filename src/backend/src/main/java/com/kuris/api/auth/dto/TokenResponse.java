package com.kuris.api.auth.dto;

/** Respuesta con access token (refresh token va en cookie httpOnly). */
public record TokenResponse(String accessToken) {}
