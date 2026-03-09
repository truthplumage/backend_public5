package com.grepp.backend5.member.presentation.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 생성/수정 요청")
public record MemberReq(
        @Schema(description = "이메일")
        String email,
        @Schema(description = "이름")
        String name,
        @Schema(description = "비밀번호")
        String password,
        @Schema(description = "전화번호")
        String phone,
        @Schema(description = "주소")
        String address,
        @Schema(description = "회원 상태")
        String status
) {
}
