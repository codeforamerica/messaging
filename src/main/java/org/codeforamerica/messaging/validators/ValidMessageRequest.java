package org.codeforamerica.messaging.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { MessageRequestValidator.class })
@Documented
public @interface ValidMessageRequest {
    String message() default "{org.codeforamerica.models.Message.invalidMessageRequest}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
