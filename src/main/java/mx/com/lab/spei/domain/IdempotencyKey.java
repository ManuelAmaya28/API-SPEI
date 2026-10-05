package mx.com.lab.spei.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a stored idempotency key used to prevent duplicate
 * processing of SPEI inbound requests.
 *
 * <p>The lifecycle of a row is:
 * <pre>
 *   INSERT (IN_PROGRESS)  →  UPDATE (COMPLETED, speiOrderId set)
 * </pre>
 *
 * <p>The {@code UNIQUE} constraint on {@code key_value} — both at the column
 * level and via {@code @UniqueConstraint} — is the primary concurrency safety
 * mechanism: a concurrent duplicate request will trigger a
 * {@code DataIntegrityViolationException} that the service layer translates
 * into HTTP 409.
 *
 * <p>Requirements: 3.1, 3.4
 */
@Entity
@Table(
    name = "idempotency_keys",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_idempotency_keys_key_value",
        columnNames = "key_value"
    )
)
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_value", nullable = false, unique = true)
    private UUID keyValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IdempotencyStatus status;

    /** Populated only when {@link IdempotencyStatus#COMPLETED}; {@code null} while in progress. */
    @Column
    private UUID speiOrderId;

    @Column(nullable = false)
    private Instant createdAt;

    // -------------------------------------------------------------------------
    // No-arg constructor required by JPA
    // -------------------------------------------------------------------------

    public IdempotencyKey() {
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(UUID keyValue) {
        this.keyValue = keyValue;
    }

    public IdempotencyStatus getStatus() {
        return status;
    }

    public void setStatus(IdempotencyStatus status) {
        this.status = status;
    }

    public UUID getSpeiOrderId() {
        return speiOrderId;
    }

    public void setSpeiOrderId(UUID speiOrderId) {
        this.speiOrderId = speiOrderId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
