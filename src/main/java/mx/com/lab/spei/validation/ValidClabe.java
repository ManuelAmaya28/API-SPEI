package mx.com.lab.spei.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates the check digit (18th digit) of a CLABE number using the ABM algorithm.
 *
 * <p>The CLABE (Clave Bancaria Estandarizada) is an 18-digit banking code used in Mexico.
 * The 18th digit is a control/check digit computed by applying weights [3, 7, 1] repeatedly
 * over the first 17 digits, summing {@code (digit × weight) mod 10}, and deriving the check
 * digit as {@code (10 - (sum mod 10)) mod 10}.</p>
 *
 * <p>This annotation should be applied after a {@code @Pattern(regexp = "\\d{18}")} annotation
 * so that format validation runs first and the algorithm is only applied to well-formed inputs.</p>
 *
 * <p>Requirement: 2.9</p>
 */
@Documented
@Constraint(validatedBy = ClabeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidClabe {

    String message() default "cuentaBeneficiaria has an invalid CLABE check digit";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
