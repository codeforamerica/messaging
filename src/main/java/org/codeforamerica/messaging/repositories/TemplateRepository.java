package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.Template;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends CrudRepository<Template, Long> {
    Template findFirstByNameAndLanguageAndVariant(String name, String language, String variant);
}