package rw.utility.billing.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.utility.billing.entity.Meter;

import java.util.UUID;

public interface MeterRepository extends JpaRepository<Meter, UUID> {
    boolean existsByMeterNumber(String meterNumber);
    Page<Meter> findByMeterNumberContainingIgnoreCase(String meterNumber, Pageable pageable);
}
