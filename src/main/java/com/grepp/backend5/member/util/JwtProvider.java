package com.grepp.backend5.member.util;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.security.oauthbearer.JwtValidatorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class JwtProvider {
    @Value("${jwt.private-key}")
    private String privateKey;
    @Value("${jwt.public-key}")
    private String publicKey;

    private static final long JWT_EXPIRATION_MS = 1000*60*60;
    private static final long JWT_REFRESH_EXPIRATION_MS = 86400000L * 7;

    public String generateToken(Authentication authentication)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + JWT_EXPIRATION_MS);
//        log.info("tokenSecret {}", tokenSecret);
        return Jwts.builder().subject((String) authentication.getPrincipal())
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(loadPrivateKey(), Jwts.SIG.RS256)
//                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(tokenSecret)), Jwts.SIG.HS512)
                .compact();
    }

    public KeyPair makeRsaKey() {

        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        KeyPair pair = generator.genKeyPair();
        log.info("privateKey = {}", Base64.getEncoder()
                .encodeToString(pair.getPrivate().getEncoded()));
        log.info("publicKey = {}", Base64.getEncoder()
                .encodeToString(pair.getPublic().getEncoded()));
        return pair;
    }

    public Jws<Claims> validateToken(String token){
        try {
            return Jwts.parser().verifyWith(loadPublicKey()).build().parseSignedClaims(token);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (JwtValidatorException validatorException){
            validatorException.printStackTrace();
            throw validatorException;
        } catch (ExpiredJwtException expiredJwtException){
            expiredJwtException.printStackTrace();
            throw expiredJwtException;
        } catch (JwtException jwtException){
            jwtException.printStackTrace();
            throw jwtException;
        }
    }

    private PublicKey loadPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }
    private PrivateKey loadPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateBytes = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }
}
