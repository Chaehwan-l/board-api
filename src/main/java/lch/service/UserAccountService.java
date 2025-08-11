package lch.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lch.entity.UserAccount;
import lch.repository.UserAccountRepository;

@Service
public class UserAccountService {

    private final UserAccountRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountRepository userRepo,
                              BCryptPasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // 일반 회원가입 (기존 유지)
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

    /**
     * 소셜 인증 기반 회원가입 "완료 처리"
     * - 성공핸들러가 만든 임시 소셜계정(provider+providerId)이 있으면 해당 레코드에 username/password를 세팅
     * - 없다면(성공핸들러 없이 바로 들어온 예외 케이스) 새로 생성
     */
    @Transactional
    public UserAccount registerWithProvider(
            String username, String rawPassword,
            String provider, String providerId) {

        // username 중복 체크 (동일 username을 이미 사용하는 타 유저가 있으면 예외)
        userRepo.findByUsername(username).ifPresent(u -> {
            // 임시 username은 "PROVIDER_PROVIDERID" 형태라 보통 충돌하지 않지만 방어적으로 체크
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        });

        return userRepo.findByProviderAndProviderId(provider, providerId)
            .map(user -> {
                // 임시 소셜계정이 이미 "완료"된 상태라면 가입 불가
                if (user.getPassword() != null && !user.getPassword().isBlank()) {
                    throw new IllegalArgumentException("이미 가입이 완료된 계정입니다. 로그인해 주세요.");
                }
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(rawPassword));
                return userRepo.save(user);
            })
            .orElseGet(() -> {
                // 성공핸들러가 임시 유저를 아직 만들지 않은 예외적 케이스 대비
                UserAccount user = UserAccount.builder()
                    .username(username)
                    .password(passwordEncoder.encode(rawPassword))
                    .role("USER")
                    .provider(provider)
                    .providerId(providerId)
                    .build();
                return userRepo.save(user);
            });
    }
}
