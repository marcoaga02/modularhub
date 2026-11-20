package com.marcoaga02.modularhub.modules.usermanagement.api.mapper;

import com.marcoaga02.modularhub.core.domain.model.Role;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.RoleSummaryDTO;
import org.junit.jupiter.api.Test;

import static com.marcoaga02.modularhub.fixture.UserFixtures.adminRole;
import static com.marcoaga02.modularhub.fixture.UserFixtures.role;
import static org.assertj.core.api.Assertions.assertThat;

class RoleMapperTest {

    private final RoleMapper mapper = new RoleMapperImpl();

    @Test
    void toSummary_shouldExposeUuidAsIdNameAndSystemFlag() {
        Role role = adminRole();

        assertThat(mapper.toSummary(role))
                .extracting(RoleSummaryDTO::getId, RoleSummaryDTO::getName, RoleSummaryDTO::getSystem)
                .containsExactly(role.getUuid(), SecurityConstants.ADMIN_ROLE_CODE, true);
    }

    @Test
    void toSummary_shouldMarkCustomRolesAsNotSystem() {
        assertThat(mapper.toSummary(role("custom")).getSystem()).isFalse();
    }

    @Test
    void toSummary_shouldReturnNull_whenRoleIsNull() {
        assertThat(mapper.toSummary(null)).isNull();
    }
}
