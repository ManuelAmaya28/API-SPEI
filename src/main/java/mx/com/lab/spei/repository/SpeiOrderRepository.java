package mx.com.lab.spei.repository;

import mx.com.lab.spei.domain.SpeiOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link SpeiOrder} entities.
 *
 * <p>Provides standard CRUD and pagination operations inherited from
 * {@link JpaRepository}. No custom queries are required at this stage;
 * persistence of a fully-populated order is handled via {@code save()}.
 *
 * <p>Requirements: 7.1, 9.1
 */
@Repository
public interface SpeiOrderRepository extends JpaRepository<SpeiOrder, UUID> {
}
