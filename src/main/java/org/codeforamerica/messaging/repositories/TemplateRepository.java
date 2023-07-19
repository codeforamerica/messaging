package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.Template;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface TemplateRepository extends CrudRepository<Template, Long> {
    @Query("select t from Template t where t.status='ACTIVE'")
    Optional<Template> findFirstActiveByNameIgnoreCase(String name);
    Optional<Template> findFirstByNameIgnoreCaseOrderByCreationTimestampDesc(String name);
    Optional<Template> findFirstByNameIgnoreCaseAndVersion(String name, int version);
    Set<Template> findAllByNameIgnoreCase(String name);
    @Query("select max(t.version) from Template t where t.name=?1")
    int findMaxTemplateVersionByNameIgnoreCase(String name);
}