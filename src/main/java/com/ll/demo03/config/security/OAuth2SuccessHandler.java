package com.ll.demo03.config.security;

import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.ll.demo03.domain.oauth.token.TokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final TokenProvider tokenProvider;

    @Value("${app.client.url}")
    private String defaultRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 토큰 생성
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication, accessToken);

        // 쿠키 설정
        Cookie accessCookie = new Cookie("_hoauth", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(3600); // 1시간

        Cookie refreshCookie = new Cookie("_hrauth", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(3600); // 1시간

        response.addHeader("Set-Cookie", "myCookie=cookieValue; Path=/; Secure; HttpOnly; SameSite=None");

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        // 사용자 지정 리다이렉트 URL 확인
        String targetUrl = defaultRedirectUrl;

        // state 매개변수에서 리다이렉트 URL 추출
        String state = request.getParameter("state");
        if (state != null && !state.isEmpty()) {
            try {
                // URL 디코딩
                String customRedirectUrl = URLDecoder.decode(state, StandardCharsets.UTF_8);
                if (isValidRedirectUrl(customRedirectUrl)) {
                    targetUrl = customRedirectUrl;
                }
            } catch (Exception e) {
                // 디코딩 오류 시 기본 URL 사용
            }
        }

        response.sendRedirect(targetUrl);
    }

    /**
     * 리다이렉트 URL의 유효성 검사
     * 필요한 보안 검사를 추가할 수 있음
     */
    private boolean isValidRedirectUrl(String url) {
        // 기본적인 검증 - 필요에 따라 추가 검증 로직 구현
        return url != null && !url.isEmpty() &&
                (url.startsWith("http://") || url.startsWith("https://"));
    }
}