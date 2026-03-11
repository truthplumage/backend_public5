package com.grepp.backend5.member.application.dto;

public record TokenResponse(boolean isLogin, String token,
                            String refreshToken) {
}
