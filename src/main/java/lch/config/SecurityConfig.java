package lch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lch.security.OAuth2SuccessHandler;
import lch.service.CustomOAuth2UserService;
import lch.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService oauth2UserService;
    private final OAuth2SuccessHandler oauth2SuccessHandler;

    public SecurityConfig(CustomUserDetailsService uds,
                          CustomOAuth2UserService oauth2UserService,
                          OAuth2SuccessHandler oauth2SuccessHandler) {
        this.userDetailsService = uds;
        this.oauth2UserService  = oauth2UserService;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 폼 로그인용 DaoAuthenticationProvider만 추가 (OAuth2 Provider는 Spring이 자동 등록)
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          // ★ 이 줄 제거: .authenticationManager(authenticationManager(http))

          .authorizeHttpRequests(auth -> auth
            .requestMatchers(
              "/", "/login", "/register/**",
              "/css/**", "/js/**", "/posts/**",
              "/oauth2/**", "/login/oauth2/**",
              "/images/**"
            ).permitAll()
            .anyRequest().authenticated()
          )

          .authenticationProvider(daoAuthenticationProvider()) // 폼 로그인 처리용 Provider 추가

          .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/posts", true)
            .permitAll()
          )

          .oauth2Login(oauth -> oauth
            .loginPage("/login")
            .userInfoEndpoint(u -> u.userService(oauth2UserService))
            .successHandler(oauth2SuccessHandler)
            // 필요 시 임시: 실패 원인 로그 확인
            // .failureHandler((req, res, ex) -> { ex.printStackTrace(); res.sendRedirect("/login?error"); })
          )

          .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
          );

        return http.build();
    }
}
