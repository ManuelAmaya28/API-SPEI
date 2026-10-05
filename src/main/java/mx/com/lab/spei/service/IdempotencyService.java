package mx.com.lab.spei.service;

import mx.com.lab.spei.domain.SpeiOrder;
import mx.com.lab.spei.dto.SpeiOrderResponse;

import java.util.Optional;
import java.util.UUID;

/**
 * Manages idempotency lifecycle for SPEI inbound requests.
 *
 * <p>The contract for each method:
 * <ul>
 *   <li>{@link #checkAndLock(UUID)} — atomic check-and-insert gate.
 *       Returns {@link Optional#empty()} when the key is {@code null} (no
 *       idempotency tracking) or when the key is new (lock acquired).
 *       Returns the stored {@link SpeiOrderResponse} when the key is already
 *       {@code COMPLETED}.  Throws {@link mx.com.lab.spei.exception.IdempotencyConflictException}
 *       when the key is {@code IN_PROGRESS}.</li>
 *   <li>{@link #complete(UUID, SpeiOrder)} — transitions the key row from
 *       {@code IN_PROGRESS} to {@code COMPLETED} and links the resulting
 *       {@code SpeiOrder} identifier.</li>
 *   <li>{@link #release(UUID)} — removes the key row; called on processing
 *       failure so that the client may retry with the same key.</li>
 * </ul>
 *
 * <p>Requirements: 3.1–3.5
 */
public interface IdempotencyService {

    /**
     * Checks whether the supplied key has already been processed and, if not,
     * acquires an {@code IN_PROGRESS} lock.
     *
     * @param key the idempotency key UUID from the request header; may be {@code null}
     * @return {@link Optional#empty()} when the key is {@code null} or newly inserted;
     *         an {@link Optional} containing the cached {@link SpeiOrderResponse} when
     *         the key is already {@code COMPLETED}
     * @throws mx.com.lab.spei.exception.IdempotencyConflictException when the key exists
     *         with status {@code IN_PROGRESS} (concurrent duplicate)
     */
    Optional<SpeiOrderResponse> checkAndLock(UUID key);

    /**
     * Marks the idempotency key as {@code COMPLETED} and records the
     * {@code SpeiOrder} identifier so future retries can return the cached response.
     *
     * @param key   the idempotency key UUID; if {@code null} this method is a no-op
     * @param order the persisted {@link SpeiOrder} whose {@code id} is stored
     */
    void complete(UUID key, SpeiOrder order);

    /**
     * Deletes the idempotency key row, allowing the client to retry with the
     * same key after a transient failure.
     *
     * @param key the idempotency key UUID; if {@code null} this method is a no-op
     */
    void release(UUID key);
}
