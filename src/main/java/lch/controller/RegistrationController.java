package lch.controller;

import java.util.Map;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lch.service.UserAccountService;

// 회원가입 폼을 보여주고, 폼 제출을 처리할 컨트롤러

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
        return "redirect:/login?registered";  // 회원가입 후 로그인 화면으로
    }

    /** OAuth 로그인이 성공하면 Spring이 이 토큰으로 인증해 주고, 곧바로 이 URL(/register/complete)로 보내줍니다. */
    @GetMapping("/register/complete")
    public String showCompleteForm(Model model, OAuth2AuthenticationToken token) {
        Map<String,Object> attrs = token.getPrincipal().getAttributes();
        String email      = (String) attrs.get("email");
        String name       = (String) attrs.getOrDefault("name", attrs.get("login"));
        String provider   = token.getAuthorizedClientRegistrationId().toUpperCase();
        String providerId = attrs.get("id").toString();

        model.addAttribute("email",      email);
        model.addAttribute("name",       name);
        model.addAttribute("provider",   provider);
        model.addAttribute("providerId", providerId);

        return "register_complete";
    }

    @PostMapping("/register/complete")
    public String completeRegistration(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String provider,
            @RequestParam String providerId,
            Model model) {

        try {
            userService.registerWithProvider(
               username, password,
               provider, providerId);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register_complete";
        }
        return "redirect:/login?registered";
    }

}
