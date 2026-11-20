package com.marcoaga02.modularhub.modules.usermanagement.domain.service;

import com.marcoaga02.modularhub.core.domain.model.Role;
import com.marcoaga02.modularhub.core.domain.repository.RoleRepository;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.RoleSummaryDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.mapper.RoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class RoleService {

    private static final Comparator<Role> SYSTEM_FIRST_THEN_NAME = Comparator
            .comparing(Role::isSystem, Comparator.reverseOrder())
            .thenComparing(Role::getName, String.CASE_INSENSITIVE_ORDER);

    private final RoleRepository roleRepository;

    private final RoleMapper roleMapper;

    public RoleService(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Transactional(readOnly = true)
    public List<RoleSummaryDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .sorted(SYSTEM_FIRST_THEN_NAME)
                .map(roleMapper::toSummary)
                .toList();
    }
}
