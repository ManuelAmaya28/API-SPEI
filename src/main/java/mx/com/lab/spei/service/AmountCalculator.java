package mx.com.lab.spei.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Stateless component that performs the three pure amount calculations
 * required for a SPEI inbound transfer.
 *
 * <p>All methods are side-effect free and deterministic: given the same
 * inputs they always produce the same result, making them straightforward
 * to unit-test and property-test in isolation.</p>
 *
 * <p>Design properties addressed by this class:
 * <ul>
 *   <li><b>Property 5</b> – Spread calculation formula.</li>
 *   <li><b>Property 6</b> – Principal identity.</li>
 * </ul>
 * </p>
 *
 * <p>Requirements addressed by this class:
 * <ul>
 *   <li><b>Requirement 5.1</b> – Spread = monto × spread_rate, HALF_UP, 2 decimal places.</li>
 *   <li><b>Requirement 5.2</b> – Commission = configured fixed value, HALF_UP, 2 decimal places.</li>
 *   <li><b>Requirement 5.3</b> – Principal = monto without modification.</li>
 * </ul>
 * </p>
 */
@Component
public class AmountCalculator {

    /**
     * Returns the principal amount for a SPEI transfer.
     *
     * <p>The principal is defined as the raw {@code monto} value submitted by the
     * caller, scaled to exactly 2 decimal places using {@link RoundingMode#HALF_UP}.
     * No other transformation is applied.</p>
     *
     * <p><b>Property 6 – Principal identity:</b> for any valid {@code monto} M,
     * {@code calculatePrincipal(M)} SHALL equal {@code M.setScale(2, HALF_UP)}.</p>
     *
     * <p><b>Requirement 5.3:</b> THE SPEI_API SHALL treat the Principal as exactly M,
     * without modification.</p>
     *
     * @param monto the transfer amount as supplied in the request body; must be
     *              positive and have at most 2 decimal places
     * @return {@code monto} scaled to 2 decimal places using {@link RoundingMode#HALF_UP}
     */
    public BigDecimal calculatePrincipal(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the spread charge for a SPEI transfer.
     *
     * <p>The spread is computed as {@code monto × spreadRate}, then rounded to
     * exactly 2 decimal places using {@link RoundingMode#HALF_UP}.</p>
     *
     * <p><b>Property 5 – Spread calculation formula:</b> for any positive {@code monto}
     * M with at most 2 decimal places and any {@code spreadRate} R in the range
     * (0.000001, 0.999999), the result SHALL equal
     * {@code M.multiply(R).setScale(2, HALF_UP)}.</p>
     *
     * <p><b>Requirement 5.1:</b> THE SPEI_API SHALL calculate Spread as
     * {@code M × spread_rate} rounded to 2 decimal places using HALF_UP rounding mode,
     * where {@code spread_rate} is read from application configuration.</p>
     *
     * @param monto      the transfer amount; must be positive
     * @param spreadRate the configured spread rate; must be in the range
     *                   (0.000001, 0.999999) as enforced by {@code SpeiProperties}
     * @return spread amount rounded to 2 decimal places using {@link RoundingMode#HALF_UP}
     */
    public BigDecimal calculateSpread(BigDecimal monto, BigDecimal spreadRate) {
        return monto.multiply(spreadRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Returns the commission charge for a SPEI transfer.
     *
     * <p>The commission is a fixed MXN amount read from application configuration.
     * This method simply scales it to exactly 2 decimal places using
     * {@link RoundingMode#HALF_UP} to ensure a consistent representation before
     * it is submitted to Fineract and persisted.</p>
     *
     * <p><b>Requirement 5.2:</b> THE SPEI_API SHALL use the Commission value read
     * directly from application configuration, expressed in MXN, rounded to 2
     * decimal places using HALF_UP rounding mode.</p>
     *
     * @param configuredCommission the fixed commission value from {@code SpeiProperties};
     *                             must be in the range (0.01, 999999.99) as enforced at
     *                             startup by {@code @Validated} configuration binding
     * @return commission scaled to 2 decimal places using {@link RoundingMode#HALF_UP}
     */
    public BigDecimal calculateCommission(BigDecimal configuredCommission) {
        return configuredCommission.setScale(2, RoundingMode.HALF_UP);
    }
}
