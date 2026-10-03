package com.marcoaga02.modularhub.modules.usermanagement.api.controller;

import com.marcoaga02.modularhub.core.config.CorsProperties;
import com.marcoaga02.modularhub.core.config.MethodSecurityConfig;
import com.marcoaga02.modularhub.core.config.SecurityConfig;
import com.marcoaga02.modularhub.modules.usermanagement.domain.service.UserService;
import com.marcoaga02.modularhub.modules.usermanagement.security.UserPermissions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, MethodSecurityConfig.class, CorsProperties.class, UserPermissions.class})
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void getAllUsers_shouldReturnOk_whenUserHasUserManagementRole() throws Exception {
        when(userService.getAllUsers(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER_MANAGEMENT"))))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_shouldReturnForbidden_whenUserLacksUserManagementRole() throws Exception {
        mockMvc.perform(get("/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SOME_OTHER_ROLE"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void getAllUsers_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    void deleteUserByUuid_shouldReturnNoContent_whenUserHasUserManagementRole() throws Exception {
        mockMvc.perform(delete("/users/user-uuid")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER_MANAGEMENT"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUserByUuid_shouldReturnForbidden_whenUserLacksUserManagementRole() throws Exception {
        mockMvc.perform(delete("/users/user-uuid")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SOME_OTHER_ROLE"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void deleteUserByUuid_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/users/user-uuid"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

}
