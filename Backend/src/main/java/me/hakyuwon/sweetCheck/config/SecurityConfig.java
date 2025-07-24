package me.hakyuwon.sweetCheck.config;

import com.google.firebase.auth.FirebaseAuth;
import lombok.AllArgsConstructor;
import me.hakyuwon.sweetCheck.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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
                // CSRF 비활성화 (API 전용 서버)
                .csrf(csrf -> csrf.disable())

                // 세션 사용 안 함 (토큰 기반 인증)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인증 없이 허용할 엔드포인트
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/login",
                                "/api/users/signup",
                                "/api/users/profile"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // 기본 폼 로그인, HTTP Basic 인증 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // Firebase 토큰 필터 등록
                .addFilterBefore(
                        new FirebaseTokenFilter(firebaseAuth, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

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