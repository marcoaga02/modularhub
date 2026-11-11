package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.IdentityGroupDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserUpdateRequestDTO;
import com.marcoaga02.modularhub.core.api.exception.SelfDeletionException;
import com.marcoaga02.modularhub.core.spi.IdentityProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class IdentityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityService.class);

    private final IdentityProviderService identityProvider;

    private final AccountService accountService;

    public IdentityService(IdentityProviderService identityProvider, AccountService accountService) {
        this.identityProvider = identityProvider;
        this.accountService = accountService;
    }

    public String createUser(IdentityUserCreateRequestDTO user) {
        String userId = identityProvider.createUser(user);

        if (StringUtils.hasText(user.getPassword())) {
            try {
                identityProvider.updatePassword(userId, user.getPassword());
            } catch (RuntimeException e) {
                rollbackCreatedUser(userId);
                throw e;
            }
        }

        return userId;
    }

    public void rollbackCreatedUser(String identityId) {
        try {
            identityProvider.deleteUser(identityId);
        } catch (RuntimeException e) {
            LOGGER.warn("Unable to rollback created identity user '{}'", identityId, e);
        }
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