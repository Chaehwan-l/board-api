package lch.controller;

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
}
