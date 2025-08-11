package lch.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;

        // ex) "google" | "github" | "naver"
        String provider = token.getAuthorizedClientRegistrationId().toUpperCase();

        // CustomOAuth2UserService 가 내려준 attributes
        Map<String, Object> attrs = token.getPrincipal().getAttributes();

        // 1순위: CustomOAuth2UserService 가 넣어둔 providerId 사용
        String providerId = attrs.get("providerId") != null ? attrs.get("providerId").toString() : null;

        // 2순위: (안 들어온 경우) 공급자별 원본 키에서 보정
        if (providerId == null) {
            Object raw = null;
            if ("GOOGLE".equals(provider)) {
                raw = attrs.get("sub");
            } else if ("GITHUB".equals(provider)) {
                raw = attrs.get("id");
            } else if ("NAVER".equals(provider)) {
                Object resp = attrs.get("response");
                if (resp instanceof Map<?, ?> respMap) {
                    raw = respMap.get("id");
                }
            }
            if (raw != null) {
				providerId = raw.toString();
			}
        }

        if (providerId == null) {
            response.sendRedirect("/login?error");
            return;
        }

        // ★ 메서드 "내부"에서 final 복사본을 만든 뒤 람다에 전달
        final String fProvider   = provider;
        final String fProviderId = providerId;

        UserAccount user = userRepo.findByProviderAndProviderId(fProvider, fProviderId)
                .orElseGet(() -> {
                    UserAccount u = new UserAccount();
                    u.setUsername(fProvider + "_" + fProviderId);
                    u.setPassword(""); // 비밀번호 미설정 상태
                    u.setRole("USER");
                    u.setProvider(fProvider);
                    u.setProviderId(fProviderId);
                    return userRepo.save(u);
                });

        boolean passwordSet = user.getPassword() != null && !user.getPassword().isBlank();

        // 완료 폼에서 hidden으로 쓸 값 세션에 보관
        request.getSession().setAttribute("provider", fProvider);
        request.getSession().setAttribute("providerId", fProviderId);

        if (passwordSet) {
            response.sendRedirect("/");
        } else {
            response.sendRedirect("/register/complete");
        }
    }
}
