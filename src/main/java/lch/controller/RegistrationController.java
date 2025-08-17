package lch.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lch.service.UserAccountService;

@Controller
public class RegistrationController {

    private final UserAccountService userService;
    private final AuthenticationManager authenticationManager; // ← 추가

    public RegistrationController(UserAccountService userService,
                                  AuthenticationManager authenticationManager) { // ← 주입
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam("passwordConfirm") String passwordConfirm,
                               Model model,
                               HttpServletRequest request) { // ← request 필요

        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "비밀번호 확인이 일치하지 않습니다.");
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        }

        try {
            userService.register(username, email, password);
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            // 1) 중복 계정인지 판별
            if (isDuplicate(e)) {
                // 2) 입력 비밀번호로 즉시 인증 시도
                try {
                    Authentication auth = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(username, password)
                    );
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(auth);
                    SecurityContextHolder.setContext(context);

                    HttpSession session = request.getSession(true);
                    session.setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context
                    );

                    return "redirect:/posts?autologin=true";
                } catch (AuthenticationException bad) {
                    // 비번 불일치 → 로그인 페이지로 유도하며 아이디 프리필
                    String u = URLEncoder.encode(username, StandardCharsets.UTF_8);
                    return "redirect:/login?exists&username=" + u;
                }
            }

            // 중복이 아니면 원래대로 에러 반환
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        }
    }

    @GetMapping("/register/complete")
    public String showCompleteForm(HttpServletRequest req, Model model) {
        String provider   = (String) req.getSession().getAttribute("provider");
        String providerId = (String) req.getSession().getAttribute("providerId");
        String email      = (String) req.getSession().getAttribute("email");
        if (provider == null || providerId == null) {
            return "redirect:/login";
        }
        model.addAttribute("provider", provider);
        model.addAttribute("providerId", providerId);
        model.addAttribute("email", email);
        return "register_complete";
    }

    @PostMapping("/register/complete")
    public String complete(@RequestParam("username") String username,
                           @RequestParam("email") String email,
                           @RequestParam("password") String password,
                           @RequestParam("passwordConfirm") String passwordConfirm,
                           @RequestParam("provider") String provider,
                           @RequestParam("providerId") String providerId,
                           Model model,
                           HttpServletRequest req) {

        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "비밀번호 확인이 일치하지 않습니다.");
            model.addAttribute("username", username);
            model.addAttribute("provider", provider);
            model.addAttribute("providerId", providerId);
            model.addAttribute("email", email);
            return "register_complete";
        }

        try {
            userService.registerWithProvider(username, email, password, provider, providerId);
            // OAuth 로그인 컨텍스트가 이미 존재하는 경우가 일반적
            return "redirect:/posts";
        } catch (RuntimeException e) {
            if (isDuplicate(e)) {
                return "redirect:/posts?autologin=true";
            }
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("provider", provider);
            model.addAttribute("providerId", providerId);
            model.addAttribute("email", email);
            return "register_complete";
        } finally {
            req.getSession().removeAttribute("provider");
            req.getSession().removeAttribute("providerId");
            req.getSession().removeAttribute("email");
        }
    }

    // 중복 계정 판별 유틸
    private boolean isDuplicate(Throwable t) {
        while (t != null) {
            String msg = (t.getMessage() == null ? "" : t.getMessage()).toLowerCase();

            if (t instanceof DataIntegrityViolationException) {
				return true;
			}

            // 영문 키워드
            if (msg.contains("duplicate") || msg.contains("constraint")
                    || msg.contains("already") || msg.contains("exists")) {
				return true;
			}

            // 한글 키워드(현재 서비스 메시지와 일치)
            if (msg.contains("이미 존재") || msg.contains("중복")
                    || msg.contains("이미 사용 중")
                    || msg.contains("가입 완료")) {
				return true;
			}

            t = t.getCause();
        }
        return false;
    }
}
