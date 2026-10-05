package mx.com.lab.spei.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import mx.com.lab.spei.domain.SpeiOrder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO returned for every successfully processed (or cached) SPEI order.
 *
 * <p>Monetary fields ({@code principal}, {@code spread}, {@code commission}) are
 * serialized as plain JSON numbers. Because {@link SpeiOrder} always stores them
 * with scale 2 via {@code BigDecimal.setScale(2, HALF_UP)}, Jackson emits exactly
 * two decimal places — satisfying requirement 8.3 without an extra serializer.</p>
 *
 * <p>{@code processedAt} is formatted as {@code yyyy-MM-dd'T'HH:mm:ss'Z'} in UTC,
 * as required by requirement 8.2.</p>
 *
 * <p>Requirements: 8.1, 8.2, 8.3</p>
 */
public record SpeiOrderResponse(

        /** Unique order identifier (UUID). */
        UUID id,

        /** Bank-assigned tracking key. */
        String claveRastreo,

        /** Beneficiary CLABE (18 digits). */
        String cuentaBeneficiaria,

        /** Fineract client identifier for the beneficiary. */
        Long fineractClientId,

        /** Fineract client name for the beneficiary. */
        String fineractClientName,

        /** Fineract savings account identifier. */
        Long fineractSavingsId,

        /** Principal amount deposited (2 decimal places). */
        BigDecimal principal,

        /** Spread amount deposited (2 decimal places). */
        BigDecimal spread,

        /** Commission amount deposited (2 decimal places). */
        BigDecimal commission,

        /** Fineract transaction ID for the principal deposit. */
        Long fineractDepositTxId,

        /** Fineract transaction ID for the spread deposit. */
        Long fineractSpreadTxId,

        /** Fineract transaction ID for the commission deposit. */
        Long fineractCommissionTxId,

        /**
         * UTC timestamp when the order was processed.
         * Serialized as ISO-8601 string: {@code yyyy-MM-dd'T'HH:mm:ss'Z'}.
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING,
                    pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    timezone = "UTC")
        Instant processedAt,

        /** Order status — always {@code "APPLIED"} for a processed order. */
        String status

) {

    /**
     * Maps a persisted {@link SpeiOrder} entity to a {@code SpeiOrderResponse}.
     *
     * @param order the entity to map; must not be {@code null}
     * @return a fully populated response DTO
     */
    public static SpeiOrderResponse from(SpeiOrder order) {
        return new SpeiOrderResponse(
                order.getId(),
                order.getClaveRastreo(),
                order.getCuentaBeneficiaria(),
                order.getFineractClientId(),
                order.getFineractClientName(),
                order.getFineractSavingsId(),
                order.getPrincipal(),
                order.getSpread(),
                order.getCommission(),
                order.getFineractDepositTxId(),
                order.getFineractSpreadTxId(),
                order.getFineractCommissionTxId(),
                order.getProcessedAt(),
                order.getStatus().name()
        );
    }
}
