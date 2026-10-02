package com.marcoaga02.modularhub.core.spi;

import com.marcoaga02.modularhub.core.api.dto.IdentityGroupDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserUpdateRequestDTO;

import java.util.List;

public interface IdentityProviderService {

    String createUser(IdentityUserCreateRequestDTO user);

    void updateUser(String userId, IdentityUserUpdateRequestDTO user);

    void deleteUser(String userId);

    IdentityUserResponseDTO getUserById(String userId);

    List<IdentityGroupDTO> getGroups();

    List<IdentityGroupDTO> getUserGroups(String userId);

    void updatePassword(String userId, String password);

    void resetPassword(String userId);
}
