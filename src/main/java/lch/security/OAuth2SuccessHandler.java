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
        String provider   = token.getAuthorizedClientRegistrationId(); // ex) google, github, naver
        OAuth2User principal = token.getPrincipal();

        // CustomOAuth2UserService에서 심은 키 우선 사용
        String providerId = valueOf(principal, "providerId");
        if (providerId == null || providerId.isBlank()) {
            providerId = token.getName(); // fallback
        }
        String email = valueOf(principal, "email");

        // 기존 사용자 매핑 확인
        UserAccount user = userRepo.findByProviderAndProviderId(provider, providerId).orElse(null);

        // 추가 입력 필요 여부 판단
        boolean completed = (user != null) && Boolean.TRUE.equals(user.getSignupCompleted());
        if (completed) {
            response.sendRedirect("/posts");
            return;
        }

        // 완료 폼에서 사용할 값 세션에 보관
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
