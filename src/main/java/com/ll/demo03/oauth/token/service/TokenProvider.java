package com.ll.demo03.oauth.token.service;


import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
import com.ll.demo03.oauth.domain.PrincipalDetails;
import com.ll.demo03.global.exception.CustomException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import com.ll.demo03.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Slf4j
public class TokenProvider {

    @Value("${jwt.key}")
    private String key;
    private SecretKey secretKey;
    private static final String KEY_ROLE = "role";
    private final MemberRepository memberRepository;


    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        PrincipalDetails principalDetails = new PrincipalDetails(
                member,
                new HashMap<>(),
                "email"
        );

        return new UsernamePasswordAuthenticationToken(principalDetails, "", principalDetails.getAuthorities());
    }

    private List<SimpleGrantedAuthority> getAuthorities(Claims claims) {
        return Collections.singletonList(new SimpleGrantedAuthority(
                claims.get(KEY_ROLE).toString()));
    }


    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            boolean isValid = claims.getExpiration().after(new Date());
            return isValid;
        } catch (Exception e) {
            System.out.println("Error parsing claims: " + e.getMessage());
            return false;
        }
    }

    public Claims parseClaims(String token) {
        try {
            if (!validateToken(token)) {
                throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
            }
            return Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (MalformedJwtException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        } catch (SecurityException e) {
            throw new CustomException(ErrorCode.INVALID_JWT_SIGNATURE);
        }
    }

}
