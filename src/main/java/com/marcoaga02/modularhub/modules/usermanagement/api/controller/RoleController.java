package com.marcoaga02.modularhub.modules.usermanagement.api.controller;

import com.marcoaga02.modularhub.modules.usermanagement.api.dto.RoleSummaryDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.openapi.RoleApi;
import com.marcoaga02.modularhub.modules.usermanagement.domain.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("@userPermissions.canManageUsers()")
public class RoleController implements RoleApi {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public ResponseEntity<List<RoleSummaryDTO>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }
}
