package com.marcoaga02.modularhub.modules.usermanagement.domain.service;

import com.marcoaga02.modularhub.core.api.dto.GenderDTO;
import com.marcoaga02.modularhub.core.api.dto.*;
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
import com.marcoaga02.modularhub.core.domain.service.UserAccountService;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCriteriaDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCreateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserUpdateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserManagementExceptionCodes;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.UserAlreadyExistsException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.UserNotFoundException;
import com.marcoaga02.modularhub.modules.usermanagement.api.mapper.UserMapper;
import org.apache.logging.log4j.util.Strings;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final OffsetDateTime CREATED_ON = OffsetDateTime.parse("2026-01-01T10:15:30+01:00");
    private static final OffsetDateTime UPDATED_ON = OffsetDateTime.parse("2026-02-01T10:15:30+01:00");

    public static final String TAX_ID_NUMBER_USER_1 = "TAX_ID_01";
    public static final String USERNAME_USER_1 = "username1";
    public static final String EMAIL_USER_1 = "email@mock.it";

    public static final String TAX_ID_NUMBER_USER_2 = "TAX_ID_02";
    public static final String USERNAME_USER_2 = "username2";
    public static final String EMAIL_USER_2 = "vittoria@mock.it";

    @Mock
    private UserRepository userRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private IdentityService identityService;

    @Mock
    private AccountService accountService;

    @Mock
    private UserAccountService userAccountService;

    @InjectMocks
    private UserService userService;

    private User user1, user2;

    private UserResponseDTO userResponseDTO1, userResponseDTO2;

    private Language language;

    private LanguageResponseDTO languageResponseDTO;

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clear();
    }

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();

        language = new Language();
        language.setCode("it-IT");
        language.setLabel("Italiano");
        language.setIsDefault(true);

        languageResponseDTO = new LanguageResponseDTO();
        languageResponseDTO.setId(language.getUuid());
        languageResponseDTO.setCode("it-IT");
        languageResponseDTO.setLabel("Italiano");
        languageResponseDTO.setIsDefault(true);

        Language previousLanguage = new Language();
        previousLanguage.setCode("en-US");

        AccountPreferences preferences1 = new AccountPreferences();
        preferences1.setLanguage(previousLanguage);

        user1 = new User();
        user1.setPreferences(preferences1);
        user1.setFirstname("Mario");
        user1.setLastname("Rossi");
        user1.setGender(Gender.M);
        user1.setMobileNumber("0000");
        user1.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
        user1.setEmail(EMAIL_USER_1);
        user1.setUsername(USERNAME_USER_1);
        user1.setEnabled(true);
        user1.setCreatedOn(CREATED_ON);
        user1.setUpdatedOn(UPDATED_ON);
        user1.setCreatedBy("Vittoria Bianchi");
        user1.setUpdatedBy("Mario Rossi");

        userResponseDTO1 = new UserResponseDTO();
        userResponseDTO1.setId(user1.getUuid());
        userResponseDTO1.setFirstname("Mario");
        userResponseDTO1.setLastname("Rossi");
        userResponseDTO1.setGender(GenderDTO.M);
        userResponseDTO1.setLanguage(languageResponseDTO);
        userResponseDTO1.setMobileNumber("0000");
        userResponseDTO1.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
        userResponseDTO1.setEmail("mario@mock.it");
        userResponseDTO1.setUsername(USERNAME_USER_1);
        userResponseDTO1.setEnabled(true);

        user2 = new User();
        user2.setFirstname("Vittoria");
        user2.setLastname("Bianchi");
        user2.setGender(Gender.F);
        user2.setMobileNumber("1111");
        user2.setTaxIdNumber(TAX_ID_NUMBER_USER_2);
        user2.setEmail(EMAIL_USER_2);
        user2.setUsername(USERNAME_USER_2);
        user2.setEnabled(false);

        userResponseDTO2 = new UserResponseDTO();
        userResponseDTO2.setId(user1.getUuid());
        userResponseDTO2.setFirstname("Vittoria");
        userResponseDTO2.setLastname("Bianci");
        userResponseDTO2.setGender(GenderDTO.F);
        userResponseDTO2.setLanguage(languageResponseDTO);
        userResponseDTO2.setMobileNumber("1111");
        userResponseDTO2.setTaxIdNumber(TAX_ID_NUMBER_USER_2);
        userResponseDTO2.setEmail(EMAIL_USER_2);
        userResponseDTO2.setUsername(USERNAME_USER_2);
        userResponseDTO2.setEnabled(false);
    }

    @Nested
    class GetAllUsers {

        @Test
        void getAllUsers_shouldReturnEmptyPage_whenNoUsersMatch() {
            UserCriteriaDTO criteria = new UserCriteriaDTO();
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = Page.empty(pageable);

            when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable)))
                    .thenReturn(emptyPage);

            Page<UserResponseDTO> result = userService.getAllUsers(criteria, pageable);

            assertThat(result).isEmpty();

            verify(userRepository).findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable));
            verify(userMapper, never()).toDto(any());
        }
        @Test
        void getAllUsers_shouldReturnMappedPage_whenSingleUser() {
            UserCriteriaDTO criteria = new UserCriteriaDTO();
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(user1), pageable, 1);

            when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable)))
                    .thenReturn(userPage);
            when(userMapper.toDto(user1))
                    .thenReturn(userResponseDTO1);

            Page<UserResponseDTO> result = userService.getAllUsers(criteria, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).containsExactly(userResponseDTO1);

            verify(userRepository).findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable));
            verify(userMapper).toDto(user1);
        }
        @Test
        void getAllUsers_shouldReturnMappedPage_whenMultipleUsers() {
            UserCriteriaDTO criteria = new UserCriteriaDTO();
            Pageable pageable = PageRequest.of(0, 10);

            Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, 2);

            when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable)))
                    .thenReturn(userPage);
            when(userMapper.toDto(user1)).thenReturn(userResponseDTO1);
            when(userMapper.toDto(user2)).thenReturn(userResponseDTO2);

            Page<UserResponseDTO> result = userService.getAllUsers(criteria, pageable);

            assertThat(result.getContent()).containsExactly(userResponseDTO1, userResponseDTO2);

            verify(userRepository).findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable));
            verify(userMapper).toDto(user1);
            verify(userMapper).toDto(user2);
        }
    }

    @Nested
    class GetUserByUuid {

        @Test
        void getUserByUuid_shouldReturnDTO_whenUserExists() {
            final String userUuid = "abc-123";
            final String identityId1 = "identityId1";
            final String identityId2 = "identityId2";

            user1.setIdentityId(identityId1);
            user1.setCreatedBy(identityId2);
            user1.setUpdatedBy(identityId1);

            IdentityGroupDTO groupDTO = new IdentityGroupDTO();
            groupDTO.setId("group-id");
            groupDTO.setName("First Group");
            groupDTO.setDescription("First Group Description");

            IdentityUserResponseDTO userResponseDTO = new IdentityUserResponseDTO();
            userResponseDTO.setId(identityId1);
            userResponseDTO.setUsername("username");
            userResponseDTO.setEmail("email");
            userResponseDTO.setFirstName("firstName");
            userResponseDTO.setLastName("lastName");
            userResponseDTO.setEnabled(true);
            userResponseDTO.setGroups(List.of(groupDTO));

            when(userRepository.findByUuidAndDeletedOnIsNull(userUuid))
                    .thenReturn(Optional.of(user1));
            when(userAccountService.resolveFullName(identityId2))
                    .thenReturn("Vittoria Bianchi");
            when(userAccountService.resolveFullName(identityId1))
                    .thenReturn("Mario Rossi");
            when(userMapper.toDto(user1))
                    .thenReturn(userResponseDTO1);
            when(identityService.getUserById(identityId1))
                    .thenReturn(userResponseDTO);

            UserResponseDTO result = userService.getUserByUuid(userUuid);

            assertThat(result).isNotNull().isEqualTo(userResponseDTO1);
            assertThat(result.getAudit()).isNotNull();
            assertThat(result.getAudit().getCreatedBy()).isEqualTo("Vittoria Bianchi");
            assertThat(result.getAudit().getUpdatedBy()).isEqualTo("Mario Rossi");
            assertThat(result.getAudit().getCreatedOn()).isEqualTo(CREATED_ON);
            assertThat(result.getAudit().getUpdatedOn()).isEqualTo(UPDATED_ON);

            assertThat(result.getGroups()).hasSize(1).containsExactly(groupDTO);
            assertThat(result.getLanguage()).isEqualTo(languageResponseDTO);

            verify(userRepository).findByUuidAndDeletedOnIsNull(userUuid);
            verify(userAccountService).resolveFullName(identityId2);
            verify(userAccountService).resolveFullName(identityId1);
            verify(identityService).getUserById(identityId1);
            verify(userMapper).toDto(user1);
        }
        @Test
        void getUserByUuid_shouldReturnDTO_whenUserExistsAndUnresolvedFullName() {
            final String userUuid = "abc-123";
            final String identityId1 = "identityId1";
            final String identityId2 = "identityId2";

            user1.setIdentityId(identityId1);
            user1.setCreatedBy(identityId2);
            user1.setUpdatedBy(null);

            IdentityGroupDTO groupDTO = new IdentityGroupDTO();
            groupDTO.setId("group-id");
            groupDTO.setName("First Group");
            groupDTO.setDescription("First Group Description");

            IdentityUserResponseDTO userResponseDTO = new IdentityUserResponseDTO();
            userResponseDTO.setId(identityId1);
            userResponseDTO.setUsername("username");
            userResponseDTO.setEmail("email");
            userResponseDTO.setFirstName("firstName");
            userResponseDTO.setLastName("lastName");
            userResponseDTO.setEnabled(true);
            userResponseDTO.setGroups(List.of(groupDTO));

            when(userRepository.findByUuidAndDeletedOnIsNull(userUuid))
                    .thenReturn(Optional.of(user1));
            when(userAccountService.resolveFullName(identityId2))
                    .thenReturn("");
            when(userAccountService.resolveFullName(null))
                    .thenReturn("");
            when(userMapper.toDto(user1))
                    .thenReturn(userResponseDTO1);
            when(identityService.getUserById(identityId1))
                    .thenReturn(userResponseDTO);

            UserResponseDTO result = userService.getUserByUuid(userUuid);

            assertThat(result).isNotNull().isEqualTo(userResponseDTO1);
            assertThat(result.getAudit()).isNotNull();
            assertThat(result.getAudit().getCreatedBy()).isEqualTo(Strings.EMPTY);
            assertThat(result.getAudit().getUpdatedBy()).isEqualTo(Strings.EMPTY);
            assertThat(result.getAudit().getCreatedOn()).isEqualTo(CREATED_ON);
            assertThat(result.getAudit().getUpdatedOn()).isEqualTo(UPDATED_ON);

            assertThat(result.getGroups()).hasSize(1).containsExactly(groupDTO);
            assertThat(result.getLanguage()).isEqualTo(languageResponseDTO);

            verify(userRepository).findByUuidAndDeletedOnIsNull(userUuid);
            verify(userAccountService).resolveFullName(identityId2);
            verify(userAccountService).resolveFullName(null);
            verify(identityService).getUserById(identityId1);
            verify(userMapper).toDto(user1);
        }
        @Test
        void getUserByUuid_shouldReturnDTO_whenUserExistsWithNullGroups() {
            final String userUuid = "abc-123";
            final String identityId1 = "identityId1";
            final String identityId2 = "identityId2";

            user1.setIdentityId(identityId1);
            user1.setCreatedBy(identityId2);
            user1.setUpdatedBy(identityId1);

            IdentityGroupDTO groupDTO = new IdentityGroupDTO();
            groupDTO.setId("group-id");
            groupDTO.setName("First Group");
            groupDTO.setDescription("First Group Description");

            IdentityUserResponseDTO userResponseDTO = new IdentityUserResponseDTO();
            userResponseDTO.setId(identityId1);
            userResponseDTO.setUsername("username");
            userResponseDTO.setEmail("email");
            userResponseDTO.setFirstName("firstName");
            userResponseDTO.setLastName("lastName");
            userResponseDTO.setEnabled(true);
            userResponseDTO.setGroups(null);

            when(userRepository.findByUuidAndDeletedOnIsNull(userUuid))
                    .thenReturn(Optional.of(user1));
            when(userAccountService.resolveFullName(identityId2))
                    .thenReturn("Vittoria Bianchi");
            when(userAccountService.resolveFullName(identityId1))
                    .thenReturn("Mario Rossi");
            when(userMapper.toDto(user1))
                    .thenReturn(userResponseDTO1);
            when(identityService.getUserById(identityId1))
                    .thenReturn(userResponseDTO);

            UserResponseDTO result = userService.getUserByUuid(userUuid);

            assertThat(result).isNotNull().isEqualTo(userResponseDTO1);
            assertThat(result.getAudit()).isNotNull();
            assertThat(result.getAudit().getCreatedBy()).isEqualTo("Vittoria Bianchi");
            assertThat(result.getAudit().getUpdatedBy()).isEqualTo("Mario Rossi");
            assertThat(result.getAudit().getCreatedOn()).isEqualTo(CREATED_ON);
            assertThat(result.getAudit().getUpdatedOn()).isEqualTo(UPDATED_ON);

            assertThat(result.getGroups()).isEmpty();

            verify(userRepository).findByUuidAndDeletedOnIsNull(userUuid);
            verify(userAccountService).resolveFullName(identityId2);
            verify(userAccountService).resolveFullName(identityId1);
            verify(identityService).getUserById(identityId1);
            verify(userMapper).toDto(user1);
        }
        @Test
        void getUserByUuid_shouldThrowNotFoundException_whenUserNotFound() {
            final String uuid = "not-existing";
            when(userRepository.findByUuidAndDeletedOnIsNull(uuid))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserByUuid(uuid))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(e -> {
                        UserNotFoundException ex = (UserNotFoundException) e;
                        assertThat(ex).isInstanceOf(NotFoundException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.NOT_FOUND);
                        assertThat(ex.getMessage()).isEqualTo("User not found: not-existing");
                    });

            verify(userRepository).findByUuidAndDeletedOnIsNull(uuid);
            verify(userAccountService, never()).resolveFullName(any());
            verify(userMapper, never()).toDto(any());
        }
    }

    @Nested
    class CreateUser {

        @Test
        void createUser_shouldCreateAndReturnDTO_whenTaxIdNumberNotExists() {
            final String identityId = "identityId";

            UserCreateRequestDTO dto = buildUserCreateRequestDTO();

            when(languageRepository.findByUuid("LANG_ID_01"))
                    .thenReturn(Optional.of(language));
            when(userRepository.findByTaxIdNumberAndDeletedOnIsNull("TAX_ID_NEW"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByUsernameAndDeletedOnIsNull("new username"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDeletedOnIsNull("new@email.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.saveAndFlush(any(User.class)))
                    .thenReturn(user1);
            when(userMapper.toDto(user1))
                    .thenReturn(userResponseDTO1);
            when(identityService.createUser(any(IdentityUserCreateRequestDTO.class)))
                    .thenReturn(identityId);

            UserResponseDTO result = userService.createUser(dto);

            assertThat(result).isNotNull().isEqualTo(userResponseDTO1);

            verify(languageRepository).findByUuid("LANG_ID_01");
            verify(userRepository).findByTaxIdNumberAndDeletedOnIsNull("TAX_ID_NEW");
            verify(userRepository).findByUsernameAndDeletedOnIsNull("new username");
            verify(userRepository).findByEmailAndDeletedOnIsNull("new@email.com");

            ArgumentCaptor<IdentityUserCreateRequestDTO> identityCaptor =
                    ArgumentCaptor.forClass(IdentityUserCreateRequestDTO.class);

            verify(identityService).createUser(identityCaptor.capture());

            IdentityUserCreateRequestDTO captured = identityCaptor.getValue();

            assertThat(captured.getUsername()).isEqualTo("new username");
            assertThat(captured.getEmail()).isEqualTo("new@email.com");
            assertThat(captured.getFirstName()).isEqualTo("new firstname");
            assertThat(captured.getLastName()).isEqualTo("new lastname");
            assertThat(captured.getEnabled()).isFalse();
            assertThat(captured.getPassword()).isEqualTo("password");
            assertThat(captured.getGroupIds()).containsExactly("group-id");
            assertThat(captured.getEmailVerified()).isTrue();

            verify(userMapper).toDto(user1);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).saveAndFlush(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getIdentityId()).isEqualTo(identityId);
            assertThat(savedUser.getPreferences()).isNotNull();
            assertThat(savedUser.getPreferences().getLanguage()).isSameAs(language);

            InOrder inOrder = inOrder(identityService, userRepository);
            inOrder.verify(identityService).createUser(any(IdentityUserCreateRequestDTO.class));
            inOrder.verify(userRepository).saveAndFlush(any(User.class));

            assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(1);
        }
        @Test
        void createUser_shouldRollbackIdentityUser_whenTransactionRollsBack() {
            final String identityId = "identityId";

            mockSuccessfulCreation(identityId);

            userService.createUser(buildUserCreateRequestDTO());

            completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);

            verify(identityService).rollbackCreatedUser(identityId);
        }
        @Test
        void createUser_shouldNotRollbackIdentityUser_whenTransactionCommits() {
            final String identityId = "identityId";

            mockSuccessfulCreation(identityId);

            userService.createUser(buildUserCreateRequestDTO());

            completeTransaction(TransactionSynchronization.STATUS_COMMITTED);

            verify(identityService, never()).rollbackCreatedUser(any());
        }
        @Test
        void createUser_shouldPropagateException_whenSaveFailsAfterIdentityCreation() {
            final String identityId = "identityId";
            final RuntimeException dbError = new RuntimeException("db error");

            when(languageRepository.findByUuid("LANG_ID_01"))
                    .thenReturn(Optional.of(language));
            when(identityService.createUser(any(IdentityUserCreateRequestDTO.class)))
                    .thenReturn(identityId);
            when(userRepository.saveAndFlush(any(User.class)))
                    .thenThrow(dbError);

            UserCreateRequestDTO dto = buildUserCreateRequestDTO();

            assertThatThrownBy(() -> userService.createUser(dto)).isSameAs(dbError);

            assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(1);
            verify(userMapper, never()).toDto(any());
        }
        @Test
        void createUser_shouldThrowUserAlreadyExistsException_whenTaxIdNumberAlreadyExists() {
            UserCreateRequestDTO dto = new UserCreateRequestDTO();
            dto.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
            dto.setLanguageId("LANG_ID_01");

            when(languageRepository.findByUuid("LANG_ID_01"))
                    .thenReturn(Optional.of(language));
            when(userRepository.findByTaxIdNumberAndDeletedOnIsNull(TAX_ID_NUMBER_USER_1))
                    .thenReturn(Optional.of(user1));

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .satisfies(e -> {
                        UserAlreadyExistsException ex = (UserAlreadyExistsException) e;
                        assertThat(ex).isInstanceOf(ConflictException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_TAX_ID);
                        assertThat(ex.getMessage()).isEqualTo("User with the same taxIdNumber already exists");
                    });

            verify(languageRepository).findByUuid("LANG_ID_01");
            verify(userRepository).findByTaxIdNumberAndDeletedOnIsNull(TAX_ID_NUMBER_USER_1);
            verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
            verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
            verify(identityService, never()).createUser(any(IdentityUserCreateRequestDTO.class));
            verify(userRepository, never()).saveAndFlush(any());
            verify(userMapper, never()).toDto(any());
            assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();
        }
        @Test
        void createUser_shouldThrowUserAlreadyExistsException_whenUsernameAlreadyExists() {
            UserCreateRequestDTO dto = new UserCreateRequestDTO();
            dto.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
            dto.setUsername("existing.username");
            dto.setLanguageId("LANG_ID_01");

            when(languageRepository.findByUuid("LANG_ID_01"))
                    .thenReturn(Optional.of(language));
            when(userRepository.findByTaxIdNumberAndDeletedOnIsNull(TAX_ID_NUMBER_USER_1))
                    .thenReturn(Optional.empty());
            when(userRepository.findByUsernameAndDeletedOnIsNull("existing.username"))
                    .thenReturn(Optional.of(user1));

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .satisfies(e -> {
                        UserAlreadyExistsException ex = (UserAlreadyExistsException) e;
                        assertThat(ex).isInstanceOf(ConflictException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_USERNAME);
                        assertThat(ex.getMessage()).isEqualTo("User with the same username already exists");
                    });

            verify(languageRepository).findByUuid("LANG_ID_01");
            verify(userRepository).findByTaxIdNumberAndDeletedOnIsNull(TAX_ID_NUMBER_USER_1);
            verify(userRepository).findByUsernameAndDeletedOnIsNull("existing.username");
            verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
            verify(identityService, never()).createUser(any(IdentityUserCreateRequestDTO.class));
            verify(userRepository, never()).saveAndFlush(any());
            verify(userMapper, never()).toDto(any());
            assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();
        }
        @Test
        void createUser_shouldThrowUserAlreadyExistsException_whenEmailAlreadyExists() {
            UserCreateRequestDTO dto = new UserCreateRequestDTO();
            dto.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
            dto.setUsername("new.username");
            dto.setEmail("existing@email.it");
            dto.setLanguageId("LANG_ID_01");

            when(languageRepository.findByUuid("LANG_ID_01"))
                    .thenReturn(Optional.of(language));
            when(userRepository.findByTaxIdNumberAndDeletedOnIsNull(TAX_ID_NUMBER_USER_1))
                    .thenReturn(Optional.empty());
            when(userRepository.findByUsernameAndDeletedOnIsNull("new.username"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailAndDeletedOnIsNull("existing@email.it"))
                    .thenReturn(Optional.of(user1));

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .satisfies(e -> {
                        UserAlreadyExistsException ex = (UserAlreadyExistsException) e;
                        assertThat(ex).isInstanceOf(ConflictException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_EMAIL);
                        assertThat(ex.getMessage()).isEqualTo("User with the same email already exists");
                    });

            verify(languageRepository).findByUuid("LANG_ID_01");
            verify(userRepository).findByTaxIdNumberAndDeletedOnIsNull(TAX_ID_NUMBER_USER_1);
            verify(userRepository).findByUsernameAndDeletedOnIsNull("new.username");
            verify(userRepository).findByEmailAndDeletedOnIsNull("existing@email.it");
            verify(identityService, never()).createUser(any(IdentityUserCreateRequestDTO.class));
            verify(userRepository, never()).saveAndFlush(any());
            verify(userMapper, never()).toDto(any());
            assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();
        }
        @Test
        void createUser_shouldThrowLanguageNotFoundException_whenInvalidLanguageId() {
            final String languageId = "INVALID-LANG";

            UserCreateRequestDTO dto = new UserCreateRequestDTO();
            dto.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
            dto.setLanguageId(languageId);

            when(languageRepository.findByUuid(languageId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(LanguageNotFoundException.class)
                    .satisfies(e -> {
                        LanguageNotFoundException ex = (LanguageNotFoundException) e;
                        assertThat(ex).isInstanceOf(BadRequestException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.Model.Language.NOT_FOUND);
                        assertThat(ex.getMessage()).isEqualTo("Language not found: INVALID-LANG");
                    });

            verify(languageRepository).findByUuid(languageId);
            verify(userRepository, never()).findByTaxIdNumberAndDeletedOnIsNull(any());
            verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
            verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
            verify(identityService, never()).createUser(any(IdentityUserCreateRequestDTO.class));
            verify(userRepository, never()).saveAndFlush(any());
            verify(userMapper, never()).toDto(any());
            assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void updateUser_shouldUpdateAndReturnDTO_whenValid() {
            final String identityId = "identityId";
            user1.setIdentityId(identityId);

            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO();
            dto.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
            dto.setUsername(USERNAME_USER_1);
            dto.setEmail(EMAIL_USER_1);

            when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                    .thenReturn(Optional.of(user1));
            when(languageRepository.findByUuid("LANG_ID_01"))
                    .thenReturn(Optional.of(language));
            when(userMapper.toDto(user1))
                    .thenReturn(userResponseDTO1);

            UserResponseDTO result = userService.updateUser(user1.getUuid(), dto);

            assertThat(result).isNotNull().isEqualTo(userResponseDTO1);

            verify(userRepository).findByUuidAndDeletedOnIsNull(user1.getUuid());
            verify(languageRepository).findByUuid("LANG_ID_01");
            verify(userRepository, never()).findByTaxIdNumberAndDeletedOnIsNull(any());
            verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
            verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
            verify(userMapper).updateEntity(dto, user1);

            ArgumentCaptor<IdentityUserUpdateRequestDTO> captor =
                    ArgumentCaptor.forClass(IdentityUserUpdateRequestDTO.class);

            verify(identityService).updateUser(eq(identityId), captor.capture());

            IdentityUserUpdateRequestDTO captured = captor.getValue();

            assertThat(captured.getUsername()).isEqualTo(USERNAME_USER_1);
            assertThat(captured.getEmail()).isEqualTo(EMAIL_USER_1);
            assertThat(captured.getFirstName()).isEqualTo("new firstname");
            assertThat(captured.getLastName()).isEqualTo("new lastname");
            assertThat(captured.getEnabled()).isFalse();
            assertThat(captured.getGroupIds()).containsExactly("group-id");

            assertThat(user1.getPreferences().getLanguage()).isSameAs(language);

            InOrder inOrder = inOrder(userRepository, identityService);
            inOrder.verify(userRepository).saveAndFlush(user1);
            inOrder.verify(identityService).updateUser(eq(identityId), any(IdentityUserUpdateRequestDTO.class));

            verify(userMapper).toDto(user1);
        }
        @Test
        void updateUser_shouldThrowNotFoundException_whenUserNotFound() {
            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO();

            final String uuid = "not-existing";
            when(userRepository.findByUuidAndDeletedOnIsNull(uuid))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(uuid, dto))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(e -> {
                        UserNotFoundException ex = (UserNotFoundException) e;
                        assertThat(ex).isInstanceOf(NotFoundException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.NOT_FOUND);
                        assertThat(ex.getMessage()).isEqualTo("User not found: not-existing");
                    });

            verify(userRepository).findByUuidAndDeletedOnIsNull(uuid);
            verify(languageRepository, never()).findByUuid(any());
            verify(userRepository, never()).findByTaxIdNumberAndDeletedOnIsNull(any());
            verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
            verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
            verify(userMapper, never()).updateEntity(any(UserUpdateRequestDTO.class), any());
            verify(identityService, never()).updateUser(any(), any());
            verify(userMapper, never()).toDto(any());
        }
        @Test
        void updateUser_shouldThrowLanguageNotFoundException_whenInvalidLanguage() {
            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO();
            dto.setLanguageId("INVALID-LANG");

            when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                    .thenReturn(Optional.of(user1));
            when(languageRepository.findByUuid("INVALID-LANG"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(user1.getUuid(), dto))
                    .isInstanceOf(LanguageNotFoundException.class)
                    .satisfies(e -> {
                        LanguageNotFoundException ex = (LanguageNotFoundException) e;
                        assertThat(ex).isInstanceOf(BadRequestException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.Model.Language.NOT_FOUND);
                        assertThat(ex.getMessage()).isEqualTo("Language not found: INVALID-LANG");
                    });

            verify(userRepository).findByUuidAndDeletedOnIsNull(user1.getUuid());
            verify(languageRepository).findByUuid("INVALID-LANG");
            verify(userRepository, never()).findByTaxIdNumberAndDeletedOnIsNull(any());
            verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
            verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
            verify(userMapper, never()).updateEntity(any(UserUpdateRequestDTO.class), any());
            verify(identityService, never()).updateUser(any(), any());
            verify(userMapper, never()).toDto(any());
        }
        @Test
        void updateUser_shouldThrowUserAlreadyExistsException_whenTaxIdNumberAlreadyExists() {
            final String taxIdNumber = TAX_ID_NUMBER_USER_2;

            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO();
            dto.setTaxIdNumber(taxIdNumber);
            dto.setUsername(USERNAME_USER_1);
            dto.setEmail(EMAIL_USER_1);

            when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                    .thenReturn(Optional.of(user1));
            when(languageRepository.findByUuid("LANG_ID_01"))
                    .thenReturn(Optional.of(language));
            when(userRepository.findByTaxIdNumberAndDeletedOnIsNull(taxIdNumber))
                    .thenReturn(Optional.of(user2));

            assertThatThrownBy(() -> userService.updateUser(user1.getUuid(), dto))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .satisfies(e -> {
                        UserAlreadyExistsException ex = (UserAlreadyExistsException) e;
                        assertThat(ex).isInstanceOf(ConflictException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_TAX_ID);
                        assertThat(ex.getMessage()).isEqualTo("User with the same taxIdNumber already exists");
                    });

            verify(userRepository).findByUuidAndDeletedOnIsNull(user1.getUuid());
            verify(languageRepository).findByUuid("LANG_ID_01");
            verify(userRepository).findByTaxIdNumberAndDeletedOnIsNull(taxIdNumber);
            verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
            verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
            verify(userMapper, never()).updateEntity(any(UserUpdateRequestDTO.class), any());
            verify(identityService, never()).updateUser(any(), any());
            verify(userMapper, never()).toDto(any());
        }
        @Test
        void updateUser_shouldUpdateAndReturnDTO_whenTaxIdNumberChangedAndNotExists() {
            final String identityId = "identityId";
            user1.setIdentityId(identityId);

            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO();
            dto.setUsername(USERNAME_USER_1);
            dto.setEmail(EMAIL_USER_1);

            when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                    .thenReturn(Optional.of(user1));
            when(languageRepository.findByUuid("LANG_ID_01"))
                    .thenReturn(Optional.of(language));
            when(userRepository.findByTaxIdNumberAndDeletedOnIsNull("TAX_ID_NEW"))
                    .thenReturn(Optional.empty());
            when(userMapper.toDto(user1))
                    .thenReturn(userResponseDTO1);

            UserResponseDTO result = userService.updateUser(user1.getUuid(), dto);

            assertThat(result).isNotNull().isEqualTo(userResponseDTO1);

            verify(userRepository).findByUuidAndDeletedOnIsNull(user1.getUuid());
            verify(languageRepository).findByUuid("LANG_ID_01");
            verify(userRepository).findByTaxIdNumberAndDeletedOnIsNull("TAX_ID_NEW");
            verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
            verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
            verify(userMapper).updateEntity(dto, user1);

            ArgumentCaptor<IdentityUserUpdateRequestDTO> captor =
                    ArgumentCaptor.forClass(IdentityUserUpdateRequestDTO.class);

            verify(identityService).updateUser(eq(identityId), captor.capture());

            IdentityUserUpdateRequestDTO captured = captor.getValue();

            assertThat(captured.getUsername()).isEqualTo(USERNAME_USER_1);
            assertThat(captured.getEmail()).isEqualTo(EMAIL_USER_1);
            assertThat(captured.getFirstName()).isEqualTo("new firstname");
            assertThat(captured.getLastName()).isEqualTo("new lastname");
            assertThat(captured.getEnabled()).isFalse();
            assertThat(captured.getGroupIds()).containsExactly("group-id");

            assertThat(user1.getPreferences().getLanguage()).isSameAs(language);

            InOrder inOrder = inOrder(userRepository, identityService);
            inOrder.verify(userRepository).saveAndFlush(user1);
            inOrder.verify(identityService).updateUser(eq(identityId), any(IdentityUserUpdateRequestDTO.class));

            verify(userMapper).toDto(user1);
        }
        @Test
        void updateUser_shouldThrowUserAlreadyExistsException_whenUsernameAlreadyExists() {
            final String username = USERNAME_USER_2;

            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO();
            dto.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
            dto.setUsername(username);
            dto.setEmail(EMAIL_USER_1);

            when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                    .thenReturn(Optional.of(user1));
            when(languageRepository.findByUuid("LANG_ID_01"))
                    .thenReturn(Optional.of(language));
            when(userRepository.findByUsernameAndDeletedOnIsNull(username))
                    .thenReturn(Optional.of(user2));

            assertThatThrownBy(() -> userService.updateUser(user1.getUuid(), dto))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .satisfies(e -> {
                        UserAlreadyExistsException ex = (UserAlreadyExistsException) e;
                        assertThat(ex).isInstanceOf(ConflictException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_USERNAME);
                        assertThat(ex.getMessage()).isEqualTo("User with the same username already exists");
                    });

            verify(userRepository).findByUuidAndDeletedOnIsNull(user1.getUuid());
            verify(languageRepository).findByUuid("LANG_ID_01");
            verify(userRepository, never()).findByTaxIdNumberAndDeletedOnIsNull(anyString());
            verify(userRepository).findByUsernameAndDeletedOnIsNull(username);
            verify(userRepository, never()).findByEmailAndDeletedOnIsNull(any());
            verify(userMapper, never()).updateEntity(any(UserUpdateRequestDTO.class), any());
            verify(identityService, never()).updateUser(any(), any());
            verify(userMapper, never()).toDto(any());
        }
        @Test
        void updateUser_shouldUpdateAndReturnDTO_whenUsernameChangedAndNotExists() {
            final String identityId = "identityId";
            user1.setIdentityId(identityId);

            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO();
            dto.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
            dto.setEmail(EMAIL_USER_1);

            when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                    .thenReturn(Optional.of(user1));
            when(languageRepository.findByUuid("LANG_ID_01"))
                    .thenReturn(Optional.of(language));
            when(userRepository.findByUsernameAndDeletedOnIsNull("new username"))
                    .thenReturn(Optional.empty());
            when(userMapper.toDto(user1))
                    .thenReturn(userResponseDTO1);

            UserResponseDTO result = userService.updateUser(user1.getUuid(), dto);

            assertThat(result).isNotNull().isEqualTo(userResponseDTO1);

            verify(userRepository).findByUuidAndDeletedOnIsNull(user1.getUuid());
            verify(languageRepository).findByUuid("LANG_ID_01");
            verify(userRepository, never()).findByTaxIdNumberAndDeletedOnIsNull(anyString());
            verify(userRepository).findByUsernameAndDeletedOnIsNull("new username");
            verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
            verify(userMapper).updateEntity(dto, user1);

            ArgumentCaptor<IdentityUserUpdateRequestDTO> captor =
                    ArgumentCaptor.forClass(IdentityUserUpdateRequestDTO.class);

            verify(identityService).updateUser(eq(identityId), captor.capture());

            IdentityUserUpdateRequestDTO captured = captor.getValue();

            assertThat(captured.getUsername()).isEqualTo("new username");
            assertThat(captured.getEmail()).isEqualTo(EMAIL_USER_1);
            assertThat(captured.getFirstName()).isEqualTo("new firstname");
            assertThat(captured.getLastName()).isEqualTo("new lastname");
            assertThat(captured.getEnabled()).isFalse();
            assertThat(captured.getGroupIds()).containsExactly("group-id");

            assertThat(user1.getPreferences().getLanguage()).isSameAs(language);

            InOrder inOrder = inOrder(userRepository, identityService);
            inOrder.verify(userRepository).saveAndFlush(user1);
            inOrder.verify(identityService).updateUser(eq(identityId), any(IdentityUserUpdateRequestDTO.class));

            verify(userMapper).toDto(user1);
        }
        @Test
        void updateUser_shouldThrowUserAlreadyExistsException_whenEmailAlreadyExists() {
            final String email = EMAIL_USER_2;

            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO();
            dto.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
            dto.setUsername(USERNAME_USER_1);
            dto.setEmail(email);

            when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                    .thenReturn(Optional.of(user1));
            when(languageRepository.findByUuid("LANG_ID_01"))
                    .thenReturn(Optional.of(language));
            when(userRepository.findByEmailAndDeletedOnIsNull(email))
                    .thenReturn(Optional.of(user2));

            assertThatThrownBy(() -> userService.updateUser(user1.getUuid(), dto))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .satisfies(e -> {
                        UserAlreadyExistsException ex = (UserAlreadyExistsException) e;
                        assertThat(ex).isInstanceOf(ConflictException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_EMAIL);
                        assertThat(ex.getMessage()).isEqualTo("User with the same email already exists");
                    });

            verify(userRepository).findByUuidAndDeletedOnIsNull(user1.getUuid());
            verify(languageRepository).findByUuid("LANG_ID_01");
            verify(userRepository, never()).findByTaxIdNumberAndDeletedOnIsNull(anyString());
            verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
            verify(userRepository).findByEmailAndDeletedOnIsNull(email);
            verify(userMapper, never()).updateEntity(any(UserUpdateRequestDTO.class), any());
            verify(identityService, never()).updateUser(any(), any());
            verify(userMapper, never()).toDto(any());
        }
        @Test
        void updateUser_shouldUpdateAndReturnDTO_whenEmailChangedAndNotExists() {
            final String identityId = "identityId";
            user1.setIdentityId(identityId);

            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO();
            dto.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
            dto.setUsername(USERNAME_USER_1);

            when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                    .thenReturn(Optional.of(user1));
            when(languageRepository.findByUuid("LANG_ID_01"))
                    .thenReturn(Optional.of(language));
            when(userRepository.findByEmailAndDeletedOnIsNull("new@email.com"))
                    .thenReturn(Optional.empty());
            when(userMapper.toDto(user1))
                    .thenReturn(userResponseDTO1);

            UserResponseDTO result = userService.updateUser(user1.getUuid(), dto);

            assertThat(result).isNotNull().isEqualTo(userResponseDTO1);

            verify(userRepository).findByUuidAndDeletedOnIsNull(user1.getUuid());
            verify(languageRepository).findByUuid("LANG_ID_01");
            verify(userRepository, never()).findByTaxIdNumberAndDeletedOnIsNull(anyString());
            verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
            verify(userRepository).findByEmailAndDeletedOnIsNull("new@email.com");
            verify(userMapper).updateEntity(dto, user1);

            ArgumentCaptor<IdentityUserUpdateRequestDTO> captor =
                    ArgumentCaptor.forClass(IdentityUserUpdateRequestDTO.class);

            verify(identityService).updateUser(eq(identityId), captor.capture());

            IdentityUserUpdateRequestDTO captured = captor.getValue();

            assertThat(captured.getUsername()).isEqualTo(USERNAME_USER_1);
            assertThat(captured.getEmail()).isEqualTo("new@email.com");
            assertThat(captured.getFirstName()).isEqualTo("new firstname");
            assertThat(captured.getLastName()).isEqualTo("new lastname");
            assertThat(captured.getEnabled()).isFalse();
            assertThat(captured.getGroupIds()).containsExactly("group-id");

            assertThat(user1.getPreferences().getLanguage()).isSameAs(language);

            InOrder inOrder = inOrder(userRepository, identityService);
            inOrder.verify(userRepository).saveAndFlush(user1);
            inOrder.verify(identityService).updateUser(eq(identityId), any(IdentityUserUpdateRequestDTO.class));

            verify(userMapper).toDto(user1);
        }
        @Test
        void updateUser_shouldPropagateException_whenIdentityUpdateFails() {
            final RuntimeException identityError = new RuntimeException("identity error");

            UserUpdateRequestDTO dto = buildUserUpdateRequestDTO();
            dto.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
            dto.setUsername(USERNAME_USER_1);
            dto.setEmail(EMAIL_USER_1);

            when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                    .thenReturn(Optional.of(user1));
            when(languageRepository.findByUuid("LANG_ID_01"))
                    .thenReturn(Optional.of(language));
            doThrow(identityError).when(identityService).updateUser(any(), any());

            assertThatThrownBy(() -> userService.updateUser(user1.getUuid(), dto)).isSameAs(identityError);

            verify(userRepository).saveAndFlush(user1);
            verify(userMapper, never()).toDto(any());
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void deleteUser_shouldSoftDeleteUser_whenValid() {
            final String identityId = "identityId";

            when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                    .thenReturn(Optional.of(user1));
            when(accountService.getCurrentIdentityId())
                    .thenReturn(identityId);

            assertThat(user1.getDeletedOn()).isNull();

            userService.deleteUser(user1.getUuid());

            assertThat(user1.getDeletedBy()).isEqualTo(identityId);
            assertThat(user1.getDeletedOn()).isNotNull();

            verify(userRepository).findByUuidAndDeletedOnIsNull(user1.getUuid());
            verify(accountService).getCurrentIdentityId();

            InOrder inOrder = inOrder(userRepository, identityService);
            inOrder.verify(userRepository).saveAndFlush(user1);
            inOrder.verify(identityService).deleteUser(user1.getIdentityId());
        }
        @Test
        void deleteUser_shouldPropagateException_whenIdentityDeletionFails() {
            final RuntimeException identityError = new RuntimeException("identity error");

            when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                    .thenReturn(Optional.of(user1));
            when(accountService.getCurrentIdentityId())
                    .thenReturn("identityId");
            doThrow(identityError).when(identityService).deleteUser(user1.getIdentityId());

            assertThatThrownBy(() -> userService.deleteUser(user1.getUuid())).isSameAs(identityError);

            verify(userRepository).saveAndFlush(user1);
        }
        @Test
        void deleteUser_shouldThrowNotFoundException_whenUserNotFound() {
            final String uuid = "non-existing";
            when(userRepository.findByUuidAndDeletedOnIsNull(uuid))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUser(uuid))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(e -> {
                        UserNotFoundException ex = (UserNotFoundException) e;
                        assertThat(ex).isInstanceOf(NotFoundException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.NOT_FOUND);
                        assertThat(ex.getMessage()).isEqualTo("User not found: non-existing");
                    });

            verify(userRepository).findByUuidAndDeletedOnIsNull(uuid);
            verify(identityService, never()).deleteUser(any());
            verify(accountService, never()).getCurrentIdentityId();
            verify(userRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    class ResetPassword {

        @Test
        void resetPassword_shouldResetPassword_whenValid() {
            when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                    .thenReturn(Optional.of(user1));

            userService.resetPassword(user1.getUuid());

            verify(userRepository).findByUuidAndDeletedOnIsNull(user1.getUuid());
            verify(identityService).resetPassword(user1.getIdentityId());
        }
        @Test
        void resetPassword_shouldThrowNotFoundException_whenUserNotFound() {
            final String uuid = "not-existing";
            when(userRepository.findByUuidAndDeletedOnIsNull(uuid))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.resetPassword(uuid))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(e -> {
                        UserNotFoundException ex = (UserNotFoundException) e;
                        assertThat(ex).isInstanceOf(NotFoundException.class);
                        assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.Model.User.NOT_FOUND);
                        assertThat(ex.getMessage()).isEqualTo("User not found: not-existing");
                    });

            verify(userRepository).findByUuidAndDeletedOnIsNull(uuid);
            verify(identityService, never()).resetPassword(any());
        }
    }

    private void mockSuccessfulCreation(String identityId) {
        when(languageRepository.findByUuid("LANG_ID_01"))
                .thenReturn(Optional.of(language));
        when(identityService.createUser(any(IdentityUserCreateRequestDTO.class)))
                .thenReturn(identityId);
        when(userRepository.saveAndFlush(any(User.class)))
                .thenReturn(user1);
    }

    private void completeTransaction(int status) {
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCompletion(status));
    }

    private UserCreateRequestDTO buildUserCreateRequestDTO() {
        UserCreateRequestDTO dto = new UserCreateRequestDTO();
        dto.setFirstname("new firstname");
        dto.setLastname("new lastname");
        dto.setGender(GenderDTO.F);
        dto.setLanguageId("LANG_ID_01");
        dto.setMobileNumber("9999");
        dto.setTaxIdNumber("TAX_ID_NEW");
        dto.setEmail("new@email.com");
        dto.setUsername("new username");
        dto.setEnabled(false);
        dto.setPassword("password");
        dto.setGroupIds(List.of("group-id"));
        return dto;
    }

    private UserUpdateRequestDTO buildUserUpdateRequestDTO() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
        dto.setFirstname("new firstname");
        dto.setLastname("new lastname");
        dto.setGender(GenderDTO.F);
        dto.setLanguageId("LANG_ID_01");
        dto.setMobileNumber("9999");
        dto.setTaxIdNumber("TAX_ID_NEW");
        dto.setEmail("new@email.com");
        dto.setUsername("new username");
        dto.setEnabled(false);
        dto.setGroupIds(List.of("group-id"));
        return dto;
    }

}