package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.IdentityGroupDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserUpdateRequestDTO;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.SelfDeletionException;
import com.marcoaga02.modularhub.core.spi.IdentityProviderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    @Mock
    private IdentityProviderService identityProvider;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private IdentityService identityService;

    @Test
    void createUser_shouldCreateUser_whenPasswordIsNotBlank() {
        IdentityUserCreateRequestDTO user = createIdentityUserCreateRequestDto(
                "mrossi",
                "rossi@email.it",
                "Mario",
                "Rossi",
                true,
                true,
                "Password123!",
                List.of("group1", "group2")
        );

        when(identityProvider.createUser(any()))
                .thenReturn("user-id");

        String userId = identityService.createUser(user);

        assertThat(userId).isEqualTo("user-id");
        verify(identityProvider).createUser(user);
        verify(identityProvider).updatePassword("user-id", "Password123!");
    }

    @Test
    void createUser_shouldCreateUser_whenPasswordIsBlank() {
        IdentityUserCreateRequestDTO user = createIdentityUserCreateRequestDto(
                "mrossi",
                "rossi@email.it",
                "Mario",
                "Rossi",
                true,
                true,
                "",
                List.of("group1", "group2")
        );

        when(identityProvider.createUser(any()))
                .thenReturn("user-id");

        String userId = identityService.createUser(user);

        assertThat(userId).isEqualTo("user-id");
        verify(identityProvider).createUser(user);
        verify(identityProvider, never()).updatePassword(any(), anyString());
    }

    @Test
    void createUser_shouldCreateUser_whenPasswordIsNull() {
        IdentityUserCreateRequestDTO user = createIdentityUserCreateRequestDto(
                "mrossi",
                "rossi@email.it",
                "Mario",
                "Rossi",
                true,
                true,
                null,
                List.of("group1", "group2")
        );

        when(identityProvider.createUser(any()))
                .thenReturn("user-id");

        String userId = identityService.createUser(user);

        assertThat(userId).isEqualTo("user-id");
        verify(identityProvider).createUser(user);
        verify(identityProvider, never()).updatePassword(any(), anyString());
    }

    @Test
    void updateUser_shoudUpdateUser() {
        IdentityUserUpdateRequestDTO user = createIdentityUserUpdateRequestDto(
                    "mrossi",
                    "rossi@email.it",
                    "Mario",
                    "Rossi",
                    true,
                    List.of("group1", "group2")
        );

        final String userId = "user-id";

        identityService.updateUser(userId, user);

        verify(identityProvider).updateUser(userId, user);
    }

    @Test
    void getUserById_shouldReturnUser() {
        final String identityId = "identity-id";
        IdentityGroupDTO groupDTO1 = createIdentityGroupDTO("group-id1", "first group", "first group description");
        IdentityGroupDTO groupDTO2 = createIdentityGroupDTO("group-id2", "second group", "second group description");

        IdentityUserResponseDTO userResponseDTO = createIdentityUserResponseDto(
                identityId,
                "mrossi",
                "rossi@email.it",
                "Mario",
                "Rossi",
                true,
                List.of(groupDTO1, groupDTO2)
        );

        when(identityProvider.getUserById(identityId)).thenReturn(userResponseDTO);

        IdentityUserResponseDTO result = identityService.getUserById(identityId);

        assertThat(result).isEqualTo(userResponseDTO);
        verify(identityProvider).getUserById(identityId);
    }


    @Test
    void deleteUser_shouldDeleteUser_whenValid() {
        final String userId = "user-id";
        
        when(accountService.getCurrentIdentityId())
                .thenReturn("another-id");

        identityService.deleteUser(userId);

        verify(accountService).getCurrentIdentityId();
        verify(identityProvider).deleteUser(userId);
    }

    @Test
    void deleteUser_shouldDeleteUser_whenDeletingTheCurrentUser() {
        final String userId = "user-id";

        when(accountService.getCurrentIdentityId())
                .thenReturn(userId);

        assertThatThrownBy(() -> identityService.deleteUser(userId))
                .isInstanceOf(SelfDeletionException.class)
                .satisfies(e -> {
                    SelfDeletionException ex = (SelfDeletionException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.SELF_DELETION);
                    assertThat(ex.getLogMessage()).isEqualTo("Account with identityId 'user-id' attempted to delete their own account");
                });

        verify(accountService).getCurrentIdentityId();
        verify(identityProvider, never()).deleteUser(anyString());
    }

    @Test
    void resetPassword_shouldResetPassword() {
        final String userId = "user-id";

        identityService.resetPassword(userId);

        verify(identityProvider).resetPassword(userId);
    }

    @Test
    void getGroups_shouldReturnEmptyList_whenNoGroupIsPresent() {
        when(identityProvider.getGroups()).thenReturn(List.of());

        List<IdentityGroupDTO> result = identityService.getGroups();

        assertThat(result).isEmpty();

        verify(identityProvider).getGroups();
    }

    @Test
    void getGroups_shouldReturnGroup_whenOneGroupIsPresent() {
        IdentityGroupDTO groupDto = createIdentityGroupDTO("group-id1", "first group", "first group description");

        when(identityProvider.getGroups()).thenReturn(List.of(groupDto));

        List<IdentityGroupDTO> result = identityService.getGroups();

        assertThat(result)
                .hasSize(1)
                .containsExactly(groupDto);

        verify(identityProvider).getGroups();
    }

    @Test
    void getGroups_shouldReturnAllGroups_whenMultipleGroupsArePresent() {
        IdentityGroupDTO firstGroupDto = createIdentityGroupDTO("group-id1", "first group", "first group description");
        IdentityGroupDTO secondGroupDto = createIdentityGroupDTO("group-id2", "second group",  "second group description");

        when(identityProvider.getGroups()).thenReturn(List.of(firstGroupDto, secondGroupDto));

        List<IdentityGroupDTO> result = identityService.getGroups();

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(firstGroupDto, secondGroupDto);

        verify(identityProvider).getGroups();
    }

    private IdentityUserCreateRequestDTO createIdentityUserCreateRequestDto(
            String username,
            String email,
            String firstName,
            String lastName,
            Boolean enabled,
            Boolean emailVerified,
            String password,
            List<String> groupIds
    ) {
        IdentityUserCreateRequestDTO userRequestDTO = new IdentityUserCreateRequestDTO();
        userRequestDTO.setUsername(username);
        userRequestDTO.setEmail(email);
        userRequestDTO.setFirstName(firstName);
        userRequestDTO.setLastName(lastName);
        userRequestDTO.setEnabled(enabled);
        userRequestDTO.setEmailVerified(emailVerified);
        userRequestDTO.setPassword(password);
        userRequestDTO.setGroupIds(groupIds);

        return userRequestDTO;
    }

    private IdentityUserUpdateRequestDTO createIdentityUserUpdateRequestDto(
            String username,
            String email,
            String firstName,
            String lastName,
            Boolean enabled,
            List<String> groupIds
    ) {
        IdentityUserUpdateRequestDTO userUpdateDTO = new IdentityUserUpdateRequestDTO();
        userUpdateDTO.setUsername(username);
        userUpdateDTO.setEmail(email);
        userUpdateDTO.setFirstName(firstName);
        userUpdateDTO.setLastName(lastName);
        userUpdateDTO.setEnabled(enabled);
        userUpdateDTO.setGroupIds(groupIds);

        return userUpdateDTO;
    }

    private IdentityUserResponseDTO createIdentityUserResponseDto(
            String identityId,
            String username,
            String email,
            String firstName,
            String lastName,
            Boolean enabled,
            List<IdentityGroupDTO> groups
    ) {
        IdentityUserResponseDTO userResponseDTO = new IdentityUserResponseDTO();
        userResponseDTO.setId(identityId);
        userResponseDTO.setUsername(username);
        userResponseDTO.setEmail(email);
        userResponseDTO.setFirstName(firstName);
        userResponseDTO.setLastName(lastName);
        userResponseDTO.setEnabled(enabled);
        userResponseDTO.setGroups(groups);

        return userResponseDTO;
    }

    private IdentityGroupDTO createIdentityGroupDTO(
            String id,
            String name,
            String description
    ) {
        IdentityGroupDTO groupDTO = new IdentityGroupDTO();
        groupDTO.setId(id);
        groupDTO.setName(name);
        groupDTO.setDescription(description);

        return groupDTO;
    }
}