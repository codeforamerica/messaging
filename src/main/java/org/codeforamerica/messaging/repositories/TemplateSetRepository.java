package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.TemplateSet;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateSetRepository extends CrudRepository<TemplateSet, Long> {
    TemplateSet findFirstByNameIgnoreCase(String name);
}