package com.marcoaga02.modularhub.core.domain.repository;

import com.marcoaga02.modularhub.core.domain.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long>, JpaSpecificationExecutor<Language> {

    Optional<Language> findByUuid(String uuid);

}
