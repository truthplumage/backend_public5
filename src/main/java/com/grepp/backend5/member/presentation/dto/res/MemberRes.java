package com.grepp.backend5.member.presentation.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "회원 기본 응답")
public record MemberRes(
        @Schema(description = "회원 ID")
        UUID id,
        @Schema(description = "회원 이름")
        String name,
        @Schema(description = "회원 주소")
        String address
) {
}
