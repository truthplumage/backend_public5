package com.grepp.backend5.auth.presentation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/authorizations")
public class AuthController {
    @GetMapping("check")
    public ResponseEntity<Boolean> check(@Param("httpMethod") String httpMethod, @Param("requestPath") String requestPath){
        log.info("httpMethod = {}, requestPath = {}", httpMethod, requestPath);
        //TODO: 권한 테이블에 정보를 확인해서 토큰 유저의 권한을 체크하는 작업.
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }
}
