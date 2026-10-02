package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.IdentityGroupDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserUpdateRequestDTO;
import com.marcoaga02.modularhub.core.api.exception.SelfDeletionException;
import com.marcoaga02.modularhub.core.spi.IdentityProviderService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class IdentityService {

    private final IdentityProviderService identityProvider;

    private final AccountService accountService;

    public IdentityService(IdentityProviderService identityProvider, AccountService accountService) {
        this.identityProvider = identityProvider;
        this.accountService = accountService;
    }

    public String createUser(IdentityUserCreateRequestDTO user) {
        String userId = identityProvider.createUser(user);

        if (StringUtils.hasText(user.getPassword())) {
            identityProvider.updatePassword(userId, user.getPassword());
        }

        return userId;
    }

    public void updateUser(String identityId, IdentityUserUpdateRequestDTO user) {
        identityProvider.updateUser(identityId, user);
    }

    public IdentityUserResponseDTO getUserById(String identityId) {
        return identityProvider.getUserById(identityId);
    }

    public void resetPassword(String identityId) {
        identityProvider.resetPassword(identityId);
    }

    public void deleteUser(String identityId) {
        String currentIdentityId = accountService.getCurrentIdentityId();

        if (identityId.equals(currentIdentityId)) {
            throw new SelfDeletionException(identityId);
        }

        identityProvider.deleteUser(identityId);
    }

    public List<IdentityGroupDTO> getGroups() {
        return identityProvider.getGroups();
    }
}