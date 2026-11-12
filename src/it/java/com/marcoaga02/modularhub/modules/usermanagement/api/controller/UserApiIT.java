package com.marcoaga02.modularhub.modules.usermanagement.api.controller;

import com.marcoaga02.modularhub.config.BaseApiIT;
import com.marcoaga02.modularhub.core.api.dto.GenderDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityGroupDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserResponseDTO;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.GlobalExceptionHandler;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.core.domain.repository.LanguageRepository;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.core.util.constant.PaginationHeaders;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCreateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserUpdateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserManagementExceptionCodes;
import com.marcoaga02.modularhub.modules.usermanagement.util.constant.UserRoles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static com.marcoaga02.modularhub.fixture.UserFixtures.language;
import static com.marcoaga02.modularhub.fixture.UserFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserApiIT extends BaseApiIT {

    private static final String USERS = "/users";
    private static final String USER = "/users/{uuid}";
    private static final String ADMIN_FULL_NAME = "firstname-admin lastname-admin";
    private static final String NEW_IDENTITY_ID = "new-identity-id";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Language italian;

    private User admin;

    private User mario;

    @BeforeEach
    void setUp() {
        italian = languageRepository.save(language("it-IT", true));
        admin = userRepository.save(user("admin", italian));
        mario = userRepository.save(user("mario", italian));
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
        languageRepository.deleteAll();
    }

    @Nested
    class Security {

        @Test
        void users_shouldReturnUnauthorized_whenTokenIsMissing() throws Exception {
            mockMvc.perform(get(USERS))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Access.UNAUTHORIZED));
        }

        @Test
        void users_shouldReturnForbidden_whenUserManagementRoleIsMissing() throws Exception {
            mockMvc.perform(get(USERS).with(jwtOf(admin.getIdentityId())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Access.DENIED));
        }
    }

    @Nested
    class GetAllUsers {

        @Test
        void getAllUsers_shouldReturnFilteredSortedPageWithPaginationHeaders() throws Exception {
            userRepository.save(user("maria", italian));

            mockMvc.perform(get(USERS).with(asAdmin())
                            .param("text", "MAR")
                            .param("page", "0")
                            .param("size", "1")
                            .param("sort", "firstname,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].username", contains("username-mario")))
                    .andExpect(header().string(PaginationHeaders.TOTAL_COUNT, "2"))
                    .andExpect(header().string(PaginationHeaders.TOTAL_PAGES, "2"))
                    .andExpect(header().string(PaginationHeaders.CURRENT_PAGE, "0"))
                    .andExpect(header().string(PaginationHeaders.PAGE_SIZE, "1"));
        }
    }

    @Nested
    class GetUserByUuid {

        @Test
        void getUserByUuid_shouldReturnUserWithIdentityGroupsAndAuditFullNames() throws Exception {
            String uuid = createUserAsAdmin();
            when(identityProvider.getUserById(NEW_IDENTITY_ID)).thenReturn(identityUserWithGroup());

            mockMvc.perform(get(USER, uuid).with(asAdmin()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(uuid))
                    .andExpect(jsonPath("$.language.id").value(italian.getUuid()))
                    .andExpect(jsonPath("$.groups[0].name").value("group"))
                    .andExpect(jsonPath("$.audit.createdBy").value(ADMIN_FULL_NAME))
                    .andExpect(jsonPath("$.audit.updatedBy").value(ADMIN_FULL_NAME))
                    .andExpect(jsonPath("$.audit.createdOn").isNotEmpty());
        }

        @Test
        void getUserByUuid_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
            mockMvc.perform(get(USER, "unknown").with(asAdmin()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value(UserManagementExceptionCodes.Model.User.NOT_FOUND));
        }
    }

    @Nested
    class CreateUser {

        @Test
        void createUser_shouldPersistUserAuditedWithCurrentIdentity_whenRequestIsValid() throws Exception {
            String uuid = createUserAsAdmin();

            User saved = userRepository.findByUuidAndDeletedOnIsNull(uuid).orElseThrow();
            assertThat(saved)
                    .extracting(User::getIdentityId, User::getUsername, User::getCreatedBy)
                    .containsExactly(NEW_IDENTITY_ID, "username-new", admin.getIdentityId());
            assertThat(saved.getCreatedOn()).isNotNull();
        }

        @Test
        void createUser_shouldReturnConflictWithValue_whenUsernameIsTaken() throws Exception {
            mockMvc.perform(json(post(USERS), createRequest().username(mario.getUsername())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode")
                            .value(UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_USERNAME))
                    .andExpect(jsonPath("$.params.value").value(mario.getUsername()));

            verifyNoInteractions(identityProvider);
        }

        @Test
        void createUser_shouldReturnBadRequestWithInvalidFields_whenBodyIsInvalid() throws Exception {
            mockMvc.perform(json(post(USERS), createRequest().password(null).email("not-an-email")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Validation.INVALID_FIELDS))
                    .andExpect(jsonPath("$.params." + GlobalExceptionHandler.FIELDS_PARAM,
                            containsInAnyOrder("password", "email")));

            verifyNoInteractions(identityProvider);
        }

        @Test
        void createUser_shouldDeleteIdentityUserAndPersistNothing_whenSaveFails() throws Exception {
            when(identityProvider.createUser(any())).thenReturn(NEW_IDENTITY_ID);

            mockMvc.perform(json(post(USERS), createRequest().taxIdNumber("X".repeat(300))))
                    .andExpect(status().isInternalServerError());

            verify(identityProvider).deleteUser(NEW_IDENTITY_ID);
            assertThat(userRepository.findByIdentityId(NEW_IDENTITY_ID)).isEmpty();
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void updateUser_shouldPersistChangesAuditedWithCurrentIdentity() throws Exception {
            mockMvc.perform(json(put(USER, mario.getUuid()), updateRequest(mario).firstname("Luigi")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstname").value("Luigi"));

            User updated = userRepository.findByUuidAndDeletedOnIsNull(mario.getUuid()).orElseThrow();
            assertThat(updated)
                    .extracting(User::getFirstname, User::getUpdatedBy)
                    .containsExactly("Luigi", admin.getIdentityId());
        }

        @Test
        void updateUser_shouldReturnConflict_whenEmailBelongsToAnotherUser() throws Exception {
            mockMvc.perform(json(put(USER, mario.getUuid()), updateRequest(mario).email(admin.getEmail())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode")
                            .value(UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_EMAIL));

            verify(identityProvider, never()).updateUser(anyString(), any());
        }
    }

    @Nested
    class DeleteUserByUuid {

        @Test
        void deleteUserByUuid_shouldSoftDeleteUser_whenUserIsNotTheCurrentOne() throws Exception {
            mockMvc.perform(delete(USER, mario.getUuid()).with(asAdmin()))
                    .andExpect(status().isNoContent());

            User deleted = userRepository.findByIdentityId(mario.getIdentityId()).orElseThrow();
            assertThat(deleted.getDeletedBy()).isEqualTo(admin.getIdentityId());
            assertThat(deleted.getDeletedOn()).isNotNull();
            verify(identityProvider).deleteUser(mario.getIdentityId());
        }

        @Test
        void deleteUserByUuid_shouldReturnForbiddenAndKeepUser_whenUserDeletesItself() throws Exception {
            mockMvc.perform(delete(USER, admin.getUuid()).with(asAdmin()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Model.Account.SELF_DELETION));

            assertThat(userRepository.findByUuidAndDeletedOnIsNull(admin.getUuid())).isPresent();
            verify(identityProvider, never()).deleteUser(anyString());
        }
    }

    @Nested
    class ResetPassword {

        @Test
        void resetPassword_shouldResetIdentityUserPassword() throws Exception {
            mockMvc.perform(post(USER + "/reset-password", mario.getUuid()).with(asAdmin()))
                    .andExpect(status().isNoContent());

            verify(identityProvider).resetPassword(mario.getIdentityId());
        }
    }

    private String createUserAsAdmin() throws Exception {
        when(identityProvider.createUser(any())).thenReturn(NEW_IDENTITY_ID);

        String body = mockMvc.perform(json(post(USERS), createRequest()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(body).get("id").asString();
    }

    private RequestPostProcessor asAdmin() {
        return jwtOf(admin.getIdentityId(), UserRoles.USER_MANAGEMENT);
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, Object body) {
        return request.with(asAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    private UserCreateRequestDTO createRequest() {
        return new UserCreateRequestDTO()
                .firstname("firstname-new")
                .lastname("lastname-new")
                .gender(GenderDTO.F)
                .languageId(italian.getUuid())
                .taxIdNumber("TAX-new")
                .email("new@test.com")
                .username("username-new")
                .password("password")
                .groupIds(List.of("group-id"))
                .enabled(true);
    }

    private UserUpdateRequestDTO updateRequest(User user) {
        return new UserUpdateRequestDTO()
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .gender(GenderDTO.M)
                .languageId(italian.getUuid())
                .taxIdNumber(user.getTaxIdNumber())
                .email(user.getEmail())
                .username(user.getUsername())
                .enabled(true);
    }

    private static IdentityUserResponseDTO identityUserWithGroup() {
        IdentityUserResponseDTO identityUser = new IdentityUserResponseDTO();
        identityUser.setId(NEW_IDENTITY_ID);
        identityUser.setGroups(List.of(new IdentityGroupDTO().id("group-id").name("group")));
        return identityUser;
    }
}
