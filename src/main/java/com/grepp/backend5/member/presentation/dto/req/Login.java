package com.grepp.backend5.member.presentation.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청")
public record Login(
        @Schema(description = "로그인 이메일")
        String email,
        @Schema(description = "로그인 비밀번호")
        String password
) {
}
