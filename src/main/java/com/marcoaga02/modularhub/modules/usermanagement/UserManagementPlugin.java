package com.marcoaga02.modularhub.modules.usermanagement;

import com.marcoaga02.modularhub.core.ModulePlugin;
import com.marcoaga02.modularhub.modules.usermanagement.util.constant.UserRoles;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserManagementPlugin implements ModulePlugin {

    @Override
    public String getPath() {
        return "user-management";
    }

    @Override
    public String getName() {
        return "user-management";
    }

    @Override
    public String getDescription() {
        return "modules.user-management.description";
    }

    @Override
    public String getIcon() {
        return "pi pi-user";
    }

    @Override
    public List<String> getRequiredRoles() {
        return List.of(
                UserRoles.USER_MANAGEMENT
        );
    }
}
