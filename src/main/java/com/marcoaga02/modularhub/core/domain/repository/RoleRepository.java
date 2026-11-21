package com.marcoaga02.modularhub.core.domain.repository;

import com.marcoaga02.modularhub.core.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    List<Role> findAllByUuidIn(Collection<String> uuids);

}
