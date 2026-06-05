package rw.utility.billing.service;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.utility.billing.dto.CoreDtos.*;
import rw.utility.billing.entity.*;
import rw.utility.billing.enums.*;
import rw.utility.billing.exception.*;
import rw.utility.billing.repository.BillRepository;
import rw.utility.billing.repository.NotificationRepository;

import java.math.*;
import java.time.YearMonth;
import java.util.UUID;

@Service
public class BillingService {
    private final BillRepository bills;
    private final MeterReadingService readings;
    private final TariffService tariffs;
    private final NotificationRepository notifications;
    private final EmailService emailService;
    private final AuditService audit;

    public BillingService(BillRepository bills, MeterReadingService readings, TariffService tariffs,
                          NotificationRepository notifications, EmailService emailService, AuditService audit) {
        this.bills = bills;
        this.readings = readings;
        this.tariffs = tariffs;
        this.notifications = notifications;
        this.emailService = emailService;
        this.audit = audit;
    }

    public Page<BillResponse> list(String search, Pageable pageable) {
        Page<Bill> page = search == null || search.isBlank() ? bills.findAll(pageable) : bills.findByReferenceContainingIgnoreCase(search, pageable);
        return page.map(this::toResponse);
    }

    public BillResponse get(UUID id) { return toResponse(find(id)); }

    @Transactional
    public BillResponse generate(BillRequest request) {
        MeterReading reading = readings.find(request.readingId());
        if (bills.existsByReadingId(reading.getId())) throw new BadRequestException("A bill already exists for this reading.");
        if (reading.getMeter().getCustomer().getStatus() != AccountStatus.ACTIVE) throw new BadRequestException("Inactive customers cannot receive bills.");
        YearMonth cycle = YearMonth.of(reading.getBillingYear(), reading.getBillingMonth());
        Tariff tariff = tariffs.applicable(reading.getMeter().getMeterType(), cycle);
        BigDecimal amount = calculate(reading.consumption(), tariff);
        Bill bill = new Bill();
        bill.setCustomer(reading.getMeter().getCustomer());
        bill.setReading(reading);
        bill.setBillingMonth(reading.getBillingMonth());
        bill.setBillingYear(reading.getBillingYear());
        bill.setReference("BILL-" + reading.getBillingYear() + "-" + reading.getBillingMonth() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        bill.setAmount(amount);
        bill.setOutstandingBalance(amount);
        bill.setDueDate(request.dueDate());
        bills.saveAndFlush(bill);
        sendBillNotification(bill);
        audit.record("GENERATE", "Bill", bill.getId());
        return toResponse(bill);
    }

    @Transactional
    public BillResponse approve(UUID id) {
        Bill bill = find(id);
        bill.setStatus(BillStatus.APPROVED);
        audit.record("APPROVE", "Bill", id);
        return toResponse(bill);
    }

    Bill findByReference(String reference) {
        return bills.findByReference(reference).orElseThrow(() -> new NotFoundException("Bill reference was not found."));
    }

    Bill save(Bill bill) { return bills.save(bill); }

    private Bill find(UUID id) {
        return bills.findById(id).orElseThrow(() -> new NotFoundException("Bill was not found."));
    }

    private BigDecimal calculate(BigDecimal consumption, Tariff tariff) {
        BigDecimal base;
        if (tariff.getMode() == TariffMode.FLAT) {
            base = consumption.multiply(tariff.getFlatRate());
        } else {
            base = tariff.getTiers().stream()
                    .map(t -> {
                        BigDecimal max = t.getMaxConsumption() == null ? consumption : t.getMaxConsumption();
                        BigDecimal units = consumption.min(max).subtract(t.getMinConsumption()).max(BigDecimal.ZERO);
                        return units.multiply(t.getRate());
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        BigDecimal taxed = base.add(tariff.getFixedCharge());
        return taxed.add(taxed.multiply(tariff.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP);
    }

    private void sendBillNotification(Bill bill) {
        String message = billMessage(bill);
        emailService.sendNotification(bill.getCustomer().getEmail(), message);
        Notification notification = notifications
                .findTopByCustomerIdAndMessageOrderByCreatedAtDesc(bill.getCustomer().getId(), message)
                .orElseGet(() -> {
                    Notification created = new Notification();
                    created.setCustomer(bill.getCustomer());
                    created.setMessage(message);
                    return created;
                });
        notification.setSent(true);
        notifications.save(notification);
    }

    private String billMessage(Bill bill) {
        return "Dear " + bill.getCustomer().getFullNames() + ",\nYour " + bill.getBillingMonth() + "/" + bill.getBillingYear()
                + " utility bill of " + bill.getAmount() + " FRW has been successfully processed.";
    }

    BillResponse toResponse(Bill b) {
        return new BillResponse(b.getId(), b.getReference(), b.getCustomer().getId(), b.getCustomer().getFullNames(), b.getBillingMonth(), b.getBillingYear(), b.getAmount(), b.getOutstandingBalance(), b.getStatus(), b.getDueDate());
    }
}
