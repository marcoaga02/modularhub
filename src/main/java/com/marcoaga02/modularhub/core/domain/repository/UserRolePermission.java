package com.marcoaga02.modularhub.core.domain.repository;

public record UserRolePermission(String roleCode, String permission) {

    public boolean isSystemRole() {
        return roleCode != null;
    }
}
