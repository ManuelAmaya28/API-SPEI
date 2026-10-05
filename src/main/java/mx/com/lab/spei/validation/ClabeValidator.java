package mx.com.lab.spei.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates the check digit (18th digit) of a CLABE number using the ABM algorithm.
 *
 * <p>Algorithm:</p>
 * <ol>
 *   <li>Weights sequence: {@code [3, 7, 1]} cycled over positions 0–16.</li>
 *   <li>For each digit at position {@code i} (0 to 16):
 *       {@code sum += (digit × WEIGHTS[i % 3]) % 10}</li>
 *   <li>Expected check digit: {@code (10 - (sum % 10)) % 10}</li>
 *   <li>Compare expected check digit to the actual 18th digit (index 17).</li>
 * </ol>
 *
 * <p>Returns {@code false} if the value is {@code null} or does not match {@code \d{18}}.
 * Returns {@code true} if and only if the computed check digit matches the 18th digit.</p>
 *
 * <p>Requirements: 2.8, 2.9</p>
 */
public class ClabeValidator implements ConstraintValidator<ValidClabe, String> {

    private static final int[] WEIGHTS = {3, 7, 1};

    @Override
    public void initialize(ValidClabe constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || !value.matches("\\d{18}")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            int digit = value.charAt(i) - '0';
            sum += (digit * WEIGHTS[i % 3]) % 10;
        }

        int expectedCheckDigit = (10 - (sum % 10)) % 10;
        int actualCheckDigit = value.charAt(17) - '0';

        return expectedCheckDigit == actualCheckDigit;
    }
}
