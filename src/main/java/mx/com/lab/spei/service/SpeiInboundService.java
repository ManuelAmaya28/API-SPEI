package mx.com.lab.spei.service;

import mx.com.lab.spei.dto.SpeiInboundRequest;
import mx.com.lab.spei.dto.SpeiInboundResult;
import mx.com.lab.spei.dto.SpeiOrderResponse;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Primary service contract for inbound SPEI transfer processing.
 *
 * <p>The implementation orchestrates the full processing pipeline:</p>
 * <ol>
 *   <li>Idempotency check — return cached result if the key was already completed.</li>
 *   <li>Fineract account lookup by CLABE.</li>
 *   <li>Amount calculation (principal, spread, commission).</li>
 *   <li>Three sequential deposit transactions in Fineract.</li>
 *   <li>Persistence of the resulting {@code SpeiOrder}.</li>
 * </ol>
 *
 * <p>Requirements: 8.1, 9.1</p>
 */
public interface SpeiInboundService {

    /**
     * Processes an inbound SPEI transfer.
     *
     * <p>When {@code idempotencyKey} is non-null and a completed order already
     * exists for that key, the method returns the stored response with
     * {@code isIdempotentReplay = true} (caller should respond with HTTP 200).
     * For a freshly processed order {@code isIdempotentReplay} is {@code false}
     * (caller should respond with HTTP 201).</p>
     *
     * <p>If the same key is already {@code IN_PROGRESS} (concurrent duplicate),
     * an {@code IdempotencyConflictException} is thrown, which maps to HTTP 409.</p>
     *
     * @param request        validated inbound transfer request; must not be {@code null}
     * @param idempotencyKey caller-supplied idempotency key; may be {@code null}
     * @return a {@link SpeiInboundResult} containing the response and the replay flag
     */
    SpeiInboundResult process(SpeiInboundRequest request, UUID idempotencyKey);

    /**
     * Retrieves a previously processed SPEI order by its unique identifier.
     *
     * @param id the UUID of the SPEI order; must not be {@code null}
     * @return the corresponding {@link SpeiOrderResponse}
     * @throws ResponseStatusException with HTTP 404 if no order exists for {@code id}
     */
    SpeiOrderResponse findById(UUID id);
}
