package com.marcoaga02.modularhub.modules.usermanagement.api.controller;

import com.marcoaga02.modularhub.core.util.PageResponseEntity;
import com.marcoaga02.modularhub.core.validation.OnCreate;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCriteriaDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.domain.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("@userPermissions.canManageUsers()")
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(
            UserCriteriaDTO criteria,
            Pageable pageable
    ) {
        Page<UserResponseDTO> pageResult = userService.getAllUsers(criteria, pageable);
        return PageResponseEntity.fromPage(pageResult);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<UserResponseDTO> getUserByUuid(@PathVariable String uuid) {
        return ResponseEntity.ok(userService.getUserByUuid(uuid));
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Validated(OnCreate.class) @RequestBody UserRequestDTO user) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(user));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable String uuid,
            @Validated @RequestBody UserRequestDTO user
    ) {
        return ResponseEntity.ok(userService.updateUser(uuid, user));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteUserByUuid(@PathVariable String uuid) {
        userService.deleteUser(uuid);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable String uuid) {
        userService.resetPassword(uuid);

        return ResponseEntity.noContent().build();
    }

}
