package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.ModulePlugin;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.core.domain.repository.UserRolePermission;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final List<ModulePlugin> plugins;

    private final UserRepository userRepository;

    public PermissionService(List<ModulePlugin> plugins, UserRepository userRepository) {
        this.plugins = plugins;
        this.userRepository = userRepository;
    }

    public Set<String> getAvailablePermissions() {
        return plugins.stream()
                .flatMap(plugin -> plugin.getPermissions().stream())
                .collect(Collectors.collectingAndThen(Collectors.toCollection(TreeSet::new), Collections::unmodifiableSet));
    }

    public Set<String> getEffectivePermissions(String identityId) {
        List<UserRolePermission> grants = userRepository.findActiveRolePermissionsByIdentityId(identityId);
        Set<String> available = getAvailablePermissions();

        if (grants.stream().anyMatch(UserRolePermission::isSystemRole)) {
            return available;
        }

        return grants.stream()
                .map(UserRolePermission::permission)
                .filter(Objects::nonNull)
                .filter(available::contains)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(TreeSet::new), Collections::unmodifiableSet));
    }
}
