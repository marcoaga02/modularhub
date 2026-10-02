package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.AccountDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountProfileDTO;
import com.marcoaga02.modularhub.core.api.exception.InternalStateException;
import com.marcoaga02.modularhub.core.infrastructure.keycloak.KeycloakClaims;
import com.marcoaga02.modularhub.core.spi.AccountDetailsProvider;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequestScope
public class AccountService {

    // TODO valutare se fare chaching

    private final AccountPreferencesService accountPreferencesService;

    private final AccountDetailsProvider accountDetailsProvider;

    public AccountService(AccountPreferencesService accountPreferencesService, AccountDetailsProvider accountDetailsProvider) {
        this.accountPreferencesService = accountPreferencesService;
        this.accountDetailsProvider = accountDetailsProvider;
    }

    private JwtAuthenticationToken getJwtAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth;
        }

        throw new InternalStateException("No JWT authentication found in security context");
    }

    public AccountDTO getCurrentAccount() {
        Jwt jwt = getJwtAuthentication().getToken();
        String identityId = jwt.getSubject();

        AccountPreferencesResponseDTO accountPreferencesResponseDTO = accountPreferencesService.getAccountPreferences(identityId);

        AccountProfileDTO accountProfileDTO = accountDetailsProvider.getDetails(identityId);

        return AccountDTO.builder()
                .identityId(identityId)
                .email(jwt.getClaimAsString(KeycloakClaims.EMAIL))
                .username(jwt.getClaimAsString(KeycloakClaims.USERNAME))
                .firstName(jwt.getClaimAsString(KeycloakClaims.FIRST_NAME))
                .lastName(jwt.getClaimAsString(KeycloakClaims.LAST_NAME))
                .roles(getRoles())
                .preferences(accountPreferencesResponseDTO)
                .gender(accountProfileDTO.getGender())
                .mobileNumber(accountProfileDTO.getMobileNumber())
                .taxIdNumber(accountProfileDTO.getTaxIdNumber())
                .build();
    }

    public Set<String> getRoles() {
        return getJwtAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .filter(a -> a.startsWith(SecurityConstants.ROLE_PREFIX))
                .map(a -> a.substring(SecurityConstants.ROLE_PREFIX.length()))
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    public boolean hasAnyRole(String... roles) {
        Set<String> userRoles = getRoles();
        return Arrays.stream(roles).anyMatch(userRoles::contains);
    }

    public AccountPreferencesResponseDTO updateCurrentAccountPreferences(AccountPreferencesUpdateDTO dto) {
        return accountPreferencesService.updateAccountPreferencesByIdentityId(getCurrentIdentityId(), dto);
    }

    public String getCurrentIdentityId() {
        return getJwtAuthentication().getToken().getSubject();
    }
}
