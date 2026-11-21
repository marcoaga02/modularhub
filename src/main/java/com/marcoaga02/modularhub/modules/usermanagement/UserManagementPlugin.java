package com.marcoaga02.modularhub.modules.usermanagement;

import com.marcoaga02.modularhub.core.ModulePlugin;
import com.marcoaga02.modularhub.modules.usermanagement.util.constant.UserManagementPermissions;
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
    public List<String> getPermissions() {
        return List.of(
                UserManagementPermissions.USER_MANAGEMENT
        );
    }
}
