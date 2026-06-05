package rw.utility.billing.entity;

import jakarta.persistence.*;
import rw.utility.billing.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Payment extends BaseEntity {
    @ManyToOne(optional = false)
    private Bill bill;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amountPaid;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;
    @Column(nullable = false)
    private LocalDate paymentDate;
    public Bill getBill() { return bill; }
    public void setBill(Bill bill) { this.bill = bill; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
}
