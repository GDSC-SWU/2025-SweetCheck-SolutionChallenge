package me.hakyuwon.sweetCheck.config;

import com.google.firebase.auth.FirebaseAuth;
import lombok.AllArgsConstructor;
import me.hakyuwon.sweetCheck.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final FirebaseAuth firebaseAuth;
    private final UserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                // 요청별 접근 허용 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/oauth2/**",              // 웹용 인증 경로
                                "/api/users/login", "/api/users/profile"  // 로그인/프로필 등록용 API는 인증 없이 허용
                        ).permitAll()
                        .anyRequest().authenticated()                 // 웹 페이지 요청도 인증 필요
                )

                // OAuth2 로그인 설정 (웹 클라이언트 전용)
                .oauth2Login(oauth -> oauth
                        .loginPage("/login") // 직접 만든 로그인 페이지로 리디렉트
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                )

                // Firebase 토큰 필터 추가
                .addFilterBefore(firebaseTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public FirebaseTokenFilter firebaseTokenFilter() {
        return new FirebaseTokenFilter(firebaseAuth, userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
