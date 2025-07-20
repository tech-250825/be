package com.ll.demo03.oauth.token.controller;

import com.ll.demo03.oauth.token.service.TokenGenerator;
import com.ll.demo03.oauth.token.controller.request.AccessTokenRequest;
import com.ll.demo03.global.dto.GlobalResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

import static com.ll.demo03.global.util.CookieUtils.addCookie;

@RequiredArgsConstructor
@RestController
public class TokenController {

    private final TokenGenerator tokenGenerator;

    @PostMapping("/auth/token/verify")
    public ResponseEntity<GlobalResponse<String>> getToken(@RequestBody AccessTokenRequest request , HttpServletResponse response) {
        String accessToken = tokenGenerator.generateAccessTokenFromRefreshToken(request.get_hrauth());

        addCookie(response, "_hoauth", accessToken, 3600);

        return ResponseEntity.ok()
                .body(GlobalResponse.success("재인증되었습니다."));

    }
}


