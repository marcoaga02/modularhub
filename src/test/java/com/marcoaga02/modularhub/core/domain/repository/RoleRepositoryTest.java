package com.marcoaga02.modularhub.core.domain.repository;

import com.marcoaga02.modularhub.config.BaseRepositoryTest;
import com.marcoaga02.modularhub.core.domain.model.Role;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static com.marcoaga02.modularhub.fixture.UserFixtures.adminRole;
import static com.marcoaga02.modularhub.fixture.UserFixtures.role;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

    private Role admin;

    private Role custom;

    @BeforeEach
    void setUp() {
        admin = roleRepository.save(adminRole());
        custom = roleRepository.save(role("Custom", "USER_MANAGEMENT", "REPORTS"));
        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    class FindByCode {

        @Test
        void findByCode_shouldReturnTheSystemRole() {
            assertThat(roleRepository.findByCode(SecurityConstants.ADMIN_ROLE_CODE))
                    .map(Role::getUuid)
                    .contains(admin.getUuid());
        }

        @Test
        void findByCode_shouldReturnEmpty_whenNoRoleHasTheCode() {
            assertThat(roleRepository.findByCode("UNKNOWN")).isEmpty();
        }
    }

    @Nested
    class FindAllByUuidIn {

        @Test
        void findAllByUuidIn_shouldReturnOnlyTheExistingRoles() {
            assertThat(roleRepository.findAllByUuidIn(List.of(admin.getUuid(), "unknown")))
                    .extracting(Role::getUuid)
                    .containsExactly(admin.getUuid());
        }
    }

    @Nested
    class Save {

        @Test
        void save_shouldRejectDuplicateNames() {
            assertThatThrownBy(() -> roleRepository.saveAndFlush(role("Custom")))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
