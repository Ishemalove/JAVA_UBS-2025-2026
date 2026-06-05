package rw.utility.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.utility.billing.entity.Meter;
import rw.utility.billing.entity.MeterReading;

import java.util.UUID;

public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {
    boolean existsByMeterAndBillingMonthAndBillingYear(Meter meter, int billingMonth, int billingYear);
}
