package com.marcoaga02.modularhub.core.config;

import com.marcoaga02.modularhub.core.domain.model.Role;
import com.marcoaga02.modularhub.core.domain.repository.RoleRepository;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.Optional;

import static com.marcoaga02.modularhub.fixture.UserFixtures.adminRole;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemRoleInitializerTest {

    private static final DefaultApplicationArguments ARGS = new DefaultApplicationArguments();

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private SystemRoleInitializer initializer;

    @Test
    void run_shouldCreateAdminSystemRole_whenItDoesNotExist() {
        when(roleRepository.findByCode(SecurityConstants.ADMIN_ROLE_CODE)).thenReturn(Optional.empty());

        initializer.run(ARGS);

        ArgumentCaptor<Role> saved = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(saved.capture());
        assertThat(saved.getValue())
                .extracting(Role::getCode, Role::getName, Role::isSystem)
                .containsExactly(SecurityConstants.ADMIN_ROLE_CODE, SecurityConstants.ADMIN_ROLE_CODE, true);
        assertThat(saved.getValue().getPermissions()).isEmpty();
    }

    @Test
    void run_shouldNotSaveAnything_whenAdminRoleAlreadyExists() {
        when(roleRepository.findByCode(SecurityConstants.ADMIN_ROLE_CODE)).thenReturn(Optional.of(adminRole()));

        initializer.run(ARGS);

        verify(roleRepository, never()).save(any());
    }
}
