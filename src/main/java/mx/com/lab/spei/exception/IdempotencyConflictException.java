package mx.com.lab.spei.exception;

import java.util.UUID;

public class IdempotencyConflictException extends SpeiBusinessException {

    private final UUID idempotencyKey;

    public IdempotencyConflictException(UUID idempotencyKey) {
        super("A request with this Idempotency-Key is already in progress: " + idempotencyKey);
        this.idempotencyKey = idempotencyKey;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }
}
