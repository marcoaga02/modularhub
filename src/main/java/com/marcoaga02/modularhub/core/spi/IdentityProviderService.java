package com.marcoaga02.modularhub.core.spi;

import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserUpdateRequestDTO;


public interface IdentityProviderService {

    String createUser(IdentityUserCreateRequestDTO user);

    void updateUser(String userId, IdentityUserUpdateRequestDTO user);

    void deleteUser(String userId);

    void updatePassword(String userId, String password);

    void resetPassword(String userId);
}
