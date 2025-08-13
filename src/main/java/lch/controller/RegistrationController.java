package lch.controller;

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
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam("passwordConfirm") String passwordConfirm,
                               Model model) {
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "비밀번호 확인이 일치하지 않습니다.");
            return "register";
        }
        try {
            userService.register(username, email, password);
            return "redirect:/posts";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
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
            model.addAttribute("provider", provider);
            model.addAttribute("providerId", providerId);
            model.addAttribute("email", email);
            return "register_complete";
        }
        try {
            userService.registerWithProvider(username, email, password, provider, providerId);
            return "redirect:/posts";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
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
}
