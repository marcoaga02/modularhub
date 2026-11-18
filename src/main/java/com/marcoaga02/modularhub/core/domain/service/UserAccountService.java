package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.api.mapper.AccountPreferencesMapper;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.core.infrastructure.keycloak.KeycloakClaims;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {

    private final UserRepository userRepository;

    private final AccountPreferencesMapper accountPreferencesMapper;

    public UserAccountService(UserRepository userRepository, AccountPreferencesMapper accountPreferencesMapper) {
        this.userRepository = userRepository;
        this.accountPreferencesMapper = accountPreferencesMapper;
    }

    @Transactional
    public User syncUser(Jwt jwt) {
        User user = userRepository.findByIdentityId(jwt.getSubject())
                .orElseGet(() -> newUser(jwt.getSubject()));

        user.setUsername(jwt.getClaimAsString(KeycloakClaims.USERNAME));
        user.setEmail(jwt.getClaimAsString(KeycloakClaims.EMAIL));
        user.setFirstname(jwt.getClaimAsString(KeycloakClaims.FIRST_NAME));
        user.setLastname(jwt.getClaimAsString(KeycloakClaims.LAST_NAME));

        return userRepository.save(user);
    }

    @Transactional
    public AccountPreferencesResponseDTO updatePreferences(Jwt jwt, AccountPreferencesUpdateDTO dto) {
        User user = syncUser(jwt);

        accountPreferencesMapper.updateEntity(dto, user.getPreferences());

        return accountPreferencesMapper.toDto(user.getPreferences());
    }

    public String resolveFullName(String identityId) {
        if (identityId == null) {
            return "";
        }

        return userRepository.findByIdentityId(identityId)
                .map(User::getFullName)
                .orElse("");
    }

    private User newUser(String identityId) {
        AccountPreferences preferences = new AccountPreferences();
        preferences.setLanguage(Language.DEFAULT);

        User user = new User();
        user.setIdentityId(identityId);
        user.setEnabled(true);
        user.setPreferences(preferences);

        return user;
    }
}
