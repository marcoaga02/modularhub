package com.marcoaga02.modularhub.modules.usermanagement.api.controller;

import com.marcoaga02.modularhub.core.util.PageResponseEntity;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCreateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCriteriaDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserUpdateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.openapi.UserApi;
import com.marcoaga02.modularhub.modules.usermanagement.domain.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("@userPermissions.canManageUsers()")
public class UserController implements UserApi {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(String text, Pageable pageable) {
        Page<UserResponseDTO> pageResult = userService.getAllUsers(new UserCriteriaDTO(text), pageable);
        return PageResponseEntity.fromPage(pageResult);
    }

    @Override
    public ResponseEntity<UserResponseDTO> getUserByUuid(String uuid) {
        return ResponseEntity.ok(userService.getUserByUuid(uuid));
    }

    @Override
    public ResponseEntity<UserResponseDTO> createUser(UserCreateRequestDTO user) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(user));
    }

    @Override
    public ResponseEntity<UserResponseDTO> updateUser(String uuid, UserUpdateRequestDTO user) {
        return ResponseEntity.ok(userService.updateUser(uuid, user));
    }

    @Override
    public ResponseEntity<Void> deleteUserByUuid(String uuid) {
        userService.deleteUser(uuid);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> resetPassword(String uuid) {
        userService.resetPassword(uuid);

        return ResponseEntity.noContent().build();
    }

}
