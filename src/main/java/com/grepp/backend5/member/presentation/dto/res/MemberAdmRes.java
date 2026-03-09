package com.grepp.backend5.member.presentation.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "회원 관리자 상세 응답")
public record MemberAdmRes(
        @Schema(description = "회원 ID")
        UUID id,
        @Schema(description = "이메일")
        String email,
        @Schema(description = "이름")
        String name,
        @Schema(description = "전화번호")
        String phone,
        @Schema(description = "주소")
        String address,
        @Schema(description = "회원 상태")
        String status,
        @Schema(description = "생성자 ID")
        UUID regId,
        @Schema(description = "생성 일시")
        LocalDateTime regDt,
        @Schema(description = "수정자 ID")
        UUID modifyId,
        @Schema(description = "수정 일시")
        LocalDateTime modifyDt,
        @Schema(description = "플래그")
        String flag
) {
}
