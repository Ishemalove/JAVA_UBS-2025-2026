package rw.utility.billing.service;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.utility.billing.dto.CoreDtos.*;
import rw.utility.billing.entity.*;
import rw.utility.billing.exception.*;
import rw.utility.billing.repository.TariffRepository;

import java.time.YearMonth;
import java.util.UUID;

@Service
public class TariffService {
    private final TariffRepository tariffs;
    private final AuditService audit;

    public TariffService(TariffRepository tariffs, AuditService audit) {
        this.tariffs = tariffs;
        this.audit = audit;
    }

    public Page<TariffResponse> list(Pageable pageable) { return tariffs.findAll(pageable).map(this::toResponse); }
    public TariffResponse get(UUID id) { return toResponse(find(id)); }

    @Transactional
    public TariffResponse create(TariffRequest request) {
        YearMonth effectiveFrom = YearMonth.parse(request.effectiveFrom());
        if (!effectiveFrom.isAfter(YearMonth.now())) throw new BadRequestException("New tariffs must apply only to future billing cycles. Use a future YYYY-MM value such as " + YearMonth.now().plusMonths(1) + ".");
        Tariff t = new Tariff();
        int version = tariffs.findTopByMeterTypeOrderByVersionDesc(request.meterType()).map(x -> x.getVersion() + 1).orElse(1);
        t.setVersion(version);
        apply(t, request);
        tariffs.save(t);
        audit.record("CREATE", "Tariff", t.getId());
        return toResponse(t);
    }

    public Tariff applicable(rw.utility.billing.enums.MeterType type, YearMonth cycle) {
        return tariffs.findTopByMeterTypeAndEffectiveFromLessThanEqualOrderByEffectiveFromDescVersionDesc(type, cycle)
                .orElseThrow(() -> new BadRequestException("No tariff is configured for " + type + " and billing cycle " + cycle + "."));
    }

    @Transactional
    public void delete(UUID id) {
        tariffs.delete(find(id));
        audit.record("DELETE", "Tariff", id);
    }

    private Tariff find(UUID id) {
        return tariffs.findById(id).orElseThrow(() -> new NotFoundException("Tariff was not found."));
    }

    private void apply(Tariff t, TariffRequest r) {
        t.setMeterType(r.meterType());
        t.setMode(r.mode());
        t.setEffectiveFrom(YearMonth.parse(r.effectiveFrom()));
        t.setFlatRate(r.flatRate());
        t.setFixedCharge(r.fixedCharge());
        t.setTaxRate(r.taxRate());
        t.setLatePenaltyRate(r.latePenaltyRate());
        t.getTiers().clear();
        if (r.tiers() != null) {
            r.tiers().forEach(x -> {
                TariffTier tier = new TariffTier();
                tier.setTariff(t);
                tier.setMinConsumption(x.minConsumption());
                tier.setMaxConsumption(x.maxConsumption());
                tier.setRate(x.rate());
                t.getTiers().add(tier);
            });
        }
    }

    private TariffResponse toResponse(Tariff t) {
        return new TariffResponse(t.getId(), t.getMeterType(), t.getMode(), t.getVersion(), t.getEffectiveFrom().toString(), t.getFlatRate(), t.getFixedCharge(), t.getTaxRate(), t.getLatePenaltyRate());
    }
}
