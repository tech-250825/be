package com.ll.demo03.config.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.ll.demo03.domain.oauth.token.TokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
//어쩌구
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final TokenProvider tokenProvider;
    private static final String URI = "http://localhost:5173/";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication, accessToken);

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

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
        response.sendRedirect(URI);
    }
}
