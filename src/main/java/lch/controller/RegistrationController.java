package lch.controller;

import java.util.Map;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import lch.service.UserAccountService;

@Controller
public class RegistrationController {

    private final UserAccountService userService;

    public RegistrationController(UserAccountService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam(name = "username") String username,
            @RequestParam(name = "password") String password,
            Model model) {

        try {
            userService.register(username, password);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }

    /** OAuth 성공 후 추가정보 입력 폼 */
    @GetMapping("/register/complete")
    public String showCompleteForm(Model model,
                                   OAuth2AuthenticationToken token,
                                   HttpServletRequest request) {
        // 1) 성공핸들러가 세션에 넣어둔 값 우선 사용
        Object sp = request.getSession().getAttribute("provider");
        Object si = request.getSession().getAttribute("providerId");

        String provider = (sp != null) ? sp.toString() : null;
        String providerId = (si != null) ? si.toString() : null;

        // 2) 세션에 없으면 토큰/attributes에서 보정 추출
        Map<String, Object> attrs = (token != null) ? token.getPrincipal().getAttributes() : null;
        if (provider == null && token != null) {
            provider = token.getAuthorizedClientRegistrationId().toUpperCase();
        }
        if (providerId == null && attrs != null) {
            // CustomOAuth2UserService 가 넣어둔 표준 키 우선
            Object pid = attrs.get("providerId");
            if (pid == null) {
                // 공급자별 원본 키 보정
                if ("GOOGLE".equals(provider)) {
                    pid = attrs.get("sub");
                } else if ("GITHUB".equals(provider)) {
                    pid = attrs.get("id");
                } else if ("NAVER".equals(provider)) {
                    Object resp = attrs.get("response");
                    if (resp instanceof Map<?,?> r) {
                        pid = r.get("id");
                    }
                }
            }
            if (pid != null) {
				providerId = pid.toString();
			}
        }

        if (provider == null || providerId == null) {
            // 식별 불가 → 로그인 오류로 돌려보냄
            return "redirect:/login?error";
        }

        // 이메일/이름은 있을 때만 표시(없어도 가입엔 영향 없음)
        String email = null;
        String name  = null;
        if (attrs != null) {
            Object e = attrs.get("email");
            email = (e != null) ? e.toString() : null;

            Object n = attrs.get("name");
            if (n != null) {
				name = n.toString();
			}
            if (name == null) { // GitHub fallback
                Object login = attrs.get("login");
                if (login != null) {
					name = login.toString();
				}
            }
        }

        model.addAttribute("email", email);
        model.addAttribute("name", name);
        model.addAttribute("provider", provider);
        model.addAttribute("providerId", providerId);

        return "register_complete";
    }

    @PostMapping("/register/complete")
    public String completeRegistration(
    		@RequestParam("username") String username,
    		@RequestParam("password") String password,
    		@RequestParam("provider") String provider,
    		@RequestParam("providerId") String providerId,
            Model model) {

        try {
            // 메서드명은 그대로 유지(내부 로직을 “완료 처리” 중심으로 보완)
            userService.registerWithProvider(username, password, provider, providerId);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            // 실패 시에도 hidden 값 유지
            model.addAttribute("provider", provider);
            model.addAttribute("providerId", providerId);
            return "register_complete";
        }
        return "redirect:/login?registered";
    }
}
