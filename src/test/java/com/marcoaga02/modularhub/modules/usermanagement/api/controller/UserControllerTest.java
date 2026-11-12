package com.marcoaga02.modularhub.modules.usermanagement.api.controller;

import com.marcoaga02.modularhub.core.api.dto.GenderDTO;
import com.marcoaga02.modularhub.core.util.constant.PaginationHeaders;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCreateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserUpdateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.user.UserNotFoundException;
import com.marcoaga02.modularhub.modules.usermanagement.domain.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserResponseDTO dto;

    @BeforeEach
    void setUp() {
        dto = new UserResponseDTO();
        dto.setId("user-uuid");
    }

    @Nested
    class GetAllUsers {

        @Test
        void getAllUsers_shouldReturnOkWithListAndPaginationHeaders_whenValidRequest() throws Exception {
            Page<UserResponseDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
            when(userService.getAllUsers(any(), any())).thenReturn(page);

            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].id").value("user-uuid"))
                    .andExpect(header().string(PaginationHeaders.TOTAL_COUNT, "1"))
                    .andExpect(header().string(PaginationHeaders.TOTAL_PAGES, "1"))
                    .andExpect(header().string(PaginationHeaders.CURRENT_PAGE, "0"))
                    .andExpect(header().string(PaginationHeaders.PAGE_SIZE, "10"));
        }
        @Test
        void getAllUsers_shouldReturnOkWithEmptyList_whenNoUsersExist() throws Exception {
            when(userService.getAllUsers(any(), any())).thenReturn(Page.empty());

            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    class GetUserByUuid {

        @Test
        void getUserByUuid_shouldReturnOk_whenUserExists() throws Exception {
            when(userService.getUserByUuid("user-uuid")).thenReturn(dto);

            mockMvc.perform(get("/users/user-uuid"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("user-uuid"));
        }
        @Test
        void getUserByUuid_shouldReturn404_whenUserNotFound() throws Exception {
            final String uuid = "not-existing";
            when(userService.getUserByUuid(uuid))
                    .thenThrow(new UserNotFoundException(uuid));

            mockMvc.perform(get("/users/not-existing"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class CreateUser {

        @Test
        void createUser_shouldReturnCreated() throws Exception {
            UserCreateRequestDTO request = buildCreateRequestDTO();

            when(userService.createUser(any())).thenReturn(dto);

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value("user-uuid"));
        }
        @Test
        void createUser_shouldReturn400_whenMissingRequiredFields() throws Exception {
            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void updateUser_shouldReturnOk_whenValidRequest() throws Exception {
            UserUpdateRequestDTO request = buildUpdateRequestDTO();

            when(userService.updateUser(eq("user-uuid"), any())).thenReturn(dto);

            mockMvc.perform(put("/users/user-uuid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("user-uuid"));
        }
        @Test
        void updateUser_shouldReturn404_whenUserNotFound() throws Exception {
            final String uuid = "not-existing";
            UserUpdateRequestDTO request = buildUpdateRequestDTO();

            when(userService.updateUser(eq(uuid), any()))
                    .thenThrow(new UserNotFoundException(uuid));

            mockMvc.perform(put("/users/not-existing")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
        @Test
        void updateUser_shouldReturn400_whenMissingRequiredFields() throws Exception {
            mockMvc.perform(put("/users/user-uuid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void deleteUser_shouldReturnNoContent_whenValidRequest() throws Exception {
            mockMvc.perform(delete("/users/user-uuid"))
                    .andExpect(status().isNoContent());

            verify(userService).deleteUser("user-uuid");
        }
        @Test
        void deleteUser_shouldReturn404_whenUserNotFound() throws Exception {
            final String uuid = "not-existing";
            doThrow(new UserNotFoundException(uuid))
                    .when(userService).deleteUser(uuid);

            mockMvc.perform(delete("/users/" + uuid))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class ResetPassword {

        @Test
        void resetPassword_shouldReturnNoContent_whenValidRequest() throws Exception {
            mockMvc.perform(post("/users/user-uuid/reset-password"))
                    .andExpect(status().isNoContent());

            verify(userService).resetPassword("user-uuid");
        }
        @Test
        void resetPassword_shouldReturn404_whenUserNotFound() throws Exception {
            final String uuid = "not-existing";
            doThrow(new UserNotFoundException(uuid))
                    .when(userService).resetPassword(uuid);

            mockMvc.perform(post("/users/" + uuid + "/reset-password"))
                    .andExpect(status().isNotFound());
        }
    }

    private UserUpdateRequestDTO buildUpdateRequestDTO() {
        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
        request.setFirstname("new firstname");
        request.setLastname("new lastname");
        request.setGender(GenderDTO.F);
        request.setLanguageId("lang-uuid");
        request.setTaxIdNumber("TAXID01");
        request.setEmail("new@mock.it");
        request.setUsername("new.username");
        request.setEnabled(false);
        return request;
    }

    private UserCreateRequestDTO buildCreateRequestDTO() {
        UserCreateRequestDTO request = new UserCreateRequestDTO();
        request.setFirstname("Mario");
        request.setLastname("Rossi");
        request.setGender(GenderDTO.M);
        request.setLanguageId("lang-uuid");
        request.setTaxIdNumber("TAXID01");
        request.setEmail("mario@mock.it");
        request.setUsername("mario.rossi");
        request.setPassword("password");
        request.setEnabled(true);
        return request;
    }

}
