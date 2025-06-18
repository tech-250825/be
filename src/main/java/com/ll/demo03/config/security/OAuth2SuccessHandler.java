package com.ll.demo03.config.security;

import com.ll.demo03.domain.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.ll.demo03.global.util.CookieUtils;
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
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final TokenProvider tokenProvider;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Value("${app.client.url}")
    private String defaultRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            String accessToken = tokenProvider.generateAccessToken(authentication);

            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            Optional<String> redirectUriCookie = CookieUtils.getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                    .map(cookie -> cookie.getValue());

            String redirectUri;
            if (redirectUriCookie.isPresent()) {
                redirectUri = redirectUriCookie.get();
                log.info("쿠키에서 가져온 리다이렉트 URI: {}", redirectUri);
            } else {
                redirectUri = defaultRedirectUrl;
                log.info("기본 리다이렉트 URI 사용: {}", redirectUri);
            }

            ResponseCookie accessTokenCookie = ResponseCookie.from("_hoauth", accessToken)
                    .httpOnly(true)
                    .path("/")
                    // .domain(".hoit.my")
                    .maxAge(3600)
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("_hrauth", refreshToken)
                    .httpOnly(true)
                    .path("/")
                    // .domain(".hoit.my")
                    .maxAge(604800)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, redirectUri);

        } catch (Exception e) {
            log.error("OAuth2 성공 핸들러 예외 발생", e);
            throw e;
        }
    }
}