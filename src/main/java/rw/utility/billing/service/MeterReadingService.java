package rw.utility.billing.service;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.utility.billing.dto.CoreDtos.*;
import rw.utility.billing.entity.*;
import rw.utility.billing.enums.AccountStatus;
import rw.utility.billing.exception.*;
import rw.utility.billing.repository.MeterReadingRepository;

import java.util.UUID;

@Service
public class MeterReadingService {
    private final MeterReadingRepository readings;
    private final MeterService meters;
    private final AuditService audit;

    public MeterReadingService(MeterReadingRepository readings, MeterService meters, AuditService audit) {
        this.readings = readings;
        this.meters = meters;
        this.audit = audit;
    }

    public Page<ReadingResponse> list(Pageable pageable) { return readings.findAll(pageable).map(this::toResponse); }
    public ReadingResponse get(UUID id) { return toResponse(find(id)); }

    @Transactional
    public ReadingResponse create(ReadingRequest request) {
        Meter meter = meters.find(request.meterId());
        if (meter.getStatus() != AccountStatus.ACTIVE) throw new BadRequestException("Meter is inactive and cannot receive readings.");
        if (request.currentReading().compareTo(request.previousReading()) <= 0) throw new BadRequestException("Current reading must be greater than previous reading.");
        int month = request.readingDate().getMonthValue();
        int year = request.readingDate().getYear();
        if (readings.existsByMeterAndBillingMonthAndBillingYear(meter, month, year)) throw new BadRequestException("This meter already has a reading for the selected month and year.");
        MeterReading r = new MeterReading();
        r.setMeter(meter);
        r.setPreviousReading(request.previousReading());
        r.setCurrentReading(request.currentReading());
        r.setReadingDate(request.readingDate());
        r.setBillingMonth(month);
        r.setBillingYear(year);
        readings.save(r);
        audit.record("CREATE", "MeterReading", r.getId());
        return toResponse(r);
    }

    @Transactional
    public void delete(UUID id) {
        readings.delete(find(id));
        audit.record("DELETE", "MeterReading", id);
    }

    MeterReading find(UUID id) {
        return readings.findById(id).orElseThrow(() -> new NotFoundException("Meter reading was not found."));
    }

    private ReadingResponse toResponse(MeterReading r) {
        return new ReadingResponse(r.getId(), r.getMeter().getId(), r.getMeter().getMeterNumber(), r.getPreviousReading(), r.getCurrentReading(), r.getReadingDate(), r.getBillingMonth(), r.getBillingYear());
    }
}
