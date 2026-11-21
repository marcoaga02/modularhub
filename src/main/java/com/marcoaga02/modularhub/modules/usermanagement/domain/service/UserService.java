package com.marcoaga02.modularhub.modules.usermanagement.domain.service;

import com.marcoaga02.modularhub.core.api.dto.AuditDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserUpdateRequestDTO;
import com.marcoaga02.modularhub.core.domain.model.Role;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.core.domain.repository.RoleRepository;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.core.domain.service.AccountService;
import com.marcoaga02.modularhub.core.domain.service.IdentityService;
import com.marcoaga02.modularhub.core.domain.service.UserAccountService;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCreateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCriteriaDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserUpdateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.role.RoleNotFoundException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.LastAdminException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.UserAlreadyExistsException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.UserNotFoundException;
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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Boolean EMAIL_VERIFIED = true;

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final IdentityService identityService;

    private final AccountService accountService;

    private final UserAccountService userAccountService;

    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, UserMapper userMapper, IdentityService identityService,
                       AccountService accountService, UserAccountService userAccountService,
                       RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.identityService = identityService;
        this.accountService = accountService;
        this.userAccountService = userAccountService;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(UserCriteriaDTO criteria, Pageable pageable) {
        Page<User> pageResult = userRepository.findAll(
                UserSpecification.byCriteria(criteria),
                pageable
        );

        return pageResult.map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
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

        return dto;
    }

    @Transactional
    public UserResponseDTO createUser(UserCreateRequestDTO dto) {
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
        user.setRoles(findRolesOrElseThrow(dto.getRoleIds()));

        IdentityUserCreateRequestDTO userRequestDTO = new IdentityUserCreateRequestDTO(
                dto.getUsername(),
                dto.getEmail(),
                dto.getFirstname(),
                dto.getLastname(),
                dto.getEnabled(),
                EMAIL_VERIFIED,
                dto.getPassword()
        );

        String identityId = identityService.createUser(userRequestDTO);
        rollbackIdentityUserOnTransactionRollback(identityId);

        user.setIdentityId(identityId);

        return userMapper.toDto(userRepository.saveAndFlush(user));
    }

    @Transactional
    public UserResponseDTO updateUser(String uuid, UserUpdateRequestDTO dto) {
        User user = userRepository.findByUuidAndDeletedOnIsNull(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));

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

        boolean wasActiveAdmin = isActiveAdmin(user);

        userMapper.updateEntity(dto, user);
        user.setRoles(findRolesOrElseThrow(dto.getRoleIds()));

        if (wasActiveAdmin && !isActiveAdmin(user)) {
            requireAnotherActiveAdmin(user);
        }

        userRepository.saveAndFlush(user);

        IdentityUserUpdateRequestDTO userRequestDTO = new IdentityUserUpdateRequestDTO(
                dto.getUsername(),
                dto.getEmail(),
                dto.getFirstname(),
                dto.getLastname(),
                dto.getEnabled()
        );

        identityService.updateUser(user.getIdentityId(), userRequestDTO);

        return userMapper.toDto(user);
    }

    @Transactional
    public void deleteUser(String uuid) {
        User user = userRepository.findByUuidAndDeletedOnIsNull(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));

        if (isActiveAdmin(user)) {
            requireAnotherActiveAdmin(user);
        }

        user.getRoles().clear();
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

    private Set<Role> findRolesOrElseThrow(Set<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Role> roles = roleRepository.findAllByUuidIn(roleIds);
        Set<String> foundIds = roles.stream()
                .map(Role::getUuid)
                .collect(Collectors.toSet());

        roleIds.stream()
                .filter(id -> !foundIds.contains(id))
                .findFirst()
                .ifPresent(id -> {
                    throw new RoleNotFoundException(id);
                });

        return new HashSet<>(roles);
    }

    private static boolean isActiveAdmin(User user) {
        return Boolean.TRUE.equals(user.getEnabled())
                && user.getDeletedOn() == null
                && user.getRoles().stream().anyMatch(role -> SecurityConstants.ADMIN_ROLE_CODE.equals(role.getCode()));
    }

    private void requireAnotherActiveAdmin(User user) {
        if (userRepository.countByRoles_CodeAndEnabledTrueAndDeletedOnIsNullAndIdNot(
                SecurityConstants.ADMIN_ROLE_CODE, user.getId()) == 0) {
            throw new LastAdminException(user.getUuid());
        }
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
}
