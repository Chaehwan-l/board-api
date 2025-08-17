package lch.config;

import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import lch.entity.UserAccount;
import lch.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;

@Component
@ControllerAdvice
@RequiredArgsConstructor
public class CurrentUserAdvice {

    private final UserAccountRepository userRepo;

    @ModelAttribute
    public void addAuth(Model model, Authentication auth) {
        if (auth == null) {
			return;
		}

        String username = null;
        String email    = null;
        String role     = null;

        // 1) 폼 로그인: username으로 조회
        if (auth.getPrincipal() instanceof UserDetails ud) {
            username = ud.getUsername();
            userRepo.findByUsername(username).ifPresent(u -> {
                model.addAttribute("loginName", u.getUsername());
                model.addAttribute("loginEmail", u.getEmail());
                model.addAttribute("loginRole",  u.getRole()); // String/Enum 유형에 맞게
            });
            return;
        }

        // 2) OAuth2: email 또는 providerId(sub)로 조회
        if (auth.getPrincipal() instanceof OAuth2User ou) {
            String sub   = Objects.toString(ou.getAttribute("sub"), null);
            email        = Objects.toString(ou.getAttribute("email"), null);

            UserAccount u = null;
            if (email != null) {
                u = userRepo.findByEmail(email).orElse(null);
            }
            if (u == null && sub != null) {
                // 필요 시 구현되어 있어야 함
                u = userRepo.findByProviderId(sub).orElse(null);
            }

            if (u != null) {
                model.addAttribute("loginName",  u.getUsername());
                model.addAttribute("loginEmail", u.getEmail());
                model.addAttribute("loginRole",  u.getRole());
                return;
            }
        }

        // 3) 최후 보강: ROLE_*만 필터해서 노출(스코프, OIDC_USER 제거)
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .collect(Collectors.joining(","));

        model.addAttribute("loginName",  auth.getName()); // OAuth면 sub가 나올 수 있음
        model.addAttribute("loginEmail", null);
        model.addAttribute("loginRole",  roles.isEmpty() ? null : roles);
    }
}
