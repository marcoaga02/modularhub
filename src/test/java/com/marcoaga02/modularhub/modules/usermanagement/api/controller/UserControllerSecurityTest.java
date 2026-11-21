package com.marcoaga02.modularhub.modules.usermanagement.api.controller;

import com.marcoaga02.modularhub.config.BaseControllerSecurityTest;
import com.marcoaga02.modularhub.modules.usermanagement.domain.service.UserService;
import com.marcoaga02.modularhub.modules.usermanagement.security.UserPermissions;
import com.marcoaga02.modularhub.modules.usermanagement.util.constant.UserManagementPermissions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(UserPermissions.class)
class UserControllerSecurityTest extends BaseControllerSecurityTest {

    private static final String USER_URL = "/users/user-uuid";

    private static final String UPDATE_BODY = """
            {
                "firstname": "Mario",
                "lastname": "Rossi",
                "gender": "M",
                "language": "en",
                "taxIdNumber": "TAXID01",
                "email": "mario@mock.it",
                "username": "mario",
                "roleIds": [],
                "enabled": true
            }
            """;

    private static final String CREATE_BODY = """
            {
                "firstname": "Mario",
                "lastname": "Rossi",
                "gender": "M",
                "language": "en",
                "taxIdNumber": "TAXID01",
                "email": "mario@mock.it",
                "username": "mario",
                "password": "password",
                "roleIds": [],
                "enabled": true
            }
            """;

    @MockitoBean
    private UserService userService;

    @Nested
    class GetAllUsers {

        private MockHttpServletRequestBuilder request() {
            return get("/users");
        }

        @Test
        void getAllUsers_shouldReturnOk_whenUserHasUserManagementPermission() throws Exception {
            when(userService.getAllUsers(any(), any())).thenReturn(Page.empty());

            performWithPermissions(request(), UserManagementPermissions.USER_MANAGEMENT).andExpect(status().isOk());
        }

        @Test
        void getAllUsers_shouldReturnForbidden_whenUserLacksUserManagementPermission() throws Exception {
            assertForbidden(request(), OTHER_PERMISSION);

            verifyNoInteractions(userService);
        }

        @Test
        void getAllUsers_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
            assertUnauthorized(request());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    class GetUserByUuid {

        private MockHttpServletRequestBuilder request() {
            return get(USER_URL);
        }

        @Test
        void getUserByUuid_shouldReturnOk_whenUserHasUserManagementPermission() throws Exception {
            performWithPermissions(request(), UserManagementPermissions.USER_MANAGEMENT).andExpect(status().isOk());
        }

        @Test
        void getUserByUuid_shouldReturnForbidden_whenUserLacksUserManagementPermission() throws Exception {
            assertForbidden(request(), OTHER_PERMISSION);

            verifyNoInteractions(userService);
        }

        @Test
        void getUserByUuid_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
            assertUnauthorized(request());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    class CreateUser {

        private MockHttpServletRequestBuilder request() {
            return post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CREATE_BODY);
        }

        @Test
        void createUser_shouldReturnCreated_whenUserHasUserManagementPermission() throws Exception {
            performWithPermissions(request(), UserManagementPermissions.USER_MANAGEMENT).andExpect(status().isCreated());
        }

        @Test
        void createUser_shouldReturnForbidden_whenUserLacksUserManagementPermission() throws Exception {
            assertForbidden(request(), OTHER_PERMISSION);

            verifyNoInteractions(userService);
        }

        @Test
        void createUser_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
            assertUnauthorized(request());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    class UpdateUser {

        private MockHttpServletRequestBuilder request() {
            return put(USER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(UPDATE_BODY);
        }

        @Test
        void updateUser_shouldReturnOk_whenUserHasUserManagementPermission() throws Exception {
            performWithPermissions(request(), UserManagementPermissions.USER_MANAGEMENT).andExpect(status().isOk());
        }

        @Test
        void updateUser_shouldReturnForbidden_whenUserLacksUserManagementPermission() throws Exception {
            assertForbidden(request(), OTHER_PERMISSION);

            verifyNoInteractions(userService);
        }

        @Test
        void updateUser_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
            assertUnauthorized(request());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    class DeleteUserByUuid {

        private MockHttpServletRequestBuilder request() {
            return delete(USER_URL);
        }

        @Test
        void deleteUserByUuid_shouldReturnNoContent_whenUserHasUserManagementPermission() throws Exception {
            performWithPermissions(request(), UserManagementPermissions.USER_MANAGEMENT).andExpect(status().isNoContent());
        }

        @Test
        void deleteUserByUuid_shouldReturnForbidden_whenUserLacksUserManagementPermission() throws Exception {
            assertForbidden(request(), OTHER_PERMISSION);

            verifyNoInteractions(userService);
        }

        @Test
        void deleteUserByUuid_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
            assertUnauthorized(request());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    class ResetPassword {

        private MockHttpServletRequestBuilder request() {
            return post(USER_URL + "/reset-password");
        }

        @Test
        void resetPassword_shouldReturnNoContent_whenUserHasUserManagementPermission() throws Exception {
            performWithPermissions(request(), UserManagementPermissions.USER_MANAGEMENT).andExpect(status().isNoContent());
        }

        @Test
        void resetPassword_shouldReturnForbidden_whenUserLacksUserManagementPermission() throws Exception {
            assertForbidden(request(), OTHER_PERMISSION);

            verifyNoInteractions(userService);
        }

        @Test
        void resetPassword_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
            assertUnauthorized(request());

            verifyNoInteractions(userService);
        }
    }
}
