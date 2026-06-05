package rw.utility.billing.service;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.utility.billing.dto.CoreDtos.*;
import rw.utility.billing.entity.Meter;
import rw.utility.billing.exception.*;
import rw.utility.billing.repository.MeterRepository;

import java.util.UUID;

@Service
public class MeterService {
    private final MeterRepository meters;
    private final CustomerService customers;
    private final AuditService audit;

    public MeterService(MeterRepository meters, CustomerService customers, AuditService audit) {
        this.meters = meters;
        this.customers = customers;
        this.audit = audit;
    }

    public Page<MeterResponse> list(String search, Pageable pageable) {
        Page<Meter> page = search == null || search.isBlank() ? meters.findAll(pageable) : meters.findByMeterNumberContainingIgnoreCase(search, pageable);
        return page.map(this::toResponse);
    }

    public MeterResponse get(UUID id) { return toResponse(find(id)); }

    @Transactional
    public MeterResponse create(MeterRequest request) {
        if (meters.existsByMeterNumber(request.meterNumber())) throw new BadRequestException("Meter number is already registered.");
        Meter m = new Meter();
        apply(m, request);
        meters.save(m);
        audit.record("CREATE", "Meter", m.getId());
        return toResponse(m);
    }

    @Transactional
    public MeterResponse update(UUID id, MeterRequest request) {
        Meter m = find(id);
        if (!m.getMeterNumber().equals(request.meterNumber()) && meters.existsByMeterNumber(request.meterNumber())) throw new BadRequestException("Meter number is already registered.");
        apply(m, request);
        audit.record("UPDATE", "Meter", id);
        return toResponse(m);
    }

    @Transactional
    public void delete(UUID id) {
        meters.delete(find(id));
        audit.record("DELETE", "Meter", id);
    }

    Meter find(UUID id) {
        return meters.findById(id).orElseThrow(() -> new NotFoundException("Meter was not found."));
    }

    private void apply(Meter m, MeterRequest r) {
        m.setCustomer(customers.find(r.customerId()));
        m.setMeterNumber(r.meterNumber());
        m.setMeterType(r.meterType());
        m.setInstallationDate(r.installationDate());
        m.setStatus(r.status());
    }

    private MeterResponse toResponse(Meter m) {
        return new MeterResponse(m.getId(), m.getCustomer().getId(), m.getCustomer().getFullNames(), m.getMeterNumber(), m.getMeterType(), m.getInstallationDate(), m.getStatus());
    }
}
