package lch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lch.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    public SecurityConfig(CustomUserDetailsService uds) {
        this.userDetailsService = uds;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     AuthenticationManager를 수동으로 구성합니다.
     UserDetailsService + PasswordEncoder 바인딩
     DaoAuthenticationProvider#setUserDetailsService 대신 사용
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    /*
     SecurityFilterChain 정의
     /login, /register, 정적 리소스, /posts/**는 인증 없이 접근 허용, 그 외는 모두 인증 필요
     formLogin, logout 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          // 우리가 만든 AuthenticationManager를 사용하도록 연결
          .authenticationManager(authenticationManager(http))

          .authorizeHttpRequests(auth -> auth
            .requestMatchers(
              "/login",
              "/",
              "/register",
              "/css/**",
              "/js/**",
              "/posts/**"
            ).permitAll()
            .anyRequest().authenticated()
          )

          .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/posts", true)
            .permitAll()
          )

          .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
          );

        return http.build();
    }
}
