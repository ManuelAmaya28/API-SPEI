package mx.com.lab.spei.domain;

/**
 * Tracks the processing state of an idempotency key stored in the
 * {@code idempotency_keys} table.
 *
 * <p>The state machine is linear:
 * <pre>
 *   IN_PROGRESS  →  COMPLETED
 * </pre>
 * A row is inserted as {@code IN_PROGRESS} when the first request for a given
 * key begins processing.  It transitions to {@code COMPLETED} once the
 * associated {@link SpeiOrder} has been successfully persisted.
 */
public enum IdempotencyStatus {

    /**
     * The request is currently being processed.  Concurrent requests carrying
     * the same key will receive HTTP 409 while a row with this status exists.
     */
    IN_PROGRESS,

    /**
     * Processing finished successfully.  The associated {@code speiOrderId}
     * is populated.  Subsequent requests with the same key will receive the
     * stored {@link SpeiOrder} response with HTTP 200.
     */
    COMPLETED
}
