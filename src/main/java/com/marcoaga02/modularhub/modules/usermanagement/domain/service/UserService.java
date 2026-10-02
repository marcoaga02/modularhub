package com.marcoaga02.modularhub.modules.usermanagement.domain.service;

import com.marcoaga02.modularhub.core.api.dto.*;
import com.marcoaga02.modularhub.core.api.exception.LanguageNotFoundException;
import com.marcoaga02.modularhub.core.domain.repository.LanguageRepository;
import com.marcoaga02.modularhub.core.domain.service.AccountPreferencesService;
import com.marcoaga02.modularhub.core.domain.service.AccountService;
import com.marcoaga02.modularhub.core.domain.service.IdentityService;
import com.marcoaga02.modularhub.core.spi.AccountDetailsProvider;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCriteriaDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserAlreadyExistsException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserNotFoundException;
import com.marcoaga02.modularhub.modules.usermanagement.api.mapper.AccountProfileMapper;
import com.marcoaga02.modularhub.modules.usermanagement.api.mapper.UserMapper;
import com.marcoaga02.modularhub.modules.usermanagement.domain.model.User;
import com.marcoaga02.modularhub.modules.usermanagement.domain.repository.UserRepository;
import com.marcoaga02.modularhub.modules.usermanagement.domain.repository.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;

@Service
public class UserService implements AccountDetailsProvider {

    private static final Boolean EMAIL_VERIFIED = true;

    private final UserRepository userRepository;

    private final LanguageRepository languageRepository;

    private final UserMapper userMapper;

    private final AccountProfileMapper accountProfileMapper;

    private final IdentityService identityService;

    private final AccountService accountService;

    private final AccountPreferencesService accountPreferencesService;

    public UserService(UserRepository userRepository, LanguageRepository languageRepository, UserMapper userMapper, AccountProfileMapper accountProfileMapper, IdentityService identityService, AccountService accountService, AccountPreferencesService accountPreferencesService) {
        this.userRepository = userRepository;
        this.languageRepository = languageRepository;
        this.userMapper = userMapper;
        this.accountProfileMapper = accountProfileMapper;
        this.identityService = identityService;
        this.accountService = accountService;
        this.accountPreferencesService = accountPreferencesService;
    }

    public Page<UserResponseDTO> getAllUsers(UserCriteriaDTO criteria, Pageable pageable) {
        Page<User> pageResult = userRepository.findAll(
                UserSpecification.byCriteria(criteria),
                pageable
        );

        return pageResult.map(userMapper::toDto);
    }

    public UserResponseDTO getUserByUuid(String uuid) {
        User user = userRepository.findByUuidAndDeletedOnIsNull(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));

        AuditDTO auditDto = new AuditDTO();
        auditDto.setCreatedOn(user.getCreatedOn());
        auditDto.setCreatedBy(resolveUserFullName(user.getCreatedBy()));
        auditDto.setUpdatedOn(user.getUpdatedOn());
        auditDto.setUpdatedBy(resolveUserFullName(user.getUpdatedBy()));

        UserResponseDTO dto = userMapper.toDto(user);
        dto.setAudit(auditDto);

        IdentityUserResponseDTO userResponseDTO = identityService.getUserById(user.getIdentityId());

        if (userResponseDTO.getGroups() != null) {
            dto.setGroups(new HashSet<>(userResponseDTO.getGroups()));
        }

        AccountPreferencesResponseDTO preferencesDTO = accountPreferencesService.getAccountPreferences(user.getIdentityId());

        dto.setLanguage(preferencesDTO.getLanguage());

        return dto;
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto) {
        validateLanguage(dto.getLanguageId());

        if (existsActiveUserWithSameTaxIdNumber(dto.getTaxIdNumber())) {
            throw UserAlreadyExistsException.taxIdNumber(dto.getTaxIdNumber());
        }

        if (existsActiveUserWithSameUsername(dto.getUsername())) {
            throw UserAlreadyExistsException.username(dto.getUsername());
        }

        if (existsActiveUserWithSameEmail(dto.getEmail())) {
            throw UserAlreadyExistsException.email(dto.getEmail());
        }

        User user = new User();

        userMapper.updateEntity(dto, user);

        IdentityUserCreateRequestDTO userRequestDTO = new IdentityUserCreateRequestDTO(
                dto.getUsername(),
                dto.getEmail(),
                dto.getFirstname(),
                dto.getLastname(),
                dto.getEnabled(),
                EMAIL_VERIFIED,
                dto.getPassword(),
                dto.getGroupIds()
        );

        String identityId = identityService.createUser(userRequestDTO);
        user.setIdentityId(identityId);

        AccountPreferencesCreateDTO preferencesRequestDTO = new AccountPreferencesCreateDTO(
                identityId,
                dto.getLanguageId()
        );

        AccountPreferencesResponseDTO preferencesResponseDTO =
                accountPreferencesService.createAccountPreferences(preferencesRequestDTO);

        UserResponseDTO userResponseDTO = userMapper.toDto(userRepository.save(user));
        userResponseDTO.setLanguage(preferencesResponseDTO.getLanguage());

        return userResponseDTO;
    }

    @Transactional
    public UserResponseDTO updateUser(String uuid, UserRequestDTO dto) {
        User user = userRepository.findByUuidAndDeletedOnIsNull(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));

        validateLanguage(dto.getLanguageId());

        if (!Objects.equals(user.getTaxIdNumber(), dto.getTaxIdNumber())
                && existsActiveUserWithSameTaxIdNumber(dto.getTaxIdNumber())
        ) {
            throw UserAlreadyExistsException.taxIdNumber(dto.getTaxIdNumber());
        }

        if (!Objects.equals(user.getUsername(), dto.getUsername())
                && existsActiveUserWithSameUsername(dto.getUsername())
        ) {
            throw UserAlreadyExistsException.username(dto.getUsername());
        }

        if (!Objects.equals(user.getEmail(), dto.getEmail())
                && existsActiveUserWithSameEmail(dto.getEmail())
        ) {
            throw UserAlreadyExistsException.email(dto.getEmail());
        }


        userMapper.updateEntity(dto, user);

        IdentityUserUpdateRequestDTO userRequestDTO = new IdentityUserUpdateRequestDTO(
                dto.getUsername(),
                dto.getEmail(),
                dto.getFirstname(),
                dto.getLastname(),
                dto.getEnabled(),
                dto.getGroupIds()
        );

        identityService.updateUser(user.getIdentityId(), userRequestDTO);

        AccountPreferencesUpdateDTO preferencesRequestDTO = new AccountPreferencesUpdateDTO(
                dto.getLanguageId()
        );

        AccountPreferencesResponseDTO preferencesResponseDTO =
                accountPreferencesService.updateAccountPreferencesByIdentityId(user.getIdentityId(), preferencesRequestDTO);

        UserResponseDTO userResponseDTO = userMapper.toDto(user);
        userResponseDTO.setLanguage(preferencesResponseDTO.getLanguage());

        return userResponseDTO;
    }

    @Transactional
    public void deleteUser(String uuid) {
        User user = userRepository.findByUuidAndDeletedOnIsNull(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));

        identityService.deleteUser(user.getIdentityId());

        user.setDeletedBy(accountService.getCurrentIdentityId());
        user.setDeletedOn(OffsetDateTime.now());
        userRepository.save(user);
    }

    public void resetPassword(String uuid) {
        User user = userRepository.findByUuidAndDeletedOnIsNull(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));

        identityService.resetPassword(user.getIdentityId());
    }

    private boolean existsActiveUserWithSameTaxIdNumber(String taxIdNumber) {
        return userRepository.findByTaxIdNumberAndDeletedOnIsNull(taxIdNumber).isPresent();
    }

    private boolean existsActiveUserWithSameUsername(String username) {
        return userRepository.findByUsernameAndDeletedOnIsNull(username).isPresent();
    }

    private boolean existsActiveUserWithSameEmail(String email) {
        return userRepository.findByEmailAndDeletedOnIsNull(email).isPresent();
    }

    private void validateLanguage(String langUuid) {
        languageRepository.findByUuid(langUuid)
                .orElseThrow(() -> new LanguageNotFoundException(langUuid));
    }

    private String resolveUserFullName(String identityId) {
        if (identityId == null) {
            return "";
        }

        return userRepository.findByIdentityId(identityId)
                .map(User::getFullName)
                .orElse("");
    }

    @Override
    public AccountProfileDTO getDetails(String identityId) {
        User user = userRepository.findByIdentityId(identityId)
                .orElseThrow(() -> new UserNotFoundException(identityId));

        return accountProfileMapper.toAccountProfileDTO(user);
    }
}
