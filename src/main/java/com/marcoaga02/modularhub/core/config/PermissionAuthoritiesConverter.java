package com.marcoaga02.modularhub.core.config;

import com.marcoaga02.modularhub.core.domain.service.PermissionService;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class PermissionAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final PermissionService permissionService;

    public PermissionAuthoritiesConverter(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return permissionService.getEffectivePermissions(jwt.getSubject()).stream()
                .<GrantedAuthority>map(permission -> new SimpleGrantedAuthority(SecurityConstants.PERMISSION_PREFIX + permission))
                .toList();
    }
}
