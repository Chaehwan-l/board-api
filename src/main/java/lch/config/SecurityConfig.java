package lch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lch.security.OAuth2SuccessHandler;
import lch.service.CustomOAuth2UserService;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;   // 인터페이스로
    private final CustomOAuth2UserService oauth2UserService;
    private final OAuth2SuccessHandler oauth2SuccessHandler;

    public SecurityConfig(UserDetailsService uds,
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

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider(userDetailsService); // 생성자 사용
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .authorizeHttpRequests(auth -> auth
            .requestMatchers(
              "/", "/login", "/register/**",
              "/css/**", "/js/**",
              "/oauth2/**", "/login/oauth2/**",
              "/images/**"
            ).permitAll()
            .requestMatchers("/posts/**").authenticated() // ← 게시판은 로그인 필요
            .anyRequest().authenticated()
          )

          .authenticationProvider(daoAuthenticationProvider())

          .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/posts", true) // 로그인 성공 후 게시판으로
            .permitAll()
          )

          .oauth2Login(oauth -> oauth
            .loginPage("/login")
            .userInfoEndpoint(u -> u.userService(oauth2UserService))
            .successHandler(oauth2SuccessHandler) // 완료된 Oauth 로그인도 게시판으로(핸들러에서 처리)
          )

          .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
          );

        return http.build();
    }

}
