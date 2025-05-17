package com.ll.demo03.config.security;


import com.ll.demo03.domain.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import lombok.RequiredArgsConstructor;
import com.ll.demo03.domain.oauth.service.CustomOAuth2UserService;
import com.ll.demo03.domain.oauth.token.TokenAuthenticationFilter;
import com.ll.demo03.domain.oauth.token.TokenExceptionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() { // security를 적용하지 않을 리소스
        return web -> web.ignoring()
                // error endpoint를 열어줘야 함, favicon.ico 추가!
                .requestMatchers("/error", "/favicon.ico",  "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // rest api 설정
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable) // 기본 인증 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // 기본 login form 비활성화
                .logout(AbstractHttpConfigurer::disable) // 기본 logout 비활성화
                .headers(c -> c.frameOptions(
                        FrameOptionsConfig::disable).disable()) // X-Frame-Options 비활성화
                .sessionManagement(c ->
                        c.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용하지 않음
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // request 인증, 인가 설정
                .authorizeHttpRequests(request ->
                                request.requestMatchers(
                                                "/",
                                                "/api/shared-images",
                                                "/api/shared-images/**",
                                                "/api/images/webhook",
                                                "/api/videos/webhook",
                                                "/api/upscale-images/webhook",
                                                "/auth/token/verify",
                                                "/api/auth/login/*",
                                                "/ws/**",
                                                "/auth/google/redirect",
                                                "/swagger-ui/**",
                                                "/swagger-resources/**",
                                                "/admin/mypage/**",
                                                "/home/**",
                                                "/actuator/health",
                                        "/actuator/prometheus"

                                ).permitAll() //
                .anyRequest().authenticated() //나머지는 인증 필요
                )

                // oauth2 설정
                .oauth2Login(oauth ->
                        oauth
                                .authorizationEndpoint(authorization ->
                                        authorization
                                                .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                                )
                                .redirectionEndpoint(endpoint ->
                                        endpoint.baseUri("/auth/google/redirect")
                                )
                                .userInfoEndpoint(c ->
                                        c.userService(oAuth2UserService)
                                )
                                .successHandler(oAuth2SuccessHandler)
                )

                // jwt 관련 설정
                .addFilterBefore(tokenAuthenticationFilter, //tokenAuthenticationFilter: JWT 인증을 처리하는 커스텀 필터, 모든 요청에서 토큰을 검사하고 인증 여부를 결정
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new TokenExceptionFilter(), tokenAuthenticationFilter.getClass()) // 토큰 예외 핸들링

                // 인증/인가 예외 핸들링
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()) // 인증 실패 시 처리
                        .accessDeniedHandler(new CustomAccessDeniedHandler())); //인가 실패 시 처리

        return http.build();
    }
}