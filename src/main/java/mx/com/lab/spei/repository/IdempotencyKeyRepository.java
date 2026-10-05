package mx.com.lab.spei.repository;

import mx.com.lab.spei.domain.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link IdempotencyKey} entities.
 *
 * <p>Beyond the standard CRUD operations, exposes two query methods used by
 * the idempotency-check logic in the service layer:
 * <ul>
 *   <li>{@link #findByKeyValue(UUID)} — fetches the record (if any) for a
 *       given idempotency key so the service can inspect its status and
 *       return a cached response.</li>
 *   <li>{@link #existsByKeyValue(UUID)} — cheap existence check used before
 *       attempting an insert, allowing the service to short-circuit quickly.</li>
 * </ul>
 *
 * <p>Requirements: 3.1, 3.4
 */
@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    /**
     * Returns the {@link IdempotencyKey} whose {@code key_value} matches the
     * supplied UUID, or {@link Optional#empty()} if no record exists yet.
     *
     * @param keyValue the idempotency key UUID sent by the client
     * @return an {@code Optional} wrapping the persisted record
     */
    Optional<IdempotencyKey> findByKeyValue(UUID keyValue);

    /**
     * Returns {@code true} if a row with the given {@code key_value} already
     * exists in the table.
     *
     * @param keyValue the idempotency key UUID to check
     * @return {@code true} when a matching record is found
     */
    boolean existsByKeyValue(UUID keyValue);
}
