package mx.com.lab.spei.dto;

/**
 * Wraps a processed SPEI order response together with a flag indicating whether
 * the result came from an idempotency cache hit.
 *
 * <p>The controller uses {@code isIdempotentReplay} to decide the HTTP status:
 * {@code 201 Created} for a freshly processed order, {@code 200 OK} for a
 * previously completed order returned via the idempotency key.</p>
 *
 * <p>Requirements: 3.1, 3.5, 8.1</p>
 *
 * @param response          the fully populated order response
 * @param isIdempotentReplay {@code true} when the response was served from the
 *                           idempotency cache; {@code false} when it was just created
 */
public record SpeiInboundResult(SpeiOrderResponse response, boolean isIdempotentReplay) {}
