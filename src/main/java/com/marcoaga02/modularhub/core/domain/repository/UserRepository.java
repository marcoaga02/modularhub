package com.marcoaga02.modularhub.core.domain.repository;

import com.marcoaga02.modularhub.core.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUuidAndDeletedOnIsNull(String uuid);

    Optional<User> findByTaxIdNumberAndDeletedOnIsNull(String taxIdNumber);

    Optional<User> findByUsernameAndDeletedOnIsNull(String username);

    Optional<User> findByEmailAndDeletedOnIsNull(String email);

    Optional<User> findByIdentityId(String identityId);

    long countByRoles_CodeAndEnabledTrueAndDeletedOnIsNull(String roleCode);

    long countByRoles_CodeAndEnabledTrueAndDeletedOnIsNullAndIdNot(String roleCode, Long excludedUserId);

    @Query("""
            select r.code, p
            from User u
            join u.roles r
            left join r.permissions p
            where u.identityId = :identityId
              and u.enabled = true
              and u.deletedOn is null
            """)
    List<UserRolePermission> findActiveRolePermissionsByIdentityId(String identityId);

}