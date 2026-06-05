package rw.utility.billing.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.utility.billing.entity.Bill;
import rw.utility.billing.entity.Customer;

import java.util.Optional;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {
    Optional<Bill> findByReference(String reference);
    boolean existsByReadingId(UUID readingId);
    Page<Bill> findByCustomer(Customer customer, Pageable pageable);
    Page<Bill> findByReferenceContainingIgnoreCase(String reference, Pageable pageable);
}
