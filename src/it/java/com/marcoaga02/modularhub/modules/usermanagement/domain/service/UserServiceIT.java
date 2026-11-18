package com.marcoaga02.modularhub.modules.usermanagement.domain.service;

import com.marcoaga02.modularhub.core.api.dto.LanguageDTO;
import com.marcoaga02.modularhub.config.BaseITWithMockIdentity;
import com.marcoaga02.modularhub.core.api.dto.AuditDTO;
import com.marcoaga02.modularhub.core.api.dto.GenderDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityGroupDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserUpdateRequestDTO;
import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCreateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCriteriaDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserUpdateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserManagementExceptionCodes;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.UserAlreadyExistsException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.UserNotFoundException;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static com.marcoaga02.modularhub.fixture.SecurityFixtures.authenticate;
import static com.marcoaga02.modularhub.fixture.UserFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Transactional
class UserServiceIT extends BaseITWithMockIdentity {

    private static final String NEW_IDENTITY_ID = "new-identity-id";
    private static final String CURRENT_IDENTITY_ID = "current-identity-id";
    private static final Sort BY_FIRSTNAME = Sort.by("firstname").ascending();

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User first;

    private User second;

    private User third;

    private User deleted;

    @BeforeEach
    void setUp() {

        first = userRepository.save(user("1", Language.IT));
        second = userRepository.save(user("2", Language.EN));
        third = userRepository.save(user("3", Language.EN));

        deleted = user("deleted", Language.IT);
        deleted.setDeletedOn(OffsetDateTime.now());
        userRepository.save(deleted);

        flushAndClear();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class GetAllUsers {

        @Test
        void getAllUsers_shouldReturnOnlyActiveUsers_whenCriteriaIsNull() {
            Page<UserResponseDTO> result = userService.getAllUsers(null, PageRequest.of(0, 10));

            assertThat(result.getContent())
                    .extracting(UserResponseDTO::getId)
                    .containsExactlyInAnyOrder(first.getUuid(), second.getUuid(), third.getUuid());
        }

        @Test
        void getAllUsers_shouldFilterByText_whenCriteriaHasText() {
            Page<UserResponseDTO> result = userService.getAllUsers(new UserCriteriaDTO("LASTNAME-2"),
                    PageRequest.of(0, 10));

            assertThat(result.getContent()).extracting(UserResponseDTO::getId).containsExactly(second.getUuid());
        }

        @Test
        void getAllUsers_shouldReturnRequestedSortedPage_whenPageIsSmallerThanResult() {
            Page<UserResponseDTO> result = userService.getAllUsers(null, PageRequest.of(1, 2, BY_FIRSTNAME));

            assertThat(result.getContent()).extracting(UserResponseDTO::getId).containsExactly(third.getUuid());
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
        }

        @Test
        void getAllUsers_shouldMapLanguageFromPreferences() {
            Page<UserResponseDTO> result = userService.getAllUsers(null, PageRequest.of(0, 1, BY_FIRSTNAME));

            assertThat(result.getContent().getFirst().getLanguage()).isEqualTo(LanguageDTO.IT);
        }
    }

    @Nested
    class GetUserByUuid {

        @Test
        void getUserByUuid_shouldReturnUserWithIdentityGroups_whenUserExists() {
            IdentityGroupDTO group = new IdentityGroupDTO().id("group-id").name("group");
            when(identityProvider.getUserById(first.getIdentityId())).thenReturn(identityUser(List.of(group)));

            UserResponseDTO result = userService.getUserByUuid(first.getUuid());

            assertThat(result)
                    .extracting(UserResponseDTO::getId, UserResponseDTO::getUsername, UserResponseDTO::getEmail)
                    .containsExactly(first.getUuid(), first.getUsername(), first.getEmail());
            assertThat(result.getLanguage()).isEqualTo(LanguageDTO.IT);
            assertThat(result.getGroups()).containsExactly(group);
        }

        @Test
        void getUserByUuid_shouldResolveAuditAuthorsToFullNames_whenAuthorsAreKnownUsers() {
            authenticate(first.getIdentityId());
            User created = userRepository.save(user("audited", Language.IT));
            flushAndClear();
            when(identityProvider.getUserById(created.getIdentityId())).thenReturn(identityUser(null));

            AuditDTO audit = userService.getUserByUuid(created.getUuid()).getAudit();

            assertThat(audit.getCreatedBy()).isEqualTo("firstname-1 lastname-1");
            assertThat(audit.getUpdatedBy()).isEqualTo("firstname-1 lastname-1");
            assertThat(audit.getCreatedOn()).isNotNull();
        }

        @Test
        void getUserByUuid_shouldThrowUserNotFoundException_whenUserIsDeleted() {
            assertUserNotFound(() -> userService.getUserByUuid(deleted.getUuid()), deleted.getUuid());
        }
    }

    @Nested
    class CreateUser {

        @Test
        void createUser_shouldPersistUserWithLanguageAndIdentityId_whenRequestIsValid() {
            when(identityProvider.createUser(any())).thenReturn(NEW_IDENTITY_ID);

            UserResponseDTO result = userService.createUser(createRequest("new"));
            flushAndClear();

            User saved = userRepository.findByUuidAndDeletedOnIsNull(result.getId()).orElseThrow();
            assertThat(saved)
                    .extracting(User::getIdentityId, User::getUsername, User::getTaxIdNumber, User::getGender)
                    .containsExactly(NEW_IDENTITY_ID, "username-new", "TAX-new", Gender.F);
            assertThat(saved.getPreferences().getLanguage()).isEqualTo(Language.EN);
            assertThat(result.getLanguage()).isEqualTo(LanguageDTO.EN);
        }

        @Test
        void createUser_shouldCreateIdentityUserWithPasswordAndGroups() {
            when(identityProvider.createUser(any())).thenReturn(NEW_IDENTITY_ID);

            userService.createUser(createRequest("new"));

            verify(identityProvider).createUser(new IdentityUserCreateRequestDTO("username-new", "new@test.com",
                    "firstname-new", "lastname-new", true, true, "password", List.of("group-id")));
            verify(identityProvider).updatePassword(NEW_IDENTITY_ID, "password");
        }

        @Test
        void createUser_shouldAllowValuesOfDeletedUsers() {
            when(identityProvider.createUser(any())).thenReturn(NEW_IDENTITY_ID);
            UserCreateRequestDTO request = createRequest("new")
                    .taxIdNumber(deleted.getTaxIdNumber())
                    .username(deleted.getUsername())
                    .email(deleted.getEmail());

            UserResponseDTO result = userService.createUser(request);

            assertThat(result.getTaxIdNumber()).isEqualTo(deleted.getTaxIdNumber());
        }

        @Test
        void createUser_shouldThrowUserAlreadyExistsException_whenTaxIdNumberIsTaken() {
            UserCreateRequestDTO request = createRequest("new").taxIdNumber(first.getTaxIdNumber());

            assertAlreadyExists(() -> userService.createUser(request),
                    UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_TAX_ID);
        }

        @Test
        void createUser_shouldThrowUserAlreadyExistsException_whenUsernameIsTaken() {
            UserCreateRequestDTO request = createRequest("new").username(first.getUsername());

            assertAlreadyExists(() -> userService.createUser(request),
                    UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_USERNAME);
        }

        @Test
        void createUser_shouldThrowUserAlreadyExistsException_whenEmailIsTaken() {
            UserCreateRequestDTO request = createRequest("new").email(first.getEmail());

            assertAlreadyExists(() -> userService.createUser(request),
                    UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_EMAIL);
        }

        private UserCreateRequestDTO createRequest(String key) {
            return new UserCreateRequestDTO()
                    .firstname("firstname-" + key)
                    .lastname("lastname-" + key)
                    .gender(GenderDTO.F)
                    .language(LanguageDTO.EN)
                    .mobileNumber("000-" + key)
                    .taxIdNumber("TAX-" + key)
                    .email(key + "@test.com")
                    .username("username-" + key)
                    .password("password")
                    .groupIds(List.of("group-id"))
                    .enabled(true);
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void updateUser_shouldPersistNewDataAndLanguage_whenRequestIsValid() {
            UserUpdateRequestDTO request = updateRequest(first)
                    .firstname("Luigi")
                    .gender(GenderDTO.F)
                    .language(LanguageDTO.EN)
                    .enabled(false);

            userService.updateUser(first.getUuid(), request);
            flushAndClear();

            User updated = userRepository.findByUuidAndDeletedOnIsNull(first.getUuid()).orElseThrow();
            assertThat(updated)
                    .extracting(User::getFirstname, User::getGender, User::getEnabled, User::getIdentityId)
                    .containsExactly("Luigi", Gender.F, false, first.getIdentityId());
            assertThat(updated.getPreferences().getLanguage()).isEqualTo(Language.EN);
        }

        @Test
        void updateUser_shouldUpdateIdentityUser() {
            UserUpdateRequestDTO request = updateRequest(first).username("new.username");

            userService.updateUser(first.getUuid(), request);

            verify(identityProvider).updateUser(first.getIdentityId(), new IdentityUserUpdateRequestDTO(
                    "new.username", first.getEmail(), first.getFirstname(), first.getLastname(), true,
                    List.of("group-id")));
        }

        @Test
        void updateUser_shouldThrowUserAlreadyExistsException_whenTaxIdNumberBelongsToAnotherUser() {
            UserUpdateRequestDTO request = updateRequest(first).taxIdNumber(second.getTaxIdNumber());

            assertAlreadyExists(() -> userService.updateUser(first.getUuid(), request),
                    UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_TAX_ID);
        }

        @Test
        void updateUser_shouldThrowUserAlreadyExistsException_whenUsernameBelongsToAnotherUser() {
            UserUpdateRequestDTO request = updateRequest(first).username(second.getUsername());

            assertAlreadyExists(() -> userService.updateUser(first.getUuid(), request),
                    UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_USERNAME);
        }

        @Test
        void updateUser_shouldThrowUserAlreadyExistsException_whenEmailBelongsToAnotherUser() {
            UserUpdateRequestDTO request = updateRequest(first).email(second.getEmail());

            assertAlreadyExists(() -> userService.updateUser(first.getUuid(), request),
                    UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_EMAIL);
        }

        @Test
        void updateUser_shouldThrowUserNotFoundException_whenUserIsDeleted() {
            UserUpdateRequestDTO request = updateRequest(deleted);

            assertUserNotFound(() -> userService.updateUser(deleted.getUuid(), request), deleted.getUuid());
        }

        private UserUpdateRequestDTO updateRequest(User user) {
            return new UserUpdateRequestDTO()
                    .firstname(user.getFirstname())
                    .lastname(user.getLastname())
                    .gender(GenderDTO.M)
                    .language(LanguageDTO.IT)
                    .mobileNumber(user.getMobileNumber())
                    .taxIdNumber(user.getTaxIdNumber())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .groupIds(List.of("group-id"))
                    .enabled(true);
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void deleteUser_shouldSoftDeleteUserAndDeleteIdentityUser_whenUserIsNotTheCurrentOne() {
            authenticate(CURRENT_IDENTITY_ID);

            userService.deleteUser(first.getUuid());
            flushAndClear();

            User softDeleted = userRepository.findByIdentityId(first.getIdentityId()).orElseThrow();
            assertThat(softDeleted.getDeletedBy()).isEqualTo(CURRENT_IDENTITY_ID);
            assertThat(softDeleted.getDeletedOn()).isNotNull();
            assertThat(userRepository.findByUuidAndDeletedOnIsNull(first.getUuid())).isEmpty();
            verify(identityProvider).deleteUser(first.getIdentityId());
        }

        @Test
        void deleteUser_shouldThrowUserNotFoundException_whenUserIsAlreadyDeleted() {
            assertUserNotFound(() -> userService.deleteUser(deleted.getUuid()), deleted.getUuid());
        }
    }

    @Nested
    class ResetPassword {

        @Test
        void resetPassword_shouldResetIdentityUserPassword_whenUserExists() {
            userService.resetPassword(first.getUuid());

            verify(identityProvider).resetPassword(first.getIdentityId());
        }

        @Test
        void resetPassword_shouldThrowUserNotFoundException_whenUserIsDeleted() {
            assertUserNotFound(() -> userService.resetPassword(deleted.getUuid()), deleted.getUuid());
        }
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private static IdentityUserResponseDTO identityUser(List<IdentityGroupDTO> groups) {
        IdentityUserResponseDTO identityUser = new IdentityUserResponseDTO();
        identityUser.setGroups(groups);
        return identityUser;
    }

    private static void assertUserNotFound(ThrowingCallable call, String uuid) {
        assertThatThrownBy(call)
                .isInstanceOfSatisfying(UserNotFoundException.class, ex -> {
                    assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.NOT_FOUND);
                    assertThat(ex).hasMessage("User not found: " + uuid);
                });
    }

    private static void assertAlreadyExists(ThrowingCallable call, String errorCode) {
        assertThatThrownBy(call)
                .isInstanceOfSatisfying(UserAlreadyExistsException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(errorCode));
    }
}
