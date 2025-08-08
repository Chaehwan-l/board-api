package lch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lch.service.CustomOAuth2UserService;
import lch.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService oauth2UserService;

    // CustomOAuth2UserService 까지 함께 주입받도록 생성자 변경
    public SecurityConfig(CustomUserDetailsService uds,
                          CustomOAuth2UserService oauth2UserService) {
        this.userDetailsService = uds;
        this.oauth2UserService   = oauth2UserService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());

        return authBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .authenticationManager(authenticationManager(http))

          .authorizeHttpRequests(auth -> auth
            .requestMatchers(
              "/", "/login", "/register/**",
              "/css/**", "/js/**", "/posts/**",
              "/oauth2/**", "/login/oauth2/**"
            ).permitAll()

            .requestMatchers("/oauth2/**").permitAll()

            .anyRequest().authenticated()
          )

          .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/posts", true)
            .permitAll()
          )

          // 인증 후, 사용자 정보 가져오고 완료 폼으로 보냄
          .oauth2Login(oauth -> oauth
            .loginPage("/login")
            .userInfoEndpoint(userInfo ->
              userInfo.userService(oauth2UserService)
            )
            .defaultSuccessUrl("/register/complete", true)
          )

          .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
          );

        return http.build();
    }
}
