package com.ll.demo03.domain.oauth.controller;

import com.ll.demo03.domain.oauth.token.TokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class OauthController {

    private final TokenProvider tokenProvider;

    @GetMapping("/login/google")
    public ResponseEntity<Map<String, String>> getLoginUrl(
            @RequestParam(required = false) String redirectUrl,
            HttpSession session
    ) {
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            session.setAttribute("OAUTH2_REDIRECT_URL", redirectUrl);
        }

        String loginUrl = "/oauth2/authorization/google";

        Map<String, String> response = new HashMap<>();
        response.put("url", loginUrl);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/process")
    public ResponseEntity<Map<String, Object>> processLogin(
            @RequestParam String redirectUrl,
            HttpServletResponse response
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Not authenticated");
            return ResponseEntity.status(401).body(errorResponse);
        }

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication, accessToken);

        String accessCookieStr = "_hoauth=" + accessToken + "; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=3600";
        String refreshCookieStr = "_hrauth=" + refreshToken + "; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=3600";

        response.addHeader("Set-Cookie", accessCookieStr);
        response.addHeader("Set-Cookie", refreshCookieStr);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("redirectUrl", redirectUrl);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/login/status")
    public ResponseEntity<Map<String, Object>> checkLoginStatus(
            @CookieValue(name = "_hoauth", required = false) String accessToken
    ) {
        Map<String, Object> response = new HashMap<>();
        boolean isLoggedIn = accessToken != null;
        response.put("isLoggedIn", isLoggedIn);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {

        Cookie accessCookie = new Cookie("_hoauth", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);

        Cookie refreshCookie = new Cookie("_hrauth", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        SecurityContextHolder.clearContext();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);

        return ResponseEntity.ok(responseData);
    }
}