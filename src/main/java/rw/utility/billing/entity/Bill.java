package rw.utility.billing.entity;

import jakarta.persistence.*;
import rw.utility.billing.enums.BillStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bills", indexes = @Index(name = "idx_bill_reference", columnList = "reference", unique = true))
public class Bill extends BaseEntity {
    @ManyToOne(optional = false)
    private Customer customer;
    @ManyToOne(optional = false)
    private MeterReading reading;
    @Column(nullable = false, unique = true)
    private String reference;
    @Column(nullable = false)
    private int billingMonth;
    @Column(nullable = false)
    private int billingYear;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal outstandingBalance;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillStatus status = BillStatus.DRAFT;
    @Column(nullable = false)
    private LocalDate dueDate;
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public MeterReading getReading() { return reading; }
    public void setReading(MeterReading reading) { this.reading = reading; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public int getBillingMonth() { return billingMonth; }
    public void setBillingMonth(int billingMonth) { this.billingMonth = billingMonth; }
    public int getBillingYear() { return billingYear; }
    public void setBillingYear(int billingYear) { this.billingYear = billingYear; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }
    public BillStatus getStatus() { return status; }
    public void setStatus(BillStatus status) { this.status = status; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
