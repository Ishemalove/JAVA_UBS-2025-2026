package rw.utility.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.utility.billing.entity.Tariff;
import rw.utility.billing.enums.MeterType;

import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

public interface TariffRepository extends JpaRepository<Tariff, UUID> {
    Optional<Tariff> findTopByMeterTypeAndEffectiveFromLessThanEqualOrderByEffectiveFromDescVersionDesc(MeterType type, YearMonth cycle);
    Optional<Tariff> findTopByMeterTypeOrderByVersionDesc(MeterType type);
}
