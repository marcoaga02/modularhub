package com.marcoaga02.modularhub.modules.usermanagement.api.controller;

import com.marcoaga02.modularhub.config.BaseControllerSecurityTest;
import com.marcoaga02.modularhub.modules.usermanagement.domain.service.RoleService;
import com.marcoaga02.modularhub.modules.usermanagement.security.UserPermissions;
import com.marcoaga02.modularhub.modules.usermanagement.util.constant.UserManagementPermissions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@Import(UserPermissions.class)
class RoleControllerSecurityTest extends BaseControllerSecurityTest {

    @MockitoBean
    private RoleService roleService;

    @Nested
    class GetAllRoles {

        private MockHttpServletRequestBuilder request() {
            return get("/roles");
        }

        @Test
        void getAllRoles_shouldReturnOk_whenUserHasUserManagementPermission() throws Exception {
            when(roleService.getAllRoles()).thenReturn(List.of());

            performWithPermissions(request(), UserManagementPermissions.USER_MANAGEMENT).andExpect(status().isOk());
        }

        @Test
        void getAllRoles_shouldReturnForbidden_whenUserLacksUserManagementPermission() throws Exception {
            assertForbidden(request(), OTHER_PERMISSION);

            verifyNoInteractions(roleService);
        }

        @Test
        void getAllRoles_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
            assertUnauthorized(request());

            verifyNoInteractions(roleService);
        }
    }
}
