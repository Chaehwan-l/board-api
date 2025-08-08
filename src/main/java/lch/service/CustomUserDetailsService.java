package lch.service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lch.entity.UserAccount;
import lch.repository.UserAccountRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserAccountRepository repo;

    public CustomUserDetailsService(UserAccountRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserAccount u = repo.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("사용자 없음: " + username));
        return new User(u.getUsername(), u.getPassword(),
                      List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole())));
    }
}
