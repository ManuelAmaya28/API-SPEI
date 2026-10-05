package mx.com.lab.spei.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import mx.com.lab.spei.validation.ValidClabe;

import java.math.BigDecimal;

/**
 * Inbound request body for a SPEI transfer.
 *
 * <p>Bean Validation annotations enforce all field-level constraints defined in
 * requirements 2.1–2.11. The CLABE check-digit algorithm (requirement 2.9) is
 * applied by {@link ValidClabe} <em>after</em> the {@code @Pattern} format guard,
 * so the algorithm only runs on well-formed 18-digit numeric strings.</p>
 *
 * <p>Requirements: 2.1–2.11</p>
 */
public record SpeiInboundRequest(

        /**
         * Unique tracking key assigned by the originating bank.
         * Required, max 40 characters. (Requirements 2.1, 2.2)
         */
        @NotBlank(message = "claveRastreo is required")
        @Size(max = 40, message = "claveRastreo must not exceed 40 characters")
        String claveRastreo,

        /**
         * Transfer amount in MXN.
         * Must be > 0, ≤ 999,999,999.99, with at most 2 decimal places.
         * (Requirements 2.3–2.6)
         */
        @NotNull(message = "monto is required")
        @DecimalMin(value = "0.01", message = "monto must be greater than zero")
        @DecimalMax(value = "999999999.99", message = "monto must not exceed 999999999.99")
        @Digits(integer = 9, fraction = 2, message = "monto must have at most 2 decimal places")
        BigDecimal monto,

        /**
         * Beneficiary CLABE (18-digit Mexican bank account code).
         * Format guard runs first; check-digit guard runs second.
         * (Requirements 2.7–2.9)
         */
        @NotBlank(message = "cuentaBeneficiaria is required")
        @Pattern(regexp = "\\d{18}", message = "cuentaBeneficiaria must be exactly 18 numeric digits")
        @ValidClabe(message = "cuentaBeneficiaria has an invalid CLABE check digit")
        String cuentaBeneficiaria,

        /**
         * Optional free-text description of the transfer. (Requirement 2.10)
         */
        String concepto

) {}
