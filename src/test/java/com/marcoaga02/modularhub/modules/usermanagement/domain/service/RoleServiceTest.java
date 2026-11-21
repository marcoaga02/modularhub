package com.marcoaga02.modularhub.modules.usermanagement.domain.service;

import com.marcoaga02.modularhub.core.domain.model.Role;
import com.marcoaga02.modularhub.core.domain.repository.RoleRepository;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.RoleSummaryDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.mapper.RoleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.marcoaga02.modularhub.fixture.UserFixtures.adminRole;
import static com.marcoaga02.modularhub.fixture.UserFixtures.role;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleService roleService;

    @Test
    void getAllRoles_shouldReturnSystemRolesFirstThenCustomRolesByNameIgnoringCase() {
        Role zeta = role("zeta");
        Role admin = adminRole();
        Role alpha = role("Alpha");
        when(roleRepository.findAll()).thenReturn(List.of(zeta, admin, alpha));
        mockToSummary(zeta, admin, alpha);

        assertThat(roleService.getAllRoles())
                .extracting(RoleSummaryDTO::getName)
                .containsExactly(admin.getName(), "Alpha", "zeta");
        verify(roleRepository).findAll();
        verify(roleMapper).toSummary(zeta);
        verify(roleMapper).toSummary(admin);
        verify(roleMapper).toSummary(alpha);
    }

    @Test
    void getAllRoles_shouldReturnEmptyList_whenNoRoleExists() {
        when(roleRepository.findAll()).thenReturn(List.of());

        assertThat(roleService.getAllRoles()).isEmpty();
        verify(roleRepository).findAll();
    }

    private void mockToSummary(Role... roles) {
        for (Role role : roles) {
            when(roleMapper.toSummary(role)).thenReturn(new RoleSummaryDTO().id(role.getUuid()).name(role.getName()));
        }
    }
}
