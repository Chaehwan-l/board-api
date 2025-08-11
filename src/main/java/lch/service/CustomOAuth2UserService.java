package lch.service;

import java.util.HashMap;
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


@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	@Override
	public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
		String provider = req.getClientRegistration().getRegistrationId().toUpperCase();
		OAuth2User delegate = new DefaultOAuth2UserService().loadUser(req);
		Map<String, Object> a = delegate.getAttributes();

		String providerId, email = null, name = null;

		if ("NAVER".equals(provider)) {
			Map<String, Object> r = (Map<String, Object>) a.get("response");
			providerId = r.get("id").toString();
			email = (String) r.get("email");
			name = (String) r.get("name");
		} else if ("GOOGLE".equals(provider)) {
			providerId = a.get("sub").toString(); // Google은 sub
			email = (String) a.get("email");
			name = (String) a.get("name");
		} else if ("GITHUB".equals(provider)) {
			providerId = a.get("id").toString();
			Object e = a.get("email"); // null 가능
			email = (e != null) ? e.toString() : null;
			Object n = a.get("name");
			name = (n != null) ? n.toString() : a.get("login").toString();
		} else {
			throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
		}

		Map<String, Object> principal = new HashMap<>();
		principal.put("provider", provider);
		principal.put("providerId", providerId);
		principal.put("email", email);
		principal.put("name", name);

		return new DefaultOAuth2User(
				List.of(new SimpleGrantedAuthority("ROLE_PRE_REGISTERED")),
				principal,
				"providerId"
		);
	}
}
