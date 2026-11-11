package com.marcoaga02.modularhub.modules.usermanagement.domain.service;

import com.marcoaga02.modularhub.core.api.dto.*;
import com.marcoaga02.modularhub.core.api.exception.LanguageNotFoundException;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.core.domain.repository.LanguageRepository;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.core.domain.service.AccountService;
import com.marcoaga02.modularhub.core.domain.service.IdentityService;
import com.marcoaga02.modularhub.core.domain.service.UserAccountService;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCriteriaDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserAlreadyExistsException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserNotFoundException;
import com.marcoaga02.modularhub.modules.usermanagement.api.mapper.UserMapper;
import com.marcoaga02.modularhub.modules.usermanagement.domain.repository.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;

@Service
public class UserService {

    private static final Boolean EMAIL_VERIFIED = true;

    private final UserRepository userRepository;

    private final LanguageRepository languageRepository;

    private final UserMapper userMapper;

    private final IdentityService identityService;

    private final AccountService accountService;

    private final UserAccountService userAccountService;

    public UserService(UserRepository userRepository, LanguageRepository languageRepository, UserMapper userMapper, IdentityService identityService, AccountService accountService, UserAccountService userAccountService) {
        this.userRepository = userRepository;
        this.languageRepository = languageRepository;
        this.userMapper = userMapper;
        this.identityService = identityService;
        this.accountService = accountService;
        this.userAccountService = userAccountService;
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
        auditDto.setCreatedBy(userAccountService.resolveFullName(user.getCreatedBy()));
        auditDto.setUpdatedOn(user.getUpdatedOn());
        auditDto.setUpdatedBy(userAccountService.resolveFullName(user.getUpdatedBy()));

        UserResponseDTO dto = userMapper.toDto(user);
        dto.setAudit(auditDto);

        IdentityUserResponseDTO userResponseDTO = identityService.getUserById(user.getIdentityId());

        if (userResponseDTO.getGroups() != null) {
            dto.setGroups(new HashSet<>(userResponseDTO.getGroups()));
        }

        return dto;
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto) {
        Language language = findLanguageOrElseThrow(dto.getLanguageId());

        if (existsActiveUserWithSameTaxIdNumber(dto.getTaxIdNumber())) {
            throw UserAlreadyExistsException.taxIdNumber(dto.getTaxIdNumber());
        }

        if (existsActiveUserWithSameUsername(dto.getUsername())) {
            throw UserAlreadyExistsException.username(dto.getUsername());
        }

        if (existsActiveUserWithSameEmail(dto.getEmail())) {
            throw UserAlreadyExistsException.email(dto.getEmail());
        }

        AccountPreferences preferences = new AccountPreferences();
        preferences.setLanguage(language);

        User user = new User();
        user.setPreferences(preferences);

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
        rollbackIdentityUserOnTransactionRollback(identityId);

        user.setIdentityId(identityId);

        return userMapper.toDto(userRepository.saveAndFlush(user));
    }

    @Transactional
    public UserResponseDTO updateUser(String uuid, UserRequestDTO dto) {
        User user = userRepository.findByUuidAndDeletedOnIsNull(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));

        Language language = findLanguageOrElseThrow(dto.getLanguageId());

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
        user.getPreferences().setLanguage(language);
        userRepository.saveAndFlush(user);

        IdentityUserUpdateRequestDTO userRequestDTO = new IdentityUserUpdateRequestDTO(
                dto.getUsername(),
                dto.getEmail(),
                dto.getFirstname(),
                dto.getLastname(),
                dto.getEnabled(),
                dto.getGroupIds()
        );

        identityService.updateUser(user.getIdentityId(), userRequestDTO);

        return userMapper.toDto(user);
    }

    @Transactional
    public void deleteUser(String uuid) {
        User user = userRepository.findByUuidAndDeletedOnIsNull(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));

        user.setDeletedBy(accountService.getCurrentIdentityId());
        user.setDeletedOn(OffsetDateTime.now());
        userRepository.saveAndFlush(user);

        identityService.deleteUser(user.getIdentityId());
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

    private void rollbackIdentityUserOnTransactionRollback(String identityId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    identityService.rollbackCreatedUser(identityId);
                }
            }
        });
    }

    private Language findLanguageOrElseThrow(String langUuid) {
        return languageRepository.findByUuid(langUuid)
                .orElseThrow(() -> new LanguageNotFoundException(langUuid));
    }
}
