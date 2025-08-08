package lch.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lch.entity.UserAccount;
import lch.repository.UserAccountRepository;

// 비밀번호 인코딩과 저장 로직 등 로그인 관련 담당할 서비스 클래스

@Service
public class UserAccountService {

    private final UserAccountRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountRepository userRepo,
                              BCryptPasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // 새 계정을 등록합니다 (중복 검사 + 비밀번호 암호화)
    public UserAccount register(String username, String rawPassword) {
        if (userRepo.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        UserAccount user = UserAccount.builder()
            .username(username)
            .password(passwordEncoder.encode(rawPassword))
            .role("USER")
            .build();
        return userRepo.save(user);
    }

    // 소셜 인증 기반 회원가입
    public UserAccount registerWithProvider(
            String username, String rawPassword,
            String provider, String providerId) {
        if (userRepo.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        UserAccount user = UserAccount.builder()
            .username(username)
            .password(passwordEncoder.encode(rawPassword))
            .role("USER")
            .provider(provider)
            .providerId(providerId)
            .build();
        return userRepo.save(user);
    }
}
