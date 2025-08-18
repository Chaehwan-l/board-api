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
      http
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
        .authorizeHttpRequests(auth -> auth
          // 공개
          .requestMatchers("/", "/login", "/register/**",
                           "/oauth2/**", "/login/oauth2/**",
                           "/css/**", "/js/**", "/images/**").permitAll()

          // 화면: 글쓰기/수정 페이지는 로그인 필요
          .requestMatchers(HttpMethod.GET, "/posts/new", "/posts/*/edit").authenticated()

          // 화면: 목록/상세는 공개
          .requestMatchers(HttpMethod.GET, "/posts", "/posts/*").permitAll()

          // API: 새 커맨드 엔드포인트만 쓰기 허용
          .requestMatchers("/api/secure/**").authenticated()

          // 구(PostController)의 쓰기 API가 남아있다면 차단
          .requestMatchers(HttpMethod.POST,   "/api/posts/**").denyAll()
          .requestMatchers(HttpMethod.PUT,    "/api/posts/**").denyAll()
          .requestMatchers(HttpMethod.DELETE, "/api/posts/**").denyAll()

          // 화면에서의 변경 요청은 로그인 필요
          .requestMatchers(HttpMethod.POST,   "/posts/**").authenticated()
          .requestMatchers(HttpMethod.PUT,    "/posts/**").authenticated()
          .requestMatchers(HttpMethod.PATCH,  "/posts/**").authenticated()
          .requestMatchers(HttpMethod.DELETE, "/posts/**").authenticated()

          // 나머지
          .anyRequest().permitAll()
        )
        .authenticationProvider(daoAuthenticationProvider())
        .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/posts", true).failureUrl("/login?error").permitAll())
        .oauth2Login(o -> o.loginPage("/login").userInfoEndpoint(u -> u.userService(oauth2UserService))
                           .successHandler(oauth2SuccessHandler))
        .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/").invalidateHttpSession(true).deleteCookies("JSESSIONID"))
        .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> res.sendRedirect("/register?required")));
      return http.build();
    }

}
