package rw.utility.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.utility.billing.entity.Notification;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByCustomerEmail(String email, Pageable pageable);
    Optional<Notification> findTopByCustomerIdAndMessageOrderByCreatedAtDesc(UUID customerId, String message);
}
