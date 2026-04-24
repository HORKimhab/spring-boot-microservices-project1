package com.appsdeveloperblog.estore.transfers.io;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for {@link TransferEntity}.
 *
 * <p>This interface provides inherited CRUD and query operations for transfer
 * records stored in the database.
 *
 * <p>Example:
 * <pre>{@code
 * @Autowired
 * private TransferRepository transferRepository;
 *
 * TransferEntity transfer = new TransferEntity(
 *     "t-1001",
 *     "user-1",
 *     "user-2",
 *     new BigDecimal("250.00")
 * );
 *
 * transferRepository.save(transfer);
 * }</pre>
 */
public interface TransferRepository extends JpaRepository<TransferEntity, Object>{

}
