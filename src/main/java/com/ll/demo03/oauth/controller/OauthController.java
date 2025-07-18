package com.ll.demo03.oauth.controller;

import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OauthController {

    @PostMapping("/logout")
    public GlobalResponse<?> logout(HttpServletRequest request, HttpServletResponse response) {

        CookieUtils.deleteCookie(request, response, "_hoauth");
        CookieUtils.deleteCookie(request, response, "_hrauth");

        return GlobalResponse.success("로그아웃 되었습니다.");
    }
}