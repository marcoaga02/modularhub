package com.marcoaga02.modularhub.fixture;

import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.Role;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;

import java.util.HashSet;
import java.util.Set;

public final class UserFixtures {

    private UserFixtures() {
    }

    public static User user(String key, Language language) {
        AccountPreferences preferences = new AccountPreferences();
        preferences.setLanguage(language);

        User user = new User();
        user.setPreferences(preferences);
        user.setIdentityId("identity-" + key);
        user.setFirstname("firstname-" + key);
        user.setLastname("lastname-" + key);
        user.setGender(Gender.M);
        user.setMobileNumber("000-" + key);
        user.setTaxIdNumber("TAX-" + key);
        user.setEmail(key + "@test.com");
        user.setUsername("username-" + key);
        user.setEnabled(true);
        return user;
    }

    public static Role role(String name, String... permissions) {
        Role role = new Role();
        role.setName(name);
        role.setDescription("description-" + name);
        role.setPermissions(new HashSet<>(Set.of(permissions)));
        return role;
    }

    public static Role adminRole() {
        Role role = new Role();
        role.setCode(SecurityConstants.ADMIN_ROLE_CODE);
        role.setName(SecurityConstants.ADMIN_ROLE_CODE);
        return role;
    }
}
