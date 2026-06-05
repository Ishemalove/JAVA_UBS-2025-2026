package rw.utility.billing.service;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.utility.billing.dto.CoreDtos.*;
import rw.utility.billing.entity.*;
import rw.utility.billing.enums.BillStatus;
import rw.utility.billing.exception.BadRequestException;
import rw.utility.billing.repository.*;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository payments;
    private final NotificationRepository notifications;
    private final BillingService billing;
    private final EmailService email;
    private final AuditService audit;

    public PaymentService(PaymentRepository payments, NotificationRepository notifications, BillingService billing, EmailService email, AuditService audit) {
        this.payments = payments;
        this.notifications = notifications;
        this.billing = billing;
        this.email = email;
        this.audit = audit;
    }

    public Page<PaymentResponse> list(String search, Pageable pageable) {
        Page<Payment> page = search == null || search.isBlank() ? payments.findAll(pageable) : payments.findByBillReferenceContainingIgnoreCase(search, pageable);
        return page.map(this::toResponse);
    }

    @Transactional
    public PaymentResponse record(PaymentRequest request) {
        Bill bill = billing.findByReference(request.billReference());
        if (bill.getStatus() == BillStatus.PAID) throw new BadRequestException("Bill is already fully paid.");
        if (request.amountPaid().compareTo(bill.getOutstandingBalance()) > 0) throw new BadRequestException("Payment amount cannot exceed outstanding balance.");
        Payment payment = new Payment();
        payment.setBill(bill);
        payment.setAmountPaid(request.amountPaid());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setPaymentDate(request.paymentDate());
        BigDecimal balance = bill.getOutstandingBalance().subtract(request.amountPaid());
        bill.setOutstandingBalance(balance);
        bill.setStatus(balance.compareTo(BigDecimal.ZERO) == 0 ? BillStatus.PAID : BillStatus.PARTIALLY_PAID);
        payments.save(payment);
        billing.save(bill);
        if (bill.getStatus() == BillStatus.PAID) notifyFullPayment(bill);
        audit.record("PAYMENT", "Payment", payment.getId());
        return toResponse(payment);
    }

    @Transactional
    public void delete(UUID id) {
        payments.deleteById(id);
        audit.record("DELETE", "Payment", id);
    }

    private void notifyFullPayment(Bill bill) {
        String message = "Dear " + bill.getCustomer().getFullNames() + ",\nYour " + bill.getBillingMonth() + "/" + bill.getBillingYear()
                + " utility bill of " + bill.getAmount() + " FRW has been successfully processed.";
        Notification notification = new Notification();
        notification.setCustomer(bill.getCustomer());
        notification.setMessage(message);
        notification.setSent(true);
        notifications.save(notification);
        email.sendNotification(bill.getCustomer().getEmail(), message);
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(p.getId(), p.getBill().getReference(), p.getAmountPaid(), p.getPaymentMethod(), p.getPaymentDate(), p.getBill().getOutstandingBalance());
    }
}
