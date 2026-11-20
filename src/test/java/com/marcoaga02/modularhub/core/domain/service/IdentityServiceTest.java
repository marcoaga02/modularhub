package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserUpdateRequestDTO;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.account.SelfDeletionException;
import com.marcoaga02.modularhub.core.spi.IdentityProviderService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    private static final String IDENTITY_ID = "identity-id";
    private static final String CURRENT_IDENTITY_ID = "current-identity-id";
    private static final String PASSWORD = "Password123!";

    @Mock
    private IdentityProviderService identityProvider;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private IdentityService identityService;

    @Nested
    class CreateUser {

        @Test
        void createUser_shouldCreateUserAndSetPassword_whenPasswordHasText() {
            IdentityUserCreateRequestDTO request = createRequest(PASSWORD);
            when(identityProvider.createUser(request)).thenReturn(IDENTITY_ID);

            String result = identityService.createUser(request);

            assertThat(result).isEqualTo(IDENTITY_ID);
            InOrder inOrder = inOrder(identityProvider);
            inOrder.verify(identityProvider).createUser(request);
            inOrder.verify(identityProvider).updatePassword(IDENTITY_ID, PASSWORD);
            verifyNoMoreInteractions(identityProvider);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        void createUser_shouldCreateUserWithoutPassword_whenPasswordIsBlank(String password) {
            IdentityUserCreateRequestDTO request = createRequest(password);
            when(identityProvider.createUser(request)).thenReturn(IDENTITY_ID);

            String result = identityService.createUser(request);

            assertThat(result).isEqualTo(IDENTITY_ID);
            verify(identityProvider).createUser(request);
            verifyNoMoreInteractions(identityProvider);
        }

        @Test
        void createUser_shouldDeleteCreatedUserAndRethrow_whenPasswordUpdateFails() {
            IdentityUserCreateRequestDTO request = createRequest(PASSWORD);
            RuntimeException passwordError = new RuntimeException("password error");
            when(identityProvider.createUser(request)).thenReturn(IDENTITY_ID);
            doThrow(passwordError).when(identityProvider).updatePassword(IDENTITY_ID, PASSWORD);

            assertThatThrownBy(() -> identityService.createUser(request)).isSameAs(passwordError);

            verify(identityProvider).deleteUser(IDENTITY_ID);
            verifyNoInteractions(accountService);
        }

        @Test
        void createUser_shouldRethrowPasswordError_whenRollbackDeletionAlsoFails() {
            IdentityUserCreateRequestDTO request = createRequest(PASSWORD);
            RuntimeException passwordError = new RuntimeException("password error");
            when(identityProvider.createUser(request)).thenReturn(IDENTITY_ID);
            doThrow(passwordError).when(identityProvider).updatePassword(IDENTITY_ID, PASSWORD);
            doThrow(new RuntimeException("delete error")).when(identityProvider).deleteUser(IDENTITY_ID);

            assertThatThrownBy(() -> identityService.createUser(request)).isSameAs(passwordError);
        }

        @Test
        void createUser_shouldNotSetPassword_whenUserCreationFails() {
            IdentityUserCreateRequestDTO request = createRequest(PASSWORD);
            RuntimeException creationError = new RuntimeException("creation error");
            when(identityProvider.createUser(request)).thenThrow(creationError);

            assertThatThrownBy(() -> identityService.createUser(request)).isSameAs(creationError);

            verify(identityProvider, never()).updatePassword(anyString(), anyString());
            verify(identityProvider, never()).deleteUser(anyString());
        }

        private static IdentityUserCreateRequestDTO createRequest(String password) {
            return new IdentityUserCreateRequestDTO("mario.rossi", "mario@test.com", "Mario", "Rossi",
                    true, true, password);
        }
    }

    @Nested
    class RollbackCreatedUser {

        @Test
        void rollbackCreatedUser_shouldDeleteUserWithoutSelfDeletionCheck() {
            identityService.rollbackCreatedUser(IDENTITY_ID);

            verify(identityProvider).deleteUser(IDENTITY_ID);
            verifyNoInteractions(accountService);
        }

        @Test
        void rollbackCreatedUser_shouldNotThrow_whenDeletionFails() {
            doThrow(new RuntimeException("delete error")).when(identityProvider).deleteUser(IDENTITY_ID);

            assertThatNoException().isThrownBy(() -> identityService.rollbackCreatedUser(IDENTITY_ID));

            verify(identityProvider).deleteUser(IDENTITY_ID);
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void updateUser_shouldDelegateToIdentityProvider() {
            IdentityUserUpdateRequestDTO request = new IdentityUserUpdateRequestDTO("mario.rossi", "mario@test.com",
                    "Mario", "Rossi", true);

            identityService.updateUser(IDENTITY_ID, request);

            verify(identityProvider).updateUser(IDENTITY_ID, request);
        }
    }

    @Nested
    class ResetPassword {

        @Test
        void resetPassword_shouldDelegateToIdentityProvider() {
            identityService.resetPassword(IDENTITY_ID);

            verify(identityProvider).resetPassword(IDENTITY_ID);
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void deleteUser_shouldDeleteUser_whenUserIsNotTheCurrentOne() {
            when(accountService.getCurrentIdentityId()).thenReturn(CURRENT_IDENTITY_ID);

            identityService.deleteUser(IDENTITY_ID);

            verify(identityProvider).deleteUser(IDENTITY_ID);
        }

        @Test
        void deleteUser_shouldThrowSelfDeletionException_whenUserIsTheCurrentOne() {
            when(accountService.getCurrentIdentityId()).thenReturn(IDENTITY_ID);

            assertThatThrownBy(() -> identityService.deleteUser(IDENTITY_ID))
                    .isInstanceOfSatisfying(SelfDeletionException.class, ex -> {
                        assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.Model.Account.SELF_DELETION);
                        assertThat(ex).hasMessage("Account attempted to delete itself: " + IDENTITY_ID);
                    });

            verifyNoInteractions(identityProvider);
        }
    }
}
