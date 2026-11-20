package com.marcoaga02.modularhub.core.config;

import com.marcoaga02.modularhub.core.domain.model.Role;
import com.marcoaga02.modularhub.core.domain.repository.RoleRepository;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SystemRoleInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemRoleInitializer.class);

    private final RoleRepository roleRepository;

    public SystemRoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (roleRepository.findByCode(SecurityConstants.ADMIN_ROLE_CODE).isPresent()) {
            return;
        }

        Role admin = new Role();
        admin.setCode(SecurityConstants.ADMIN_ROLE_CODE);
        admin.setName(SecurityConstants.ADMIN_ROLE_CODE);
        roleRepository.save(admin);

        LOGGER.info("System role {} initialized", SecurityConstants.ADMIN_ROLE_CODE);
    }
}
