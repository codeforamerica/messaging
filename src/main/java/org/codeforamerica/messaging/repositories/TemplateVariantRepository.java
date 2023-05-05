package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.TemplateSet;
import org.codeforamerica.messaging.models.TemplateVariant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateVariantRepository extends CrudRepository<TemplateVariant, Long> {
    TemplateVariant findFirstByTemplateSetAndLanguageAndTreatment(
            TemplateSet templateSet, String language, String treatment);
}