package lch.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lch.entity.UserAccount;
import lch.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserAccountRepository userRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String provider = token.getAuthorizedClientRegistrationId(); // google, github, naver
        OAuth2User principal = token.getPrincipal();

        String providerId = valueOf(principal, "providerId");
        if (providerId == null || providerId.isBlank()) {
            providerId = token.getName(); // fallback
        }
        String email = valueOf(principal, "email");

        // 1) provider+providerId 우선, 2) 없으면 email로도 조회
        UserAccount user = userRepo.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> (email != null && !email.isBlank())
                        ? userRepo.findByEmail(email).orElse(null)
                        : null);

        boolean completed = (user != null) && Boolean.TRUE.equals(user.getSignupCompleted());
        if (completed) {
            // 기존 계정 → 게시판 진입 + 알림 플래그
            response.sendRedirect("/posts?autologin=true");
            return;
        }

        // 추가 정보 필요 → 세션에 보관 후 완료 페이지로
        request.getSession().setAttribute("provider", provider);
        request.getSession().setAttribute("providerId", providerId);
        if (email != null) {
            request.getSession().setAttribute("email", email);
        }
        response.sendRedirect("/register/complete");
    }

    private static String valueOf(OAuth2User user, String key) {
        Object v = user.getAttributes().get(key);
        return v == null ? null : String.valueOf(v);
    }
}
