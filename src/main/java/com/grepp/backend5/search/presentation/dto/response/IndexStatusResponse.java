package com.grepp.backend5.search.presentation.dto.response;

import java.util.Map;

// 인덱스 상태 응답 DTO: 존재 여부 + 설정 + 매핑 정보 반환
public record IndexStatusResponse(boolean exists, Map<String, Object> settings, Map<String, Object> mapping) {
}
