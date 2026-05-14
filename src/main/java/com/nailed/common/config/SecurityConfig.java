package com.nailed.common.config;

import com.nailed.common.config.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//** SecurityConfig
//=> 어플리케이션 보안 설정
//=> 인증(Authentication), 인가(Authorization) 설정
//=> JWT 커스텀 필터 등록
//=> demo 프로젝트 SecurityConfig 참고

@Configuration
@EnableWebSecurity
@Log4j2
@RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //1) JWT 필터 등록
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        //2) HttpSecurity 설정 & return
        return http
                .httpBasic(httpBasic -> httpBasic.disable())    // HTTP 기본 인증 비활성화
                .formLogin(formLogin -> formLogin.disable())    // 폼 로그인 비활성화
                .logout(logout -> logout.disable())             // 기본 로그아웃 비활성화
                .csrf(csrf -> csrf.disable())                   // CSRF 비활성화 (JWT 사용)
                .cors(cors -> {})                               // CORS 활성화 (WebConfig 설정 적용)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //=> 세션 비활성화: JWT 사용이므로 서버에 세션 저장하지 않음
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        //=> CORS preflight 요청 허용

                        //=> 인증 없이 접근 가능한 경로
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/signup",
                                "/api/auth/send-verification",
                                "/api/auth/verify-code",
                                "/api/auth/signup/email-verification/request",
                                "/api/auth/signup/email-verification/confirm",
                                "/api/auth/signup/phone-verification/request",
                                "/api/auth/signup/phone-verification/confirm",
                                "/api/auth/login",
                                "/api/auth/email-login",
                                "/api/auth/email-login/request",
                                "/api/auth/email-login/confirm",
                                "/api/auth/logout",
                                "/api/auth/password/reset",
                                "/api/auth/password/find",
                                "/api/auth/refresh"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/auth/check-email",
                                "/api/auth/check-nickname",
                                "/api/products",
                                "/api/products/**",
                                "/api/users/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/products/*/view"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        //=> ADMIN Role 필요
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        //=> 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .build();
    } //filterChain

} //class
