package com.grepp.backend5.search.presentation.dto.request;

// 인덱스 생성 시 샤드/레플리카 설정 요청 DTO
public record IndexConfigRequest(Integer numberOfShards, Integer numberOfReplicas) {
}
