package com.marcoaga02.modularhub.modules.usermanagement.domain.service;

import com.marcoaga02.modularhub.core.api.dto.*;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.LanguageNotFoundException;
import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.repository.LanguageRepository;
import com.marcoaga02.modularhub.core.domain.service.AccountPreferencesService;
import com.marcoaga02.modularhub.core.domain.service.AccountService;
import com.marcoaga02.modularhub.core.domain.service.IdentityService;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCriteriaDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserAlreadyExistsException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserManagementExceptionCodes;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserNotFoundException;
import com.marcoaga02.modularhub.modules.usermanagement.api.mapper.AccountProfileMapper;
import com.marcoaga02.modularhub.modules.usermanagement.api.mapper.UserMapper;
import com.marcoaga02.modularhub.modules.usermanagement.domain.model.User;
import com.marcoaga02.modularhub.modules.usermanagement.domain.repository.UserRepository;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

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
    private AccountPreferencesService accountPreferencesService;

    @Mock
    private AccountProfileMapper accountProfileMapper;

    @InjectMocks
    private UserService userService;

    private User user1, user2;

    private UserResponseDTO userResponseDTO1, userResponseDTO2;

    private Language language;

    private LanguageResponseDTO languageResponseDTO;

    @BeforeEach
    void setUp() {
        language = new Language();
        language.setCode("it-IT");
        language.setLabel("Italiano");
        language.setIsDefault(true);

        languageResponseDTO = new LanguageResponseDTO();
        languageResponseDTO.setId(language.getUuid());
        languageResponseDTO.setCode("it-IT");
        languageResponseDTO.setLabel("Italiano");
        languageResponseDTO.setIsDefault(true);

        user1 = new User();
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
        userResponseDTO1.setGender(Gender.M.name());
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
        userResponseDTO2.setGender(Gender.F.name());
        userResponseDTO2.setLanguage(languageResponseDTO);
        userResponseDTO2.setMobileNumber("1111");
        userResponseDTO2.setTaxIdNumber(TAX_ID_NUMBER_USER_2);
        userResponseDTO2.setEmail(EMAIL_USER_2);
        userResponseDTO2.setUsername(USERNAME_USER_2);
        userResponseDTO2.setEnabled(false);
    }

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

        AccountPreferencesResponseDTO preferencesResponseDTO = new AccountPreferencesResponseDTO();
        preferencesResponseDTO.setLanguage(languageResponseDTO);

        when(userRepository.findByUuidAndDeletedOnIsNull(userUuid))
                .thenReturn(Optional.of(user1));
        when(userRepository.findByIdentityId(identityId2))
                .thenReturn(Optional.of(user2));
        when(userRepository.findByIdentityId(identityId1))
                .thenReturn(Optional.of(user1));
        when(userMapper.toDto(user1))
                .thenReturn(userResponseDTO1);
        when(identityService.getUserById(identityId1))
                .thenReturn(userResponseDTO);
        when(accountPreferencesService.getAccountPreferences(identityId1))
                .thenReturn(preferencesResponseDTO);

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
        verify(userRepository).findByIdentityId(identityId2);
        verify(userRepository).findByIdentityId(identityId1);
        verify(identityService).getUserById(identityId1);
        verify(accountPreferencesService).getAccountPreferences(identityId1);
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

        AccountPreferencesResponseDTO preferencesResponseDTO = new AccountPreferencesResponseDTO();
        preferencesResponseDTO.setLanguage(languageResponseDTO);

        when(userRepository.findByUuidAndDeletedOnIsNull(userUuid))
                .thenReturn(Optional.of(user1));
        when(userRepository.findByIdentityId(identityId2))
                .thenReturn(Optional.empty());
        when(userMapper.toDto(user1))
                .thenReturn(userResponseDTO1);
        when(identityService.getUserById(identityId1))
                .thenReturn(userResponseDTO);
        when(accountPreferencesService.getAccountPreferences(identityId1))
                .thenReturn(preferencesResponseDTO);

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
        verify(userRepository).findByIdentityId(identityId2);
        verify(userRepository, times(1)).findByIdentityId(any());
        verify(identityService).getUserById(identityId1);
        verify(accountPreferencesService).getAccountPreferences(identityId1);
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

        AccountPreferencesResponseDTO preferencesResponseDTO = new AccountPreferencesResponseDTO();
        preferencesResponseDTO.setLanguage(languageResponseDTO);

        when(userRepository.findByUuidAndDeletedOnIsNull(userUuid))
                .thenReturn(Optional.of(user1));
        when(userRepository.findByIdentityId(identityId2))
                .thenReturn(Optional.of(user2));
        when(userRepository.findByIdentityId(identityId1))
                .thenReturn(Optional.of(user1));
        when(userMapper.toDto(user1))
                .thenReturn(userResponseDTO1);
        when(identityService.getUserById(identityId1))
                .thenReturn(userResponseDTO);
        when(accountPreferencesService.getAccountPreferences(identityId1))
                .thenReturn(preferencesResponseDTO);

        UserResponseDTO result = userService.getUserByUuid(userUuid);

        assertThat(result).isNotNull().isEqualTo(userResponseDTO1);
        assertThat(result.getAudit()).isNotNull();
        assertThat(result.getAudit().getCreatedBy()).isEqualTo("Vittoria Bianchi");
        assertThat(result.getAudit().getUpdatedBy()).isEqualTo("Mario Rossi");
        assertThat(result.getAudit().getCreatedOn()).isEqualTo(CREATED_ON);
        assertThat(result.getAudit().getUpdatedOn()).isEqualTo(UPDATED_ON);

        assertThat(result.getGroups()).isEmpty();

        verify(userRepository).findByUuidAndDeletedOnIsNull(userUuid);
        verify(userRepository).findByIdentityId(identityId2);
        verify(userRepository).findByIdentityId(identityId1);
        verify(identityService).getUserById(identityId1);
        verify(accountPreferencesService).getAccountPreferences(identityId1);
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
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.USER_NOT_FOUND);
                    assertThat(ex.getLogMessage()).isEqualTo("User with uuid 'not-existing' not found");
                });

        verify(userRepository).findByUuidAndDeletedOnIsNull(uuid);
        verify(userRepository, never()).findByIdentityId(anyString());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void createUser_shouldCreateAndReturnDTO_whenTaxIdNumberNotExists() {
        final String identityId = "identityId";

        UserRequestDTO dto = buildUserRequestDTO();

        AccountPreferencesResponseDTO preferencesResponseDTO = new AccountPreferencesResponseDTO();
        preferencesResponseDTO.setLanguage(languageResponseDTO);

        when(languageRepository.findByUuid("LANG_ID_01"))
                .thenReturn(Optional.of(language));
        when(userRepository.findByTaxIdNumberAndDeletedOnIsNull("TAX_ID_NEW"))
                .thenReturn(Optional.empty());
        when(userRepository.findByUsernameAndDeletedOnIsNull("new username"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndDeletedOnIsNull("new@email.com"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenReturn(user1);
        when(userMapper.toDto(user1))
                .thenReturn(userResponseDTO1);
        when(identityService.createUser(any(IdentityUserCreateRequestDTO.class)))
                .thenReturn(identityId);
        when(accountPreferencesService.createAccountPreferences(any(AccountPreferencesCreateDTO.class)))
                .thenReturn(preferencesResponseDTO);

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

        ArgumentCaptor<AccountPreferencesCreateDTO> preferencesCaptor =
                ArgumentCaptor.forClass(AccountPreferencesCreateDTO.class);

        verify(accountPreferencesService).createAccountPreferences(preferencesCaptor.capture());

        AccountPreferencesCreateDTO preferencesCaptured = preferencesCaptor.getValue();

        assertThat(preferencesCaptured.getIdentityId()).isEqualTo(identityId);
        assertThat(preferencesCaptured.getLanguageId()).isEqualTo("LANG_ID_01");

        verify(userMapper).toDto(user1);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getIdentityId()).isEqualTo(identityId);
    }

    @Test
    void createUser_shouldThrowUserAlreadyExistsException_whenTaxIdNumberAlreadyExists() {
        UserRequestDTO dto = new UserRequestDTO();
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
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.USER_TAX_ID_ALREADY_EXISTS);
                    assertThat(ex.getLogMessage()).isEqualTo("User with taxIdNumber 'TAX_ID_01' already exists");
                });

        verify(languageRepository).findByUuid("LANG_ID_01");
        verify(userRepository).findByTaxIdNumberAndDeletedOnIsNull(TAX_ID_NUMBER_USER_1);
        verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
        verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
        verify(identityService, never()).createUser(any(IdentityUserCreateRequestDTO.class));
        verify(accountPreferencesService, never()).createAccountPreferences(any(AccountPreferencesCreateDTO.class));
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void createUser_shouldThrowUserAlreadyExistsException_whenUsernameAlreadyExists() {
        UserRequestDTO dto = new UserRequestDTO();
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
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.USER_USERNAME_ALREADY_EXISTS);
                    assertThat(ex.getLogMessage()).isEqualTo("User with username 'existing.username' already exists");
                });

        verify(languageRepository).findByUuid("LANG_ID_01");
        verify(userRepository).findByTaxIdNumberAndDeletedOnIsNull(TAX_ID_NUMBER_USER_1);
        verify(userRepository).findByUsernameAndDeletedOnIsNull("existing.username");
        verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
        verify(identityService, never()).createUser(any(IdentityUserCreateRequestDTO.class));
        verify(accountPreferencesService, never()).createAccountPreferences(any(AccountPreferencesCreateDTO.class));
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void createUser_shouldThrowUserAlreadyExistsException_whenEmailAlreadyExists() {
        UserRequestDTO dto = new UserRequestDTO();
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
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.USER_EMAIL_ALREADY_EXISTS);
                    assertThat(ex.getLogMessage()).isEqualTo("User with email 'existing@email.it' already exists");
                });

        verify(languageRepository).findByUuid("LANG_ID_01");
        verify(userRepository).findByTaxIdNumberAndDeletedOnIsNull(TAX_ID_NUMBER_USER_1);
        verify(userRepository).findByUsernameAndDeletedOnIsNull("new.username");
        verify(userRepository).findByEmailAndDeletedOnIsNull("existing@email.it");
        verify(identityService, never()).createUser(any(IdentityUserCreateRequestDTO.class));
        verify(accountPreferencesService, never()).createAccountPreferences(any(AccountPreferencesCreateDTO.class));
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void createUser_shouldThrowLanguageNotFoundException_whenInvalidLanguageId() {
        final String languageId = "INVALID-LANG";

        UserRequestDTO dto = new UserRequestDTO();
        dto.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
        dto.setLanguageId(languageId);

        when(languageRepository.findByUuid(languageId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(LanguageNotFoundException.class)
                .satisfies(e -> {
                    LanguageNotFoundException ex = (LanguageNotFoundException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.LANGUAGE_NOT_FOUND);
                    assertThat(ex.getLogMessage()).isEqualTo("Language with uuid 'INVALID-LANG' not found");
                });

        verify(languageRepository).findByUuid(languageId);
        verify(userRepository, never()).findByTaxIdNumberAndDeletedOnIsNull(any());
        verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
        verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
        verify(identityService, never()).createUser(any(IdentityUserCreateRequestDTO.class));
        verify(accountPreferencesService, never()).createAccountPreferences(any(AccountPreferencesCreateDTO.class));
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void updateUser_shouldUpdateAndReturnDTO_whenValid() {
        final String identityId = "identityId";
        user1.setIdentityId(identityId);

        UserRequestDTO dto = buildUserRequestDTO();
        dto.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
        dto.setUsername(USERNAME_USER_1);
        dto.setEmail(EMAIL_USER_1);

        AccountPreferencesResponseDTO preferencesResponseDTO = new AccountPreferencesResponseDTO();
        preferencesResponseDTO.setLanguage(languageResponseDTO);

        when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                .thenReturn(Optional.of(user1));
        when(languageRepository.findByUuid("LANG_ID_01"))
                .thenReturn(Optional.of(language));
        when(userMapper.toDto(user1))
                .thenReturn(userResponseDTO1);
        when(accountPreferencesService.updateAccountPreferencesByIdentityId(
                eq(identityId),
                any(AccountPreferencesUpdateDTO.class)
        )).thenReturn(preferencesResponseDTO);

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

        ArgumentCaptor<AccountPreferencesUpdateDTO> preferencesCaptor =
                ArgumentCaptor.forClass(AccountPreferencesUpdateDTO.class);

        verify(accountPreferencesService).updateAccountPreferencesByIdentityId(
                eq(identityId),
                preferencesCaptor.capture()
        );

        AccountPreferencesUpdateDTO preferencesCaptured = preferencesCaptor.getValue();

        assertThat(preferencesCaptured.getLanguageId()).isEqualTo("LANG_ID_01");

        verify(userMapper).toDto(user1);
    }

    @Test
    void updateUser_shouldThrowNotFoundException_whenUserNotFound() {
        UserRequestDTO dto = buildUserRequestDTO();

        final String uuid = "not-existing";
        when(userRepository.findByUuidAndDeletedOnIsNull(uuid))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(uuid, dto))
                .isInstanceOf(UserNotFoundException.class)
                .satisfies(e -> {
                    UserNotFoundException ex = (UserNotFoundException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.USER_NOT_FOUND);
                    assertThat(ex.getLogMessage()).isEqualTo("User with uuid 'not-existing' not found");
                });

        verify(userRepository).findByUuidAndDeletedOnIsNull(uuid);
        verify(languageRepository, never()).findByUuid(any());
        verify(userRepository, never()).findByTaxIdNumberAndDeletedOnIsNull(any());
        verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
        verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
        verify(userMapper, never()).updateEntity(any(), any());
        verify(identityService, never()).updateUser(any(), any());
        verify(accountPreferencesService, never()).updateAccountPreferencesByIdentityId(anyString(), any(AccountPreferencesUpdateDTO.class));
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void updateUser_shouldThrowLanguageNotFoundException_whenInvalidLanguage() {
        UserRequestDTO dto = buildUserRequestDTO();
        dto.setLanguageId("INVALID-LANG");

        when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                .thenReturn(Optional.of(user1));
        when(languageRepository.findByUuid("INVALID-LANG"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(user1.getUuid(), dto))
                .isInstanceOf(LanguageNotFoundException.class)
                .satisfies(e -> {
                    LanguageNotFoundException ex = (LanguageNotFoundException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.LANGUAGE_NOT_FOUND);
                    assertThat(ex.getLogMessage()).isEqualTo("Language with uuid 'INVALID-LANG' not found");
                });

        verify(userRepository).findByUuidAndDeletedOnIsNull(user1.getUuid());
        verify(languageRepository).findByUuid("INVALID-LANG");
        verify(userRepository, never()).findByTaxIdNumberAndDeletedOnIsNull(any());
        verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
        verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
        verify(userMapper, never()).updateEntity(any(), any());
        verify(identityService, never()).updateUser(any(), any());
        verify(accountPreferencesService, never()).updateAccountPreferencesByIdentityId(anyString(), any(AccountPreferencesUpdateDTO.class));
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void updateUser_shouldThrowUserAlreadyExistsException_whenTaxIdNumberAlreadyExists() {
        final String taxIdNumber = TAX_ID_NUMBER_USER_2;

        UserRequestDTO dto = buildUserRequestDTO();
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
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.USER_TAX_ID_ALREADY_EXISTS);
                    assertThat(ex.getLogMessage()).isEqualTo("User with taxIdNumber 'TAX_ID_02' already exists");
                });

        verify(userRepository).findByUuidAndDeletedOnIsNull(user1.getUuid());
        verify(languageRepository).findByUuid("LANG_ID_01");
        verify(userRepository).findByTaxIdNumberAndDeletedOnIsNull(taxIdNumber);
        verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
        verify(userRepository, never()).findByEmailAndDeletedOnIsNull(anyString());
        verify(userMapper, never()).updateEntity(any(), any());
        verify(identityService, never()).updateUser(any(), any());
        verify(accountPreferencesService, never()).updateAccountPreferencesByIdentityId(anyString(), any(AccountPreferencesUpdateDTO.class));
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void updateUser_shouldUpdateAndReturnDTO_whenTaxIdNumberChangedAndNotExists() {
        final String identityId = "identityId";
        user1.setIdentityId(identityId);

        UserRequestDTO dto = buildUserRequestDTO();
        dto.setUsername(USERNAME_USER_1);
        dto.setEmail(EMAIL_USER_1);

        AccountPreferencesResponseDTO preferencesResponseDTO = new AccountPreferencesResponseDTO();
        preferencesResponseDTO.setLanguage(languageResponseDTO);

        when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                .thenReturn(Optional.of(user1));
        when(languageRepository.findByUuid("LANG_ID_01"))
                .thenReturn(Optional.of(language));
        when(userRepository.findByTaxIdNumberAndDeletedOnIsNull("TAX_ID_NEW"))
                .thenReturn(Optional.empty());
        when(userMapper.toDto(user1))
                .thenReturn(userResponseDTO1);
        when(accountPreferencesService.updateAccountPreferencesByIdentityId(
                eq(identityId),
                any(AccountPreferencesUpdateDTO.class)
        )).thenReturn(preferencesResponseDTO);

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

        ArgumentCaptor<AccountPreferencesUpdateDTO> preferencesCaptor =
                ArgumentCaptor.forClass(AccountPreferencesUpdateDTO.class);

        verify(accountPreferencesService).updateAccountPreferencesByIdentityId(
                eq(identityId),
                preferencesCaptor.capture()
        );

        AccountPreferencesUpdateDTO preferencesCaptured = preferencesCaptor.getValue();

        assertThat(preferencesCaptured.getLanguageId()).isEqualTo("LANG_ID_01");

        verify(userMapper).toDto(user1);
    }

    @Test
    void updateUser_shouldThrowUserAlreadyExistsException_whenUsernameAlreadyExists() {
        final String username = USERNAME_USER_2;

        UserRequestDTO dto = buildUserRequestDTO();
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
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.USER_USERNAME_ALREADY_EXISTS);
                    assertThat(ex.getLogMessage()).isEqualTo("User with username 'username2' already exists");
                });

        verify(userRepository).findByUuidAndDeletedOnIsNull(user1.getUuid());
        verify(languageRepository).findByUuid("LANG_ID_01");
        verify(userRepository, never()).findByTaxIdNumberAndDeletedOnIsNull(anyString());
        verify(userRepository).findByUsernameAndDeletedOnIsNull(username);
        verify(userRepository, never()).findByEmailAndDeletedOnIsNull(any());
        verify(userMapper, never()).updateEntity(any(), any());
        verify(identityService, never()).updateUser(any(), any());
        verify(accountPreferencesService, never()).updateAccountPreferencesByIdentityId(anyString(), any(AccountPreferencesUpdateDTO.class));
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void updateUser_shouldUpdateAndReturnDTO_whenUsernameChangedAndNotExists() {
        final String identityId = "identityId";
        user1.setIdentityId(identityId);

        UserRequestDTO dto = buildUserRequestDTO();
        dto.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
        dto.setEmail(EMAIL_USER_1);

        AccountPreferencesResponseDTO preferencesResponseDTO = new AccountPreferencesResponseDTO();
        preferencesResponseDTO.setLanguage(languageResponseDTO);

        when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                .thenReturn(Optional.of(user1));
        when(languageRepository.findByUuid("LANG_ID_01"))
                .thenReturn(Optional.of(language));
        when(userRepository.findByUsernameAndDeletedOnIsNull("new username"))
                .thenReturn(Optional.empty());
        when(userMapper.toDto(user1))
                .thenReturn(userResponseDTO1);
        when(accountPreferencesService.updateAccountPreferencesByIdentityId(
                eq(identityId),
                any(AccountPreferencesUpdateDTO.class)
        )).thenReturn(preferencesResponseDTO);

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

        ArgumentCaptor<AccountPreferencesUpdateDTO> preferencesCaptor =
                ArgumentCaptor.forClass(AccountPreferencesUpdateDTO.class);

        verify(accountPreferencesService).updateAccountPreferencesByIdentityId(
                eq(identityId),
                preferencesCaptor.capture()
        );

        AccountPreferencesUpdateDTO preferencesCaptured = preferencesCaptor.getValue();

        assertThat(preferencesCaptured.getLanguageId()).isEqualTo("LANG_ID_01");

        verify(userMapper).toDto(user1);
    }

    @Test
    void updateUser_shouldThrowUserAlreadyExistsException_whenEmailAlreadyExists() {
        final String email = EMAIL_USER_2;

        UserRequestDTO dto = buildUserRequestDTO();
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
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.USER_EMAIL_ALREADY_EXISTS);
                    assertThat(ex.getLogMessage()).isEqualTo("User with email 'vittoria@mock.it' already exists");
                });

        verify(userRepository).findByUuidAndDeletedOnIsNull(user1.getUuid());
        verify(languageRepository).findByUuid("LANG_ID_01");
        verify(userRepository, never()).findByTaxIdNumberAndDeletedOnIsNull(anyString());
        verify(userRepository, never()).findByUsernameAndDeletedOnIsNull(anyString());
        verify(userRepository).findByEmailAndDeletedOnIsNull(email);
        verify(userMapper, never()).updateEntity(any(), any());
        verify(identityService, never()).updateUser(any(), any());
        verify(accountPreferencesService, never()).updateAccountPreferencesByIdentityId(anyString(), any(AccountPreferencesUpdateDTO.class));
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void updateUser_shouldUpdateAndReturnDTO_whenEmailChangedAndNotExists() {
        final String identityId = "identityId";
        user1.setIdentityId(identityId);

        UserRequestDTO dto = buildUserRequestDTO();
        dto.setTaxIdNumber(TAX_ID_NUMBER_USER_1);
        dto.setUsername(USERNAME_USER_1);

        AccountPreferencesResponseDTO preferencesResponseDTO = new AccountPreferencesResponseDTO();
        preferencesResponseDTO.setLanguage(languageResponseDTO);

        when(userRepository.findByUuidAndDeletedOnIsNull(user1.getUuid()))
                .thenReturn(Optional.of(user1));
        when(languageRepository.findByUuid("LANG_ID_01"))
                .thenReturn(Optional.of(language));
        when(userRepository.findByEmailAndDeletedOnIsNull("new@email.com"))
                .thenReturn(Optional.empty());
        when(userMapper.toDto(user1))
                .thenReturn(userResponseDTO1);
        when(accountPreferencesService.updateAccountPreferencesByIdentityId(
                eq(identityId),
                any(AccountPreferencesUpdateDTO.class)
        )).thenReturn(preferencesResponseDTO);

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

        ArgumentCaptor<AccountPreferencesUpdateDTO> preferencesCaptor =
                ArgumentCaptor.forClass(AccountPreferencesUpdateDTO.class);

        verify(accountPreferencesService).updateAccountPreferencesByIdentityId(
                eq(identityId),
                preferencesCaptor.capture()
        );

        AccountPreferencesUpdateDTO preferencesCaptured = preferencesCaptor.getValue();

        assertThat(preferencesCaptured.getLanguageId()).isEqualTo("LANG_ID_01");

        verify(userMapper).toDto(user1);
    }

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
        verify(identityService).deleteUser(user1.getIdentityId());
        verify(accountService).getCurrentIdentityId();
        verify(userRepository).save(user1);
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
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.USER_NOT_FOUND);
                    assertThat(ex.getLogMessage()).isEqualTo("User with uuid 'non-existing' not found");
                });

        verify(userRepository).findByUuidAndDeletedOnIsNull(uuid);
        verify(identityService, never()).updateUser(any(), any());
        verify(accountService, never()).getCurrentAccount();
        verify(userRepository, never()).save(any());
    }

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
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.USER_NOT_FOUND);
                    assertThat(ex.getLogMessage()).isEqualTo("User with uuid 'not-existing' not found");
                });

        verify(userRepository).findByUuidAndDeletedOnIsNull(uuid);
        verify(identityService, never()).resetPassword(any());
    }

    @Test
    void getDetails_shouldReturnDetails_whenValid() {
        final String identityId = "identityId";

        AccountProfileDTO accountProfileDTO = new AccountProfileDTO();
        accountProfileDTO.setGender(Gender.M);
        accountProfileDTO.setMobileNumber("mobile-number");
        accountProfileDTO.setTaxIdNumber("tax-id-number");

        when(userRepository.findByIdentityId(identityId))
                .thenReturn(Optional.of(user1));
        when(accountProfileMapper.toAccountProfileDTO(user1))
                .thenReturn(accountProfileDTO);

        AccountProfileDTO result = userService.getDetails(identityId);

        assertThat(result).isEqualTo(accountProfileDTO);

        verify(userRepository).findByIdentityId(identityId);
        verify(accountProfileMapper).toAccountProfileDTO(user1);
    }

    @Test
    void getDetails_shouldThrowUserNotFoundException_whenInvalidUuid() {
        final String identityId = "not-existing";

        when(userRepository.findByIdentityId(identityId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getDetails(identityId))
                .isInstanceOf(UserNotFoundException.class)
                .satisfies(e -> {
                    UserNotFoundException ex = (UserNotFoundException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(UserManagementExceptionCodes.USER_NOT_FOUND);
                    assertThat(ex.getLogMessage()).isEqualTo("User with uuid 'not-existing' not found");
                });
    }

    private UserRequestDTO buildUserRequestDTO() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setFirstname("new firstname");
        dto.setLastname("new lastname");
        dto.setGender(Gender.F);
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

}