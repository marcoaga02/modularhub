package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.api.mapper.AccountPreferencesMapper;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.core.domain.repository.RoleRepository;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.core.infrastructure.keycloak.KeycloakClaims;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserAccountService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final AccountPreferencesMapper accountPreferencesMapper;

    private final String initialAdminUsername;

    public UserAccountService(UserRepository userRepository, RoleRepository roleRepository,
                              AccountPreferencesMapper accountPreferencesMapper,
                              @Value("${modularhub.initial-admin-username:}") String initialAdminUsername) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.accountPreferencesMapper = accountPreferencesMapper;
        this.initialAdminUsername = initialAdminUsername;
    }

    @Transactional
    public User syncUser(Jwt jwt) {
        User user = userRepository.findByIdentityId(jwt.getSubject())
                .orElseGet(() -> newUser(jwt.getSubject()));

        user.setUsername(jwt.getClaimAsString(KeycloakClaims.USERNAME));
        user.setEmail(jwt.getClaimAsString(KeycloakClaims.EMAIL));
        user.setFirstname(jwt.getClaimAsString(KeycloakClaims.FIRST_NAME));
        user.setLastname(jwt.getClaimAsString(KeycloakClaims.LAST_NAME));

        grantAdminToInitialAdministrator(user);

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

    private void grantAdminToInitialAdministrator(User user) {
        if (!StringUtils.hasText(initialAdminUsername)
                || !initialAdminUsername.equals(user.getUsername())
                || userRepository.countByRoles_CodeAndEnabledTrueAndDeletedOnIsNull(SecurityConstants.ADMIN_ROLE_CODE) > 0) {
            return;
        }

        roleRepository.findByCode(SecurityConstants.ADMIN_ROLE_CODE)
                .ifPresent(admin -> user.getRoles().add(admin));
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
