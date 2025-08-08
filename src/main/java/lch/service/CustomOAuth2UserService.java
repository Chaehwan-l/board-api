package lch.service;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lch.entity.UserAccount;
import lch.repository.UserAccountRepository;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserAccountRepository userRepo;

    public CustomOAuth2UserService(UserAccountRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        String provider = req.getClientRegistration().getRegistrationId().toUpperCase();
        OAuth2User oidcUser = new DefaultOAuth2UserService().loadUser(req);
        Map<String,Object> attrs = oidcUser.getAttributes();

        // provider별로 id, email, name 꺼내기
        String providerId;
        String email;
        String name;
        if (provider.equals("NAVER")) {
            Map<String,Object> response = (Map<String,Object>)attrs.get("response");
            providerId = response.get("id").toString();
            email      = response.get("email").toString();
            name       = response.get("name").toString();
        } else {
            providerId = attrs.get("id").toString();
            email      = attrs.get("email").toString();
            name       = attrs.getOrDefault("name", attrs.get("login")).toString();
        }

        // user_account에 있으면 조회, 없으면 생성
        UserAccount user = userRepo.findByProviderAndProviderId(provider, providerId)
            .orElseGet(() -> {
                UserAccount u = UserAccount.builder()
                    .username(provider + "_" + providerId)
                    .password("")  // 소셜 로그인만 쓰므로 빈 스트링
                    .role("USER")
                    .provider(provider)
                    .providerId(providerId)
                    .build();
                return userRepo.save(u);
            });

        // Spring Security에 사용할 권한·속성 세팅
        return new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
            Map.of(
              "id", user.getId(),
              "username", user.getUsername(),
              "email", email,
              "name", name
            ),
            "username"
        );
    }
}
