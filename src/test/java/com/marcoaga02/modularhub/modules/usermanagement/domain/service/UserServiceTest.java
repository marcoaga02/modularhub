package com.marcoaga02.modularhub.modules.usermanagement.domain.service;

import com.marcoaga02.modularhub.core.api.dto.AuditDTO;
import com.marcoaga02.modularhub.core.api.dto.GenderDTO;
import com.marcoaga02.modularhub.core.api.dto.LanguageDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserUpdateRequestDTO;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.Role;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.core.domain.repository.RoleRepository;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.core.domain.service.AccountService;
import com.marcoaga02.modularhub.core.domain.service.IdentityService;
import com.marcoaga02.modularhub.core.domain.service.UserAccountService;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCreateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCriteriaDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserUpdateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserManagementExceptionCodes;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.role.RoleNotFoundException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.LastAdminException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.UserAlreadyExistsException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.UserNotFoundException;
import com.marcoaga02.modularhub.modules.usermanagement.api.mapper.UserMapper;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.marcoaga02.modularhub.fixture.UserFixtures.adminRole;
import static com.marcoaga02.modularhub.fixture.UserFixtures.role;
import static com.marcoaga02.modularhub.fixture.UserFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String UNKNOWN_UUID = "unknown-uuid";
    private static final String IDENTITY_ID = "identity-id";
    private static final String CURRENT_IDENTITY_ID = "current-identity-id";
    private static final String NEW_TAX_ID = "NEW-TAX";
    private static final String NEW_USERNAME = "new.username";
    private static final String NEW_EMAIL = "new@test.com";
    private static final String ADMIN = "ADMIN";
    private static final OffsetDateTime CREATED_ON = OffsetDateTime.parse("2026-01-01T10:00:00+01:00");
    private static final OffsetDateTime UPDATED_ON = OffsetDateTime.parse("2026-02-01T10:00:00+01:00");

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private IdentityService identityService;

    @Mock
    private AccountService accountService;

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();
        existingUser = user("existing", Language.EN);
        existingUser.setIdentityId(IDENTITY_ID);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clear();
    }

    @Nested
    class GetAllUsers {

        @Test
        void getAllUsers_shouldReturnMappedPageKeepingPagination_whenUsersMatch() {
            Pageable pageable = PageRequest.of(1, 2);
            User otherUser = user("other", Language.EN);
            UserResponseDTO existingDto = new UserResponseDTO().id("existing");
            UserResponseDTO otherDto = new UserResponseDTO().id("other");
            when(userRepository.findAll(anySpecification(), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(existingUser, otherUser), pageable, 5));
            when(userMapper.toDto(existingUser)).thenReturn(existingDto);
            when(userMapper.toDto(otherUser)).thenReturn(otherDto);

            Page<UserResponseDTO> result = userService.getAllUsers(new UserCriteriaDTO("text"), pageable);

            assertThat(result.getContent()).containsExactly(existingDto, otherDto);
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getPageable()).isEqualTo(pageable);
        }

        @Test
        void getAllUsers_shouldReturnEmptyPage_whenNoUserMatches() {
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findAll(anySpecification(), eq(pageable))).thenReturn(Page.empty(pageable));

            Page<UserResponseDTO> result = userService.getAllUsers(new UserCriteriaDTO(), pageable);

            assertThat(result).isEmpty();
            verifyNoInteractions(userMapper);
        }

        private static Specification<User> anySpecification() {
            return ArgumentMatchers.any();
        }
    }

    @Nested
    class GetUserByUuid {

        @Test
        void getUserByUuid_shouldReturnMappedUserWithResolvedAudit_whenUserExists() {
            existingUser.setCreatedOn(CREATED_ON);
            existingUser.setCreatedBy("creator-id");
            existingUser.setUpdatedOn(UPDATED_ON);
            existingUser.setUpdatedBy("updater-id");
            UserResponseDTO dto = new UserResponseDTO().id(existingUser.getUuid());
            mockExistingUser();
            when(userMapper.toDto(existingUser)).thenReturn(dto);
            when(userAccountService.resolveFullName("creator-id")).thenReturn("Creator Name");
            when(userAccountService.resolveFullName("updater-id")).thenReturn("Updater Name");

            UserResponseDTO result = userService.getUserByUuid(existingUser.getUuid());

            assertThat(result).isSameAs(dto);
            assertThat(result.getAudit())
                    .extracting(AuditDTO::getCreatedOn, AuditDTO::getCreatedBy, AuditDTO::getUpdatedOn,
                            AuditDTO::getUpdatedBy)
                    .containsExactly(CREATED_ON, "Creator Name", UPDATED_ON, "Updater Name");
            verifyNoInteractions(identityService);
        }

        @Test
        void getUserByUuid_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
            when(userRepository.findByUuidAndDeletedOnIsNull(UNKNOWN_UUID)).thenReturn(Optional.empty());

            assertUserNotFound(() -> userService.getUserByUuid(UNKNOWN_UUID));

            verifyNoInteractions(userMapper, userAccountService, identityService);
        }
    }

    @Nested
    class CreateUser {

        @Test
        void createUser_shouldCreateIdentityUserAndPersistMappedUserWithIdentityId_whenRequestIsValid() {
            UserCreateRequestDTO request = createRequest();
            User persisted = new User();
            UserResponseDTO dto = new UserResponseDTO();
            mockValidCreation();
            when(userRepository.saveAndFlush(any(User.class))).thenReturn(persisted);
            when(userMapper.toDto(persisted)).thenReturn(dto);

            UserResponseDTO result = userService.createUser(request);

            assertThat(result).isSameAs(dto);
            User saved = captureSavedUser();
            assertThat(saved.getIdentityId()).isEqualTo(IDENTITY_ID);
            verify(userMapper).updateEntity(request, saved);
        }

        @Test
        void createUser_shouldSendRequestDataToIdentityProviderWithVerifiedEmail() {
            mockValidCreation();

            userService.createUser(createRequest());

            verify(identityService).createUser(new IdentityUserCreateRequestDTO(NEW_USERNAME, NEW_EMAIL, "Mario",
                    "Rossi", true, true, "password"));
        }

        @Test
        void createUser_shouldCreateIdentityUserBeforeSavingTheUser() {
            mockValidCreation();

            userService.createUser(createRequest());

            InOrder inOrder = inOrder(identityService, userRepository);
            inOrder.verify(identityService).createUser(any());
            inOrder.verify(userRepository).saveAndFlush(any(User.class));
        }

        @Test
        void createUser_shouldDeleteIdentityUser_whenTransactionRollsBack() {
            mockValidCreation();

            userService.createUser(createRequest());
            completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);

            verify(identityService).rollbackCreatedUser(IDENTITY_ID);
        }

        @Test
        void createUser_shouldKeepIdentityUser_whenTransactionCommits() {
            mockValidCreation();

            userService.createUser(createRequest());
            completeTransaction(TransactionSynchronization.STATUS_COMMITTED);

            verify(identityService, never()).rollbackCreatedUser(anyString());
        }

        @Test
        void createUser_shouldPropagateErrorAndKeepRollbackRegistered_whenSaveFails() {
            RuntimeException dbError = new RuntimeException("db error");
            mockValidCreation();
            when(userRepository.saveAndFlush(any(User.class))).thenThrow(dbError);
            UserCreateRequestDTO request = createRequest();

            assertThatThrownBy(() -> userService.createUser(request)).isSameAs(dbError);

            completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);
            verify(identityService).rollbackCreatedUser(IDENTITY_ID);
            verify(userMapper, never()).toDto(any());
        }

        @Test
        void createUser_shouldNotSaveUser_whenIdentityCreationFails() {
            RuntimeException identityError = new RuntimeException("identity error");
            when(identityService.createUser(any())).thenThrow(identityError);
            UserCreateRequestDTO request = createRequest();

            assertThatThrownBy(() -> userService.createUser(request)).isSameAs(identityError);

            verify(userRepository, never()).saveAndFlush(any());
            assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();
        }

        @Test
        void createUser_shouldThrowUserAlreadyExistsException_whenTaxIdNumberIsTaken() {
            when(userRepository.findByTaxIdNumberAndDeletedOnIsNull(NEW_TAX_ID)).thenReturn(Optional.of(existingUser));
            UserCreateRequestDTO request = createRequest();

            assertAlreadyExists(() -> userService.createUser(request),
                    UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_TAX_ID, NEW_TAX_ID);

            assertNothingCreated();
        }

        @Test
        void createUser_shouldThrowUserAlreadyExistsException_whenUsernameIsTaken() {
            when(userRepository.findByUsernameAndDeletedOnIsNull(NEW_USERNAME)).thenReturn(Optional.of(existingUser));
            UserCreateRequestDTO request = createRequest();

            assertAlreadyExists(() -> userService.createUser(request),
                    UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_USERNAME, NEW_USERNAME);

            assertNothingCreated();
        }

        @Test
        void createUser_shouldThrowUserAlreadyExistsException_whenEmailIsTaken() {
            when(userRepository.findByEmailAndDeletedOnIsNull(NEW_EMAIL)).thenReturn(Optional.of(existingUser));
            UserCreateRequestDTO request = createRequest();

            assertAlreadyExists(() -> userService.createUser(request),
                    UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_EMAIL, NEW_EMAIL);

            assertNothingCreated();
        }

        @Test
        void createUser_shouldAssignRequestedRoles_whenRolesExist() {
            Role custom = role("custom");
            UserCreateRequestDTO request = createRequest().roleIds(Set.of(custom.getUuid()));
            mockValidCreation();
            when(roleRepository.findAllByUuidIn(Set.of(custom.getUuid()))).thenReturn(List.of(custom));

            userService.createUser(request);

            assertThat(captureSavedUser().getRoles()).containsExactly(custom);
        }

        @Test
        void createUser_shouldThrowRoleNotFoundException_whenARoleDoesNotExist() {
            UserCreateRequestDTO request = createRequest().roleIds(Set.of(UNKNOWN_UUID));
            when(roleRepository.findAllByUuidIn(Set.of(UNKNOWN_UUID))).thenReturn(List.of());

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOfSatisfying(RoleNotFoundException.class, ex ->
                            assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.Role.NOT_FOUND));

            assertNothingCreated();
        }

        private void mockValidCreation() {
            when(identityService.createUser(any())).thenReturn(IDENTITY_ID);
        }

        private User captureSavedUser() {
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).saveAndFlush(captor.capture());
            return captor.getValue();
        }

        private void assertNothingCreated() {
            verifyNoInteractions(identityService);
            verify(userRepository, never()).saveAndFlush(any());
            assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();
        }

        private static UserCreateRequestDTO createRequest() {
            return new UserCreateRequestDTO()
                    .firstname("Mario")
                    .lastname("Rossi")
                    .gender(GenderDTO.M)
                    .language(LanguageDTO.EN)
                    .taxIdNumber(NEW_TAX_ID)
                    .email(NEW_EMAIL)
                    .username(NEW_USERNAME)
                    .password("password")
                    .enabled(true);
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void updateUser_shouldUpdateEntityAndIdentityUser_whenRequestIsValid() {
            UserUpdateRequestDTO request = updateRequestKeepingUniqueFields();
            UserResponseDTO dto = new UserResponseDTO();
            mockExistingUser();
            when(userMapper.toDto(existingUser)).thenReturn(dto);

            UserResponseDTO result = userService.updateUser(existingUser.getUuid(), request);

            assertThat(result).isSameAs(dto);
            verify(userMapper).updateEntity(request, existingUser);
            verify(identityService).updateUser(IDENTITY_ID, new IdentityUserUpdateRequestDTO(
                    request.getUsername(), request.getEmail(), "Luigi", "Verdi", false));
        }

        @Test
        void updateUser_shouldSaveUserBeforeUpdatingIdentityUser() {
            mockExistingUser();

            userService.updateUser(existingUser.getUuid(), updateRequestKeepingUniqueFields());

            InOrder inOrder = inOrder(userRepository, identityService);
            inOrder.verify(userRepository).saveAndFlush(existingUser);
            inOrder.verify(identityService).updateUser(eq(IDENTITY_ID), any());
        }

        @Test
        void updateUser_shouldNotCheckDuplicates_whenUniqueFieldsAreUnchanged() {
            mockExistingUser();

            userService.updateUser(existingUser.getUuid(), updateRequestKeepingUniqueFields());

            verify(userRepository, never()).findByTaxIdNumberAndDeletedOnIsNull(anyString());
            verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
            verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
        }

        @Test
        void updateUser_shouldCheckDuplicatesAndUpdate_whenUniqueFieldsChangeToFreeValues() {
            mockExistingUser();

            userService.updateUser(existingUser.getUuid(), updateRequestChangingUniqueFields());

            verify(userRepository).findByTaxIdNumberAndDeletedOnIsNull(NEW_TAX_ID);
            verify(userRepository).findByUsernameAndDeletedOnIsNull(NEW_USERNAME);
            verify(userRepository).findByEmailAndDeletedOnIsNull(NEW_EMAIL);
            verify(userRepository).saveAndFlush(existingUser);
        }

        @Test
        void updateUser_shouldThrowUserAlreadyExistsException_whenNewTaxIdNumberIsTaken() {
            mockExistingUser();
            when(userRepository.findByTaxIdNumberAndDeletedOnIsNull(NEW_TAX_ID)).thenReturn(Optional.of(new User()));
            UserUpdateRequestDTO request = updateRequestChangingUniqueFields();

            assertAlreadyExists(() -> userService.updateUser(existingUser.getUuid(), request),
                    UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_TAX_ID, NEW_TAX_ID);

            assertNothingUpdated();
        }

        @Test
        void updateUser_shouldThrowUserAlreadyExistsException_whenNewUsernameIsTaken() {
            mockExistingUser();
            when(userRepository.findByUsernameAndDeletedOnIsNull(NEW_USERNAME)).thenReturn(Optional.of(new User()));
            UserUpdateRequestDTO request = updateRequestChangingUniqueFields();

            assertAlreadyExists(() -> userService.updateUser(existingUser.getUuid(), request),
                    UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_USERNAME, NEW_USERNAME);

            assertNothingUpdated();
        }

        @Test
        void updateUser_shouldThrowUserAlreadyExistsException_whenNewEmailIsTaken() {
            mockExistingUser();
            when(userRepository.findByEmailAndDeletedOnIsNull(NEW_EMAIL)).thenReturn(Optional.of(new User()));
            UserUpdateRequestDTO request = updateRequestChangingUniqueFields();

            assertAlreadyExists(() -> userService.updateUser(existingUser.getUuid(), request),
                    UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_EMAIL, NEW_EMAIL);

            assertNothingUpdated();
        }

        @Test
        void updateUser_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
            when(userRepository.findByUuidAndDeletedOnIsNull(UNKNOWN_UUID)).thenReturn(Optional.empty());
            UserUpdateRequestDTO request = updateRequestKeepingUniqueFields();

            assertUserNotFound(() -> userService.updateUser(UNKNOWN_UUID, request));

            assertNothingUpdated();
        }

        @Test
        void updateUser_shouldPropagateError_whenIdentityUpdateFails() {
            RuntimeException identityError = new RuntimeException("identity error");
            mockExistingUser();
            doThrow(identityError).when(identityService).updateUser(eq(IDENTITY_ID), any());
            UserUpdateRequestDTO request = updateRequestKeepingUniqueFields();

            assertThatThrownBy(() -> userService.updateUser(existingUser.getUuid(), request)).isSameAs(identityError);

            verify(userMapper, never()).toDto(any());
        }

        @Test
        void updateUser_shouldReplaceRolesWithRequestedOnes_whenRolesExist() {
            Role previous = role("previous");
            Role requested = role("requested");
            existingUser.getRoles().add(previous);
            UserUpdateRequestDTO request = updateRequestKeepingUniqueFields().roleIds(Set.of(requested.getUuid()));
            mockExistingUser();
            when(roleRepository.findAllByUuidIn(Set.of(requested.getUuid()))).thenReturn(List.of(requested));

            userService.updateUser(existingUser.getUuid(), request);

            assertThat(existingUser.getRoles()).containsExactly(requested);
            verify(userRepository).saveAndFlush(existingUser);
        }

        @Test
        void updateUser_shouldThrowRoleNotFoundException_whenARoleDoesNotExist() {
            UserUpdateRequestDTO request = updateRequestKeepingUniqueFields().roleIds(Set.of(UNKNOWN_UUID));
            mockExistingUser();
            when(roleRepository.findAllByUuidIn(Set.of(UNKNOWN_UUID))).thenReturn(List.of());

            assertThatThrownBy(() -> userService.updateUser(existingUser.getUuid(), request))
                    .isInstanceOf(RoleNotFoundException.class);

            verify(userRepository, never()).saveAndFlush(any());
            verifyNoInteractions(identityService);
        }

        @Test
        void updateUser_shouldThrowLastAdminException_whenRemovingAdminFromTheLastActiveAdmin() {
            existingUser.getRoles().add(adminRole());
            UserUpdateRequestDTO request = updateRequestKeepingUniqueFields();
            mockExistingUser();
            when(userRepository.countByRoles_CodeAndEnabledTrueAndDeletedOnIsNullAndIdNot(ADMIN, existingUser.getId()))
                    .thenReturn(0L);

            assertLastAdmin(() -> userService.updateUser(existingUser.getUuid(), request));

            verify(userRepository, never()).saveAndFlush(any());
            verifyNoInteractions(identityService);
        }

        @Test
        void updateUser_shouldThrowLastAdminException_whenDisablingTheLastActiveAdmin() {
            Role admin = adminRole();
            existingUser.getRoles().add(admin);
            UserUpdateRequestDTO request = updateRequestKeepingUniqueFields().roleIds(Set.of(admin.getUuid()));
            mockExistingUser();
            when(roleRepository.findAllByUuidIn(Set.of(admin.getUuid()))).thenReturn(List.of(admin));
            doAnswer(invocation -> {
                existingUser.setEnabled(false);
                return existingUser;
            }).when(userMapper).updateEntity(request, existingUser);
            when(userRepository.countByRoles_CodeAndEnabledTrueAndDeletedOnIsNullAndIdNot(ADMIN, existingUser.getId()))
                    .thenReturn(0L);

            assertLastAdmin(() -> userService.updateUser(existingUser.getUuid(), request));

            verify(userRepository, never()).saveAndFlush(any());
        }

        @Test
        void updateUser_shouldRemoveAdmin_whenAnotherActiveAdminExists() {
            existingUser.getRoles().add(adminRole());
            UserUpdateRequestDTO request = updateRequestKeepingUniqueFields();
            mockExistingUser();
            when(userRepository.countByRoles_CodeAndEnabledTrueAndDeletedOnIsNullAndIdNot(ADMIN, existingUser.getId()))
                    .thenReturn(1L);

            userService.updateUser(existingUser.getUuid(), request);

            assertThat(existingUser.getRoles()).isEmpty();
            verify(userRepository).saveAndFlush(existingUser);
        }

        @Test
        void updateUser_shouldNotCountAdmins_whenTheAdminKeepsTheRoleAndStaysEnabled() {
            Role admin = adminRole();
            existingUser.getRoles().add(admin);
            UserUpdateRequestDTO request = updateRequestKeepingUniqueFields().roleIds(Set.of(admin.getUuid()));
            mockExistingUser();
            when(roleRepository.findAllByUuidIn(Set.of(admin.getUuid()))).thenReturn(List.of(admin));

            userService.updateUser(existingUser.getUuid(), request);

            verify(userRepository, never()).countByRoles_CodeAndEnabledTrueAndDeletedOnIsNullAndIdNot(any(), any());
            verify(userRepository).saveAndFlush(existingUser);
        }

        private void assertNothingUpdated() {
            verify(userMapper, never()).updateEntity(any(UserUpdateRequestDTO.class), any());
            verify(userRepository, never()).saveAndFlush(any());
            verifyNoInteractions(identityService);
        }

        private UserUpdateRequestDTO updateRequestKeepingUniqueFields() {
            return updateRequest(existingUser.getTaxIdNumber(), existingUser.getUsername(), existingUser.getEmail());
        }

        private static UserUpdateRequestDTO updateRequestChangingUniqueFields() {
            return updateRequest(NEW_TAX_ID, NEW_USERNAME, NEW_EMAIL);
        }

        private static UserUpdateRequestDTO updateRequest(String taxIdNumber, String username, String email) {
            return new UserUpdateRequestDTO()
                    .firstname("Luigi")
                    .lastname("Verdi")
                    .gender(GenderDTO.M)
                    .language(LanguageDTO.EN)
                    .taxIdNumber(taxIdNumber)
                    .email(email)
                    .username(username)
                    .enabled(false);
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void deleteUser_shouldSoftDeleteUserWithCurrentIdentityAndDeleteIdentityUser_whenUserExists() {
            mockExistingUser();
            when(accountService.getCurrentIdentityId()).thenReturn(CURRENT_IDENTITY_ID);
            OffsetDateTime before = OffsetDateTime.now();

            userService.deleteUser(existingUser.getUuid());

            assertThat(existingUser.getDeletedBy()).isEqualTo(CURRENT_IDENTITY_ID);
            assertThat(existingUser.getDeletedOn()).isBetween(before, OffsetDateTime.now());
            assertThat(existingUser.getRoles()).isEmpty();
            InOrder inOrder = inOrder(userRepository, identityService);
            inOrder.verify(userRepository).saveAndFlush(existingUser);
            inOrder.verify(identityService).deleteUser(IDENTITY_ID);
        }

        @Test
        void deleteUser_shouldThrowLastAdminException_whenDeletingTheLastActiveAdmin() {
            existingUser.getRoles().add(adminRole());
            mockExistingUser();
            when(userRepository.countByRoles_CodeAndEnabledTrueAndDeletedOnIsNullAndIdNot(ADMIN, existingUser.getId()))
                    .thenReturn(0L);

            assertLastAdmin(() -> userService.deleteUser(existingUser.getUuid()));

            assertThat(existingUser.getDeletedOn()).isNull();
            verify(userRepository, never()).saveAndFlush(any());
            verifyNoInteractions(identityService);
        }

        @Test
        void deleteUser_shouldDeleteAdminAndClearRoles_whenAnotherActiveAdminExists() {
            existingUser.getRoles().add(adminRole());
            mockExistingUser();
            when(userRepository.countByRoles_CodeAndEnabledTrueAndDeletedOnIsNullAndIdNot(ADMIN, existingUser.getId()))
                    .thenReturn(1L);
            when(accountService.getCurrentIdentityId()).thenReturn(CURRENT_IDENTITY_ID);

            userService.deleteUser(existingUser.getUuid());

            assertThat(existingUser.getRoles()).isEmpty();
            assertThat(existingUser.getDeletedOn()).isNotNull();
            verify(identityService).deleteUser(IDENTITY_ID);
        }

        @Test
        void deleteUser_shouldPropagateError_whenIdentityDeletionFails() {
            RuntimeException identityError = new RuntimeException("identity error");
            mockExistingUser();
            when(accountService.getCurrentIdentityId()).thenReturn(CURRENT_IDENTITY_ID);
            doThrow(identityError).when(identityService).deleteUser(IDENTITY_ID);

            assertThatThrownBy(() -> userService.deleteUser(existingUser.getUuid())).isSameAs(identityError);
        }

        @Test
        void deleteUser_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
            when(userRepository.findByUuidAndDeletedOnIsNull(UNKNOWN_UUID)).thenReturn(Optional.empty());

            assertUserNotFound(() -> userService.deleteUser(UNKNOWN_UUID));

            verify(userRepository, never()).saveAndFlush(any());
            verifyNoInteractions(accountService, identityService);
        }
    }

    @Nested
    class ResetPassword {

        @Test
        void resetPassword_shouldResetIdentityUserPassword_whenUserExists() {
            mockExistingUser();

            userService.resetPassword(existingUser.getUuid());

            verify(identityService).resetPassword(IDENTITY_ID);
        }

        @Test
        void resetPassword_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
            when(userRepository.findByUuidAndDeletedOnIsNull(UNKNOWN_UUID)).thenReturn(Optional.empty());

            assertUserNotFound(() -> userService.resetPassword(UNKNOWN_UUID));

            verifyNoInteractions(identityService);
        }
    }

    private void mockExistingUser() {
        when(userRepository.findByUuidAndDeletedOnIsNull(existingUser.getUuid())).thenReturn(Optional.of(existingUser));
    }

    private static void completeTransaction(int status) {
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCompletion(status));
    }

    private static void assertUserNotFound(ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOfSatisfying(UserNotFoundException.class, ex -> {
                    assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.NOT_FOUND);
                    assertThat(ex).hasMessage("User not found: " + UNKNOWN_UUID);
                });
    }

    private static void assertLastAdmin(ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOfSatisfying(LastAdminException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.LAST_ADMIN));
    }

    private static void assertAlreadyExists(ThrowingCallable call, String errorCode, String value) {
        assertThatThrownBy(call)
                .isInstanceOfSatisfying(UserAlreadyExistsException.class, ex -> {
                    assertThat(ex.getErrorCode()).isEqualTo(errorCode);
                    assertThat(ex.getParams()).containsEntry("value", value);
                });
    }
}
