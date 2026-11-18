package com.marcoaga02.modularhub.modules.usermanagement.api.controller;

import com.marcoaga02.modularhub.core.api.dto.GenderDTO;
import com.marcoaga02.modularhub.core.api.dto.LanguageDTO;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.GlobalExceptionHandler;
import com.marcoaga02.modularhub.core.api.exception.account.SelfDeletionException;
import com.marcoaga02.modularhub.core.util.constant.PaginationHeaders;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCreateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCriteriaDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserUpdateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserManagementExceptionCodes;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.UserAlreadyExistsException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.UserNotFoundException;
import com.marcoaga02.modularhub.modules.usermanagement.domain.service.UserService;
import com.marcoaga02.modularhub.modules.usermanagement.security.UserPermissions;
import com.marcoaga02.modularhub.config.BaseControllerTest;
import com.marcoaga02.modularhub.modules.usermanagement.util.constant.UserRoles;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(UserPermissions.class)
class UserControllerTest extends BaseControllerTest {

    private static final String USER_ID = "user-id";
    private static final String USERS = "/users";
    private static final String USER = "/users/{uuid}";
    private static final String FIELDS = "$.params." + GlobalExceptionHandler.FIELDS_PARAM;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Nested
    class GetAllUsers {

        @Test
        void getAllUsers_shouldPassTextAndPaginationToServiceAndReturnPaginationHeaders() throws Exception {
            Pageable pageable = PageRequest.of(1, 2, Sort.by("lastname").descending());
            Page<UserResponseDTO> page = new PageImpl<>(List.of(user()), pageable, 3);
            when(userService.getAllUsers(new UserCriteriaDTO("mario"), pageable)).thenReturn(page);

            perform(get(USERS)
                            .param("text", "mario")
                            .param("page", "1")
                            .param("size", "2")
                            .param("sort", "lastname,desc"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(USER_ID))
                    .andExpect(header().string(PaginationHeaders.TOTAL_COUNT, "3"))
                    .andExpect(header().string(PaginationHeaders.TOTAL_PAGES, "2"))
                    .andExpect(header().string(PaginationHeaders.CURRENT_PAGE, "1"))
                    .andExpect(header().string(PaginationHeaders.PAGE_SIZE, "2"));

            verify(userService).getAllUsers(new UserCriteriaDTO("mario"), pageable);
        }

        @Test
        void getAllUsers_shouldUseDefaultPaginationAndNoText_whenNoParameterIsGiven() throws Exception {
            when(userService.getAllUsers(any(), any())).thenReturn(Page.empty());

            perform(get(USERS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(userService).getAllUsers(eq(new UserCriteriaDTO(null)), eq(PageRequest.of(0, 20)));
        }
    }

    @Nested
    class GetUserByUuid {

        @Test
        void getUserByUuid_shouldReturnOkWithUser_whenUserExists() throws Exception {
            when(userService.getUserByUuid(USER_ID)).thenReturn(user());

            perform(get(USER, USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(USER_ID))
                    .andExpect(jsonPath("$.username").value("mario.rossi"));

            verify(userService).getUserByUuid(USER_ID);
        }

        @Test
        void getUserByUuid_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
            when(userService.getUserByUuid(USER_ID)).thenThrow(new UserNotFoundException(USER_ID));

            perform(get(USER, USER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value(UserManagementExceptionCodes.Model.User.NOT_FOUND));
        }
    }

    @Nested
    class CreateUser {

        @Test
        void createUser_shouldReturnCreatedWithUser_whenRequestIsValid() throws Exception {
            UserCreateRequestDTO request = createRequest();
            when(userService.createUser(request)).thenReturn(user());

            perform(json(post(USERS), request))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(USER_ID));

            verify(userService).createUser(request);
        }

        @Test
        void createUser_shouldReturnBadRequestWithInvalidFields_whenPasswordIsMissing() throws Exception {
            perform(json(post(USERS), createRequest().password(null)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Validation.INVALID_FIELDS))
                    .andExpect(jsonPath(FIELDS).value("password"));

            verifyNoInteractions(userService);
        }

        @Test
        void createUser_shouldReturnBadRequestWithInvalidFields_whenEmailIsMalformed() throws Exception {
            perform(json(post(USERS), createRequest().email("not-an-email")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath(FIELDS).value("email"));

            verifyNoInteractions(userService);
        }

        @Test
        void createUser_shouldReturnBadRequestWithInvalidFields_whenRequiredTextIsBlank() throws Exception {
            perform(json(post(USERS), createRequest().firstname(" ").username("")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath(FIELDS, containsInAnyOrder("firstname", "username")));

            verifyNoInteractions(userService);
        }

        @Test
        void createUser_shouldReturnBadRequestWithInvalidFields_whenLanguageIsMissing() throws Exception {
            perform(json(post(USERS), createRequest().language(null)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Validation.INVALID_FIELDS))
                    .andExpect(jsonPath(FIELDS).value("language"));

            verifyNoInteractions(userService);
        }

        @Test
        void createUser_shouldReturnBadRequest_whenLanguageIsNotSupported() throws Exception {
            String body = objectMapper.writeValueAsString(createRequest()).replace("\"en\"", "\"fr\"");

            perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Validation.INVALID_REQUEST));

            verifyNoInteractions(userService);
        }

        @Test
        void createUser_shouldReturnBadRequest_whenBodyIsMalformed() throws Exception {
            perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content("{"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Validation.INVALID_REQUEST));

            verifyNoInteractions(userService);
        }

        @Test
        void createUser_shouldReturnConflictWithValue_whenUserAlreadyExists() throws Exception {
            UserCreateRequestDTO request = createRequest();
            when(userService.createUser(request)).thenThrow(UserAlreadyExistsException.username("mario.rossi"));

            perform(json(post(USERS), request))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode")
                            .value(UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_USERNAME))
                    .andExpect(jsonPath("$.params.value").value("mario.rossi"));
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void updateUser_shouldReturnOkWithUser_whenRequestIsValid() throws Exception {
            UserUpdateRequestDTO request = updateRequest();
            when(userService.updateUser(USER_ID, request)).thenReturn(user());

            perform(json(put(USER, USER_ID), request))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(USER_ID));

            verify(userService).updateUser(USER_ID, request);
        }

        @Test
        void updateUser_shouldReturnBadRequestWithInvalidFields_whenRequiredFieldsAreMissing() throws Exception {
            perform(json(put(USER, USER_ID), new UserUpdateRequestDTO()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Validation.INVALID_FIELDS));

            verifyNoInteractions(userService);
        }

        @Test
        void updateUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
            UserUpdateRequestDTO request = updateRequest();
            when(userService.updateUser(USER_ID, request)).thenThrow(new UserNotFoundException(USER_ID));

            perform(json(put(USER, USER_ID), request))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value(UserManagementExceptionCodes.Model.User.NOT_FOUND));
        }

        @Test
        void updateUser_shouldReturnConflictWithValue_whenEmailBelongsToAnotherUser() throws Exception {
            UserUpdateRequestDTO request = updateRequest();
            when(userService.updateUser(USER_ID, request))
                    .thenThrow(UserAlreadyExistsException.email("mario@test.com"));

            perform(json(put(USER, USER_ID), request))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode")
                            .value(UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_EMAIL))
                    .andExpect(jsonPath("$.params.value").value("mario@test.com"));
        }
    }

    @Nested
    class DeleteUserByUuid {

        @Test
        void deleteUserByUuid_shouldReturnNoContent_whenUserIsDeleted() throws Exception {
            perform(delete(USER, USER_ID))
                    .andExpect(status().isNoContent());

            verify(userService).deleteUser(USER_ID);
        }

        @Test
        void deleteUserByUuid_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
            doThrow(new UserNotFoundException(USER_ID)).when(userService).deleteUser(USER_ID);

            perform(delete(USER, USER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value(UserManagementExceptionCodes.Model.User.NOT_FOUND));
        }

        @Test
        void deleteUserByUuid_shouldReturnForbidden_whenUserDeletesItself() throws Exception {
            doThrow(new SelfDeletionException("identity-id")).when(userService).deleteUser(USER_ID);

            perform(delete(USER, USER_ID))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Model.Account.SELF_DELETION));
        }
    }

    @Nested
    class ResetPassword {

        @Test
        void resetPassword_shouldReturnNoContent_whenEmailIsSent() throws Exception {
            perform(post(USER + "/reset-password", USER_ID))
                    .andExpect(status().isNoContent());

            verify(userService).resetPassword(USER_ID);
        }

        @Test
        void resetPassword_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
            doThrow(new UserNotFoundException(USER_ID)).when(userService).resetPassword(USER_ID);

            perform(post(USER + "/reset-password", USER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value(UserManagementExceptionCodes.Model.User.NOT_FOUND));
        }
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, Object body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
    }

    private static UserResponseDTO user() {
        return new UserResponseDTO().id(USER_ID).username("mario.rossi");
    }

    private static UserCreateRequestDTO createRequest() {
        return new UserCreateRequestDTO()
                .firstname("Mario")
                .lastname("Rossi")
                .gender(GenderDTO.M)
                .language(LanguageDTO.EN)
                .taxIdNumber("TAX")
                .email("mario@test.com")
                .username("mario.rossi")
                .password("password")
                .groupIds(List.of("group-id"))
                .enabled(true);
    }

    private static UserUpdateRequestDTO updateRequest() {
        return new UserUpdateRequestDTO()
                .firstname("Mario")
                .lastname("Rossi")
                .gender(GenderDTO.M)
                .language(LanguageDTO.EN)
                .taxIdNumber("TAX")
                .email("mario@test.com")
                .username("mario.rossi")
                .groupIds(List.of("group-id"))
                .enabled(true);
    }

    private ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
        return performWithRoles(request, UserRoles.USER_MANAGEMENT);
    }
}
