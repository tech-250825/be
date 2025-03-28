package com.ll.demo03.config.security;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
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

        // SameSite=None 속성을 포함한 쿠키 설정 (직접 헤더 설정)
        String accessCookieStr = "_hoauth=" + accessToken + "; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=3600";
        String refreshCookieStr = "_hrauth=" + refreshToken + "; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=3600";

        response.addHeader("Set-Cookie", accessCookieStr);
        response.addHeader("Set-Cookie", refreshCookieStr);

        // localhost용 CORS 설정
        response.addHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.addHeader("Access-Control-Allow-Credentials", "true");

        // 사용자 지정 리다이렉트 URL 확인
        String targetUrl = defaultRedirectUrl;

        HttpSession session = request.getSession(false);

        if (session != null) {
            String redirectUrl = (String) session.getAttribute("OAUTH2_REDIRECT_URL");
            if (redirectUrl != null && isValidRedirectUrl(redirectUrl)) {
                targetUrl = redirectUrl;
                session.removeAttribute("OAUTH2_REDIRECT_URL");  // 사용 후 제거
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