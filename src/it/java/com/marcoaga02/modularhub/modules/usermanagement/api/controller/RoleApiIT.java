package com.marcoaga02.modularhub.modules.usermanagement.api.controller;

import com.marcoaga02.modularhub.config.BaseApiIT;
import com.marcoaga02.modularhub.core.domain.repository.RoleRepository;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import com.marcoaga02.modularhub.modules.usermanagement.util.constant.UserManagementPermissions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.marcoaga02.modularhub.fixture.UserFixtures.role;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleApiIT extends BaseApiIT {

    @Autowired
    private RoleRepository roleRepository;

    @AfterEach
    void cleanUp() {
        roleRepository.deleteAll(roleRepository.findAll().stream().filter(r -> !r.isSystem()).toList());
    }

    @Test
    void getAllRoles_shouldListTheSystemRoleFirstThenTheCustomRoles() throws Exception {
        roleRepository.save(role("custom", UserManagementPermissions.USER_MANAGEMENT));

        mockMvc.perform(get("/roles").with(jwtOf("manager-identity", UserManagementPermissions.USER_MANAGEMENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value(SecurityConstants.ADMIN_ROLE_CODE))
                .andExpect(jsonPath("$[0].system").value(true))
                .andExpect(jsonPath("$[1].name").value("custom"))
                .andExpect(jsonPath("$[1].system").value(false));
    }
}
