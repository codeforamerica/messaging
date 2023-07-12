package org.codeforamerica.messaging.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE, ANNOTATION_TYPE, TYPE_USE })
@Retention(RUNTIME)
@Constraint(validatedBy = { MessageContentsValidator.class })
@Documented
public @interface ValidMessageContents {
    String message() default "{org.codeforamerica.models.Message.invalidMessageContents}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
