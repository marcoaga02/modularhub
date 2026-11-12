package com.marcoaga02.modularhub.modules.usermanagement.domain.service;

import com.marcoaga02.modularhub.core.api.dto.GenderDTO;
import com.marcoaga02.modularhub.ModularhubApplication;
import com.marcoaga02.modularhub.config.BaseITWithMockIdentity;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserResponseDTO;
import com.marcoaga02.modularhub.core.api.exception.BadRequestException;
import com.marcoaga02.modularhub.core.api.exception.ConflictException;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.NotFoundException;
import com.marcoaga02.modularhub.core.api.exception.language.LanguageNotFoundException;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.core.domain.repository.LanguageRepository;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.core.domain.service.AccountService;
import com.marcoaga02.modularhub.core.domain.service.IdentityService;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCreateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserUpdateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserManagementExceptionCodes;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.UserAlreadyExistsException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.UserNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ModularhubApplication.class)
@Transactional
class UserServiceIT extends BaseITWithMockIdentity {

    private static final String TAX_ID_NUMBER = "USR_1";

    @MockitoBean
    private IdentityService identityService;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private EntityManager entityManager;

    private User enabledUser, disabledUser, anotherUser;

    private Language lang1, lang2;

    @BeforeEach
    void setUp() {
        lang1 = createAndSaveLanguage("it-IT", "Italiano", true);
        lang2 = createAndSaveLanguage("en-US", "English", false);

        enabledUser = createAndSaveUser(
                "identity1",
                lang1,
                "firstname1",
                "lastname1",
                Gender.M,
                "0000",
                TAX_ID_NUMBER,
                "enabled@email.com",
                "username1",
                true,
                null
        );

        disabledUser = createAndSaveUser(
                "identity2",
                lang2,
                "firstname2",
                "lastname2",
                Gender.F,
                "1111",
                "USR_2",
                "disabled@email.com",
                "username2",
                false,
                null
        );

        anotherUser = createAndSaveUser(
                "identity3",
                lang2,
                "firstname3",
                "lastname3",
                Gender.F,
                "2222",
                "USR_3",
                "another@email.com",
                "username3",
                true,
                null
        );

        // deleted user
        createAndSaveUser(
                "identity4",
                lang1,
                "firstname4",
                "lastname4",
                Gender.M,
                "3333",
                "USR_4",
                "deleted@email.com",
                "username4",
                true,
                OffsetDateTime.now()
        );

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    class GetAllUsers {

        @Test
        void getAllUsers_shouldReturnAllUsers_whenCriteriaIsNull() {
            Page<UserResponseDTO> result = userService.getAllUsers(
                    null,
                    PageRequest.of(0, 10)
            );

            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getNumber()).isZero();
            assertThat(result.getTotalPages()).isEqualTo(1);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent())
                    .extracting(UserResponseDTO::getId)
                    .containsExactlyInAnyOrder(
                            enabledUser.getUuid(),
                            disabledUser.getUuid(),
                            anotherUser.getUuid()
                    );
        }
        @Test
        void getAllUsers_shouldReturnOnlyOneUser_whenPageSizeIsOne() {
            Page<UserResponseDTO> result = userService.getAllUsers(
                    null,
                    PageRequest.of(0, 1, Sort.by("firstname").ascending())
            );

            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.getNumber()).isZero();
            assertThat(result.getSize()).isEqualTo(1);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent())
                    .extracting(UserResponseDTO::getId)
                    .containsExactly(
                            enabledUser.getUuid()
                    );
        }
        @Test
        void getAllUsers_shouldReturnTwoUsers_whenPageSizeIsTwo() {
            Page<UserResponseDTO> result = userService.getAllUsers(
                    null,
                    PageRequest.of(0, 2, Sort.by("firstname").ascending())
            );

            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isZero();
            assertThat(result.getSize()).isEqualTo(2);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(UserResponseDTO::getId)
                    .containsExactly(
                            enabledUser.getUuid(),
                            disabledUser.getUuid()
                    );
        }
        @Test
        void getAllUsers_shouldReturnCorrectUsers_whenPageNumberRefersToTheSecondPage() {
            Page<UserResponseDTO> result = userService.getAllUsers(
                    null,
                    PageRequest.of(1, 2, Sort.by("firstname").ascending())
            );

            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(2);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent())
                    .extracting(UserResponseDTO::getId)
                    .containsExactly(anotherUser.getUuid());
        }
    }

    @Nested
    class GetUserByUuid {

        @Test
        void getUserByUuid_shouldReturnUser_whenUserExists() {
            when(identityService.getUserById(any()))
                    .thenReturn(new IdentityUserResponseDTO());

            UserResponseDTO result = userService.getUserByUuid(enabledUser.getUuid());

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(enabledUser.getUuid());
        }
        @Test
        void getUserByUuid_shouldThrowNotFoundException_whenUserNotFound() {
            assertThatThrownBy(() -> userService.getUserByUuid("non-existing-uuid"))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(e -> {
                        UserNotFoundException ex = (UserNotFoundException) e;
                        assertThat(ex).isInstanceOf(NotFoundException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.NOT_FOUND);
                        assertThat(ex.getMessage()).isEqualTo("User not found: non-existing-uuid");
                    });
        }
    }

    @Nested
    class CreateUser {

        @Test
        void createUser_shouldCreateAndReturnDTO_whenValid() {
            UserCreateRequestDTO dto = buildUserCreateRequestDTO(lang1.getUuid(), "NEW_TAX_ID");

            UserResponseDTO result = userService.createUser(dto);
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getTaxIdNumber()).isEqualTo("NEW_TAX_ID");
            assertThat(result.getLanguage().getId()).isEqualTo(lang1.getUuid());

            entityManager.flush();
            entityManager.clear();

            User saved = userRepository.findByUuidAndDeletedOnIsNull(result.getId()).orElseThrow();
            assertThat(saved.getPreferences().getLanguage().getUuid()).isEqualTo(lang1.getUuid());
        }
        @Test
        void createUser_shouldThrowLanguageNotFoundException_whenInvalidLanguageId() {
            UserCreateRequestDTO dto = buildUserCreateRequestDTO("NON-EXIST-LANG", "NEW_TAX_ID");

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(LanguageNotFoundException.class)
                    .satisfies(e -> {
                        LanguageNotFoundException ex = (LanguageNotFoundException) e;
                        assertThat(ex).isInstanceOf(BadRequestException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.Model.Language.NOT_FOUND);
                        assertThat(ex.getMessage()).isEqualTo("Language not found: NON-EXIST-LANG");
                    });
        }
        @Test
        void createUser_shouldThrowUserAlreadyExistsException_whenTaxIdNumberAlreadyExists() {
            UserCreateRequestDTO dto = buildUserCreateRequestDTO(lang1.getUuid(), TAX_ID_NUMBER);

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .satisfies(e -> {
                        UserAlreadyExistsException ex = (UserAlreadyExistsException) e;
                        assertThat(ex).isInstanceOf(ConflictException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_TAX_ID);
                        assertThat(ex.getMessage()).isEqualTo("User with the same taxIdNumber already exists");
                    });
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void updateUser_shouldUpdateAndReturnDTO_whenValidRequest() {
            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO(lang2.getUuid(), enabledUser.getTaxIdNumber());

            UserResponseDTO result = userService.updateUser(enabledUser.getUuid(), dto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(enabledUser.getUuid());
            assertThat(result.getFirstname()).isEqualTo("new firstname");
            assertThat(result.getLastname()).isEqualTo("new lastname");
            assertThat(result.getGender()).isEqualTo(GenderDTO.F);
            assertThat(result.getMobileNumber()).isEqualTo("9999");
            assertThat(result.getTaxIdNumber()).isEqualTo(enabledUser.getTaxIdNumber());
            assertThat(result.getEmail()).isEqualTo("new@email.com");
            assertThat(result.getEnabled()).isFalse();
            assertThat(result.getLanguage().getId()).isEqualTo(lang2.getUuid());
        }
        @Test
        void updateUser_shouldUpdateAndReturnDTO_whenTaxIdNumberChangedAndNotExists() {
            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO(lang1.getUuid(), "TAX_ID_NEW");

            UserResponseDTO result = userService.updateUser(enabledUser.getUuid(), dto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(enabledUser.getUuid());
            assertThat(result.getTaxIdNumber()).isEqualTo("TAX_ID_NEW");
        }
        @Test
        void updateUser_shouldThrowNotFoundException_whenUserNotFound() {
            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO(lang1.getUuid(), "TAX_ID_NEW");

            assertThatThrownBy(() -> userService.updateUser("non-existing-uuid", dto))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(e -> {
                        UserNotFoundException ex = (UserNotFoundException) e;
                        assertThat(ex).isInstanceOf(NotFoundException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.NOT_FOUND);
                        assertThat(ex.getMessage()).isEqualTo("User not found: non-existing-uuid");
                    });
        }
        @Test
        void updateUser_shouldThrowLanguageNotFoundException_whenInvalidLanguage() {
            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO("NON-EXIST-LANG", enabledUser.getTaxIdNumber());

            assertThatThrownBy(() -> userService.updateUser(enabledUser.getUuid(), dto))
                    .isInstanceOf(LanguageNotFoundException.class)
                    .satisfies(e -> {
                        LanguageNotFoundException ex = (LanguageNotFoundException) e;
                        assertThat(ex).isInstanceOf(BadRequestException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.Model.Language.NOT_FOUND);
                        assertThat(ex.getMessage()).isEqualTo("Language not found: NON-EXIST-LANG");
                    });
        }
        @Test
        void updateUser_shouldThrowUserAlreadyExistsException_whenTaxIdNumberAlreadyExists() {
            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO(lang1.getUuid(), anotherUser.getTaxIdNumber());

            assertThatThrownBy(() -> userService.updateUser(enabledUser.getUuid(), dto))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .satisfies(e -> {
                        UserAlreadyExistsException ex = (UserAlreadyExistsException) e;
                        assertThat(ex).isInstanceOf(ConflictException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_TAX_ID);
                        assertThat(ex.getMessage()).isEqualTo("User with the same taxIdNumber already exists");
                    });
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void deleteUser_shouldSoftDelete_whenUserExists() {
            when(accountService.getCurrentIdentityId())
                    .thenReturn("current-id");

            User existing = userRepository.findByUuidAndDeletedOnIsNull(enabledUser.getUuid())
                    .orElseThrow();

            assertThat(existing.getDeletedOn()).isNull();

            userService.deleteUser(enabledUser.getUuid());

            entityManager.flush();
            entityManager.clear();

            User deleted = userRepository.findById(enabledUser.getId()).orElseThrow();
            assertThat(deleted.getDeletedBy()).isEqualTo("current-id");
            assertThat(deleted.getDeletedOn()).isNotNull();
        }
        @Test
        void deleteUser_shouldThrowNotFoundException_whenUserNotFound() {
            assertThatThrownBy(() -> userService.deleteUser("non-existing-uuid"))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(e -> {
                        UserNotFoundException ex = (UserNotFoundException) e;
                        assertThat(ex).isInstanceOf(NotFoundException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.NOT_FOUND);
                        assertThat(ex.getMessage()).isEqualTo("User not found: non-existing-uuid");
                    });
        }
    }

    @Nested
    class ResetPassword {

        @Test
        void resetPassword_shouldThrowNotFoundException_whenUserNotFound() {
            assertThatThrownBy(() -> userService.resetPassword("non-existing-uuid"))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(e -> {
                        UserNotFoundException ex = (UserNotFoundException) e;
                        assertThat(ex).isInstanceOf(NotFoundException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.NOT_FOUND);
                        assertThat(ex.getMessage()).isEqualTo("User not found: non-existing-uuid");
                    });
        }
    }

    private User createAndSaveUser(
            String identityId,
            Language language,
            String firstname,
            String lastname,
            Gender gender,
            String mobileNumber,
            String taxIdNumber,
            String email,
            String username,
            Boolean enabled,
            OffsetDateTime deletedOn
    ) {
        AccountPreferences preferences = new AccountPreferences();
        preferences.setLanguage(language);

        User user = new User();
        user.setPreferences(preferences);
        user.setIdentityId(identityId);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setGender(gender);
        user.setMobileNumber(mobileNumber);
        user.setTaxIdNumber(taxIdNumber);
        user.setEmail(email);
        user.setUsername(username);
        user.setEnabled(enabled);
        user.setDeletedOn(deletedOn);

        userRepository.save(user);

        return user;
    }

    private Language createAndSaveLanguage(String code, String label, boolean isDefault) {
        Language language = new Language();
        language.setCode(code);
        language.setLabel(label);
        language.setIsDefault(isDefault);

        languageRepository.save(language);

        return language;
    }

    private UserCreateRequestDTO buildUserCreateRequestDTO(String languageId, String taxIdNumber) {
        UserCreateRequestDTO dto = new UserCreateRequestDTO();
        dto.setFirstname("new firstname");
        dto.setLastname("new lastname");
        dto.setGender(GenderDTO.F);
        dto.setLanguageId(languageId);
        dto.setMobileNumber("9999");
        dto.setTaxIdNumber(taxIdNumber);
        dto.setEmail("new@email.com");
        dto.setUsername("new username");
        dto.setPassword("newpassword");
        dto.setEnabled(false);
        return dto;
    }

    private UserUpdateRequestDTO buildUserUpdateRequestDTO(String languageId, String taxIdNumber) {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
        dto.setFirstname("new firstname");
        dto.setLastname("new lastname");
        dto.setGender(GenderDTO.F);
        dto.setLanguageId(languageId);
        dto.setMobileNumber("9999");
        dto.setTaxIdNumber(taxIdNumber);
        dto.setEmail("new@email.com");
        dto.setUsername("new username");
        dto.setEnabled(false);
        return dto;
    }
}