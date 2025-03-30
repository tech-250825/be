package com.ll.demo03.config.security;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.ll.demo03.domain.oauth.token.TokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication, accessToken);

        String targetUrl = defaultRedirectUrl;

        HttpSession session = request.getSession(false);

        if (session != null) {
            String redirectUrl = (String) session.getAttribute("OAUTH2_REDIRECT_URL");
            if (redirectUrl != null && isValidRedirectUrl(redirectUrl)) {
                targetUrl = redirectUrl;
                session.removeAttribute("OAUTH2_REDIRECT_URL");
            }
        }
        // AccessToken 쿠키 생성 및 응답에 추가
        ResponseCookie accessTokenCookie = ResponseCookie.from("_hoauth", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(3600)
                .sameSite("None")
                .domain(".hoit.my")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

// RefreshToken 쿠키 생성 및 응답에 추가
        ResponseCookie refreshTokenCookie = ResponseCookie.from("_hrauth", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(3600)
                .sameSite("None")
                .domain(".hoit.my")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        response.sendRedirect(targetUrl);
    }

    private boolean isValidRedirectUrl(String url) {
        return url != null && !url.isEmpty() &&
                (url.startsWith("http://") || url.startsWith("https://"));
    }
}