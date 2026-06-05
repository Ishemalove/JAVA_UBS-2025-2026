package rw.utility.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.utility.billing.entity.AuditLog;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
