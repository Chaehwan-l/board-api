package lch.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lch.entity.UserAccount;
import lch.repository.UserAccountRepository;

@Service
public class UserAccountService {

    private final UserAccountRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountRepository userRepo, BCryptPasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // 기존 시그니처 유지(호환)
    @Transactional
    public UserAccount register(String username, String rawPassword) {
        // 이메일이 없는 예전 로직은 username만 검증
        if (userRepo.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        UserAccount user = UserAccount.builder()
            .username(username)
            .email(username + "@placeholder.local") // 호환용. 새 UI에서는 사용하지 않음.
            .password(passwordEncoder.encode(rawPassword))
            .role("USER")
            .signupCompleted(true)
            .build();
        return userRepo.save(user);
    }

    // 새 로직: 일반 회원가입(아이디+이메일+비밀번호)
    @Transactional
    public UserAccount register(String username, String email, String rawPassword) {
        if (userRepo.findByUsername(username).isPresent()) {
			throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
		}
        if (userRepo.findByEmail(email).isPresent()) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}
        try {
            UserAccount user = UserAccount.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role("USER")
                .signupCompleted(true)
                .build();
            return userRepo.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("아이디 또는 이메일이 중복입니다.");
        }
    }

    /**
     * 소셜 인증 기반 회원가입 완료 처리
     * (provider, providerId)로 매핑된 임시 사용자에 ID/Email/PW 설정 후 완료 플래그 세팅
     */
    @Transactional
    public UserAccount registerWithProvider(String username, String email, String rawPassword,
                                            String provider, String providerId) {
        if (userRepo.findByUsername(username).isPresent()) {
			throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
		}
        if (userRepo.findByEmail(email).isPresent()) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}

        return userRepo.findByProviderAndProviderId(provider, providerId)
            .map(user -> {
                if (Boolean.TRUE.equals(user.getSignupCompleted())) {
					throw new IllegalArgumentException("이미 가입 완료된 계정입니다.");
				}
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(rawPassword));
                user.setSignupCompleted(true);
                if (user.getRole() == null) {
					user.setRole("USER");
				}
                return userRepo.save(user);
            })
            .orElseGet(() -> {
                UserAccount user = UserAccount.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .role("USER")
                    .provider(provider)
                    .providerId(providerId)
                    .signupCompleted(true)
                    .build();
                return userRepo.save(user);
            });
    }

    // 기존 시그니처 유지(호환): 이메일 없이 완료 처리 → 이메일 미설정 상태를 막기 위해 호출 지양
    @Transactional
    public UserAccount registerWithProvider(String username, String rawPassword,
                                            String provider, String providerId) {
        return registerWithProvider(username, username + "@placeholder.local", rawPassword, provider, providerId);
    }
}
