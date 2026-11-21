package com.marcoaga02.modularhub.modules.usermanagement.api.controller;

import com.marcoaga02.modularhub.config.BaseControllerTest;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.RoleSummaryDTO;
import com.marcoaga02.modularhub.modules.usermanagement.domain.service.RoleService;
import com.marcoaga02.modularhub.modules.usermanagement.security.UserPermissions;
import com.marcoaga02.modularhub.modules.usermanagement.util.constant.UserManagementPermissions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@Import(UserPermissions.class)
class RoleControllerTest extends BaseControllerTest {

    private static final String ROLE_ID = "role-id";

    @MockitoBean
    private RoleService roleService;

    @Test
    void getAllRoles_shouldReturnOkWithRoles() throws Exception {
        when(roleService.getAllRoles()).thenReturn(List.of(new RoleSummaryDTO().id(ROLE_ID).name("custom").system(false)));

        performWithPermissions(get("/roles"), UserManagementPermissions.USER_MANAGEMENT)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ROLE_ID))
                .andExpect(jsonPath("$[0].name").value("custom"))
                .andExpect(jsonPath("$[0].system").value(false));

        verify(roleService).getAllRoles();
    }
}
