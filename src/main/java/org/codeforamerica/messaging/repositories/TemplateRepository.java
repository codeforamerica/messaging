package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.Template;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateRepository extends CrudRepository<Template, Long> {
    Optional<Template> findFirstByNameIgnoreCase(String name);
}