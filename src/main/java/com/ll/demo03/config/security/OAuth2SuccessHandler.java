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
import java.util.List;

//어쩌구
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final TokenProvider tokenProvider;

    @Value("${app.client.urls}")
    private List<String> clientUrls;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication, accessToken);

        Cookie accessCookie = new Cookie("_hoauth", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(3600); // 1시간

        Cookie refreshCookie = new Cookie("_hrauth", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(3600); // 1시간

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");


        String redirectUrl = clientUrls.stream()
                .filter(url -> origin != null && origin.startsWith(url) ||
                        referer != null && referer.startsWith(url))
                .findFirst()
                .orElse(clientUrls.get(0));

        response.sendRedirect(redirectUrl);
    }
}
