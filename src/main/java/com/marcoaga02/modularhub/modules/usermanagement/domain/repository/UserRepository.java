package com.marcoaga02.modularhub.modules.usermanagement.domain.repository;

import com.marcoaga02.modularhub.modules.usermanagement.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUuidAndDeletedOnIsNull(String uuid);

    Optional<User> findByTaxIdNumberAndDeletedOnIsNull(String taxIdNumber);

    Optional<User> findByUsernameAndDeletedOnIsNull(String username);

    Optional<User> findByEmailAndDeletedOnIsNull(String email);

    Optional<User> findByIdentityId(String identityId);

}