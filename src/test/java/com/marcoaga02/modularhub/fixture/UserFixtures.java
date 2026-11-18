package com.marcoaga02.modularhub.fixture;

import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.User;

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
}
