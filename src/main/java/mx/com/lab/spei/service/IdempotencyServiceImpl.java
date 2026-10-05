package mx.com.lab.spei.service;

import mx.com.lab.spei.domain.IdempotencyKey;
import mx.com.lab.spei.domain.IdempotencyStatus;
import mx.com.lab.spei.domain.SpeiOrder;
import mx.com.lab.spei.dto.SpeiOrderResponse;
import mx.com.lab.spei.exception.IdempotencyConflictException;
import mx.com.lab.spei.repository.IdempotencyKeyRepository;
import mx.com.lab.spei.repository.SpeiOrderRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Default implementation of {@link IdempotencyService}.
 *
 * <p>Concurrency safety relies on the {@code UNIQUE} constraint on
 * {@code idempotency_keys.key_value}: the first request inserts a row with
 * status {@code IN_PROGRESS}; any concurrent request that tries to insert the
 * same key receives a {@link DataIntegrityViolationException} from the
 * database, which this implementation translates into
 * {@link IdempotencyConflictException} (→ HTTP 409).
 *
 * <p>Requirements: 3.1–3.5
 */
@Service
@Transactional
public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final SpeiOrderRepository speiOrderRepository;

    public IdempotencyServiceImpl(
            IdempotencyKeyRepository idempotencyKeyRepository,
            SpeiOrderRepository speiOrderRepository) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.speiOrderRepository = speiOrderRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Processing flow:
     * <ol>
     *   <li>If {@code key} is {@code null}, return {@link Optional#empty()} — no tracking.</li>
     *   <li>Look up the key in the database.</li>
     *   <li>If found with status {@code COMPLETED}: load the associated {@link SpeiOrder}
     *       and return it wrapped in an {@link Optional}.</li>
     *   <li>If found with status {@code IN_PROGRESS}: throw
     *       {@link IdempotencyConflictException}.</li>
     *   <li>If not found: insert a new row with status {@code IN_PROGRESS} using
     *       {@code saveAndFlush} so the constraint violation surfaces immediately.
     *       A {@link DataIntegrityViolationException} (concurrent duplicate race) is
     *       caught and rethrown as {@link IdempotencyConflictException}.</li>
     * </ol>
     */
    @Override
    public Optional<SpeiOrderResponse> checkAndLock(UUID key) {
        if (key == null) {
            return Optional.empty();
        }

        Optional<IdempotencyKey> existing = idempotencyKeyRepository.findByKeyValue(key);

        if (existing.isPresent()) {
            IdempotencyKey idkKey = existing.get();

            if (idkKey.getStatus() == IdempotencyStatus.COMPLETED) {
                // Return the cached response for the already-completed order
                SpeiOrder order = speiOrderRepository
                        .findById(idkKey.getSpeiOrderId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Orphaned idempotency key: no SpeiOrder found for id "
                                        + idkKey.getSpeiOrderId()));
                return Optional.of(SpeiOrderResponse.from(order));
            }

            // Status is IN_PROGRESS — another request is currently processing this key
            throw new IdempotencyConflictException(key);
        }

        // Key not seen before — try to acquire the lock
        try {
            IdempotencyKey newKey = new IdempotencyKey();
            newKey.setKeyValue(key);
            newKey.setStatus(IdempotencyStatus.IN_PROGRESS);
            newKey.setCreatedAt(Instant.now());
            idempotencyKeyRepository.saveAndFlush(newKey);
            return Optional.empty();
        } catch (DataIntegrityViolationException e) {
            // Concurrent request already inserted the same key between our SELECT and INSERT
            throw new IdempotencyConflictException(key);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Finds the key row and transitions it to {@code COMPLETED}, storing the
     * {@code SpeiOrder} identifier so subsequent retries can retrieve the cached result.
     * If {@code key} is {@code null} or the row is no longer present, this method is a no-op.
     */
    @Override
    public void complete(UUID key, SpeiOrder order) {
        if (key == null) {
            return;
        }
        idempotencyKeyRepository.findByKeyValue(key).ifPresent(ik -> {
            ik.setStatus(IdempotencyStatus.COMPLETED);
            ik.setSpeiOrderId(order.getId());
            idempotencyKeyRepository.save(ik);
        });
    }

    /**
     * {@inheritDoc}
     *
     * <p>Deletes the key row so the caller can retry with the same key after a
     * transient failure.  If {@code key} is {@code null} or the row does not
     * exist, this method is a no-op.
     */
    @Override
    public void release(UUID key) {
        if (key == null) {
            return;
        }
        idempotencyKeyRepository.findByKeyValue(key)
                .ifPresent(idempotencyKeyRepository::delete);
    }
}
