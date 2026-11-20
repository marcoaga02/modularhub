package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.AccountDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.api.exception.InternalStateException;
import com.marcoaga02.modularhub.core.api.mapper.AccountMapper;
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

    private final UserAccountService userAccountService;

    private final AccountMapper accountMapper;

    private final PermissionService permissionService;

    public AccountService(UserAccountService userAccountService, AccountMapper accountMapper, PermissionService permissionService) {
        this.userAccountService = userAccountService;
        this.accountMapper = accountMapper;
        this.permissionService = permissionService;
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
        AccountDTO accountDTO = accountMapper.toDto(userAccountService.syncUser(jwt));
        accountDTO.setPermissions(permissionService.getEffectivePermissions(jwt.getSubject()));

        return accountDTO;
    }

    public Set<String> getPermissions() {
        return getJwtAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .filter(a -> a.startsWith(SecurityConstants.PERMISSION_PREFIX))
                .map(a -> a.substring(SecurityConstants.PERMISSION_PREFIX.length()))
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean hasPermission(String permission) {
        return getPermissions().contains(permission);
    }

    public boolean hasAnyPermission(String... permissions) {
        Set<String> userPermissions = getPermissions();
        return Arrays.stream(permissions).anyMatch(userPermissions::contains);
    }

    public AccountPreferencesResponseDTO updateCurrentAccountPreferences(AccountPreferencesUpdateDTO dto) {
        return userAccountService.updatePreferences(getJwtAuthentication().getToken(), dto);
    }

    public String getCurrentIdentityId() {
        return getJwtAuthentication().getToken().getSubject();
    }
}
