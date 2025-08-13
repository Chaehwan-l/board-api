package lch.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import lch.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

	Optional<UserAccount> findByUsername(String username);
	Optional<UserAccount> findByEmail(String email);

	// Oauth
	Optional<UserAccount> findByProviderAndProviderId(String provider, String providerId);

}
