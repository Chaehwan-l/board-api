package lch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lch.security.OAuth2SuccessHandler;
import lch.service.CustomOAuth2UserService;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
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
    public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http .authorizeHttpRequests(auth -> auth
          // 공개 화면
          .requestMatchers("/", "/login", "/register/**",
                           "/oauth2/**", "/login/oauth2/**",
                           "/css/**", "/js/**", "/images/**").permitAll()

          // 화면: 글쓰기/수정 페이지는 로그인 필요
          .requestMatchers(HttpMethod.GET, "/posts/new", "/posts/*/edit").authenticated()

          // 화면: 목록/상세는 공개
          .requestMatchers(HttpMethod.GET, "/posts", "/posts/*").permitAll()

          // 화면에서의 변경 요청은 로그인 필요
          .requestMatchers(HttpMethod.POST,   "/posts/**").authenticated()
          .requestMatchers(HttpMethod.PUT,    "/posts/**").authenticated()
          .requestMatchers(HttpMethod.PATCH,  "/posts/**").authenticated()
          .requestMatchers(HttpMethod.DELETE, "/posts/**").authenticated()

          // 댓글 기능
          .requestMatchers(org.springframework.http.HttpMethod.POST,   "/posts/*/comments").authenticated()
          .requestMatchers(org.springframework.http.HttpMethod.POST,   "/comments/*").authenticated()
          .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/comments/*").authenticated()

          // 나머지
          .anyRequest().permitAll()
        )

        .authenticationProvider(daoAuthenticationProvider())

        .formLogin(f -> f.loginPage("/login")
            .defaultSuccessUrl("/", false)
            .failureUrl("/login?error")
            .permitAll())

        .oauth2Login(o -> o.loginPage("/login")
            .userInfoEndpoint(u -> u.userService(oauth2UserService))
            .successHandler(oauth2SuccessHandler))

        .logout(l -> l.logoutUrl("/logout")
            .logoutSuccessUrl("/?logout")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID"))

        .exceptionHandling(e -> e.authenticationEntryPoint(
            (req, res, ex) -> res.sendRedirect("/register?required")
        ));

      return http.build();
    }
}
