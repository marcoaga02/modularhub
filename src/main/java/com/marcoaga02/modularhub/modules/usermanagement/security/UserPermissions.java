package com.marcoaga02.modularhub.modules.usermanagement.security;

import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import com.marcoaga02.modularhub.modules.usermanagement.util.constant.UserRoles;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userPermissions")
public class UserPermissions {

    public boolean canManageUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(SecurityConstants.ROLE_PREFIX + UserRoles.USER_MANAGEMENT));
    }

}
