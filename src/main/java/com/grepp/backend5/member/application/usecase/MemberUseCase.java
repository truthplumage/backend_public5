package com.grepp.backend5.member.application.usecase;

import com.grepp.backend5.member.application.dto.TokenResponse;
import com.grepp.backend5.member.presentation.dto.req.Login;
import com.grepp.backend5.member.presentation.dto.req.MemberReq;
import com.grepp.backend5.member.presentation.dto.res.MemberAdmRes;
import com.grepp.backend5.member.presentation.dto.res.MemberRes;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;

public interface MemberUseCase {
    List<MemberRes> findAll();
    List<MemberAdmRes> findAdmAll();

    MemberRes save(MemberReq memberReq);

    TokenResponse login(Login login) throws NoSuchAlgorithmException, InvalidKeySpecException;
}
